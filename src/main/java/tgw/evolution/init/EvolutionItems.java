package tgw.evolution.init;

import com.google.common.collect.Maps;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.items.*;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.items.modular.part.*;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.MathHelper;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "ObjectAllocationInLoop"})
public final class EvolutionItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Evolution.MODID);
    //Dev
    public static final RegistryObject<Item> DEBUG_ITEM = ITEMS.register("debug_item", () -> item(propDev()));
    public static final RegistryObject<Item> DEV_FOOD = ITEMS.register("dev_food",
                                                                       () -> new ItemFood(propDev(), new IConsumable.FoodProperties(250)));
    public static final RegistryObject<Item> DEV_DRINK = ITEMS.register("dev_drink",
                                                                        () -> new ItemDrink(propDev(), new IConsumable.DrinkProperties(250)));
    public static final RegistryObject<Item> PLACEHOLDER_BLOCK = ITEMS.register("placeholder_block",
                                                                                () -> itemBlock(EvolutionBlocks.PLACEHOLDER_BLOCK, propDev()));
    public static final RegistryObject<Item> CRICKET = ITEMS.register("cricket", () -> new ItemCricket(propDev()));
    public static final RegistryObject<Item> NITROGEN_SETTER = ITEMS.register("nitrogen_setter",
                                                                              () -> new ItemChunkStorageSetter(propDev(), EnumStorage.NITROGEN));
    public static final RegistryObject<Item> NITROGEN_GETTER = ITEMS.register("nitrogen_getter",
                                                                              () -> new ItemChunkStorageGetter(propDev(), EnumStorage.NITROGEN));
    public static final RegistryObject<Item> PHOSPHORUS_SETTER = ITEMS.register("phosphorus_setter",
                                                                                () -> new ItemChunkStorageSetter(propDev(), EnumStorage.PHOSPHORUS));
    public static final RegistryObject<Item> PHOSPHORUS_GETTER = ITEMS.register("phosphorus_getter",
                                                                                () -> new ItemChunkStorageGetter(propDev(), EnumStorage.PHOSPHORUS));
    public static final RegistryObject<Item> POTASSIUM_SETTER = ITEMS.register("potassium_setter",
                                                                               () -> new ItemChunkStorageSetter(propDev(), EnumStorage.POTASSIUM));
    public static final RegistryObject<Item> POTASSIUM_GETTER = ITEMS.register("potassium_getter",
                                                                               () -> new ItemChunkStorageGetter(propDev(), EnumStorage.POTASSIUM));
    public static final RegistryObject<Item> WATER_SETTER = ITEMS.register("water_setter",
                                                                           () -> new ItemChunkStorageSetter(propDev(), EnumStorage.WATER));
    public static final RegistryObject<Item> WATER_GETTER = ITEMS.register("water_getter",
                                                                           () -> new ItemChunkStorageGetter(propDev(), EnumStorage.WATER));
    public static final RegistryObject<Item> CARBON_DIOXIDE_SETTER = ITEMS.register("carbon_dioxide_setter",
                                                                                    () -> new ItemChunkStorageSetter(propDev(),
                                                                                                                     EnumStorage.CARBON_DIOXIDE));
    public static final RegistryObject<Item> CARBON_DIOXIDE_GETTER = ITEMS.register("carbon_dioxide_getter",
                                                                                    () -> new ItemChunkStorageGetter(propDev(),
                                                                                                                     EnumStorage.CARBON_DIOXIDE));
    public static final RegistryObject<Item> OXYGEN_SETTER = ITEMS.register("oxygen_setter",
                                                                            () -> new ItemChunkStorageSetter(propDev(), EnumStorage.OXYGEN));
    public static final RegistryObject<Item> OXYGEN_GETTER = ITEMS.register("oxygen_getter",
                                                                            () -> new ItemChunkStorageGetter(propDev(), EnumStorage.OXYGEN));
    public static final RegistryObject<Item> GAS_NITROGEN_SETTER = ITEMS.register("gas_nitrogen_setter", () -> new ItemChunkStorageSetter(propDev(),
                                                                                                                                          EnumStorage.GAS_NITROGEN));
    public static final RegistryObject<Item> GAS_NITROGEN_GETTER = ITEMS.register("gas_nitrogen_getter", () -> new ItemChunkStorageGetter(propDev(),
                                                                                                                                          EnumStorage.GAS_NITROGEN));
    public static final RegistryObject<Item> ORGANIC_SETTER = ITEMS.register("organic_setter",
                                                                             () -> new ItemChunkStorageSetter(propDev(), EnumStorage.ORGANIC));
    public static final RegistryObject<Item> ORGANIC_GETTER = ITEMS.register("organic_getter",
                                                                             () -> new ItemChunkStorageGetter(propDev(), EnumStorage.ORGANIC));
    public static final RegistryObject<Item> CLOCK = ITEMS.register("clock", () -> new ItemClock(propDev()));
    public static final RegistryObject<Item> SEXTANT = ITEMS.register("sextant", () -> new ItemSextant(propDev()));
    public static final RegistryObject<Item> PUZZLE = ITEMS.register("puzzle", () -> itemBlock(EvolutionBlocks.PUZZLE, propDev()));
    public static final RegistryObject<Item> SCHEMATIC_BLOCK = ITEMS.register("schematic_block",
                                                                              () -> itemBlock(EvolutionBlocks.SCHEMATIC_BLOCK, propDev()));
    //Parts
    public static final RegistryObject<Item> BLADE_PART = ITEMS.register("blade_part", () -> new ItemBladePart(propDev()));
    public static final RegistryObject<Item> GUARD_PART = ITEMS.register("guard_part", () -> new ItemGuardPart(propDev()));
    public static final RegistryObject<Item> HALFHEAD_PART = ITEMS.register("halfhead_part", () -> new ItemHalfHeadPart(propDev()));
    public static final RegistryObject<Item> HANDLE_PART = ITEMS.register("handle_part", () -> new ItemHandlePart(propDev()));
    public static final RegistryObject<Item> HEAD_PART = ITEMS.register("head_part", () -> new ItemHeadPart(propDev()));
    public static final RegistryObject<Item> HILT_PART = ITEMS.register("hilt_part", () -> new ItemHiltPart(propDev()));
    public static final RegistryObject<Item> POLE_PART = ITEMS.register("pole_part", () -> new ItemPolePart(propDev()));
    public static final RegistryObject<Item> POMMEL_PART = ITEMS.register("pommel_part", () -> new ItemPommelPart(propDev()));

    public static final RegistryObject<Item> MODULAR_TOOL = ITEMS.register("modular_tool", () -> new ItemModularTool(propDev()));
    //Stick
    public static final RegistryObject<Item> STICK = ITEMS.register("stick", () -> new ItemStick(EvolutionBlocks.STICK.get(), propTreesAndWood()));
    //Stone
    public static final Map<RockVariant, RegistryObject<Item>> ALL_STONE = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_",
                                                                                e -> () -> itemBlock(e.getStone()));
    //Cobblestone
    public static final Map<RockVariant, RegistryObject<Item>> ALL_COBBLE = make(RockVariant.class, RockVariant.VALUES_STONE, "cobble_",
                                                                                 e -> () -> itemBlock(e.getCobble()));
    //Rocks
    public static final Map<RockVariant, RegistryObject<Item>> ALL_ROCK = make(RockVariant.class, RockVariant.VALUES_STONE, "rock_",
                                                                               e -> () -> new ItemRock(e.getRock(), propMisc(), e));
    //Polished Stones
    public static final Map<RockVariant, RegistryObject<Item>> ALL_POLISHED_STONE = make(RockVariant.class, RockVariant.VALUES_STONE,
                                                                                         "polished_stone_",
                                                                                         e -> () -> itemBlock(e.getPolishedStone()));
    //Sand
    public static final Map<RockVariant, RegistryObject<Item>> ALL_SAND = make(RockVariant.class, RockVariant.VALUES_STONE, "sand_",
                                                                               e -> () -> itemBlock(e.getSand()));
    //Dirt
    public static final Map<RockVariant, RegistryObject<Item>> ALL_DIRT = make(RockVariant.class, RockVariant.VALUES_STONE, "dirt_",
                                                                               e -> () -> itemBlock(e.getDirt()));
    //Gravel
    public static final Map<RockVariant, RegistryObject<Item>> ALL_GRAVEL = make(RockVariant.class, RockVariant.VALUES_STONE, "gravel_",
                                                                                 e -> () -> itemBlock(e.getGravel()));
    //Grass
    public static final Map<RockVariant, RegistryObject<Item>> ALL_GRASS = make(RockVariant.class, RockVariant.VALUES, "grass_",
                                                                                e -> () -> itemBlock(e.getGrass()));
    //Clay
    public static final RegistryObject<Item> CLAY = ITEMS.register("clay", () -> itemBlock(EvolutionBlocks.CLAY.get()));
    public static final RegistryObject<Item> CLAYBALL = ITEMS.register("clayball", ItemClay::new);
    //Peat
    public static final RegistryObject<Item> PEAT = ITEMS.register("peat", () -> itemBlock(EvolutionBlocks.PEAT.get()));
    //Dry Grass
    public static final Map<RockVariant, RegistryObject<Item>> ALL_DRY_GRASS = make(RockVariant.class, RockVariant.VALUES_STONE, "dry_grass_",
                                                                                    e -> () -> itemBlock(e.getDryGrass()));
    //Log
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_LOG = make(WoodVariant.class, WoodVariant.VALUES, "log_",
                                                                              e -> () -> new ItemLog(e, e.getLog(), propTreesAndWood()));
    //Leaves
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_LEAVES = make(WoodVariant.class, WoodVariant.VALUES, "leaves_",
                                                                                 e -> () -> woodBlock(e.getLeaves()));
    //Sapling
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_SAPLING = make(WoodVariant.class, WoodVariant.VALUES, "sapling_",
                                                                                  e -> () -> woodBlock(e.getSapling()));
    //Firewood
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_FIREWOOD = make(WoodVariant.class, WoodVariant.VALUES, "firewood_",
                                                                                   e -> () -> new ItemFirewood(e));

    //Vegetation
    public static final RegistryObject<Item> GRASS = ITEMS.register("grass", () -> itemBlock(EvolutionBlocks.GRASS.get()));
    public static final RegistryObject<Item> TALLGRASS = ITEMS.register("tallgrass", () -> itemBlock(EvolutionBlocks.TALLGRASS.get()));
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
    public static final RegistryObject<Item> axe_andesite = ITEMS.register("axe_andesite", () -> axeStone(ItemMaterial.STONE_ANDESITE));
    public static final RegistryObject<Item> axe_basalt = ITEMS.register("axe_basalt", () -> axeStone(ItemMaterial.STONE_BASALT));
    public static final RegistryObject<Item> axe_chalk = ITEMS.register("axe_chalk", () -> axeStone(ItemMaterial.STONE_CHALK));
    public static final RegistryObject<Item> axe_chert = ITEMS.register("axe_chert", () -> axeStone(ItemMaterial.STONE_CHERT));
    public static final RegistryObject<Item> axe_conglomerate = ITEMS.register("axe_conglomerate", () -> axeStone(ItemMaterial.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> axe_dacite = ITEMS.register("axe_dacite", () -> axeStone(ItemMaterial.STONE_DACITE));
    public static final RegistryObject<Item> axe_diorite = ITEMS.register("axe_diorite", () -> axeStone(ItemMaterial.STONE_DIORITE));
    public static final RegistryObject<Item> axe_dolomite = ITEMS.register("axe_dolomite", () -> axeStone(ItemMaterial.STONE_DOLOMITE));
    public static final RegistryObject<Item> axe_gabbro = ITEMS.register("axe_gabbro", () -> axeStone(ItemMaterial.STONE_GABBRO));
    public static final RegistryObject<Item> axe_gneiss = ITEMS.register("axe_gneiss", () -> axeStone(ItemMaterial.STONE_GNEISS));
    public static final RegistryObject<Item> axe_granite = ITEMS.register("axe_granite", () -> axeStone(ItemMaterial.STONE_GRANITE));
    public static final RegistryObject<Item> axe_limestone = ITEMS.register("axe_limestone", () -> axeStone(ItemMaterial.STONE_LIMESTONE));
    public static final RegistryObject<Item> axe_marble = ITEMS.register("axe_marble", () -> axeStone(ItemMaterial.STONE_MARBLE));
    public static final RegistryObject<Item> axe_phyllite = ITEMS.register("axe_phyllite", () -> axeStone(ItemMaterial.STONE_PHYLLITE));
    public static final RegistryObject<Item> axe_quartzite = ITEMS.register("axe_quartzite", () -> axeStone(ItemMaterial.STONE_QUARTZITE));
    public static final RegistryObject<Item> axe_red_sandstone = ITEMS.register("axe_red_sandstone",
                                                                                () -> axeStone(ItemMaterial.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> axe_sandstone = ITEMS.register("axe_sandstone", () -> axeStone(ItemMaterial.STONE_SANDSTONE));
    public static final RegistryObject<Item> axe_schist = ITEMS.register("axe_schist", () -> axeStone(ItemMaterial.STONE_SCHIST));
    public static final RegistryObject<Item> axe_shale = ITEMS.register("axe_shale", () -> axeStone(ItemMaterial.STONE_SHALE));
    public static final RegistryObject<Item> axe_slate = ITEMS.register("axe_slate", () -> axeStone(ItemMaterial.STONE_SLATE));
    //Shovels
    public static final RegistryObject<Item> shovel_andesite = ITEMS.register("shovel_andesite", () -> shovelStone(ItemMaterial.STONE_ANDESITE));
    public static final RegistryObject<Item> shovel_basalt = ITEMS.register("shovel_basalt", () -> shovelStone(ItemMaterial.STONE_BASALT));
    public static final RegistryObject<Item> shovel_chalk = ITEMS.register("shovel_chalk", () -> shovelStone(ItemMaterial.STONE_CHALK));
    public static final RegistryObject<Item> shovel_chert = ITEMS.register("shovel_chert", () -> shovelStone(ItemMaterial.STONE_CHERT));
    public static final RegistryObject<Item> shovel_conglomerate = ITEMS.register("shovel_conglomerate",
                                                                                  () -> shovelStone(ItemMaterial.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> shovel_dacite = ITEMS.register("shovel_dacite", () -> shovelStone(ItemMaterial.STONE_DACITE));
    public static final RegistryObject<Item> shovel_diorite = ITEMS.register("shovel_diorite", () -> shovelStone(ItemMaterial.STONE_DIORITE));
    public static final RegistryObject<Item> shovel_dolomite = ITEMS.register("shovel_dolomite", () -> shovelStone(ItemMaterial.STONE_DOLOMITE));
    public static final RegistryObject<Item> shovel_gabbro = ITEMS.register("shovel_gabbro", () -> shovelStone(ItemMaterial.STONE_GABBRO));
    public static final RegistryObject<Item> shovel_gneiss = ITEMS.register("shovel_gneiss", () -> shovelStone(ItemMaterial.STONE_GNEISS));
    public static final RegistryObject<Item> shovel_granite = ITEMS.register("shovel_granite", () -> shovelStone(ItemMaterial.STONE_GRANITE));
    public static final RegistryObject<Item> shovel_limestone = ITEMS.register("shovel_limestone", () -> shovelStone(ItemMaterial.STONE_LIMESTONE));
    public static final RegistryObject<Item> shovel_marble = ITEMS.register("shovel_marble", () -> shovelStone(ItemMaterial.STONE_MARBLE));
    public static final RegistryObject<Item> shovel_phyllite = ITEMS.register("shovel_phyllite", () -> shovelStone(ItemMaterial.STONE_PHYLLITE));
    public static final RegistryObject<Item> shovel_quartzite = ITEMS.register("shovel_quartzite", () -> shovelStone(ItemMaterial.STONE_QUARTZITE));
    public static final RegistryObject<Item> shovel_red_sandstone = ITEMS.register("shovel_red_sandstone",
                                                                                   () -> shovelStone(ItemMaterial.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> shovel_sandstone = ITEMS.register("shovel_sandstone", () -> shovelStone(ItemMaterial.STONE_SANDSTONE));
    public static final RegistryObject<Item> shovel_schist = ITEMS.register("shovel_schist", () -> shovelStone(ItemMaterial.STONE_SCHIST));
    public static final RegistryObject<Item> shovel_shale = ITEMS.register("shovel_shale", () -> shovelStone(ItemMaterial.STONE_SHALE));
    public static final RegistryObject<Item> shovel_slate = ITEMS.register("shovel_slate", () -> shovelStone(ItemMaterial.STONE_SLATE));
    //Planks
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_PLANKS = make(WoodVariant.class, WoodVariant.VALUES, "planks_",
                                                                                 e -> () -> woodBlock(e.getPlanks()));
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
    public static final RegistryObject<Item> javelin_andesite = ITEMS.register("javelin_andesite", () -> javelin(ItemMaterial.STONE_ANDESITE));
    public static final RegistryObject<Item> javelin_basalt = ITEMS.register("javelin_basalt", () -> javelin(ItemMaterial.STONE_BASALT));
    public static final RegistryObject<Item> javelin_chalk = ITEMS.register("javelin_chalk", () -> javelin(ItemMaterial.STONE_CHALK));
    public static final RegistryObject<Item> javelin_chert = ITEMS.register("javelin_chert", () -> javelin(ItemMaterial.STONE_CHERT));
    public static final RegistryObject<Item> javelin_conglomerate = ITEMS.register("javelin_conglomerate",
                                                                                   () -> javelin(ItemMaterial.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> javelin_dacite = ITEMS.register("javelin_dacite", () -> javelin(ItemMaterial.STONE_DACITE));
    public static final RegistryObject<Item> javelin_diorite = ITEMS.register("javelin_diorite", () -> javelin(ItemMaterial.STONE_DIORITE));
    public static final RegistryObject<Item> javelin_dolomite = ITEMS.register("javelin_dolomite", () -> javelin(ItemMaterial.STONE_DOLOMITE));
    public static final RegistryObject<Item> javelin_gabbro = ITEMS.register("javelin_gabbro", () -> javelin(ItemMaterial.STONE_GABBRO));
    public static final RegistryObject<Item> javelin_gneiss = ITEMS.register("javelin_gneiss", () -> javelin(ItemMaterial.STONE_GNEISS));
    public static final RegistryObject<Item> javelin_granite = ITEMS.register("javelin_granite", () -> javelin(ItemMaterial.STONE_GRANITE));
    public static final RegistryObject<Item> javelin_limestone = ITEMS.register("javelin_limestone", () -> javelin(ItemMaterial.STONE_LIMESTONE));
    public static final RegistryObject<Item> javelin_marble = ITEMS.register("javelin_marble", () -> javelin(ItemMaterial.STONE_MARBLE));
    public static final RegistryObject<Item> javelin_phyllite = ITEMS.register("javelin_phyllite", () -> javelin(ItemMaterial.STONE_PHYLLITE));
    public static final RegistryObject<Item> javelin_quartzite = ITEMS.register("javelin_quartzite", () -> javelin(ItemMaterial.STONE_QUARTZITE));
    public static final RegistryObject<Item> javelin_red_sandstone = ITEMS.register("javelin_red_sandstone",
                                                                                    () -> javelin(ItemMaterial.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> javelin_sandstone = ITEMS.register("javelin_sandstone", () -> javelin(ItemMaterial.STONE_SANDSTONE));
    public static final RegistryObject<Item> javelin_schist = ITEMS.register("javelin_schist", () -> javelin(ItemMaterial.STONE_SCHIST));
    public static final RegistryObject<Item> javelin_shale = ITEMS.register("javelin_shale", () -> javelin(ItemMaterial.STONE_SHALE));
    public static final RegistryObject<Item> javelin_slate = ITEMS.register("javelin_slate", () -> javelin(ItemMaterial.STONE_SLATE));
    //Metal Blocks
    public static final RegistryObject<Item> BLOCK_METAL_COPPER = ITEMS.register("block_metal_copper",
                                                                                 () -> itemBlock(EvolutionBlocks.BLOCK_METAL_COPPER, propMetal()));
    public static final RegistryObject<Item> BLOCK_METAL_COPPER_EXPOSED = ITEMS.register("block_metal_copper_exposed",
                                                                                         () -> itemBlock(EvolutionBlocks.BLOCK_METAL_COPPER_EXP,
                                                                                                         propMetal()));
    public static final RegistryObject<Item> BLOCK_METAL_COPPER_WEATHERED = ITEMS.register("block_metal_copper_weathered",
                                                                                           () -> itemBlock(EvolutionBlocks.BLOCK_METAL_COPPER_WEAT,
                                                                                                           propMetal()));
    public static final RegistryObject<Item> BLOCK_METAL_COPPER_OXIDIZED = ITEMS.register("block_metal_copper_oxidized",
                                                                                          () -> itemBlock(EvolutionBlocks.BLOCK_METAL_COPPER_OXID,
                                                                                                          propMetal()));
    //Metal Ingots
    public static final RegistryObject<Item> INGOT_COPPER = ITEMS.register("ingot_copper", () -> new ItemIngot(propMetal()));
    //Metal Nuggets
    public static final RegistryObject<Item> NUGGET_COPPER = ITEMS.register("nugget_copper", () -> item(propMisc()));
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
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_PLANK = make(WoodVariant.class, WoodVariant.VALUES, "plank_",
                                                                                e -> EvolutionItems::wood);
    //Pickaxes
    public static final RegistryObject<Item> pickaxe_copper = ITEMS.register("pickaxe_copper", () -> pickaxe(ItemMaterial.COPPER));
    //Chopping Blocks
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_CHOPPING_BLOCK = make(WoodVariant.class, WoodVariant.VALUES, "chopping_block_",
                                                                                         e -> () -> woodBlock(e.getChoppingBlock()));
    //Destroy Blocks
    public static final RegistryObject<Item> DESTROY_3 = ITEMS.register("destroy_3",
                                                                        () -> new ItemBlock(EvolutionBlocks.DESTROY_3.get(), new Item.Properties()));
    public static final RegistryObject<Item> DESTROY_6 = ITEMS.register("destroy_6",
                                                                        () -> new ItemBlock(EvolutionBlocks.DESTROY_6.get(), new Item.Properties()));
    public static final RegistryObject<Item> DESTROY_9 = ITEMS.register("destroy_9",
                                                                        () -> new ItemBlock(EvolutionBlocks.DESTROY_9.get(), new Item.Properties()));
    //Molding
    public static final RegistryObject<Item> mold_clay_axe = ITEMS.register("mold_clay_axe", () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_AXE));
    public static final RegistryObject<Item> mold_clay_shovel = ITEMS.register("mold_clay_shovel",
                                                                               () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SHOVEL));
    public static final RegistryObject<Item> mold_clay_hoe = ITEMS.register("mold_clay_hoe", () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_HOE));
    public static final RegistryObject<Item> mold_clay_hammer = ITEMS.register("mold_clay_hammer",
                                                                               () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_HAMMER));
    public static final RegistryObject<Item> mold_clay_pickaxe = ITEMS.register("mold_clay_pickaxe",
                                                                                () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_PICKAXE));
    public static final RegistryObject<Item> mold_clay_spear = ITEMS.register("mold_clay_spear",
                                                                              () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SPEAR));
    public static final RegistryObject<Item> mold_clay_sword = ITEMS.register("mold_clay_sword",
                                                                              () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SWORD));
    public static final RegistryObject<Item> mold_clay_guard = ITEMS.register("mold_clay_guard",
                                                                              () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_GUARD));
    public static final RegistryObject<Item> mold_clay_prospecting = ITEMS.register("mold_clay_prospecting",
                                                                                    () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_PROSPECTING));
    public static final RegistryObject<Item> mold_clay_saw = ITEMS.register("mold_clay_saw", () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SAW));
    public static final RegistryObject<Item> mold_clay_knife = ITEMS.register("mold_clay_knife",
                                                                              () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_KNIFE));
    public static final RegistryObject<Item> mold_clay_ingot = ITEMS.register("mold_clay_ingot",
                                                                              () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_INGOT));
    public static final RegistryObject<Item> mold_clay_plate = ITEMS.register("mold_clay_plate",
                                                                              () -> new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_PLATE));
    public static final RegistryObject<Item> brick_clay = ITEMS.register("brick_clay", () -> new ItemClayMolded(EvolutionBlocks.BRICK_CLAY));
    public static final RegistryObject<Item> crucible_clay = ITEMS.register("crucible_clay",
                                                                            () -> new ItemClayMolded(EvolutionBlocks.CRUCIBLE_CLAY, true));
    public static final RegistryObject<Item> straw = ITEMS.register("straw", () -> item(propMisc()));
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
    public static final RegistryObject<Item> rope = ITEMS.register("rope", () -> item(propMisc()));
    public static final RegistryObject<Item> climbing_stake = ITEMS.register("climbing_stake", () -> itemBlock(EvolutionBlocks.CLIMBING_STAKE.get()));
    public static final RegistryObject<Item> climbing_hook = ITEMS.register("climbing_hook", ItemClimbingHook::new);
    //Hammer
    public static final RegistryObject<Item> hammer_andesite = ITEMS.register("hammer_andesite", () -> hammerStone(ItemMaterial.STONE_ANDESITE));
    public static final RegistryObject<Item> hammer_basalt = ITEMS.register("hammer_basalt", () -> hammerStone(ItemMaterial.STONE_BASALT));
    public static final RegistryObject<Item> hammer_chalk = ITEMS.register("hammer_chalk", () -> hammerStone(ItemMaterial.STONE_CHALK));
    public static final RegistryObject<Item> hammer_chert = ITEMS.register("hammer_chert", () -> hammerStone(ItemMaterial.STONE_CHERT));
    public static final RegistryObject<Item> hammer_conglomerate = ITEMS.register("hammer_conglomerate",
                                                                                  () -> hammerStone(ItemMaterial.STONE_CONGLOMERATE));
    public static final RegistryObject<Item> hammer_dacite = ITEMS.register("hammer_dacite", () -> hammerStone(ItemMaterial.STONE_DACITE));
    public static final RegistryObject<Item> hammer_diorite = ITEMS.register("hammer_diorite", () -> hammerStone(ItemMaterial.STONE_DIORITE));
    public static final RegistryObject<Item> hammer_dolomite = ITEMS.register("hammer_dolomite", () -> hammerStone(ItemMaterial.STONE_DOLOMITE));
    public static final RegistryObject<Item> hammer_gabbro = ITEMS.register("hammer_gabbro", () -> hammerStone(ItemMaterial.STONE_GABBRO));
    public static final RegistryObject<Item> hammer_gneiss = ITEMS.register("hammer_gneiss", () -> hammerStone(ItemMaterial.STONE_GNEISS));
    public static final RegistryObject<Item> hammer_granite = ITEMS.register("hammer_granite", () -> hammerStone(ItemMaterial.STONE_GRANITE));
    public static final RegistryObject<Item> hammer_limestone = ITEMS.register("hammer_limestone", () -> hammerStone(ItemMaterial.STONE_LIMESTONE));
    public static final RegistryObject<Item> hammer_marble = ITEMS.register("hammer_marble", () -> hammerStone(ItemMaterial.STONE_MARBLE));
    public static final RegistryObject<Item> hammer_phyllite = ITEMS.register("hammer_phyllite", () -> hammerStone(ItemMaterial.STONE_PHYLLITE));
    public static final RegistryObject<Item> hammer_quartzite = ITEMS.register("hammer_quartzite", () -> hammerStone(ItemMaterial.STONE_QUARTZITE));
    public static final RegistryObject<Item> hammer_red_sandstone = ITEMS.register("hammer_red_sandstone",
                                                                                   () -> hammerStone(ItemMaterial.STONE_RED_SANDSTONE));
    public static final RegistryObject<Item> hammer_sandstone = ITEMS.register("hammer_sandstone", () -> hammerStone(ItemMaterial.STONE_SANDSTONE));
    public static final RegistryObject<Item> hammer_schist = ITEMS.register("hammer_schist", () -> hammerStone(ItemMaterial.STONE_SCHIST));
    public static final RegistryObject<Item> hammer_shale = ITEMS.register("hammer_shale", () -> hammerStone(ItemMaterial.STONE_SHALE));
    public static final RegistryObject<Item> hammer_slate = ITEMS.register("hammer_slate", () -> hammerStone(ItemMaterial.STONE_SLATE));
    //Stone Bricks
    public static final Map<RockVariant, RegistryObject<Item>> ALL_STONE_BRICKS = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_bricks_",
                                                                                       e -> () -> itemBlock(e.getStoneBricks()));
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
                                                                        () -> new ItemSword(MathHelper.attackSpeed(0.7f), ItemMaterial.COPPER,
                                                                                            propDev(), ItemMaterial.COPPER.getSwordMass()));
    public static final RegistryObject<Item> shield_dev = ITEMS.register("shield_dev", () -> new ItemShield(propDev().durability(400)));

    private EvolutionItems() {
    }

    private static Item axeStone(ItemMaterial tier) {
        return new ItemAxe(tier, MathHelper.attackSpeed(1.25F), propPartTool(), tier.getAxeMass());
    }

    private static Item bucketCeramic(Supplier<? extends Fluid> fluid) {
        if (fluid instanceof RegistryObject) {
            return new ItemBucketCeramic(fluid, propLiquid().stacksTo(1));
        }
        return new ItemBucketCeramic(fluid, propLiquid().stacksTo(fluid.get() == Fluids.EMPTY ? 16 : 1));
    }

    private static Item bucketCreative(Supplier<? extends Fluid> fluid) {
        if (fluid instanceof RegistryObject) {
            return new ItemBucketCreative(fluid, propLiquid().stacksTo(1));
        }
        return new ItemBucketCreative(fluid, propLiquid().stacksTo(fluid.get() == Fluids.EMPTY ? 16 : 1));
    }

    private static Item hammerStone(ItemMaterial tier) {
        return new ItemHammer(tier, MathHelper.attackSpeed(1.25F), propPartTool(), tier.getHammerMass());
    }

    private static Item item(Item.Properties prop) {
        return new ItemEv(prop);
    }

    private static Item itemBlock(Block block) {
        return new ItemBlock(block, propMisc());
    }

    private static Item itemBlock(RegistryObject<Block> block, Item.Properties prop) {
        return new ItemBlock(block.get(), prop);
    }

    private static Item javelin(ItemMaterial tier) {
        return new ItemJavelin(MathHelper.attackSpeed(1.15f), tier, propPartTool().durability(/*tier.getUses()*/1),
                /*tier.getAttackDamageBonus()*/0, tier.getJavelinMass(), tier.getName());
    }

    private static <E extends Enum<E> & IVariant> Map<E, RegistryObject<Item>> make(Class<E> clazz,
                                                                                    E[] values,
                                                                                    String name,
                                                                                    Function<E, Supplier<Item>> item) {
        Map<E, RegistryObject<Item>> map = new EnumMap<>(clazz);
        for (E e : values) {
            map.put(e, ITEMS.register(name + e.getName(), item.apply(e)));
        }
        return Maps.immutableEnumMap(map);
    }

    private static Item pickaxe(ItemMaterial tier) {
        return new ItemPickaxe(tier, MathHelper.attackSpeed(0.85F), propMisc(), tier.getPickaxeMass());
    }

    public static Item.Properties propDev() {
        return new Item.Properties().tab(EvolutionCreativeTabs.DEV);
    }

    public static Item.Properties propEgg() {
        return new Item.Properties().tab(EvolutionCreativeTabs.EGGS);
    }

    public static Item.Properties propLiquid() {
        return new Item.Properties().tab(EvolutionCreativeTabs.LIQUIDS);
    }

    public static Item.Properties propMetal() {
        return new Item.Properties().tab(EvolutionCreativeTabs.METAL);
    }

    public static Item.Properties propMisc() {
        return new Item.Properties().tab(EvolutionCreativeTabs.MISC);
    }

    public static Item.Properties propPartTool() {
        return new Item.Properties().tab(EvolutionCreativeTabs.PARTS_AND_TOOLS);
    }

    public static Item.Properties propTreesAndWood() {
        return new Item.Properties().tab(EvolutionCreativeTabs.TREES_AND_WOOD);
    }

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static Item shovelStone(ItemMaterial tier) {
        return new ItemShovel(tier, MathHelper.attackSpeed(1.0F), propPartTool(), tier.getShovelMass());
    }

    private static Item stoneHeads() {
        return new ItemEv(propPartTool().stacksTo(16));
    }

    private static Item wood() {
        return new ItemEv(propTreesAndWood());
    }

    private static Item woodBlock(Block block) {
        return new ItemBlock(block, propTreesAndWood());
    }

}
