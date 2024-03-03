package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.Supplier;

@Mixin(WorldGenRegion.class)
public abstract class Mixin_M_WorldGenRegion implements WorldGenLevel {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private ChunkAccess center;
    @Shadow private @Nullable Supplier<String> currentlyGenerating;
    @Shadow @Final private ChunkStatus generatingStatus;
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private int writeRadiusCutoff;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean ensureCanWrite(BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean ensureCanWrite_(int x, int y, int z) {
        int secX = SectionPos.blockToSectionCoord(x);
        int secZ = SectionPos.blockToSectionCoord(z);
        ChunkPos chunkPos = this.getCenter();
        int dx = Math.abs(chunkPos.x - secX);
        int dz = Math.abs(chunkPos.z - secZ);
        if (dx <= this.writeRadiusCutoff && dz <= this.writeRadiusCutoff) {
            if (this.center.isUpgrading()) {
                LevelHeightAccessor level = this.center.getHeightAccessorForGeneration();
                return y >= level.getMinBuildHeight() && y < level.getMaxBuildHeight();
            }
            return true;
        }
        Util.logAndPauseIfInIde("Detected setBlock in a far chunk [" +
                                secX +
                                ", " +
                                secZ +
                                "], pos: " +
                                x + ", " + y + ", " + z +
                                ", status: " +
                                this.generatingStatus +
                                (this.currentlyGenerating == null ? "" : ", currently generating: " + this.currentlyGenerating.get()));
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        ChunkAccess chunk = this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        BlockEntity te = chunk.getBlockEntity_(x, y, z);
        if (te != null) {
            return te;
        }
        CompoundTag compoundTag = chunk.getBlockEntityNbt_(x, y, z);
        BlockState state = chunk.getBlockState_(x, y, z);
        if (compoundTag != null) {
            if ("DUMMY".equals(compoundTag.getString("id"))) {
                if (!state.hasBlockEntity()) {
                    return null;
                }
                te = ((EntityBlock) state.getBlock()).newBlockEntity(new BlockPos(x, y, z), state);
            }
            else {
                te = TEUtils.loadStatic(x, y, z, state, compoundTag);
            }
            if (te != null) {
                chunk.setBlockEntity(te);
                return te;
            }
        }
        if (state.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created at [{}, {}, {}]", x, y, z);
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getBlockState_(x, y, z);
    }

    @Shadow
    public abstract ChunkPos getCenter();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getCurrentDifficultyAt_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt_(int x, int y, int z) {
        if (!this.hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        }
        return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z)).getFluidState_(x, y, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
        Evolution.deprecatedMethod();
    }

    @Override
    public void levelEvent_(@Nullable Player player, int event, int x, int y, int z, int data) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @DeleteMethod
    private void markPosForPostprocessing(BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        Evolution.deprecatedMethod();
        return this.removeBlock_(pos.getX(), pos.getY(), pos.getZ(), isMoving);
    }

    @Override
    public boolean removeBlock_(int x, int y, int z, boolean isMoving) {
        return this.setBlock_(x, y, z, Blocks.AIR.defaultBlockState(), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean setBlock(BlockPos pos, BlockState state, @BlockFlags int flags, int limit) {
        Evolution.deprecatedMethod();
        return this.setBlock_(pos.getX(), pos.getY(), pos.getZ(), state, flags, limit);
    }

    @Override
    public boolean setBlock_(int x, int y, int z, BlockState state, @BlockFlags int flags, int limit) {
        if (!this.ensureCanWrite_(x, y, z)) {
            return false;
        }
        ChunkAccess chunk = this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        BlockState oldState = chunk.setBlockState_(x, y, z, state, false);
        if (oldState != null) {
            this.level.onBlockStateChange_(x, y, z, oldState, state);
        }
        if (state.hasBlockEntity()) {
            if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
                //Allocation here is fine
                BlockEntity blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(new BlockPos(x, y, z), state);
                if (blockEntity != null) {
                    chunk.setBlockEntity(blockEntity);
                }
                else {
                    chunk.removeBlockEntity_(BlockPos.asLong(x, y, z));
                }
            }
            else {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putInt("x", x);
                compoundTag.putInt("y", y);
                compoundTag.putInt("z", z);
                compoundTag.putString("id", "DUMMY");
                chunk.setBlockEntityNbt(compoundTag);
            }
        }
        else if (oldState != null && oldState.hasBlockEntity()) {
            chunk.removeBlockEntity_(BlockPos.asLong(x, y, z));
        }
        if (state.hasPostProcess_(this, x, y, z)) {
            chunk.markPosForPostprocessing_(x, y, z);
        }
        return true;
    }
}
