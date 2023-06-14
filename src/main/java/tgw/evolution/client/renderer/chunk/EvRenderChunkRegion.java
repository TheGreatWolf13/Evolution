package tgw.evolution.client.renderer.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;

public class EvRenderChunkRegion implements BlockAndTintGetter {
    protected final EvRenderChunk[][] chunks;
    protected final Level level;
    private final int centerX;
    private final int centerZ;

    EvRenderChunkRegion(Level level, int centerX, int centerZ, EvRenderChunk[][] chunks) {
        this.level = level;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.chunks = chunks;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        int i = SectionPos.blockToSectionCoord(pos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int i = SectionPos.blockToSectionCoord(pos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockState(pos);
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return this.level.getBlockTint(pos, colorResolver);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        int i = SectionPos.blockToSectionCoord(pos.getX()) - this.centerX;
        int j = SectionPos.blockToSectionCoord(pos.getZ()) - this.centerZ;
        return this.chunks[i][j].getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return this.level.getShade(direction, shade);
    }

    public boolean isSectionEmpty(int posX, int posY, int posZ) {
        int i = SectionPos.blockToSectionCoord(posX) - this.centerX;
        int j = SectionPos.blockToSectionCoord(posZ) - this.centerZ;
        return this.chunks[i][j].isSectionEmpty(posY);
    }
}
