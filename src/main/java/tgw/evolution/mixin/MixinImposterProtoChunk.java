package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.world.lighting.SWMRNibbleArray;
import tgw.evolution.world.lighting.SWMRShortArray;

import java.util.stream.Stream;

@Mixin(ImposterProtoChunk.class)
public abstract class MixinImposterProtoChunk extends ProtoChunk {

    @Shadow @Final private boolean allowWrites;
    @Shadow @Final private LevelChunk wrapped;

    public MixinImposterProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, @Nullable LevelChunkSection[] levelChunkSections, ProtoChunkTicks<Block> protoChunkTicks, ProtoChunkTicks<Fluid> protoChunkTicks2, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelChunkSections, protoChunkTicks, protoChunkTicks2, levelHeightAccessor, registry, blendingData);
    }

    @Override
    public boolean @Nullable [] getBlockEmptinessMap() {
        return this.wrapped.getBlockEmptinessMap();
    }

    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Overwrite
    @Override
    public @Nullable CompoundTag getBlockEntityNbt(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntityNbt_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Overwrite
    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntityNbtForSaving_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving_(int x, int y, int z) {
        return this.wrapped.getBlockEntityNbtForSaving_(x, y, z);
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbt_(int x, int y, int z) {
        return this.wrapped.getBlockEntityNbt_(x, y, z);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return this.wrapped.getBlockEntity_(x, y, z);
    }

    @Override
    public SWMRShortArray[] getBlockShorts() {
        return this.wrapped.getBlockShorts();
    }

    @Override
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return this.wrapped.getBlockState_(x, y, z);
    }

    @Override
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return this.wrapped.getFluidState_(x, y, z);
    }

    @Override
    @Overwrite
    public Stream<BlockPos> getLights() {
        Evolution.deprecatedMethod();
        return this.wrapped.getLights();
    }

    @Override
    public LList getLights_() {
        return this.wrapped.getLights_();
    }

    @Override
    public boolean @Nullable [] getSkyEmptinessMap() {
        return this.wrapped.getSkyEmptinessMap();
    }

    @Override
    public SWMRNibbleArray[] getSkyNibbles() {
        return this.wrapped.getSkyNibbles();
    }

    @Override
    @Overwrite
    public void markPosForPostprocessing(BlockPos pos) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void markPosForPostprocessing_(int x, int y, int z) {
    }

    @Override
    @Overwrite
    public void removeBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void removeBlockEntity_(long pos) {
    }

    @Override
    public void setBlockEmptinessMap(boolean @Nullable [] emptinessMap) {
        this.wrapped.setBlockEmptinessMap(emptinessMap);
    }

    @Override
    public void setBlockShorts(SWMRShortArray[] nibbles) {
        this.wrapped.setBlockShorts(nibbles);
    }

    @Override
    @Overwrite
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        Evolution.deprecatedMethod();
        return this.setBlockState_(pos.getX(), pos.getY(), pos.getZ(), state, isMoving);
    }

    @Override
    public @Nullable BlockState setBlockState_(int x, int y, int z, BlockState state, boolean isMoving) {
        return this.allowWrites ? this.wrapped.setBlockState_(x, y, z, state, isMoving) : null;
    }

    @Override
    public void setSkyEmptinessMap(boolean @Nullable [] emptinessMap) {
        this.wrapped.setSkyEmptinessMap(emptinessMap);
    }

    @Override
    public void setSkyNibbles(SWMRNibbleArray[] nibbles) {
        this.wrapped.setSkyNibbles(nibbles);
    }
}
