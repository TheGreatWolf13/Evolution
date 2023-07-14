package tgw.evolution.capabilities.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.network.PacketSCThirstData;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.time.Time;

public class CapabilityThirst {

    public static final CapabilityThirst CLIENT_INSTANCE = new CapabilityThirst();
    public static final int THIRST_CAPACITY = 3_000;
    public static final int HYDRATION_CAPACITY = 3_000;
    public static final int INTOXICATION = 1_000;
    public static final int INTOXICATION_II = 2_000;
    private static final float SCALE = THIRST_CAPACITY / 10.0f;
    private static final float DAILY_CONSUMPTION = 2_500.0f;
    /**
     * Bit 0: intoxicated;<br>
     * Bit 1: veryIntoxicated;<br>
     * Bit 2: extremelyIntoxicated;<br>
     * Bit 3: dehydrated;<br>
     * Bit 4: veryDehydrated;<br>
     * Bit 5: extremelyDehydrated;<br>
     */
    private byte flags;
    private float hydrationExhaustion;
    private int hydrationLevel;
    private boolean needsUpdate = true;
    private float thirstExhaustion;
    private int thirstLevel = THIRST_CAPACITY;

    public static int hydrationLevel(int amount) {
        return Mth.ceil(amount / (SCALE / 4));
    }

    public static int thirstLevel(int amount) {
        return Mth.ceil(amount / (SCALE / 2));
    }

    public void addHydrationExhaustion(float exhaustion) {
        if (this.hydrationLevel > 0) {
            this.hydrationExhaustion += exhaustion;
            while (this.hydrationExhaustion >= 1) {
                this.hydrationExhaustion -= 1;
                this.decreaseHydrationLevel();
                this.increaseThirstLevel(1);
            }
        }
    }

    public void addThirstExhaustion(float exhaustion) {
        this.thirstExhaustion += exhaustion;
        while (this.thirstExhaustion >= 1) {
            this.thirstExhaustion -= 1;
            this.decreaseThirstLevel();
        }
    }

    public void decreaseHydrationLevel() {
        if (this.hydrationLevel > 0) {
            int old = this.hydrationLevel;
            this.hydrationLevel--;
            if (hydrationLevel(old) != hydrationLevel(this.hydrationLevel)) {
                this.needsUpdate = true;
            }
        }
    }

    public void decreaseThirstLevel() {
        if (this.thirstLevel > 0) {
            int old = this.thirstLevel;
            this.thirstLevel--;
            if (thirstLevel(old) != thirstLevel(this.thirstLevel)) {
                this.needsUpdate = true;
            }
        }
    }

    public void deserializeNBT(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        this.setThirstLevel(nbt.getShort("ThirstLevel"));
        this.setHydrationLevel(nbt.getShort("HydrationLevel"));
        this.setThirstExhaustion(nbt.getFloat("ThirstExhaustion"));
        this.setHydrationExhaustion(nbt.getFloat("HydrationExhaustion"));
        this.flags = nbt.getByte("Flags");
    }

    public float getHydrationExhaustion() {
        return this.hydrationExhaustion;
    }

    public int getHydrationLevel() {
        return this.hydrationLevel;
    }

    public float getThirstExhaustion() {
        return this.thirstExhaustion;
    }

    public int getThirstLevel() {
        return this.thirstLevel;
    }

    public void increaseHydrationLevel(int amount) {
        if (amount > 0 && this.hydrationLevel < HYDRATION_CAPACITY) {
            this.setHydrationLevel(this.hydrationLevel + amount);
        }
    }

    public void increaseThirstLevel(int amount) {
        if (amount > 0 && this.thirstLevel < THIRST_CAPACITY) {
            this.setThirstLevel(this.thirstLevel + amount);
        }
    }

    public boolean isDehydrated() {
        return (this.flags & 8) != 0;
    }

    public boolean isExtremelyDehydrated() {
        return (this.flags & 32) != 0;
    }

    public boolean isExtremelyIntoxicated() {
        return (this.flags & 4) != 0;
    }

    public boolean isIntoxicated() {
        return (this.flags & 1) != 0;
    }

    public boolean isVeryDehydrated() {
        return (this.flags & 16) != 0;
    }

    public boolean isVeryIntoxicated() {
        return (this.flags & 2) != 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putShort("ThirstLevel", (short) this.thirstLevel);
        nbt.putShort("HydrationLevel", (short) this.hydrationLevel);
        nbt.putFloat("ThirstExhaustion", this.thirstExhaustion);
        nbt.putFloat("HydrationExhaustion", this.hydrationExhaustion);
        nbt.putByte("Flags", this.flags);
        return nbt;
    }

    public void set(CapabilityThirst old) {
        this.thirstLevel = old.thirstLevel;
        this.hydrationLevel = old.hydrationLevel;
        this.thirstExhaustion = old.thirstExhaustion;
        this.hydrationExhaustion = old.hydrationExhaustion;
        this.flags = old.flags;
        this.needsUpdate = true;
    }

