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

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Evolution.MODID);

    //Placeholder
    public static final RegistryObject<Block> PLACEHOLDER_BLOCK = BLOCKS.register("placeholder_block", BlockPlaceholder::new);
    public static final RegistryObject<Block> GLASS = BLOCKS.register("glass", BlockGlass::new);
    //Stick
    public static final RegistryObject<Block> STICK = BLOCKS.register("stick", () -> new BlockPlaceableItem(
            Block.Properties.of(Material.DECORATION).sound(SoundType.WOOD)));
    //Stone
    public static final Map<RockVariant, RegistryObject<Block>> ALL_STONE = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_",
                                                                                 e -> () -> new BlockStone(e));
    //Cobblestone
    public static final Map<RockVariant, RegistryObject<Block>> ALL_COBBLE = make(RockVariant.class, RockVariant.VALUES_STONE, "cobble_",
                                                                                  e -> () -> new BlockCobblestone(e));
    //Polished Stones
    public static final Map<RockVariant, RegistryObject<Block>> ALL_POLISHED_STONE = make(RockVariant.class, RockVariant.VALUES_STONE,
                                                                                          "polished_stone_", e -> () -> new BlockPolishedStone(e));
    //Rocks
    public static final Map<RockVariant, RegistryObject<Block>> ALL_ROCK = make(RockVariant.class, RockVariant.VALUES_STONE, "rock_",
                                                                                e -> BlockPlaceableRock::new);
    //Knapping
    public static final Map<RockVariant, RegistryObject<Block>> ALL_KNAPPING = make(RockVariant.class, RockVariant.VALUES_STONE, "knapping_block_",
                                                                                    e -> () -> new BlockKnapping(e, e.getMass() / 4));
    //Sand
    public static final Map<RockVariant, RegistryObject<Block>> ALL_SAND = make(RockVariant.class, RockVariant.VALUES_STONE, "sand_",
                                                                                e -> () -> new BlockSand(e));
    //Dirt
    public static final Map<RockVariant, RegistryObject<Block>> ALL_DIRT = make(RockVariant.class, RockVariant.VALUES_STONE, "dirt_",
                                                                                e -> () -> new BlockDirt(e));
    //Gravel
    public static final Map<RockVariant, RegistryObject<Block>> ALL_GRAVEL = make(RockVariant.class, RockVariant.VALUES_STONE, "gravel_",
                                                                                  e -> () -> new BlockGravel(e));
    //Grass
    public static final Map<RockVariant, RegistryObject<Block>> ALL_GRASS = make(RockVariant.class, RockVariant.VALUES, "grass_",
                                                                                 e -> () -> new BlockGrass(e));
    //Clay
    public static final RegistryObject<Block> CLAY = BLOCKS.register("clay", BlockClay::new);
    //Peat
    public static final RegistryObject<Block> PEAT = BLOCKS.register("peat", BlockPeat::new);
    //Dry Grass
    public static final Map<RockVariant, RegistryObject<Block>> ALL_DRY_GRASS = make(RockVariant.class, RockVariant.VALUES_STONE, "dry_grass_",
                                                                                     e -> () -> new BlockDryGrass(e));
    //Log
    public static final Map<WoodVariant, RegistryObject<Block>> ALL_LOG = make(WoodVariant.class, WoodVariant.VALUES, "log_",
                                                                               e -> () -> new BlockLog(e));
    //Leaves
    public static final Map<WoodVariant, RegistryObject<Block>> ALL_LEAVES = make(WoodVariant.class, WoodVariant.VALUES, "leaves_",
                                                                                  e -> BlockLeaves::new);
    //Sapling
    public static final Map<WoodVariant, RegistryObject<Block>> ALL_SAPLING = make(WoodVariant.class, WoodVariant.VALUES, "sapling_",
                                                                                   e -> () -> new BlockSapling(null));
    //Planks
    public static final Map<WoodVariant, RegistryObject<Block>> ALL_PLANKS = make(WoodVariant.class, WoodVariant.VALUES, "planks_",
                                                                                  e -> () -> new BlockPlanks(e));
    //Firewood Pile
    public static final RegistryObject<Block> FIREWOOD_PILE = BLOCKS.register("firewood_pile", BlockFirewoodPile::new);
    //Torches
    public static final RegistryObject<Block> TORCH = BLOCKS.register("torch", BlockTorch::new);
    public static final RegistryObject<Block> WALL_TORCH = BLOCKS.register("wall_torch", BlockWallTorch::new);
    //Vegetation
    public static final RegistryObject<Block> GRASS = BLOCKS.register("grass", BlockTallGrass::new);
    public static final RegistryObject<Block> TALLGRASS = BLOCKS.register("tallgrass", BlockDoublePlant::new);
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
    public static final RegistryObject<Block> BRICK_CLAY = BLOCKS.register("brick_clay", () -> new BlockMoldClay(Block.box(2, 0, 5, 14, 6, 11)));
    public static final RegistryObject<Block> CRUCIBLE_CLAY = BLOCKS.register("crucible_clay", () -> new BlockMoldClay(5));
    //Metal Blocks
    public static final RegistryObject<Block> BLOCK_METAL_COPPER = BLOCKS.register("block_metal_copper",
                                                                                   () -> metal(MetalVariant.COPPER, Oxidation.NONE));
    public static final RegistryObject<Block> BLOCK_METAL_COPPER_EXP = BLOCKS.register("block_metal_copper_exposed",
                                                                                       () -> metal(MetalVariant.COPPER, Oxidation.EXPOSED));
    public static final RegistryObject<Block> BLOCK_METAL_COPPER_WEAT = BLOCKS.register("block_metal_copper_weathered",
                                                                                        () -> metal(MetalVariant.COPPER, Oxidation.WEATHERED));
    public static final RegistryObject<Block> BLOCK_METAL_COPPER_OXID = BLOCKS.register("block_metal_copper_oxidized",
                                                                                        () -> metal(MetalVariant.COPPER, Oxidation.OXIDIZED));
    //Chopping Blocks
    public static final Map<WoodVariant, RegistryObject<Block>> ALL_CHOPPING_BLOCK = make(WoodVariant.class, WoodVariant.VALUES, "chopping_block_",
                                                                                          e -> () -> new BlockChopping(e));
    //Destroy Blocks
    public static final RegistryObject<Block> DESTROY_3 = BLOCKS.register("destroy_3", () -> new GlassBlock(Block.Properties.of(Material.AIR)));
    public static final RegistryObject<Block> DESTROY_6 = BLOCKS.register("destroy_6", () -> new GlassBlock(Block.Properties.of(Material.AIR)));
    public static final RegistryObject<Block> DESTROY_9 = BLOCKS.register("destroy_9", () -> new GlassBlock(Block.Properties.of(Material.AIR)));

    public static final RegistryObject<Block> PIT_KILN = BLOCKS.register("pit_kiln", BlockPitKiln::new);

    public static final RegistryObject<BlockFire> FIRE = BLOCKS.register("fire", BlockFire::new);
    //Rope
    public static final RegistryObject<Block> ROPE = BLOCKS.register("rope", BlockRope::new);
    public static final RegistryObject<Block> GROUND_ROPE = BLOCKS.register("ground_rope", BlockRopeGround::new);
    public static final RegistryObject<Block> CLIMBING_STAKE = BLOCKS.register("climbing_stake", BlockClimbingStake::new);
    public static final RegistryObject<Block> CLIMBING_HOOK = BLOCKS.register("climbing_hook", BlockClimbingHook::new);
    //Stone Bricks
    public static final Map<RockVariant, RegistryObject<Block>> ALL_STONE_BRICKS = make(RockVariant.class, RockVariant.VALUES_STONE, "stone_bricks_",
                                                                                        e -> () -> new BlockStoneBricks(e));
    //Dev
    public static final RegistryObject<Block> PUZZLE = BLOCKS.register("puzzle", BlockPuzzle::new);
    public static final RegistryObject<Block> SCHEMATIC_BLOCK = BLOCKS.register("schematic_block", BlockSchematic::new);
    //Fluids
    public static final RegistryObject<BlockGenericFluid> FRESH_WATER = BLOCKS.register("fresh_water", BlockFreshWater::new);
    public static final RegistryObject<BlockGenericFluid> SALT_WATER = BLOCKS.register("salt_water", BlockSaltWater::new);

    private EvolutionBlocks() {
    }

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

    private static Block metal(MetalVariant variant, Oxidation oxidation) {
        return new BlockMetal(variant, oxidation);
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}	
