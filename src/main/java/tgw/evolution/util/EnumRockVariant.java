package tgw.evolution.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.blocks.tileentities.EnumKnapping;
import tgw.evolution.init.EvolutionBlocks;

import static tgw.evolution.init.EvolutionBlocks.*;
import static tgw.evolution.init.EvolutionItems.*;

public enum EnumRockVariant {
    ANDESITE(0, "andesite", STONE_ANDESITE, COBBLE_ANDESITE, ROCK_ANDESITE, KNAPPING_ANDESITE, DIRT_ANDESITE, GRASS_ANDESITE, DRY_GRASS_ANDESITE, axe_head_andesite, javelin_head_andesite, shovel_head_andesite, hammer_head_andesite, hoe_head_andesite, knife_blade_andesite, GRAVEL_ANDESITE, STONE_BRICKS_ANDESITE),
    BASALT(1, "basalt", STONE_BASALT, COBBLE_BASALT, ROCK_BASALT, KNAPPING_BASALT, DIRT_BASALT, GRASS_BASALT, DRY_GRASS_BASALT, axe_head_basalt, javelin_head_basalt, shovel_head_basalt, hammer_head_basalt, hoe_head_basalt, knife_blade_basalt, GRAVEL_BASALT, STONE_BRICKS_BASALT),
    CHALK(2, "chalk", STONE_CHALK, COBBLE_CHALK, ROCK_CHALK, KNAPPING_CHALK, DIRT_CHALK, GRASS_CHALK, DRY_GRASS_CHALK, axe_head_chalk, javelin_head_chalk, shovel_head_chalk, hammer_head_chalk, hoe_head_chalk, knife_blade_chalk, GRAVEL_CHALK, STONE_BRICKS_CHALK),
    CHERT(3, "chert", STONE_CHERT, COBBLE_CHERT, ROCK_CHERT, KNAPPING_CHERT, DIRT_CHERT, GRASS_CHERT, DRY_GRASS_CHERT, axe_head_chert, javelin_head_chert, shovel_head_chert, hammer_head_chert, hoe_head_chert, knife_blade_chert, GRAVEL_CHERT, STONE_BRICKS_CHERT),
    CONGLOMERATE(4, "conglomerate", STONE_CONGLOMERATE, COBBLE_CONGLOMERATE, ROCK_CONGLOMERATE, KNAPPING_CONGLOMERATE, DIRT_CONGLOMERATE, GRASS_CONGLOMERATE, DRY_GRASS_CONGLOMERATE, axe_head_conglomerate, javelin_head_conglomerate, shovel_head_conglomerate, hammer_head_conglomerate, hoe_head_conglomerate, knife_blade_conglomerate, GRAVEL_CONGLOMERATE, STONE_BRICKS_CONGLOMERATE),
    DACITE(5, "dacite", STONE_DACITE, COBBLE_DACITE, ROCK_DACITE, KNAPPING_DACITE, DIRT_DACITE, GRASS_DACITE, DRY_GRASS_DACITE, axe_head_dacite, javelin_head_dacite, shovel_head_dacite, hammer_head_dacite, hoe_head_dacite, knife_blade_dacite, GRAVEL_DACITE, STONE_BRICKS_DACITE),
    DIORITE(6, "diorite", STONE_DIORITE, COBBLE_DIORITE, ROCK_DIORITE, KNAPPING_DIORITE, DIRT_DIORITE, GRASS_DIORITE, DRY_GRASS_DIORITE, axe_head_diorite, javelin_head_diorite, shovel_head_diorite, hammer_head_diorite, hoe_head_diorite, knife_blade_diorite, GRAVEL_DIORITE, STONE_BRICKS_DIORITE),
    DOLOMITE(7, "dolomite", STONE_DOLOMITE, COBBLE_DOLOMITE, ROCK_DOLOMITE, KNAPPING_DOLOMITE, DIRT_DOLOMITE, GRASS_DOLOMITE, DRY_GRASS_DOLOMITE, axe_head_dolomite, javelin_head_dolomite, shovel_head_dolomite, hammer_head_dolomite, hoe_head_dolomite, knife_blade_dolomite, GRAVEL_DOLOMITE, STONE_BRICKS_DOLOMITE),
    GABBRO(8, "gabbro", STONE_GABBRO, COBBLE_GABBRO, ROCK_GABBRO, KNAPPING_GABBRO, DIRT_GABBRO, GRASS_GABBRO, DRY_GRASS_GABBRO, axe_head_gabbro, javelin_head_gabbro, shovel_head_gabbro, hammer_head_gabbro, hoe_head_gabbro, knife_blade_gabbro, GRAVEL_GABBRO, STONE_BRICKS_GABBRO),
    GNEISS(9, "gneiss", STONE_GNEISS, COBBLE_GNEISS, ROCK_GNEISS, KNAPPING_GNEISS, DIRT_GNEISS, GRASS_GNEISS, DRY_GRASS_GNEISS, axe_head_gneiss, javelin_head_gneiss, shovel_head_gneiss, hammer_head_gneiss, hoe_head_gneiss, knife_blade_gneiss, GRAVEL_GNEISS, STONE_BRICKS_GNEISS),
    GRANITE(10, "granite", STONE_GRANITE, COBBLE_GRANITE, ROCK_GRANITE, KNAPPING_GRANITE, DIRT_GRANITE, GRASS_GRANITE, DRY_GRASS_GRANITE, axe_head_granite, javelin_head_granite, shovel_head_granite, hammer_head_granite, hoe_head_granite, knife_blade_granite, GRAVEL_GRANITE, STONE_GRANITE),
    LIMESTONE(11, "limestone", STONE_LIMESTONE, COBBLE_LIMESTONE, ROCK_LIMESTONE, KNAPPING_LIMESTONE, DIRT_LIMESTONE, GRASS_LIMESTONE, DRY_GRASS_LIMESTONE, axe_head_limestone, javelin_head_limestone, shovel_head_limestone, hammer_head_limestone, hoe_head_limestone, knife_blade_limestone, GRAVEL_LIMESTONE, STONE_BRICKS_LIMESTONE),
    MARBLE(12, "marble", STONE_MARBLE, COBBLE_MARBLE, ROCK_MARBLE, KNAPPING_MARBLE, DIRT_MARBLE, GRASS_MARBLE, DRY_GRASS_MARBLE, axe_head_marble, javelin_head_marble, shovel_head_marble, hammer_head_marble, hoe_head_marble, knife_blade_marble, GRAVEL_MARBLE, STONE_BRICKS_MARBLE),
    PHYLLITE(13, "phyllite", STONE_PHYLLITE, COBBLE_PHYLLITE, ROCK_PHYLLITE, KNAPPING_PHYLLITE, DIRT_PHYLLITE, GRASS_PHYLLITE, DRY_GRASS_PHYLLITE, axe_head_phyllite, javelin_head_phyllite, shovel_head_phyllite, hammer_head_phyllite, hoe_head_phyllite, knife_blade_phyllite, GRAVEL_PHYLLITE, STONE_BRICKS_PHYLLITE),
    QUARTZITE(14, "quartzite", STONE_QUARTZITE, COBBLE_QUARTZITE, ROCK_QUARTZITE, KNAPPING_QUARTZITE, DIRT_QUARTZITE, GRASS_QUARTZITE, DRY_GRASS_QUARTZITE, axe_head_quartzite, javelin_head_quartzite, shovel_head_quartzite, hammer_head_quartzite, hoe_head_quartzite, knife_blade_quartzite, GRAVEL_QUARTZITE, STONE_BRICKS_QUARTZITE),
    RED_SANDSTONE(15, "red_sandstone", STONE_RED_SANDSTONE, COBBLE_RED_SANDSTONE, ROCK_RED_SANDSTONE, KNAPPING_RED_SANDSTONE, DIRT_RED_SANDSTONE, GRASS_RED_SANDSTONE, DRY_GRASS_RED_SANDSTONE, axe_head_red_sandstone, javelin_head_red_sandstone, shovel_head_red_sandstone, hammer_head_red_sandstone, hoe_head_red_sandstone, knife_blade_red_sandstone, GRAVEL_RED_SANDSTONE, STONE_BRICKS_RED_SANDSTONE),
    SANDSTONE(16, "sandstone", STONE_SANDSTONE, COBBLE_SANDSTONE, ROCK_SANDSTONE, KNAPPING_SANDSTONE, DIRT_SANDSTONE, GRASS_SANDSTONE, DRY_GRASS_SANDSTONE, axe_head_sandstone, javelin_head_sandstone, shovel_head_sandstone, hammer_head_sandstone, hoe_head_sandstone, knife_blade_sandstone, GRAVEL_SANDSTONE, STONE_BRICKS_SANDSTONE),
    SCHIST(17, "schist", STONE_SCHIST, COBBLE_SCHIST, ROCK_SCHIST, KNAPPING_SCHIST, DIRT_SCHIST, GRASS_SCHIST, DRY_GRASS_SCHIST, axe_head_schist, javelin_head_schist, shovel_head_schist, hammer_head_schist, hoe_head_schist, knife_blade_schist, GRAVEL_SCHIST, STONE_BRICKS_SCHIST),
    SHALE(18, "shale", STONE_SHALE, COBBLE_SHALE, ROCK_SHALE, KNAPPING_SHALE, DIRT_SHALE, GRASS_SHALE, DRY_GRASS_SHALE, axe_head_shale, javelin_head_shale, shovel_head_shale, hammer_head_shale, hoe_head_shale, knife_blade_shale, GRAVEL_SHALE, STONE_BRICKS_SHALE),
    SLATE(19, "slate", STONE_SLATE, COBBLE_SLATE, ROCK_SLATE, KNAPPING_SLATE, DIRT_SLATE, GRASS_SLATE, DRY_GRASS_SLATE, axe_head_slate, javelin_head_slate, shovel_head_slate, hammer_head_slate, hoe_head_slate, knife_blade_slate, GRAVEL_SLATE, STONE_BRICKS_SLATE),
    PEAT(20, "peat", null, null, null, null, EvolutionBlocks.PEAT, GRASS_PEAT, null, null, null, null, null, null, null, null, null),
    CLAY(21, "clay", null, null, null, null, EvolutionBlocks.CLAY, GRASS_CLAY, null, null, null, null, null, null, null, null, null);

