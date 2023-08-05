package tgw.evolution.init;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.*;
import tgw.evolution.blocks.fluids.BlockFreshWater;
import tgw.evolution.blocks.fluids.BlockGenericFluid;
import tgw.evolution.blocks.fluids.BlockSaltWater;
import tgw.evolution.util.constants.MetalVariant;
import tgw.evolution.util.constants.Oxidation;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"unused", "ObjectAllocationInLoop"})
public final class EvolutionBlocks {

    //Temporary
    public static final Block GLASS;
    public static final Block PLACEHOLDER_BLOCK;
    //Dev
    public static final Block ATM;
    public static final Block DESTROY_3;
    public static final Block DESTROY_6;
    public static final Block DESTROY_9;
    public static final Block PUZZLE;
    public static final Block SCHEMATIC_BLOCK;
    //Independent
    public static final Block BLOCK_METAL_COPPER;
    public static final Block BLOCK_METAL_COPPER_E;
    public static final Block BLOCK_METAL_COPPER_W;
    public static final Block BLOCK_METAL_COPPER_O;
    public static final Block BRICK_CLAY;
    public static final Block CLAY;
    public static final Block CLIMBING_HOOK;
    public static final Block CLIMBING_STAKE;
    public static final Block CRUCIBLE_CLAY;
    public static final BlockFire FIRE;
    public static final Block FIREWOOD_PILE;
    public static final Block MOLD_CLAY_AXE;
    public static final Block MOLD_CLAY_GUARD;
    public static final Block MOLD_CLAY_HAMMER;
    public static final Block MOLD_CLAY_HOE;
    public static final Block MOLD_CLAY_INGOT;
    public static final Block MOLD_CLAY_KNIFE;
    public static final Block MOLD_CLAY_PICKAXE;
    public static final Block MOLD_CLAY_SAW;
    public static final Block MOLD_CLAY_SHOVEL;
    public static final Block MOLD_CLAY_SPEAR;
    public static final Block MOLD_CLAY_SWORD;
    public static final Block MOLDING_BLOCK;
    //    public static final Block PEAT;
    public static final Block PIT_KILN;
    public static final Block ROPE;
    public static final Block ROPE_GROUND;
    public static final Block STICK;
    public static final Block TALLGRASS;
    public static final Block TALLGRASS_HIGH;
    public static final Block TORCH;
    public static final Block TORCH_WALL;
    //Collections
    public static final Map<WoodVariant, Block> CHOPPING_BLOCKS;
    public static final Map<RockVariant, Block> COBBLESTONES;
    public static final Map<RockVariant, Block> DIRTS;
    public static final Map<RockVariant, Block> DRY_GRASSES;
    public static final Map<RockVariant, Block> GRASSES;
    public static final Map<RockVariant, Block> GRAVELS;
    public static final Map<RockVariant, Block> KNAPPING_BLOCKS;
    public static final Map<WoodVariant, Block> LEAVES;
    public static final Map<WoodVariant, BlockLog> LOGS;
    public static final Map<WoodVariant, Block> PLANKS;
    public static final Map<RockVariant, Block> POLISHED_STONES;
    public static final Map<RockVariant, Block> ROCKS;
    public static final Map<RockVariant, Block> SANDS;
    public static final Map<WoodVariant, Block> SAPLINGS;
    public static final Map<RockVariant, Block> STONEBRICKS;
    public static final Map<RockVariant, Block> STONES;
    //Fluids
    public static final BlockGenericFluid FRESH_WATER;
    public static final BlockGenericFluid SALT_WATER;

