package tgw.evolution.init;

import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.math.IFormatter;
import tgw.evolution.util.math.Metric;

public final class EvolutionFormatter {

    public static final IFormatter TEMPERATURE_BODY_RELATIVE = value -> {
        String unit;
        switch (EvolutionConfig.CLIENT.bodyTemperature.get()) {
            case CELSIUS -> unit = "\u00B0C";
            case KELVIN -> {
                unit = "K";
                value = tgw.evolution.util.Temperature.C2KRelative(value);
            }
            case FAHRENHEIT -> {
                unit = "\u00B0F";
                value = tgw.evolution.util.Temperature.C2FRelative(value);
            }
            case RANKINE -> {
                unit = "\u00B0R";
                value = tgw.evolution.util.Temperature.C2RRelative(value);
            }
            default -> throw new UnregisteredFeatureException("Unregistered Temperature unit: " + EvolutionConfig.CLIENT.bodyTemperature.get());
        }
        return Metric.format(value, 1, unit, true);
    };

    public static final IFormatter FOOD = value -> {
        String unit;
        switch (EvolutionConfig.CLIENT.food.get()) {
            case KILOCALORIE -> unit = " kcal";
            case KILOJOULE -> {
                unit = " kJ";
                value *= 4.184;
            }
            default -> throw new UnregisteredFeatureException("Unregistered Energy unit: " + EvolutionConfig.CLIENT.food.get());
        }
        return Metric.format(value, 0) + unit;
    };

    public static final IFormatter MASS = value -> {
        String unit;
        switch (EvolutionConfig.CLIENT.mass.get()) {
            case KILOGRAM -> unit = " kg";
            case POUND -> {
                value *= 2.204_622_621_85;
                unit = " lb";
            }
            default -> throw new UnregisteredFeatureException("Unregistered Mass unit: " + EvolutionConfig.CLIENT.mass.get());
        }
        return Metric.format(value, 2) + unit;
    };

    public static final IFormatter DRINK = value -> {
        String unit;
        switch (EvolutionConfig.CLIENT.drink.get()) {
            case MILLILITER -> unit = " mL";
            default -> throw new UnregisteredFeatureException("Unregistered Volume unit: " + EvolutionConfig.CLIENT.drink.get());
        }
        return Metric.format(value, 0) + unit;
    };

    public static final IFormatter VOLUME = value -> {
        String unit;
        switch (EvolutionConfig.CLIENT.volume.get()) {
            case LITER -> unit = " L";
            case CUBIC_METER -> {
                unit = " m\u00B3";
                value /= 1_000;
            }
            case CUBIC_CENTIMETER -> {
                unit = " cm\u00B3";
                value *= 1_000;
            }
            default -> throw new UnregisteredFeatureException("Unregistered Volume unit: " + EvolutionConfig.CLIENT.drink.get());
        }
        return Metric.format(value, 2) + unit;
    };

    private EvolutionFormatter() {
    }

    public enum Drink {
        MILLILITER
    }

    public enum Food {
        KILOCALORIE,
        KILOJOULE
    }

    public enum Mass {
        KILOGRAM,
        POUND
    }

    public enum Temperature {
        KELVIN,
        CELSIUS,
        FAHRENHEIT,
        RANKINE
    }

    public enum Volume {
        LITER,
        CUBIC_METER,
        CUBIC_CENTIMETER
    }
}
