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
        this.bypassMagic();
        return this;
    }

    @Override
    public DamageSourceEv bypassArmor() {
        super.bypassArmor();
        return this;
    }

    @Override
    public DamageSourceEv bypassInvul() {
        super.bypassInvul();
        return this;
    }

    public DamageSourceEv fire() {
        this.setIsFire();
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
        return "DamageSourceEv{" + this.type + "," + this.msgId + '}';
    }
}
