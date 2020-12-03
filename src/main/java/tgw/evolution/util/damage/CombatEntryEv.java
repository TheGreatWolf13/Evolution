package tgw.evolution.util.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class CombatEntryEv {
    private final DamageSource damageSrc;
    private final float damage;
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
    public ITextComponent getDamageSrcDisplayName() {
        return this.damageSrc.getTrueSource() == null ? null : this.damageSrc.getTrueSource().getDisplayName();
    }

    @Nullable
    public String getFallSuffix() {
        return this.fallSuffix;
    }

    public boolean isLivingDamageSrc() {
        return this.damageSrc.getTrueSource() instanceof LivingEntity;
    }
}
