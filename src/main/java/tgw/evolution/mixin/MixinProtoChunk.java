package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.Evolution;

import java.util.Map;

@Mixin(ProtoChunk.class)
public abstract class MixinProtoChunk extends ChunkAccess {

    public MixinProtoChunk(ChunkPos chunkPos,
                           UpgradeData upgradeData,
                           LevelHeightAccessor levelHeightAccessor,
                           Registry<Biome> registry,
                           long l,
                           @Nullable LevelChunkSection[] levelChunkSections,
                           @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, registry, l, levelChunkSections, blendingData);
    }

    @Unique
    private static short packOffsetCoordinates_(int x, int y, int z) {
        return (short) (x & 15 | (y & 15) << 4 | (z & 15) << 8);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public Map<BlockPos, BlockEntity> getBlockEntities() {
        Evolution.warn("getBlockEntities() should not be called!");
        return Map.of();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.warn("getBlockEntity(BlockPos) should not be called!");
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Override
    @Overwrite
    public @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos) {
        Evolution.warn("getBlockEntityNbtForSaving(BlockPos) should not be called!");
        return this.getBlockEntityNbtForSaving_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNbtForSaving_(int x, int y, int z) {
        BlockEntity te = this.getBlockEntity_(x, y, z);
        return te != null ? te.saveWithFullMetadata() : this.pendingBlockEntities_().get(BlockPos.asLong(x, y, z));
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
        Evolution.warn("getBlockEntityNbts() should not be called!");
        return Map.of();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        return this.blockEntities_().get(BlockPos.asLong(x, y, z));
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.warn("getBlockState(BlockPos) should not be called!");
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        LevelChunkSection section = this.getSection(this.getSectionIndex(y));
        return section.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : section.getBlockState(x & 15, y & 15, z & 15);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.warn("getFluidState(BlockPos) should not be called!");
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        LevelChunkSection section = this.getSection(this.getSectionIndex(y));
        return section.hasOnlyAir() ? Fluids.EMPTY.defaultFluidState() : section.getFluidState(x & 15, y & 15, z & 15);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public void markPosForPostprocessing(BlockPos pos) {
        Evolution.warn("markPosForPostprocessing(BlockPos) should not be called!");
        this.markPosForPostprocessing_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void markPosForPostprocessing_(int x, int y, int z) {
        if (!this.isOutsideBuildHeight(y)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(y)).add(packOffsetCoordinates_(x, y, z));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Override
    @Overwrite
    public void removeBlockEntity(BlockPos pos) {
        long packed = pos.asLong();
        this.blockEntities_().remove(packed);
        this.pendingBlockEntities_().remove(packed);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace maps
     */
    @Overwrite
    @Override
    public void setBlockEntity(BlockEntity te) {
        this.blockEntities_().put(te.getBlockPos().asLong(), te);
    }
}
