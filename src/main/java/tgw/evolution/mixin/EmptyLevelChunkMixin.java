package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EmptyLevelChunk.class)
public abstract class EmptyLevelChunkMixin extends LevelChunk {

    public EmptyLevelChunkMixin(Level pLevel, ChunkPos pPos) {
        super(pLevel, pPos);
    }

    /**
     * Force override. Make sure return value is consistent with {@link EmptyLevelChunk#getFluidState(BlockPos)}
     */
    @Override
    public FluidState getFluidState(int pX, int pY, int pZ) {
        return Fluids.EMPTY.defaultFluidState();
    }
}
