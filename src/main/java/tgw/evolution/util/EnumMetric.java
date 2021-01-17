package tgw.evolution.util;

public enum EnumMetric {
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

    private final String fullName;
    private final double inNumber;
    private final String prefix;

    EnumMetric(String fullName, String prefix, double inNumber) {
        this.fullName = fullName;
        this.prefix = prefix;
        this.inNumber = inNumber;
    }

    public static double fromMetric(double value, EnumMetric metric) {
        return value * metric.inNumber;
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
