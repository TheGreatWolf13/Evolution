package tgw.evolution.capabilities.health;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import tgw.evolution.patches.ILivingEntityPatch;

public class HealthStats implements IHealth {

    public static final int REGEN_TICKS = 100;
    public static final float REGEN_FACTOR = 0.01f;
    private static final float MIN_HEALTH = 1.0f;
    private int tick;

    public boolean canNaturalRegen(ServerPlayer player) {
        if (!player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            return false;
        }
        return ((ILivingEntityPatch) player).getEffectHelper().canRegen();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.tick = nbt.getByte("Tick");
    }

    public void naturalRegen(ServerPlayer player) {
        float currentHealth = Math.max(MIN_HEALTH, player.getHealth());
        player.heal(REGEN_FACTOR * currentHealth);
    }

    public void resetTick() {
        this.tick = 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("Tick", (byte) this.tick);
        return nbt;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (player.isAlive()) {
            if (this.tick < REGEN_TICKS) {
                this.tick++;
            }
            if (player.hurtTime > 0) {
                this.resetTick();
            }
            else if (this.tick >= REGEN_TICKS) {
                if (player.isHurt() && this.canNaturalRegen(player)) {
                    this.naturalRegen(player);
                    this.resetTick();
                }
            }
        }
        else {
            this.resetTick();
        }
    }
}
