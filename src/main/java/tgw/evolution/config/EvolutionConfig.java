package tgw.evolution.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.util.time.Time;

public final class EvolutionConfig {

    public static final Common COMMON;
    public static final Client CLIENT;
    public static final Server SERVER;
    private static final ForgeConfigSpec COMMON_SPEC;
    private static final ForgeConfigSpec CLIENT_SPEC;
    private static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        assert commonSpecPair != null;
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        assert clientSpecPair != null;
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        assert serverSpecPair != null;
        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    private EvolutionConfig() {
    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    public static class Common {

        Common(final ForgeConfigSpec.Builder builder) {
            builder.push("Common");
            builder.pop();
        }
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue animatedTextures;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Temperature> bodyTemperature;
        public final ForgeConfigSpec.BooleanValue celestialEquator;
        public final ForgeConfigSpec.BooleanValue celestialForceAll;
        public final ForgeConfigSpec.BooleanValue celestialPoles;
        public final ForgeConfigSpec.BooleanValue crazyMode;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Distance> distance;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Drink> drink;
        public final ForgeConfigSpec.BooleanValue ecliptic;
        public final ForgeConfigSpec.BooleanValue followUps;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Food> food;
        public final ForgeConfigSpec.BooleanValue hitmarkers;
        public final ForgeConfigSpec.IntValue leavesCulling;
        public final ForgeConfigSpec.BooleanValue limitTimeUnitsToHour;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Mass> mass;
        public final ForgeConfigSpec.BooleanValue planets;
        public final ForgeConfigSpec.BooleanValue showPlanets;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Speed> speed;
        public final ForgeConfigSpec.EnumValue<EvolutionFormatter.Volume> volume;

        Client(final ForgeConfigSpec.Builder builder) {
            builder.push("Client");
            this.crazyMode = builder.translation("evolution.config.crazyMode").define("crazyMode", false);
            this.hitmarkers = builder.translation("evolution.config.hitmarkers").define("hitmarkers", true);
            this.followUps = builder.translation("evolution.config.followUps").define("followUps", true);
            builder.push("performance");
            this.leavesCulling = builder.translation("evolution.config.leavesCulling").defineInRange("leavesCulling", 3, 0, 8);
            this.showPlanets = builder.translation("evolution.config.showPlanets").define("showPlanets", true);
            this.animatedTextures = builder.translation("evolution.config.animatedTextures").define("animatedTextures", true);
            builder.pop();
            builder.push("units");
            this.limitTimeUnitsToHour = builder.translation("evolution.config.limitTimeUnitsToHour").define("limitTimeUnitsToHour", false);
            this.distance = builder.translation("evolution.config.distance").defineEnum("distance", EvolutionFormatter.Distance.METRIC);
            this.bodyTemperature = builder.translation("evolution.config.bodyTemperature")
                                          .defineEnum("bodyTemperature", EvolutionFormatter.Temperature.CELSIUS);
            this.food = builder.translation("evolution.config.food").defineEnum("food", EvolutionFormatter.Food.KILOCALORIE);
            this.mass = builder.translation("evolution.config.mass").defineEnum("mass", EvolutionFormatter.Mass.KILOGRAM);
            this.drink = builder.translation("evolution.config.drink").defineEnum("drink", EvolutionFormatter.Drink.MILLILITER);
            this.speed = builder.translation("evolution.config.speed").defineEnum("speed", EvolutionFormatter.Speed.METERS_PER_SECOND);
            this.volume = builder.translation("evolution.config.volume").defineEnum("volume", EvolutionFormatter.Volume.LITER);
            builder.pop();
            builder.push("debug");
            builder.push("sky");
            this.celestialForceAll = builder.translation("evolution.config.celestialForceAll").define("celestialForceAll", false);
            this.celestialEquator = builder.translation("evolution.config.celestialEquator").define("celestialEquator", false);
            this.celestialPoles = builder.translation("evolution.config.celestialPoles").define("celestialPoles", false);
            this.ecliptic = builder.translation("evolution.config.ecliptic").define("ecliptic", false);
            this.planets = builder.translation("evolution.config.planets").define("planets", false);
            builder.pop();
            builder.pop();
            builder.pop();
        }
    }

    public static class Server {

        public final ForgeConfigSpec.IntValue torchTime;

        Server(final ForgeConfigSpec.Builder builder) {
            builder.push("Server");
            this.torchTime = builder.translation("evolution.config.torchTime")
                                    .defineInRange("torchTime", 10, 0, Time.TICKS_PER_YEAR / Time.TICKS_PER_HOUR);
            builder.pop();
        }
    }
}
