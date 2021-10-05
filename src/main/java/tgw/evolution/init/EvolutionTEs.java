package tgw.evolution.init;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockChopping;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockPlaceableRock;
import tgw.evolution.blocks.tileentities.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static tgw.evolution.init.EvolutionBlocks.*;

public final class EvolutionTEs {

    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Evolution.MODID);

    public static final RegistryObject<TileEntityType<TEChopping>> CHOPPING;
    public static final RegistryObject<TileEntityType<TEFirewoodPile>> FIREWOOD_PILE;
    public static final RegistryObject<TileEntityType<TEKnapping>> KNAPPING;
    public static final RegistryObject<TileEntityType<TELiquid>> LIQUID;
    public static final RegistryObject<TileEntityType<TELoggable>> LOGGABLE;
    public static final RegistryObject<TileEntityType<TEMetal>> METAL;
    public static final RegistryObject<TileEntityType<TEMolding>> MOLDING;
    public static final RegistryObject<TileEntityType<TEPitKiln>> PIT_KILN;
    public static final RegistryObject<TileEntityType<TEPuzzle>> PUZZLE;
    public static final RegistryObject<TileEntityType<TESchematic>> SCHEMATIC;
    public static final RegistryObject<TileEntityType<TETorch>> TORCH;

    static {
        CHOPPING = TILES.register("te_chopping", () -> Builder.of(TEChopping::new, getMatchingBlocks(BlockChopping.class)).build(null));
        FIREWOOD_PILE = TILES.register("te_firewood_pile", () -> Builder.of(TEFirewoodPile::new, EvolutionBlocks.FIREWOOD_PILE.get()).build(null));
        KNAPPING = TILES.register("te_knapping", () -> Builder.of(TEKnapping::new, getMatchingBlocks(BlockKnapping.class)).build(null));
        LIQUID = TILES.register("te_liquid", () -> Builder.of(TELiquid::new, FRESH_WATER.get(), SALT_WATER.get()).build(null));
        LOGGABLE = TILES.register("te_loggable",
                                  () -> Builder.of(TELoggable::new, getMatchingBlocks(BlockPlaceableRock.class, PEAT.get(), STICK.get()))
                                               .build(null));
        METAL = TILES.register("te_metal", () -> Builder.of(TEMetal::new, getMetals()).build(null));
        MOLDING = TILES.register("te_molding", () -> Builder.of(TEMolding::new, EvolutionBlocks.MOLDING.get()).build(null));
        PIT_KILN = TILES.register("te_pit_kiln", () -> Builder.of(TEPitKiln::new, EvolutionBlocks.PIT_KILN.get()).build(null));
        PUZZLE = TILES.register("te_puzzle", () -> Builder.of(TEPuzzle::new, EvolutionBlocks.PUZZLE.get()).build(null));
        SCHEMATIC = TILES.register("te_schematic", () -> Builder.of(TESchematic::new, SCHEMATIC_BLOCK.get()).build(null));
        TORCH = TILES.register("te_torch", () -> Builder.of(TETorch::new, WALL_TORCH.get(), EvolutionBlocks.TORCH.get()).build(null));
    }

    private EvolutionTEs() {
    }

    public static Block[] getMatchingBlocks(Class<? extends Block> clazz) {
        return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block.getClass() == clazz).toArray(Block[]::new);
    }

    public static Block[] getMatchingBlocks(Class<? extends Block> clazz, Block... otherBlocks) {
        List<Block> blockList = ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block.getClass() == clazz).collect(Collectors.toList());
        blockList.addAll(Arrays.asList(otherBlocks));
        return blockList.toArray(new Block[0]);
    }

    private static Block[] getMetals() {
        return new Block[]{BLOCK_METAL_COPPER.get(), BLOCK_METAL_COPPER_EXP.get(), BLOCK_METAL_COPPER_WEAT.get()};
    }

    public static void register() {
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
