package tgw.evolution.init;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

public final class EvolutionItems {

    //Temporary
    public static final Item GLASS;
    public static final Item PLACEHOLDER_BLOCK;
    public static final Item BACKPACK;
    public static final Item HAT;
    public static final Item SHIRT;
    public static final Item TROUSERS;
    public static final Item SOCKS;
    public static final Item MASK;
    public static final Item QUIVER;
    public static final Item LANTERN;
    public static final Item SOUL_LANTERN;
    //Dev
    public static final Item CLOCK;
    public static final Item DEBUG_ITEM;
    public static final Item DESTROY_3;
    public static final Item DESTROY_6;
    public static final Item DESTROY_9;
    public static final Item DEV_DRINK;
    public static final Item DEV_FOOD;
    public static final Item PUZZLE;
    public static final Item SCHEMATIC_BLOCK;
    public static final Item SEXTANT;
    public static final Item SHIELD_DEV;
    public static final Item SPEEDOMETER;
    //Spawn Eggs
//    public static final Item SPAWN_EGG_COW;
    //Independent
    public static final Item BLOCK_METAL_COPPER;
    public static final Item BLOCK_METAL_COPPER_E;
    public static final Item BLOCK_METAL_COPPER_W;
    public static final Item BLOCK_METAL_COPPER_O;
    public static final Item BRICK_CLAY;
    public static final Item CLAY;
    public static final Item CLAYBALL;
    public static final Item CLIMBING_HOOK;
    public static final Item CLIMBING_STAKE;
    public static final Item CRUCIBLE_CLAY;
    public static final Item FIRE_STARTER;
    public static final Item TALLGRASS;
    public static final Item INGOT_COPPER;
    public static final ItemModularTool MODULAR_TOOL;
    public static final Item MOLD_CLAY_AXE;
    public static final Item MOLD_CLAY_GUARD;
    public static final Item MOLD_CLAY_HAMMER;
    public static final Item MOLD_CLAY_HOE;
    public static final Item MOLD_CLAY_INGOT;
    public static final Item MOLD_CLAY_KNIFE;
    public static final Item MOLD_CLAY_PICKAXE;
    public static final Item MOLD_CLAY_SAW;
    public static final Item MOLD_CLAY_SHOVEL;
    public static final Item MOLD_CLAY_SPEAR;
    public static final Item MOLD_CLAY_SWORD;
    public static final Item NUGGET_COPPER;
    public static final ItemPartBlade PART_BLADE;
    public static final ItemPartHilt PART_GRIP;
    public static final ItemPartGuard PART_GUARD;
    public static final ItemPartHalfHead PART_HALFHEAD;
    public static final ItemPartHandle PART_HANDLE;
    public static final ItemPartHead PART_HEAD;
    public static final ItemPartPole PART_POLE;
    public static final ItemPartPommel PART_POMMEL;
    //    public static final Item PEAT;
    public static final Item ROPE;
    public static final Item STICK;
    public static final Item STRAW;
    public static final Item TALLGRASS_HIGH;
    public static final Item TORCH;
    public static final Item TORCH_UNLIT;
    //Collection
    public static final Map<WoodVariant, Item> CHOPPING_BLOCKS;
    public static final Map<RockVariant, Item> COBBLESTONES;
    public static final Map<RockVariant, Item> DIRTS;
    public static final Map<RockVariant, Item> DRY_GRASSES;
    public static final Map<WoodVariant, Item> FIREWOODS;
    public static final Map<RockVariant, Item> GRASSES;
    public static final Map<RockVariant, Item> GRAVELS;
    public static final Map<WoodVariant, Item> LEAVES;
    public static final Map<WoodVariant, Item> LOGS;
    public static final Map<WoodVariant, Item> PLANK;
    public static final Map<WoodVariant, Item> PLANKS;
    public static final Map<RockVariant, Item> POLISHED_STONES;
    public static final Map<RockVariant, Item> PRIMITIVE_KNIVES;
    public static final Map<RockVariant, Item> ROCKS;
    public static final Map<RockVariant, Item> SANDS;
    public static final Map<WoodVariant, Item> SAPLINGS;
    public static final Map<RockVariant, Item> STONEBRICKS;
    public static final Map<RockVariant, Item> STONES;

