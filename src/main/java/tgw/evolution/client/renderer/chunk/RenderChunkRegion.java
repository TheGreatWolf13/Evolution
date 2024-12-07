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
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.util.physics.EarthHelper;

public class RenderChunkRegion implements BlockAndTintGetter {

    protected final EvRenderChunk[][] chunks;
    protected final Level level;
    private final int startX;
    private final int startZ;

    RenderChunkRegion(Level level, int startX, int startZ, EvRenderChunk[][] chunks) {
        this.level = level;
        this.startX = startX;
        this.startZ = startZ;
        this.chunks = chunks;
    }

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        int i = SectionPos.blockToSectionCoord(x) - this.startX;
        int j = SectionPos.blockToSectionCoord(z) - this.startZ;
        return this.chunks[i][j].getBlockEntity(x, y, z);
    }

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        int i = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(x) - this.startX);
        int j = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(z) - this.startZ);
        return this.chunks[i][j].getBlockState(x, y, z);
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        Evolution.deprecatedMethod();
        return this.getBlockTint_(pos.getX(), pos.getY(), pos.getZ(), colorResolver);
    }

    @Override
    public int getBlockTint_(int x, int y, int z, ColorResolver colorResolver) {
        return this.level.getBlockTint_(x, y, z, colorResolver);
    }

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        int i = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(x) - this.startX);
        int j = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(z) - this.startZ);
        return this.chunks[i][j].getBlockState(x, y, z).getFluidState();
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
        int i = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(posX) - this.startX);
        int j = EarthHelper.wrapChunkCoordinate(SectionPos.blockToSectionCoord(posZ) - this.startZ);
        return this.chunks[i][j].isSectionEmpty(posY);
    }
}
