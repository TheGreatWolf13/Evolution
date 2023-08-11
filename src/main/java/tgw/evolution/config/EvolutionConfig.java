package tgw.evolution.config;

import tgw.evolution.init.EvolutionFormatter;

import java.util.function.Supplier;

public final class EvolutionConfig {

    public static final Client CLIENT;

    static {
//        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
//        assert clientSpecPair != null;
//        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = /*clientSpecPair.getLeft();*/new Client();
    }

    private EvolutionConfig() {
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
}
