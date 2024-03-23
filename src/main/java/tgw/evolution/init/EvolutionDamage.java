package tgw.evolution.init;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.damage.DamageSourceEntity;
import tgw.evolution.util.damage.DamageSourceEntityIndirect;
import tgw.evolution.util.damage.DamageSourceEv;
import tgw.evolution.util.damage.DamageSourcePlayer;
import tgw.evolution.util.math.MathHelper;

import java.util.Arrays;

public final class EvolutionDamage {

    public static final Type[] ALL = Arrays.stream(Type.VALUES).filter(type -> type != Type.GENERIC).toArray(Type[]::new);
    public static final Type[] NON_PLAYER = Arrays.stream(Type.VALUES)
                                                  .filter(type -> type != Type.GENERIC &&
                                                                  type != Type.CRUSHING &&
                                                                  type != Type.PIERCING &&
                                                                  type != Type.SLASHING
                                                  )
                                                  .toArray(Type[]::new);
    public static final Type[] PLAYER = {Type.CRUSHING, Type.PIERCING, Type.SLASHING, Type.MELEE, Type.RANGED, Type.TOTAL};
    public static final OSet<String> ALL_SOURCES;
    //Damage Sources
    public static final DamageSourceEv BORDER;
    public static final DamageSourceEv DEHYDRATION;
    public static final DamageSourceEv DROWN;
    public static final DamageSourceEv DUMMY;
    public static final DamageSourceEv FALL;
    public static final DamageSourceEv FALLING_METAL;
    public static final DamageSourceEv FALLING_ROCK;
    public static final DamageSourceEv FALLING_SOIL;
    public static final DamageSourceEv FALLING_TREE;
    public static final DamageSourceEv FALLING_WOOD;
    public static final DamageSourceEv IN_FIRE;
    public static final DamageSourceEv IN_WALL;
    public static final DamageSourceEv KILL;
    public static final DamageSourceEv ON_FIRE;
    public static final DamageSourceEv VOID;
    public static final DamageSourceEv WALL_IMPACT;
    public static final DamageSourceEv WATER_IMPACT;
    public static final DamageSourceEv WATER_INTOXICATION;
    //Constants
    public static final int DROWNING_IMMUNITY = 20;

    static {
        OSet<String> m = new OHashSet<>();
        m.add("arrow");
        m.add("doomed_to_fall");
        m.add("fall");
        m.add("fall_then_finished");
        m.add("hook");
        m.add("player");
        m.add("spear");
        ALL_SOURCES = m.view();
        BORDER = createSrc(new DamageSourceEv("border", Type.SUFFOCATION).bypassArmor());
        DEHYDRATION = createSrc(new DamageSourceEv("dehydration", Type.SICKNESS).bypassArmor().absolute());
        DROWN = createSrc(new DamageSourceEv("drown", Type.DROWNING).bypassArmor());
        DUMMY = new DamageSourceEv("dummy", Type.GENERIC);
        FALL = new DamageSourceEv("fall_damage", Type.IMPACT).bypassArmor();
        FALLING_METAL = createSrc(new DamageSourceEv("falling_metal", Type.CRUSHING).bypassArmor());
        FALLING_ROCK = createSrc(new DamageSourceEv("falling_rock", Type.CRUSHING).bypassArmor());
        FALLING_SOIL = createSrc(new DamageSourceEv("falling_soil", Type.CRUSHING).bypassArmor());
        FALLING_TREE = createSrc(new DamageSourceEv("falling_tree", Type.CRUSHING).bypassArmor());
        FALLING_WOOD = createSrc(new DamageSourceEv("falling_wood", Type.CRUSHING).bypassArmor());
        IN_FIRE = createSrc(new DamageSourceEv("in_fire", Type.FIRE).fire().bypassArmor());
        IN_WALL = createSrc(new DamageSourceEv("in_wall", Type.SUFFOCATION).bypassArmor());
        KILL = createSrc(new DamageSourceEv("kill", Type.VOID).bypassArmor().bypassInvul().absolute());
        ON_FIRE = createSrc(new DamageSourceEv("on_fire", Type.FIRE).fire().bypassArmor());
        VOID = createSrc(new DamageSourceEv("void", Type.VOID).bypassArmor().bypassInvul());
        WALL_IMPACT = createSrc(new DamageSourceEv("wall_impact", Type.IMPACT).bypassArmor());
        WATER_IMPACT = createSrc(new DamageSourceEv("water_impact", Type.IMPACT).bypassArmor());
        WATER_INTOXICATION = createSrc(new DamageSourceEv("water_intoxication", Type.SICKNESS).bypassArmor().absolute());
    }

    private EvolutionDamage() {
    }

    public static DamageSourceEv causeArrowDamage(EntityGenericProjectile arrow, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("arrow", arrow, trueSource, Type.PIERCING).projectile();
    }

    public static DamageSourceEv causeHookDamage(Entity source, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("hook", source, trueSource, Type.PIERCING).projectile();
    }

    public static DamageSourceEv causeMobMeleeDamage(LivingEntity mob, Type type) {
        return new DamageSourceEntity("mob", mob, type);
    }

    public static DamageSourceEv causePlayerMeleeDamage(Player player, Type type) {
        return new DamageSourcePlayer("player", player, type);
    }

    public static DamageSourceEv causeSpearDamage(EntitySpear source, @Nullable Entity trueSource) {
        return new DamageSourceEntityIndirect("spear", source, trueSource, Type.PIERCING).projectile();
    }

    private static DamageSourceEv createSrc(DamageSourceEv src) {
        ALL_SOURCES.add(src.msgId);
        return src;
    }

    public enum Type {
        CRUSHING("crushing", 0, 1, 0),
        DROWNING("drowning", 7, 0, DROWNING_IMMUNITY),
        FIRE("fire", 6, 0, 10),
        GENERIC("generic", 0, 0, 0),
        IMPACT("impact", 5, 0, 0),
        MELEE("melee", 4, 1, 0),     //Used only for statistics
        PIERCING("piercing", 1, 1, 0),
        RANGED("ranged", 3, 1, 0),   //Used only for statistics
        SICKNESS("sickness", 4, 0, 20),
        SLASHING("slashing", 2, 1, 0),
        SUFFOCATION("suffocation", 2, 0, 5),
        TOTAL("total", 3, 0, 0),     //Used only for statistics
        VOID("void", 1, 0, 0);

        public static final Type[] VALUES = values();
        private final byte immunity;
        private final String name;
        private final int texX;
        private final int texY;
        private final Component textComponent;
        private final String translationKey;

        Type(String name, int texX, int texY, @Range(from = 0, to = 127) int immunity) {
            this.name = name;
            this.translationKey = "evolution.tooltip.damage." + name;
            this.textComponent = new TranslatableComponent(this.translationKey + ".name");
            this.texX = texX;
            this.texY = texY;
            this.immunity = MathHelper.toByteExact(immunity);
        }

        public static @Nullable Type byName(String type) {
            for (Type value : VALUES) {
                if (value.name.equals(type)) {
                    return value;
                }
            }
            return null;
        }

        public byte getImmunity() {
            return this.immunity;
        }

        public String getName() {
            return this.name;
        }

        public int getTexX() {
            return this.texX;
        }

        public int getTexY() {
            return this.texY;
        }

        public Component getTextComponent() {
            return this.textComponent;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}
