package tgw.evolution.config;

import tgw.evolution.init.EvolutionFormatter;

import java.util.function.Supplier;

public final class EvolutionConfig {

    public static final Common COMMON;
    public static final Client CLIENT;
    public static final Server SERVER;
//    private static final ForgeConfigSpec COMMON_SPEC;
//    private static final ForgeConfigSpec CLIENT_SPEC;
//    private static final ForgeConfigSpec SERVER_SPEC;

    static {
//        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
//        assert commonSpecPair != null;
//        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = /*commonSpecPair.getLeft();*/new Common();
//        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
//        assert clientSpecPair != null;
//        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = /*clientSpecPair.getLeft();*/new Client();
//        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
//        assert serverSpecPair != null;
//        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = /*serverSpecPair.getLeft();*/new Server();
    }

    private EvolutionConfig() {
    }

//    public static void register(final ModLoadingContext context) {
//        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
//        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
//        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
//    }

    public static class Common {

//        Common(final ForgeConfigSpec.Builder builder) {
//            builder.push("Common");
//            builder.pop();
//        }
    }

    public static class Client {

        public final Supplier<Boolean> animatedTextures = () -> true;
        public final Supplier<EvolutionFormatter.Temperature> bodyTemperature = () -> EvolutionFormatter.Temperature.CELSIUS;
        public final Supplier<Boolean> celestialEquator = () -> false;
        public final Supplier<Boolean> celestialForceAll = () -> false;
        public final Supplier<Boolean> celestialPoles = () -> false;
        public final Supplier<Boolean> crazyMode = () -> false;
        public final Supplier<EvolutionFormatter.Distance> distance = () -> EvolutionFormatter.Distance.METRIC;
        public final Supplier<EvolutionFormatter.Drink> drink = () -> EvolutionFormatter.Drink.MILLILITER;
        public final Supplier<Boolean> ecliptic = () -> false;
        public final Supplier<Boolean> followUps = () -> true;
        public final Supplier<EvolutionFormatter.Food> food = () -> EvolutionFormatter.Food.KILOCALORIE;
        public final Supplier<Boolean> hitmarkers = () -> true;
        public final Supplier<Integer> leavesCulling = () -> 3;
        public final Supplier<Boolean> limitTimeUnitsToHour = () -> false;
        public final Supplier<EvolutionFormatter.Mass> mass = () -> EvolutionFormatter.Mass.KILOGRAM;
        public final Supplier<Boolean> planets = () -> false;
        public final Supplier<Boolean> renderHeightmap = () -> false;
        public final Supplier<Boolean> showPlanets = () -> true;
        public final Supplier<EvolutionFormatter.Speed> speed = () -> EvolutionFormatter.Speed.METERS_PER_SECOND;
        public final Supplier<Boolean> sunPath = () -> false;
        public final Supplier<Boolean> syncRendering = () -> false;
        public final Supplier<EvolutionFormatter.Volume> volume = () -> EvolutionFormatter.Volume.LITER;

//        Client(final ForgeConfigSpec.Builder builder) {
//            builder.push("Client");
//            this.crazyMode = builder.translation("evolution.config.crazyMode").define("crazyMode", false);
//            this.hitmarkers = builder.translation("evolution.config.hitmarkers").define("hitmarkers", true);
//            this.followUps = builder.translation("evolution.config.followUps").define("followUps", true);
//            builder.push("performance");
//            this.leavesCulling = builder.translation("evolution.config.leavesCulling").defineInRange("leavesCulling", 3, 0, 8);
//            this.showPlanets = builder.translation("evolution.config.showPlanets").define("showPlanets", true);
//            this.animatedTextures = builder.translation("evolution.config.animatedTextures").define("animatedTextures", true);
//            this.syncRendering = builder.translation("evolution.config.syncRendering").define("syncRendering", false);
//            builder.pop();
//            builder.push("units");
//            this.limitTimeUnitsToHour = builder.translation("evolution.config.limitTimeUnitsToHour").define("limitTimeUnitsToHour", false);
//            this.distance = builder.translation("evolution.config.distance").defineEnum("distance", EvolutionFormatter.Distance.METRIC);
//            this.bodyTemperature = builder.translation("evolution.config.bodyTemperature")
//                                          .defineEnum("bodyTemperature", EvolutionFormatter.Temperature.CELSIUS);
//            this.food = builder.translation("evolution.config.food").defineEnum("food", EvolutionFormatter.Food.KILOCALORIE);
//            this.mass = builder.translation("evolution.config.mass").defineEnum("mass", EvolutionFormatter.Mass.KILOGRAM);
//            this.drink = builder.translation("evolution.config.drink").defineEnum("drink", EvolutionFormatter.Drink.MILLILITER);
//            this.speed = builder.translation("evolution.config.speed").defineEnum("speed", EvolutionFormatter.Speed.METERS_PER_SECOND);
//            this.volume = builder.translation("evolution.config.volume").defineEnum("volume", EvolutionFormatter.Volume.LITER);
//            builder.pop();
//            builder.push("debug");
//            this.renderHeightmap = builder.translation("evolution.config.renderHeightmap").define("renderHeightmap", false);
//            builder.push("sky");
//            this.celestialForceAll = builder.translation("evolution.config.celestialForceAll").define("celestialForceAll", false);
//            this.celestialEquator = builder.translation("evolution.config.celestialEquator").define("celestialEquator", false);
//            this.celestialPoles = builder.translation("evolution.config.celestialPoles").define("celestialPoles", false);
//            this.ecliptic = builder.translation("evolution.config.ecliptic").define("ecliptic", false);
//            this.sunPath = builder.translation("evolution.config.sunPath").define("sunPath", false);
//            this.planets = builder.translation("evolution.config.planets").define("planets", false);
//            builder.pop();
//            builder.pop();
//            builder.pop();
//        }
    }

    public static class Server {

        public final Supplier<Integer> torchTime = () -> 10;

//        Server(final ForgeConfigSpec.Builder builder) {
//            builder.push("Server");
//            this.torchTime = builder.translation("evolution.config.torchTime")
//                                    .defineInRange("torchTime", 10, 0, Time.TICKS_PER_YEAR / Time.TICKS_PER_HOUR);
//            builder.pop();
//        }
    }
}