    static {
        //Temporary Blocks used for testing
        PLACEHOLDER_BLOCK = register("placeholder_block", new BlockPlaceholder());
        GLASS = register("glass", new BlockGlass());
        //Dev Blocks
        ATM = register("atm", new BlockAtm());
        DESTROY_3 = register("destroy_3", new GlassBlock(Block.Properties.of(Material.AIR).noDrops()));
        DESTROY_6 = register("destroy_6", new GlassBlock(Block.Properties.of(Material.AIR).noDrops()));
        DESTROY_9 = register("destroy_9", new GlassBlock(Block.Properties.of(Material.AIR).noDrops()));
        PUZZLE = register("puzzle", new BlockPuzzle());
        SCHEMATIC_BLOCK = register("schematic_block", new BlockSchematic());
        //Independent
        BLOCK_METAL_COPPER = register("block_metal_copper", new BlockMetal(MetalVariant.COPPER, Oxidation.NONE));
        BLOCK_METAL_COPPER_E = register("block_metal_copper_exposed", new BlockMetal(MetalVariant.COPPER, Oxidation.EXPOSED));
        BLOCK_METAL_COPPER_W = register("block_metal_copper_weathered", new BlockMetal(MetalVariant.COPPER, Oxidation.WEATHERED));
        BLOCK_METAL_COPPER_O = register("block_metal_copper_oxidized", new BlockMetal(MetalVariant.COPPER, Oxidation.OXIDIZED));
        BRICK_CLAY = register("brick_clay", new BlockMoldClay(Block.box(2, 0, 5, 14, 6, 11)));
        CLAY = register("clay", new BlockClay());
        CLIMBING_HOOK = register("climbing_hook", new BlockClimbingHook());
        CLIMBING_STAKE = register("climbing_stake", new BlockClimbingStake());
        CRUCIBLE_CLAY = register("crucible_clay", new BlockMoldClay(5));
        FIRE = register("fire", new BlockFire());
        FIREWOOD_PILE = register("firewood_pile", new BlockFirewoodPile());
        MOLD_CLAY_AXE = register("mold_clay_axe", new BlockMoldClay(1));
        MOLD_CLAY_GUARD = register("mold_clay_guard", new BlockMoldClay(1));
        MOLD_CLAY_HAMMER = register("mold_clay_hammer", new BlockMoldClay(1));
        MOLD_CLAY_HOE = register("mold_clay_hoe", new BlockMoldClay(1));
        MOLD_CLAY_INGOT = register("mold_clay_ingot", new BlockMoldClay(1));
        MOLD_CLAY_KNIFE = register("mold_clay_knife", new BlockMoldClay(1));
        MOLD_CLAY_PICKAXE = register("mold_clay_pickaxe", new BlockMoldClay(1));
        MOLD_CLAY_SAW = register("mold_clay_saw", new BlockMoldClay(1));
        MOLD_CLAY_SHOVEL = register("mold_clay_shovel", new BlockMoldClay(1));
        MOLD_CLAY_SPEAR = register("mold_clay_spear", new BlockMoldClay(1));
        MOLD_CLAY_SWORD = register("mold_clay_sword", new BlockMoldClay(1));
        MOLDING_BLOCK = register("molding_block", new BlockMolding());
//        PEAT = register("peat", new BlockPeat());
        PIT_KILN = register("pit_kiln", new BlockPitKiln());
        ROPE = register("rope", new BlockRope());
        ROPE_GROUND = register("rope_ground", new BlockRopeGround());
        STICK = register("stick", new BlockPlaceableItem(Block.Properties.of(Material.DECORATION).sound(SoundType.WOOD)));
        TALLGRASS = register("tallgrass", new BlockTallGrass());
        TALLGRASS_HIGH = register("tallgrass_high", BlockDoublePlant.make(false));
        TORCH = register("torch", new BlockTorch());
        TORCH_WALL = register("torch_wall", new BlockTorchWall());
        //Collection
        CHOPPING_BLOCKS = make(WoodVariant.class, WoodVariant.VALUES, "chopping_block_", BlockChopping::new);
        COBBLESTONES = make(RockVariant.class, RockVariant.VALUES_STONE, "cobblestone_", BlockCobblestone::new);
        DIRTS = make(RockVariant.class, RockVariant.VALUES_STONE, "dirt_", BlockDirt::new);
        DRY_GRASSES = make(RockVariant.class, RockVariant.VALUES_STONE, "dry_grass_", BlockDryGrass::new);
        GRASSES = make(RockVariant.class, RockVariant.VALUES, "grass_", BlockGrass::new);
        GRAVELS = make(RockVariant.class, RockVariant.VALUES_STONE, "gravel_", BlockGravel::new);
        KNAPPING_BLOCKS = make(RockVariant.class, RockVariant.VALUES_STONE, "knapping_block_", BlockKnapping::new);
        LEAVES = make(WoodVariant.class, WoodVariant.VALUES, "leaves_", e -> new BlockLeaves());
        LOGS = make(WoodVariant.class, WoodVariant.VALUES, "log_", BlockLog::new);
        PLANKS = make(WoodVariant.class, WoodVariant.VALUES, "planks_", BlockPlanks::new);
        POLISHED_STONES = make(RockVariant.class, RockVariant.VALUES_STONE, "polished_stone_", BlockPolishedStone::new);
        ROCKS = make(RockVariant.class, RockVariant.VALUES_STONE, "rock_", e -> new BlockPlaceableRock());
        SANDS = make(RockVariant.class, RockVariant.VALUES_STONE, "sand_", BlockSand::new);
        SAPLINGS = make(WoodVariant.class, WoodVariant.VALUES, "sapling_", e -> new BlockSapling(null));
        STONEBRICKS = make(RockVariant.class, RockVariant.VALUES_STONE, "stonebricks_", BlockStoneBricks::new);
        STONES = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_", BlockStone::new);
        //Fluids
        FRESH_WATER = register("fresh_water", new BlockFreshWater());
        SALT_WATER = register("salt_water", new BlockSaltWater());
    }

    private EvolutionBlocks() {
    }

    @Contract("_, _, _, _ -> new")
    private static <E extends Enum<E> & IVariant, B extends Block> Map<E, B> make(Class<E> clazz,
                                                                                  E[] values,
                                                                                  String name,
                                                                                  Function<E, B> blockMaker) {
        Map<E, B> map = new EnumMap<>(clazz);
        for (E e : values) {
            map.put(e, register(name + e.getName(), blockMaker.apply(e)));
        }
        return Maps.immutableEnumMap(map);
    }

    private static <B extends Block> B register(String name, B block) {
        return Registry.register(Registry.BLOCK, Evolution.getResource(name), block);
    }

    public static void register() {
        //Blocks are registered via class-loading.
    }
}	
