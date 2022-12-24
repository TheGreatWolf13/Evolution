package tgw.evolution.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionFluids;

public enum EvolutionRenderLayer {
    CUTOUT,
    CUTOUT_MIPPED,
    LEAVES,
    TRANSLUCENT;

    private static void set(RegistryObject<? extends Block> block, EvolutionRenderLayer layer) {
        switch (layer) {
            case CUTOUT -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout());
            case CUTOUT_MIPPED -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped());
            case LEAVES -> ItemBlockRenderTypes.setRenderLayer(block.get(), renderType -> {
                if (!Minecraft.useFancyGraphics()) {
                    return renderType == RenderType.solid();
                }
                return renderType == RenderType.cutoutMipped();
            });
            case TRANSLUCENT -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.translucent());
        }
    }

    private static void setFluid(RegistryObject<? extends Fluid> fluid, EvolutionRenderLayer layer) {
        switch (layer) {
            case CUTOUT -> ItemBlockRenderTypes.setRenderLayer(fluid.get(), RenderType.cutout());
            case CUTOUT_MIPPED -> ItemBlockRenderTypes.setRenderLayer(fluid.get(), RenderType.cutoutMipped());
            case TRANSLUCENT -> ItemBlockRenderTypes.setRenderLayer(fluid.get(), RenderType.translucent());
        }
    }

    public static void setup() {
        setFluid(EvolutionFluids.FRESH_WATER, TRANSLUCENT);
        setFluid(EvolutionFluids.SALT_WATER, TRANSLUCENT);
        set(EvolutionBlocks.CLIMBING_HOOK, CUTOUT);
        set(EvolutionBlocks.CLIMBING_STAKE, CUTOUT);
        for (RegistryObject<Block> block : EvolutionBlocks.ALL_DRY_GRASS.values()) {
            set(block, CUTOUT_MIPPED);
        }
        set(EvolutionBlocks.FIRE, CUTOUT);
        set(EvolutionBlocks.GRASS, CUTOUT);
        for (RegistryObject<Block> block : EvolutionBlocks.ALL_GRASS.values()) {
            set(block, CUTOUT_MIPPED);
        }
        set(EvolutionBlocks.GROUND_ROPE, CUTOUT_MIPPED);
        for (RegistryObject<Block> block : EvolutionBlocks.ALL_LEAVES.values()) {
            set(block, LEAVES);
        }
        set(EvolutionBlocks.ROPE, CUTOUT_MIPPED);
        for (RegistryObject<Block> block : EvolutionBlocks.ALL_SAPLING.values()) {
            set(block, CUTOUT);
        }
        set(EvolutionBlocks.TALLGRASS, CUTOUT);
        set(EvolutionBlocks.TORCH, CUTOUT);
        set(EvolutionBlocks.WALL_TORCH, CUTOUT);
        set(EvolutionBlocks.GLASS, CUTOUT);
    }
}
