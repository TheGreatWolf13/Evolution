package tgw.evolution.util;

import java.util.Locale;

public final class Metrics {

    public static final int SECONDS_IN_A_MINUTE = 60;
    public static final int SECONDS_IN_AN_HOUR = 60 * SECONDS_IN_A_MINUTE;
    public static final int SECONDS_IN_A_DAY = 24 * SECONDS_IN_AN_HOUR;
    public static final int SECONDS_IN_A_YEAR = 365 * SECONDS_IN_A_DAY + 6 * SECONDS_IN_AN_HOUR;

    private Metrics() {
    }

    public static String metric(double value, String unit, int decimalPlaces, boolean full) {
        int magnitude = Math.floorDiv(MathHelper.floor(Math.log10(Math.abs(value))), 3);
        Metric metric = magnitude > 0 ? Metric.OVER_METRIC : Metric.UNDER_METRIC;
        switch (magnitude) {
            case -8:
                metric = Metric.YOCTO;
                break;
            case -7:
                metric = Metric.ZEPTO;
                break;
            case -6:
                metric = Metric.ATTO;
                break;
            case -5:
                metric = Metric.FEMTO;
                break;
            case -4:
                metric = Metric.PICO;
                break;
            case -3:
                metric = Metric.NANO;
                break;
            case -2:
                metric = Metric.MICRO;
                break;
            case -1:
                metric = Metric.MILLI;
                break;
            case 0:
                metric = Metric.NONE;
                break;
            case 1:
                metric = Metric.KILO;
                break;
            case 2:
                metric = Metric.MEGA;
                break;
            case 3:
                metric = Metric.GIGA;
                break;
            case 4:
                metric = Metric.TERA;
                break;
            case 5:
                metric = Metric.PETA;
                break;
            case 6:
                metric = Metric.EXA;
                break;
            case 7:
                metric = Metric.ZETTA;
                break;
            case 8:
                metric = Metric.YOTTA;
                break;
        }
        value /= metric.getInNumber();
        String format = "%." + decimalPlaces + "f";
        String changedValue = String.format(Locale.ENGLISH, format, value);
        return changedValue + " " + (full ? metric.getFullName() : metric.getPrefix()) + unit;
    }

    public static String metricBytes(double value, int decimalPlaces) {
        int numberOfDivisions = 0;
        while (value > 1_024L) {
            value /= 1_024L;
            numberOfDivisions++;
        }
        Metric metric = Metric.NONE;
        switch (numberOfDivisions) {
            case 1:
                metric = Metric.KILO;
                break;
            case 2:
                metric = Metric.MEGA;
                break;
            case 3:
                metric = Metric.GIGA;
                break;
            case 4:
                metric = Metric.TERA;
                break;
            case 5:
                metric = Metric.PETA;
                break;
        }
        return String.format(Locale.ROOT, "%." + decimalPlaces + "f " + metric.getPrefix() + "B", value);
    }

    public static String time(double timeInSeconds, int decimalPlaces, boolean full) {
        int magnitude = Math.floorDiv(MathHelper.floor(Math.log10(Math.abs(timeInSeconds))), 3);
        if (magnitude < -8) {
            return timeInSeconds + (full ? " Seconds" : " s");
        }
        Metric metric = Metric.NONE;
        switch (magnitude) {
            case -8:
                metric = Metric.YOCTO;
                break;
            case -7:
                metric = Metric.ZEPTO;
                break;
            case -6:
                metric = Metric.ATTO;
                break;
            case -5:
                metric = Metric.FEMTO;
                break;
            case -4:
                metric = Metric.PICO;
                break;
            case -3:
                metric = Metric.NANO;
                break;
            case -2:
                metric = Metric.MICRO;
                break;
            case -1:
                metric = Metric.MILLI;
                break;
        }
        String format = "%." + decimalPlaces + "f";
        if (metric != Metric.NONE) {
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
        metric = Metric.OVER_METRIC;
        String post = full ? "annum" : "a";
        switch (magnitude) {
            case 0:
                metric = Metric.NONE;
                post = full ? "Annum" : "a";
                break;
            case 1:
                metric = Metric.KILO;
                break;
            case 2:
                metric = Metric.MEGA;
                break;
            case 3:
                metric = Metric.GIGA;
                break;
            case 4:
                metric = Metric.TERA;
                break;
            case 5:
                metric = Metric.PETA;
                break;
            case 6:
                metric = Metric.EXA;
                break;
            case 7:
                metric = Metric.ZETTA;
                break;
            case 8:
                metric = Metric.YOTTA;
                break;
        }
        String changedValue = String.format(Locale.ENGLISH, format, timeInSeconds / metric.getInNumber());
        return changedValue + " " + (full ? metric.getFullName() : metric.getPrefix()) + post;
    }
}
