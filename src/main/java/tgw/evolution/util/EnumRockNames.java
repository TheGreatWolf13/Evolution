package tgw.evolution.util;

import static tgw.evolution.util.EnumRockType.*;

public enum EnumRockNames {
    ANDESITE(IGNEOUS_EXTRUSIVE, "andesite", 2565, 40000000),
    BASALT(IGNEOUS_EXTRUSIVE, "basalt", 2768, 30000000),
    CHALK(SEDIMENTARY, "chalk", 2499, 4000000),
    CHERT(SEDIMENTARY, "chert", 2564, 9000000),
    CONGLOMERATE(SEDIMENTARY, "conglomerate", 2570, 6000000),
    DACITE(IGNEOUS_EXTRUSIVE, "dacite", 2402, 30000000),
    DIORITE(IGNEOUS_INTRUSIVE, "diorite", 2797, 18000000),
    DOLOMITE(SEDIMENTARY, "dolomite", 2899, 10000000),
    GABBRO(IGNEOUS_INTRUSIVE, "gabbro", 2884, 60000000),
    GNEISS(METAMORPHIC, "gneiss", 2812, 10000000),
    GRANITE(IGNEOUS_INTRUSIVE, "granite", 2640, 20000000),
    LIMESTONE(SEDIMENTARY, "limestone", 2484, 20000000),
    MARBLE(METAMORPHIC, "marble", 2716, 20000000),
    PHYLLITE(METAMORPHIC, "phyllite", 2575, 15000000),
    QUARTZITE(METAMORPHIC, "quartzite", 2612, 7500000),
    RED_SANDSTONE(SEDIMENTARY, "red_sandstone", 2475, 8000000),
    SANDSTONE(SEDIMENTARY, "sandstone", 2463, 8000000),
    SCHIST(METAMORPHIC, "schist", 2732, 5000000),
    SHALE(SEDIMENTARY, "shale", 2335, 5000000),
    SLATE(METAMORPHIC, "slate", 2691, 10000000),
    PEAT(null, "peat", 1156, 0),
    CLAY(null, "clay", 2067, 0);

    private final EnumRockType rockType;
    private final String name;
    private final int density;
    private final int shearStrength;

    EnumRockNames(EnumRockType rockType, String name, int densityInkg, int shearStrengthInPa) {
        this.rockType = rockType;
        this.name = name;
        this.density = densityInkg;
        this.shearStrength = shearStrengthInPa;
    }

    public String getName() {
        return this.name;
    }

    public EnumRockType getRockType() {
        return this.rockType;
    }

    public int getMass() {
        return this.density;
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
