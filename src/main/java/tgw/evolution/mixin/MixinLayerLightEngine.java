package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEngine;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LayerLightEngine.class)
public abstract class MixinLayerLightEngine {

    @Shadow @Final protected LightChunkGetter chunkSource;
    @Shadow @Final protected BlockPos.MutableBlockPos pos;

    @Shadow
    protected abstract @Nullable BlockGetter getChunk(int i, int j);

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    public BlockState getStateAndOpacity(long pos, @Nullable MutableInt opacity) {
        if (pos == Long.MAX_VALUE) {
            if (opacity != null) {
                opacity.setValue(0);
            }
            return Blocks.AIR.defaultBlockState();
        }
        int secX = SectionPos.blockToSectionCoord(BlockPos.getX(pos));
        int secZ = SectionPos.blockToSectionCoord(BlockPos.getZ(pos));
        BlockGetter chunk = this.getChunk(secX, secZ);
        if (chunk == null) {
            if (opacity != null) {
                opacity.setValue(16);
            }
            return Blocks.BEDROCK.defaultBlockState();
        }
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockState blockState = chunk.getBlockState_(x, y, z);
        boolean blocksLight = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
        if (opacity != null) {
            opacity.setValue(blockState.getLightBlock(this.chunkSource.getLevel(), this.pos.set(x, y, z)));
        }
        return blocksLight ? blockState : Blocks.AIR.defaultBlockState();
    }
}
