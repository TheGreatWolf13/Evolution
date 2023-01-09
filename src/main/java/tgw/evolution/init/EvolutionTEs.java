package tgw.evolution.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.*;

import java.util.Collection;

import static tgw.evolution.init.EvolutionBlocks.*;

@SuppressWarnings("ConstantConditions")
public final class EvolutionTEs {

    public static final RegistryObject<BlockEntityType<TEChopping>> CHOPPING;
    public static final RegistryObject<BlockEntityType<TEFirewoodPile>> FIREWOOD_PILE;
    public static final RegistryObject<BlockEntityType<TEKnapping>> KNAPPING;
    public static final RegistryObject<BlockEntityType<TEMetal>> METAL;
    public static final RegistryObject<BlockEntityType<TEMolding>> MOLDING;
    public static final RegistryObject<BlockEntityType<TEPitKiln>> PIT_KILN;
    public static final RegistryObject<BlockEntityType<TEPuzzle>> PUZZLE;
    public static final RegistryObject<BlockEntityType<TESchematic>> SCHEMATIC;
    public static final RegistryObject<BlockEntityType<TETorch>> TORCH;

    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Evolution.MODID);

    static {
        CHOPPING = TILES.register("te_chopping", () -> Builder.of(TEChopping::new, get(CHOPPING_BLOCKS.values())).build(null));
        FIREWOOD_PILE = TILES.register("te_firewood_pile", () -> Builder.of(TEFirewoodPile::new, EvolutionBlocks.FIREWOOD_PILE.get()).build(null));
        KNAPPING = TILES.register("te_knapping", () -> Builder.of(TEKnapping::new, get(KNAPPING_BLOCKS.values())).build(null));
        METAL = TILES.register("te_metal", () -> Builder.of(TEMetal::new, new Block[]{BLOCK_METAL_COPPER.get(),
                                                                                      BLOCK_METAL_COPPER_E.get(),
                                                                                      BLOCK_METAL_COPPER_W.get()}).build(null));
        MOLDING = TILES.register("te_molding", () -> Builder.of(TEMolding::new, MOLDING_BLOCK.get()).build(null));
        PIT_KILN = TILES.register("te_pit_kiln", () -> Builder.of(TEPitKiln::new, EvolutionBlocks.PIT_KILN.get()).build(null));
        PUZZLE = TILES.register("te_puzzle", () -> Builder.of(TEPuzzle::new, EvolutionBlocks.PUZZLE.get()).build(null));
        SCHEMATIC = TILES.register("te_schematic", () -> Builder.of(TESchematic::new, SCHEMATIC_BLOCK.get()).build(null));
        TORCH = TILES.register("te_torch", () -> Builder.of(TETorch::new, EvolutionBlocks.TORCH.get(), TORCH_WALL.get()).build(null));
    }

    private EvolutionTEs() {
    }

    @Contract("_ -> new")
    public static Block[] get(Collection<RegistryObject<Block>> collection) {
        Block[] blocks = new Block[collection.size()];
        int i = 0;
        for (RegistryObject<Block> r : collection) {
            blocks[i++] = r.get();
        }
        return blocks;
    }

    public static void register() {
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
