package tgw.evolution.util;

public final class Temperature {

    private Temperature() {
    }

    /**
     * @return Converts a temperature given in degrees Celsius to Kelvin.
     */
    public static double C2K(double celsius) {
        return celsius - 273;
    }

    /**
     * @return Converts a temperature given in Kelvin to degrees Celsius.
     */
    public static double K2C(double kelvin) {
        return kelvin + 273;
    }

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
            alpha = (int) (255 * (temperature - 8));
        }
        else if (temperature <= 10) {
            red = 255;
            green = (int) (68 * (temperature - 9));
            blue = 0;
            alpha = 255;
        }
        else if (temperature <= 19) {
            red = 255;
            green = (int) (99.470_802_586_1 * Math.log(temperature) - 161.119_568_166_1);
            green = MathHelper.clamp(green, 0, 255);
            blue = 0;
            alpha = 255;
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
}
