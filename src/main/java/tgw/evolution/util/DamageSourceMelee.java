package tgw.evolution.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;
import tgw.evolution.init.EvolutionDamage;

import javax.annotation.Nullable;

public class DamageSourceMelee extends EntityDamageSource {

    private final EvolutionDamage.Type type;

    public DamageSourceMelee(String name, @Nullable Entity source, EvolutionDamage.Type type) {
        super(name, source);
        this.type = type;
    }

    public EvolutionDamage.Type getType() {
        return this.type;
    }
}
