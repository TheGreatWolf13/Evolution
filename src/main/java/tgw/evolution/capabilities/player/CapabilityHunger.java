package tgw.evolution.capabilities.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.network.PacketSCHungerData;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.patches.PatchPlayer;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

public class CapabilityHunger {

    public static final CapabilityHunger CLIENT_INSTANCE = new CapabilityHunger();
    public static final int HUNGER_CAPACITY = 3_000;
    public static final int SATURATION_CAPACITY = 3_000;
    public static final int OVEREAT = 1_000;
    public static final int OVEREAT_II = 2_000;
    private static final float DAILY_CONSUMPTION = 1_680.0f;
    private static final float SCALE = HUNGER_CAPACITY / 10.0f;
    /**
     * Bit 0: Overeat; <br>
     * Bit 1: Very overeat; <br>
     * Bit 2: Extremely overeat; <br>
     * Bit 3: Hungry; <br>
     * Bit 4: Very hungry; <br>
     * Bit 5: Starving; <br>
     */
    private byte flags;
    private float hungerExhaustion;
    private int hungerLevel = HUNGER_CAPACITY;
    private boolean needsUpdate = true;
    private float saturationExhaustion;
    private int saturationLevel;

    public static int hungerLevel(int amount) {
        return Mth.ceil(amount / (SCALE / 2));
    }

    public static int saturationLevel(int amount) {
        return Mth.ceil(amount / (SCALE / 4));
    }

    private void addHungerExhaustion(float exhaustion) {
        this.hungerExhaustion += exhaustion;
        while (this.hungerExhaustion >= 1) {
            this.hungerExhaustion -= 1;
            this.decreaseHungerLevel();
        }
    }

    private void addSaturationExhaustion(float exhaustion) {
        if (this.saturationLevel > 0) {
            this.saturationExhaustion += exhaustion;
            while (this.saturationExhaustion >= 1) {
                this.saturationExhaustion -= 1;
                this.decreaseSaturationLevel();
                this.increaseHungerLevel(1);
            }
        }
    }

    private void decreaseHungerLevel() {
        if (this.hungerLevel > 0) {
            int old = this.hungerLevel;
            this.hungerLevel--;
            if (hungerLevel(old) != hungerLevel(this.hungerLevel)) {
                this.needsUpdate = true;
            }
        }
    }

    private void decreaseSaturationLevel() {
        if (this.saturationLevel > 0) {
            int old = this.saturationLevel;
            this.saturationLevel--;
            if (saturationLevel(old) != saturationLevel(this.saturationLevel)) {
                this.needsUpdate = true;
            }
        }
    }

    public void deserializeNBT(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        this.setHungerLevel(nbt.getShort("HungerLevel"));
        this.setSaturationLevel(nbt.getShort("SaturationLevel"));
        this.setHungerExhaustion(nbt.getFloat("HungerExhaustion"));
        this.setSaturationExhaustion(nbt.getFloat("SaturationExhaustion"));
        this.flags = nbt.getByte("Flags");
    }

    public int getHungerLevel() {
        return this.hungerLevel;
    }

    public int getSaturationLevel() {
        return this.saturationLevel;
    }

    public void increaseHungerLevel(int amount) {
        if (amount > 0 && this.hungerLevel < HUNGER_CAPACITY) {
            this.setHungerLevel(this.hungerLevel + amount);
        }
    }

    public void increaseSaturationLevel(int amount) {
        if (amount > 0 && this.saturationLevel < SATURATION_CAPACITY) {
            this.setSaturationLevel(this.saturationLevel + amount);
        }
    }

    private boolean isExtremelyOvereat() {
        return (this.flags & 4) != 0;
    }

    private boolean isHungry() {
        return (this.flags & 8) != 0;
    }

    private boolean isOvereat() {
        return (this.flags & 1) != 0;
    }

    private boolean isStarving() {
        return (this.flags & 32) != 0;
    }

    private boolean isVeryHungry() {
        return (this.flags & 16) != 0;
    }

    private boolean isVeryOvereat() {
        return (this.flags & 2) != 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putShort("HungerLevel", (short) this.hungerLevel);
        nbt.putShort("SaturationLevel", (short) this.saturationLevel);
        nbt.putFloat("HungerExhaustion", this.hungerExhaustion);
        nbt.putFloat("SaturationExhaustion", this.saturationExhaustion);
        nbt.putByte("Flags", this.flags);
        return nbt;
    }

    public void set(CapabilityHunger old) {
        this.hungerLevel = old.hungerLevel;
        this.saturationLevel = old.saturationLevel;
        this.hungerExhaustion = old.hungerExhaustion;
        this.saturationExhaustion = old.saturationExhaustion;
        this.flags = old.flags;
        this.needsUpdate = true;
    }

    private void setExtremelyOvereat(boolean extremelyOvereat) {
        if (extremelyOvereat) {
            this.flags |= 4;
        }
        else {
            this.flags &= ~4;
        }
    }

    private void setHungerExhaustion(float exhaustion) {
        this.hungerExhaustion = Math.max(exhaustion, 0);
    }

