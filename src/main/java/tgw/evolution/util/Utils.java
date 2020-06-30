package tgw.evolution.util;

import java.util.Locale;

public class Utils {

    public static final int SECONDS_IN_A_MINUTE = 60;
    public static final int SECONDS_IN_AN_HOUR = 3600;
    public static final int SECONDS_IN_A_DAY = 86400;
    public static final int SECONDS_IN_A_YEAR = 31557600;

    public static String metric(double value, String unit, int decimalPlaces, boolean full) {
        if (value / Math.pow(10, -27) < 1) {
            return value + unit;
        }
        String prefix;
        if (value / Math.pow(10, -27) >= 1 && value / Math.pow(10, -21) < 1) {
            if (full) {
                prefix = "Yocto";
            }
            else {
                prefix = "y";
            }
            value /= Math.pow(10, -24);
        }
        else if (value / Math.pow(10, -21) >= 1 && value / Math.pow(10, -18) < 1) {
            if (full) {
                prefix = "Zepto";
            }
            else {
                prefix = "z";
            }
            value /= Math.pow(10, -21);
        }
        else if (value / Math.pow(10, -18) >= 1 && value / Math.pow(10, -15) < 1) {
            if (full) {
                prefix = "Atto";
            }
            else {
                prefix = "a";
            }
            value /= Math.pow(10, -18);
        }
        else if (value / Math.pow(10, -15) >= 1 && value / Math.pow(10, -12) < 1) {
            if (full) {
                prefix = "Femto";
            }
            else {
                prefix = "f";
            }
            value /= Math.pow(10, -15);
        }
        else if (value / Math.pow(10, -12) >= 1 && value / Math.pow(10, -9) < 1) {
            if (full) {
                prefix = "Pico";
            }
            else {
                prefix = "p";
            }
            value /= Math.pow(10, -12);
        }
        else if (value / Math.pow(10, -9) >= 1 && value / Math.pow(10, -6) < 1) {
            if (full) {
                prefix = "Nano";
            }
            else {
                prefix = "n";
            }
            value /= Math.pow(10, -9);
        }
        else if (value / Math.pow(10, -6) >= 1 && value / Math.pow(10, -3) < 1) {
            if (full) {
                prefix = "Micro";
            }
            else {
                prefix = "µ";
            }
            value /= Math.pow(10, -6);
        }
        else if (value / Math.pow(10, -3) >= 1 && value / Math.pow(10, 0) < 1) {
            if (full) {
                prefix = "Milli";
            }
            else {
                prefix = "m";
            }
            value /= Math.pow(10, -3);
        }
        else if (value / Math.pow(10, 0) >= 1 && value / Math.pow(10, 3) < 1) {
            if (full) {
                String first = unit.substring(0, 1).toUpperCase();
                unit = first + unit.substring(1);
            }
            prefix = "";
            value /= Math.pow(10, 0);
        }
        else if (value / Math.pow(10, 3) >= 1 && value / Math.pow(10, 6) < 1) {
            if (full) {
                prefix = "Kilo";
            }
            else {
                prefix = "k";
            }
            value /= Math.pow(10, 3);
        }
        else if (value / Math.pow(10, 6) >= 1 && value / Math.pow(10, 9) < 1) {
            if (full) {
                prefix = "Mega";
            }
            else {
                prefix = "M";
            }
            value /= Math.pow(10, 6);
        }
        else if (value / Math.pow(10, 9) >= 1 && value / Math.pow(10, 12) < 1) {
            if (full) {
                prefix = "Giga";
            }
            else {
                prefix = "G";
            }
            value /= Math.pow(10, 9);
        }
        else if (value / Math.pow(10, 12) >= 1 && value / Math.pow(10, 15) < 1) {
            if (full) {
                prefix = "Tera";
            }
            else {
                prefix = "T";
            }
            value /= Math.pow(10, 12);
        }
        else if (value / Math.pow(10, 15) >= 1 && value / Math.pow(10, 18) < 1) {
            if (full) {
                prefix = "Peta";
            }
            else {
                prefix = "P";
            }
            value /= Math.pow(10, 15);
        }
        else if (value / Math.pow(10, 18) >= 1 && value / Math.pow(10, 21) < 1) {
            if (full) {
                prefix = "Exa";
            }
            else {
                prefix = "E";
            }
            value /= Math.pow(10, 18);
        }
        else if (value / Math.pow(10, 21) >= 1 && value / Math.pow(10, 24) < 1) {
            if (full) {
                prefix = "Zetta";
            }
            else {
                prefix = "Z";
            }
            value /= Math.pow(10, 21);
        }
        else if (value / Math.pow(10, 24) >= 1 && value / Math.pow(10, 30) < 1) {
            if (full) {
                prefix = "Yotta";
            }
            else {
                prefix = "Y";
            }
            value /= Math.pow(10, 24);
        }
        else {
            return value + unit;
        }
        String format = "%." + decimalPlaces + "f";
        String changedValue = String.format(Locale.ENGLISH, format, value);
        return changedValue + " " + prefix + unit;
    }

