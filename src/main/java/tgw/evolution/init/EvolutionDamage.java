package tgw.evolution.init;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;

import javax.annotation.Nullable;

public class EvolutionDamage {

    public static final DamageSource FALL = new DamageSource("fall_damage").setDamageBypassesArmor();
    public static final DamageSource FALLING_ROCK = new DamageSource("falling_rock");
    public static final DamageSource FALLING_SOIL = new DamageSource("falling_soil");
    public static final DamageSource FALLING_WOOD = new DamageSource("falling_wood");
    public static final DamageSource FALLING_TREE = new DamageSource("falling_tree");

    public static DamageSource causeSpearDamage(Entity source, @Nullable Entity indirectEntityIn) {
        return new IndirectEntityDamageSource("spear", source, indirectEntityIn).setProjectile();
    }

    public static DamageSource causeHookDamage(Entity source, @Nullable Entity indirectEntityIn) {
        return new IndirectEntityDamageSource("hook", source, indirectEntityIn).setProjectile();
    }

    public enum Type {
        CRUSHING("crushing"),
        GENERIC("generic"),
        PIERCING("piercing"),
        SLASHING("slashing");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
