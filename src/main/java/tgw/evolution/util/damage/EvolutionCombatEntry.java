package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionDamage;

public class EvolutionCombatEntry {
    private final float damage;
    private final DamageSource damageSrc;
    private final @Nullable String fallSuffix;

    public EvolutionCombatEntry(DamageSource damageSrcIn, float damageAmount, @Nullable String fallSuffixIn) {
        this.damageSrc = damageSrcIn;
        this.damage = damageAmount;
        this.fallSuffix = fallSuffixIn;
    }

    public float getDamage() {
        return this.damage;
    }

    public float getDamageAmount() {
        return this.damageSrc == EvolutionDamage.VOID ? Float.MAX_VALUE : this.damage;
    }

    public DamageSource getDamageSrc() {
        return this.damageSrc;
    }

    public @Nullable Component getDamageSrcDisplayName() {
        return this.damageSrc.getEntity() == null ? null : this.damageSrc.getEntity().getDisplayName();
    }

    public @Nullable String getFallSuffix() {
        return this.fallSuffix;
    }

    public boolean isLivingDamageSrc() {
        return this.damageSrc.getEntity() instanceof LivingEntity;
    }
}
