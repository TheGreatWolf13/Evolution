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
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.items.*;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.items.modular.part.*;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

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
    public static final RegistryObject<ItemPartBlade> BLADE_PART = ITEMS.register("blade_part", () -> new ItemPartBlade(propPartTool()));
    public static final RegistryObject<ItemPartGuard> GUARD_PART = ITEMS.register("guard_part", () -> new ItemPartGuard(propPartTool()));
    public static final RegistryObject<ItemPartHalfHead> HALFHEAD_PART = ITEMS.register("halfhead_part", () -> new ItemPartHalfHead(propPartTool()));
    public static final RegistryObject<ItemPartHandle> HANDLE_PART = ITEMS.register("handle_part", () -> new ItemPartHandle(propPartTool()));
    public static final RegistryObject<ItemPartHead> HEAD_PART = ITEMS.register("head_part", () -> new ItemPartHead(propPartTool()));
    public static final RegistryObject<ItemPartHilt> HILT_PART = ITEMS.register("hilt_part", () -> new ItemPartHilt(propPartTool()));
    public static final RegistryObject<ItemPartPole> POLE_PART = ITEMS.register("pole_part", () -> new ItemPartPole(propPartTool()));
    public static final RegistryObject<ItemPartPommel> POMMEL_PART = ITEMS.register("pommel_part", () -> new ItemPartPommel(propPartTool()));

    public static final RegistryObject<Item> MODULAR_TOOL = ITEMS.register("modular_tool", () -> new ItemModularTool(propPartTool()));
    //Stick
    public static final RegistryObject<Item> GLASS = ITEMS.register("glass", () -> itemBlock(EvolutionBlocks.GLASS.get()));
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
    //Planks
    public static final Map<WoodVariant, RegistryObject<Item>> ALL_PLANKS = make(WoodVariant.class, WoodVariant.VALUES, "planks_",
                                                                                 e -> () -> woodBlock(e.getPlanks()));
    //Shadow Hound Block
//    public static final RegistryObject<Item> shadowhound = ITEMS.register("shadowhound",
//                                                                          () -> new ItemBlock(SHADOWHOUND.get(),
//                                                                                              propMisc().setTEISR(() ->
//                                                                                              RenderStackTileShadowHound::new)));
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
    //Rope
    public static final RegistryObject<Item> rope = ITEMS.register("rope", () -> item(propMisc()));
    public static final RegistryObject<Item> climbing_stake = ITEMS.register("climbing_stake", () -> itemBlock(EvolutionBlocks.CLIMBING_STAKE.get()));
    public static final RegistryObject<Item> climbing_hook = ITEMS.register("climbing_hook", ItemClimbingHook::new);
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
    public static final RegistryObject<Item> shield_dev = ITEMS.register("shield_dev", () -> new ItemShield(propDev().durability(400)));

    private EvolutionItems() {
    }

    @Contract(pure = true, value = "_ -> new")
    private static Item bucketCeramic(Supplier<? extends Fluid> fluid) {
        if (fluid instanceof RegistryObject) {
            return new ItemBucketCeramic(fluid, propLiquid().stacksTo(1));
        }
        return new ItemBucketCeramic(fluid, propLiquid().stacksTo(fluid.get() == Fluids.EMPTY ? 16 : 1));
    }

    @Contract(pure = true, value = "_ -> new")
    private static Item bucketCreative(Supplier<? extends Fluid> fluid) {
        if (fluid instanceof RegistryObject) {
            return new ItemBucketCreative(fluid, propLiquid().stacksTo(1));
        }
        return new ItemBucketCreative(fluid, propLiquid().stacksTo(fluid.get() == Fluids.EMPTY ? 16 : 1));
    }

    @Contract(pure = true, value = "_ -> new")
    private static Item item(Item.Properties prop) {
        return new ItemEv(prop);
    }

    @Contract(pure = true, value = "_ -> new")
    private static Item itemBlock(Block block) {
        return new ItemBlock(block, propMisc());
    }

    @Contract(pure = true, value = "_, _ -> new")
    private static Item itemBlock(RegistryObject<Block> block, Item.Properties prop) {
        return new ItemBlock(block.get(), prop);
    }

    @Contract(pure = true, value = "_, _, _, _ -> new")
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

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propDev() {
        return new Item.Properties().tab(EvolutionCreativeTabs.DEV);
    }

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propEgg() {
        return new Item.Properties().tab(EvolutionCreativeTabs.EGGS);
    }

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propLiquid() {
        return new Item.Properties().tab(EvolutionCreativeTabs.LIQUIDS);
    }

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propMetal() {
        return new Item.Properties().tab(EvolutionCreativeTabs.METAL);
    }

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propMisc() {
        return new Item.Properties().tab(EvolutionCreativeTabs.MISC);
    }

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propPartTool() {
        return new Item.Properties().tab(EvolutionCreativeTabs.PARTS_AND_TOOLS);
    }

    @Contract(pure = true, value = " -> new")
    public static Item.Properties propTreesAndWood() {
        return new Item.Properties().tab(EvolutionCreativeTabs.TREES_AND_WOOD);
    }

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Contract(pure = true, value = " -> new")
    private static Item wood() {
        return new ItemEv(propTreesAndWood());
    }

    @Contract(pure = true, value = "_ -> new")
    private static Item woodBlock(Block block) {
        return new ItemBlock(block, propTreesAndWood());
    }

}
