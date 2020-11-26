package tgw.evolution.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.util.damage.EvDamageSource;
import tgw.evolution.util.damage.EvEntityDamageSource;
import tgw.evolution.util.damage.EvIndirectEntityDamageSource;

import javax.annotation.Nullable;

public final class EvolutionDamage {

    public static final DamageSource FALL = new EvDamageSource("fall_damage", Type.FALL).setDamageBypassesArmor();
    public static final DamageSource FALLING_ROCK = new EvDamageSource("falling_rock", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource FALLING_SOIL = new EvDamageSource("falling_soil", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource FALLING_WOOD = new EvDamageSource("falling_wood", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource FALLING_TREE = new EvDamageSource("falling_tree", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource IN_FIRE = new EvDamageSource("inFire", Type.FIRE).setFireDamage();
    public static final DamageSource IN_WALL = new EvDamageSource("inWall", Type.SUFFOCATION).setDamageBypassesArmor();
    public static final DamageSource ON_FIRE = new EvDamageSource("onFire", Type.FIRE).setFireDamage();

    private EvolutionDamage() {
    }

    public static DamageSource causeArrowDamage(EntityGenericProjectile arrow, @Nullable Entity trueSource) {
        return new EvIndirectEntityDamageSource("arrow", arrow, trueSource, Type.PIERCING).setProjectile();
    }

    public static DamageSource causeSpearDamage(Entity source, @Nullable Entity trueSource) {
        return new EvIndirectEntityDamageSource("spear", source, trueSource, Type.PIERCING).setProjectile();
    }

    public static DamageSource causeHookDamage(Entity source, @Nullable Entity trueSource) {
        return new EvIndirectEntityDamageSource("hook", source, trueSource, Type.PIERCING).setProjectile();
    }

    public static DamageSource causePlayerMeleeDamage(PlayerEntity player, Type type) {
        return new EvEntityDamageSource("player", player, type);
    }

    public enum Type {
        CRUSHING("crushing"),
        FALL("fall"),
        FIRE("fire"),
        GENERIC("generic"),
        PIERCING("piercing"),
        SLASHING("slashing"),
        SUFFOCATION("suffocation");

        private final String name;
        private final String translationKey;

        Type(String name) {
            this.name = name;
            this.translationKey = "evolution.tooltip.damage." + name;
        }

        public String getName() {
            return this.name;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}
