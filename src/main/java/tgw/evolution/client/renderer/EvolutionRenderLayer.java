package tgw.evolution.client.renderer;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;

//TODO needs to be remade with mixins to account for leaves
public enum EvolutionRenderLayer {
    CUTOUT,
    CUTOUT_MIPPED,
    LEAVES,
    TRANSLUCENT;

    private static void set(Block block, EvolutionRenderLayer layer) {
        switch (layer) {
//            case CUTOUT -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
//            case CUTOUT_MIPPED -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutoutMipped());
//            case LEAVES -> ItemBlockRenderTypes.setRenderLayer(block.get(), renderType -> {
//                if (!Minecraft.useFancyGraphics()) {
//                    return renderType == RenderType.solid();
//                }
//                return renderType == RenderType.cutoutMipped();
//            });
//            case TRANSLUCENT -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
        }
    }

    private static void set(Fluid fluid, EvolutionRenderLayer layer) {
        switch (layer) {
//            case CUTOUT -> BlockRenderLayerMap.INSTANCE.putFluid(fluid, RenderType.cutout());
//            case CUTOUT_MIPPED -> BlockRenderLayerMap.INSTANCE.putFluid(fluid, RenderType.cutoutMipped());
//            case TRANSLUCENT -> BlockRenderLayerMap.INSTANCE.putFluid(fluid, RenderType.translucent());
        }
    }

    public static void setup() {
        set(EvolutionBlocks.ATM, CUTOUT);
        set(EvolutionFluids.FRESH_WATER, TRANSLUCENT);
        set(EvolutionFluids.SALT_WATER, TRANSLUCENT);
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
