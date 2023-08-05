package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import org.jetbrains.annotations.Contract;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.*;

import java.util.Collection;

import static tgw.evolution.init.EvolutionBlocks.*;

@SuppressWarnings("ConstantConditions")
public final class EvolutionTEs {

    public static final BlockEntityType<TEChopping> CHOPPING;
    public static final BlockEntityType<TEFirewoodPile> FIREWOOD_PILE;
    public static final BlockEntityType<TEKnapping> KNAPPING;
    public static final BlockEntityType<TEMetal> METAL;
    public static final BlockEntityType<TEMolding> MOLDING;
    public static final BlockEntityType<TEPitKiln> PIT_KILN;
    public static final BlockEntityType<TEPuzzle> PUZZLE;
    public static final BlockEntityType<TESchematic> SCHEMATIC;
    public static final BlockEntityType<TETorch> TORCH;

    static {
        CHOPPING = register("te_chopping", Builder.of(TEChopping::new, get(CHOPPING_BLOCKS.values())).build(null));
        FIREWOOD_PILE = register("te_firewood_pile", Builder.of(TEFirewoodPile::new, EvolutionBlocks.FIREWOOD_PILE).build(null));
        KNAPPING = register("te_knapping", Builder.of(TEKnapping::new, get(KNAPPING_BLOCKS.values())).build(null));
        METAL = register("te_metal",
                         Builder.of(TEMetal::new, new Block[]{BLOCK_METAL_COPPER, BLOCK_METAL_COPPER_E, BLOCK_METAL_COPPER_W}).build(null));
        MOLDING = register("te_molding", Builder.of(TEMolding::new, MOLDING_BLOCK).build(null));
        PIT_KILN = register("te_pit_kiln", Builder.of(TEPitKiln::new, EvolutionBlocks.PIT_KILN).build(null));
        PUZZLE = register("te_puzzle", Builder.of(TEPuzzle::new, EvolutionBlocks.PUZZLE).build(null));
        SCHEMATIC = register("te_schematic", Builder.of(TESchematic::new, SCHEMATIC_BLOCK).build(null));
        TORCH = register("te_torch", Builder.of(TETorch::new, EvolutionBlocks.TORCH, TORCH_WALL).build(null));
    }

    private EvolutionTEs() {
    }

    @Contract("_ -> new")
    public static Block[] get(Collection<Block> collection) {
        Block[] blocks = new Block[collection.size()];
        int i = 0;
        for (Block r : collection) {
            blocks[i++] = r;
        }
        return blocks;
    }

    public static void register() {
        //BlockEntities are registered via class-loading.
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, Evolution.getResource(name), type);
    }
}
