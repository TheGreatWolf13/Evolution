package tgw.evolution.init;

import com.google.common.collect.Maps;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
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
    //Temporary
    public static final RegistryObject<Item> GLASS;
    public static final RegistryObject<Item> PLACEHOLDER_BLOCK;
    public static final RegistryObject<Item> BACKPACK;
    public static final RegistryObject<Item> HAT;
    public static final RegistryObject<Item> SHIRT;
    public static final RegistryObject<Item> TROUSERS;
    public static final RegistryObject<Item> SOCKS;
    public static final RegistryObject<Item> MASK;
    public static final RegistryObject<Item> QUIVER;
    //Dev
    public static final RegistryObject<Item> CLOCK;
    public static final RegistryObject<Item> CRICKET;
    public static final RegistryObject<Item> DEBUG_ITEM;
    public static final RegistryObject<Item> DESTROY_3;
    public static final RegistryObject<Item> DESTROY_6;
    public static final RegistryObject<Item> DESTROY_9;
    public static final RegistryObject<Item> DEV_DRINK;
    public static final RegistryObject<Item> DEV_FOOD;
    public static final RegistryObject<Item> PUZZLE;
    public static final RegistryObject<Item> SCHEMATIC_BLOCK;
    public static final RegistryObject<Item> SEXTANT;
    public static final RegistryObject<Item> SHIELD_DEV;
    public static final RegistryObject<Item> SPEEDOMETER;
    //Independent
    public static final RegistryObject<Item> BLOCK_METAL_COPPER;
    public static final RegistryObject<Item> BLOCK_METAL_COPPER_E;
    public static final RegistryObject<Item> BLOCK_METAL_COPPER_W;
    public static final RegistryObject<Item> BLOCK_METAL_COPPER_O;
    public static final RegistryObject<Item> BRICK_CLAY;
    public static final RegistryObject<Item> CLAY;
    public static final RegistryObject<Item> CLAYBALL;
    public static final RegistryObject<Item> CLIMBING_HOOK;
    public static final RegistryObject<Item> CLIMBING_STAKE;
    public static final RegistryObject<Item> CRUCIBLE_CLAY;
    public static final RegistryObject<Item> FIRE_STARTER;
    public static final RegistryObject<Item> TALLGRASS;
    public static final RegistryObject<Item> INGOT_COPPER;
    public static final RegistryObject<Item> MODULAR_TOOL;
    public static final RegistryObject<Item> MOLD_CLAY_AXE;
    public static final RegistryObject<Item> MOLD_CLAY_GUARD;
    public static final RegistryObject<Item> MOLD_CLAY_HAMMER;
    public static final RegistryObject<Item> MOLD_CLAY_HOE;
    public static final RegistryObject<Item> MOLD_CLAY_INGOT;
    public static final RegistryObject<Item> MOLD_CLAY_KNIFE;
    public static final RegistryObject<Item> MOLD_CLAY_PICKAXE;
    public static final RegistryObject<Item> MOLD_CLAY_SAW;
    public static final RegistryObject<Item> MOLD_CLAY_SHOVEL;
    public static final RegistryObject<Item> MOLD_CLAY_SPEAR;
    public static final RegistryObject<Item> MOLD_CLAY_SWORD;
    public static final RegistryObject<Item> NUGGET_COPPER;
    public static final RegistryObject<ItemPartBlade> PART_BLADE;
    public static final RegistryObject<ItemPartHilt> PART_GRIP;
    public static final RegistryObject<ItemPartGuard> PART_GUARD;
    public static final RegistryObject<ItemPartHalfHead> PART_HALFHEAD;
    public static final RegistryObject<ItemPartHandle> PART_HANDLE;
    public static final RegistryObject<ItemPartHead> PART_HEAD;
    public static final RegistryObject<ItemPartPole> PART_POLE;
    public static final RegistryObject<ItemPartPommel> PART_POMMEL;
    public static final RegistryObject<Item> PEAT;
    public static final RegistryObject<Item> ROPE;
    public static final RegistryObject<Item> STICK;
    public static final RegistryObject<Item> STRAW;
    public static final RegistryObject<Item> TALLGRASS_HIGH;
    public static final RegistryObject<Item> TORCH;
    public static final RegistryObject<Item> TORCH_UNLIT;
    //Collection
    public static final Map<WoodVariant, RegistryObject<Item>> CHOPPING_BLOCKS;
    public static final Map<RockVariant, RegistryObject<Item>> COBBLESTONES;
    public static final Map<RockVariant, RegistryObject<Item>> DIRTS;
    public static final Map<RockVariant, RegistryObject<Item>> DRY_GRASSES;
    public static final Map<WoodVariant, RegistryObject<Item>> FIREWOODS;
    public static final Map<RockVariant, RegistryObject<Item>> GRASSES;
    public static final Map<RockVariant, RegistryObject<Item>> GRAVELS;
    public static final Map<WoodVariant, RegistryObject<Item>> LEAVES;
    public static final Map<WoodVariant, RegistryObject<Item>> LOGS;
    public static final Map<WoodVariant, RegistryObject<Item>> PLANK;
    public static final Map<WoodVariant, RegistryObject<Item>> PLANKS;
    public static final Map<RockVariant, RegistryObject<Item>> POLISHED_STONES;
    public static final Map<RockVariant, RegistryObject<Item>> PRIMITIVE_KNIVES;
    public static final Map<RockVariant, RegistryObject<Item>> ROCKS;
    public static final Map<RockVariant, RegistryObject<Item>> SANDS;
    public static final Map<WoodVariant, RegistryObject<Item>> SAPLINGS;
    public static final Map<RockVariant, RegistryObject<Item>> STONEBRICKS;
    public static final Map<RockVariant, RegistryObject<Item>> STONES;

    static {
        //Temporary
        GLASS = makeBlock(EvolutionBlocks.GLASS, b -> () -> new ItemBlock(b.get(), propMisc()));
        PLACEHOLDER_BLOCK = makeBlock(EvolutionBlocks.PLACEHOLDER_BLOCK, b -> () -> new ItemBlock(b.get(), propDev()));
        BACKPACK = ITEMS.register("temp_backpack", () -> new ItemBackpack(propMisc()));
        HAT = ITEMS.register("temp_hat", () -> new ItemHat(propMisc()));
        SHIRT = ITEMS.register("temp_shirt", () -> new ItemShirt(propMisc()));
        TROUSERS = ITEMS.register("temp_trousers", () -> new ItemTrousers(propMisc()));
        SOCKS = ITEMS.register("temp_socks", () -> new ItemSocks(propMisc()));
        MASK = ITEMS.register("temp_mask", () -> new ItemMask(propMisc()));
        QUIVER = ITEMS.register("temp_quiver", () -> new ItemQuiver(propMisc()));
        //Dev
        CLOCK = ITEMS.register("clock", () -> new ItemClock(propDev()));
        CRICKET = ITEMS.register("cricket", () -> new ItemCricket(propDev()));
        DEBUG_ITEM = ITEMS.register("debug_item", () -> item(propDev()));
        DESTROY_3 = makeBlock(EvolutionBlocks.DESTROY_3, b -> () -> new ItemBlock(b.get(), new Item.Properties()));
        DESTROY_6 = makeBlock(EvolutionBlocks.DESTROY_6, b -> () -> new ItemBlock(b.get(), new Item.Properties()));
        DESTROY_9 = makeBlock(EvolutionBlocks.DESTROY_9, b -> () -> new ItemBlock(b.get(), new Item.Properties()));
        DEV_DRINK = ITEMS.register("dev_drink", () -> new ItemDrink(propDev(), new IConsumable.DrinkProperties(250)));
        DEV_FOOD = ITEMS.register("dev_food", () -> new ItemFood(propDev(), new IConsumable.FoodProperties(250)));
        PUZZLE = makeBlock(EvolutionBlocks.PUZZLE, b -> () -> new ItemBlock(b.get(), propDev()));
        SCHEMATIC_BLOCK = makeBlock(EvolutionBlocks.SCHEMATIC_BLOCK, b -> () -> new ItemBlock(b.get(), propDev()));
        SEXTANT = ITEMS.register("sextant", () -> new ItemSextant(propDev()));
        SHIELD_DEV = ITEMS.register("shield_dev", () -> new ItemShield(propDev().durability(400)));
        SPEEDOMETER = ITEMS.register("speedometer", () -> new ItemSpeedometer(propDev()));
        //Independent
        BLOCK_METAL_COPPER = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER, b -> () -> new ItemBlock(b.get(), propMetal()));
        BLOCK_METAL_COPPER_E = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER_E, b -> () -> new ItemBlock(b.get(), propMetal()));
        BLOCK_METAL_COPPER_W = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER_W, b -> () -> new ItemBlock(b.get(), propMetal()));
        BLOCK_METAL_COPPER_O = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER_O, b -> () -> new ItemBlock(b.get(), propMetal()));
        BRICK_CLAY = makeBlock(EvolutionBlocks.BRICK_CLAY, b -> () -> new ItemClayMolded(b.get()));
        CLAY = makeBlock(EvolutionBlocks.CLAY, b -> () -> itemBlock(b.get()));
        CLAYBALL = ITEMS.register("clayball", ItemClay::new);
        CLIMBING_HOOK = ITEMS.register("climbing_hook", ItemClimbingHook::new);
        CLIMBING_STAKE = ITEMS.register("climbing_stake", () -> itemBlock(EvolutionBlocks.CLIMBING_STAKE.get()));
        CRUCIBLE_CLAY = makeBlock(EvolutionBlocks.CRUCIBLE_CLAY, b -> () -> new ItemClayMolded(b.get(), true));
        FIRE_STARTER = ITEMS.register("fire_starter", ItemFireStarter::new);
        INGOT_COPPER = ITEMS.register("ingot_copper", () -> new ItemIngot(propMetal()));
        MODULAR_TOOL = ITEMS.register("modular_tool", () -> new ItemModularTool(propPartTool()));
        MOLD_CLAY_AXE = makeBlock(EvolutionBlocks.MOLD_CLAY_AXE, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_GUARD = makeBlock(EvolutionBlocks.MOLD_CLAY_GUARD, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_HAMMER = makeBlock(EvolutionBlocks.MOLD_CLAY_HAMMER, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_HOE = makeBlock(EvolutionBlocks.MOLD_CLAY_HOE, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_INGOT = makeBlock(EvolutionBlocks.MOLD_CLAY_INGOT, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_KNIFE = makeBlock(EvolutionBlocks.MOLD_CLAY_KNIFE, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_PICKAXE = makeBlock(EvolutionBlocks.MOLD_CLAY_PICKAXE, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_SAW = makeBlock(EvolutionBlocks.MOLD_CLAY_SAW, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_SHOVEL = makeBlock(EvolutionBlocks.MOLD_CLAY_SHOVEL, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_SPEAR = makeBlock(EvolutionBlocks.MOLD_CLAY_SPEAR, b -> () -> new ItemClayMolded(b.get()));
        MOLD_CLAY_SWORD = makeBlock(EvolutionBlocks.MOLD_CLAY_SWORD, b -> () -> new ItemClayMolded(b.get()));
        NUGGET_COPPER = ITEMS.register("nugget_copper", () -> item(propMisc()));
        PART_BLADE = ITEMS.register("part_blade", () -> new ItemPartBlade(propPartTool()));
        PART_GRIP = ITEMS.register("part_grip", () -> new ItemPartHilt(propPartTool()));
        PART_GUARD = ITEMS.register("part_guard", () -> new ItemPartGuard(propPartTool()));
        PART_HALFHEAD = ITEMS.register("part_halfhead", () -> new ItemPartHalfHead(propPartTool()));
        PART_HANDLE = ITEMS.register("part_handle", () -> new ItemPartHandle(propPartTool()));
        PART_HEAD = ITEMS.register("part_head", () -> new ItemPartHead(propPartTool()));
        PART_POLE = ITEMS.register("part_pole", () -> new ItemPartPole(propPartTool()));
        PART_POMMEL = ITEMS.register("part_pommel", () -> new ItemPartPommel(propPartTool()));
        PEAT = makeBlock(EvolutionBlocks.PEAT, b -> () -> itemBlock(b.get()));
        ROPE = ITEMS.register("rope", () -> item(propMisc()));
        STICK = makeBlock(EvolutionBlocks.STICK, b -> () -> new ItemStick(b.get(), propTreesAndWood()));
        STRAW = ITEMS.register("straw", () -> item(propMisc()));
        TALLGRASS = makeBlock(EvolutionBlocks.TALLGRASS, b -> () -> itemBlock(b.get()));
        TALLGRASS_HIGH = makeBlock(EvolutionBlocks.TALLGRASS_HIGH, b -> () -> itemBlock(b.get()));
        TORCH = makeBlock(EvolutionBlocks.TORCH, b -> () -> new ItemTorch(propMisc()));
        TORCH_UNLIT = ITEMS.register("torch_unlit", () -> new ItemTorchUnlit(propMisc()));
        //Collection
        CHOPPING_BLOCKS = makeBlock(WoodVariant.class, EvolutionBlocks.CHOPPING_BLOCKS, e -> () -> woodBlock(e.get(EvolutionBlocks.CHOPPING_BLOCKS)));
        COBBLESTONES = makeBlock(RockVariant.class, EvolutionBlocks.COBBLESTONES, e -> () -> itemBlock(e.get(EvolutionBlocks.COBBLESTONES)));
        DIRTS = makeBlock(RockVariant.class, EvolutionBlocks.DIRTS, e -> () -> itemBlock(e.get(EvolutionBlocks.DIRTS)));
        DRY_GRASSES = makeBlock(RockVariant.class, EvolutionBlocks.DRY_GRASSES, e -> () -> itemBlock(e.get(EvolutionBlocks.DRY_GRASSES)));
        FIREWOODS = make(WoodVariant.class, WoodVariant.VALUES, "firewood_", e -> () -> new ItemFirewood(e));
        GRASSES = makeBlock(RockVariant.class, EvolutionBlocks.GRASSES, e -> () -> itemBlock(e.get(EvolutionBlocks.GRASSES)));
        GRAVELS = makeBlock(RockVariant.class, EvolutionBlocks.GRAVELS, e -> () -> itemBlock(e.get(EvolutionBlocks.GRAVELS)));
        LEAVES = makeBlock(WoodVariant.class, EvolutionBlocks.LEAVES, e -> () -> woodBlock(e.get(EvolutionBlocks.LEAVES)));
        LOGS = makeBlock(WoodVariant.class, EvolutionBlocks.LOGS, e -> () -> new ItemLog(e, e.get(EvolutionBlocks.LOGS), propTreesAndWood()));
        PLANK = make(WoodVariant.class, WoodVariant.VALUES, "plank_", e -> EvolutionItems::wood);
        PLANKS = makeBlock(WoodVariant.class, EvolutionBlocks.PLANKS, e -> () -> woodBlock(e.get(EvolutionBlocks.PLANKS)));
        POLISHED_STONES = makeBlock(RockVariant.class, EvolutionBlocks.POLISHED_STONES, e -> () -> itemBlock(e.get(EvolutionBlocks.POLISHED_STONES)));
        PRIMITIVE_KNIVES = make(RockVariant.class, RockVariant.VALUES_STONE, "primitive_knife_", e -> () -> new ItemEv(propPartTool()));
        ROCKS = makeBlock(RockVariant.class, EvolutionBlocks.ROCKS, e -> () -> new ItemRock(e.get(EvolutionBlocks.ROCKS), propMisc(), e));
        SANDS = makeBlock(RockVariant.class, EvolutionBlocks.SANDS, e -> () -> itemBlock(e.get(EvolutionBlocks.SANDS)));
        SAPLINGS = makeBlock(WoodVariant.class, EvolutionBlocks.SAPLINGS, e -> () -> woodBlock(e.get(EvolutionBlocks.SAPLINGS)));
        STONEBRICKS = makeBlock(RockVariant.class, EvolutionBlocks.STONEBRICKS, e -> () -> itemBlock(e.get(EvolutionBlocks.STONEBRICKS)));
        STONES = makeBlock(RockVariant.class, EvolutionBlocks.STONES, e -> () -> itemBlock(e.get(EvolutionBlocks.STONES)));
    }

    private EvolutionItems() {
    }

//    @Contract(pure = true, value = "_ -> new")
//    private static Item bucketCeramic(Supplier<? extends Fluid> fluid) {
//        if (fluid instanceof RegistryObject) {
//            return new ItemBucketCeramic(fluid, propLiquid().stacksTo(1));
//        }
//        return new ItemBucketCeramic(fluid, propLiquid().stacksTo(fluid.get() == Fluids.EMPTY ? 16 : 1));
//    }

//    @Contract(pure = true, value = "_ -> new")
//    private static Item bucketCreative(Supplier<? extends Fluid> fluid) {
//        if (fluid instanceof RegistryObject) {
//            return new ItemBucketCreative(fluid, propLiquid().stacksTo(1));
//        }
//        return new ItemBucketCreative(fluid, propLiquid().stacksTo(fluid.get() == Fluids.EMPTY ? 16 : 1));
//    }

    @Contract(pure = true, value = "_ -> new")
    private static Item item(Item.Properties prop) {
        return new ItemEv(prop);
    }

    @Contract(pure = true, value = "_ -> new")
    private static Item itemBlock(Block block) {
        return new ItemBlock(block, propMisc());
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

    private static RegistryObject<Item> makeBlock(RegistryObject<Block> block, Function<RegistryObject<Block>, Supplier<Item>> item) {
        return ITEMS.register(block.getId().getPath(), item.apply(block));
    }

    @Contract(pure = true, value = "_, _, _ -> new")
    private static <E extends Enum<E> & IVariant, B extends Block> Map<E, RegistryObject<Item>> makeBlock(Class<E> clazz,
                                                                                                          Map<E, RegistryObject<B>> blocks,
                                                                                                          Function<E, Supplier<Item>> item) {
        Map<E, RegistryObject<Item>> map = new EnumMap<>(clazz);
        for (Map.Entry<E, RegistryObject<B>> entry : blocks.entrySet()) {
            E e = entry.getKey();
            map.put(e, ITEMS.register(entry.getValue().getId().getPath(), item.apply(e)));
        }
        return Maps.immutableEnumMap(map);
    }

    @Contract(pure = true, value = " -> new")
    private static Item.Properties propDev() {
        return new Item.Properties().tab(EvolutionCreativeTabs.DEV);
    }

//    @Contract(pure = true, value = " -> new")
//    private static Item.Properties propLiquid() {
//        return new Item.Properties().tab(EvolutionCreativeTabs.LIQUIDS);
//    }

    @Contract(pure = true, value = " -> new")
    private static Item.Properties propMetal() {
        return new Item.Properties().tab(EvolutionCreativeTabs.METAL);
    }

    @Contract(pure = true, value = " -> new")
    private static Item.Properties propMisc() {
        return new Item.Properties().tab(EvolutionCreativeTabs.MISC);
    }

    @Contract(pure = true, value = " -> new")
    private static Item.Properties propPartTool() {
        return new Item.Properties().tab(EvolutionCreativeTabs.PARTS_AND_TOOLS);
    }

    @Contract(pure = true, value = " -> new")
    private static Item.Properties propTreesAndWood() {
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
