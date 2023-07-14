package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

@Mixin(BrightnessCombiner.class)
public abstract class MixinBrightnessCombiner<S extends BlockEntity> {

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public Int2IntFunction acceptDouble(S first, S second) {
        return light -> {
            //noinspection ConstantConditions
            int i = EvLevelRenderer.getLightColor(first.getLevel(), first.getBlockPos());
            //noinspection ConstantConditions
            int j = EvLevelRenderer.getLightColor(second.getLevel(), second.getBlockPos());
            int k = LightTexture.block(i);
            int l = LightTexture.block(j);
            int i1 = LightTexture.sky(i);
            int j1 = LightTexture.sky(j);
            return LightTexture.pack(Math.max(k, l), Math.max(i1, j1));
        };
    }
}
