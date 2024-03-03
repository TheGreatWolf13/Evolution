package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.world.lighting.SWMRNibbleArray;
import tgw.evolution.world.lighting.SWMRShortArray;
import tgw.evolution.world.lighting.StarLightEngine;

@Mixin(EmptyLevelChunk.class)
public abstract class Mixin_M_EmptyLevelChunk extends LevelChunk {

    public Mixin_M_EmptyLevelChunk(Level level, ChunkPos pos) {
        super(level, pos);
    }

    @Override
    public boolean @Nullable [] getBlockEmptinessMap() {
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z, EntityCreationType creationType) {
        return null;
    }

    @Override
    public SWMRShortArray[] getBlockShorts() {
        return StarLightEngine.getFilledEmptyLightShort(this.getLevel());
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    @DeleteMethod
    public BlockState getBlockState(BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState getBlockStateAtSide(int x, int y, int z, Direction direction) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    @DeleteMethod
    public FluidState getFluidState(BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return Fluids.EMPTY.defaultFluidState();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    @DeleteMethod
    public int getLightEmission(BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getLightEmission_(int x, int y, int z) {
        return 0;
    }

    @Override
    public boolean @Nullable [] getSkyEmptinessMap() {
        return null;
    }

    @Override
    public SWMRNibbleArray[] getSkyNibbles() {
        return StarLightEngine.getFilledEmptyLightNibble(this.getLevel());
    }

    @Override
    public void primeAtm(boolean needsResetting) {
        //Do nothing
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void removeBlockEntity(BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Override
    public void removeBlockEntity_(long pos) {
        //Do nothing
    }

    @Override
    public void setBlockEmptinessMap(boolean @Nullable [] emptinessMap) {
        //Do nothing
    }

    @Override
    public void setBlockShorts(SWMRShortArray[] nibbles) {
        //Do nothing
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        Evolution.deprecatedMethod();
        return this.setBlockState_(pos.getX(), pos.getY(), pos.getZ(), state, isMoving);
    }

    @Override
    public @Nullable BlockState setBlockState_(int x, int y, int z, BlockState state, boolean isMoving) {
        return null;
    }

    @Override
    public void setSkyEmptinessMap(boolean @Nullable [] emptinessMap) {
        //Do nothing
    }

    @Override
    public void setSkyNibbles(SWMRNibbleArray[] nibbles) {
        //Do nothing
    }
}
