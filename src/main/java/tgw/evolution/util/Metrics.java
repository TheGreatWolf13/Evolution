package tgw.evolution.util;

import java.util.Locale;

public class Metrics {

    public static final int SECONDS_IN_A_MINUTE = 60;
    public static final int SECONDS_IN_AN_HOUR = 60 * SECONDS_IN_A_MINUTE;
    public static final int SECONDS_IN_A_DAY = 24 * SECONDS_IN_AN_HOUR;
    public static final int SECONDS_IN_A_YEAR = 365 * SECONDS_IN_A_DAY + 6 * SECONDS_IN_AN_HOUR;

    public static String metric(double value, String unit, int decimalPlaces, boolean full) {
        int magnitude = Math.floorDiv(MathHelper.floor(Math.log10(Math.abs(value))), 3);
        EnumMetric metric = magnitude > 0 ? EnumMetric.OVER_METRIC : EnumMetric.UNDER_METRIC;
        switch (magnitude) {
            case -8:
                metric = EnumMetric.YOCTO;
                break;
            case -7:
                metric = EnumMetric.ZEPTO;
                break;
            case -6:
                metric = EnumMetric.ATTO;
                break;
            case -5:
                metric = EnumMetric.FEMTO;
                break;
            case -4:
                metric = EnumMetric.PICO;
                break;
            case -3:
                metric = EnumMetric.NANO;
                break;
            case -2:
                metric = EnumMetric.MICRO;
                break;
            case -1:
                metric = EnumMetric.MILLI;
                break;
            case 0:
                metric = EnumMetric.NONE;
                break;
            case 1:
                metric = EnumMetric.KILO;
                break;
            case 2:
                metric = EnumMetric.MEGA;
                break;
            case 3:
                metric = EnumMetric.GIGA;
                break;
            case 4:
                metric = EnumMetric.TERA;
                break;
            case 5:
                metric = EnumMetric.PETA;
                break;
            case 6:
                metric = EnumMetric.EXA;
                break;
            case 7:
                metric = EnumMetric.ZETTA;
                break;
            case 8:
                metric = EnumMetric.YOTTA;
                break;
        }
        value /= metric.getInNumber();
        String format = "%." + decimalPlaces + "f";
        String changedValue = String.format(Locale.ENGLISH, format, value);
        return changedValue + " " + (full ? metric.getFullName() : metric.getPrefix()) + unit;
    }

    public static String time(double timeInSeconds, int decimalPlaces, boolean full) {
        int magnitude = Math.floorDiv(MathHelper.floor(Math.log10(Math.abs(timeInSeconds))), 3);
        if (magnitude < -8) {
            return timeInSeconds + (full ? " Seconds" : " s");
        }
        EnumMetric metric = EnumMetric.NONE;
        switch (magnitude) {
            case -8:
                metric = EnumMetric.YOCTO;
                break;
            case -7:
                metric = EnumMetric.ZEPTO;
                break;
            case -6:
                metric = EnumMetric.ATTO;
                break;
            case -5:
                metric = EnumMetric.FEMTO;
                break;
            case -4:
                metric = EnumMetric.PICO;
                break;
            case -3:
                metric = EnumMetric.NANO;
                break;
            case -2:
                metric = EnumMetric.MICRO;
                break;
            case -1:
                metric = EnumMetric.MILLI;
                break;
        }
        String format = "%." + decimalPlaces + "f";
        if (metric != EnumMetric.NONE) {
            String changedValue = String.format(Locale.ENGLISH, format, timeInSeconds / metric.getInNumber());
            return changedValue + " " + (full ? metric.getFullName() + "seconds" : metric.getPrefix() + "s");
        }
        if (timeInSeconds / SECONDS_IN_A_MINUTE < 1) {
            String prefix = full ? " Seconds" : " s";
            String changedValue = String.format(Locale.ENGLISH, format, timeInSeconds);
            return changedValue + prefix;
        }
        if (timeInSeconds / SECONDS_IN_AN_HOUR < 1) {
            int fullMinutes = (int) (timeInSeconds / SECONDS_IN_A_MINUTE);
            int remainingSeconds = (int) (timeInSeconds - fullMinutes * SECONDS_IN_A_MINUTE);
            if (full) {
                return fullMinutes + " Minutes and " + remainingSeconds + " Seconds";
            }
            return fullMinutes + " min " + remainingSeconds + " s";
        }
        if (timeInSeconds / SECONDS_IN_A_DAY < 1) {
            int fullHours = (int) (timeInSeconds / SECONDS_IN_AN_HOUR);
            int remainingMinutes = (int) ((timeInSeconds - fullHours * SECONDS_IN_AN_HOUR) / SECONDS_IN_A_MINUTE);
            if (full) {
                return fullHours + " Hours and " + remainingMinutes + " Minutes";
            }
            return fullHours + " h " + remainingMinutes + " min";
        }
        if (timeInSeconds / SECONDS_IN_A_YEAR < 1) {
            int fullDays = (int) (timeInSeconds / SECONDS_IN_A_DAY);
            int remainingHours = (int) ((timeInSeconds - fullDays * SECONDS_IN_A_DAY) / SECONDS_IN_AN_HOUR);
            if (full) {
                return fullDays + " Days and " + remainingHours + " Hours";
            }
            return fullDays + " d " + remainingHours + " h";
        }
        timeInSeconds /= SECONDS_IN_A_YEAR;
        magnitude = MathHelper.floor(Math.log10(Math.abs(timeInSeconds))) / 3;
        metric = EnumMetric.OVER_METRIC;
        String post = full ? "annum" : "a";
        switch (magnitude) {
            case 0:
                metric = EnumMetric.NONE;
                post = full ? "Annum" : "a";
                break;
            case 1:
                metric = EnumMetric.KILO;
                break;
            case 2:
                metric = EnumMetric.MEGA;
                break;
            case 3:
                metric = EnumMetric.GIGA;
                break;
            case 4:
                metric = EnumMetric.TERA;
                break;
            case 5:
                metric = EnumMetric.PETA;
                break;
            case 6:
                metric = EnumMetric.EXA;
                break;
            case 7:
                metric = EnumMetric.ZETTA;
                break;
            case 8:
                metric = EnumMetric.YOTTA;
                break;
        }
        String changedValue = String.format(Locale.ENGLISH, format, timeInSeconds / metric.getInNumber());
        return changedValue + " " + (full ? metric.getFullName() : metric.getPrefix()) + post;
    }
}