    private final byte id;
    private final String name;
    private final Block stone;
    private final Block cobble;
    private final Block knapping;
    private final Block rock;
    private final Block dirt;
    private final Block dryGrass;
    private final Block grass;
    private final Block gravel;
    private final Block stoneBricks;
    private final Item axeHead;
    private final Item javelinHead;
    private final Item shovelHead;
    private final Item hammerHead;
    private final Item hoeHead;
    private final Item knifeBlade;

    EnumRockVariant(int id, String name, RegistryObject<Block> stone, RegistryObject<Block> cobble, RegistryObject<Block> rock, RegistryObject<Block> knapping, RegistryObject<Block> dirt, RegistryObject<Block> grass, RegistryObject<Block> dryGrass, RegistryObject<Item> axeHead, RegistryObject<Item> javelinHead, RegistryObject<Item> shovelHead, RegistryObject<Item> hammerHead, RegistryObject<Item> hoeHead, RegistryObject<Item> knifeBlade, RegistryObject<Block> gravel, RegistryObject<Block> stoneBricks) {
        this.id = (byte) id;
        this.name = name;
        this.stone = stone != null ? stone.get() : null;
        this.cobble = cobble != null ? cobble.get() : null;
        this.knapping = knapping != null ? knapping.get() : null;
        this.rock = rock != null ? rock.get() : null;
        this.grass = grass != null ? grass.get() : null;
        this.dryGrass = dryGrass != null ? dryGrass.get() : null;
        this.dirt = dirt != null ? dirt.get() : null;
        this.axeHead = axeHead != null ? axeHead.get() : null;
        this.gravel = gravel != null ? gravel.get() : null;
        this.javelinHead = javelinHead != null ? javelinHead.get() : null;
        this.shovelHead = shovelHead != null ? shovelHead.get() : null;
        this.hammerHead = hammerHead != null ? hammerHead.get() : null;
        this.knifeBlade = knifeBlade != null ? knifeBlade.get() : null;
        this.hoeHead = hoeHead != null ? hoeHead.get() : null;
        this.stoneBricks = stoneBricks != null ? stoneBricks.get() : null;
    }

