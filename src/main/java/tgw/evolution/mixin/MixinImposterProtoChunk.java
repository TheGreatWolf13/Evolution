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

@Mixin(ImposterProtoChunk.class)
public abstract class MixinImposterProtoChunk extends ProtoChunk {

    @Shadow @Final private LevelChunk wrapped;

    public MixinImposterProtoChunk(ChunkPos chunkPos,
                                   UpgradeData upgradeData,
                                   @Nullable LevelChunkSection[] levelChunkSections,
                                   ProtoChunkTicks<Block> protoChunkTicks,
                                   ProtoChunkTicks<Fluid> protoChunkTicks2,
                                   LevelHeightAccessor levelHeightAccessor,
                                   Registry<Biome> registry,
                                   @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelChunkSections, protoChunkTicks, protoChunkTicks2, levelHeightAccessor, registry, blendingData);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.warn("getBlockEntity(BlockPos) should not be called!");
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable CompoundTag getBlockEntityNbt(BlockPos pos) {
        Evolution.warn("getBlockEntityNbt(BlockPos) should not be called!");
        return this.getBlockEntityNbt_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
        Evolution.warn("getBlockEntityNbtForSaving(BlockPos) should not be called");
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

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Override
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        Evolution.warn("getBlockState(BlockPos) should not be called!");
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return this.wrapped.getBlockState_(x, y, z);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Override
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        Evolution.warn("getFluidState(BlockPos) should not be called!");
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return this.wrapped.getFluidState_(x, y, z);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Override
    @Overwrite
    public void markPosForPostprocessing(BlockPos pos) {
        Evolution.warn("markPosForPostprocessing(BlockPos) should not be called!");
        this.markPosForPostprocessing_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void markPosForPostprocessing_(int x, int y, int z) {
    }
}
