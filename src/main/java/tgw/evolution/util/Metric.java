package tgw.evolution.util;

import tgw.evolution.init.EvolutionTexts;

import java.util.Locale;

public enum Metric {
    UNDER_METRIC("Underflow", "under", 1E-27),
    YOCTO("Yocto", "y", 1E-24),
    ZEPTO("Zepto", "z", 1E-21),
    ATTO("Atto", "a", 1E-18),
    FEMTO("Femto", "f", 1E-15),
    PICO("Pico", "p", 1E-12),
    NANO("Nano", "n", 1E-9),
    MICRO("Micro", "\u03bc", 1E-6),
    MILLI("Milli", "m", 1E-3),
    NONE("", "", 1),
    KILO("Kilo", "k", 1E3),
    MEGA("Mega", "M", 1E6),
    GIGA("Giga", "G", 1E9),
    TERA("Tera", "T", 1E12),
    PETA("Peta", "P", 1E15),
    EXA("Exa", "E", 1E18),
    ZETTA("Zetta", "Z", 1E21),
    YOTTA("Yotta", "Y", 1E24),
    OVER_METRIC("Overflow", "over", 1E27);

    public static final int SECONDS_IN_A_MINUTE = 60;
    public static final int MINUTES_IN_AN_HOUR = 60;
    public static final int HOURS_IN_A_DAY = 24;
    public static final double DAYS_IN_A_YEAR = 365.25;

    private final String fullName;
    private final double inNumber;
    private final String prefix;

    Metric(String fullName, String prefix, double inNumber) {
        this.fullName = fullName;
        this.prefix = prefix;
        this.inNumber = inNumber;
    }

    public static Metric byMagnitude(int magnitude) {
        switch (magnitude) {
            case -9:
                return UNDER_METRIC;
            case -8:
                return YOCTO;
            case -7:
                return ZEPTO;
            case -6:
                return ATTO;
            case -5:
                return FEMTO;
            case -4:
                return PICO;
            case -3:
                return NANO;
            case -2:
                return MICRO;
            case -1:
                return MILLI;
            case 0:
                return NONE;
            case 1:
                return KILO;
            case 2:
                return MEGA;
            case 3:
                return GIGA;
            case 4:
                return TERA;
            case 5:
                return PETA;
            case 6:
                return EXA;
            case 7:
                return ZETTA;
            case 8:
                return YOTTA;
            case 9:
                return OVER_METRIC;
        }
        return magnitude > 0 ? OVER_METRIC : UNDER_METRIC;
    }

    public static String bytes(double value, int decimalPlaces) {
        int magnitude = 0;
        while (value >= 1_024L) {
            value /= 1_024L;
            magnitude++;
        }
        Metric metric = byMagnitude(magnitude);
        return format(value, decimalPlaces) + " " + metric.prefix + "B";
    }

    public static String format(double value, int decimalPlaces) {
        switch (decimalPlaces) {
            case 0:
                return EvolutionTexts.DEFAULT.format(value);
            case 1:
                return EvolutionTexts.ONE_PLACE.format(value);
            case 2:
                return EvolutionTexts.TWO_PLACES.format(value);
            case 3:
                return EvolutionTexts.THREE_PLACES.format(value);
        }
        if (decimalPlaces < 0) {
            throw new IllegalStateException("Negative Decimal Places?");
        }
        return String.format(Locale.ROOT, "%." + decimalPlaces + "f", value);
    }

    public static String format(double value, int decimalPlaces, String unit) {
        boolean spacing = !unit.isEmpty();
        if (Double.isNaN(value)) {
            return spacing ? "NaN " + unit : "NaN";
        }
        if (Double.isInfinite(value)) {
            return spacing ? value + " " + unit : String.valueOf(value);
        }
        if (value == 0) {
            return spacing ? "0 " + unit : "0";
        }
        String sign = value > 0 ? "" : "-";
        value = Math.abs(value);
        int magnitude = 0;
        while (value < 1) {
            value *= 1_000;
            magnitude--;
        }
        while (value >= 1_000) {
            value /= 1_000;
            magnitude++;
        }
        Metric metric = byMagnitude(magnitude);
        return sign + format(value, decimalPlaces) + (spacing ? " " + metric.prefix + unit : metric.prefix);
    }

    public static double fromMetric(double value, Metric metric) {
        return value * metric.inNumber;
    }

    public static String time(double timeInSeconds, int decimalPlaces) {
        if (Double.isNaN(timeInSeconds)) {
            return "NaN s";
        }
        if (Double.isInfinite(timeInSeconds)) {
            return timeInSeconds + " s";
        }
        if (timeInSeconds == 0) {
            return "0 s";
        }
        if (timeInSeconds < 0) {
            return "Negative time?";
        }
        int magnitude = 0;
        while (timeInSeconds < 1) {
            timeInSeconds *= 1_000;
            magnitude--;
        }
        Metric metric = byMagnitude(magnitude);
        if (metric != NONE) {
            return format(timeInSeconds, decimalPlaces) + " " + metric.prefix + "s";
        }
        if (timeInSeconds < SECONDS_IN_A_MINUTE) {
            return format(timeInSeconds, decimalPlaces) + " s";
        }
        double minutes = timeInSeconds / SECONDS_IN_A_MINUTE;
        if (minutes < MINUTES_IN_AN_HOUR) {
            minutes = (int) minutes;
            timeInSeconds -= minutes * SECONDS_IN_A_MINUTE;
            return (int) minutes + " min " + format(timeInSeconds, decimalPlaces) + " s";
        }
        double hours = minutes / MINUTES_IN_AN_HOUR;
        if (hours < HOURS_IN_A_DAY) {
            hours = (int) hours;
            minutes -= hours * MINUTES_IN_AN_HOUR;
            return (int) hours + " h " + format(minutes, decimalPlaces) + " min";
        }
        double days = hours / HOURS_IN_A_DAY;
        if (days < DAYS_IN_A_YEAR) {
            days = (int) days;
            hours -= days * HOURS_IN_A_DAY;
            return (int) days + " d " + format(hours, decimalPlaces) + " h";
        }
        double years = days / DAYS_IN_A_YEAR;
        magnitude = 0;
        while (years > 1_000) {
            years /= 1_000;
            magnitude++;
        }
        metric = byMagnitude(magnitude);
        return format(years, decimalPlaces) + " " + metric.prefix + "a";
    }

    public String getFullName() {
        return this.fullName;
    }

    public double getInNumber() {
        return this.inNumber;
    }

    public String getPrefix() {
        return this.prefix;
    }
}
