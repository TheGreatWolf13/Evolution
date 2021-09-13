package tgw.evolution.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;

public enum EvolutionRenderLayer {
    CUTOUT,
    CUTOUT_MIPPED,
    TRANSLUCENT;

    private static void set(RegistryObject<? extends Block> block, EvolutionRenderLayer layer) {
        switch (layer) {
            case CUTOUT: {
                RenderTypeLookup.setRenderLayer(block.get(), RenderType.cutout());
                break;
            }
            case CUTOUT_MIPPED: {
                RenderTypeLookup.setRenderLayer(block.get(), RenderType.cutoutMipped());
                break;
            }
            case TRANSLUCENT: {
                RenderTypeLookup.setRenderLayer(block.get(), RenderType.translucent());
                break;
            }
        }
    }

    private static void setFluid(RegistryObject<? extends Fluid> fluid, EvolutionRenderLayer layer) {
        switch (layer) {
            case CUTOUT: {
                RenderTypeLookup.setRenderLayer(fluid.get(), RenderType.cutout());
                break;
            }
            case CUTOUT_MIPPED: {
                RenderTypeLookup.setRenderLayer(fluid.get(), RenderType.cutoutMipped());
                break;
            }
            case TRANSLUCENT: {
                RenderTypeLookup.setRenderLayer(fluid.get(), RenderType.translucent());
                break;
            }
        }
    }

    public static void setup() {
        setFluid(EvolutionFluids.FRESH_WATER, TRANSLUCENT);
        setFluid(EvolutionFluids.SALT_WATER, TRANSLUCENT);
        set(EvolutionBlocks.CLIMBING_HOOK, CUTOUT);
        set(EvolutionBlocks.CLIMBING_STAKE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_ANDESITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_BASALT, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_CHALK, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_CHERT, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_CONGLOMERATE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_DACITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_DIORITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_DOLOMITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_GABBRO, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_GNEISS, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_GRANITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_LIMESTONE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_MARBLE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_PHYLLITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_QUARTZITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_RED_SANDSTONE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_SANDSTONE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_SCHIST, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_SHALE, CUTOUT_MIPPED);
        set(EvolutionBlocks.DRY_GRASS_SLATE, CUTOUT_MIPPED);
        set(EvolutionBlocks.FIRE, CUTOUT);
        set(EvolutionBlocks.GRASS, CUTOUT);
        set(EvolutionBlocks.GRASS_ANDESITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_BASALT, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_CHALK, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_CHERT, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_CONGLOMERATE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_DACITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_DIORITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_DOLOMITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_GABBRO, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_GNEISS, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_GRANITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_LIMESTONE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_MARBLE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_PHYLLITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_QUARTZITE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_RED_SANDSTONE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_SANDSTONE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_SCHIST, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_SHALE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GRASS_SLATE, CUTOUT_MIPPED);
        set(EvolutionBlocks.GROUND_ROPE, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_ACACIA, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_ASPEN, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_BIRCH, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_CEDAR, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_EBONY, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_ELM, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_EUCALYPTUS, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_FIR, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_KAPOK, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_MANGROVE, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_MAPLE, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_OAK, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_OLD_OAK, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_PALM, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_PINE, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_REDWOOD, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_SPRUCE, CUTOUT_MIPPED);
        set(EvolutionBlocks.LEAVES_WILLOW, CUTOUT_MIPPED);
        set(EvolutionBlocks.ROPE, CUTOUT_MIPPED);
        set(EvolutionBlocks.SAPLING_ACACIA, CUTOUT);
        set(EvolutionBlocks.SAPLING_ASPEN, CUTOUT);
        set(EvolutionBlocks.SAPLING_BIRCH, CUTOUT);
        set(EvolutionBlocks.SAPLING_CEDAR, CUTOUT);
        set(EvolutionBlocks.SAPLING_EBONY, CUTOUT);
        set(EvolutionBlocks.SAPLING_ELM, CUTOUT);
        set(EvolutionBlocks.SAPLING_EUCALYPTUS, CUTOUT);
        set(EvolutionBlocks.SAPLING_FIR, CUTOUT);
        set(EvolutionBlocks.SAPLING_KAPOK, CUTOUT);
        set(EvolutionBlocks.SAPLING_MANGROVE, CUTOUT);
        set(EvolutionBlocks.SAPLING_MAPLE, CUTOUT);
        set(EvolutionBlocks.SAPLING_OAK, CUTOUT);
        set(EvolutionBlocks.SAPLING_OLD_OAK, CUTOUT);
        set(EvolutionBlocks.SAPLING_PALM, CUTOUT);
        set(EvolutionBlocks.SAPLING_PINE, CUTOUT);
        set(EvolutionBlocks.SAPLING_REDWOOD, CUTOUT);
        set(EvolutionBlocks.SAPLING_SPRUCE, CUTOUT);
        set(EvolutionBlocks.SAPLING_WILLOW, CUTOUT);
        set(EvolutionBlocks.TALLGRASS, CUTOUT);
        set(EvolutionBlocks.TORCH, CUTOUT);
        set(EvolutionBlocks.WALL_TORCH, CUTOUT);
    }
}
