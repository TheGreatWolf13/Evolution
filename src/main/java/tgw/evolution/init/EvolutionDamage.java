package tgw.evolution.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.util.damage.*;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class EvolutionDamage {

    public static final Type[] PLAYER = {Type.CRUSHING, Type.PIERCING, Type.SLASHING, Type.MELEE, Type.RANGED, Type.TOTAL};
    public static final Type[] NON_PLAYER = Arrays.stream(Type.values())
                                                  .filter(type -> type != Type.GENERIC &&
                                                                  type != Type.CRUSHING &&
                                                                  type != Type.PIERCING &&
                                                                  type != Type.SLASHING)
                                                  .toArray(Type[]::new);
    public static final Type[] ALL = Arrays.stream(Type.values()).filter(type -> type != Type.GENERIC).toArray(Type[]::new);
    public static final Set<String> ALL_SOURCES = new HashSet<>();
    public static final DamageSource DEHYDRATION = createSrc(new DamageSourceEv("dehydration", Type.SICKNESS).bypassArmor().absolute());
    public static final DamageSource DROWN = createSrc(new DamageSourceEv("drown", Type.DROWNING).bypassArmor());
    public static final DamageSource FALL = new DamageSourceEv("fall_damage", Type.IMPACT).bypassArmor();
    public static final DamageSource FALLING_METAL = createSrc(new DamageSourceEv("falling_metal", Type.CRUSHING).bypassArmor());
    public static final DamageSource FALLING_ROCK = createSrc(new DamageSourceEv("falling_rock", Type.CRUSHING).bypassArmor());
    public static final DamageSource FALLING_SOIL = createSrc(new DamageSourceEv("falling_soil", Type.CRUSHING).bypassArmor());
    public static final DamageSource FALLING_WOOD = createSrc(new DamageSourceEv("falling_wood", Type.CRUSHING).bypassArmor());
    public static final DamageSource FALLING_TREE = createSrc(new DamageSourceEv("falling_tree", Type.CRUSHING).bypassArmor());
    public static final DamageSource IN_FIRE = createSrc(new DamageSourceEv("in_fire", Type.FIRE).fire().bypassArmor());
    public static final DamageSource IN_WALL = createSrc(new DamageSourceEv("in_wall", Type.SUFFOCATION).bypassArmor());
    public static final DamageSource ON_FIRE = createSrc(new DamageSourceEv("on_fire", Type.FIRE).fire().bypassArmor());
    public static final DamageSource VOID = createSrc(new DamageSourceEv("void", Type.VOID).bypassArmor().creative());
    public static final DamageSource WALL_IMPACT = createSrc(new DamageSourceEv("wall_impact", Type.IMPACT).bypassArmor());
    public static final DamageSource WATER_IMPACT = createSrc(new DamageSourceEv("water_impact", Type.IMPACT).bypassArmor());
    public static final DamageSource WATER_INTOXICATION = createSrc(new DamageSourceEv("water_intoxication", Type.SICKNESS).bypassArmor().absolute());

    static {
        ALL_SOURCES.add("arrow");
        ALL_SOURCES.add("doomed_to_fall");
        ALL_SOURCES.add("fall");
        ALL_SOURCES.add("fall_then_finished");
        ALL_SOURCES.add("hook");
        ALL_SOURCES.add("player");
        ALL_SOURCES.add("spear");
    }

    private EvolutionDamage() {
    }

    public static DamageSourceEv causeArrowDamage(EntityGenericProjectile<?> arrow, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("arrow", arrow, trueSource, Type.PIERCING).projectile();
    }

    public static DamageSourceEv causeHookDamage(Entity source, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("hook", source, trueSource, Type.PIERCING).projectile();
    }

    //TODO
    public static DamageSourceEv causeMobDamage(LivingEntity mob) {
        return new DamageSourceEntity("mob", mob, Type.GENERIC);
    }

    public static DamageSourceEv causePVPMeleeDamage(PlayerEntity player, Type type, Hand hand, EquipmentSlotType slot) {
        return new DamageSourcePVP("player", player, type, hand, slot);
    }

    public static DamageSourceEv causePlayerMeleeDamage(PlayerEntity player, Type type, Hand hand) {
        return new DamageSourcePlayer("player", player, type, hand);
    }

    public static DamageSourceEv causeSpearDamage(Entity source, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("spear", source, trueSource, Type.PIERCING).projectile();
    }

    private static DamageSource createSrc(DamageSource src) {
        ALL_SOURCES.add(src.damageType);
        return src;
    }

    public enum Type {
        CRUSHING("crushing"),
        DROWNING("drowning"),
        FIRE("fire"),
        GENERIC("generic"),
        IMPACT("impact"),
        MELEE("melee"),     //Used only for statistics
        PIERCING("piercing"),
        RANGED("ranged"),   //Used only for statistics
        SICKNESS("sickness"),
        SLASHING("slashing"),
        SUFFOCATION("suffocation"),
        TOTAL("total"),     //Used only for statistics
        VOID("void");

        private final String name;
        private final ITextComponent textComponent;
        private final String translationKey;

        Type(String name) {
            this.name = name;
            this.translationKey = "evolution.tooltip.damage." + name;
            this.textComponent = new TranslationTextComponent(this.translationKey);
        }

        public String getName() {
            return this.name;
        }

        public ITextComponent getTextComponent() {
            return this.textComponent;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}
