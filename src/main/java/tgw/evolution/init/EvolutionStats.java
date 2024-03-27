package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.stats.IEvoStatFormatter;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.math.Metric;

import java.util.EnumMap;
import java.util.Map;

public final class EvolutionStats {

    public static final IEvoStatFormatter DEFAULT = new IEvoStatFormatter() {
        @Override
        public String format(long value) {
            return Metric.DEFAULT.format(value);
        }

        @Override
        public String format(int value) {
            Evolution.warn("Incorrect stats method!");
            return "null";
        }
    };
    public static final IEvoStatFormatter METRIC = new IEvoStatFormatter() {
        @Override
        public String format(long value) {
            return Metric.format(value, 1, "");
        }

        @Override
        public String format(int value) {
            Evolution.warn("Incorrect stats method!");
            return "null";
        }
    };
    public static final IEvoStatFormatter DAMAGE = new IEvoStatFormatter() {
        @Override
        public String format(long value) {
            return Metric.HP_FORMAT.format(value);
        }

        @Override
        public String format(int value) {
            Evolution.warn("Incorrect stats method!");
            return "null";
        }
    };
    public static final IEvoStatFormatter TIME = new IEvoStatFormatter() {
        @Override
        public String format(long ticks) {
            double seconds = ticks / 20.0;
            double minutes = seconds / 60.0;
            double hours = minutes / 60.0;
            if (!EvolutionConfig.LIMIT_TIME_UNITS_TO_HOUR.get()) {
                double days = hours / 24.0;
                double years = days / 365.25;
                if (years > 1) {
                    return Metric.TWO_PLACES.format(years) + " a";
                }
                if (days > 1) {
                    return Metric.TWO_PLACES.format(days) + " d";
                }
            }
            if (hours > 1) {
                return Metric.TWO_PLACES.format(hours) + " h";
            }
            return minutes > 1 ? Metric.TWO_PLACES.format(minutes) + " min" : Metric.TWO_PLACES.format(seconds) + " s";
        }

        @Override
        public String format(int value) {
            Evolution.warn("Incorrect stats method!");
            return "null";
        }
    };
    public static final IEvoStatFormatter DISTANCE = new IEvoStatFormatter() {
        @Override
        public String format(long millimeters) {
            return EvolutionFormatter.DISTANCE.format(millimeters);
        }

        @Override
        public String format(int value) {
            Evolution.warn("Incorrect stats method!");
            return "null";
        }
    };

