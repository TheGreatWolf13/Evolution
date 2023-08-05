package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchModelBlockRendererCache;

@Mixin(ModelBlockRenderer.Cache.class)
public abstract class MixinModelBlockRenderer_Cache implements PatchModelBlockRendererCache {

    @Shadow @Final private Long2FloatLinkedOpenHashMap brightnessCache;
    @Shadow @Final private Long2IntLinkedOpenHashMap colorCache;
    @Shadow private boolean enabled;

    @DeleteMethod
    @Overwrite
    public int getLightColor(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public int getLightColor_(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        long packedPos = BlockPos.asLong(x, y, z);
        if (this.enabled) {
            int color = this.colorCache.get(packedPos);
            if (color != Integer.MAX_VALUE) {
                return color;
            }
        }
        int color = EvLevelRenderer.getLightColor(level, state, x, y, z);
        if (this.enabled) {
            if (this.colorCache.size() == 100) {
                this.colorCache.removeFirstInt();
            }
            this.colorCache.put(packedPos, color);
        }
        return color;
    }

    @Overwrite
    @DeleteMethod
    public float getShadeBrightness(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public float getShadeBrightness_(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        long packedPos = BlockPos.asLong(x, y, z);
        float bright;
        if (this.enabled) {
            bright = this.brightnessCache.get(packedPos);
            if (!Float.isNaN(bright)) {
                return bright;
            }
        }
        bright = state.getShadeBrightness_(level, x, y, z);
        if (this.enabled) {
            if (this.brightnessCache.size() == 100) {
                this.brightnessCache.removeFirstFloat();
            }

            this.brightnessCache.put(packedPos, bright);
        }
        return bright;
    }
}
