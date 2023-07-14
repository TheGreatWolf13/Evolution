package tgw.evolution.mixin;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.maps.R2OHashMap;

import java.util.Map;

@Mixin(ItemBlockRenderTypes.class)
public abstract class MixinItemBlockRenderTypes {

    @Mutable @Shadow @Final private static Map<Block, RenderType> TYPE_BY_BLOCK;
    @Mutable @Shadow @Final private static Map<Fluid, RenderType> TYPE_BY_FLUID;

    static {
        // Replace the backing collection types with something a bit faster, since this is a hot spot in chunk rendering.
        TYPE_BY_BLOCK = new R2OHashMap<>(TYPE_BY_BLOCK);
        TYPE_BY_FLUID = new R2OHashMap<>(TYPE_BY_FLUID);
    }
}