    static {
        //Temporary
        GLASS = makeBlock(EvolutionBlocks.GLASS, new ItemBlock(EvolutionBlocks.GLASS, propMisc()));
        PLACEHOLDER_BLOCK = makeBlock(EvolutionBlocks.PLACEHOLDER_BLOCK, new ItemBlock(EvolutionBlocks.PLACEHOLDER_BLOCK, propDev()));
        BACKPACK = register("temp_backpack", new ItemBackpack(propMisc()));
        HAT = register("temp_hat", new ItemHat(propMisc()));
        SHIRT = register("temp_shirt", new ItemShirt(propMisc()));
        TROUSERS = register("temp_trousers", new ItemTrousers(propMisc()));
        SOCKS = register("temp_socks", new ItemSocks(propMisc()));
        MASK = register("temp_mask", new ItemMask(propMisc()));
        QUIVER = register("temp_quiver", new ItemQuiver(propMisc()));
        LANTERN = register("lantern", new ItemLantern(0b1_1110_1_1110_1_1110, propMisc()));
        SOUL_LANTERN = register("soul_lantern", new ItemLantern(0b1_1110_1_1110_0_0000, propMisc()));
        //Dev
        CLOCK = register("clock", new ItemClock(propDev()));
        DEBUG_ITEM = register("debug_item", item(propDev()));
        DESTROY_3 = makeBlock(EvolutionBlocks.DESTROY_3, new ItemBlock(EvolutionBlocks.DESTROY_3, new Item.Properties()));
        DESTROY_6 = makeBlock(EvolutionBlocks.DESTROY_6, new ItemBlock(EvolutionBlocks.DESTROY_6, new Item.Properties()));
        DESTROY_9 = makeBlock(EvolutionBlocks.DESTROY_9, new ItemBlock(EvolutionBlocks.DESTROY_9, new Item.Properties()));
        DEV_DRINK = register("dev_drink", new ItemDrink(propDev(), new IConsumable.DrinkProperties(250)));
        DEV_FOOD = register("dev_food", new ItemFood(propDev(), new IConsumable.FoodProperties(250)));
        PUZZLE = makeBlock(EvolutionBlocks.PUZZLE, new ItemBlock(EvolutionBlocks.PUZZLE, propDev()));
        SCHEMATIC_BLOCK = makeBlock(EvolutionBlocks.SCHEMATIC_BLOCK, new ItemBlock(EvolutionBlocks.SCHEMATIC_BLOCK, propDev()));
        SEXTANT = register("sextant", new ItemSextant(propDev()));
        SHIELD_DEV = register("shield_dev", new ItemShield(propDev().durability(400)));
        SPEEDOMETER = register("speedometer", new ItemSpeedometer(propDev()));
        //Spawn Eggs
//        SPAWN_EGG_COW = genEgg(EvolutionEntities.COW);
        //Independent
        BLOCK_METAL_COPPER = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER, new ItemBlock(EvolutionBlocks.BLOCK_METAL_COPPER, propMetal()));
        BLOCK_METAL_COPPER_E = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER_E, new ItemBlock(EvolutionBlocks.BLOCK_METAL_COPPER_E, propMetal()));
        BLOCK_METAL_COPPER_W = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER_W, new ItemBlock(EvolutionBlocks.BLOCK_METAL_COPPER_W, propMetal()));
        BLOCK_METAL_COPPER_O = makeBlock(EvolutionBlocks.BLOCK_METAL_COPPER_O, new ItemBlock(EvolutionBlocks.BLOCK_METAL_COPPER_O, propMetal()));
        BRICK_CLAY = makeBlock(EvolutionBlocks.BRICK_CLAY, new ItemClayMolded(EvolutionBlocks.BRICK_CLAY));
        CLAY = makeBlock(EvolutionBlocks.CLAY, itemBlock(EvolutionBlocks.CLAY));
        CLAYBALL = register("clayball", new ItemClay());
        CLIMBING_HOOK = register("climbing_hook", new ItemClimbingHook());
        CLIMBING_STAKE = makeBlock(EvolutionBlocks.CLIMBING_STAKE, itemBlock(EvolutionBlocks.CLIMBING_STAKE));
        CRUCIBLE_CLAY = makeBlock(EvolutionBlocks.CRUCIBLE_CLAY, new ItemClayMolded(EvolutionBlocks.CRUCIBLE_CLAY, true));
        FIRE_STARTER = register("fire_starter", new ItemFireStarter());
        INGOT_COPPER = register("ingot_copper", new ItemIngot(propMetal()));
        MODULAR_TOOL = register("modular_tool", new ItemModularTool(propPartTool()));
        MOLD_CLAY_AXE = makeBlock(EvolutionBlocks.MOLD_CLAY_AXE, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_AXE));
        MOLD_CLAY_GUARD = makeBlock(EvolutionBlocks.MOLD_CLAY_GUARD, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_GUARD));
        MOLD_CLAY_HAMMER = makeBlock(EvolutionBlocks.MOLD_CLAY_HAMMER, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_HAMMER));
        MOLD_CLAY_HOE = makeBlock(EvolutionBlocks.MOLD_CLAY_HOE, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_HOE));
        MOLD_CLAY_INGOT = makeBlock(EvolutionBlocks.MOLD_CLAY_INGOT, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_INGOT));
        MOLD_CLAY_KNIFE = makeBlock(EvolutionBlocks.MOLD_CLAY_KNIFE, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_KNIFE));
        MOLD_CLAY_PICKAXE = makeBlock(EvolutionBlocks.MOLD_CLAY_PICKAXE, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_PICKAXE));
        MOLD_CLAY_SAW = makeBlock(EvolutionBlocks.MOLD_CLAY_SAW, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SAW));
        MOLD_CLAY_SHOVEL = makeBlock(EvolutionBlocks.MOLD_CLAY_SHOVEL, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SHOVEL));
        MOLD_CLAY_SPEAR = makeBlock(EvolutionBlocks.MOLD_CLAY_SPEAR, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SPEAR));
        MOLD_CLAY_SWORD = makeBlock(EvolutionBlocks.MOLD_CLAY_SWORD, new ItemClayMolded(EvolutionBlocks.MOLD_CLAY_SWORD));
        NUGGET_COPPER = register("nugget_copper", item(propMisc()));
        PART_BLADE = register("part_blade", new ItemPartBlade(propPartTool()));
        PART_GRIP = register("part_grip", new ItemPartHilt(propPartTool()));
        PART_GUARD = register("part_guard", new ItemPartGuard(propPartTool()));
        PART_HALFHEAD = register("part_halfhead", new ItemPartHalfHead(propPartTool()));
        PART_HANDLE = register("part_handle", new ItemPartHandle(propPartTool()));
        PART_HEAD = register("part_head", new ItemPartHead(propPartTool()));
        PART_POLE = register("part_pole", new ItemPartPole(propPartTool()));
        PART_POMMEL = register("part_pommel", new ItemPartPommel(propPartTool()));
