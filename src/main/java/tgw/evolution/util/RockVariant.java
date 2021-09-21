package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;

import javax.annotation.Nullable;

import static tgw.evolution.util.RockType.*;

public enum RockVariant {
    ANDESITE(0, IGNEOUS_EXTRUSIVE, "andesite", 2_565, 40_000_000),
    BASALT(1, IGNEOUS_EXTRUSIVE, "basalt", 2_768, 30_000_000),
    CHALK(2, SEDIMENTARY, "chalk", 2_499, 4_000_000),
    CHERT(3, SEDIMENTARY, "chert", 2_564, 9_000_000),
    CONGLOMERATE(4, SEDIMENTARY, "conglomerate", 2_570, 6_000_000),
    DACITE(5, IGNEOUS_EXTRUSIVE, "dacite", 2_402, 30_000_000),
    DIORITE(6, IGNEOUS_INTRUSIVE, "diorite", 2_797, 18_000_000),
    DOLOMITE(7, SEDIMENTARY, "dolomite", 2_899, 10_000_000),
    GABBRO(8, IGNEOUS_INTRUSIVE, "gabbro", 2_884, 60_000_000),
    GNEISS(9, METAMORPHIC, "gneiss", 2_812, 10_000_000),
    GRANITE(10, IGNEOUS_INTRUSIVE, "granite", 2_640, 20_000_000),
    LIMESTONE(11, SEDIMENTARY, "limestone", 2_484, 20_000_000),
    MARBLE(12, METAMORPHIC, "marble", 2_716, 20_000_000),
    PHYLLITE(13, METAMORPHIC, "phyllite", 2_575, 15_000_000),
    QUARTZITE(14, METAMORPHIC, "quartzite", 2_612, 7_500_000),
    RED_SANDSTONE(15, SEDIMENTARY, "red_sandstone", 2_475, 8_000_000),
    SANDSTONE(16, SEDIMENTARY, "sandstone", 2_463, 8_000_000),
    SCHIST(17, METAMORPHIC, "schist", 2_732, 5_000_000),
    SHALE(18, SEDIMENTARY, "shale", 2_335, 5_000_000),
    SLATE(19, METAMORPHIC, "slate", 2_691, 10_000_000),
    PEAT(20, null, "peat", 1_156, 0),
    CLAY(21, null, "clay", 2_067, 0);

    private final int density;
    private final byte id;
    private final String name;
    private final RockType rockType;
    private final int shearStrength;

    RockVariant(int id, @Nullable RockType rockType, String name, int densityInkg, int shearStrengthInPa) {
        this.id = (byte) id;
        this.rockType = rockType;
        this.name = name;
        this.density = densityInkg;
        this.shearStrength = shearStrengthInPa;
    }

    public static RockVariant fromId(byte id) {
        switch (id) {
            case 0: {
                return ANDESITE;
            }
            case 1: {
                return BASALT;
            }
            case 2: {
                return CHALK;
            }
            case 3: {
                return CHERT;
            }
            case 4: {
                return CONGLOMERATE;
            }
            case 5: {
                return DACITE;
            }
            case 6: {
                return DIORITE;
            }
            case 7: {
                return DOLOMITE;
            }
            case 8: {
                return GABBRO;
            }
            case 9: {
                return GNEISS;
            }
            case 10: {
                return GRANITE;
            }
            case 11: {
                return LIMESTONE;
            }
            case 12: {
                return MARBLE;
            }
            case 13: {
                return PHYLLITE;
            }
            case 14: {
                return QUARTZITE;
            }
            case 15: {
                return RED_SANDSTONE;
            }
            case 16: {
                return SANDSTONE;
            }
            case 17: {
                return SCHIST;
            }
            case 18: {
                return SHALE;
            }
            case 19: {
                return SLATE;
            }
            case 20: {
                return PEAT;
            }
            case 21: {
                return CLAY;
            }
        }
        throw new UnregisteredFeatureException("Unregistered variant for id: " + id);
    }

    public Block fromEnumVanillaRep(VanillaRockVariant vanilla) {
        switch (vanilla) {
            case DIRT: {
                return this.getDirt();
            }
            case COBBLESTONE: {
                return this.getCobble();
            }
            case GRAVEL: {
                return this.getGravel();
            }
            case GRASS: {
                return this.getGrass();
            }
            case SAND: {
                return this.getSand();
            }
            case STONE: {
                return this.getStone();
            }
            case STONE_BRICKS: {
                return this.getStoneBricks();
            }
        }
        throw new UnregisteredFeatureException("Could not find a Block to replace vanilla " + vanilla);
    }

