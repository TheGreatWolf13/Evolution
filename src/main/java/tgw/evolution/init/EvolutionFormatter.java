package tgw.evolution.init;

import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.util.math.IFormatter;
import tgw.evolution.util.math.Metric;

public final class EvolutionFormatter {

    public static final IFormatter TEMPERATURE_BODY_RELATIVE = value -> {
        Temperature temperature = EvolutionConfig.CLIENT.bodyTemperature.get();
        value = switch (temperature) {
            case CELSIUS, KELVIN -> value;
            case FAHRENHEIT -> tgw.evolution.util.Temperature.C2FRelative(value);
            case RANKINE -> tgw.evolution.util.Temperature.C2RRelative(value);
        };
        return Metric.format(value, 1, temperature.getUnit(), true);
    };

    public static final IFormatter FOOD = value -> {
        Food food = EvolutionConfig.CLIENT.food.get();
        value = switch (food) {
            case KILOCALORIE -> value;
            case KILOJOULE -> value * 4.184;
        };
        return Metric.format(value, 0) + food.getUnit();
    };

    public static final IFormatter MASS = value -> {
        Mass mass = EvolutionConfig.CLIENT.mass.get();
        value = switch (mass) {
            case KILOGRAM -> value;
            case POUND -> value * 2.204_622_621_85;
        };
        return Metric.format(value, 2) + mass.getUnit();
    };

    public static final IFormatter DRINK = value -> {
        Drink drink = EvolutionConfig.CLIENT.drink.get();
        value = switch (drink) {
            case FLUID_OUNCE -> value / 28.413_062_5;
            case MILLILITER -> value;
        };
        return Metric.format(value, 0) + drink.getUnit();
    };

    public static final IFormatter VOLUME = value -> {
        Volume volume = EvolutionConfig.CLIENT.volume.get();
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

    public enum Drink implements IUnit {
        FLUID_OUNCE("fl oz"),
        MILLILITER("mL");

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

    public enum Temperature implements IUnit {
        CELSIUS("\u00B0C"),
        FAHRENHEIT("\u00B0F"),
        KELVIN("K"),
        RANKINE("\u00B0R");

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
