package tgw.evolution.util.damage;

import net.minecraft.util.DamageSource;
import tgw.evolution.init.EvolutionDamage;

public class DamageSourceEv extends DamageSource {

    private final EvolutionDamage.Type type;

    public DamageSourceEv(String name, EvolutionDamage.Type type) {
        super(name);
        this.type = type;
    }

    public DamageSourceEv absolute() {
        this.setDamageIsAbsolute();
        return this;
    }

    public DamageSourceEv bypassArmor() {
        this.setDamageBypassesArmor();
        return this;
    }

    public DamageSourceEv creative() {
        this.setDamageAllowedInCreativeMode();
        return this;
    }

    public DamageSourceEv fire() {
        this.setFireDamage();
        return this;
    }

    public EvolutionDamage.Type getType() {
        return this.type;
    }

    public DamageSourceEv projectile() {
        this.setProjectile();
        return this;
    }

    @Override
    public String toString() {
        return "DamageSourceEv{" + this.type + "," + this.damageType + '}';
    }
}
