package tgw.evolution.util.damage;

import net.minecraft.util.DamageSource;
import tgw.evolution.init.EvolutionDamage;

public class EvDamageSource extends DamageSource {

    private final EvolutionDamage.Type type;

    public EvDamageSource(String name, EvolutionDamage.Type type) {
        super(name);
        this.type = type;
    }

    public EvolutionDamage.Type getType() {
        return this.type;
    }
}
