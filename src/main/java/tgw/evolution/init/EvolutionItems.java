package tgw.evolution.init;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.client.renderer.item.RenderStackItemJavelin;
import tgw.evolution.items.*;
import tgw.evolution.test.ItemChessboard;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.RockVariant;
import tgw.evolution.util.WoodVariant;

import java.util.function.Supplier;

import static tgw.evolution.init.EvolutionBlocks.*;
import static tgw.evolution.util.RockVariant.*;
import static tgw.evolution.util.WoodVariant.*;

@SuppressWarnings("unused")
@EventBusSubscriber
public final class EvolutionItems {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Evolution.MODID);
    //Placeholder
    public static final RegistryObject<Item> dev_drink = ITEMS.register("dev_drink", () -> new ItemDevDrink(propMisc()));
    public static final RegistryObject<Item> chessboard = ITEMS.register("mbe15_item_chessboard_registry_name", ItemChessboard::new);
    public static final RegistryObject<Item> placeholder_item = ITEMS.register("placeholder_item", EvolutionItems::item);
    public static final RegistryObject<Item> placeholder_block = ITEMS.register("placeholder_block", () -> itemBlock(PLACEHOLDER_BLOCK));
    //Stick
    public static final RegistryObject<Item> stick = ITEMS.register("stick", () -> new ItemStick(STICK.get(), propTreesAndWood()));
    //Stone
    public static final RegistryObject<Item> stone_andesite = ITEMS.register("stone_andesite", () -> itemBlock(STONE_ANDESITE));
    public static final RegistryObject<Item> stone_basalt = ITEMS.register("stone_basalt", () -> itemBlock(STONE_BASALT));
    public static final RegistryObject<Item> stone_chalk = ITEMS.register("stone_chalk", () -> itemBlock(STONE_CHALK));
    public static final RegistryObject<Item> stone_chert = ITEMS.register("stone_chert", () -> itemBlock(STONE_CHERT));
    public static final RegistryObject<Item> stone_conglomerate = ITEMS.register("stone_conglomerate", () -> itemBlock(STONE_CONGLOMERATE));
    public static final RegistryObject<Item> stone_dacite = ITEMS.register("stone_dacite", () -> itemBlock(STONE_DACITE));
    public static final RegistryObject<Item> stone_diorite = ITEMS.register("stone_diorite", () -> itemBlock(STONE_DIORITE));
    public static final RegistryObject<Item> stone_dolomite = ITEMS.register("stone_dolomite", () -> itemBlock(STONE_DOLOMITE));
    public static final RegistryObject<Item> stone_gabbro = ITEMS.register("stone_gabbro", () -> itemBlock(STONE_GABBRO));
    public static final RegistryObject<Item> stone_gneiss = ITEMS.register("stone_gneiss", () -> itemBlock(STONE_GNEISS));
    public static final RegistryObject<Item> stone_granite = ITEMS.register("stone_granite", () -> itemBlock(STONE_GRANITE));
    public static final RegistryObject<Item> stone_limestone = ITEMS.register("stone_limestone", () -> itemBlock(STONE_LIMESTONE));
    public static final RegistryObject<Item> stone_marble = ITEMS.register("stone_marble", () -> itemBlock(STONE_MARBLE));
    public static final RegistryObject<Item> stone_phyllite = ITEMS.register("stone_phyllite", () -> itemBlock(STONE_PHYLLITE));
    public static final RegistryObject<Item> stone_quartzite = ITEMS.register("stone_quartzite", () -> itemBlock(STONE_QUARTZITE));
    public static final RegistryObject<Item> stone_red_sandstone = ITEMS.register("stone_red_sandstone", () -> itemBlock(STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> stone_sandstone = ITEMS.register("stone_sandstone", () -> itemBlock(STONE_SANDSTONE));
    public static final RegistryObject<Item> stone_schist = ITEMS.register("stone_schist", () -> itemBlock(STONE_SCHIST));
    public static final RegistryObject<Item> stone_shale = ITEMS.register("stone_shale", () -> itemBlock(STONE_SHALE));
    public static final RegistryObject<Item> stone_slate = ITEMS.register("stone_slate", () -> itemBlock(STONE_SLATE));
    //Cobblestone
    public static final RegistryObject<Item> cobble_andesite = ITEMS.register("cobble_andesite", () -> itemBlock(COBBLE_ANDESITE));
    public static final RegistryObject<Item> cobble_basalt = ITEMS.register("cobble_basalt", () -> itemBlock(COBBLE_BASALT));
    public static final RegistryObject<Item> cobble_chalk = ITEMS.register("cobble_chalk", () -> itemBlock(COBBLE_CHALK));
    public static final RegistryObject<Item> cobble_chert = ITEMS.register("cobble_chert", () -> itemBlock(COBBLE_CHERT));
    public static final RegistryObject<Item> cobble_conglomerate = ITEMS.register("cobble_conglomerate", () -> itemBlock(COBBLE_CONGLOMERATE));
    public static final RegistryObject<Item> cobble_dacite = ITEMS.register("cobble_dacite", () -> itemBlock(COBBLE_DACITE));
    public static final RegistryObject<Item> cobble_diorite = ITEMS.register("cobble_diorite", () -> itemBlock(COBBLE_DIORITE));
    public static final RegistryObject<Item> cobble_dolomite = ITEMS.register("cobble_dolomite", () -> itemBlock(COBBLE_DOLOMITE));
    public static final RegistryObject<Item> cobble_gabbro = ITEMS.register("cobble_gabbro", () -> itemBlock(COBBLE_GABBRO));
    public static final RegistryObject<Item> cobble_gneiss = ITEMS.register("cobble_gneiss", () -> itemBlock(COBBLE_GNEISS));
    public static final RegistryObject<Item> cobble_granite = ITEMS.register("cobble_granite", () -> itemBlock(COBBLE_GRANITE));
    public static final RegistryObject<Item> cobble_limestone = ITEMS.register("cobble_limestone", () -> itemBlock(COBBLE_LIMESTONE));
    public static final RegistryObject<Item> cobble_marble = ITEMS.register("cobble_marble", () -> itemBlock(COBBLE_MARBLE));
    public static final RegistryObject<Item> cobble_phyllite = ITEMS.register("cobble_phyllite", () -> itemBlock(COBBLE_PHYLLITE));
    public static final RegistryObject<Item> cobble_quartzite = ITEMS.register("cobble_quartzite", () -> itemBlock(COBBLE_QUARTZITE));
    public static final RegistryObject<Item> cobble_red_sandstone = ITEMS.register("cobble_red_sandstone", () -> itemBlock(COBBLE_RED_SANDSTONE));
    public static final RegistryObject<Item> cobble_sandstone = ITEMS.register("cobble_sandstone", () -> itemBlock(COBBLE_SANDSTONE));
    public static final RegistryObject<Item> cobble_schist = ITEMS.register("cobble_schist", () -> itemBlock(COBBLE_SCHIST));
    public static final RegistryObject<Item> cobble_shale = ITEMS.register("cobble_shale", () -> itemBlock(COBBLE_SHALE));
    public static final RegistryObject<Item> cobble_slate = ITEMS.register("cobble_slate", () -> itemBlock(COBBLE_SLATE));
    //Rocks
    public static final RegistryObject<Item> rock_andesite = ITEMS.register("rock_andesite", () -> itemRock(ROCK_ANDESITE, ANDESITE));
    public static final RegistryObject<Item> rock_basalt = ITEMS.register("rock_basalt", () -> itemRock(ROCK_BASALT, BASALT));
    public static final RegistryObject<Item> rock_chalk = ITEMS.register("rock_chalk", () -> itemRock(ROCK_CHALK, CHALK));
    public static final RegistryObject<Item> rock_chert = ITEMS.register("rock_chert", () -> itemRock(ROCK_CHERT, CHERT));
    public static final RegistryObject<Item> rock_conglomerate = ITEMS.register("rock_conglomerate", () -> itemRock(ROCK_CONGLOMERATE, CONGLOMERATE));
    public static final RegistryObject<Item> rock_dacite = ITEMS.register("rock_dacite", () -> itemRock(ROCK_DACITE, DACITE));
    public static final RegistryObject<Item> rock_diorite = ITEMS.register("rock_diorite", () -> itemRock(ROCK_DIORITE, DIORITE));
    public static final RegistryObject<Item> rock_dolomite = ITEMS.register("rock_dolomite", () -> itemRock(ROCK_DOLOMITE, DOLOMITE));
    public static final RegistryObject<Item> rock_gabbro = ITEMS.register("rock_gabbro", () -> itemRock(ROCK_GABBRO, GABBRO));
    public static final RegistryObject<Item> rock_gneiss = ITEMS.register("rock_gneiss", () -> itemRock(ROCK_GNEISS, GNEISS));
    public static final RegistryObject<Item> rock_granite = ITEMS.register("rock_granite", () -> itemRock(ROCK_GRANITE, GRANITE));
    public static final RegistryObject<Item> rock_limestone = ITEMS.register("rock_limestone", () -> itemRock(ROCK_LIMESTONE, LIMESTONE));
    public static final RegistryObject<Item> rock_marble = ITEMS.register("rock_marble", () -> itemRock(ROCK_MARBLE, MARBLE));
    public static final RegistryObject<Item> rock_phyllite = ITEMS.register("rock_phyllite", () -> itemRock(ROCK_PHYLLITE, PHYLLITE));
    public static final RegistryObject<Item> rock_quartzite = ITEMS.register("rock_quartzite", () -> itemRock(ROCK_QUARTZITE, QUARTZITE));
    public static final RegistryObject<Item> rock_red_sandstone = ITEMS.register("rock_red_sandstone",
                                                                                 () -> itemRock(ROCK_RED_SANDSTONE, RED_SANDSTONE));
    public static final RegistryObject<Item> rock_sandstone = ITEMS.register("rock_sandstone", () -> itemRock(ROCK_SANDSTONE, SANDSTONE));
    public static final RegistryObject<Item> rock_schist = ITEMS.register("rock_schist", () -> itemRock(ROCK_SCHIST, SCHIST));
    public static final RegistryObject<Item> rock_shale = ITEMS.register("rock_shale", () -> itemRock(ROCK_SHALE, SHALE));
    public static final RegistryObject<Item> rock_slate = ITEMS.register("rock_slate", () -> itemRock(ROCK_SLATE, SLATE));
    //Polished Stones
    public static final RegistryObject<Item> polished_stone_andesite = ITEMS.register("polished_stone_andesite",
                                                                                      () -> itemBlock(POLISHED_STONE_ANDESITE));
    public static final RegistryObject<Item> polished_stone_basalt = ITEMS.register("polished_stone_basalt", () -> itemBlock(POLISHED_STONE_BASALT));
    public static final RegistryObject<Item> polished_stone_chalk = ITEMS.register("polished_stone_chalk", () -> itemBlock(POLISHED_STONE_CHALK));
    public static final RegistryObject<Item> polished_stone_chert = ITEMS.register("polished_stone_chert", () -> itemBlock(POLISHED_STONE_CHERT));
    public static final RegistryObject<Item> polished_stone_conglomerate = ITEMS.register("polished_stone_conglomerate",
                                                                                          () -> itemBlock(POLISHED_STONE_CONGLOMERATE));
    public static final RegistryObject<Item> polished_stone_dacite = ITEMS.register("polished_stone_dacite", () -> itemBlock(POLISHED_STONE_DACITE));
    public static final RegistryObject<Item> polished_stone_diorite = ITEMS.register("polished_stone_diorite",
                                                                                     () -> itemBlock(POLISHED_STONE_DIORITE));
    public static final RegistryObject<Item> polished_stone_dolomite = ITEMS.register("polished_stone_dolomite",
                                                                                      () -> itemBlock(POLISHED_STONE_DOLOMITE));
    public static final RegistryObject<Item> polished_stone_gabbro = ITEMS.register("polished_stone_gabbro", () -> itemBlock(POLISHED_STONE_GABBRO));
    public static final RegistryObject<Item> polished_stone_gneiss = ITEMS.register("polished_stone_gneiss", () -> itemBlock(POLISHED_STONE_GNEISS));
    public static final RegistryObject<Item> polished_stone_granite = ITEMS.register("polished_stone_granite",
                                                                                     () -> itemBlock(POLISHED_STONE_GRANITE));
    public static final RegistryObject<Item> polished_stone_limestone = ITEMS.register("polished_stone_limestone",
                                                                                       () -> itemBlock(POLISHED_STONE_LIMESTONE));
    public static final RegistryObject<Item> polished_stone_marble = ITEMS.register("polished_stone_marble", () -> itemBlock(POLISHED_STONE_MARBLE));
    public static final RegistryObject<Item> polished_stone_phyllite = ITEMS.register("polished_stone_phyllite",
                                                                                      () -> itemBlock(POLISHED_STONE_PHYLLITE));
    public static final RegistryObject<Item> polished_stone_quartzite = ITEMS.register("polished_stone_quartzite",
                                                                                       () -> itemBlock(POLISHED_STONE_QUARTZITE));
    public static final RegistryObject<Item> polished_stone_red_sandstone = ITEMS.register("polished_stone_red_sandstone",
                                                                                           () -> itemBlock(POLISHED_STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> polished_stone_sandstone = ITEMS.register("polished_stone_sandstone",
                                                                                       () -> itemBlock(POLISHED_STONE_SANDSTONE));
    public static final RegistryObject<Item> polished_stone_schist = ITEMS.register("polished_stone_schist", () -> itemBlock(POLISHED_STONE_SCHIST));
    public static final RegistryObject<Item> polished_stone_shale = ITEMS.register("polished_stone_shale", () -> itemBlock(POLISHED_STONE_SHALE));
    public static final RegistryObject<Item> polished_stone_slate = ITEMS.register("polished_stone_slate", () -> itemBlock(POLISHED_STONE_SLATE));
    //Sand
    public static final RegistryObject<Item> sand_andesite = ITEMS.register("sand_andesite", () -> itemBlock(SAND_ANDESITE));
    public static final RegistryObject<Item> sand_basalt = ITEMS.register("sand_basalt", () -> itemBlock(SAND_BASALT));
    public static final RegistryObject<Item> sand_chalk = ITEMS.register("sand_chalk", () -> itemBlock(SAND_CHALK));
    public static final RegistryObject<Item> sand_chert = ITEMS.register("sand_chert", () -> itemBlock(SAND_CHERT));
    public static final RegistryObject<Item> sand_conglomerate = ITEMS.register("sand_conglomerate", () -> itemBlock(SAND_CONGLOMERATE));
    public static final RegistryObject<Item> sand_dacite = ITEMS.register("sand_dacite", () -> itemBlock(SAND_DACITE));
    public static final RegistryObject<Item> sand_diorite = ITEMS.register("sand_diorite", () -> itemBlock(SAND_DIORITE));
    public static final RegistryObject<Item> sand_dolomite = ITEMS.register("sand_dolomite", () -> itemBlock(SAND_DOLOMITE));
    public static final RegistryObject<Item> sand_gabbro = ITEMS.register("sand_gabbro", () -> itemBlock(SAND_GABBRO));
    public static final RegistryObject<Item> sand_gneiss = ITEMS.register("sand_gneiss", () -> itemBlock(SAND_GNEISS));
    public static final RegistryObject<Item> sand_granite = ITEMS.register("sand_granite", () -> itemBlock(SAND_GRANITE));
    public static final RegistryObject<Item> sand_limestone = ITEMS.register("sand_limestone", () -> itemBlock(SAND_LIMESTONE));
    public static final RegistryObject<Item> sand_marble = ITEMS.register("sand_marble", () -> itemBlock(SAND_MARBLE));
    public static final RegistryObject<Item> sand_phyllite = ITEMS.register("sand_phyllite", () -> itemBlock(SAND_PHYLLITE));
    public static final RegistryObject<Item> sand_quartzite = ITEMS.register("sand_quartzite", () -> itemBlock(SAND_QUARTZITE));
    public static final RegistryObject<Item> sand_red_sandstone = ITEMS.register("sand_red_sandstone", () -> itemBlock(SAND_RED_SANDSTONE));
    public static final RegistryObject<Item> sand_sandstone = ITEMS.register("sand_sandstone", () -> itemBlock(SAND_SANDSTONE));
    public static final RegistryObject<Item> sand_schist = ITEMS.register("sand_schist", () -> itemBlock(SAND_SCHIST));
    public static final RegistryObject<Item> sand_shale = ITEMS.register("sand_shale", () -> itemBlock(SAND_SHALE));
    public static final RegistryObject<Item> sand_slate = ITEMS.register("sand_slate", () -> itemBlock(SAND_SLATE));
    //Dirt
    public static final RegistryObject<Item> dirt_andesite = ITEMS.register("dirt_andesite", () -> itemBlock(DIRT_ANDESITE));
    public static final RegistryObject<Item> dirt_basalt = ITEMS.register("dirt_basalt", () -> itemBlock(DIRT_BASALT));
    public static final RegistryObject<Item> dirt_chalk = ITEMS.register("dirt_chalk", () -> itemBlock(DIRT_CHALK));
    public static final RegistryObject<Item> dirt_chert = ITEMS.register("dirt_chert", () -> itemBlock(DIRT_CHERT));
    public static final RegistryObject<Item> dirt_conglomerate = ITEMS.register("dirt_conglomerate", () -> itemBlock(DIRT_CONGLOMERATE));
    public static final RegistryObject<Item> dirt_dacite = ITEMS.register("dirt_dacite", () -> itemBlock(DIRT_DACITE));
    public static final RegistryObject<Item> dirt_diorite = ITEMS.register("dirt_diorite", () -> itemBlock(DIRT_DIORITE));
    public static final RegistryObject<Item> dirt_dolomite = ITEMS.register("dirt_dolomite", () -> itemBlock(DIRT_DOLOMITE));
    public static final RegistryObject<Item> dirt_gabbro = ITEMS.register("dirt_gabbro", () -> itemBlock(DIRT_GABBRO));
    public static final RegistryObject<Item> dirt_gneiss = ITEMS.register("dirt_gneiss", () -> itemBlock(DIRT_GNEISS));
    public static final RegistryObject<Item> dirt_granite = ITEMS.register("dirt_granite", () -> itemBlock(DIRT_GRANITE));
    public static final RegistryObject<Item> dirt_limestone = ITEMS.register("dirt_limestone", () -> itemBlock(DIRT_LIMESTONE));
    public static final RegistryObject<Item> dirt_marble = ITEMS.register("dirt_marble", () -> itemBlock(DIRT_MARBLE));
    public static final RegistryObject<Item> dirt_phyllite = ITEMS.register("dirt_phyllite", () -> itemBlock(DIRT_PHYLLITE));
    public static final RegistryObject<Item> dirt_quartzite = ITEMS.register("dirt_quartzite", () -> itemBlock(DIRT_QUARTZITE));
    public static final RegistryObject<Item> dirt_red_sandstone = ITEMS.register("dirt_red_sandstone", () -> itemBlock(DIRT_RED_SANDSTONE));
    public static final RegistryObject<Item> dirt_sandstone = ITEMS.register("dirt_sandstone", () -> itemBlock(DIRT_SANDSTONE));
    public static final RegistryObject<Item> dirt_schist = ITEMS.register("dirt_schist", () -> itemBlock(DIRT_SCHIST));
    public static final RegistryObject<Item> dirt_shale = ITEMS.register("dirt_shale", () -> itemBlock(DIRT_SHALE));
    public static final RegistryObject<Item> dirt_slate = ITEMS.register("dirt_slate", () -> itemBlock(DIRT_SLATE));
    //Gravel
    public static final RegistryObject<Item> gravel_andesite = ITEMS.register("gravel_andesite", () -> itemBlock(GRAVEL_ANDESITE));
    public static final RegistryObject<Item> gravel_basalt = ITEMS.register("gravel_basalt", () -> itemBlock(GRAVEL_BASALT));
    public static final RegistryObject<Item> gravel_chalk = ITEMS.register("gravel_chalk", () -> itemBlock(GRAVEL_CHALK));
    public static final RegistryObject<Item> gravel_chert = ITEMS.register("gravel_chert", () -> itemBlock(GRAVEL_CHERT));
    public static final RegistryObject<Item> gravel_conglomerate = ITEMS.register("gravel_conglomerate", () -> itemBlock(GRAVEL_CONGLOMERATE));
    public static final RegistryObject<Item> gravel_dacite = ITEMS.register("gravel_dacite", () -> itemBlock(GRAVEL_DACITE));
    public static final RegistryObject<Item> gravel_diorite = ITEMS.register("gravel_diorite", () -> itemBlock(GRAVEL_DIORITE));
    public static final RegistryObject<Item> gravel_dolomite = ITEMS.register("gravel_dolomite", () -> itemBlock(GRAVEL_DOLOMITE));
    public static final RegistryObject<Item> gravel_gabbro = ITEMS.register("gravel_gabbro", () -> itemBlock(GRAVEL_GABBRO));
    public static final RegistryObject<Item> gravel_gneiss = ITEMS.register("gravel_gneiss", () -> itemBlock(GRAVEL_GNEISS));
    public static final RegistryObject<Item> gravel_granite = ITEMS.register("gravel_granite", () -> itemBlock(GRAVEL_GRANITE));
    public static final RegistryObject<Item> gravel_limestone = ITEMS.register("gravel_limestone", () -> itemBlock(GRAVEL_LIMESTONE));
    public static final RegistryObject<Item> gravel_marble = ITEMS.register("gravel_marble", () -> itemBlock(GRAVEL_MARBLE));
    public static final RegistryObject<Item> gravel_phyllite = ITEMS.register("gravel_phyllite", () -> itemBlock(GRAVEL_PHYLLITE));
    public static final RegistryObject<Item> gravel_quartzite = ITEMS.register("gravel_quartzite", () -> itemBlock(GRAVEL_QUARTZITE));
    public static final RegistryObject<Item> gravel_red_sandstone = ITEMS.register("gravel_red_sandstone", () -> itemBlock(GRAVEL_RED_SANDSTONE));
    public static final RegistryObject<Item> gravel_sandstone = ITEMS.register("gravel_sandstone", () -> itemBlock(GRAVEL_SANDSTONE));
    public static final RegistryObject<Item> gravel_schist = ITEMS.register("gravel_schist", () -> itemBlock(GRAVEL_SCHIST));
    public static final RegistryObject<Item> gravel_shale = ITEMS.register("gravel_shale", () -> itemBlock(GRAVEL_SHALE));
    public static final RegistryObject<Item> gravel_slate = ITEMS.register("gravel_slate", () -> itemBlock(GRAVEL_SLATE));
    //Grass
    public static final RegistryObject<Item> grass_andesite = ITEMS.register("grass_andesite", () -> itemBlock(GRASS_ANDESITE));
    public static final RegistryObject<Item> grass_basalt = ITEMS.register("grass_basalt", () -> itemBlock(GRASS_BASALT));
    public static final RegistryObject<Item> grass_chalk = ITEMS.register("grass_chalk", () -> itemBlock(GRASS_CHALK));
    public static final RegistryObject<Item> grass_chert = ITEMS.register("grass_chert", () -> itemBlock(GRASS_CHERT));
    public static final RegistryObject<Item> grass_conglomerate = ITEMS.register("grass_conglomerate", () -> itemBlock(GRASS_CONGLOMERATE));
    public static final RegistryObject<Item> grass_dacite = ITEMS.register("grass_dacite", () -> itemBlock(GRASS_DACITE));
    public static final RegistryObject<Item> grass_diorite = ITEMS.register("grass_diorite", () -> itemBlock(GRASS_DIORITE));
    public static final RegistryObject<Item> grass_dolomite = ITEMS.register("grass_dolomite", () -> itemBlock(GRASS_DOLOMITE));
    public static final RegistryObject<Item> grass_gabbro = ITEMS.register("grass_gabbro", () -> itemBlock(GRASS_GABBRO));
    public static final RegistryObject<Item> grass_gneiss = ITEMS.register("grass_gneiss", () -> itemBlock(GRASS_GNEISS));
    public static final RegistryObject<Item> grass_granite = ITEMS.register("grass_granite", () -> itemBlock(GRASS_GRANITE));
    public static final RegistryObject<Item> grass_limestone = ITEMS.register("grass_limestone", () -> itemBlock(GRASS_LIMESTONE));
    public static final RegistryObject<Item> grass_marble = ITEMS.register("grass_marble", () -> itemBlock(GRASS_MARBLE));
    public static final RegistryObject<Item> grass_phyllite = ITEMS.register("grass_phyllite", () -> itemBlock(GRASS_PHYLLITE));
    public static final RegistryObject<Item> grass_quartzite = ITEMS.register("grass_quartzite", () -> itemBlock(GRASS_QUARTZITE));
    public static final RegistryObject<Item> grass_red_sandstone = ITEMS.register("grass_red_sandstone", () -> itemBlock(GRASS_RED_SANDSTONE));
    public static final RegistryObject<Item> grass_sandstone = ITEMS.register("grass_sandstone", () -> itemBlock(GRASS_SANDSTONE));
    public static final RegistryObject<Item> grass_schist = ITEMS.register("grass_schist", () -> itemBlock(GRASS_SCHIST));
    public static final RegistryObject<Item> grass_shale = ITEMS.register("grass_shale", () -> itemBlock(GRASS_SHALE));
    public static final RegistryObject<Item> grass_slate = ITEMS.register("grass_slate", () -> itemBlock(GRASS_SLATE));
    //Clay
    public static final RegistryObject<Item> clay = ITEMS.register("clay", () -> itemBlock(EvolutionBlocks.CLAY));
    public static final RegistryObject<Item> clayball = ITEMS.register("clayball", ItemClay::new);
    //Peat
    public static final RegistryObject<Item> peat = ITEMS.register("peat", () -> itemBlock(EvolutionBlocks.PEAT));
    public static final RegistryObject<Item> grass_peat = ITEMS.register("grass_peat", () -> itemBlock(GRASS_PEAT));
    //Dry Grass
    public static final RegistryObject<Item> dry_grass_andesite = ITEMS.register("dry_grass_andesite", () -> itemBlock(DRY_GRASS_ANDESITE));
    public static final RegistryObject<Item> dry_grass_basalt = ITEMS.register("dry_grass_basalt", () -> itemBlock(DRY_GRASS_BASALT));
    public static final RegistryObject<Item> dry_grass_chalk = ITEMS.register("dry_grass_chalk", () -> itemBlock(DRY_GRASS_CHALK));
    public static final RegistryObject<Item> dry_grass_chert = ITEMS.register("dry_grass_chert", () -> itemBlock(DRY_GRASS_CHERT));
    public static final RegistryObject<Item> dry_grass_conglomerate = ITEMS.register("dry_grass_conglomerate",
                                                                                     () -> itemBlock(DRY_GRASS_CONGLOMERATE));
    public static final RegistryObject<Item> dry_grass_dacite = ITEMS.register("dry_grass_dacite", () -> itemBlock(DRY_GRASS_DACITE));
    public static final RegistryObject<Item> dry_grass_diorite = ITEMS.register("dry_grass_diorite", () -> itemBlock(DRY_GRASS_DIORITE));
    public static final RegistryObject<Item> dry_grass_dolomite = ITEMS.register("dry_grass_dolomite", () -> itemBlock(DRY_GRASS_DOLOMITE));
    public static final RegistryObject<Item> dry_grass_gabbro = ITEMS.register("dry_grass_gabbro", () -> itemBlock(DRY_GRASS_GABBRO));
    public static final RegistryObject<Item> dry_grass_gneiss = ITEMS.register("dry_grass_gneiss", () -> itemBlock(DRY_GRASS_GNEISS));
    public static final RegistryObject<Item> dry_grass_granite = ITEMS.register("dry_grass_granite", () -> itemBlock(DRY_GRASS_GRANITE));
    public static final RegistryObject<Item> dry_grass_limestone = ITEMS.register("dry_grass_limestone", () -> itemBlock(DRY_GRASS_LIMESTONE));
    public static final RegistryObject<Item> dry_grass_marble = ITEMS.register("dry_grass_marble", () -> itemBlock(DRY_GRASS_MARBLE));
    public static final RegistryObject<Item> dry_grass_phyllite = ITEMS.register("dry_grass_phyllite", () -> itemBlock(DRY_GRASS_PHYLLITE));
    public static final RegistryObject<Item> dry_grass_quartzite = ITEMS.register("dry_grass_quartzite", () -> itemBlock(DRY_GRASS_QUARTZITE));
    public static final RegistryObject<Item> dry_grass_red_sandstone = ITEMS.register("dry_grass_red_sandstone",
                                                                                      () -> itemBlock(DRY_GRASS_RED_SANDSTONE));
    public static final RegistryObject<Item> dry_grass_sandstone = ITEMS.register("dry_grass_sandstone", () -> itemBlock(DRY_GRASS_SANDSTONE));
    public static final RegistryObject<Item> dry_grass_schist = ITEMS.register("dry_grass_schist", () -> itemBlock(DRY_GRASS_SCHIST));
    public static final RegistryObject<Item> dry_grass_shale = ITEMS.register("dry_grass_shale", () -> itemBlock(DRY_GRASS_SHALE));
    public static final RegistryObject<Item> dry_grass_slate = ITEMS.register("dry_grass_slate", () -> itemBlock(DRY_GRASS_SLATE));
    //Log
    public static final RegistryObject<ItemLog> log_acacia = ITEMS.register("log_acacia", () -> itemLog(ACACIA, LOG_ACACIA));
    public static final RegistryObject<ItemLog> log_aspen = ITEMS.register("log_aspen", () -> itemLog(ASPEN, LOG_ASPEN));
    public static final RegistryObject<ItemLog> log_birch = ITEMS.register("log_birch", () -> itemLog(BIRCH, LOG_BIRCH));
    public static final RegistryObject<ItemLog> log_cedar = ITEMS.register("log_cedar", () -> itemLog(CEDAR, LOG_CEDAR));
    public static final RegistryObject<ItemLog> log_ebony = ITEMS.register("log_ebony", () -> itemLog(EBONY, LOG_EBONY));
    public static final RegistryObject<ItemLog> log_elm = ITEMS.register("log_elm", () -> itemLog(ELM, LOG_ELM));
    public static final RegistryObject<ItemLog> log_eucalyptus = ITEMS.register("log_eucalyptus", () -> itemLog(EUCALYPTUS, LOG_EUCALYPTUS));
    public static final RegistryObject<ItemLog> log_fir = ITEMS.register("log_fir", () -> itemLog(FIR, LOG_FIR));
    public static final RegistryObject<ItemLog> log_kapok = ITEMS.register("log_kapok", () -> itemLog(KAPOK, LOG_KAPOK));
    public static final RegistryObject<ItemLog> log_mangrove = ITEMS.register("log_mangrove", () -> itemLog(MANGROVE, LOG_MANGROVE));
    public static final RegistryObject<ItemLog> log_maple = ITEMS.register("log_maple", () -> itemLog(MAPLE, LOG_MAPLE));
    public static final RegistryObject<ItemLog> log_oak = ITEMS.register("log_oak", () -> itemLog(OAK, LOG_OAK));
    public static final RegistryObject<ItemLog> log_old_oak = ITEMS.register("log_old_oak", () -> itemLog(OLD_OAK, LOG_OLD_OAK));
    public static final RegistryObject<ItemLog> log_palm = ITEMS.register("log_palm", () -> itemLog(PALM, LOG_PALM));
    public static final RegistryObject<ItemLog> log_pine = ITEMS.register("log_pine", () -> itemLog(PINE, LOG_PINE));
    public static final RegistryObject<ItemLog> log_redwood = ITEMS.register("log_redwood", () -> itemLog(REDWOOD, LOG_REDWOOD));
    public static final RegistryObject<ItemLog> log_spruce = ITEMS.register("log_spruce", () -> itemLog(SPRUCE, LOG_SPRUCE));
    public static final RegistryObject<ItemLog> log_willow = ITEMS.register("log_willow", () -> itemLog(WILLOW, LOG_WILLOW));
    //Leaves
    public static final RegistryObject<Item> leaves_acacia = ITEMS.register("leaves_acacia", () -> woodBlock(LEAVES_ACACIA));
    public static final RegistryObject<Item> leaves_aspen = ITEMS.register("leaves_aspen", () -> woodBlock(LEAVES_ASPEN));
    public static final RegistryObject<Item> leaves_birch = ITEMS.register("leaves_birch", () -> woodBlock(LEAVES_BIRCH));
    public static final RegistryObject<Item> leaves_cedar = ITEMS.register("leaves_cedar", () -> woodBlock(LEAVES_CEDAR));
    public static final RegistryObject<Item> leaves_ebony = ITEMS.register("leaves_ebony", () -> woodBlock(LEAVES_EBONY));
    public static final RegistryObject<Item> leaves_elm = ITEMS.register("leaves_elm", () -> woodBlock(LEAVES_ELM));
    public static final RegistryObject<Item> leaves_eucalyptus = ITEMS.register("leaves_eucalyptus", () -> woodBlock(LEAVES_EUCALYPTUS));
    public static final RegistryObject<Item> leaves_fir = ITEMS.register("leaves_fir", () -> woodBlock(LEAVES_FIR));
    public static final RegistryObject<Item> leaves_kapok = ITEMS.register("leaves_kapok", () -> woodBlock(LEAVES_KAPOK));
    public static final RegistryObject<Item> leaves_mangrove = ITEMS.register("leaves_mangrove", () -> woodBlock(LEAVES_MANGROVE));
    public static final RegistryObject<Item> leaves_maple = ITEMS.register("leaves_maple", () -> woodBlock(LEAVES_MAPLE));
    public static final RegistryObject<Item> leaves_oak = ITEMS.register("leaves_oak", () -> woodBlock(LEAVES_OAK));
    public static final RegistryObject<Item> leaves_old_oak = ITEMS.register("leaves_old_oak", () -> woodBlock(LEAVES_OLD_OAK));
    public static final RegistryObject<Item> leaves_palm = ITEMS.register("leaves_palm", () -> woodBlock(LEAVES_PALM));
    public static final RegistryObject<Item> leaves_pine = ITEMS.register("leaves_pine", () -> woodBlock(LEAVES_PINE));
    public static final RegistryObject<Item> leaves_redwood = ITEMS.register("leaves_redwood", () -> woodBlock(LEAVES_REDWOOD));
    public static final RegistryObject<Item> leaves_spruce = ITEMS.register("leaves_spruce", () -> woodBlock(LEAVES_SPRUCE));
    public static final RegistryObject<Item> leaves_willow = ITEMS.register("leaves_willow", () -> woodBlock(LEAVES_WILLOW));
    //Sapling
    public static final RegistryObject<Item> sapling_acacia = ITEMS.register("sapling_acacia", () -> woodBlock(SAPLING_ACACIA));
    public static final RegistryObject<Item> sapling_aspen = ITEMS.register("sapling_aspen", () -> woodBlock(SAPLING_ASPEN));
    public static final RegistryObject<Item> sapling_birch = ITEMS.register("sapling_birch", () -> woodBlock(SAPLING_BIRCH));
    public static final RegistryObject<Item> sapling_cedar = ITEMS.register("sapling_cedar", () -> woodBlock(SAPLING_CEDAR));
    public static final RegistryObject<Item> sapling_ebony = ITEMS.register("sapling_ebony", () -> woodBlock(SAPLING_EBONY));
    public static final RegistryObject<Item> sapling_elm = ITEMS.register("sapling_elm", () -> woodBlock(SAPLING_ELM));
    public static final RegistryObject<Item> sapling_eucalyptus = ITEMS.register("sapling_eucalyptus", () -> woodBlock(SAPLING_EUCALYPTUS));
    public static final RegistryObject<Item> sapling_fir = ITEMS.register("sapling_fir", () -> woodBlock(SAPLING_FIR));
    public static final RegistryObject<Item> sapling_kapok = ITEMS.register("sapling_kapok", () -> woodBlock(SAPLING_KAPOK));
    public static final RegistryObject<Item> sapling_mangrove = ITEMS.register("sapling_mangrove", () -> woodBlock(SAPLING_MANGROVE));
    public static final RegistryObject<Item> sapling_maple = ITEMS.register("sapling_maple", () -> woodBlock(SAPLING_MAPLE));
    public static final RegistryObject<Item> sapling_oak = ITEMS.register("sapling_oak", () -> woodBlock(SAPLING_OAK));
    public static final RegistryObject<Item> sapling_old_oak = ITEMS.register("sapling_old_oak", () -> woodBlock(SAPLING_OLD_OAK));
    public static final RegistryObject<Item> sapling_palm = ITEMS.register("sapling_palm", () -> woodBlock(SAPLING_PALM));
    public static final RegistryObject<Item> sapling_pine = ITEMS.register("sapling_pine", () -> woodBlock(SAPLING_PINE));
    public static final RegistryObject<Item> sapling_redwood = ITEMS.register("sapling_redwood", () -> woodBlock(SAPLING_REDWOOD));
    public static final RegistryObject<Item> sapling_spruce = ITEMS.register("sapling_spruce", () -> woodBlock(SAPLING_SPRUCE));
    public static final RegistryObject<Item> sapling_willow = ITEMS.register("sapling_willow", () -> woodBlock(SAPLING_WILLOW));
    //Debug
    public static final RegistryObject<Item> nitrogen_setter = ITEMS.register("nitrogen_setter",
                                                                              () -> new ItemChunkStorageSetter(propMisc(), EnumStorage.NITROGEN));
    public static final RegistryObject<Item> nitrogen_getter = ITEMS.register("nitrogen_getter",
                                                                              () -> new ItemChunkStorageGetter(propMisc(), EnumStorage.NITROGEN));
    public static final RegistryObject<Item> phosphorus_setter = ITEMS.register("phosphorus_setter",
                                                                                () -> new ItemChunkStorageSetter(propMisc(), EnumStorage.PHOSPHORUS));
    public static final RegistryObject<Item> phosphorus_getter = ITEMS.register("phosphorus_getter",
                                                                                () -> new ItemChunkStorageGetter(propMisc(), EnumStorage.PHOSPHORUS));
    public static final RegistryObject<Item> potassium_setter = ITEMS.register("potassium_setter",
                                                                               () -> new ItemChunkStorageSetter(propMisc(), EnumStorage.POTASSIUM));
    public static final RegistryObject<Item> potassium_getter = ITEMS.register("potassium_getter",
                                                                               () -> new ItemChunkStorageGetter(propMisc(), EnumStorage.POTASSIUM));
    public static final RegistryObject<Item> water_setter = ITEMS.register("water_setter",
                                                                           () -> new ItemChunkStorageSetter(propMisc(), EnumStorage.WATER));
    public static final RegistryObject<Item> water_getter = ITEMS.register("water_getter",
                                                                           () -> new ItemChunkStorageGetter(propMisc(), EnumStorage.WATER));
    public static final RegistryObject<Item> carbon_dioxide_setter = ITEMS.register("carbon_dioxide_setter",
                                                                                    () -> new ItemChunkStorageSetter(propMisc(),
                                                                                                                     EnumStorage.CARBON_DIOXIDE));
    public static final RegistryObject<Item> carbon_dioxide_getter = ITEMS.register("carbon_dioxide_getter",
                                                                                    () -> new ItemChunkStorageGetter(propMisc(),
                                                                                                                     EnumStorage.CARBON_DIOXIDE));
    public static final RegistryObject<Item> oxygen_setter = ITEMS.register("oxygen_setter",
                                                                            () -> new ItemChunkStorageSetter(propMisc(), EnumStorage.OXYGEN));
    public static final RegistryObject<Item> oxygen_getter = ITEMS.register("oxygen_getter",
                                                                            () -> new ItemChunkStorageGetter(propMisc(), EnumStorage.OXYGEN));
    public static final RegistryObject<Item> gas_nitrogen_setter = ITEMS.register("gas_nitrogen_setter",
                                                                                  () -> new ItemChunkStorageSetter(propMisc(),
                                                                                                                   EnumStorage.GAS_NITROGEN));
    public static final RegistryObject<Item> gas_nitrogen_getter = ITEMS.register("gas_nitrogen_getter",
                                                                                  () -> new ItemChunkStorageGetter(propMisc(),
                                                                                                                   EnumStorage.GAS_NITROGEN));
    public static final RegistryObject<Item> organic_setter = ITEMS.register("organic_setter",
                                                                             () -> new ItemChunkStorageSetter(propMisc(), EnumStorage.ORGANIC));
    public static final RegistryObject<Item> organic_getter = ITEMS.register("organic_getter",
                                                                             () -> new ItemChunkStorageGetter(propMisc(), EnumStorage.ORGANIC));
    public static final RegistryObject<Item> clock = ITEMS.register("clock", () -> new ItemClock(propMisc()));
    public static final RegistryObject<Item> puzzle = ITEMS.register("puzzle", () -> itemBlock(PUZZLE));
    public static final RegistryObject<Item> schematic_block = ITEMS.register("schematic_block", () -> itemBlock(SCHEMATIC_BLOCK));
    //Vegetation
    public static final RegistryObject<Item> grass = ITEMS.register("grass", () -> itemBlock(GRASS));
    public static final RegistryObject<Item> tallgrass = ITEMS.register("tallgrass", () -> itemBlock(TALLGRASS));
    //Feces
    public static final RegistryObject<Item> feces = ITEMS.register("feces", () -> itemBlock(FECES));
    //Axe Heads
    public static final RegistryObject<Item> axe_head_andesite = ITEMS.register("axe_head_andesite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_basalt = ITEMS.register("axe_head_basalt", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_chalk = ITEMS.register("axe_head_chalk", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_chert = ITEMS.register("axe_head_chert", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_conglomerate = ITEMS.register("axe_head_conglomerate", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_dacite = ITEMS.register("axe_head_dacite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_diorite = ITEMS.register("axe_head_diorite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_dolomite = ITEMS.register("axe_head_dolomite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_gabbro = ITEMS.register("axe_head_gabbro", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_gneiss = ITEMS.register("axe_head_gneiss", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_granite = ITEMS.register("axe_head_granite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_limestone = ITEMS.register("axe_head_limestone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_marble = ITEMS.register("axe_head_marble", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_phyllite = ITEMS.register("axe_head_phyllite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_quartzite = ITEMS.register("axe_head_quartzite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_red_sandstone = ITEMS.register("axe_head_red_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_sandstone = ITEMS.register("axe_head_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_schist = ITEMS.register("axe_head_schist", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_shale = ITEMS.register("axe_head_shale", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> axe_head_slate = ITEMS.register("axe_head_slate", EvolutionItems::stoneHeads);
    //Axes
    public static final RegistryObject<Item> axe_andesite = ITEMS.register("axe_andesite", () -> axeStone(EvolutionToolMaterials.STONE_ANDESITE));
    public static final RegistryObject<Item> axe_basalt = ITEMS.register("axe_basalt", () -> axeStone(EvolutionToolMaterials.STONE_BASALT));
    public static final RegistryObject<Item> axe_chalk = ITEMS.register("axe_chalk", () -> axeStone(EvolutionToolMaterials.STONE_CHALK));
    public static final RegistryObject<Item> axe_chert = ITEMS.register("axe_chert", () -> axeStone(EvolutionToolMaterials.STONE_CHERT));
    public static final RegistryObject<Item> axe_conglomerate = ITEMS.register("axe_conglomerate",
                                                                               () -> axeStone(EvolutionToolMaterials.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> axe_dacite = ITEMS.register("axe_dacite", () -> axeStone(EvolutionToolMaterials.STONE_DACITE));
    public static final RegistryObject<Item> axe_diorite = ITEMS.register("axe_diorite", () -> axeStone(EvolutionToolMaterials.STONE_DIORITE));
    public static final RegistryObject<Item> axe_dolomite = ITEMS.register("axe_dolomite", () -> axeStone(EvolutionToolMaterials.STONE_DOLOMITE));
    public static final RegistryObject<Item> axe_gabbro = ITEMS.register("axe_gabbro", () -> axeStone(EvolutionToolMaterials.STONE_GABBRO));
    public static final RegistryObject<Item> axe_gneiss = ITEMS.register("axe_gneiss", () -> axeStone(EvolutionToolMaterials.STONE_GNEISS));
    public static final RegistryObject<Item> axe_granite = ITEMS.register("axe_granite", () -> axeStone(EvolutionToolMaterials.STONE_GRANITE));
    public static final RegistryObject<Item> axe_limestone = ITEMS.register("axe_limestone", () -> axeStone(EvolutionToolMaterials.STONE_LIMESTONE));
    public static final RegistryObject<Item> axe_marble = ITEMS.register("axe_marble", () -> axeStone(EvolutionToolMaterials.STONE_MARBLE));
    public static final RegistryObject<Item> axe_phyllite = ITEMS.register("axe_phyllite", () -> axeStone(EvolutionToolMaterials.STONE_PHYLLITE));
    public static final RegistryObject<Item> axe_quartzite = ITEMS.register("axe_quartzite", () -> axeStone(EvolutionToolMaterials.STONE_QUARTZITE));
    public static final RegistryObject<Item> axe_red_sandstone = ITEMS.register("axe_red_sandstone",
                                                                                () -> axeStone(EvolutionToolMaterials.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> axe_sandstone = ITEMS.register("axe_sandstone", () -> axeStone(EvolutionToolMaterials.STONE_SANDSTONE));
    public static final RegistryObject<Item> axe_schist = ITEMS.register("axe_schist", () -> axeStone(EvolutionToolMaterials.STONE_SCHIST));
    public static final RegistryObject<Item> axe_shale = ITEMS.register("axe_shale", () -> axeStone(EvolutionToolMaterials.STONE_SHALE));
    public static final RegistryObject<Item> axe_slate = ITEMS.register("axe_slate", () -> axeStone(EvolutionToolMaterials.STONE_SLATE));
    //Shovels
    public static final RegistryObject<Item> shovel_andesite = ITEMS.register("shovel_andesite",
                                                                              () -> shovelStone(EvolutionToolMaterials.STONE_ANDESITE));
    public static final RegistryObject<Item> shovel_basalt = ITEMS.register("shovel_basalt", () -> shovelStone(EvolutionToolMaterials.STONE_BASALT));
    public static final RegistryObject<Item> shovel_chalk = ITEMS.register("shovel_chalk", () -> shovelStone(EvolutionToolMaterials.STONE_CHALK));
    public static final RegistryObject<Item> shovel_chert = ITEMS.register("shovel_chert", () -> shovelStone(EvolutionToolMaterials.STONE_CHERT));
    public static final RegistryObject<Item> shovel_conglomerate = ITEMS.register("shovel_conglomerate",
                                                                                  () -> shovelStone(EvolutionToolMaterials.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> shovel_dacite = ITEMS.register("shovel_dacite", () -> shovelStone(EvolutionToolMaterials.STONE_DACITE));
    public static final RegistryObject<Item> shovel_diorite = ITEMS.register("shovel_diorite",
                                                                             () -> shovelStone(EvolutionToolMaterials.STONE_DIORITE));
    public static final RegistryObject<Item> shovel_dolomite = ITEMS.register("shovel_dolomite",
                                                                              () -> shovelStone(EvolutionToolMaterials.STONE_DOLOMITE));
    public static final RegistryObject<Item> shovel_gabbro = ITEMS.register("shovel_gabbro", () -> shovelStone(EvolutionToolMaterials.STONE_GABBRO));
    public static final RegistryObject<Item> shovel_gneiss = ITEMS.register("shovel_gneiss", () -> shovelStone(EvolutionToolMaterials.STONE_GNEISS));
    public static final RegistryObject<Item> shovel_granite = ITEMS.register("shovel_granite",
                                                                             () -> shovelStone(EvolutionToolMaterials.STONE_GRANITE));
    public static final RegistryObject<Item> shovel_limestone = ITEMS.register("shovel_limestone",
                                                                               () -> shovelStone(EvolutionToolMaterials.STONE_LIMESTONE));
    public static final RegistryObject<Item> shovel_marble = ITEMS.register("shovel_marble", () -> shovelStone(EvolutionToolMaterials.STONE_MARBLE));
    public static final RegistryObject<Item> shovel_phyllite = ITEMS.register("shovel_phyllite",
                                                                              () -> shovelStone(EvolutionToolMaterials.STONE_PHYLLITE));
    public static final RegistryObject<Item> shovel_quartzite = ITEMS.register("shovel_quartzite",
                                                                               () -> shovelStone(EvolutionToolMaterials.STONE_QUARTZITE));
    public static final RegistryObject<Item> shovel_red_sandstone = ITEMS.register("shovel_red_sandstone",
                                                                                   () -> shovelStone(EvolutionToolMaterials.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> shovel_sandstone = ITEMS.register("shovel_sandstone",
                                                                               () -> shovelStone(EvolutionToolMaterials.STONE_SANDSTONE));
    public static final RegistryObject<Item> shovel_schist = ITEMS.register("shovel_schist", () -> shovelStone(EvolutionToolMaterials.STONE_SCHIST));
    public static final RegistryObject<Item> shovel_shale = ITEMS.register("shovel_shale", () -> shovelStone(EvolutionToolMaterials.STONE_SHALE));
    public static final RegistryObject<Item> shovel_slate = ITEMS.register("shovel_slate", () -> shovelStone(EvolutionToolMaterials.STONE_SLATE));
    //Planks
    public static final RegistryObject<Item> planks_acacia = ITEMS.register("planks_acacia", () -> woodBlock(PLANKS_ACACIA));
    public static final RegistryObject<Item> planks_aspen = ITEMS.register("planks_aspen", () -> woodBlock(PLANKS_ASPEN));
    public static final RegistryObject<Item> planks_birch = ITEMS.register("planks_birch", () -> woodBlock(PLANKS_BIRCH));
    public static final RegistryObject<Item> planks_cedar = ITEMS.register("planks_cedar", () -> woodBlock(PLANKS_CEDAR));
    public static final RegistryObject<Item> planks_ebony = ITEMS.register("planks_ebony", () -> woodBlock(PLANKS_EBONY));
    public static final RegistryObject<Item> planks_elm = ITEMS.register("planks_elm", () -> woodBlock(PLANKS_ELM));
    public static final RegistryObject<Item> planks_eucalyptus = ITEMS.register("planks_eucalyptus", () -> woodBlock(PLANKS_EUCALYPTUS));
    public static final RegistryObject<Item> planks_fir = ITEMS.register("planks_fir", () -> woodBlock(PLANKS_FIR));
    public static final RegistryObject<Item> planks_kapok = ITEMS.register("planks_kapok", () -> woodBlock(PLANKS_KAPOK));
    public static final RegistryObject<Item> planks_mangrove = ITEMS.register("planks_mangrove", () -> woodBlock(PLANKS_MANGROVE));
    public static final RegistryObject<Item> planks_maple = ITEMS.register("planks_maple", () -> woodBlock(PLANKS_MAPLE));
    public static final RegistryObject<Item> planks_oak = ITEMS.register("planks_oak", () -> woodBlock(PLANKS_OAK));
    public static final RegistryObject<Item> planks_old_oak = ITEMS.register("planks_old_oak", () -> woodBlock(PLANKS_OLD_OAK));
    public static final RegistryObject<Item> planks_palm = ITEMS.register("planks_palm", () -> woodBlock(PLANKS_PALM));
    public static final RegistryObject<Item> planks_pine = ITEMS.register("planks_pine", () -> woodBlock(PLANKS_PINE));
    public static final RegistryObject<Item> planks_redwood = ITEMS.register("planks_redwood", () -> woodBlock(PLANKS_REDWOOD));
    public static final RegistryObject<Item> planks_spruce = ITEMS.register("planks_spruce", () -> woodBlock(PLANKS_SPRUCE));
    public static final RegistryObject<Item> planks_willow = ITEMS.register("planks_willow", () -> woodBlock(PLANKS_WILLOW));
    //Shadow Hound Block
//    public static final RegistryObject<Item> shadowhound = ITEMS.register("shadowhound",
//                                                                          () -> new ItemBlock(SHADOWHOUND.get(),
//                                                                                              propMisc().setTEISR(() ->
//                                                                                              RenderStackTileShadowHound::new)));
    //Javelin Heads
    public static final RegistryObject<Item> javelin_head_andesite = ITEMS.register("javelin_head_andesite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_basalt = ITEMS.register("javelin_head_basalt", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_chalk = ITEMS.register("javelin_head_chalk", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_chert = ITEMS.register("javelin_head_chert", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_conglomerate = ITEMS.register("javelin_head_conglomerate", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_dacite = ITEMS.register("javelin_head_dacite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_diorite = ITEMS.register("javelin_head_diorite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_dolomite = ITEMS.register("javelin_head_dolomite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_gabbro = ITEMS.register("javelin_head_gabbro", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_gneiss = ITEMS.register("javelin_head_gneiss", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_granite = ITEMS.register("javelin_head_granite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_limestone = ITEMS.register("javelin_head_limestone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_marble = ITEMS.register("javelin_head_marble", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_phyllite = ITEMS.register("javelin_head_phyllite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_quartzite = ITEMS.register("javelin_head_quartzite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_red_sandstone = ITEMS.register("javelin_head_red_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_sandstone = ITEMS.register("javelin_head_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_schist = ITEMS.register("javelin_head_schist", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_shale = ITEMS.register("javelin_head_shale", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> javelin_head_slate = ITEMS.register("javelin_head_slate", EvolutionItems::stoneHeads);
    //Javelins
    public static final RegistryObject<Item> javelin_andesite = ITEMS.register("javelin_andesite",
                                                                               () -> javelin(EvolutionToolMaterials.STONE_ANDESITE));
    public static final RegistryObject<Item> javelin_basalt = ITEMS.register("javelin_basalt", () -> javelin(EvolutionToolMaterials.STONE_BASALT));
    public static final RegistryObject<Item> javelin_chalk = ITEMS.register("javelin_chalk", () -> javelin(EvolutionToolMaterials.STONE_CHALK));
    public static final RegistryObject<Item> javelin_chert = ITEMS.register("javelin_chert", () -> javelin(EvolutionToolMaterials.STONE_CHERT));
    public static final RegistryObject<Item> javelin_conglomerate = ITEMS.register("javelin_conglomerate",
                                                                                   () -> javelin(EvolutionToolMaterials.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> javelin_dacite = ITEMS.register("javelin_dacite", () -> javelin(EvolutionToolMaterials.STONE_DACITE));
    public static final RegistryObject<Item> javelin_diorite = ITEMS.register("javelin_diorite", () -> javelin(EvolutionToolMaterials.STONE_DIORITE));
    public static final RegistryObject<Item> javelin_dolomite = ITEMS.register("javelin_dolomite",
                                                                               () -> javelin(EvolutionToolMaterials.STONE_DOLOMITE));
    public static final RegistryObject<Item> javelin_gabbro = ITEMS.register("javelin_gabbro", () -> javelin(EvolutionToolMaterials.STONE_GABBRO));
    public static final RegistryObject<Item> javelin_gneiss = ITEMS.register("javelin_gneiss", () -> javelin(EvolutionToolMaterials.STONE_GNEISS));
    public static final RegistryObject<Item> javelin_granite = ITEMS.register("javelin_granite", () -> javelin(EvolutionToolMaterials.STONE_GRANITE));
    public static final RegistryObject<Item> javelin_limestone = ITEMS.register("javelin_limestone",
                                                                                () -> javelin(EvolutionToolMaterials.STONE_LIMESTONE));
    public static final RegistryObject<Item> javelin_marble = ITEMS.register("javelin_marble", () -> javelin(EvolutionToolMaterials.STONE_MARBLE));
    public static final RegistryObject<Item> javelin_phyllite = ITEMS.register("javelin_phyllite",
                                                                               () -> javelin(EvolutionToolMaterials.STONE_PHYLLITE));
    public static final RegistryObject<Item> javelin_quartzite = ITEMS.register("javelin_quartzite",
                                                                                () -> javelin(EvolutionToolMaterials.STONE_QUARTZITE));
    public static final RegistryObject<Item> javelin_red_sandstone = ITEMS.register("javelin_red_sandstone",
                                                                                    () -> javelin(EvolutionToolMaterials.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> javelin_sandstone = ITEMS.register("javelin_sandstone",
                                                                                () -> javelin(EvolutionToolMaterials.STONE_SANDSTONE));
    public static final RegistryObject<Item> javelin_schist = ITEMS.register("javelin_schist", () -> javelin(EvolutionToolMaterials.STONE_SCHIST));
    public static final RegistryObject<Item> javelin_shale = ITEMS.register("javelin_shale", () -> javelin(EvolutionToolMaterials.STONE_SHALE));
    public static final RegistryObject<Item> javelin_slate = ITEMS.register("javelin_slate", () -> javelin(EvolutionToolMaterials.STONE_SLATE));
    //Metal Blocks
    public static final RegistryObject<Item> block_copper = ITEMS.register("block_copper", () -> new ItemBlockMetal(BLOCK_COPPER.get(), propMisc()));
    //Metal Ingots
    public static final RegistryObject<Item> ingot_copper = ITEMS.register("ingot_copper", EvolutionItems::item);
    //Metal Nuggets
    public static final RegistryObject<Item> nugget_copper = ITEMS.register("nugget_copper", EvolutionItems::item);
    //Shovel Heads
    public static final RegistryObject<Item> shovel_head_andesite = ITEMS.register("shovel_head_andesite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_basalt = ITEMS.register("shovel_head_basalt", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_chalk = ITEMS.register("shovel_head_chalk", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_chert = ITEMS.register("shovel_head_chert", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_conglomerate = ITEMS.register("shovel_head_conglomerate", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_dacite = ITEMS.register("shovel_head_dacite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_diorite = ITEMS.register("shovel_head_diorite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_dolomite = ITEMS.register("shovel_head_dolomite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_gabbro = ITEMS.register("shovel_head_gabbro", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_gneiss = ITEMS.register("shovel_head_gneiss", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_granite = ITEMS.register("shovel_head_granite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_limestone = ITEMS.register("shovel_head_limestone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_marble = ITEMS.register("shovel_head_marble", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_phyllite = ITEMS.register("shovel_head_phyllite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_quartzite = ITEMS.register("shovel_head_quartzite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_red_sandstone = ITEMS.register("shovel_head_red_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_sandstone = ITEMS.register("shovel_head_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_schist = ITEMS.register("shovel_head_schist", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_shale = ITEMS.register("shovel_head_shale", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> shovel_head_slate = ITEMS.register("shovel_head_slate", EvolutionItems::stoneHeads);
    //Hammer Heads
    public static final RegistryObject<Item> hammer_head_andesite = ITEMS.register("hammer_head_andesite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_basalt = ITEMS.register("hammer_head_basalt", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_chalk = ITEMS.register("hammer_head_chalk", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_chert = ITEMS.register("hammer_head_chert", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_conglomerate = ITEMS.register("hammer_head_conglomerate", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_dacite = ITEMS.register("hammer_head_dacite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_diorite = ITEMS.register("hammer_head_diorite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_dolomite = ITEMS.register("hammer_head_dolomite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_gabbro = ITEMS.register("hammer_head_gabbro", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_gneiss = ITEMS.register("hammer_head_gneiss", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_granite = ITEMS.register("hammer_head_granite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_limestone = ITEMS.register("hammer_head_limestone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_marble = ITEMS.register("hammer_head_marble", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_phyllite = ITEMS.register("hammer_head_phyllite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_quartzite = ITEMS.register("hammer_head_quartzite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_red_sandstone = ITEMS.register("hammer_head_red_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_sandstone = ITEMS.register("hammer_head_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_schist = ITEMS.register("hammer_head_schist", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_shale = ITEMS.register("hammer_head_shale", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hammer_head_slate = ITEMS.register("hammer_head_slate", EvolutionItems::stoneHeads);
    //Torch
    public static final RegistryObject<Item> torch = ITEMS.register("torch", () -> new ItemTorch(propMisc()));
    public static final RegistryObject<Item> torch_unlit = ITEMS.register("torch_unlit", () -> new ItemTorchUnlit(propMisc()));
    //Clothes
    public static final RegistryObject<Item> hat = ITEMS.register("temp_hat", () -> new ItemHat(propMisc()));
    public static final RegistryObject<Item> shirt = ITEMS.register("temp_shirt", () -> new ItemShirt(propMisc()));
    public static final RegistryObject<Item> trousers = ITEMS.register("temp_trousers", () -> new ItemTrousers(propMisc()));
    public static final RegistryObject<Item> socks = ITEMS.register("temp_socks", () -> new ItemSocks(propMisc()));
    public static final RegistryObject<Item> cape = ITEMS.register("temp_cape", () -> new ItemCloak(propMisc()));
    public static final RegistryObject<Item> mask = ITEMS.register("temp_mask", () -> new ItemMask(propMisc()));
    public static final RegistryObject<Item> backpack = ITEMS.register("temp_backpack", () -> new ItemBackpack(propMisc()));
    public static final RegistryObject<Item> quiver = ITEMS.register("temp_quiver", () -> new ItemQuiver(propMisc()));
    //Plank
    public static final RegistryObject<Item> plank_acacia = ITEMS.register("plank_acacia", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_aspen = ITEMS.register("plank_aspen", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_birch = ITEMS.register("plank_birch", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_cedar = ITEMS.register("plank_cedar", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_ebony = ITEMS.register("plank_ebony", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_elm = ITEMS.register("plank_elm", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_eucalyptus = ITEMS.register("plank_eucalyptus", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_fir = ITEMS.register("plank_fir", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_kapok = ITEMS.register("plank_kapok", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_mangrove = ITEMS.register("plank_mangrove", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_maple = ITEMS.register("plank_maple", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_oak = ITEMS.register("plank_oak", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_old_oak = ITEMS.register("plank_old_oak", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_palm = ITEMS.register("plank_palm", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_pine = ITEMS.register("plank_pine", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_redwood = ITEMS.register("plank_redwood", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_spruce = ITEMS.register("plank_spruce", EvolutionItems::wood);
    public static final RegistryObject<Item> plank_willow = ITEMS.register("plank_willow", EvolutionItems::wood);
    //Pickaxes
    public static final RegistryObject<Item> pickaxe_copper = ITEMS.register("pickaxe_copper", () -> pickaxe(EvolutionToolMaterials.COPPER));
    //Chopping Blocks
    public static final RegistryObject<Item> chopping_block_acacia = ITEMS.register("chopping_block_acacia", () -> woodBlock(CHOPPING_BLOCK_ACACIA));
    public static final RegistryObject<Item> chopping_block_aspen = ITEMS.register("chopping_block_aspen", () -> woodBlock(CHOPPING_BLOCK_ASPEN));
    public static final RegistryObject<Item> chopping_block_birch = ITEMS.register("chopping_block_birch", () -> woodBlock(CHOPPING_BLOCK_BIRCH));
    public static final RegistryObject<Item> chopping_block_cedar = ITEMS.register("chopping_block_cedar", () -> woodBlock(CHOPPING_BLOCK_CEDAR));
    public static final RegistryObject<Item> chopping_block_ebony = ITEMS.register("chopping_block_ebony", () -> woodBlock(CHOPPING_BLOCK_EBONY));
    public static final RegistryObject<Item> chopping_block_elm = ITEMS.register("chopping_block_elm", () -> woodBlock(CHOPPING_BLOCK_ELM));
    public static final RegistryObject<Item> chopping_block_eucalyptus = ITEMS.register("chopping_block_eucalyptus",
                                                                                        () -> woodBlock(CHOPPING_BLOCK_EUCALYPTUS));
    public static final RegistryObject<Item> chopping_block_fir = ITEMS.register("chopping_block_fir", () -> woodBlock(CHOPPING_BLOCK_FIR));
    public static final RegistryObject<Item> chopping_block_kapok = ITEMS.register("chopping_block_kapok", () -> woodBlock(CHOPPING_BLOCK_KAPOK));
    public static final RegistryObject<Item> chopping_block_mangrove = ITEMS.register("chopping_block_mangrove",
                                                                                      () -> woodBlock(CHOPPING_BLOCK_MANGROVE));
    public static final RegistryObject<Item> chopping_block_maple = ITEMS.register("chopping_block_maple", () -> woodBlock(CHOPPING_BLOCK_MAPLE));
    public static final RegistryObject<Item> chopping_block_oak = ITEMS.register("chopping_block_oak", () -> woodBlock(CHOPPING_BLOCK_OAK));
    public static final RegistryObject<Item> chopping_block_old_oak = ITEMS.register("chopping_block_old_oak",
                                                                                     () -> woodBlock(CHOPPING_BLOCK_OLD_OAK));
    public static final RegistryObject<Item> chopping_block_palm = ITEMS.register("chopping_block_palm", () -> woodBlock(CHOPPING_BLOCK_PALM));
    public static final RegistryObject<Item> chopping_block_pine = ITEMS.register("chopping_block_pine", () -> woodBlock(CHOPPING_BLOCK_PINE));
    public static final RegistryObject<Item> chopping_block_redwood = ITEMS.register("chopping_block_redwood",
                                                                                     () -> woodBlock(CHOPPING_BLOCK_REDWOOD));
    public static final RegistryObject<Item> chopping_block_spruce = ITEMS.register("chopping_block_spruce", () -> woodBlock(CHOPPING_BLOCK_SPRUCE));
    public static final RegistryObject<Item> chopping_block_willow = ITEMS.register("chopping_block_willow", () -> woodBlock(CHOPPING_BLOCK_WILLOW));
    //Destroy Blocks
    public static final RegistryObject<Item> destroy_3 = ITEMS.register("destroy_3", () -> new ItemBlock(DESTROY_3.get(), new Item.Properties()));
    public static final RegistryObject<Item> destroy_6 = ITEMS.register("destroy_6", () -> new ItemBlock(DESTROY_6.get(), new Item.Properties()));
    public static final RegistryObject<Item> destroy_9 = ITEMS.register("destroy_9", () -> new ItemBlock(DESTROY_9.get(), new Item.Properties()));
    //Molding
    public static final RegistryObject<Item> mold_clay_axe = ITEMS.register("mold_clay_axe", () -> new ItemClayMolded(MOLD_CLAY_AXE));
    public static final RegistryObject<Item> mold_clay_shovel = ITEMS.register("mold_clay_shovel", () -> new ItemClayMolded(MOLD_CLAY_SHOVEL));
    public static final RegistryObject<Item> mold_clay_hoe = ITEMS.register("mold_clay_hoe", () -> new ItemClayMolded(MOLD_CLAY_HOE));
    public static final RegistryObject<Item> mold_clay_hammer = ITEMS.register("mold_clay_hammer", () -> new ItemClayMolded(MOLD_CLAY_HAMMER));
    public static final RegistryObject<Item> mold_clay_pickaxe = ITEMS.register("mold_clay_pickaxe", () -> new ItemClayMolded(MOLD_CLAY_PICKAXE));
    public static final RegistryObject<Item> mold_clay_spear = ITEMS.register("mold_clay_spear", () -> new ItemClayMolded(MOLD_CLAY_SPEAR));
    public static final RegistryObject<Item> mold_clay_sword = ITEMS.register("mold_clay_sword", () -> new ItemClayMolded(MOLD_CLAY_SWORD));
    public static final RegistryObject<Item> mold_clay_guard = ITEMS.register("mold_clay_guard", () -> new ItemClayMolded(MOLD_CLAY_GUARD));
    public static final RegistryObject<Item> mold_clay_prospecting = ITEMS.register("mold_clay_prospecting",
                                                                                    () -> new ItemClayMolded(MOLD_CLAY_PROSPECTING));
    public static final RegistryObject<Item> mold_clay_saw = ITEMS.register("mold_clay_saw", () -> new ItemClayMolded(MOLD_CLAY_SAW));
    public static final RegistryObject<Item> mold_clay_knife = ITEMS.register("mold_clay_knife", () -> new ItemClayMolded(MOLD_CLAY_KNIFE));
    public static final RegistryObject<Item> mold_clay_ingot = ITEMS.register("mold_clay_ingot", () -> new ItemClayMolded(MOLD_CLAY_INGOT));
    public static final RegistryObject<Item> mold_clay_plate = ITEMS.register("mold_clay_plate", () -> new ItemClayMolded(MOLD_CLAY_PLATE));
    public static final RegistryObject<Item> brick_clay = ITEMS.register("brick_clay", () -> new ItemClayMolded(BRICK_CLAY));
    public static final RegistryObject<Item> crucible_clay = ITEMS.register("crucible_clay", () -> new ItemClayMolded(CRUCIBLE_CLAY, true));

    public static final RegistryObject<Item> straw = ITEMS.register("straw", EvolutionItems::item);
    public static final RegistryObject<Item> fire_starter = ITEMS.register("fire_starter", ItemFireStarter::new);
    //Hoe Heads
    public static final RegistryObject<Item> hoe_head_andesite = ITEMS.register("hoe_head_andesite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_basalt = ITEMS.register("hoe_head_basalt", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_chalk = ITEMS.register("hoe_head_chalk", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_chert = ITEMS.register("hoe_head_chert", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_conglomerate = ITEMS.register("hoe_head_conglomerate", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_dacite = ITEMS.register("hoe_head_dacite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_diorite = ITEMS.register("hoe_head_diorite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_dolomite = ITEMS.register("hoe_head_dolomite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_gabbro = ITEMS.register("hoe_head_gabbro", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_gneiss = ITEMS.register("hoe_head_gneiss", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_granite = ITEMS.register("hoe_head_granite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_limestone = ITEMS.register("hoe_head_limestone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_marble = ITEMS.register("hoe_head_marble", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_phyllite = ITEMS.register("hoe_head_phyllite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_quartzite = ITEMS.register("hoe_head_quartzite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_red_sandstone = ITEMS.register("hoe_head_red_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_sandstone = ITEMS.register("hoe_head_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_schist = ITEMS.register("hoe_head_schist", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_shale = ITEMS.register("hoe_head_shale", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> hoe_head_slate = ITEMS.register("hoe_head_slate", EvolutionItems::stoneHeads);
    //Knife blades
    public static final RegistryObject<Item> knife_blade_andesite = ITEMS.register("knife_blade_andesite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_basalt = ITEMS.register("knife_blade_basalt", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_chalk = ITEMS.register("knife_blade_chalk", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_chert = ITEMS.register("knife_blade_chert", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_conglomerate = ITEMS.register("knife_blade_conglomerate", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_dacite = ITEMS.register("knife_blade_dacite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_diorite = ITEMS.register("knife_blade_diorite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_dolomite = ITEMS.register("knife_blade_dolomite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_gabbro = ITEMS.register("knife_blade_gabbro", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_gneiss = ITEMS.register("knife_blade_gneiss", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_granite = ITEMS.register("knife_blade_granite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_limestone = ITEMS.register("knife_blade_limestone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_marble = ITEMS.register("knife_blade_marble", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_phyllite = ITEMS.register("knife_blade_phyllite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_quartzite = ITEMS.register("knife_blade_quartzite", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_red_sandstone = ITEMS.register("knife_blade_red_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_sandstone = ITEMS.register("knife_blade_sandstone", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_schist = ITEMS.register("knife_blade_schist", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_shale = ITEMS.register("knife_blade_shale", EvolutionItems::stoneHeads);
    public static final RegistryObject<Item> knife_blade_slate = ITEMS.register("knife_blade_slate", EvolutionItems::stoneHeads);
    //Rope
    public static final RegistryObject<Item> rope = ITEMS.register("rope", EvolutionItems::item);
    public static final RegistryObject<Item> climbing_stake = ITEMS.register("climbing_stake", () -> itemBlock(CLIMBING_STAKE));
    public static final RegistryObject<Item> climbing_hook = ITEMS.register("climbing_hook", ItemClimbingHook::new);
    //Hammer
    public static final RegistryObject<Item> hammer_andesite = ITEMS.register("hammer_andesite",
                                                                              () -> hammerStone(EvolutionToolMaterials.STONE_ANDESITE));
    public static final RegistryObject<Item> hammer_basalt = ITEMS.register("hammer_basalt", () -> hammerStone(EvolutionToolMaterials.STONE_BASALT));
    public static final RegistryObject<Item> hammer_chalk = ITEMS.register("hammer_chalk", () -> hammerStone(EvolutionToolMaterials.STONE_CHALK));
    public static final RegistryObject<Item> hammer_chert = ITEMS.register("hammer_chert", () -> hammerStone(EvolutionToolMaterials.STONE_CHERT));
    public static final RegistryObject<Item> hammer_conglomerate = ITEMS.register("hammer_conglomerate",
                                                                                  () -> hammerStone(EvolutionToolMaterials.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> hammer_dacite = ITEMS.register("hammer_dacite", () -> hammerStone(EvolutionToolMaterials.STONE_DACITE));
    public static final RegistryObject<Item> hammer_diorite = ITEMS.register("hammer_diorite",
                                                                             () -> hammerStone(EvolutionToolMaterials.STONE_DIORITE));
    public static final RegistryObject<Item> hammer_dolomite = ITEMS.register("hammer_dolomite",
                                                                              () -> hammerStone(EvolutionToolMaterials.STONE_DOLOMITE));
    public static final RegistryObject<Item> hammer_gabbro = ITEMS.register("hammer_gabbro", () -> hammerStone(EvolutionToolMaterials.STONE_GABBRO));
    public static final RegistryObject<Item> hammer_gneiss = ITEMS.register("hammer_gneiss", () -> hammerStone(EvolutionToolMaterials.STONE_GNEISS));
    public static final RegistryObject<Item> hammer_granite = ITEMS.register("hammer_granite",
                                                                             () -> hammerStone(EvolutionToolMaterials.STONE_GRANITE));
    public static final RegistryObject<Item> hammer_limestone = ITEMS.register("hammer_limestone",
                                                                               () -> hammerStone(EvolutionToolMaterials.STONE_LIMESTONE));
    public static final RegistryObject<Item> hammer_marble = ITEMS.register("hammer_marble", () -> hammerStone(EvolutionToolMaterials.STONE_MARBLE));
    public static final RegistryObject<Item> hammer_phyllite = ITEMS.register("hammer_phyllite",
                                                                              () -> hammerStone(EvolutionToolMaterials.STONE_PHYLLITE));
    public static final RegistryObject<Item> hammer_quartzite = ITEMS.register("hammer_quartzite",
                                                                               () -> hammerStone(EvolutionToolMaterials.STONE_QUARTZITE));
    public static final RegistryObject<Item> hammer_red_sandstone = ITEMS.register("hammer_red_sandstone",
                                                                                   () -> hammerStone(EvolutionToolMaterials.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> hammer_sandstone = ITEMS.register("hammer_sandstone",
                                                                               () -> hammerStone(EvolutionToolMaterials.STONE_SANDSTONE));
    public static final RegistryObject<Item> hammer_schist = ITEMS.register("hammer_schist", () -> hammerStone(EvolutionToolMaterials.STONE_SCHIST));
    public static final RegistryObject<Item> hammer_shale = ITEMS.register("hammer_shale", () -> hammerStone(EvolutionToolMaterials.STONE_SHALE));
    public static final RegistryObject<Item> hammer_slate = ITEMS.register("hammer_slate", () -> hammerStone(EvolutionToolMaterials.STONE_SLATE));
    //Stone Bricks
    public static final RegistryObject<Item> stone_bricks_andesite = ITEMS.register("stone_bricks_andesite", () -> itemBlock(STONE_BRICKS_ANDESITE));
    public static final RegistryObject<Item> stone_bricks_basalt = ITEMS.register("stone_bricks_basalt", () -> itemBlock(STONE_BRICKS_BASALT));
    public static final RegistryObject<Item> stone_bricks_chalk = ITEMS.register("stone_bricks_chalk", () -> itemBlock(STONE_BRICKS_CHALK));
    public static final RegistryObject<Item> stone_bricks_chert = ITEMS.register("stone_bricks_chert", () -> itemBlock(STONE_BRICKS_CHERT));
    public static final RegistryObject<Item> stone_bricks_conglomerate = ITEMS.register("stone_bricks_conglomerate",
                                                                                        () -> itemBlock(STONE_BRICKS_CONGLOMERATE));
    public static final RegistryObject<Item> stone_bricks_dacite = ITEMS.register("stone_bricks_dacite", () -> itemBlock(STONE_BRICKS_DACITE));
    public static final RegistryObject<Item> stone_bricks_diorite = ITEMS.register("stone_bricks_diorite", () -> itemBlock(STONE_BRICKS_DIORITE));
    public static final RegistryObject<Item> stone_bricks_dolomite = ITEMS.register("stone_bricks_dolomite", () -> itemBlock(STONE_BRICKS_DOLOMITE));
    public static final RegistryObject<Item> stone_bricks_gabbro = ITEMS.register("stone_bricks_gabbro", () -> itemBlock(STONE_BRICKS_GABBRO));
    public static final RegistryObject<Item> stone_bricks_gneiss = ITEMS.register("stone_bricks_gneiss", () -> itemBlock(STONE_BRICKS_GNEISS));
    public static final RegistryObject<Item> stone_bricks_granite = ITEMS.register("stone_bricks_granite", () -> itemBlock(STONE_BRICKS_GRANITE));
    public static final RegistryObject<Item> stone_bricks_limestone = ITEMS.register("stone_bricks_limestone",
                                                                                     () -> itemBlock(STONE_BRICKS_LIMESTONE));
    public static final RegistryObject<Item> stone_bricks_marble = ITEMS.register("stone_bricks_marble", () -> itemBlock(STONE_BRICKS_MARBLE));
    public static final RegistryObject<Item> stone_bricks_phyllite = ITEMS.register("stone_bricks_phyllite", () -> itemBlock(STONE_BRICKS_PHYLLITE));
    public static final RegistryObject<Item> stone_bricks_quartzite = ITEMS.register("stone_bricks_quartzite",
                                                                                     () -> itemBlock(STONE_BRICKS_QUARTZITE));
    public static final RegistryObject<Item> stone_bricks_red_sandstone = ITEMS.register("stone_bricks_red_sandstone",
                                                                                         () -> itemBlock(STONE_BRICKS_RED_SANDSTONE));
    public static final RegistryObject<Item> stone_bricks_sandstone = ITEMS.register("stone_bricks_sandstone",
                                                                                     () -> itemBlock(STONE_BRICKS_SANDSTONE));
    public static final RegistryObject<Item> stone_bricks_schist = ITEMS.register("stone_bricks_schist", () -> itemBlock(STONE_BRICKS_SCHIST));
    public static final RegistryObject<Item> stone_bricks_shale = ITEMS.register("stone_bricks_shale", () -> itemBlock(STONE_BRICKS_SHALE));
    public static final RegistryObject<Item> stone_bricks_slate = ITEMS.register("stone_bricks_slate", () -> itemBlock(STONE_BRICKS_SLATE));
    //buckets
    public static final RegistryObject<Item> bucket_ceramic_empty = ITEMS.register("bucket_ceramic_empty", () -> bucketCeramic(() -> Fluids.EMPTY));
    public static final RegistryObject<Item> bucket_ceramic_fresh_water = ITEMS.register("bucket_ceramic_fresh_water",
                                                                                         () -> bucketCeramic(EvolutionFluids.FRESH_WATER));
    public static final RegistryObject<Item> bucket_ceramic_salt_water = ITEMS.register("bucket_ceramic_salt_water",
                                                                                        () -> bucketCeramic(EvolutionFluids.SALT_WATER));
    public static final RegistryObject<Item> bucket_creative_empty = ITEMS.register("bucket_creative_empty",
                                                                                    () -> bucketCreative(() -> Fluids.EMPTY));
    public static final RegistryObject<Item> bucket_creative_fresh_water = ITEMS.register("bucket_creative_fresh_water",
                                                                                          () -> bucketCreative(EvolutionFluids.FRESH_WATER));
    public static final RegistryObject<Item> bucket_creative_salt_water = ITEMS.register("bucket_creative_salt_water",
                                                                                         () -> bucketCreative(EvolutionFluids.SALT_WATER));
    public static final RegistryObject<Item> sword_dev = ITEMS.register("sword_dev",
                                                                        () -> new ItemSword(MathHelper.attackSpeed(0.7f),
                                                                                            EvolutionToolMaterials.COPPER,
                                                                                            propMisc(),
                                                                                            EvolutionToolMaterials.COPPER.getSwordMass()));
    public static final RegistryObject<Item> shield_dev = ITEMS.register("shield_dev", () -> new ItemShield(propMisc().maxDamage(400)));

    private EvolutionItems() {
    }

    private static Item axeStone(EvolutionToolMaterials tier) {
        return new ItemAxe(tier, MathHelper.attackSpeed(1.25F), propStoneTool(), tier.getAxeMass());
    }

    private static Item bucketCeramic(Supplier<? extends Fluid> fluid) {
        return new ItemBucketCeramic(fluid, propLiquid().maxStackSize(fluid.get() == Fluids.EMPTY ? 16 : 1));
    }

    private static Item bucketCreative(Supplier<? extends Fluid> fluid) {
        return new ItemBucketCreative(fluid, propLiquid().maxStackSize(fluid.get() == Fluids.EMPTY ? 16 : 1));
    }

    private static Item hammerStone(EvolutionToolMaterials tier) {
        return new ItemHammer(tier, MathHelper.attackSpeed(1.25F), propStoneTool(), tier.getHammerMass());
    }

    private static Item item() {
        return new ItemEv(propMisc());
    }

    private static Item itemBlock(RegistryObject<Block> block) {
        return new ItemBlock(block.get(), propMisc());
    }

    private static ItemLog itemLog(WoodVariant variant, RegistryObject<BlockLog> block) {
        return new ItemLog(variant, block.get(), propTreesAndWood().maxStackSize(16));
    }

    private static Item itemRock(RegistryObject<Block> block, RockVariant name) {
        return new ItemRock(block.get(), propMisc(), name);
    }

    private static Item javelin(EvolutionToolMaterials tier) {
        return new ItemJavelin(MathHelper.attackSpeed(1.15f),
                               tier,
                               propStoneTool().maxDamage(tier.getMaxUses()).setTEISR(() -> RenderStackItemJavelin::new),
                               tier.getAttackDamage(),
                               tier.getJavelinMass(),
                               tier.getName());
    }

    private static Item pickaxe(EvolutionToolMaterials tier) {
        return new ItemPickaxe(tier, MathHelper.attackSpeed(0.85F), propMisc(), tier.getPickaxeMass());
    }

    public static Item.Properties propEgg() {
        return new Item.Properties().group(EvolutionCreativeTabs.EGGS);
    }

    public static Item.Properties propLiquid() {
        return new Item.Properties().group(EvolutionCreativeTabs.LIQUIDS);
    }

    public static Item.Properties propMisc() {
        return new Item.Properties().group(EvolutionCreativeTabs.MISC);
    }

    private static Item.Properties propStoneTool() {
        return new Item.Properties().group(EvolutionCreativeTabs.STONE_TOOLS);
    }

    private static Item.Properties propTreesAndWood() {
        return new Item.Properties().group(EvolutionCreativeTabs.TREES_AND_WOOD);
    }

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static Item shovelStone(EvolutionToolMaterials tier) {
        return new ItemShovel(tier, MathHelper.attackSpeed(1.0F), propStoneTool(), tier.getShovelMass());
    }

    private static Item stoneHeads() {
        return new ItemEv(propStoneTool().maxStackSize(16));
    }

    private static Item wood() {
        return new ItemEv(propTreesAndWood());
    }

    private static Item woodBlock(RegistryObject<? extends Block> block) {
        return new ItemBlock(block.get(), propTreesAndWood());
    }
}
