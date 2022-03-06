package tgw.evolution.util.constants;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.IVariant;
import tgw.evolution.util.UnregisteredFeatureException;

import javax.annotation.Nullable;
import java.util.Arrays;

import static tgw.evolution.util.constants.RockType.*;

public enum RockVariant implements IVariant {
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
    public static final RockVariant[] VALUES_STONE = Arrays.stream(VALUES).filter(v -> v != PEAT && v != CLAY).toArray(RockVariant[]::new);
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
            default -> EvolutionBlocks.ALL_COBBLE.get(this).get();
        };
    }

    public Block getDirt() {
        return switch (this) {
            case CLAY -> EvolutionBlocks.CLAY.get();
            case PEAT -> EvolutionBlocks.PEAT.get();
            default -> EvolutionBlocks.ALL_DIRT.get(this).get();
        };
    }

    public Block getDryGrass() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a dry grass type!");
            default -> EvolutionBlocks.ALL_DRY_GRASS.get(this).get();
        };
    }

    public Block getGrass() {
        return EvolutionBlocks.ALL_GRASS.get(this).get();
    }

    public Block getGravel() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a gravel type!");
            default -> EvolutionBlocks.ALL_GRAVEL.get(this).get();
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
            default -> EvolutionBlocks.ALL_KNAPPING.get(this).get();
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

    @Override
    public String getName() {
        return this.name;
    }

    public Block getPolishedStone() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a polished stone type!");
            default -> EvolutionBlocks.ALL_POLISHED_STONE.get(this).get();
        };
    }

    public Block getRock() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a rock type!");
            default -> EvolutionBlocks.ALL_ROCK.get(this).get();
        };
    }

    public RockType getRockType() {
        return this.rockType;
    }

    public Block getSand() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a sand type!");
            default -> EvolutionBlocks.ALL_SAND.get(this).get();
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
            default -> EvolutionBlocks.ALL_STONE.get(this).get();
        };
    }

    public Block getStoneBricks() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a stone bricks type!");
            default -> EvolutionBlocks.ALL_STONE_BRICKS.get(this).get();
        };
    }
}