    //Damage
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_DEALT_BY_TYPE = genDamage("dealt", EvolutionDamage.PLAYER);
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_RESISTED_BY_TYPE = genDamage("resisted", EvolutionDamage.PLAYER);
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_TAKEN_BY_TYPE = genDamage("taken", EvolutionDamage.ALL);
    //Deaths
    public static final O2OMap<String, ResourceLocation> DEATH_SOURCE = new O2OHashMap<>();
    public static final ResourceLocation DEATHS = registerCustom("death_total", DEFAULT);
    //Distance
    public static final ResourceLocation DISTANCE_CLIMBED = registerCustom("distance_climbed", DISTANCE);
    public static final ResourceLocation DISTANCE_CROUCHED = registerCustom("distance_crouched", DISTANCE);
    public static final ResourceLocation DISTANCE_FALLEN = registerCustom("distance_fallen", DISTANCE);
    public static final ResourceLocation DISTANCE_FLOWN = registerCustom("distance_flown", DISTANCE);
    public static final ResourceLocation DISTANCE_JUMPED_HORIZONTAL = registerCustom("distance_jumped_horizontal", DISTANCE);
    public static final ResourceLocation DISTANCE_JUMPED_VERTICAL = registerCustom("distance_jumped_vertical", DISTANCE);
    public static final ResourceLocation DISTANCE_PRONE = registerCustom("distance_prone", DISTANCE);
    public static final ResourceLocation DISTANCE_SPRINTED = registerCustom("distance_sprinted", DISTANCE);
    public static final ResourceLocation DISTANCE_SWUM = registerCustom("distance_swum", DISTANCE);
    public static final ResourceLocation DISTANCE_WALKED = registerCustom("distance_walked", DISTANCE);
    public static final ResourceLocation DISTANCE_WALKED_UNDER_WATER = registerCustom("distance_walked_under_water", DISTANCE);
    public static final ResourceLocation DISTANCE_WALKED_ON_WATER = registerCustom("distance_walked_on_water", DISTANCE);
    public static final ResourceLocation TOTAL_ANIMAL_RIDDEN_DISTANCE = registerCustom("distance_total_animal_ridden", DISTANCE);
    public static final ResourceLocation TOTAL_DISTANCE_TRAVELED = registerCustom("distance_total_traveled", DISTANCE);
    public static final ResourceLocation TOTAL_VEHICLE_RIDDEN_DISTANCE = registerCustom("distance_total_vehicle_ridden", DISTANCE);
    //Time
    public static final ResourceLocation TIME_PLAYED = registerCustom("time_played", TIME);
    public static final ResourceLocation TIME_SINCE_LAST_DEATH = registerCustom("time_since_last_death", TIME);
    public static final ResourceLocation TIME_SINCE_LAST_REST = registerCustom("time_since_last_rest", TIME);
    public static final ResourceLocation TIME_SNEAKING = registerCustom("time_sneaking", TIME);
    public static final ResourceLocation TIME_WITH_WORLD_OPEN = registerCustom("time_with_world_open", TIME);
    //Generic
    public static final ResourceLocation CHESTS_OPENED = registerCustom("chests_opened", DEFAULT);
    public static final ResourceLocation ITEMS_DROPPED = registerCustom("items_dropped", DEFAULT);
    public static final ResourceLocation ITEMS_THROWN = registerCustom("items_thrown", DEFAULT);
    public static final ResourceLocation JUMPS = registerCustom("jumps", DEFAULT);
    public static final ResourceLocation LEAVE_GAME = registerCustom("leave_game", DEFAULT);
    public static final ResourceLocation MOB_KILLS = registerCustom("mob_kills", DEFAULT);
    public static final ResourceLocation PLAYER_KILLS = registerCustom("player_kills", DEFAULT);
    public static final ResourceLocation TIMES_KNAPPING = registerCustom("times_knapping", DEFAULT);
    public static final ResourceLocation TIMES_SLEPT = registerCustom("times_slept", DEFAULT);
    //Entity
    public static final StatType<EntityType<?>> DAMAGE_DEALT;
    public static final StatType<EntityType<?>> DAMAGE_TAKEN;
    //Blocks
    public static final StatType<Block> BLOCK_PLACED;

    static {
        for (long it = EvolutionDamage.ALL_SOURCES.beginIteration(); (it & 0xFFFF_FFFFL) != 0; it = EvolutionDamage.ALL_SOURCES.nextEntry(it)) {
            String name = EvolutionDamage.ALL_SOURCES.getIteration(it);
            //noinspection ObjectAllocationInLoop
            DEATH_SOURCE.put(name, registerCustom("death_" + name, DEFAULT));
        }
        DEATH_SOURCE.trim();
        DAMAGE_DEALT = Registry.register(Registry.STAT_TYPE, Evolution.getResource("damage_dealt"), new StatType<>(Registry.ENTITY_TYPE));
        DAMAGE_TAKEN = Registry.register(Registry.STAT_TYPE, Evolution.getResource("damage_taken"), new StatType<>(Registry.ENTITY_TYPE));
        BLOCK_PLACED = Registry.register(Registry.STAT_TYPE, Evolution.getResource("block_placed"), new StatType<>(Registry.BLOCK));
    }

    private EvolutionStats() {
    }

    public static void register() {
        //This is called on the <clinit> of Stats.
    }

    private static Map<EvolutionDamage.Type, ResourceLocation> genDamage(String pattern, EvolutionDamage.Type[] types) {
        Map<EvolutionDamage.Type, ResourceLocation> map = new EnumMap<>(EvolutionDamage.Type.class);
        for (EvolutionDamage.Type dmgType : types) {
            assert dmgType != null;
            //noinspection ObjectAllocationInLoop
            map.put(dmgType, registerCustom("damage_" + dmgType.getName() + "_" + pattern, DAMAGE));
        }
        return map;
    }

    private static ResourceLocation registerCustom(String key, StatFormatter formatter) {
        ResourceLocation resourceLocation = Evolution.getResource(key);
        Registry.register(Registry.CUSTOM_STAT, Evolution.MODID + ":" + key, resourceLocation);
        Stats.CUSTOM.get(resourceLocation, formatter);
        return resourceLocation;
    }
}