    public static EnumRockVariant fromId(byte id) {
        for (EnumRockVariant variant : EnumRockVariant.values()) {
            if (variant.id == id) {
                return variant;
            }
        }
        throw new IllegalStateException("Could not find EnumRockVariant with id " + id);
    }

    public Block fromEnumVanillaRep(EnumVanillaRockVariant vanilla) {
        switch (vanilla) {
            case DIRT:
                return this.dirt;
            case GRAVEL:
                return this.gravel;
            case GRASS_BLOCK:
                return this.grass;
            case STONE:
                return this.stone;
            case STONE_BRICKS:
                return this.stoneBricks;
        }
        throw new IllegalStateException("Could not find a Block to replace vanilla " + vanilla);
    }

    public String getName() {
        return this.name;
    }

    public Block getStone() {
        return this.stone;
    }

    public Block getCobble() {
        return this.cobble;
    }

    public Block getRock() {
        return this.rock;
    }

    public Block getKnapping() {
        return this.knapping;
    }

    public Block getDirt() {
        return this.dirt;
    }

    public Block getDryGrass() {
        return this.dryGrass;
    }

    public Block getGrass() {
        return this.grass;
    }

    public Block getGravel() {
        return this.gravel;
    }

    public ItemStack getKnappedStack(EnumKnapping knapping) {
        switch (knapping) {
            case AXE:
                return new ItemStack(this.axeHead);
            case HOE:
                return new ItemStack(this.hoeHead);
            case NULL:
                return new ItemStack(this.rock);
            case KNIFE:
                return new ItemStack(this.knifeBlade, 2);
            case HAMMER:
                return new ItemStack(this.hammerHead);
            case SHOVEL:
                return new ItemStack(this.shovelHead);
            case JAVELIN:
                return new ItemStack(this.javelinHead);
        }
        throw new IllegalStateException("Could not find Item for EnumKnapping " + knapping);
    }

    public byte getId() {
        return this.id;
    }
}
