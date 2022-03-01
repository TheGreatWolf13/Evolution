package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class CombatEntryEv {
    private final float damage;
    private final DamageSource damageSrc;
    private final String fallSuffix;

    public CombatEntryEv(DamageSource damageSrcIn, float damageAmount, String fallSuffixIn) {
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

    @Nullable
    public Component getDamageSrcDisplayName() {
        return this.damageSrc.getEntity() == null ? null : this.damageSrc.getEntity().getDisplayName();
    }

    @Nullable
    public String getFallSuffix() {
        return this.fallSuffix;
    }

    public boolean isLivingDamageSrc() {
        return this.damageSrc.getEntity() instanceof LivingEntity;
    }
}