//        PEAT = makeBlock(EvolutionBlocks.PEAT, itemBlock(EvolutionBlocks.PEAT));
        ROPE = register("rope", item(propMisc()));
        STICK = makeBlock(EvolutionBlocks.STICK, new ItemStick(EvolutionBlocks.STICK, propTreesAndWood()));
        STRAW = register("straw", item(propMisc()));
        TALLGRASS = makeBlock(EvolutionBlocks.TALLGRASS, itemBlock(EvolutionBlocks.TALLGRASS));
        TALLGRASS_HIGH = makeBlock(EvolutionBlocks.TALLGRASS_HIGH, itemBlock(EvolutionBlocks.TALLGRASS_HIGH));
        TORCH = makeBlock(EvolutionBlocks.TORCH, new ItemTorch(propMisc()));
        TORCH_UNLIT = register("torch_unlit", new ItemTorchUnlit(propMisc()));
        //Collection
        CHOPPING_BLOCKS = makeBlock(WoodVariant.class, EvolutionBlocks.CHOPPING_BLOCKS, e -> woodBlock(e.get(EvolutionBlocks.CHOPPING_BLOCKS)));
        COBBLESTONES = makeBlock(RockVariant.class, EvolutionBlocks.COBBLESTONES, e -> itemBlock(e.get(EvolutionBlocks.COBBLESTONES)));
        DIRTS = makeBlock(RockVariant.class, EvolutionBlocks.DIRTS, e -> itemBlock(e.get(EvolutionBlocks.DIRTS)));
        DRY_GRASSES = makeBlock(RockVariant.class, EvolutionBlocks.DRY_GRASSES, e -> itemBlock(e.get(EvolutionBlocks.DRY_GRASSES)));
        FIREWOODS = make(WoodVariant.class, WoodVariant.VALUES, "firewood_", ItemFirewood::new);
        GRASSES = makeBlock(RockVariant.class, EvolutionBlocks.GRASSES, e -> itemBlock(e.get(EvolutionBlocks.GRASSES)));
        GRAVELS = makeBlock(RockVariant.class, EvolutionBlocks.GRAVELS, e -> itemBlock(e.get(EvolutionBlocks.GRAVELS)));
        LEAVES = makeBlock(WoodVariant.class, EvolutionBlocks.LEAVES, e -> woodBlock(e.get(EvolutionBlocks.LEAVES)));
        LOGS = makeBlock(WoodVariant.class, EvolutionBlocks.LOGS, e -> new ItemLog(e, e.get(EvolutionBlocks.LOGS), propTreesAndWood()));
        PLANK = make(WoodVariant.class, WoodVariant.VALUES, "plank_", e -> wood());
        PLANKS = makeBlock(WoodVariant.class, EvolutionBlocks.PLANKS, e -> woodBlock(e.get(EvolutionBlocks.PLANKS)));
        POLISHED_STONES = makeBlock(RockVariant.class, EvolutionBlocks.POLISHED_STONES, e -> itemBlock(e.get(EvolutionBlocks.POLISHED_STONES)));
        PRIMITIVE_KNIVES = make(RockVariant.class, RockVariant.VALUES_STONE, "primitive_knife_", e -> new ItemEv(propPartTool()));
        ROCKS = makeBlock(RockVariant.class, EvolutionBlocks.ROCKS, e -> new ItemRock(e.get(EvolutionBlocks.ROCKS), propMisc(), e));
        SANDS = makeBlock(RockVariant.class, EvolutionBlocks.SANDS, e -> itemBlock(e.get(EvolutionBlocks.SANDS)));
        SAPLINGS = makeBlock(WoodVariant.class, EvolutionBlocks.SAPLINGS, e -> woodBlock(e.get(EvolutionBlocks.SAPLINGS)));
        STONEBRICKS = makeBlock(RockVariant.class, EvolutionBlocks.STONEBRICKS, e -> itemBlock(e.get(EvolutionBlocks.STONEBRICKS)));
        STONES = makeBlock(RockVariant.class, EvolutionBlocks.STONES, e -> itemBlock(e.get(EvolutionBlocks.STONES)));
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

