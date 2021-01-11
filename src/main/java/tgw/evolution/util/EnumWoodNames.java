package tgw.evolution.util;

public enum EnumWoodNames {
    ACACIA("acacia", 750, 14_412_500),
    ASPEN("aspen", 420, 6_650_000),
    BIRCH("birch", 640, 12_233_333),
    CEDAR("cedar", 530, 6_916_667),
    EBONY("ebony", 1_220, 16_250_000),
    ELM("elm", 570, 11_600_000),
    EUCALYPTUS("eucalyptus", 490, 10_500_000),
    FIR("fir", 633, 7_214_286),
    KAPOK("kapok", 230, 8_500_000),
    MANGROVE("mangrove", 485, 5_750_000),
    MAPLE("maple", 685, 12_800_000),
    OAK("oak", 750, 13_612_500),
    OLD_OAK("old_oak", 830, 12_836_400),
    PALM("palm", 500, 5_000_000),
    PINE("pine", 840, 8_753_333),
    REDWOOD("redwood", 480, 7_050_000),
    SPRUCE("spruce", 440, 8_100_000),
    WILLOW("willow", 500, 7_850_000);

    private final int density;
    private final String name;
    private final int shearStrength;

    EnumWoodNames(String name, int density, int shearStrength) {
        this.name = name;
        this.shearStrength = shearStrength;
        this.density = density;
    }

    public int getMass() {
        return this.density;
    }

    public String getName() {
        return this.name;
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
