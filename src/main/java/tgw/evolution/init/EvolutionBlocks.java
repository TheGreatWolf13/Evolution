package tgw.evolution.init;

import com.google.common.collect.Maps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
import java.util.function.Supplier;

@SuppressWarnings({"unused", "ObjectAllocationInLoop"})
public final class EvolutionBlocks {

    //Temporary
    public static final RegistryObject<Block> GLASS;
    public static final RegistryObject<Block> PLACEHOLDER_BLOCK;
    //Dev
    public static final RegistryObject<Block> ATM;
    public static final RegistryObject<Block> DESTROY_3;
    public static final RegistryObject<Block> DESTROY_6;
    public static final RegistryObject<Block> DESTROY_9;
    public static final RegistryObject<Block> PUZZLE;
    public static final RegistryObject<Block> SCHEMATIC_BLOCK;
    //Independent
    public static final RegistryObject<Block> BLOCK_METAL_COPPER;
    public static final RegistryObject<Block> BLOCK_METAL_COPPER_E;
    public static final RegistryObject<Block> BLOCK_METAL_COPPER_W;
    public static final RegistryObject<Block> BLOCK_METAL_COPPER_O;
    public static final RegistryObject<Block> BRICK_CLAY;
    public static final RegistryObject<Block> CLAY;
    public static final RegistryObject<Block> CLIMBING_HOOK;
    public static final RegistryObject<Block> CLIMBING_STAKE;
    public static final RegistryObject<Block> CRUCIBLE_CLAY;
    public static final RegistryObject<BlockFire> FIRE;
    public static final RegistryObject<Block> FIREWOOD_PILE;
    public static final RegistryObject<Block> GRASS;
    public static final RegistryObject<Block> MOLD_CLAY_AXE;
    public static final RegistryObject<Block> MOLD_CLAY_GUARD;
    public static final RegistryObject<Block> MOLD_CLAY_HAMMER;
    public static final RegistryObject<Block> MOLD_CLAY_HOE;
    public static final RegistryObject<Block> MOLD_CLAY_INGOT;
    public static final RegistryObject<Block> MOLD_CLAY_KNIFE;
    public static final RegistryObject<Block> MOLD_CLAY_PICKAXE;
    public static final RegistryObject<Block> MOLD_CLAY_SAW;
    public static final RegistryObject<Block> MOLD_CLAY_SHOVEL;
    public static final RegistryObject<Block> MOLD_CLAY_SPEAR;
    public static final RegistryObject<Block> MOLD_CLAY_SWORD;
    public static final RegistryObject<Block> MOLDING_BLOCK;
    public static final RegistryObject<Block> PEAT;
    public static final RegistryObject<Block> PIT_KILN;
    public static final RegistryObject<Block> ROPE;
    public static final RegistryObject<Block> ROPE_GROUND;
    public static final RegistryObject<Block> STICK;
    public static final RegistryObject<Block> TALLGRASS;
    public static final RegistryObject<Block> TORCH;
    public static final RegistryObject<Block> TORCH_WALL;
    //Collections
    public static final Map<WoodVariant, RegistryObject<Block>> CHOPPING_BLOCKS;
    public static final Map<RockVariant, RegistryObject<Block>> COBBLESTONES;
    public static final Map<RockVariant, RegistryObject<Block>> DIRTS;
    public static final Map<RockVariant, RegistryObject<Block>> DRY_GRASSES;
    public static final Map<RockVariant, RegistryObject<Block>> GRASSES;
    public static final Map<RockVariant, RegistryObject<Block>> GRAVELS;
    public static final Map<RockVariant, RegistryObject<Block>> KNAPPING_BLOCKS;
    public static final Map<WoodVariant, RegistryObject<Block>> LEAVES;
    public static final Map<WoodVariant, RegistryObject<Block>> LOGS;
    public static final Map<WoodVariant, RegistryObject<Block>> PLANKS;
    public static final Map<RockVariant, RegistryObject<Block>> POLISHED_STONES;
    public static final Map<RockVariant, RegistryObject<Block>> ROCKS;
    public static final Map<RockVariant, RegistryObject<Block>> SANDS;
    public static final Map<WoodVariant, RegistryObject<Block>> SAPLINGS;
    public static final Map<RockVariant, RegistryObject<Block>> STONE_BRICKS;
    public static final Map<RockVariant, RegistryObject<Block>> STONES;
    //Fluids
    public static final RegistryObject<BlockGenericFluid> FRESH_WATER;
    public static final RegistryObject<BlockGenericFluid> SALT_WATER;
    //
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Evolution.MODID);

    static {
        //Temporary Blocks used for testing
        PLACEHOLDER_BLOCK = BLOCKS.register("placeholder_block", BlockPlaceholder::new);
        GLASS = BLOCKS.register("glass", BlockGlass::new);
        //Dev Blocks
        ATM = BLOCKS.register("atm", BlockAtm::new);
        DESTROY_3 = BLOCKS.register("destroy_3", () -> new GlassBlock(Block.Properties.of(Material.AIR).noDrops()));
        DESTROY_6 = BLOCKS.register("destroy_6", () -> new GlassBlock(Block.Properties.of(Material.AIR).noDrops()));
        DESTROY_9 = BLOCKS.register("destroy_9", () -> new GlassBlock(Block.Properties.of(Material.AIR).noDrops()));
        PUZZLE = BLOCKS.register("puzzle", BlockPuzzle::new);
        SCHEMATIC_BLOCK = BLOCKS.register("schematic_block", BlockSchematic::new);
        //Independent
        BLOCK_METAL_COPPER = BLOCKS.register("block_metal_copper", () -> new BlockMetal(MetalVariant.COPPER, Oxidation.NONE));
        BLOCK_METAL_COPPER_E = BLOCKS.register("block_metal_copper_exposed", () -> new BlockMetal(MetalVariant.COPPER, Oxidation.EXPOSED));
        BLOCK_METAL_COPPER_W = BLOCKS.register("block_metal_copper_weathered", () -> new BlockMetal(MetalVariant.COPPER, Oxidation.WEATHERED));
        BLOCK_METAL_COPPER_O = BLOCKS.register("block_metal_copper_oxidized", () -> new BlockMetal(MetalVariant.COPPER, Oxidation.OXIDIZED));
        BRICK_CLAY = BLOCKS.register("brick_clay", () -> new BlockMoldClay(Block.box(2, 0, 5, 14, 6, 11)));
        CLAY = BLOCKS.register("clay", BlockClay::new);
        CLIMBING_HOOK = BLOCKS.register("climbing_hook", BlockClimbingHook::new);
        CLIMBING_STAKE = BLOCKS.register("climbing_stake", BlockClimbingStake::new);
        CRUCIBLE_CLAY = BLOCKS.register("crucible_clay", () -> new BlockMoldClay(5));
        FIRE = BLOCKS.register("fire", BlockFire::new);
        FIREWOOD_PILE = BLOCKS.register("firewood_pile", BlockFirewoodPile::new);
        GRASS = BLOCKS.register("grass", BlockTallGrass::new);
        MOLD_CLAY_AXE = BLOCKS.register("mold_clay_axe", () -> new BlockMoldClay(1));
        MOLD_CLAY_GUARD = BLOCKS.register("mold_clay_guard", () -> new BlockMoldClay(1));
        MOLD_CLAY_HAMMER = BLOCKS.register("mold_clay_hammer", () -> new BlockMoldClay(1));
        MOLD_CLAY_HOE = BLOCKS.register("mold_clay_hoe", () -> new BlockMoldClay(1));
        MOLD_CLAY_INGOT = BLOCKS.register("mold_clay_ingot", () -> new BlockMoldClay(1));
        MOLD_CLAY_KNIFE = BLOCKS.register("mold_clay_knife", () -> new BlockMoldClay(1));
        MOLD_CLAY_PICKAXE = BLOCKS.register("mold_clay_pickaxe", () -> new BlockMoldClay(1));
        MOLD_CLAY_SAW = BLOCKS.register("mold_clay_saw", () -> new BlockMoldClay(1));
        MOLD_CLAY_SHOVEL = BLOCKS.register("mold_clay_shovel", () -> new BlockMoldClay(1));
        MOLD_CLAY_SPEAR = BLOCKS.register("mold_clay_spear", () -> new BlockMoldClay(1));
        MOLD_CLAY_SWORD = BLOCKS.register("mold_clay_sword", () -> new BlockMoldClay(1));
        MOLDING_BLOCK = BLOCKS.register("molding_block", BlockMolding::new);
        PEAT = BLOCKS.register("peat", BlockPeat::new);
        PIT_KILN = BLOCKS.register("pit_kiln", BlockPitKiln::new);
        ROPE = BLOCKS.register("rope", BlockRope::new);
        ROPE_GROUND = BLOCKS.register("rope_ground", BlockRopeGround::new);
        STICK = BLOCKS.register("stick", () -> new BlockPlaceableItem(Block.Properties.of(Material.DECORATION).sound(SoundType.WOOD)));
        TALLGRASS = BLOCKS.register("tallgrass", () -> BlockDoublePlant.make(false));
        TORCH = BLOCKS.register("torch", BlockTorch::new);
        TORCH_WALL = BLOCKS.register("torch_wall", BlockTorchWall::new);
        //Collection
        CHOPPING_BLOCKS = make(WoodVariant.class, WoodVariant.VALUES, "chopping_block_", e -> () -> new BlockChopping(e));
        COBBLESTONES = make(RockVariant.class, RockVariant.VALUES_STONE, "cobblestone_", e -> () -> new BlockCobblestone(e));
        DIRTS = make(RockVariant.class, RockVariant.VALUES_STONE, "dirt_", e -> () -> new BlockDirt(e));
        DRY_GRASSES = make(RockVariant.class, RockVariant.VALUES_STONE, "dry_grass_", e -> () -> new BlockDryGrass(e));
        GRASSES = make(RockVariant.class, RockVariant.VALUES, "grass_", e -> () -> new BlockGrass(e));
        GRAVELS = make(RockVariant.class, RockVariant.VALUES_STONE, "gravel_", e -> () -> new BlockGravel(e));
        KNAPPING_BLOCKS = make(RockVariant.class, RockVariant.VALUES_STONE, "knapping_block_", e -> () -> new BlockKnapping(e));
        LEAVES = make(WoodVariant.class, WoodVariant.VALUES, "leaves_", e -> BlockLeaves::new);
        LOGS = make(WoodVariant.class, WoodVariant.VALUES, "log_", e -> () -> new BlockLog(e));
        PLANKS = make(WoodVariant.class, WoodVariant.VALUES, "planks_", e -> () -> new BlockPlanks(e));
        POLISHED_STONES = make(RockVariant.class, RockVariant.VALUES_STONE, "polished_stone_", e -> () -> new BlockPolishedStone(e));
        ROCKS = make(RockVariant.class, RockVariant.VALUES_STONE, "rock_", e -> BlockPlaceableRock::new);
        SANDS = make(RockVariant.class, RockVariant.VALUES_STONE, "sand_", e -> () -> new BlockSand(e));
        SAPLINGS = make(WoodVariant.class, WoodVariant.VALUES, "sapling_", e -> () -> new BlockSapling(null));
        STONE_BRICKS = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_bricks_", e -> () -> new BlockStoneBricks(e));
        STONES = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_", e -> () -> new BlockStone(e));
        //Fluids
        FRESH_WATER = BLOCKS.register("fresh_water", BlockFreshWater::new);
        SALT_WATER = BLOCKS.register("salt_water", BlockSaltWater::new);
    }

    private EvolutionBlocks() {
    }

    @Contract("_, _, _, _ -> new")
    private static <E extends Enum<E> & IVariant> Map<E, RegistryObject<Block>> make(Class<E> clazz,
                                                                                     E[] values,
                                                                                     String name,
                                                                                     Function<E, Supplier<Block>> block) {
        Map<E, RegistryObject<Block>> map = new EnumMap<>(clazz);
        for (E e : values) {
            map.put(e, BLOCKS.register(name + e.getName(), block.apply(e)));
        }
        return Maps.immutableEnumMap(map);
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}	