    public void setDehydrated(boolean dehydrated) {
        if (dehydrated) {
            this.flags |= 8;
        }
        else {
            this.flags &= ~8;
        }
    }

    public void setExtremelyDehydrated(boolean extremelyDehydrated) {
        if (extremelyDehydrated) {
            this.flags |= 32;
        }
        else {
            this.flags &= ~32;
        }
    }

    public void setExtremelyIntoxicated(boolean extremelyIntoxicated) {
        if (extremelyIntoxicated) {
            this.flags |= 4;
        }
        else {
            this.flags &= ~4;
        }
    }

    public void setHydrationExhaustion(float exhaustion) {
        this.hydrationExhaustion = Math.max(exhaustion, 0);
    }

    public void setHydrationLevel(int hydration) {
        int old = this.hydrationLevel;
        this.hydrationLevel = MathHelper.clamp(hydration, 0, HYDRATION_CAPACITY);
        if (hydrationLevel(old) != hydrationLevel(this.hydrationLevel)) {
            this.needsUpdate = true;
        }
    }

    public void setIntoxicated(boolean intoxicated) {
        if (intoxicated) {
            this.flags |= 1;
        }
        else {
            this.flags &= ~1;
        }
    }

    public void setThirstExhaustion(float exhaustion) {
        this.thirstExhaustion = Math.max(exhaustion, 0);
    }

    public void setThirstLevel(int thirstLevel) {
        int old = this.thirstLevel;
        this.thirstLevel = MathHelper.clamp(thirstLevel, 0, THIRST_CAPACITY);
        if (thirstLevel(old) != thirstLevel(this.thirstLevel)) {
            this.needsUpdate = true;
        }
    }

    public void setVeryDehydrated(boolean veryDehydrated) {
        if (veryDehydrated) {
            this.flags |= 16;
        }
        else {
            this.flags &= ~16;
        }
    }

    public void setVeryIntoxicated(boolean veryIntoxicated) {
        if (veryIntoxicated) {
            this.flags |= 2;
        }
        else {
            this.flags &= ~2;
        }
    }

    public void tick(ServerPlayer player) {
        if (player.isAlive()) {
            float modifier = 0.0f;
            if (player.isSprinting()) {
                modifier += 0.15f;
            }
            if (this.hydrationLevel > 0) {
                modifier -= 0.15f;
            }
            modifier += ((PatchLivingEntity) player).getEffectHelper().getThirstMod();
            if (this.thirstLevel <= 0 && !this.isExtremelyDehydrated()) {
                this.setDehydrated(true);
                this.setVeryDehydrated(true);
                this.setExtremelyDehydrated(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.DEHYDRATION, 2, false, false, true));
            }
            else if (this.thirstLevel <= 0.1 * THIRST_CAPACITY && !this.isVeryDehydrated()) {
                this.setDehydrated(true);
                this.setVeryDehydrated(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.DEHYDRATION, 1, false, false, true));
            }
            else if (this.thirstLevel <= 0.25 * THIRST_CAPACITY && !this.isDehydrated()) {
                this.setDehydrated(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.DEHYDRATION, 0, false, false, true));
            }
            else if (this.thirstLevel > 0.25 * THIRST_CAPACITY && this.isDehydrated()) {
                this.setDehydrated(false);
                this.setVeryDehydrated(false);
                this.setExtremelyDehydrated(false);
                player.removeEffect(EvolutionEffects.DEHYDRATION);
            }
            if (this.hydrationLevel >= HYDRATION_CAPACITY && !this.isExtremelyIntoxicated()) {
                this.setExtremelyIntoxicated(true);
                this.setVeryIntoxicated(true);
                this.setIntoxicated(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.WATER_INTOXICATION, 2, false, false, true));
            }
            else if (this.hydrationLevel >= INTOXICATION_II && !this.isVeryIntoxicated()) {
                this.setVeryIntoxicated(true);
                this.setIntoxicated(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.WATER_INTOXICATION, 1, false, false, true));
            }
            else if (this.hydrationLevel >= INTOXICATION && !this.isIntoxicated()) {
                this.setIntoxicated(true);
                player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.WATER_INTOXICATION, 0, false, false, true));
            }
            else if (this.hydrationLevel <= 0 && this.isIntoxicated()) {
                this.setIntoxicated(false);
                this.setVeryIntoxicated(false);
                this.setExtremelyIntoxicated(false);
                player.removeEffect(EvolutionEffects.WATER_INTOXICATION);
            }
            this.addThirstExhaustion(DAILY_CONSUMPTION / Time.TICKS_PER_DAY * (1.0f + modifier));
            this.addHydrationExhaustion(0.9f);
        }
        else {
            this.setHydrationLevel(0);
            this.setIntoxicated(false);
            this.setVeryIntoxicated(false);
            this.setExtremelyIntoxicated(false);
            this.setDehydrated(false);
            this.setVeryDehydrated(false);
            this.setExtremelyDehydrated(false);
        }
        if (this.needsUpdate) {
            player.connection.send(new PacketSCThirstData(this));
            this.needsUpdate = false;
        }
    }
}
