package tgw.evolution.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.util.damage.DamageSourceEntityIndirect;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.damage.DamageSourcePlayer;

import javax.annotation.Nullable;

public final class EvolutionDamage {

    public static final DamageSource FALL = new DamageSourceEv("fall_damage", Type.FALL).setDamageBypassesArmor();
    public static final DamageSource FALLING_ROCK = new DamageSourceEv("falling_rock", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource FALLING_SOIL = new DamageSourceEv("falling_soil", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource FALLING_WOOD = new DamageSourceEv("falling_wood", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource FALLING_TREE = new DamageSourceEv("falling_tree", Type.CRUSHING).setDamageBypassesArmor();
    public static final DamageSource IN_FIRE = new DamageSourceEv("inFire", Type.FIRE).setFireDamage();
    public static final DamageSource IN_WALL = new DamageSourceEv("inWall", Type.SUFFOCATION).setDamageBypassesArmor();
    public static final DamageSource ON_FIRE = new DamageSourceEv("onFire", Type.FIRE).setFireDamage();

    private EvolutionDamage() {
    }

    public static DamageSource causeArrowDamage(EntityGenericProjectile arrow, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("arrow", arrow, trueSource, Type.PIERCING).setProjectile();
    }

    public static DamageSource causeSpearDamage(Entity source, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("spear", source, trueSource, Type.PIERCING).setProjectile();
    }

    public static DamageSource causeHookDamage(Entity source, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("hook", source, trueSource, Type.PIERCING).setProjectile();
    }

    public static DamageSource causePlayerMeleeDamage(PlayerEntity player, Type type, Hand hand) {
        return new DamageSourcePlayer("player", player, type, hand);
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
