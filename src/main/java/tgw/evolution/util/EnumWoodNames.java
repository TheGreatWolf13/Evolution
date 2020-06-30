package tgw.evolution.util;

public enum EnumWoodNames {
    ACACIA("acacia", 750, 14412500),
    ASPEN("aspen", 420, 6650000),
    BIRCH("birch", 640, 12233333),
    CEDAR("cedar", 530, 6916667),
    EBONY("ebony", 1220, 16250000),
    ELM("elm", 570, 11600000),
    EUCALYPTUS("eucalyptus", 490, 10500000),
    FIR("fir", 633, 7214286),
    KAPOK("kapok", 230, 8500000),
    MANGROVE("mangrove", 485, 5750000),
    MAPLE("maple", 685, 12800000),
    OAK("oak", 750, 13612500),
    OLD_OAK("old_oak", 830, 12836400),
    PALM("palm", 500, 5000000),
    PINE("pine", 840, 8753333),
    REDWOOD("redwood", 480, 7050000),
    SPRUCE("spruce", 440, 8100000),
    WILLOW("willow", 500, 7850000);

    private final String name;
    private final int shearStrength;
    private final int density;

    EnumWoodNames(String name, int density, int shearStrength) {
        this.name = name;
        this.shearStrength = shearStrength;
        this.density = density;
    }

    public String getName() {
        return this.name;
    }

    public int getMass() {
        return this.density;
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
