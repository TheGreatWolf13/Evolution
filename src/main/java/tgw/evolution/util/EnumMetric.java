package tgw.evolution.util;

public enum EnumMetric {
    UNDER_METRIC("Underflow", "under", 0.000_000_000_000_000_000_000_000_001),
    YOCTO("Yocto", "y", 0.000_000_000_000_000_000_000_001),
    ZEPTO("Zepto", "z", 0.000_000_000_000_000_000_001),
    ATTO("Atto", "a", 0.000_000_000_000_000_001),
    FEMTO("Femto", "f", 0.000_000_000_000_001),
    PICO("Pico", "p", 0.000_000_000_001),
    NANO("Nano", "n", 0.000_000_001),
    MICRO("Micro", "\u03bc", 0.000_001),
    MILLI("Milli", "m", 0.001),
    NONE("", "", 1),
    KILO("Kilo", "k", 1_000),
    MEGA("Mega", "M", 1_000_000),
    GIGA("Giga", "G", 1_000_000_000),
    TERA("Tera", "T", 1_000_000_000_000.0),
    PETA("Peta", "P", 1_000_000_000_000_000.0),
    EXA("Exa", "E", 1_000_000_000_000_000_000.0),
    ZETTA("Zetta", "Z", 1_000_000_000_000_000_000_000.0),
    YOTTA("Yotta", "Y", 1_000_000_000_000_000_000_000_000.0),
    OVER_METRIC("Overflow", "over", 1_000_000_000_000_000_000_000_000_000.0);

    private final String fullName;
    private final String prefix;
    private final double inNumber;

    EnumMetric(String fullName, String prefix, double inNumber) {
        this.fullName = fullName;
        this.prefix = prefix;
        this.inNumber = inNumber;
    }

    public static double fromMetric(double value, EnumMetric metric) {
        return value * metric.inNumber;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getFullName() {
        return this.fullName;
    }

    public double getInNumber() {
        return this.inNumber;
    }
}
