package tgw.evolution.mixin;

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

@Mixin(ModelBlockRenderer.Cache.class)
public abstract class MixinModelBlockRenderer_Cache {

    @Shadow @Final private Long2IntLinkedOpenHashMap colorCache;
    @Shadow private boolean enabled;

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public int getLightColor(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        long packedPos = pos.asLong();
        if (this.enabled) {
            int color = this.colorCache.get(packedPos);
            if (color != Integer.MAX_VALUE) {
                return color;
            }
        }
        int color = EvLevelRenderer.getLightColor(level, state, pos);
        if (this.enabled) {
            if (this.colorCache.size() == 100) {
                this.colorCache.removeFirstInt();
            }
            this.colorCache.put(packedPos, color);
        }
        return color;
    }
}
