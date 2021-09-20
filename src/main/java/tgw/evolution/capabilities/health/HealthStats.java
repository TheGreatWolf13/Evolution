package tgw.evolution.capabilities.health;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.GameRules;
import tgw.evolution.init.EvolutionEffects;

public class HealthStats implements IHealth {

    public static final int REGEN_TICKS = 100;
    public static final float REGEN_FACTOR = 0.01f;
    private int tick;

    public boolean canNaturalRegen(ServerPlayerEntity player) {
        if (!player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            return false;
        }
        return !player.hasEffect(EvolutionEffects.DEHYDRATION.get());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.tick = nbt.getByte("Tick");
    }

    public void forcedRegen(ServerPlayerEntity player) {
        if (player.hasEffect(Effects.REGENERATION)) {
            EffectInstance regenInstance = player.getEffect(Effects.REGENERATION);
            int timer = 50 >> regenInstance.getAmplifier();
            if (timer < 1) {
                timer = 1;
            }
            if (regenInstance.getDuration() % timer == 0) {
                player.setHealth(player.getHealth() + 1);
            }
        }
    }

    public void naturalRegen(ServerPlayerEntity player) {
        float currentHealth = player.getHealth();
        player.setHealth(currentHealth + REGEN_FACTOR * currentHealth);
    }

    public void resetTick() {
        this.tick = 0;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putByte("Tick", (byte) this.tick);
        return nbt;
    }

    @Override
    public void tick(ServerPlayerEntity player) {
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
            if (player.isHurt()) {
                this.forcedRegen(player);
            }
        }
        else {
            this.resetTick();
        }
    }
}
