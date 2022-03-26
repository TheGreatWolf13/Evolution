package tgw.evolution.util.math;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
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
    private static final Object2ObjectMap<Locale, DateFormat> DATE_FORMATS = new Object2ObjectOpenHashMap<>();
    private static final DecimalFormatSymbols SYMBOLS = getSymbols();
    public static final DecimalFormat DEFAULT = initFormat(",##0");
    public static final DecimalFormat ONE_PLACE = initFormat(",##0.#");
    public static final DecimalFormat TWO_PLACES = initFormat(",##0.##");
    public static final DecimalFormat THREE_PLACES = initFormat(",##0.###");
    public static final DecimalFormat ONE_PLACE_FULL = initFormat(",##0.0");
    public static final DecimalFormat THREE_PLACES_FULL = initFormat(",##0.000");
    public static final DecimalFormat FIVE_PLACES_FULL = initFormat(",##0.00000");
    public static final DecimalFormat INT_2 = initFormat("00");
    public static final DecimalFormat DAMAGE_FORMAT = initFormat(",##0 HP");
    public static final DecimalFormat HOUR_FORMAT = initFormat(",##0 h");
    public static final DecimalFormat LATITUDE_FORMAT = initFormat("#0.## \u00B0");
    public static final DecimalFormat PERCENT_ONE_PLACE = initFormat(",##0.#%");
    private final String fullName;
    private final double inNumber;
    private final String prefix;

    Metric(String fullName, String prefix, double inNumber) {
        this.fullName = fullName;
        this.prefix = prefix;
        this.inNumber = inNumber;
    }

    public static Metric byMagnitude(int magnitude) {
        return switch (magnitude) {
            case -9 -> UNDER_METRIC;
            case -8 -> YOCTO;
            case -7 -> ZEPTO;
            case -6 -> ATTO;
            case -5 -> FEMTO;
            case -4 -> PICO;
            case -3 -> NANO;
            case -2 -> MICRO;
            case -1 -> MILLI;
            case 0 -> NONE;
            case 1 -> KILO;
            case 2 -> MEGA;
            case 3 -> GIGA;
            case 4 -> TERA;
            case 5 -> PETA;
            case 6 -> EXA;
            case 7 -> ZETTA;
            case 8 -> YOTTA;
            case 9 -> OVER_METRIC;
            default -> magnitude > 0 ? OVER_METRIC : UNDER_METRIC;
        };
    }

    public static String bytes(double value, int decimalPlaces) {
        int magnitude = 0;
        while (value <= -1_024L) {
            value /= 1_024L;
            magnitude++;
        }
        while (value >= 1_024L) {
            value /= 1_024L;
            magnitude++;
        }
        Metric metric = byMagnitude(magnitude);
        return format(value, decimalPlaces) + metric.prefix + "B";
    }

    public static String format(double value, int decimalPlaces) {
        switch (decimalPlaces) {
            case 0 -> {
                return DEFAULT.format(value);
            }
            case 1 -> {
                return ONE_PLACE.format(value);
            }
            case 2 -> {
                return TWO_PLACES.format(value);
            }
            case 3 -> {
                return THREE_PLACES.format(value);
            }
        }
        if (decimalPlaces < 0) {
            throw new IllegalStateException("Negative Decimal Places?");
        }
        return String.format(Locale.ROOT, "%." + decimalPlaces + "f", value);
    }

    public static String format(double value, int decimalPlaces, String unit) {
        return format(value, decimalPlaces, unit, false);
    }

    public static String format(double value, int decimalPlaces, String unit, boolean forceSign) {
        boolean hasUnit = !unit.isEmpty();
        if (Double.isNaN(value)) {
            return hasUnit ? "NaN " + unit : "NaN";
        }
        if (Double.isInfinite(value)) {
            return hasUnit ? value + " " + unit : String.valueOf(value);
        }
        if (value == 0) {
            return hasUnit ? "0" + unit : "0";
        }
        String sign = value > 0 ? forceSign ? "+" : "" : "-";
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
        return sign + format(value, decimalPlaces) + (hasUnit ? metric.prefix + unit : metric.prefix);
    }

    public static String formatForceDecimals(double value, int decimalPlaces) {
        switch (decimalPlaces) {
            case 0 -> {
                return DEFAULT.format(value);
            }
            case 1 -> {
                return ONE_PLACE_FULL.format(value);
            }
            case 3 -> {
                return THREE_PLACES_FULL.format(value);
            }
            case 5 -> {
                return FIVE_PLACES_FULL.format(value);
            }
        }
        if (decimalPlaces < 0) {
            throw new IllegalStateException("Negative Decimal Places?");
        }
        return String.format(Locale.ROOT, "%." + decimalPlaces + "f", value);
    }

    public static double fromMetric(double value, Metric metric) {
        return value * metric.inNumber;
    }

    public static DateFormat getDateFormatter(Locale locale) {
        return DATE_FORMATS.computeIfAbsent(locale, (Locale l) -> new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", l));
    }

    private static DecimalFormatSymbols getSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        return symbols;
    }

    private static DecimalFormat initFormat(String pattern) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        decimalFormat.setDecimalFormatSymbols(SYMBOLS);
        return decimalFormat;
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
