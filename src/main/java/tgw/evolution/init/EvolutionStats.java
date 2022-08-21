package tgw.evolution.init;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.stats.IEvoStatFormatter;
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
            double days = hours / 24.0;
            double years = days / 365.25;
            if (years > 1 && !EvolutionConfig.CLIENT.limitTimeUnitsToHour.get()) {
                return Metric.TWO_PLACES.format(years) + " a";
            }
            if (days > 1 && !EvolutionConfig.CLIENT.limitTimeUnitsToHour.get()) {
                return Metric.TWO_PLACES.format(days) + " d";
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
            return Metric.format(Metric.fromMetric(millimeters, Metric.MILLI), 2, "m");
        }

        @Override
        public String format(int value) {
            Evolution.warn("Incorrect stats method!");
            return "null";
        }
    };

    public static final DeferredRegister<StatType<?>> STATS = DeferredRegister.create(ForgeRegistries.STAT_TYPES, Evolution.MODID);
    //Damage Dealt
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_DEALT_ACTUAL = genDamage("dealt_actual", EvolutionDamage.PLAYER);
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_DEALT_RAW = genDamage("dealt_raw", EvolutionDamage.PLAYER);
    //Damage Taken
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_TAKEN_ACTUAL = genDamage("taken_actual", EvolutionDamage.ALL);
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_TAKEN_BLOCKED = genDamage("taken_blocked", EvolutionDamage.PLAYER);
    public static final Map<EvolutionDamage.Type, ResourceLocation> DAMAGE_TAKEN_RAW = genDamage("taken_raw", EvolutionDamage.ALL);
    //Deaths
    public static final Object2ObjectMap<String, ResourceLocation> DEATH_SOURCE = Util.make(new Object2ObjectOpenHashMap<>(), m -> {
        for (String src : EvolutionDamage.ALL_SOURCES) {
            //noinspection ObjectAllocationInLoop
            m.put(src, registerCustom("death_" + src, DEFAULT));
        }
        m.trim();
    });
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
    //Entity
    public static final RegistryObject<StatType<EntityType<?>>> DAMAGE_DEALT = STATS.register("damage_dealt",
                                                                                              () -> new StatType<>(Registry.ENTITY_TYPE));
    public static final RegistryObject<StatType<EntityType<?>>> DAMAGE_TAKEN = STATS.register("damage_taken",
                                                                                              () -> new StatType<>(Registry.ENTITY_TYPE));
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

    private EvolutionStats() {
    }

    private static Map<EvolutionDamage.Type, ResourceLocation> genDamage(String pattern, EvolutionDamage.Type[] types) {
        Map<EvolutionDamage.Type, ResourceLocation> map = new EnumMap<>(EvolutionDamage.Type.class);
        for (EvolutionDamage.Type dmgType : types) {
            //noinspection ObjectAllocationInLoop
            map.put(dmgType, registerCustom("damage_" + dmgType.getName() + "_" + pattern, DAMAGE));
        }
        return map;
    }

    public static void register() {
        STATS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static ResourceLocation registerCustom(String key, StatFormatter formatter) {
        ResourceLocation resourceLocation = Evolution.getResource(key);
        Registry.register(Registry.CUSTOM_STAT, Evolution.MODID + ":" + key, resourceLocation);
        Stats.CUSTOM.get(resourceLocation, formatter);
        return resourceLocation;
    }
}
