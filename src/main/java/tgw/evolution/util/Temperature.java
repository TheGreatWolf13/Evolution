package tgw.evolution.util;

import tgw.evolution.util.earth.ClimateZone;
import tgw.evolution.util.math.MathHelper;

public final class Temperature {

    private Temperature() {
    }

    public static double C2F(double value) {
        return value * 9 / 5 + 32;
    }

    public static double C2FRelative(double value) {
        return value * 9 / 5;
    }

    /**
     * @return Converts a temperature given in degrees Celsius to Kelvin.
     */
    public static double C2K(double celsius) {
        return celsius + 273.15;
    }

    public static double C2KRelative(double value) {
        return value;
    }

    public static double C2R(double value) {
        return value * 9 / 5 + 491.67;
    }

    public static double C2RRelative(double value) {
        return value * 9 / 5;
    }

    /**
     * @return Converts a temperature given in Kelvin to degrees Celsius.
     */
    public static double K2C(double kelvin) {
        return kelvin - 273.15;
    }

    public static double getBaseTemperatureForRegion(ClimateZone.Region region) {
        if (region == null) {
            return 0;
        }
        return switch (region) {
            case POLAR -> 15;
            case TEMPERATE -> 20;
            case TROPICAL -> 25;
        };
    }

    /**
     * @return A color in a 32 bit, ARGB value. The 8 most significant bits represent alpha, while the least significant bits represent blue.
     * This color is equivalent to the black body radiation, given a temperature in Kelvin.
     */
    public static int getBlackBodySpectrumColor(double temperature) {
        temperature /= 100;
        int red;
        int green;
        int blue;
        int alpha;
        if (temperature <= 8) {
            red = 0;
            green = 0;
            blue = 0;
            alpha = 0;
        }
        else if (temperature <= 9) {
            red = (int) (255 * (temperature - 8));
            green = 0;
            blue = 0;
            alpha = (int) (191 * (temperature - 8));
        }
        else if (temperature <= 10) {
            red = 255;
            green = (int) (68 * (temperature - 9));
            blue = 0;
            alpha = 191;
        }
        else if (temperature <= 19) {
            red = 255;
            green = (int) (99.470_802_586_1 * Math.log(temperature) - 161.119_568_166_1);
            green = MathHelper.clamp(green, 0, 255);
            blue = 0;
            alpha = (int) (191 + 64 * (temperature - 10) / 9.0);
        }
        else if (temperature <= 66) {
            red = 255;
            green = (int) (99.470_802_586_1 * Math.log(temperature) - 161.119_568_166_1);
            green = MathHelper.clamp(green, 0, 255);
            blue = (int) (138.517_731_223_1 * Math.log(temperature - 10) - 305.044_792_730_7);
            blue = MathHelper.clamp(blue, 0, 255);
            alpha = 255;
        }
        else {
            red = (int) (329.698_727_446 * Math.pow(temperature - 60, -0.133_204_759_2));
            red = MathHelper.clamp(red, 0, 255);
            green = (int) (288.122_169_528_3 * Math.pow(temperature - 60, -0.075_514_849_2));
            green = MathHelper.clamp(green, 0, 255);
            blue = 255;
            alpha = 255;
        }
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static short getMaxComfortForRegion(ClimateZone.Region region) {
        if (region == null) {
            return 0;
        }
        return (short) switch (region) {
            case POLAR -> 20;
            case TEMPERATE -> 25;
            case TROPICAL -> 30;
        };
    }

    public static short getMinComfortForRegion(ClimateZone.Region region) {
        if (region == null) {
            return 0;
        }
        return (short) switch (region) {
            case POLAR -> 10;
            case TEMPERATE -> 15;
            case TROPICAL -> 20;
        };
    }
}