    public static String time(double timeInSeconds, int decimalPlaces, boolean full) {
        if (timeInSeconds / Math.pow(10, -27) < 1) {
            if (full) {
                return timeInSeconds + " Seconds";
            }
            return timeInSeconds + " s";
        }
        String prefix;
        if (timeInSeconds / Math.pow(10, -27) >= 1 && timeInSeconds / Math.pow(10, -21) < 1) {
            if (full) {
                prefix = " Yoctoseconds";
            }
            else {
                prefix = " ys";
            }
            timeInSeconds /= Math.pow(10, -24);
        }
        else if (timeInSeconds / Math.pow(10, -21) >= 1 && timeInSeconds / Math.pow(10, -18) < 1) {
            if (full) {
                prefix = " Zeptoseconds";
            }
            else {
                prefix = " zs";
            }
            timeInSeconds /= Math.pow(10, -21);
        }
        else if (timeInSeconds / Math.pow(10, -18) >= 1 && timeInSeconds / Math.pow(10, -15) < 1) {
            if (full) {
                prefix = " Attoseconds";
            }
            else {
                prefix = " as";
            }
            timeInSeconds /= Math.pow(10, -18);
        }
        else if (timeInSeconds / Math.pow(10, -15) >= 1 && timeInSeconds / Math.pow(10, -12) < 1) {
            if (full) {
                prefix = " Femtoseconds";
            }
            else {
                prefix = " fs";
            }
            timeInSeconds /= Math.pow(10, -15);
        }
        else if (timeInSeconds / Math.pow(10, -12) >= 1 && timeInSeconds / Math.pow(10, -9) < 1) {
            if (full) {
                prefix = " Picoseconds";
            }
            else {
                prefix = " ps";
            }
            timeInSeconds /= Math.pow(10, -12);
        }
        else if (timeInSeconds / Math.pow(10, -9) >= 1 && timeInSeconds / Math.pow(10, -6) < 1) {
            if (full) {
                prefix = " Nanoseconds";
            }
            else {
                prefix = " ns";
            }
            timeInSeconds /= Math.pow(10, -9);
        }
        else if (timeInSeconds / Math.pow(10, -6) >= 1 && timeInSeconds / Math.pow(10, -3) < 1) {
            if (full) {
                prefix = " Microseconds";
            }
            else {
                prefix = " µs";
            }
            timeInSeconds /= Math.pow(10, -6);
        }
        else if (timeInSeconds / Math.pow(10, -3) >= 1 && timeInSeconds / Math.pow(10, 0) < 1) {
            if (full) {
                prefix = " Milliseconds";
            }
            else {
                prefix = " ms";
            }
            timeInSeconds /= Math.pow(10, -3);
        }
        else if (timeInSeconds / Math.pow(10, 0) >= 1 && timeInSeconds / SECONDS_IN_A_MINUTE < 1) {
            if (full) {
                prefix = " Seconds";
            }
            else {
                prefix = " s";
            }
            timeInSeconds /= Math.pow(10, 0);
        }
        else if (timeInSeconds / SECONDS_IN_A_MINUTE >= 1 && timeInSeconds / SECONDS_IN_AN_HOUR < 1) {
            int fullMinutes = (int) (timeInSeconds / SECONDS_IN_A_MINUTE);
            int remainingSeconds = (int) (timeInSeconds - fullMinutes * SECONDS_IN_A_MINUTE);
            if (full) {
                return fullMinutes + " Minutes and " + remainingSeconds + " Seconds";
            }
            return fullMinutes + " min " + remainingSeconds + " s";
        }
        else if (timeInSeconds / SECONDS_IN_AN_HOUR >= 1 && timeInSeconds / SECONDS_IN_A_DAY < 1) {
            int fullHours = (int) (timeInSeconds / SECONDS_IN_AN_HOUR);
            int remainingMinutes = (int) ((timeInSeconds - fullHours * SECONDS_IN_AN_HOUR) / SECONDS_IN_A_MINUTE);
            if (full) {
                return fullHours + " Hours and " + remainingMinutes + " Minutes";
            }
            return fullHours + " h " + remainingMinutes + " min";
        }
        else if (timeInSeconds / SECONDS_IN_A_DAY >= 1 && timeInSeconds / SECONDS_IN_A_YEAR < 1) {
            int fullDays = (int) (timeInSeconds / SECONDS_IN_A_DAY);
            int remainingHours = (int) ((timeInSeconds - fullDays * SECONDS_IN_A_DAY) / SECONDS_IN_AN_HOUR);
            if (full) {
                return fullDays + " Days and " + remainingHours + " Hours";
            }
            return fullDays + " d " + remainingHours + " h";
        }
        else if (timeInSeconds / SECONDS_IN_A_YEAR >= 1 && timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 3)) < 1) {
            int fullYears = (int) (timeInSeconds / SECONDS_IN_A_YEAR);
            int remainingDays = (int) ((timeInSeconds - fullYears * SECONDS_IN_A_YEAR) / SECONDS_IN_A_DAY);
            if (full) {
                return fullYears + " Annus and " + remainingDays + " Days";
            }
            return fullYears + " a " + remainingDays + " d";
        }
        else if (timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 3)) >= 1 && timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 6)) < 1) {
            if (full) {
                prefix = " Kiloannum";
            }
            else {
                prefix = " ka";
            }
            timeInSeconds /= SECONDS_IN_A_YEAR * Math.pow(10, 3);
        }
        else if (timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 6)) >= 1 && timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 9)) < 1) {
            if (full) {
                prefix = " Megaannum";
            }
            else {
                prefix = " Ma";
            }
            timeInSeconds /= SECONDS_IN_A_YEAR * Math.pow(10, 6);
        }
        else if (timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 9)) >= 1 && timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 12)) < 1) {
            if (full) {
                prefix = " Gigaannum";
            }
            else {
                prefix = " Ga";
            }
            timeInSeconds /= SECONDS_IN_A_YEAR * Math.pow(10, 9);
        }
        else if (timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 12)) >= 1 && timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 15)) < 1) {
            if (full) {
                prefix = " Teraannum";
            }
            else {
                prefix = " Ta";
            }
            timeInSeconds /= SECONDS_IN_A_YEAR * Math.pow(10, 12);
        }
        else if (timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 15)) >= 1 && timeInSeconds / (SECONDS_IN_A_YEAR * Math.pow(10, 18)) < 1) {
            if (full) {
                prefix = " Petaannum";
            }
            else {
                prefix = " Pa";
            }
            timeInSeconds /= SECONDS_IN_A_YEAR * Math.pow(10, 15);
        }
        else {
            if (full) {
                return timeInSeconds + " Seconds";
            }
            return timeInSeconds + " s";
        }
        String format = "%." + decimalPlaces + "f";
        String changedValue = String.format(Locale.ENGLISH, format, timeInSeconds);
        return changedValue + prefix;
    }
}
