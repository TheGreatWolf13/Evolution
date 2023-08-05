package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

@Mixin(LiquidBlockRenderer.class)
public abstract class MixinLiquidBlockRenderer {

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    private int getLightColor(BlockAndTintGetter level, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int light = EvLevelRenderer.getLightColor(level, x, y, z);
        int lightAbove = EvLevelRenderer.getLightColor(level, x, y + 1, z);
        int k = light & 255;
        int l = lightAbove & 255;
        int i1 = light >> 16 & 255;
        int j1 = lightAbove >> 16 & 255;
        return Math.max(k, l) | Math.max(i1, j1) << 16;
    }
}
