package tgw.evolution.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.collection.maps.R2BHashMap;
import tgw.evolution.util.collection.maps.R2BMap;

public enum RenderLayer {
    SOLID,
    CUTOUT,
    CUTOUT_MIPPED,
    LEAVES,
    TRANSLUCENT,
    TRIPWIRE;

    public static final RenderLayer[] VALUES = values();
    private static final R2BMap<Block> BLOCK_LAYERS = new R2BHashMap<>();
    private static final R2BMap<Fluid> FLUID_LAYERS = new R2BHashMap<>();

    public static RenderType get(Block block, boolean fancy) {
        return get(VALUES[BLOCK_LAYERS.getByte(block)], fancy);
    }

    private static RenderType get(RenderLayer layer, boolean fancy) {
        return switch (layer) {
            case SOLID -> RenderType.solid();
            case CUTOUT -> RenderType.cutout();
            case CUTOUT_MIPPED -> RenderType.cutoutMipped();
            case TRANSLUCENT -> RenderType.translucent();
            case TRIPWIRE -> RenderType.tripwire();
            case LEAVES -> fancy ? RenderType.cutoutMipped() : RenderType.solid();
        };
    }

    public static RenderType get(Fluid fluid, boolean fancy) {
        return get(VALUES[FLUID_LAYERS.getByte(fluid)], fancy);
    }

    public static void set(Fluid fluid, RenderLayer layer) {
        FLUID_LAYERS.put(fluid, (byte) layer.ordinal());
    }

    public static void set(Block block, RenderLayer layer) {
        BLOCK_LAYERS.put(block, (byte) layer.ordinal());
    }

    public static void setup() {
        set(EvolutionBlocks.ATM, CUTOUT);
//        set(EvolutionFluids.FRESH_WATER, TRANSLUCENT);
//        set(EvolutionFluids.SALT_WATER, TRANSLUCENT);
        set(EvolutionBlocks.CLIMBING_HOOK, CUTOUT);
        set(EvolutionBlocks.CLIMBING_STAKE, CUTOUT);
        for (Block block : EvolutionBlocks.DRY_GRASSES.values()) {
            set(block, CUTOUT);
        }
        set(EvolutionBlocks.FIRE, CUTOUT);
        set(EvolutionBlocks.TALLGRASS, CUTOUT);
        for (Block block : EvolutionBlocks.GRASSES.values()) {
            set(block, CUTOUT_MIPPED);
        }
        set(EvolutionBlocks.ROPE_GROUND, CUTOUT_MIPPED);
        for (Block block : EvolutionBlocks.LEAVES.values()) {
            set(block, LEAVES);
        }
        set(EvolutionBlocks.ROPE, CUTOUT_MIPPED);
        for (Block block : EvolutionBlocks.SAPLINGS.values()) {
            set(block, CUTOUT);
        }
        set(EvolutionBlocks.TALLGRASS_HIGH, CUTOUT);
        set(EvolutionBlocks.TORCH, CUTOUT);
        set(EvolutionBlocks.TORCH_WALL, CUTOUT);
        set(EvolutionBlocks.GLASS, CUTOUT);
    }
}
