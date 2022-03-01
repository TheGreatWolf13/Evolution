package tgw.evolution.util.constants;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.UnregisteredFeatureException;

import javax.annotation.Nullable;

import static tgw.evolution.util.constants.RockType.*;

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

    public static final RockVariant[] VALUES = values();
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
        return switch (id) {
            case 0 -> ANDESITE;
            case 1 -> BASALT;
            case 2 -> CHALK;
            case 3 -> CHERT;
            case 4 -> CONGLOMERATE;
            case 5 -> DACITE;
            case 6 -> DIORITE;
            case 7 -> DOLOMITE;
            case 8 -> GABBRO;
            case 9 -> GNEISS;
            case 10 -> GRANITE;
            case 11 -> LIMESTONE;
            case 12 -> MARBLE;
            case 13 -> PHYLLITE;
            case 14 -> QUARTZITE;
            case 15 -> RED_SANDSTONE;
            case 16 -> SANDSTONE;
            case 17 -> SCHIST;
            case 18 -> SHALE;
            case 19 -> SLATE;
            case 20 -> PEAT;
            case 21 -> CLAY;
            default -> throw new UnregisteredFeatureException("Unregistered variant for id: " + id);
        };
    }

    public Block fromEnumVanillaRep(VanillaRockVariant vanilla) {
        return switch (vanilla) {
            case DIRT -> this.getDirt();
            case COBBLESTONE -> this.getCobble();
            case GRAVEL -> this.getGravel();
            case GRASS -> this.getGrass();
            case SAND -> this.getSand();
            case STONE -> this.getStone();
            case STONE_BRICKS -> this.getStoneBricks();
        };
    }

    public Item getAxeHead() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have an axe head!");
            case ANDESITE -> EvolutionItems.axe_head_andesite.get();
            case BASALT -> EvolutionItems.axe_head_basalt.get();
            case CHALK -> EvolutionItems.axe_head_chalk.get();
            case CHERT -> EvolutionItems.axe_head_chert.get();
            case CONGLOMERATE -> EvolutionItems.axe_head_conglomerate.get();
            case DACITE -> EvolutionItems.axe_head_dacite.get();
            case DIORITE -> EvolutionItems.axe_head_diorite.get();
            case DOLOMITE -> EvolutionItems.axe_head_dolomite.get();
            case GABBRO -> EvolutionItems.axe_head_gabbro.get();
            case GNEISS -> EvolutionItems.axe_head_gneiss.get();
            case GRANITE -> EvolutionItems.axe_head_granite.get();
            case LIMESTONE -> EvolutionItems.axe_head_limestone.get();
            case MARBLE -> EvolutionItems.axe_head_marble.get();
            case PHYLLITE -> EvolutionItems.axe_head_phyllite.get();
            case QUARTZITE -> EvolutionItems.axe_head_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.axe_head_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.axe_head_sandstone.get();
            case SCHIST -> EvolutionItems.axe_head_schist.get();
            case SHALE -> EvolutionItems.axe_head_shale.get();
            case SLATE -> EvolutionItems.axe_head_slate.get();
        };
    }

    public Block getCobble() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a cobble type!");
            case ANDESITE -> EvolutionBlocks.COBBLE_ANDESITE.get();
            case BASALT -> EvolutionBlocks.COBBLE_BASALT.get();
            case CHALK -> EvolutionBlocks.COBBLE_CHALK.get();
            case CHERT -> EvolutionBlocks.COBBLE_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.COBBLE_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.COBBLE_DACITE.get();
            case DIORITE -> EvolutionBlocks.COBBLE_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.COBBLE_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.COBBLE_GABBRO.get();
            case GNEISS -> EvolutionBlocks.COBBLE_GNEISS.get();
            case GRANITE -> EvolutionBlocks.COBBLE_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.COBBLE_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.COBBLE_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.COBBLE_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.COBBLE_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.COBBLE_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.COBBLE_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.COBBLE_SCHIST.get();
            case SHALE -> EvolutionBlocks.COBBLE_SHALE.get();
            case SLATE -> EvolutionBlocks.COBBLE_SLATE.get();
        };
    }

    public Block getDirt() {
        return switch (this) {
            case CLAY -> EvolutionBlocks.CLAY.get();
            case PEAT -> EvolutionBlocks.PEAT.get();
            case ANDESITE -> EvolutionBlocks.DIRT_ANDESITE.get();
            case BASALT -> EvolutionBlocks.DIRT_BASALT.get();
            case CHALK -> EvolutionBlocks.DIRT_CHALK.get();
            case CHERT -> EvolutionBlocks.DIRT_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.DIRT_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.DIRT_DACITE.get();
            case DIORITE -> EvolutionBlocks.DIRT_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.DIRT_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.DIRT_GABBRO.get();
            case GNEISS -> EvolutionBlocks.DIRT_GNEISS.get();
            case GRANITE -> EvolutionBlocks.DIRT_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.DIRT_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.DIRT_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.DIRT_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.DIRT_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.DIRT_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.DIRT_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.DIRT_SCHIST.get();
            case SHALE -> EvolutionBlocks.DIRT_SHALE.get();
            case SLATE -> EvolutionBlocks.DIRT_SLATE.get();
        };
    }

    public Block getDryGrass() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a dry grass type!");
            case ANDESITE -> EvolutionBlocks.DRY_GRASS_ANDESITE.get();
            case BASALT -> EvolutionBlocks.DRY_GRASS_BASALT.get();
            case CHALK -> EvolutionBlocks.DRY_GRASS_CHALK.get();
            case CHERT -> EvolutionBlocks.DRY_GRASS_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.DRY_GRASS_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.DRY_GRASS_DACITE.get();
            case DIORITE -> EvolutionBlocks.DRY_GRASS_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.DRY_GRASS_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.DRY_GRASS_GABBRO.get();
            case GNEISS -> EvolutionBlocks.DRY_GRASS_GNEISS.get();
            case GRANITE -> EvolutionBlocks.DRY_GRASS_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.DRY_GRASS_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.DRY_GRASS_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.DRY_GRASS_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.DRY_GRASS_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.DRY_GRASS_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.DRY_GRASS_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.DRY_GRASS_SCHIST.get();
            case SHALE -> EvolutionBlocks.DRY_GRASS_SHALE.get();
            case SLATE -> EvolutionBlocks.DRY_GRASS_SLATE.get();
        };
    }

    public Block getGrass() {
        return switch (this) {
            case CLAY -> EvolutionBlocks.GRASS_CLAY.get();
            case PEAT -> EvolutionBlocks.GRASS_PEAT.get();
            case ANDESITE -> EvolutionBlocks.GRASS_ANDESITE.get();
            case BASALT -> EvolutionBlocks.GRASS_BASALT.get();
            case CHALK -> EvolutionBlocks.GRASS_CHALK.get();
            case CHERT -> EvolutionBlocks.GRASS_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.GRASS_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.GRASS_DACITE.get();
            case DIORITE -> EvolutionBlocks.GRASS_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.GRASS_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.GRASS_GABBRO.get();
            case GNEISS -> EvolutionBlocks.GRASS_GNEISS.get();
            case GRANITE -> EvolutionBlocks.GRASS_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.GRASS_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.GRASS_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.GRASS_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.GRASS_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.GRASS_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.GRASS_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.GRASS_SCHIST.get();
            case SHALE -> EvolutionBlocks.GRASS_SHALE.get();
            case SLATE -> EvolutionBlocks.GRASS_SLATE.get();
        };
    }

    public Block getGravel() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a gravel type!");
            case ANDESITE -> EvolutionBlocks.GRAVEL_ANDESITE.get();
            case BASALT -> EvolutionBlocks.GRAVEL_BASALT.get();
            case CHALK -> EvolutionBlocks.GRAVEL_CHALK.get();
            case CHERT -> EvolutionBlocks.GRAVEL_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.GRAVEL_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.GRAVEL_DACITE.get();
            case DIORITE -> EvolutionBlocks.GRAVEL_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.GRAVEL_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.GRAVEL_GABBRO.get();
            case GNEISS -> EvolutionBlocks.GRAVEL_GNEISS.get();
            case GRANITE -> EvolutionBlocks.GRAVEL_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.GRAVEL_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.GRAVEL_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.GRAVEL_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.GRAVEL_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.GRAVEL_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.GRAVEL_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.GRAVEL_SCHIST.get();
            case SHALE -> EvolutionBlocks.GRAVEL_SHALE.get();
            case SLATE -> EvolutionBlocks.GRAVEL_SLATE.get();
        };
    }

    public Item getHammerHead() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a hammer head!");
            case ANDESITE -> EvolutionItems.hammer_head_andesite.get();
            case BASALT -> EvolutionItems.hammer_head_basalt.get();
            case CHALK -> EvolutionItems.hammer_head_chalk.get();
            case CHERT -> EvolutionItems.hammer_head_chert.get();
            case CONGLOMERATE -> EvolutionItems.hammer_head_conglomerate.get();
            case DACITE -> EvolutionItems.hammer_head_dacite.get();
            case DIORITE -> EvolutionItems.hammer_head_diorite.get();
            case DOLOMITE -> EvolutionItems.hammer_head_dolomite.get();
            case GABBRO -> EvolutionItems.hammer_head_gabbro.get();
            case GNEISS -> EvolutionItems.hammer_head_gneiss.get();
            case GRANITE -> EvolutionItems.hammer_head_granite.get();
            case LIMESTONE -> EvolutionItems.hammer_head_limestone.get();
            case MARBLE -> EvolutionItems.hammer_head_marble.get();
            case PHYLLITE -> EvolutionItems.hammer_head_phyllite.get();
            case QUARTZITE -> EvolutionItems.hammer_head_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.hammer_head_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.hammer_head_sandstone.get();
            case SCHIST -> EvolutionItems.hammer_head_schist.get();
            case SHALE -> EvolutionItems.hammer_head_shale.get();
            case SLATE -> EvolutionItems.hammer_head_slate.get();
        };
    }

    public Item getHoeHead() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a hoe head!");
            case ANDESITE -> EvolutionItems.hoe_head_andesite.get();
            case BASALT -> EvolutionItems.hoe_head_basalt.get();
            case CHALK -> EvolutionItems.hoe_head_chalk.get();
            case CHERT -> EvolutionItems.hoe_head_chert.get();
            case CONGLOMERATE -> EvolutionItems.hoe_head_conglomerate.get();
            case DACITE -> EvolutionItems.hoe_head_dacite.get();
            case DIORITE -> EvolutionItems.hoe_head_diorite.get();
            case DOLOMITE -> EvolutionItems.hoe_head_dolomite.get();
            case GABBRO -> EvolutionItems.hoe_head_gabbro.get();
            case GNEISS -> EvolutionItems.hoe_head_gneiss.get();
            case GRANITE -> EvolutionItems.hoe_head_granite.get();
            case LIMESTONE -> EvolutionItems.hoe_head_limestone.get();
            case MARBLE -> EvolutionItems.hoe_head_marble.get();
            case PHYLLITE -> EvolutionItems.hoe_head_phyllite.get();
            case QUARTZITE -> EvolutionItems.hoe_head_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.hoe_head_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.hoe_head_sandstone.get();
            case SCHIST -> EvolutionItems.hoe_head_schist.get();
            case SHALE -> EvolutionItems.hoe_head_shale.get();
            case SLATE -> EvolutionItems.hoe_head_slate.get();
        };
    }

    public byte getId() {
        return this.id;
    }

    public Item getJavelin() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a javelin!");
            case ANDESITE -> EvolutionItems.javelin_andesite.get();
            case BASALT -> EvolutionItems.javelin_basalt.get();
            case CHALK -> EvolutionItems.javelin_chalk.get();
            case CHERT -> EvolutionItems.javelin_chert.get();
            case CONGLOMERATE -> EvolutionItems.javelin_conglomerate.get();
            case DACITE -> EvolutionItems.javelin_dacite.get();
            case DIORITE -> EvolutionItems.javelin_diorite.get();
            case DOLOMITE -> EvolutionItems.javelin_dolomite.get();
            case GABBRO -> EvolutionItems.javelin_gabbro.get();
            case GNEISS -> EvolutionItems.javelin_gneiss.get();
            case GRANITE -> EvolutionItems.javelin_granite.get();
            case LIMESTONE -> EvolutionItems.javelin_limestone.get();
            case MARBLE -> EvolutionItems.javelin_marble.get();
            case PHYLLITE -> EvolutionItems.javelin_phyllite.get();
            case QUARTZITE -> EvolutionItems.javelin_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.javelin_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.javelin_sandstone.get();
            case SCHIST -> EvolutionItems.javelin_schist.get();
            case SHALE -> EvolutionItems.javelin_shale.get();
            case SLATE -> EvolutionItems.javelin_slate.get();
        };
    }

    public Item getJavelinHead() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a javelin head!");
            case ANDESITE -> EvolutionItems.javelin_head_andesite.get();
            case BASALT -> EvolutionItems.javelin_head_basalt.get();
            case CHALK -> EvolutionItems.javelin_head_chalk.get();
            case CHERT -> EvolutionItems.javelin_head_chert.get();
            case CONGLOMERATE -> EvolutionItems.javelin_head_conglomerate.get();
            case DACITE -> EvolutionItems.javelin_head_dacite.get();
            case DIORITE -> EvolutionItems.javelin_head_diorite.get();
            case DOLOMITE -> EvolutionItems.javelin_head_dolomite.get();
            case GABBRO -> EvolutionItems.javelin_head_gabbro.get();
            case GNEISS -> EvolutionItems.javelin_head_gneiss.get();
            case GRANITE -> EvolutionItems.javelin_head_granite.get();
            case LIMESTONE -> EvolutionItems.javelin_head_limestone.get();
            case MARBLE -> EvolutionItems.javelin_head_marble.get();
            case PHYLLITE -> EvolutionItems.javelin_head_phyllite.get();
            case QUARTZITE -> EvolutionItems.javelin_head_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.javelin_head_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.javelin_head_sandstone.get();
            case SCHIST -> EvolutionItems.javelin_head_schist.get();
            case SHALE -> EvolutionItems.javelin_head_shale.get();
            case SLATE -> EvolutionItems.javelin_head_slate.get();
        };
    }

    public ItemStack getKnappedStack(KnappingRecipe knapping) {
        return switch (knapping) {
            case AXE -> new ItemStack(this.getAxeHead());
            case HOE -> new ItemStack(this.getHoeHead());
            case NULL -> new ItemStack(this.getRock());
            case KNIFE -> new ItemStack(this.getKnifeBlade());
            case HAMMER -> new ItemStack(this.getHammerHead());
            case SHOVEL -> new ItemStack(this.getShovelHead());
            case JAVELIN -> new ItemStack(this.getJavelinHead());
        };
    }

    public Block getKnapping() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a knapping type!");
            case ANDESITE -> EvolutionBlocks.KNAPPING_ANDESITE.get();
            case BASALT -> EvolutionBlocks.KNAPPING_BASALT.get();
            case CHALK -> EvolutionBlocks.KNAPPING_CHALK.get();
            case CHERT -> EvolutionBlocks.KNAPPING_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.KNAPPING_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.KNAPPING_DACITE.get();
            case DIORITE -> EvolutionBlocks.KNAPPING_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.KNAPPING_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.KNAPPING_GABBRO.get();
            case GNEISS -> EvolutionBlocks.KNAPPING_GNEISS.get();
            case GRANITE -> EvolutionBlocks.KNAPPING_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.KNAPPING_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.KNAPPING_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.KNAPPING_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.KNAPPING_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.KNAPPING_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.KNAPPING_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.KNAPPING_SCHIST.get();
            case SHALE -> EvolutionBlocks.KNAPPING_SHALE.get();
            case SLATE -> EvolutionBlocks.KNAPPING_SLATE.get();
        };
    }

    public Item getKnifeBlade() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a knife blade!");
            case ANDESITE -> EvolutionItems.knife_blade_andesite.get();
            case BASALT -> EvolutionItems.knife_blade_basalt.get();
            case CHALK -> EvolutionItems.knife_blade_chalk.get();
            case CHERT -> EvolutionItems.knife_blade_chert.get();
            case CONGLOMERATE -> EvolutionItems.knife_blade_conglomerate.get();
            case DACITE -> EvolutionItems.knife_blade_dacite.get();
            case DIORITE -> EvolutionItems.knife_blade_diorite.get();
            case DOLOMITE -> EvolutionItems.knife_blade_dolomite.get();
            case GABBRO -> EvolutionItems.knife_blade_gabbro.get();
            case GNEISS -> EvolutionItems.knife_blade_gneiss.get();
            case GRANITE -> EvolutionItems.knife_blade_granite.get();
            case LIMESTONE -> EvolutionItems.knife_blade_limestone.get();
            case MARBLE -> EvolutionItems.knife_blade_marble.get();
            case PHYLLITE -> EvolutionItems.knife_blade_phyllite.get();
            case QUARTZITE -> EvolutionItems.knife_blade_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.knife_blade_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.knife_blade_sandstone.get();
            case SCHIST -> EvolutionItems.knife_blade_schist.get();
            case SHALE -> EvolutionItems.knife_blade_shale.get();
            case SLATE -> EvolutionItems.knife_blade_slate.get();
        };
    }

    public int getMass() {
        return this.density;
    }

    public String getName() {
        return this.name;
    }

    public Block getRock() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a rock type!");
            case ANDESITE -> EvolutionBlocks.ROCK_ANDESITE.get();
            case BASALT -> EvolutionBlocks.ROCK_BASALT.get();
            case CHALK -> EvolutionBlocks.ROCK_CHALK.get();
            case CHERT -> EvolutionBlocks.ROCK_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.ROCK_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.ROCK_DACITE.get();
            case DIORITE -> EvolutionBlocks.ROCK_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.ROCK_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.ROCK_GABBRO.get();
            case GNEISS -> EvolutionBlocks.ROCK_GNEISS.get();
            case GRANITE -> EvolutionBlocks.ROCK_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.ROCK_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.ROCK_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.ROCK_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.ROCK_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.ROCK_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.ROCK_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.ROCK_SCHIST.get();
            case SHALE -> EvolutionBlocks.ROCK_SHALE.get();
            case SLATE -> EvolutionBlocks.ROCK_SLATE.get();
        };
    }

    public RockType getRockType() {
        return this.rockType;
    }

    public Block getSand() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a sand type!");
            case ANDESITE -> EvolutionBlocks.SAND_ANDESITE.get();
            case BASALT -> EvolutionBlocks.SAND_BASALT.get();
            case CHALK -> EvolutionBlocks.SAND_CHALK.get();
            case CHERT -> EvolutionBlocks.SAND_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.SAND_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.SAND_DACITE.get();
            case DIORITE -> EvolutionBlocks.SAND_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.SAND_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.SAND_GABBRO.get();
            case GNEISS -> EvolutionBlocks.SAND_GNEISS.get();
            case GRANITE -> EvolutionBlocks.SAND_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.SAND_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.SAND_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.SAND_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.SAND_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.SAND_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.SAND_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.SAND_SCHIST.get();
            case SHALE -> EvolutionBlocks.SAND_SHALE.get();
            case SLATE -> EvolutionBlocks.SAND_SLATE.get();
        };
    }

    public int getShearStrength() {
        return this.shearStrength;
    }

    public Item getShovelHead() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a shovel head!");
            case ANDESITE -> EvolutionItems.shovel_head_andesite.get();
            case BASALT -> EvolutionItems.shovel_head_basalt.get();
            case CHALK -> EvolutionItems.shovel_head_chalk.get();
            case CHERT -> EvolutionItems.shovel_head_chert.get();
            case CONGLOMERATE -> EvolutionItems.shovel_head_conglomerate.get();
            case DACITE -> EvolutionItems.shovel_head_dacite.get();
            case DIORITE -> EvolutionItems.shovel_head_diorite.get();
            case DOLOMITE -> EvolutionItems.shovel_head_dolomite.get();
            case GABBRO -> EvolutionItems.shovel_head_gabbro.get();
            case GNEISS -> EvolutionItems.shovel_head_gneiss.get();
            case GRANITE -> EvolutionItems.shovel_head_granite.get();
            case LIMESTONE -> EvolutionItems.shovel_head_limestone.get();
            case MARBLE -> EvolutionItems.shovel_head_marble.get();
            case PHYLLITE -> EvolutionItems.shovel_head_phyllite.get();
            case QUARTZITE -> EvolutionItems.shovel_head_quartzite.get();
            case RED_SANDSTONE -> EvolutionItems.shovel_head_red_sandstone.get();
            case SANDSTONE -> EvolutionItems.shovel_head_sandstone.get();
            case SCHIST -> EvolutionItems.shovel_head_schist.get();
            case SHALE -> EvolutionItems.shovel_head_shale.get();
            case SLATE -> EvolutionItems.shovel_head_slate.get();
        };
    }

    public Block getStone() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a stone type!");
            case ANDESITE -> EvolutionBlocks.STONE_ANDESITE.get();
            case BASALT -> EvolutionBlocks.STONE_BASALT.get();
            case CHALK -> EvolutionBlocks.STONE_CHALK.get();
            case CHERT -> EvolutionBlocks.STONE_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.STONE_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.STONE_DACITE.get();
            case DIORITE -> EvolutionBlocks.STONE_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.STONE_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.STONE_GABBRO.get();
            case GNEISS -> EvolutionBlocks.STONE_GNEISS.get();
            case GRANITE -> EvolutionBlocks.STONE_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.STONE_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.STONE_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.STONE_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.STONE_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.STONE_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.STONE_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.STONE_SCHIST.get();
            case SHALE -> EvolutionBlocks.STONE_SHALE.get();
            case SLATE -> EvolutionBlocks.STONE_SLATE.get();
        };
    }

    public Block getStoneBricks() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a stone bricks type!");
            case ANDESITE -> EvolutionBlocks.STONE_BRICKS_ANDESITE.get();
            case BASALT -> EvolutionBlocks.STONE_BRICKS_BASALT.get();
            case CHALK -> EvolutionBlocks.STONE_BRICKS_CHALK.get();
            case CHERT -> EvolutionBlocks.STONE_BRICKS_CHERT.get();
            case CONGLOMERATE -> EvolutionBlocks.STONE_BRICKS_CONGLOMERATE.get();
            case DACITE -> EvolutionBlocks.STONE_BRICKS_DACITE.get();
            case DIORITE -> EvolutionBlocks.STONE_BRICKS_DIORITE.get();
            case DOLOMITE -> EvolutionBlocks.STONE_BRICKS_DOLOMITE.get();
            case GABBRO -> EvolutionBlocks.STONE_BRICKS_GABBRO.get();
            case GNEISS -> EvolutionBlocks.STONE_BRICKS_GNEISS.get();
            case GRANITE -> EvolutionBlocks.STONE_BRICKS_GRANITE.get();
            case LIMESTONE -> EvolutionBlocks.STONE_BRICKS_LIMESTONE.get();
            case MARBLE -> EvolutionBlocks.STONE_BRICKS_MARBLE.get();
            case PHYLLITE -> EvolutionBlocks.STONE_BRICKS_PHYLLITE.get();
            case QUARTZITE -> EvolutionBlocks.STONE_BRICKS_QUARTZITE.get();
            case RED_SANDSTONE -> EvolutionBlocks.STONE_BRICKS_RED_SANDSTONE.get();
            case SANDSTONE -> EvolutionBlocks.STONE_BRICKS_SANDSTONE.get();
            case SCHIST -> EvolutionBlocks.STONE_BRICKS_SCHIST.get();
            case SHALE -> EvolutionBlocks.STONE_BRICKS_SHALE.get();
            case SLATE -> EvolutionBlocks.STONE_BRICKS_SLATE.get();
        };
    }
}
