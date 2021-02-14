package tgw.evolution.init;

import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.trees.Tree;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.blocks.fluids.BlockFreshWater;
import tgw.evolution.blocks.fluids.BlockGenericFluid;
import tgw.evolution.blocks.fluids.BlockSaltWater;
import tgw.evolution.blocks.trees.*;
import tgw.evolution.util.MetalVariant;
import tgw.evolution.util.RockVariant;
import tgw.evolution.util.WoodVariant;

import static tgw.evolution.util.MetalVariant.COPPER;
import static tgw.evolution.util.RockVariant.*;
import static tgw.evolution.util.WoodVariant.*;

@SuppressWarnings("unused")
@EventBusSubscriber
public final class EvolutionBlocks {

    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Evolution.MODID);

    //Placeholder
    public static final RegistryObject<Block> PLACEHOLDER_BLOCK = BLOCKS.register("placeholder_block", BlockPlaceholder::new);
    //Stick
    public static final RegistryObject<Block> STICK = BLOCKS.register("stick",
                                                                      () -> new BlockPlaceableItem(Block.Properties.create(Material.MISCELLANEOUS)
                                                                                                                   .sound(SoundType.WOOD)));
    //Stone
    public static final RegistryObject<Block> STONE_ANDESITE = BLOCKS.register("stone_andesite", () -> stone(ANDESITE));
    public static final RegistryObject<Block> STONE_BASALT = BLOCKS.register("stone_basalt", () -> stone(BASALT));
    public static final RegistryObject<Block> STONE_CHALK = BLOCKS.register("stone_chalk", () -> stone(CHALK));
    public static final RegistryObject<Block> STONE_CHERT = BLOCKS.register("stone_chert", () -> stone(CHERT));
    public static final RegistryObject<Block> STONE_CONGLOMERATE = BLOCKS.register("stone_conglomerate", () -> stone(CONGLOMERATE));
    public static final RegistryObject<Block> STONE_DACITE = BLOCKS.register("stone_dacite", () -> stone(DACITE));
    public static final RegistryObject<Block> STONE_DIORITE = BLOCKS.register("stone_diorite", () -> stone(DIORITE));
    public static final RegistryObject<Block> STONE_DOLOMITE = BLOCKS.register("stone_dolomite", () -> stone(DOLOMITE));
    public static final RegistryObject<Block> STONE_GABBRO = BLOCKS.register("stone_gabbro", () -> stone(GABBRO));
    public static final RegistryObject<Block> STONE_GNEISS = BLOCKS.register("stone_gneiss", () -> stone(GNEISS));
    public static final RegistryObject<Block> STONE_GRANITE = BLOCKS.register("stone_granite", () -> stone(GRANITE));
    public static final RegistryObject<Block> STONE_LIMESTONE = BLOCKS.register("stone_limestone", () -> stone(LIMESTONE));
    public static final RegistryObject<Block> STONE_MARBLE = BLOCKS.register("stone_marble", () -> stone(MARBLE));
    public static final RegistryObject<Block> STONE_PHYLLITE = BLOCKS.register("stone_phyllite", () -> stone(PHYLLITE));
    public static final RegistryObject<Block> STONE_QUARTZITE = BLOCKS.register("stone_quartzite", () -> stone(QUARTZITE));
    public static final RegistryObject<Block> STONE_RED_SANDSTONE = BLOCKS.register("stone_red_sandstone", () -> stone(RED_SANDSTONE));
    public static final RegistryObject<Block> STONE_SANDSTONE = BLOCKS.register("stone_sandstone", () -> stone(SANDSTONE));
    public static final RegistryObject<Block> STONE_SCHIST = BLOCKS.register("stone_schist", () -> stone(SCHIST));
    public static final RegistryObject<Block> STONE_SHALE = BLOCKS.register("stone_shale", () -> stone(SHALE));
    public static final RegistryObject<Block> STONE_SLATE = BLOCKS.register("stone_slate", () -> stone(SLATE));
    //Cobblestone
    public static final RegistryObject<Block> COBBLE_ANDESITE = BLOCKS.register("cobble_andesite", () -> cobblestone(ANDESITE));
    public static final RegistryObject<Block> COBBLE_BASALT = BLOCKS.register("cobble_basalt", () -> cobblestone(BASALT));
    public static final RegistryObject<Block> COBBLE_CHALK = BLOCKS.register("cobble_chalk", () -> cobblestone(CHALK));
    public static final RegistryObject<Block> COBBLE_CHERT = BLOCKS.register("cobble_chert", () -> cobblestone(CHERT));
    public static final RegistryObject<Block> COBBLE_CONGLOMERATE = BLOCKS.register("cobble_conglomerate", () -> cobblestone(CONGLOMERATE));
    public static final RegistryObject<Block> COBBLE_DACITE = BLOCKS.register("cobble_dacite", () -> cobblestone(DACITE));
    public static final RegistryObject<Block> COBBLE_DIORITE = BLOCKS.register("cobble_diorite", () -> cobblestone(DIORITE));
    public static final RegistryObject<Block> COBBLE_DOLOMITE = BLOCKS.register("cobble_dolomite", () -> cobblestone(DOLOMITE));
    public static final RegistryObject<Block> COBBLE_GABBRO = BLOCKS.register("cobble_gabbro", () -> cobblestone(GABBRO));
    public static final RegistryObject<Block> COBBLE_GNEISS = BLOCKS.register("cobble_gneiss", () -> cobblestone(GNEISS));
    public static final RegistryObject<Block> COBBLE_GRANITE = BLOCKS.register("cobble_granite", () -> cobblestone(GRANITE));
    public static final RegistryObject<Block> COBBLE_LIMESTONE = BLOCKS.register("cobble_limestone", () -> cobblestone(LIMESTONE));
    public static final RegistryObject<Block> COBBLE_MARBLE = BLOCKS.register("cobble_marble", () -> cobblestone(MARBLE));
    public static final RegistryObject<Block> COBBLE_PHYLLITE = BLOCKS.register("cobble_phyllite", () -> cobblestone(PHYLLITE));
    public static final RegistryObject<Block> COBBLE_QUARTZITE = BLOCKS.register("cobble_quartzite", () -> cobblestone(QUARTZITE));
    public static final RegistryObject<Block> COBBLE_RED_SANDSTONE = BLOCKS.register("cobble_red_sandstone", () -> cobblestone(RED_SANDSTONE));
    public static final RegistryObject<Block> COBBLE_SANDSTONE = BLOCKS.register("cobble_sandstone", () -> cobblestone(SANDSTONE));
    public static final RegistryObject<Block> COBBLE_SCHIST = BLOCKS.register("cobble_schist", () -> cobblestone(SCHIST));
    public static final RegistryObject<Block> COBBLE_SHALE = BLOCKS.register("cobble_shale", () -> cobblestone(SHALE));
    public static final RegistryObject<Block> COBBLE_SLATE = BLOCKS.register("cobble_slate", () -> cobblestone(SLATE));
    //Polished Stones
    public static final RegistryObject<Block> POLISHED_STONE_ANDESITE = BLOCKS.register("polished_stone_andesite", () -> polishedStone(ANDESITE));
    public static final RegistryObject<Block> POLISHED_STONE_BASALT = BLOCKS.register("polished_stone_basalt", () -> polishedStone(BASALT));
    public static final RegistryObject<Block> POLISHED_STONE_CHALK = BLOCKS.register("polished_stone_chalk", () -> polishedStone(CHALK));
    public static final RegistryObject<Block> POLISHED_STONE_CHERT = BLOCKS.register("polished_stone_chert", () -> polishedStone(CHERT));
    public static final RegistryObject<Block> POLISHED_STONE_CONGLOMERATE = BLOCKS.register("polished_stone_conglomerate",
                                                                                            () -> polishedStone(CONGLOMERATE));
    public static final RegistryObject<Block> POLISHED_STONE_DACITE = BLOCKS.register("polished_stone_dacite", () -> polishedStone(DACITE));
    public static final RegistryObject<Block> POLISHED_STONE_DIORITE = BLOCKS.register("polished_stone_diorite", () -> polishedStone(DIORITE));
    public static final RegistryObject<Block> POLISHED_STONE_DOLOMITE = BLOCKS.register("polished_stone_dolomite", () -> polishedStone(DOLOMITE));
    public static final RegistryObject<Block> POLISHED_STONE_GABBRO = BLOCKS.register("polished_stone_gabbro", () -> polishedStone(GABBRO));
    public static final RegistryObject<Block> POLISHED_STONE_GNEISS = BLOCKS.register("polished_stone_gneiss", () -> polishedStone(GNEISS));
    public static final RegistryObject<Block> POLISHED_STONE_GRANITE = BLOCKS.register("polished_stone_granite", () -> polishedStone(GRANITE));
    public static final RegistryObject<Block> POLISHED_STONE_LIMESTONE = BLOCKS.register("polished_stone_limestone", () -> polishedStone(LIMESTONE));
    public static final RegistryObject<Block> POLISHED_STONE_MARBLE = BLOCKS.register("polished_stone_marble", () -> polishedStone(MARBLE));
    public static final RegistryObject<Block> POLISHED_STONE_PHYLLITE = BLOCKS.register("polished_stone_phyllite", () -> polishedStone(PHYLLITE));
    public static final RegistryObject<Block> POLISHED_STONE_QUARTZITE = BLOCKS.register("polished_stone_quartzite", () -> polishedStone(QUARTZITE));
    public static final RegistryObject<Block> POLISHED_STONE_RED_SANDSTONE = BLOCKS.register("polished_stone_red_sandstone",
                                                                                             () -> polishedStone(RED_SANDSTONE));
    public static final RegistryObject<Block> POLISHED_STONE_SANDSTONE = BLOCKS.register("polished_stone_sandstone", () -> polishedStone(SANDSTONE));
    public static final RegistryObject<Block> POLISHED_STONE_SCHIST = BLOCKS.register("polished_stone_schist", () -> polishedStone(SCHIST));
    public static final RegistryObject<Block> POLISHED_STONE_SHALE = BLOCKS.register("polished_stone_shale", () -> polishedStone(SHALE));
    public static final RegistryObject<Block> POLISHED_STONE_SLATE = BLOCKS.register("polished_stone_slate", () -> polishedStone(SLATE));
    //Rocks
    public static final RegistryObject<Block> ROCK_ANDESITE = BLOCKS.register("rock_andesite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_BASALT = BLOCKS.register("rock_basalt", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_CHALK = BLOCKS.register("rock_chalk", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_CHERT = BLOCKS.register("rock_chert", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_CONGLOMERATE = BLOCKS.register("rock_conglomerate", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_DACITE = BLOCKS.register("rock_dacite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_DIORITE = BLOCKS.register("rock_diorite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_DOLOMITE = BLOCKS.register("rock_dolomite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_GABBRO = BLOCKS.register("rock_gabbro", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_GNEISS = BLOCKS.register("rock_gneiss", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_GRANITE = BLOCKS.register("rock_granite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_LIMESTONE = BLOCKS.register("rock_limestone", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_MARBLE = BLOCKS.register("rock_marble", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_PHYLLITE = BLOCKS.register("rock_phyllite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_QUARTZITE = BLOCKS.register("rock_quartzite", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_RED_SANDSTONE = BLOCKS.register("rock_red_sandstone", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_SANDSTONE = BLOCKS.register("rock_sandstone", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_SCHIST = BLOCKS.register("rock_schist", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_SHALE = BLOCKS.register("rock_shale", BlockPlaceableRock::new);
    public static final RegistryObject<Block> ROCK_SLATE = BLOCKS.register("rock_slate", BlockPlaceableRock::new);
    //Knapping
    public static final RegistryObject<Block> KNAPPING_ANDESITE = BLOCKS.register("knapping_block_andesite", () -> knapping(ANDESITE));
    public static final RegistryObject<Block> KNAPPING_BASALT = BLOCKS.register("knapping_block_basalt", () -> knapping(BASALT));
    public static final RegistryObject<Block> KNAPPING_CHALK = BLOCKS.register("knapping_block_chalk", () -> knapping(CHALK));
    public static final RegistryObject<Block> KNAPPING_CHERT = BLOCKS.register("knapping_block_chert", () -> knapping(CHERT));
    public static final RegistryObject<Block> KNAPPING_CONGLOMERATE = BLOCKS.register("knapping_block_conglomerate", () -> knapping(CONGLOMERATE));
    public static final RegistryObject<Block> KNAPPING_DACITE = BLOCKS.register("knapping_block_dacite", () -> knapping(DACITE));
    public static final RegistryObject<Block> KNAPPING_DIORITE = BLOCKS.register("knapping_block_diorite", () -> knapping(DIORITE));
    public static final RegistryObject<Block> KNAPPING_DOLOMITE = BLOCKS.register("knapping_block_dolomite", () -> knapping(DOLOMITE));
    public static final RegistryObject<Block> KNAPPING_GABBRO = BLOCKS.register("knapping_block_gabbro", () -> knapping(GABBRO));
    public static final RegistryObject<Block> KNAPPING_GNEISS = BLOCKS.register("knapping_block_gneiss", () -> knapping(GNEISS));
    public static final RegistryObject<Block> KNAPPING_GRANITE = BLOCKS.register("knapping_block_granite", () -> knapping(GRANITE));
    public static final RegistryObject<Block> KNAPPING_LIMESTONE = BLOCKS.register("knapping_block_limestone", () -> knapping(LIMESTONE));
    public static final RegistryObject<Block> KNAPPING_MARBLE = BLOCKS.register("knapping_block_marble", () -> knapping(MARBLE));
    public static final RegistryObject<Block> KNAPPING_PHYLLITE = BLOCKS.register("knapping_block_phyllite", () -> knapping(PHYLLITE));
    public static final RegistryObject<Block> KNAPPING_QUARTZITE = BLOCKS.register("knapping_block_quartzite", () -> knapping(QUARTZITE));
    public static final RegistryObject<Block> KNAPPING_RED_SANDSTONE = BLOCKS.register("knapping_block_red_sandstone", () -> knapping(RED_SANDSTONE));
    public static final RegistryObject<Block> KNAPPING_SANDSTONE = BLOCKS.register("knapping_block_sandstone", () -> knapping(SANDSTONE));
    public static final RegistryObject<Block> KNAPPING_SCHIST = BLOCKS.register("knapping_block_schist", () -> knapping(SCHIST));
    public static final RegistryObject<Block> KNAPPING_SHALE = BLOCKS.register("knapping_block_shale", () -> knapping(SHALE));
    public static final RegistryObject<Block> KNAPPING_SLATE = BLOCKS.register("knapping_block_slate", () -> knapping(SLATE));
    //Sand
    public static final RegistryObject<Block> SAND_ANDESITE = BLOCKS.register("sand_andesite", () -> sand(ANDESITE));
    public static final RegistryObject<Block> SAND_BASALT = BLOCKS.register("sand_basalt", () -> sand(BASALT));
    public static final RegistryObject<Block> SAND_CHALK = BLOCKS.register("sand_chalk", () -> sand(CHALK));
    public static final RegistryObject<Block> SAND_CHERT = BLOCKS.register("sand_chert", () -> sand(CHERT));
    public static final RegistryObject<Block> SAND_CONGLOMERATE = BLOCKS.register("sand_conglomerate", () -> sand(CONGLOMERATE));
    public static final RegistryObject<Block> SAND_DACITE = BLOCKS.register("sand_dacite", () -> sand(DACITE));
    public static final RegistryObject<Block> SAND_DIORITE = BLOCKS.register("sand_diorite", () -> sand(DIORITE));
    public static final RegistryObject<Block> SAND_DOLOMITE = BLOCKS.register("sand_dolomite", () -> sand(DOLOMITE));
    public static final RegistryObject<Block> SAND_GABBRO = BLOCKS.register("sand_gabbro", () -> sand(GABBRO));
    public static final RegistryObject<Block> SAND_GNEISS = BLOCKS.register("sand_gneiss", () -> sand(GNEISS));
    public static final RegistryObject<Block> SAND_GRANITE = BLOCKS.register("sand_granite", () -> sand(GRANITE));
    public static final RegistryObject<Block> SAND_LIMESTONE = BLOCKS.register("sand_limestone", () -> sand(LIMESTONE));
    public static final RegistryObject<Block> SAND_MARBLE = BLOCKS.register("sand_marble", () -> sand(MARBLE));
    public static final RegistryObject<Block> SAND_PHYLLITE = BLOCKS.register("sand_phyllite", () -> sand(PHYLLITE));
    public static final RegistryObject<Block> SAND_QUARTZITE = BLOCKS.register("sand_quartzite", () -> sand(QUARTZITE));
    public static final RegistryObject<Block> SAND_RED_SANDSTONE = BLOCKS.register("sand_red_sandstone", () -> sand(RED_SANDSTONE));
    public static final RegistryObject<Block> SAND_SANDSTONE = BLOCKS.register("sand_sandstone", () -> sand(SANDSTONE));
    public static final RegistryObject<Block> SAND_SCHIST = BLOCKS.register("sand_schist", () -> sand(SCHIST));
    public static final RegistryObject<Block> SAND_SHALE = BLOCKS.register("sand_shale", () -> sand(SHALE));
    public static final RegistryObject<Block> SAND_SLATE = BLOCKS.register("sand_slate", () -> sand(SLATE));
    //Dirt
    public static final RegistryObject<Block> DIRT_ANDESITE = BLOCKS.register("dirt_andesite", () -> dirt(ANDESITE));
    public static final RegistryObject<Block> DIRT_BASALT = BLOCKS.register("dirt_basalt", () -> dirt(BASALT));
    public static final RegistryObject<Block> DIRT_CHALK = BLOCKS.register("dirt_chalk", () -> dirt(CHALK));
    public static final RegistryObject<Block> DIRT_CHERT = BLOCKS.register("dirt_chert", () -> dirt(CHERT));
    public static final RegistryObject<Block> DIRT_CONGLOMERATE = BLOCKS.register("dirt_conglomerate", () -> dirt(CONGLOMERATE));
    public static final RegistryObject<Block> DIRT_DACITE = BLOCKS.register("dirt_dacite", () -> dirt(DACITE));
    public static final RegistryObject<Block> DIRT_DIORITE = BLOCKS.register("dirt_diorite", () -> dirt(DIORITE));
    public static final RegistryObject<Block> DIRT_DOLOMITE = BLOCKS.register("dirt_dolomite", () -> dirt(DOLOMITE));
    public static final RegistryObject<Block> DIRT_GABBRO = BLOCKS.register("dirt_gabbro", () -> dirt(GABBRO));
    public static final RegistryObject<Block> DIRT_GNEISS = BLOCKS.register("dirt_gneiss", () -> dirt(GNEISS));
    public static final RegistryObject<Block> DIRT_GRANITE = BLOCKS.register("dirt_granite", () -> dirt(GRANITE));
    public static final RegistryObject<Block> DIRT_LIMESTONE = BLOCKS.register("dirt_limestone", () -> dirt(LIMESTONE));
    public static final RegistryObject<Block> DIRT_MARBLE = BLOCKS.register("dirt_marble", () -> dirt(MARBLE));
    public static final RegistryObject<Block> DIRT_PHYLLITE = BLOCKS.register("dirt_phyllite", () -> dirt(PHYLLITE));
    public static final RegistryObject<Block> DIRT_QUARTZITE = BLOCKS.register("dirt_quartzite", () -> dirt(QUARTZITE));
    public static final RegistryObject<Block> DIRT_RED_SANDSTONE = BLOCKS.register("dirt_red_sandstone", () -> dirt(RED_SANDSTONE));
    public static final RegistryObject<Block> DIRT_SANDSTONE = BLOCKS.register("dirt_sandstone", () -> dirt(SANDSTONE));
    public static final RegistryObject<Block> DIRT_SCHIST = BLOCKS.register("dirt_schist", () -> dirt(SCHIST));
    public static final RegistryObject<Block> DIRT_SHALE = BLOCKS.register("dirt_shale", () -> dirt(SHALE));
    public static final RegistryObject<Block> DIRT_SLATE = BLOCKS.register("dirt_slate", () -> dirt(SLATE));
    //Gravel
    public static final RegistryObject<Block> GRAVEL_ANDESITE = BLOCKS.register("gravel_andesite", () -> gravel(ANDESITE));
    public static final RegistryObject<Block> GRAVEL_BASALT = BLOCKS.register("gravel_basalt", () -> gravel(BASALT));
    public static final RegistryObject<Block> GRAVEL_CHALK = BLOCKS.register("gravel_chalk", () -> gravel(CHALK));
    public static final RegistryObject<Block> GRAVEL_CHERT = BLOCKS.register("gravel_chert", () -> gravel(CHERT));
    public static final RegistryObject<Block> GRAVEL_CONGLOMERATE = BLOCKS.register("gravel_conglomerate", () -> gravel(CONGLOMERATE));
    public static final RegistryObject<Block> GRAVEL_DACITE = BLOCKS.register("gravel_dacite", () -> gravel(DACITE));
    public static final RegistryObject<Block> GRAVEL_DIORITE = BLOCKS.register("gravel_diorite", () -> gravel(DIORITE));
    public static final RegistryObject<Block> GRAVEL_DOLOMITE = BLOCKS.register("gravel_dolomite", () -> gravel(DOLOMITE));
    public static final RegistryObject<Block> GRAVEL_GABBRO = BLOCKS.register("gravel_gabbro", () -> gravel(GABBRO));
    public static final RegistryObject<Block> GRAVEL_GNEISS = BLOCKS.register("gravel_gneiss", () -> gravel(GNEISS));
    public static final RegistryObject<Block> GRAVEL_GRANITE = BLOCKS.register("gravel_granite", () -> gravel(GRANITE));
    public static final RegistryObject<Block> GRAVEL_LIMESTONE = BLOCKS.register("gravel_limestone", () -> gravel(LIMESTONE));
    public static final RegistryObject<Block> GRAVEL_MARBLE = BLOCKS.register("gravel_marble", () -> gravel(MARBLE));
    public static final RegistryObject<Block> GRAVEL_PHYLLITE = BLOCKS.register("gravel_phyllite", () -> gravel(PHYLLITE));
    public static final RegistryObject<Block> GRAVEL_QUARTZITE = BLOCKS.register("gravel_quartzite", () -> gravel(QUARTZITE));
    public static final RegistryObject<Block> GRAVEL_RED_SANDSTONE = BLOCKS.register("gravel_red_sandstone", () -> gravel(RED_SANDSTONE));
    public static final RegistryObject<Block> GRAVEL_SANDSTONE = BLOCKS.register("gravel_sandstone", () -> gravel(SANDSTONE));
    public static final RegistryObject<Block> GRAVEL_SCHIST = BLOCKS.register("gravel_schist", () -> gravel(SCHIST));
    public static final RegistryObject<Block> GRAVEL_SHALE = BLOCKS.register("gravel_shale", () -> gravel(SHALE));
    public static final RegistryObject<Block> GRAVEL_SLATE = BLOCKS.register("gravel_slate", () -> gravel(SLATE));
    //Grass
    public static final RegistryObject<Block> GRASS_ANDESITE = BLOCKS.register("grass_andesite", () -> grass(ANDESITE));
    public static final RegistryObject<Block> GRASS_BASALT = BLOCKS.register("grass_basalt", () -> grass(BASALT));
    public static final RegistryObject<Block> GRASS_CHALK = BLOCKS.register("grass_chalk", () -> grass(CHALK));
    public static final RegistryObject<Block> GRASS_CHERT = BLOCKS.register("grass_chert", () -> grass(CHERT));
    public static final RegistryObject<Block> GRASS_CONGLOMERATE = BLOCKS.register("grass_conglomerate", () -> grass(CONGLOMERATE));
    public static final RegistryObject<Block> GRASS_DACITE = BLOCKS.register("grass_dacite", () -> grass(DACITE));
    public static final RegistryObject<Block> GRASS_DIORITE = BLOCKS.register("grass_diorite", () -> grass(DIORITE));
    public static final RegistryObject<Block> GRASS_DOLOMITE = BLOCKS.register("grass_dolomite", () -> grass(DOLOMITE));
    public static final RegistryObject<Block> GRASS_GABBRO = BLOCKS.register("grass_gabbro", () -> grass(GABBRO));
    public static final RegistryObject<Block> GRASS_GNEISS = BLOCKS.register("grass_gneiss", () -> grass(GNEISS));
    public static final RegistryObject<Block> GRASS_GRANITE = BLOCKS.register("grass_granite", () -> grass(GRANITE));
    public static final RegistryObject<Block> GRASS_LIMESTONE = BLOCKS.register("grass_limestone", () -> grass(LIMESTONE));
    public static final RegistryObject<Block> GRASS_MARBLE = BLOCKS.register("grass_marble", () -> grass(MARBLE));
    public static final RegistryObject<Block> GRASS_PHYLLITE = BLOCKS.register("grass_phyllite", () -> grass(PHYLLITE));
    public static final RegistryObject<Block> GRASS_QUARTZITE = BLOCKS.register("grass_quartzite", () -> grass(QUARTZITE));
    public static final RegistryObject<Block> GRASS_RED_SANDSTONE = BLOCKS.register("grass_red_sandstone", () -> grass(RED_SANDSTONE));
    public static final RegistryObject<Block> GRASS_SANDSTONE = BLOCKS.register("grass_sandstone", () -> grass(SANDSTONE));
    public static final RegistryObject<Block> GRASS_SCHIST = BLOCKS.register("grass_schist", () -> grass(SCHIST));
    public static final RegistryObject<Block> GRASS_SHALE = BLOCKS.register("grass_shale", () -> grass(SHALE));
    public static final RegistryObject<Block> GRASS_SLATE = BLOCKS.register("grass_slate", () -> grass(SLATE));
    //Clay
    public static final RegistryObject<Block> CLAY = BLOCKS.register("clay", BlockClay::new);
    public static final RegistryObject<Block> GRASS_CLAY = BLOCKS.register("grass_clay", () -> grass(RockVariant.CLAY));
    //Peat
    public static final RegistryObject<Block> PEAT = BLOCKS.register("peat", BlockPeat::new);
    public static final RegistryObject<Block> GRASS_PEAT = BLOCKS.register("grass_peat", () -> grass(RockVariant.PEAT));
    //Dry Grass
    public static final RegistryObject<Block> DRY_GRASS_ANDESITE = BLOCKS.register("dry_grass_andesite", () -> dryGrass(ANDESITE));
    public static final RegistryObject<Block> DRY_GRASS_BASALT = BLOCKS.register("dry_grass_basalt", () -> dryGrass(BASALT));
    public static final RegistryObject<Block> DRY_GRASS_CHALK = BLOCKS.register("dry_grass_chalk", () -> dryGrass(CHALK));
    public static final RegistryObject<Block> DRY_GRASS_CHERT = BLOCKS.register("dry_grass_chert", () -> dryGrass(CHERT));
    public static final RegistryObject<Block> DRY_GRASS_CONGLOMERATE = BLOCKS.register("dry_grass_conglomerate", () -> dryGrass(CONGLOMERATE));
    public static final RegistryObject<Block> DRY_GRASS_DACITE = BLOCKS.register("dry_grass_dacite", () -> dryGrass(DACITE));
    public static final RegistryObject<Block> DRY_GRASS_DIORITE = BLOCKS.register("dry_grass_diorite", () -> dryGrass(DIORITE));
    public static final RegistryObject<Block> DRY_GRASS_DOLOMITE = BLOCKS.register("dry_grass_dolomite", () -> dryGrass(DOLOMITE));
    public static final RegistryObject<Block> DRY_GRASS_GABBRO = BLOCKS.register("dry_grass_gabbro", () -> dryGrass(GABBRO));
    public static final RegistryObject<Block> DRY_GRASS_GNEISS = BLOCKS.register("dry_grass_gneiss", () -> dryGrass(GNEISS));
    public static final RegistryObject<Block> DRY_GRASS_GRANITE = BLOCKS.register("dry_grass_granite", () -> dryGrass(GRANITE));
    public static final RegistryObject<Block> DRY_GRASS_LIMESTONE = BLOCKS.register("dry_grass_limestone", () -> dryGrass(LIMESTONE));
    public static final RegistryObject<Block> DRY_GRASS_MARBLE = BLOCKS.register("dry_grass_marble", () -> dryGrass(MARBLE));
    public static final RegistryObject<Block> DRY_GRASS_PHYLLITE = BLOCKS.register("dry_grass_phyllite", () -> dryGrass(PHYLLITE));
    public static final RegistryObject<Block> DRY_GRASS_QUARTZITE = BLOCKS.register("dry_grass_quartzite", () -> dryGrass(QUARTZITE));
    public static final RegistryObject<Block> DRY_GRASS_RED_SANDSTONE = BLOCKS.register("dry_grass_red_sandstone", () -> dryGrass(RED_SANDSTONE));
    public static final RegistryObject<Block> DRY_GRASS_SANDSTONE = BLOCKS.register("dry_grass_sandstone", () -> dryGrass(SANDSTONE));
    public static final RegistryObject<Block> DRY_GRASS_SCHIST = BLOCKS.register("dry_grass_schist", () -> dryGrass(SCHIST));
    public static final RegistryObject<Block> DRY_GRASS_SHALE = BLOCKS.register("dry_grass_shale", () -> dryGrass(SHALE));
    public static final RegistryObject<Block> DRY_GRASS_SLATE = BLOCKS.register("dry_grass_slate", () -> dryGrass(SLATE));
    //Log
    public static final RegistryObject<BlockLog> LOG_ACACIA = BLOCKS.register("log_acacia", () -> log(ACACIA));
    public static final RegistryObject<BlockLog> LOG_ASPEN = BLOCKS.register("log_aspen", () -> log(ASPEN));
    public static final RegistryObject<BlockLog> LOG_BIRCH = BLOCKS.register("log_birch", () -> log(BIRCH));
    public static final RegistryObject<BlockLog> LOG_CEDAR = BLOCKS.register("log_cedar", () -> log(CEDAR));
    public static final RegistryObject<BlockLog> LOG_EBONY = BLOCKS.register("log_ebony", () -> log(EBONY));
    public static final RegistryObject<BlockLog> LOG_ELM = BLOCKS.register("log_elm", () -> log(ELM));
    public static final RegistryObject<BlockLog> LOG_EUCALYPTUS = BLOCKS.register("log_eucalyptus", () -> log(EUCALYPTUS));
    public static final RegistryObject<BlockLog> LOG_FIR = BLOCKS.register("log_fir", () -> log(FIR));
    public static final RegistryObject<BlockLog> LOG_KAPOK = BLOCKS.register("log_kapok", () -> log(KAPOK));
    public static final RegistryObject<BlockLog> LOG_MANGROVE = BLOCKS.register("log_mangrove", () -> log(MANGROVE));
    public static final RegistryObject<BlockLog> LOG_MAPLE = BLOCKS.register("log_maple", () -> log(MAPLE));
    public static final RegistryObject<BlockLog> LOG_OAK = BLOCKS.register("log_oak", () -> log(OAK));
    public static final RegistryObject<BlockLog> LOG_OLD_OAK = BLOCKS.register("log_old_oak", () -> log(OLD_OAK));
    public static final RegistryObject<BlockLog> LOG_PALM = BLOCKS.register("log_palm", () -> log(PALM));
    public static final RegistryObject<BlockLog> LOG_PINE = BLOCKS.register("log_pine", () -> log(PINE));
    public static final RegistryObject<BlockLog> LOG_REDWOOD = BLOCKS.register("log_redwood", () -> log(REDWOOD));
    public static final RegistryObject<BlockLog> LOG_SPRUCE = BLOCKS.register("log_spruce", () -> log(SPRUCE));
    public static final RegistryObject<BlockLog> LOG_WILLOW = BLOCKS.register("log_willow", () -> log(WILLOW));
    //Leaves
    public static final RegistryObject<Block> LEAVES_ACACIA = BLOCKS.register("leaves_acacia", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_ASPEN = BLOCKS.register("leaves_aspen", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_BIRCH = BLOCKS.register("leaves_birch", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_CEDAR = BLOCKS.register("leaves_cedar", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_EBONY = BLOCKS.register("leaves_ebony", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_ELM = BLOCKS.register("leaves_elm", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_EUCALYPTUS = BLOCKS.register("leaves_eucalyptus", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_FIR = BLOCKS.register("leaves_fir", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_KAPOK = BLOCKS.register("leaves_kapok", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_MANGROVE = BLOCKS.register("leaves_mangrove", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_MAPLE = BLOCKS.register("leaves_maple", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_OAK = BLOCKS.register("leaves_oak", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_OLD_OAK = BLOCKS.register("leaves_old_oak", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_PALM = BLOCKS.register("leaves_palm", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_PINE = BLOCKS.register("leaves_pine", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_REDWOOD = BLOCKS.register("leaves_redwood", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_SPRUCE = BLOCKS.register("leaves_spruce", BlockLeaves::new);
    public static final RegistryObject<Block> LEAVES_WILLOW = BLOCKS.register("leaves_willow", BlockLeaves::new);
    //Sapling
    public static final RegistryObject<BlockSapling> SAPLING_ACACIA = BLOCKS.register("sapling_acacia", () -> sapling(new AcaciaTree()));
    public static final RegistryObject<BlockSapling> SAPLING_ASPEN = BLOCKS.register("sapling_aspen", () -> sapling(new AspenTree()));
    public static final RegistryObject<BlockSapling> SAPLING_BIRCH = BLOCKS.register("sapling_birch", () -> sapling(new BirchTree()));
    public static final RegistryObject<BlockSapling> SAPLING_CEDAR = BLOCKS.register("sapling_cedar", () -> sapling(new CedarTree()));
    public static final RegistryObject<BlockSapling> SAPLING_EBONY = BLOCKS.register("sapling_ebony", () -> sapling(new NullTree()));
    public static final RegistryObject<BlockSapling> SAPLING_ELM = BLOCKS.register("sapling_elm", () -> sapling(new ElmTree()));
    public static final RegistryObject<BlockSapling> SAPLING_EUCALYPTUS = BLOCKS.register("sapling_eucalyptus", () -> sapling(new NullTree()));
    public static final RegistryObject<BlockSapling> SAPLING_FIR = BLOCKS.register("sapling_fir", () -> sapling(new NullTree()));
    public static final RegistryObject<BlockSapling> SAPLING_KAPOK = BLOCKS.register("sapling_kapok", () -> sapling(new KapokTree()));
    public static final RegistryObject<BlockSapling> SAPLING_MANGROVE = BLOCKS.register("sapling_mangrove", () -> sapling(new NullTree()));
    public static final RegistryObject<BlockSapling> SAPLING_MAPLE = BLOCKS.register("sapling_maple", () -> sapling(new MapleTree()));
    public static final RegistryObject<BlockSapling> SAPLING_OAK = BLOCKS.register("sapling_oak", () -> sapling(new OakTree()));
    public static final RegistryObject<BlockSapling> SAPLING_OLD_OAK = BLOCKS.register("sapling_old_oak", () -> sapling(new OldOakTree()));
    public static final RegistryObject<BlockSapling> SAPLING_PALM = BLOCKS.register("sapling_palm", () -> sapling(new PalmTree()));
    public static final RegistryObject<BlockSapling> SAPLING_PINE = BLOCKS.register("sapling_pine", () -> sapling(new NullTree()));
    public static final RegistryObject<BlockSapling> SAPLING_REDWOOD = BLOCKS.register("sapling_redwood", () -> sapling(new RedwoodTree()));
    public static final RegistryObject<BlockSapling> SAPLING_SPRUCE = BLOCKS.register("sapling_spruce", () -> sapling(new SpruceTree()));
    public static final RegistryObject<BlockSapling> SAPLING_WILLOW = BLOCKS.register("sapling_willow", () -> sapling(new WillowTree()));
    //Planks
    public static final RegistryObject<Block> PLANKS_ACACIA = BLOCKS.register("planks_acacia", () -> planks(ACACIA));
    public static final RegistryObject<Block> PLANKS_ASPEN = BLOCKS.register("planks_aspen", () -> planks(ASPEN));
    public static final RegistryObject<Block> PLANKS_BIRCH = BLOCKS.register("planks_birch", () -> planks(BIRCH));
    public static final RegistryObject<Block> PLANKS_CEDAR = BLOCKS.register("planks_cedar", () -> planks(CEDAR));
    public static final RegistryObject<Block> PLANKS_EBONY = BLOCKS.register("planks_ebony", () -> planks(EBONY));
    public static final RegistryObject<Block> PLANKS_ELM = BLOCKS.register("planks_elm", () -> planks(ELM));
    public static final RegistryObject<Block> PLANKS_EUCALYPTUS = BLOCKS.register("planks_eucalyptus", () -> planks(EUCALYPTUS));
    public static final RegistryObject<Block> PLANKS_FIR = BLOCKS.register("planks_fir", () -> planks(FIR));
    public static final RegistryObject<Block> PLANKS_KAPOK = BLOCKS.register("planks_kapok", () -> planks(KAPOK));
    public static final RegistryObject<Block> PLANKS_MANGROVE = BLOCKS.register("planks_mangrove", () -> planks(MANGROVE));
    public static final RegistryObject<Block> PLANKS_MAPLE = BLOCKS.register("planks_maple", () -> planks(MAPLE));
    public static final RegistryObject<Block> PLANKS_OAK = BLOCKS.register("planks_oak", () -> planks(OAK));
    public static final RegistryObject<Block> PLANKS_OLD_OAK = BLOCKS.register("planks_old_oak", () -> planks(OLD_OAK));
    public static final RegistryObject<Block> PLANKS_PALM = BLOCKS.register("planks_palm", () -> planks(PALM));
    public static final RegistryObject<Block> PLANKS_PINE = BLOCKS.register("planks_pine", () -> planks(PINE));
    public static final RegistryObject<Block> PLANKS_REDWOOD = BLOCKS.register("planks_redwood", () -> planks(REDWOOD));
    public static final RegistryObject<Block> PLANKS_SPRUCE = BLOCKS.register("planks_spruce", () -> planks(SPRUCE));
    public static final RegistryObject<Block> PLANKS_WILLOW = BLOCKS.register("planks_willow", () -> planks(WILLOW));
    //Log Pile
    public static final RegistryObject<Block> LOG_PILE_ACACIA = BLOCKS.register("log_pile_acacia", () -> logPile(ACACIA));
    public static final RegistryObject<Block> LOG_PILE_ASPEN = BLOCKS.register("log_pile_aspen", () -> logPile(ASPEN));
    public static final RegistryObject<Block> LOG_PILE_BIRCH = BLOCKS.register("log_pile_birch", () -> logPile(BIRCH));
    public static final RegistryObject<Block> LOG_PILE_CEDAR = BLOCKS.register("log_pile_cedar", () -> logPile(CEDAR));
    public static final RegistryObject<Block> LOG_PILE_EBONY = BLOCKS.register("log_pile_ebony", () -> logPile(EBONY));
    public static final RegistryObject<Block> LOG_PILE_ELM = BLOCKS.register("log_pile_elm", () -> logPile(ELM));
    public static final RegistryObject<Block> LOG_PILE_EUCALYPTUS = BLOCKS.register("log_pile_eucalyptus", () -> logPile(EUCALYPTUS));
    public static final RegistryObject<Block> LOG_PILE_FIR = BLOCKS.register("log_pile_fir", () -> logPile(FIR));
    public static final RegistryObject<Block> LOG_PILE_KAPOK = BLOCKS.register("log_pile_kapok", () -> logPile(KAPOK));
    public static final RegistryObject<Block> LOG_PILE_MANGROVE = BLOCKS.register("log_pile_mangrove", () -> logPile(MANGROVE));
    public static final RegistryObject<Block> LOG_PILE_MAPLE = BLOCKS.register("log_pile_maple", () -> logPile(MAPLE));
    public static final RegistryObject<Block> LOG_PILE_OAK = BLOCKS.register("log_pile_oak", () -> logPile(OAK));
    public static final RegistryObject<Block> LOG_PILE_OLD_OAK = BLOCKS.register("log_pile_old_oak", () -> logPile(OLD_OAK));
    public static final RegistryObject<Block> LOG_PILE_PALM = BLOCKS.register("log_pile_palm", () -> logPile(PALM));
    public static final RegistryObject<Block> LOG_PILE_PINE = BLOCKS.register("log_pile_pine", () -> logPile(PINE));
    public static final RegistryObject<Block> LOG_PILE_REDWOOD = BLOCKS.register("log_pile_redwood", () -> logPile(REDWOOD));
    public static final RegistryObject<Block> LOG_PILE_SPRUCE = BLOCKS.register("log_pile_spruce", () -> logPile(SPRUCE));
    public static final RegistryObject<Block> LOG_PILE_WILLOW = BLOCKS.register("log_pile_willow", () -> logPile(WILLOW));
    //Torches
    public static final RegistryObject<Block> TORCH = BLOCKS.register("torch", BlockTorch::new);
    public static final RegistryObject<Block> WALL_TORCH = BLOCKS.register("wall_torch", BlockWallTorch::new);
    //Vegetation
    public static final RegistryObject<Block> GRASS = BLOCKS.register("grass", BlockTallGrass::new);
    public static final RegistryObject<Block> TALLGRASS = BLOCKS.register("tallgrass", BlockDoublePlant::new);
    //Feces Block
    public static final RegistryObject<Block> FECES = BLOCKS.register("feces", BlockFeces::new);
    //Shadow Hound Block
//    public static final RegistryObject<Block> SHADOWHOUND = BLOCKS.register("shadowhound", BlockShadowHound::new);
    //Molding Block
    public static final RegistryObject<Block> MOLDING = BLOCKS.register("molding_block", BlockMolding::new);
    public static final RegistryObject<Block> MOLD_CLAY_AXE = BLOCKS.register("mold_clay_axe", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_SHOVEL = BLOCKS.register("mold_clay_shovel", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_PICKAXE = BLOCKS.register("mold_clay_pickaxe", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_SWORD = BLOCKS.register("mold_clay_sword", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_GUARD = BLOCKS.register("mold_clay_guard", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_SAW = BLOCKS.register("mold_clay_saw", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_HOE = BLOCKS.register("mold_clay_hoe", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_HAMMER = BLOCKS.register("mold_clay_hammer", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_KNIFE = BLOCKS.register("mold_clay_knife", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_SPEAR = BLOCKS.register("mold_clay_spear", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_PROSPECTING = BLOCKS.register("mold_clay_prospecting", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_INGOT = BLOCKS.register("mold_clay_ingot", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> MOLD_CLAY_PLATE = BLOCKS.register("mold_clay_plate", () -> new BlockMoldClay(1));
    public static final RegistryObject<Block> BRICK_CLAY = BLOCKS.register("brick_clay",
                                                                           () -> new BlockMoldClay(Block.makeCuboidShape(2, 0, 5, 14, 6, 11)));
    public static final RegistryObject<Block> CRUCIBLE_CLAY = BLOCKS.register("crucible_clay", () -> new BlockMoldClay(5));
    //Metal Blocks
    public static final RegistryObject<Block> BLOCK_COPPER = BLOCKS.register("block_copper", () -> metal(COPPER));
    //Chopping Blocks
    public static final RegistryObject<Block> CHOPPING_BLOCK_ACACIA = BLOCKS.register("chopping_block_acacia", () -> new BlockChopping(ACACIA));
    public static final RegistryObject<Block> CHOPPING_BLOCK_ASPEN = BLOCKS.register("chopping_block_aspen", () -> new BlockChopping(ASPEN));
    public static final RegistryObject<Block> CHOPPING_BLOCK_BIRCH = BLOCKS.register("chopping_block_birch", () -> new BlockChopping(BIRCH));
    public static final RegistryObject<Block> CHOPPING_BLOCK_CEDAR = BLOCKS.register("chopping_block_cedar", () -> new BlockChopping(CEDAR));
    public static final RegistryObject<Block> CHOPPING_BLOCK_EBONY = BLOCKS.register("chopping_block_ebony", () -> new BlockChopping(EBONY));
    public static final RegistryObject<Block> CHOPPING_BLOCK_ELM = BLOCKS.register("chopping_block_elm", () -> new BlockChopping(ELM));
    public static final RegistryObject<Block> CHOPPING_BLOCK_EUCALYPTUS = BLOCKS.register("chopping_block_eucalyptus",
                                                                                          () -> new BlockChopping(EUCALYPTUS));
    public static final RegistryObject<Block> CHOPPING_BLOCK_FIR = BLOCKS.register("chopping_block_fir", () -> new BlockChopping(FIR));
    public static final RegistryObject<Block> CHOPPING_BLOCK_KAPOK = BLOCKS.register("chopping_block_kapok", () -> new BlockChopping(KAPOK));
    public static final RegistryObject<Block> CHOPPING_BLOCK_MANGROVE = BLOCKS.register("chopping_block_mangrove", () -> new BlockChopping(MANGROVE));
    public static final RegistryObject<Block> CHOPPING_BLOCK_MAPLE = BLOCKS.register("chopping_block_maple", () -> new BlockChopping(MAPLE));
    public static final RegistryObject<Block> CHOPPING_BLOCK_OAK = BLOCKS.register("chopping_block_oak", () -> new BlockChopping(OAK));
    public static final RegistryObject<Block> CHOPPING_BLOCK_OLD_OAK = BLOCKS.register("chopping_block_old_oak", () -> new BlockChopping(OLD_OAK));
    public static final RegistryObject<Block> CHOPPING_BLOCK_PALM = BLOCKS.register("chopping_block_palm", () -> new BlockChopping(PALM));
    public static final RegistryObject<Block> CHOPPING_BLOCK_PINE = BLOCKS.register("chopping_block_pine", () -> new BlockChopping(PINE));
    public static final RegistryObject<Block> CHOPPING_BLOCK_REDWOOD = BLOCKS.register("chopping_block_redwood", () -> new BlockChopping(REDWOOD));
    public static final RegistryObject<Block> CHOPPING_BLOCK_SPRUCE = BLOCKS.register("chopping_block_spruce", () -> new BlockChopping(SPRUCE));
    public static final RegistryObject<Block> CHOPPING_BLOCK_WILLOW = BLOCKS.register("chopping_block_willow", () -> new BlockChopping(WILLOW));
    //Destroy Blocks
    public static final RegistryObject<Block> DESTROY_3 = BLOCKS.register("destroy_3", () -> new GlassBlock(Block.Properties.create(Material.AIR)));
    public static final RegistryObject<Block> DESTROY_6 = BLOCKS.register("destroy_6", () -> new GlassBlock(Block.Properties.create(Material.AIR)));
    public static final RegistryObject<Block> DESTROY_9 = BLOCKS.register("destroy_9", () -> new GlassBlock(Block.Properties.create(Material.AIR)));

    public static final RegistryObject<Block> PIT_KILN = BLOCKS.register("pit_kiln", BlockPitKiln::new);

    public static final RegistryObject<BlockFire> FIRE = BLOCKS.register("fire", BlockFire::new);
    //Rope
    public static final RegistryObject<Block> ROPE = BLOCKS.register("rope", BlockRope::new);
    public static final RegistryObject<Block> GROUND_ROPE = BLOCKS.register("ground_rope", BlockRopeGround::new);
    public static final RegistryObject<Block> CLIMBING_STAKE = BLOCKS.register("climbing_stake", BlockClimbingStake::new);
    public static final RegistryObject<Block> CLIMBING_HOOK = BLOCKS.register("climbing_hook", BlockClimbingHook::new);
    //Stone Bricks
    public static final RegistryObject<Block> STONE_BRICKS_ANDESITE = BLOCKS.register("stone_bricks_andesite", () -> stoneBricks(ANDESITE));
    public static final RegistryObject<Block> STONE_BRICKS_BASALT = BLOCKS.register("stone_bricks_basalt", () -> stoneBricks(BASALT));
    public static final RegistryObject<Block> STONE_BRICKS_CHALK = BLOCKS.register("stone_bricks_chalk", () -> stoneBricks(CHALK));
    public static final RegistryObject<Block> STONE_BRICKS_CHERT = BLOCKS.register("stone_bricks_chert", () -> stoneBricks(CHERT));
    public static final RegistryObject<Block> STONE_BRICKS_CONGLOMERATE = BLOCKS.register("stone_bricks_conglomerate",
                                                                                          () -> stoneBricks(CONGLOMERATE));
    public static final RegistryObject<Block> STONE_BRICKS_DACITE = BLOCKS.register("stone_bricks_dacite", () -> stoneBricks(DACITE));
    public static final RegistryObject<Block> STONE_BRICKS_DIORITE = BLOCKS.register("stone_bricks_diorite", () -> stoneBricks(DIORITE));
    public static final RegistryObject<Block> STONE_BRICKS_DOLOMITE = BLOCKS.register("stone_bricks_dolomite", () -> stoneBricks(DOLOMITE));
    public static final RegistryObject<Block> STONE_BRICKS_GABBRO = BLOCKS.register("stone_bricks_gabbro", () -> stoneBricks(GABBRO));
    public static final RegistryObject<Block> STONE_BRICKS_GNEISS = BLOCKS.register("stone_bricks_gneiss", () -> stoneBricks(GNEISS));
    public static final RegistryObject<Block> STONE_BRICKS_GRANITE = BLOCKS.register("stone_bricks_granite", () -> stoneBricks(GRANITE));
    public static final RegistryObject<Block> STONE_BRICKS_LIMESTONE = BLOCKS.register("stone_bricks_limestone", () -> stoneBricks(LIMESTONE));
    public static final RegistryObject<Block> STONE_BRICKS_MARBLE = BLOCKS.register("stone_bricks_marble", () -> stoneBricks(MARBLE));
    public static final RegistryObject<Block> STONE_BRICKS_PHYLLITE = BLOCKS.register("stone_bricks_phyllite", () -> stoneBricks(PHYLLITE));
    public static final RegistryObject<Block> STONE_BRICKS_QUARTZITE = BLOCKS.register("stone_bricks_quartzite", () -> stoneBricks(QUARTZITE));
    public static final RegistryObject<Block> STONE_BRICKS_RED_SANDSTONE = BLOCKS.register("stone_bricks_red_sandstone",
                                                                                           () -> stoneBricks(RED_SANDSTONE));
    public static final RegistryObject<Block> STONE_BRICKS_SANDSTONE = BLOCKS.register("stone_bricks_sandstone", () -> stoneBricks(SANDSTONE));
    public static final RegistryObject<Block> STONE_BRICKS_SCHIST = BLOCKS.register("stone_bricks_schist", () -> stoneBricks(SCHIST));
    public static final RegistryObject<Block> STONE_BRICKS_SHALE = BLOCKS.register("stone_bricks_shale", () -> stoneBricks(SHALE));
    public static final RegistryObject<Block> STONE_BRICKS_SLATE = BLOCKS.register("stone_bricks_slate", () -> stoneBricks(SLATE));

    public static final RegistryObject<Block> PUZZLE = BLOCKS.register("puzzle", BlockPuzzle::new);
    public static final RegistryObject<Block> SCHEMATIC_BLOCK = BLOCKS.register("schematic_block", BlockSchematic::new);

    public static final RegistryObject<BlockGenericFluid> FRESH_WATER = BLOCKS.register("fresh_water", BlockFreshWater::new);
    public static final RegistryObject<BlockGenericFluid> SALT_WATER = BLOCKS.register("salt_water", BlockSaltWater::new);

    private EvolutionBlocks() {
    }

    private static Block cobblestone(RockVariant variant) {
        return new BlockCobblestone(variant);
    }

    private static Block dirt(RockVariant variant) {
        return new BlockDirt(variant);
    }

    private static Block dryGrass(RockVariant variant) {
        return new BlockDryGrass(variant);
    }

    private static Block grass(RockVariant variant) {
        return new BlockGrass(variant);
    }

    private static Block gravel(RockVariant variant) {
        return new BlockGravel(variant);
    }

    private static Block knapping(RockVariant variant) {
        return new BlockKnapping(variant, variant.getMass() / 4);
    }

    private static BlockLog log(WoodVariant variant) {
        return new BlockLog(variant);
    }

    private static Block logPile(WoodVariant variant) {
        return new BlockLogPile(variant);
    }

    private static Block metal(MetalVariant variant) {
        return new BlockMetal(variant);
    }

    private static Block planks(WoodVariant variant) {
        return new BlockPlanks(variant);
    }

    private static Block polishedStone(RockVariant variant) {
        return new BlockPolishedStone(variant);
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static Block sand(RockVariant variant) {
        return new BlockSand(variant);
    }

    private static BlockSapling sapling(Tree tree) {
        return new BlockSapling(tree);
    }

    private static Block stone(RockVariant variant) {
        return new BlockStone(variant);
    }

    private static Block stoneBricks(RockVariant variant) {
        return new BlockStoneBricks(variant);
    }
}	