//    @Contract
//    public static <E extends Entity> Item genEgg(EntityType<E> type) {
//        return new ItemSpawnEgg<>(type);
//    }

    @Contract("_ -> new")
    private static Item item(Item.Properties prop) {
        return new ItemEv(prop);
    }

    @Contract("_ -> new")
    private static Item itemBlock(Block block) {
        return new ItemBlock(block, propMisc());
    }

    @Contract("_, _, _, _ -> new")
    private static <E extends Enum<E> & IVariant, I extends Item> Map<E, I> make(Class<E> clazz, E[] values, String name, Function<E, I> itemMaker) {
        Map<E, I> map = new EnumMap<>(clazz);
        for (E e : values) {
            //noinspection ObjectAllocationInLoop
            map.put(e, register(name + e.getName(), itemMaker.apply(e)));
        }
        return Maps.immutableEnumMap(map);
    }

    private static <I extends Item> I makeBlock(Block block, I item) {
        return register(Registry.BLOCK.getKey(block), item);
    }

    @Contract("_, _, _ -> new")
    private static <E extends Enum<E> & IVariant, B extends Block, I extends Item> Map<E, I> makeBlock(Class<E> clazz,
                                                                                                       Map<E, B> blocks,
                                                                                                       Function<E, I> itemMaker) {
        Map<E, I> map = new EnumMap<>(clazz);
        for (Map.Entry<E, B> entry : blocks.entrySet()) {
            E e = entry.getKey();
            map.put(e, register(Registry.BLOCK.getKey(entry.getValue()), itemMaker.apply(e)));
        }
        return Maps.immutableEnumMap(map);
    }

    @Contract(" -> new")
    private static Item.Properties propDev() {
        return new Item.Properties().tab(EvolutionCreativeTabs.DEV);
    }

//    @Contract(pure = true, value = " -> new")
//    private static Item.Properties propLiquid() {
//        return new Item.Properties().tab(EvolutionCreativeTabs.LIQUIDS);
//    }

    @Contract(" -> new")
    private static Item.Properties propMetal() {
        return new Item.Properties().tab(EvolutionCreativeTabs.METAL);
    }

    @Contract(" -> new")
    private static Item.Properties propMisc() {
        return new Item.Properties().tab(EvolutionCreativeTabs.MISC);
    }

    @Contract(" -> new")
    private static Item.Properties propPartTool() {
        return new Item.Properties().tab(EvolutionCreativeTabs.PARTS_AND_TOOLS);
    }

    @Contract(" -> new")
    private static Item.Properties propTreesAndWood() {
        return new Item.Properties().tab(EvolutionCreativeTabs.TREES_AND_WOOD);
    }

    private static <I extends Item> I register(String name, I item) {
        return register(Evolution.getResource(name), item);
    }

    private static <I extends Item> I register(ResourceLocation loc, I item) {
        if (item instanceof ItemBlock i) {
            i.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(Registry.ITEM, loc, item);
    }

    public static void register() {
        //Items are registered via class-loading.
    }

    @Contract(" -> new")
    private static Item wood() {
        return new ItemEv(propTreesAndWood());
    }

    @Contract("_ -> new")
    private static Item woodBlock(Block block) {
        return new ItemBlock(block, propTreesAndWood());
    }

}
