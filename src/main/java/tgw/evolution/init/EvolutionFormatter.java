package tgw.evolution.init;

import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.util.math.IFormatter;
import tgw.evolution.util.math.Metric;

public final class EvolutionFormatter {

    public static final IFormatter DISTANCE = mm -> switch (EvolutionConfig.DISTANCE.get()) {
        case METRIC -> {
            yield Metric.format(Metric.fromMetric(mm, Metric.MILLI), 1, "m");
        }
        case IMPERIAL -> {
            double inches = mm / 25.4;
            if (inches < 12) {
                yield Metric.ONE_PLACE.format(inches) + "in";
            }
            if (inches >= 5_280 * 12) {
                yield Metric.ONE_PLACE.format(inches / (5_280 * 12)) + "mi";
            }
            double feet = inches / 12;
            if (feet < 1_000) {
                yield Metric.ONE_PLACE.format(feet) + "ft";
            }
            yield Metric.ONE_PLACE.format(feet / 3) + "yd";
        }
    };

    public static final IFormatter TEMPERATURE_BODY_RELATIVE = value -> {
        Temperature temperature = EvolutionConfig.BODY_TEMPERATURE.get();
        value = switch (temperature) {
            case CELSIUS, KELVIN -> value;
            case FAHRENHEIT -> tgw.evolution.util.Temperature.C2FRelative(value);
            case RANKINE -> tgw.evolution.util.Temperature.C2RRelative(value);
        };
        return Metric.format(value, 1, temperature.getUnit(), true);
    };

    public static final IFormatter SPEED = value -> {
        Speed speed = EvolutionConfig.SPEED.get();
        value = switch (speed) {
            case KILOMETERS_PER_HOUR -> 3.6 * 20 * value;
            case METERS_PER_SECOND -> 20 * value;
            case MILES_PER_HOUR -> 2.237 * 20 * value;
        };
        return Metric.format(value, 2, speed.getUnit());
    };

    public static final IFormatter FOOD = value -> {
        Food food = EvolutionConfig.FOOD.get();
        value = switch (food) {
            case KILOCALORIE -> value;
            case KILOJOULE -> value * 4.184;
        };
        return Metric.format(value, 0) + food.getUnit();
    };

    public static final IFormatter MASS = value -> {
        Mass mass = EvolutionConfig.MASS.get();
        value = switch (mass) {
            case KILOGRAM -> value;
            case POUND -> value * 2.204_622_621_85;
        };
        return Metric.format(value, 2) + mass.getUnit();
    };

    public static final IFormatter DRINK = value -> {
        Drink drink = EvolutionConfig.DRINK.get();
        value = switch (drink) {
            case FLUID_OUNCE -> value / 28.413_062_5;
            case MILLILITER -> value;
        };
        return Metric.format(value, 0) + drink.getUnit();
    };

    public static final IFormatter VOLUME = value -> {
        Volume volume = EvolutionConfig.VOLUME.get();
        value = switch (volume) {
            case CUBIC_CENTIMETER -> value * 1_000;
            case CUBIC_METER -> value / 1_000;
            case GALLON -> value / 4.546_09;
            case LITER -> value;
        };
        return Metric.format(value, 2) + volume.getUnit();
    };

    private EvolutionFormatter() {
    }

    public enum Distance {
        IMPERIAL,
        METRIC;

        public static final Distance[] VALUES = values();
    }

    public enum Drink implements IUnit {
        FLUID_OUNCE("fl oz"),
        MILLILITER("mL");

        public static final Drink[] VALUES = values();
        private final String name;

        Drink(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUnit() {
            return " " + this.name;
        }
    }

    public enum Food implements IUnit {
        KILOCALORIE("kcal"),
        KILOJOULE("kJ");

        public static final Food[] VALUES = values();
        private final String name;

        Food(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUnit() {
            return " " + this.name;
        }
    }

    public enum Mass implements IUnit {
        KILOGRAM("kg"),
        POUND("lb");

        public static final Mass[] VALUES = values();
        private final String name;

        Mass(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUnit() {
            return " " + this.name;
        }
    }

    public enum Speed implements IUnit {
        KILOMETERS_PER_HOUR("km/h"),
        METERS_PER_SECOND("m/s"),
        MILES_PER_HOUR("mph");

        public static final Speed[] VALUES = values();
        private final String name;

        Speed(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUnit() {
            return " " + this.name;
        }
    }

    public enum Temperature implements IUnit {
        CELSIUS("\u00B0C"),
        FAHRENHEIT("\u00B0F"),
        KELVIN("K"),
        RANKINE("\u00B0R");

        public static final Temperature[] VALUES = values();
        private final String name;

        Temperature(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUnit() {
            return this.name;
        }
    }

    public enum Volume implements IUnit {
        CUBIC_CENTIMETER("cm\u00B3"),
        CUBIC_METER("m\u00B3"),
        GALLON("gal"),
        LITER("L");

        public static final Volume[] VALUES = values();
        private final String name;

        Volume(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getUnit() {
            return " " + this.name;
        }
    }

    public interface IUnit {

        String getName();

        String getUnit();
    }
}