    public Item getAxeHead() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have an axe head!");
            }
            case ANDESITE: {
                return EvolutionItems.axe_head_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.axe_head_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.axe_head_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.axe_head_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.axe_head_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.axe_head_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.axe_head_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.axe_head_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.axe_head_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.axe_head_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.axe_head_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.axe_head_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.axe_head_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.axe_head_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.axe_head_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.axe_head_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.axe_head_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.axe_head_schist.get();
            }
            case SHALE: {
                return EvolutionItems.axe_head_shale.get();
            }
            case SLATE: {
                return EvolutionItems.axe_head_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getCobble() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a cobble type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.COBBLE_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.COBBLE_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.COBBLE_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.COBBLE_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.COBBLE_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.COBBLE_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.COBBLE_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.COBBLE_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.COBBLE_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.COBBLE_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.COBBLE_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.COBBLE_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.COBBLE_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.COBBLE_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.COBBLE_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.COBBLE_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.COBBLE_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.COBBLE_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.COBBLE_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.COBBLE_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getDirt() {
        switch (this) {
            case CLAY: {
                return EvolutionBlocks.CLAY.get();
            }
            case PEAT: {
                return EvolutionBlocks.PEAT.get();
            }
            case ANDESITE: {
                return EvolutionBlocks.DIRT_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.DIRT_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.DIRT_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.DIRT_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.DIRT_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.DIRT_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.DIRT_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.DIRT_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.DIRT_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.DIRT_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.DIRT_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.DIRT_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.DIRT_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.DIRT_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.DIRT_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.DIRT_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.DIRT_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.DIRT_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.DIRT_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.DIRT_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getDryGrass() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a dry grass type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.DRY_GRASS_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.DRY_GRASS_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.DRY_GRASS_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.DRY_GRASS_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.DRY_GRASS_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.DRY_GRASS_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.DRY_GRASS_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.DRY_GRASS_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.DRY_GRASS_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.DRY_GRASS_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.DRY_GRASS_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.DRY_GRASS_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.DRY_GRASS_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.DRY_GRASS_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.DRY_GRASS_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.DRY_GRASS_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.DRY_GRASS_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.DRY_GRASS_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.DRY_GRASS_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.DRY_GRASS_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getGrass() {
        switch (this) {
            case CLAY: {
                return EvolutionBlocks.GRASS_CLAY.get();
            }
            case PEAT: {
                return EvolutionBlocks.GRASS_PEAT.get();
            }
            case ANDESITE: {
                return EvolutionBlocks.GRASS_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.GRASS_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.GRASS_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.GRASS_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.GRASS_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.GRASS_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.GRASS_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.GRASS_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.GRASS_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.GRASS_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.GRASS_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.GRASS_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.GRASS_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.GRASS_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.GRASS_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.GRASS_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.GRASS_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.GRASS_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.GRASS_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.GRASS_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getGravel() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a gravel type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.GRAVEL_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.GRAVEL_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.GRAVEL_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.GRAVEL_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.GRAVEL_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.GRAVEL_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.GRAVEL_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.GRAVEL_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.GRAVEL_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.GRAVEL_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.GRAVEL_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.GRAVEL_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.GRAVEL_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.GRAVEL_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.GRAVEL_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.GRAVEL_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.GRAVEL_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.GRAVEL_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.GRAVEL_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.GRAVEL_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Item getHammerHead() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a hammer head!");
            }
            case ANDESITE: {
                return EvolutionItems.hammer_head_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.hammer_head_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.hammer_head_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.hammer_head_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.hammer_head_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.hammer_head_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.hammer_head_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.hammer_head_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.hammer_head_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.hammer_head_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.hammer_head_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.hammer_head_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.hammer_head_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.hammer_head_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.hammer_head_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.hammer_head_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.hammer_head_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.hammer_head_schist.get();
            }
            case SHALE: {
                return EvolutionItems.hammer_head_shale.get();
            }
            case SLATE: {
                return EvolutionItems.hammer_head_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Item getHoeHead() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a hoe head!");
            }
            case ANDESITE: {
                return EvolutionItems.hoe_head_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.hoe_head_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.hoe_head_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.hoe_head_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.hoe_head_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.hoe_head_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.hoe_head_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.hoe_head_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.hoe_head_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.hoe_head_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.hoe_head_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.hoe_head_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.hoe_head_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.hoe_head_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.hoe_head_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.hoe_head_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.hoe_head_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.hoe_head_schist.get();
            }
            case SHALE: {
                return EvolutionItems.hoe_head_shale.get();
            }
            case SLATE: {
                return EvolutionItems.hoe_head_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public byte getId() {
        return this.id;
    }

    public Item getJavelin() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a javelin!");
            }
            case ANDESITE: {
                return EvolutionItems.javelin_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.javelin_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.javelin_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.javelin_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.javelin_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.javelin_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.javelin_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.javelin_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.javelin_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.javelin_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.javelin_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.javelin_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.javelin_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.javelin_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.javelin_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.javelin_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.javelin_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.javelin_schist.get();
            }
            case SHALE: {
                return EvolutionItems.javelin_shale.get();
            }
            case SLATE: {
                return EvolutionItems.javelin_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Item getJavelinHead() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a javelin head!");
            }
            case ANDESITE: {
                return EvolutionItems.javelin_head_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.javelin_head_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.javelin_head_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.javelin_head_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.javelin_head_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.javelin_head_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.javelin_head_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.javelin_head_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.javelin_head_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.javelin_head_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.javelin_head_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.javelin_head_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.javelin_head_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.javelin_head_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.javelin_head_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.javelin_head_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.javelin_head_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.javelin_head_schist.get();
            }
            case SHALE: {
                return EvolutionItems.javelin_head_shale.get();
            }
            case SLATE: {
                return EvolutionItems.javelin_head_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public ItemStack getKnappedStack(KnappingRecipe knapping) {
        switch (knapping) {
            case AXE: {
                return new ItemStack(this.getAxeHead());
            }
            case HOE: {
                return new ItemStack(this.getHoeHead());
            }
            case NULL: {
                return new ItemStack(this.getRock());
            }
            case KNIFE: {
                return new ItemStack(this.getKnifeBlade());
            }
            case HAMMER: {
                return new ItemStack(this.getHammerHead());
            }
            case SHOVEL: {
                return new ItemStack(this.getShovelHead());
            }
            case JAVELIN: {
                return new ItemStack(this.getJavelinHead());
            }
        }
        throw new UnregisteredFeatureException("Unregistered KnappingRecipe " + knapping);
    }

    public Block getKnapping() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a knapping type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.KNAPPING_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.KNAPPING_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.KNAPPING_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.KNAPPING_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.KNAPPING_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.KNAPPING_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.KNAPPING_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.KNAPPING_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.KNAPPING_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.KNAPPING_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.KNAPPING_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.KNAPPING_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.KNAPPING_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.KNAPPING_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.KNAPPING_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.KNAPPING_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.KNAPPING_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.KNAPPING_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.KNAPPING_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.KNAPPING_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Item getKnifeBlade() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a knife blade!");
            }
            case ANDESITE: {
                return EvolutionItems.knife_blade_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.knife_blade_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.knife_blade_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.knife_blade_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.knife_blade_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.knife_blade_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.knife_blade_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.knife_blade_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.knife_blade_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.knife_blade_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.knife_blade_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.knife_blade_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.knife_blade_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.knife_blade_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.knife_blade_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.knife_blade_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.knife_blade_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.knife_blade_schist.get();
            }
            case SHALE: {
                return EvolutionItems.knife_blade_shale.get();
            }
            case SLATE: {
                return EvolutionItems.knife_blade_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public int getMass() {
        return this.density;
    }

    public String getName() {
        return this.name;
    }

    public Block getRock() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a rock type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.ROCK_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.ROCK_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.ROCK_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.ROCK_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.ROCK_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.ROCK_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.ROCK_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.ROCK_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.ROCK_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.ROCK_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.ROCK_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.ROCK_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.ROCK_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.ROCK_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.ROCK_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.ROCK_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.ROCK_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.ROCK_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.ROCK_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.ROCK_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public RockType getRockType() {
        return this.rockType;
    }

    public Block getSand() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a sand type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.SAND_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.SAND_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.SAND_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.SAND_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.SAND_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.SAND_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.SAND_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.SAND_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.SAND_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.SAND_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.SAND_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.SAND_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.SAND_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.SAND_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.SAND_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.SAND_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.SAND_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.SAND_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.SAND_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.SAND_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public int getShearStrength() {
        return this.shearStrength;
    }

    public Item getShovelHead() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a shovel head!");
            }
            case ANDESITE: {
                return EvolutionItems.shovel_head_andesite.get();
            }
            case BASALT: {
                return EvolutionItems.shovel_head_basalt.get();
            }
            case CHALK: {
                return EvolutionItems.shovel_head_chalk.get();
            }
            case CHERT: {
                return EvolutionItems.shovel_head_chert.get();
            }
            case CONGLOMERATE: {
                return EvolutionItems.shovel_head_conglomerate.get();
            }
            case DACITE: {
                return EvolutionItems.shovel_head_dacite.get();
            }
            case DIORITE: {
                return EvolutionItems.shovel_head_diorite.get();
            }
            case DOLOMITE: {
                return EvolutionItems.shovel_head_dolomite.get();
            }
            case GABBRO: {
                return EvolutionItems.shovel_head_gabbro.get();
            }
            case GNEISS: {
                return EvolutionItems.shovel_head_gneiss.get();
            }
            case GRANITE: {
                return EvolutionItems.shovel_head_granite.get();
            }
            case LIMESTONE: {
                return EvolutionItems.shovel_head_limestone.get();
            }
            case MARBLE: {
                return EvolutionItems.shovel_head_marble.get();
            }
            case PHYLLITE: {
                return EvolutionItems.shovel_head_phyllite.get();
            }
            case QUARTZITE: {
                return EvolutionItems.shovel_head_quartzite.get();
            }
            case RED_SANDSTONE: {
                return EvolutionItems.shovel_head_red_sandstone.get();
            }
            case SANDSTONE: {
                return EvolutionItems.shovel_head_sandstone.get();
            }
            case SCHIST: {
                return EvolutionItems.shovel_head_schist.get();
            }
            case SHALE: {
                return EvolutionItems.shovel_head_shale.get();
            }
            case SLATE: {
                return EvolutionItems.shovel_head_slate.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getStone() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a stone type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.STONE_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.STONE_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.STONE_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.STONE_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.STONE_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.STONE_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.STONE_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.STONE_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.STONE_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.STONE_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.STONE_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.STONE_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.STONE_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.STONE_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.STONE_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.STONE_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.STONE_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.STONE_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.STONE_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.STONE_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }

    public Block getStoneBricks() {
        switch (this) {
            case CLAY:
            case PEAT: {
                throw new IllegalStateException("This variant does not have a stone bricks type!");
            }
            case ANDESITE: {
                return EvolutionBlocks.STONE_BRICKS_ANDESITE.get();
            }
            case BASALT: {
                return EvolutionBlocks.STONE_BRICKS_BASALT.get();
            }
            case CHALK: {
                return EvolutionBlocks.STONE_BRICKS_CHALK.get();
            }
            case CHERT: {
                return EvolutionBlocks.STONE_BRICKS_CHERT.get();
            }
            case CONGLOMERATE: {
                return EvolutionBlocks.STONE_BRICKS_CONGLOMERATE.get();
            }
            case DACITE: {
                return EvolutionBlocks.STONE_BRICKS_DACITE.get();
            }
            case DIORITE: {
                return EvolutionBlocks.STONE_BRICKS_DIORITE.get();
            }
            case DOLOMITE: {
                return EvolutionBlocks.STONE_BRICKS_DOLOMITE.get();
            }
            case GABBRO: {
                return EvolutionBlocks.STONE_BRICKS_GABBRO.get();
            }
            case GNEISS: {
                return EvolutionBlocks.STONE_BRICKS_GNEISS.get();
            }
            case GRANITE: {
                return EvolutionBlocks.STONE_BRICKS_GRANITE.get();
            }
            case LIMESTONE: {
                return EvolutionBlocks.STONE_BRICKS_LIMESTONE.get();
            }
            case MARBLE: {
                return EvolutionBlocks.STONE_BRICKS_MARBLE.get();
            }
            case PHYLLITE: {
                return EvolutionBlocks.STONE_BRICKS_PHYLLITE.get();
            }
            case QUARTZITE: {
                return EvolutionBlocks.STONE_BRICKS_QUARTZITE.get();
            }
            case RED_SANDSTONE: {
                return EvolutionBlocks.STONE_BRICKS_RED_SANDSTONE.get();
            }
            case SANDSTONE: {
                return EvolutionBlocks.STONE_BRICKS_SANDSTONE.get();
            }
            case SCHIST: {
                return EvolutionBlocks.STONE_BRICKS_SCHIST.get();
            }
            case SHALE: {
                return EvolutionBlocks.STONE_BRICKS_SHALE.get();
            }
            case SLATE: {
                return EvolutionBlocks.STONE_BRICKS_SLATE.get();
            }
        }
        throw new UnregisteredFeatureException("Unregistered RockVariant: " + this);
    }
}
