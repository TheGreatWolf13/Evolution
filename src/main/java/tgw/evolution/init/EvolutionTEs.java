package tgw.evolution.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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

    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Evolution.MODID);

    public static final RegistryObject<BlockEntityType<TEChopping>> CHOPPING;
    public static final RegistryObject<BlockEntityType<TEFirewoodPile>> FIREWOOD_PILE;
    public static final RegistryObject<BlockEntityType<TEKnapping>> KNAPPING;
    public static final RegistryObject<BlockEntityType<TELiquid>> LIQUID;
    public static final RegistryObject<BlockEntityType<TELoggable>> LOGGABLE;
    public static final RegistryObject<BlockEntityType<TEMetal>> METAL;
    public static final RegistryObject<BlockEntityType<TEMolding>> MOLDING;
    public static final RegistryObject<BlockEntityType<TEPitKiln>> PIT_KILN;
    public static final RegistryObject<BlockEntityType<TEPuzzle>> PUZZLE;
    public static final RegistryObject<BlockEntityType<TESchematic>> SCHEMATIC;
    public static final RegistryObject<BlockEntityType<TETorch>> TORCH;

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
        return blockList.toArray(new Block[blockList.size()]);
    }

    private static Block[] getMetals() {
        return new Block[]{BLOCK_METAL_COPPER.get(), BLOCK_METAL_COPPER_EXP.get(), BLOCK_METAL_COPPER_WEAT.get()};
    }

    public static void register() {
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