    public void setHungerLevel(int hunger) {
        int old = this.hungerLevel;
        this.hungerLevel = MathHelper.clamp(hunger, 0, HUNGER_CAPACITY);
        if (hungerLevel(old) != hungerLevel(this.hungerLevel)) {
            this.needsUpdate = true;
        }
    }

    private void setHungry(boolean hungry) {
        if (hungry) {
            this.flags |= 8;
        }
        else {
            this.flags &= ~8;
        }
    }

    private void setOvereat(boolean overeat) {
        if (overeat) {
            this.flags |= 1;
        }
        else {
            this.flags &= ~1;
        }
    }

    private void setSaturationExhaustion(float saturationExhaustion) {
        if (saturationExhaustion < 0) {
            saturationExhaustion = 0;
        }
        this.saturationExhaustion = saturationExhaustion;
    }

    public void setSaturationLevel(int saturation) {
        int old = this.saturationLevel;
        this.saturationLevel = MathHelper.clamp(saturation, 0, SATURATION_CAPACITY);
        if (saturationLevel(old) != saturationLevel(this.saturationLevel)) {
            this.needsUpdate = true;
        }
    }

    private void setStarving(boolean starving) {
        if (starving) {
            this.flags |= 32;
        }
        else {
            this.flags &= ~32;
        }
    }

    private void setVeryHungry(boolean veryHungry) {
        if (veryHungry) {
            this.flags |= 16;
        }
        else {
            this.flags &= ~16;
        }
    }

    private void setVeryOvereat(boolean veryOvereat) {
        if (veryOvereat) {
            this.flags |= 2;
        }
        else {
            this.flags &= ~2;
        }
    }

    public void tick(ServerPlayer player) {
        if (player.isAlive()) {
            float modifier = 0.0f;
            if (((PatchPlayer) player).isMoving()) {
                modifier += 0.05f;
            }
            if (player.isSprinting()) {
                modifier += 0.15f;
            }
            if (player.isSwimming()) {
                //70% more consuming than sprinting, 1.7 * 0.15 = 0.255
                //As the player will always be sprinting when swimming, 0.255 - 0.15 = 0.105
                modifier += 0.105f;
            }
            if (player.swinging || ((PatchLivingEntity) player).isSpecialAttacking()) {
                modifier += 0.05f;
            }
            if (player.onClimbable()) {
                modifier += 0.1f;
            }
            modifier += ((PatchLivingEntity) player).getEffectHelper().getHungerMod();
            double baseMass = player.getAttributeBaseValue(EvolutionAttributes.MASS);
            double totalMass = player.getAttributeValue(EvolutionAttributes.MASS);
            double equipMass = totalMass - baseMass;
            if (equipMass > 0) {
                modifier += 0.002f * equipMass;
            }
            if (this.saturationLevel > 0) {
                modifier -= 0.15f;
            }
            if (this.hungerLevel <= 0 && !this.isStarving()) {
                this.setHungry(true);
                this.setVeryHungry(true);
                this.setStarving(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.STARVATION, 2, false, false, true));
            }
            else if (this.hungerLevel <= 0.1 * HUNGER_CAPACITY && !this.isVeryHungry()) {
                this.setHungry(true);
                this.setVeryHungry(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.STARVATION, 1, false, false, true));
            }
            else if (this.hungerLevel <= 0.25 * HUNGER_CAPACITY && !this.isHungry()) {
                this.setHungry(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.STARVATION, 0, false, false, true));
            }
            else if (this.hungerLevel > 0.25 * HUNGER_CAPACITY && this.isHungry()) {
                this.setHungry(false);
                this.setVeryHungry(false);
                this.setStarving(false);
                player.removeEffect(EvolutionEffects.STARVATION);
            }
            if (this.saturationLevel >= SATURATION_CAPACITY && !this.isExtremelyOvereat()) {
                this.setExtremelyOvereat(true);
                this.setVeryOvereat(true);
                this.setOvereat(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.OVEREAT, 2, false, false, true));
            }
            else if (this.saturationLevel >= OVEREAT_II && !this.isVeryOvereat()) {
                this.setVeryOvereat(true);
                this.setOvereat(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.OVEREAT, 1, false, false, true));
            }
            else if (this.saturationLevel >= OVEREAT && !this.isOvereat()) {
                this.setOvereat(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.OVEREAT, 0, false, false, true));
            }
            else if (this.saturationLevel <= 0 && this.isOvereat()) {
                this.setOvereat(false);
                this.setVeryOvereat(false);
                this.setExtremelyOvereat(false);
                player.removeEffect(EvolutionEffects.OVEREAT);
            }
            this.addHungerExhaustion(DAILY_CONSUMPTION / Time.TICKS_PER_DAY * (1.0f + modifier));
            this.addSaturationExhaustion(0.36f);
        }
        else {
            this.setSaturationLevel(0);
            this.setOvereat(false);
            this.setVeryOvereat(false);
            this.setExtremelyOvereat(false);
            this.setHungry(false);
            this.setVeryHungry(false);
            this.setStarving(false);
        }
        if (this.needsUpdate) {
            player.connection.send(new PacketSCHungerData(this));
            this.needsUpdate = false;
        }
    }
}
