package tgw.evolution.util;

import javax.annotation.Nullable;

import static tgw.evolution.util.EnumRockType.*;

public enum EnumRockNames {
    ANDESITE(IGNEOUS_EXTRUSIVE, "andesite", 2_565, 40_000_000),
    BASALT(IGNEOUS_EXTRUSIVE, "basalt", 2_768, 30_000_000),
    CHALK(SEDIMENTARY, "chalk", 2_499, 4_000_000),
    CHERT(SEDIMENTARY, "chert", 2_564, 9_000_000),
    CONGLOMERATE(SEDIMENTARY, "conglomerate", 2_570, 6_000_000),
    DACITE(IGNEOUS_EXTRUSIVE, "dacite", 2_402, 30_000_000),
    DIORITE(IGNEOUS_INTRUSIVE, "diorite", 2_797, 18_000_000),
    DOLOMITE(SEDIMENTARY, "dolomite", 2_899, 10_000_000),
    GABBRO(IGNEOUS_INTRUSIVE, "gabbro", 2_884, 60_000_000),
    GNEISS(METAMORPHIC, "gneiss", 2_812, 10_000_000),
    GRANITE(IGNEOUS_INTRUSIVE, "granite", 2_640, 20_000_000),
    LIMESTONE(SEDIMENTARY, "limestone", 2_484, 20_000_000),
    MARBLE(METAMORPHIC, "marble", 2_716, 20_000_000),
    PHYLLITE(METAMORPHIC, "phyllite", 2_575, 15_000_000),
    QUARTZITE(METAMORPHIC, "quartzite", 2_612, 7_500_000),
    RED_SANDSTONE(SEDIMENTARY, "red_sandstone", 2_475, 8_000_000),
    SANDSTONE(SEDIMENTARY, "sandstone", 2_463, 8_000_000),
    SCHIST(METAMORPHIC, "schist", 2_732, 5_000_000),
    SHALE(SEDIMENTARY, "shale", 2_335, 5_000_000),
    SLATE(METAMORPHIC, "slate", 2_691, 10_000_000),
    PEAT(null, "peat", 1_156, 0),
    CLAY(null, "clay", 2_067, 0);

    private final EnumRockType rockType;
    private final String name;
    private final int density;
    private final int shearStrength;

    EnumRockNames(@Nullable EnumRockType rockType, String name, int densityInkg, int shearStrengthInPa) {
        this.rockType = rockType;
        this.name = name;
        this.density = densityInkg;
        this.shearStrength = shearStrengthInPa;
    }

    public int getMass() {
        return this.density;
    }

    public String getName() {
        return this.name;
    }

    public EnumRockType getRockType() {
        return this.rockType;
    }

    public int getShearStrength() {
        return this.shearStrength;
    }
}
