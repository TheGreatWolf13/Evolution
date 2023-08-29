package tgw.evolution.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockFire;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.patches.PatchLevel;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.LvlEvent;
import tgw.evolution.util.math.DirectionUtil;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor, PatchLevel {

    @Shadow @Final public boolean isClientSide;
    @Shadow @Final private Thread thread;

    @Overwrite
    public void addDestroyBlockEffect(BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        this.addDestroyBlockEffect_(pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public void addDestroyBlockEffect_(int x, int y, int z, BlockState state) {
    }

    @Overwrite
    public void blockEntityChanged(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.blockEntityChanged_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void blockEntityChanged_(int x, int y, int z) {
        ChunkAccess chunk = this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z), ChunkStatus.FULL, false);
        if (chunk != null) {
            chunk.setUnsaved(true);
        }
    }

    @Override
    @Overwrite
    public boolean destroyBlock(BlockPos pos, boolean drop, @Nullable Entity entity, int limit) {
        Evolution.deprecatedMethod();
        return this.destroyBlock_(pos.getX(), pos.getY(), pos.getZ(), drop, entity, limit);
    }

    @Override
    public boolean destroyBlock_(int x, int y, int z, boolean drop, @Nullable Entity entity, int limit) {
        BlockState state = this.getBlockState_(x, y, z);
        if (state.isAir()) {
            return false;
        }
        FluidState fluidState = this.getFluidState_(x, y, z);
        if (!(state.getBlock() instanceof BaseFireBlock) && !(state.getBlock() instanceof BlockFire)) {
            this.levelEvent_(LevelEvent.PARTICLES_DESTROY_BLOCK, x, y, z, Block.getId(state));
        }
        if (drop) {
            BlockEntity blockEntity = state.hasBlockEntity() ? this.getBlockEntity_(x, y, z) : null;
            BlockUtils.dropResources(state, this, x, y, z, blockEntity, entity, ItemStack.EMPTY);
        }
        return this.setBlock_(x, y, z, fluidState.createLegacyBlock(), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE, limit);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return null;
        }
        return !this.isClientSide && Thread.currentThread() != this.thread ?
               null :
               this.getChunkAt_(x, z).getBlockEntity_(x, y, z, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public BlockState getBlockState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockState getBlockState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        return this.getChunkAt_(x, z).getBlockState_(x, y, z);
    }

    @Override
    @Shadow
    public abstract LevelChunk getChunk(int pChunkX, int pChunkZ);

    @Override
    @Shadow
    @Contract("_, _, _, true -> !null")
    public abstract @Nullable ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl);

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    public LevelChunk getChunkAt(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getChunkAt_(pos.getX(), pos.getZ());
    }

    @Override
    public LevelChunk getChunkAt_(int x, int z) {
        return this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    @Override
    @Overwrite
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getCurrentDifficultyAt_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt_(int x, int y, int z) {
        long inhabitedTime = 0L;
        float moonBrightness = 0.0F;
        ChunkAccess chunk = this.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z), ChunkStatus.FULL, false);
        if (chunk != null) {
            moonBrightness = this.getMoonBrightness();
            inhabitedTime = chunk.getInhabitedTime();
        }
        return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), inhabitedTime, moonBrightness);
    }

    @Shadow
    public abstract long getDayTime();

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @SuppressWarnings("removal")
    @Overwrite
    @Override
    public FluidState getFluidState(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState_(int x, int y, int z) {
        if (this.isOutsideBuildHeight(y)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getChunkAt_(x, z).getFluidState(x, y, z);
    }

    @Override
    @Shadow
    public abstract int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ);

    @Shadow
    public abstract ProfilerFiller getProfiler();

    @Overwrite
    public void globalLevelEvent(@LvlEvent int event, BlockPos pos, int data) {
        Evolution.deprecatedMethod();
        this.globalLevelEvent_(event, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    @Override
    public void globalLevelEvent_(@LvlEvent int event, int x, int y, int z, int data) {
    }

    @Shadow
    public abstract boolean isDebug();

    @Overwrite
    public boolean isHumidAt(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isHumidAt_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isHumidAt_(int x, int y, int z) {
        return this.getBiome_(x, y, z).value().isHumid();
    }

    @Overwrite
    public boolean isInWorldBounds(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isInWorldBounds_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isInWorldBounds_(int x, int y, int z) {
        return !this.isOutsideBuildHeight(y) && x >= -30_000_000 && z >= -30_000_000 && x < 30_000_000 && z < 30_000_000;
    }

    @Overwrite
    public boolean isLoaded(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isLoaded_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isLoaded_(int x, int y, int z) {
        return !this.isOutsideBuildHeight(y) && this.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
    }

    @Shadow
    public abstract boolean isRaining();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocating a BlockPos to check the heightmap y coordinate only.
     */
    @Overwrite
    public boolean isRainingAt(BlockPos pos) {
        if (!this.isRaining()) {
            return false;
        }
        if (!this.canSeeSky(pos)) {
            return false;
        }
        //The method getHeightmapPos(Heightmap.Types, BlockPos) allocates a new BlockPos using the x and z coordinates from the original BlockPos
        // and the y from the heightmap, however, only the y coordinate is needed for this check, so allocating the BlockPos is wasteful.
        if (this.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY()) {
            return false;
        }
        Biome biome = this.getBiome(pos).value();
        return biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(pos);
    }

    @Overwrite
    public void neighborChanged(BlockPos pos, Block block, BlockPos fromPos) {
        Evolution.deprecatedMethod();
        this.neighborChanged_(pos.getX(), pos.getY(), pos.getZ(), block, fromPos.getX(), fromPos.getY(), fromPos.getZ());
    }

    @Override
    public void neighborChanged_(int x, int y, int z, Block block, int fromX, int fromY, int fromZ) {
        if (!this.isClientSide) {
            BlockState state = this.getBlockState_(x, y, z);
            try {
                //noinspection ConstantConditions
                state.neighborChanged_((Level) (Object) this, x, y, z, block, fromX, fromY, fromZ, false);
            }
            catch (Throwable t) {
                CrashReport crash = CrashReport.forThrowable(t, "Exception while updating neighbours");
                CrashReportCategory category = crash.addCategory("Block being updated");
                category.setDetail("Source block type", () -> {
                    try {
                        return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(),
                                             block.getClass().getCanonicalName());
                    }
                    catch (Throwable var2) {
                        //noinspection ConstantConditions
                        return "ID #" + Registry.BLOCK.getKey(block);
                    }
                });
                category.setDetail("Block", state::toString);
                category.setDetail("Block location", () -> CrashReportCategory.formatLocation(this, x, y, z));
                throw new ReportedException(crash);
            }
        }
    }

    @Overwrite
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        Evolution.deprecatedMethod();
        this.onBlockStateChange_(pos.getX(), pos.getY(), pos.getZ(), oldState, newState);
    }

    @Override
    public void onBlockStateChange_(int x, int y, int z, BlockState oldState, BlockState newState) {
    }

    /**
     * @author TheGreatWolf
     * @reason Add ticking for sloping blocks
     */
    @Override
    @Overwrite
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        Evolution.deprecatedMethod();
        return this.removeBlock_(pos.getX(), pos.getY(), pos.getZ(), isMoving);
    }

    @Overwrite
    public void removeBlockEntity(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.removeBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void removeBlockEntity_(int x, int y, int z) {
        if (!this.isOutsideBuildHeight(y)) {
            this.getChunkAt_(x, z).removeBlockEntity_(BlockPos.asLong(x, y, z));
        }
    }

    @Override
    public boolean removeBlock_(int x, int y, int z, boolean isMoving) {
        if (!this.isClientSide) {
            BlockUtils.updateSlopingBlocks(this, x, y, z);
        }
        FluidState fluidState = this.getFluidState_(x, y, z);
        return this.setBlock_(x, y, z, fluidState.createLegacyBlock(),
                              BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | (isMoving ? BlockFlags.IS_MOVING : 0));
    }

    @Override
    @Overwrite
    public boolean setBlock(BlockPos pos, BlockState state, @BlockFlags int flags) {
        Evolution.deprecatedMethod();
        return this.setBlock_(pos.getX(), pos.getY(), pos.getZ(), state, flags);
    }

    @Override
    @Overwrite
    public boolean setBlock(BlockPos pos, BlockState state, @BlockFlags int flags, int limit) {
        Evolution.deprecatedMethod();
        return this.setBlock_(pos.getX(), pos.getY(), pos.getZ(), state, flags, limit);
    }

    @Overwrite
    public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
        Evolution.deprecatedMethod();
        return this.setBlockAndUpdate_(pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Override
    public boolean setBlockAndUpdate_(int x, int y, int z, BlockState state) {
        return this.setBlock_(x, y, z, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
    }

    @Override
    public boolean setBlock_(int x, int y, int z, BlockState state, @BlockFlags int flags, int limit) {
        if (this.isOutsideBuildHeight(y)) {
            return false;
        }
        if (!this.isClientSide && this.isDebug()) {
            return false;
        }
        LevelChunk chunk = this.getChunkAt_(x, z);
        Block block = state.getBlock();
        BlockState oldState = chunk.setBlockState_(x, y, z, state, (flags & BlockFlags.IS_MOVING) != 0);
        if (oldState == null) {
            return false;
        }
        BlockState gottenState = this.getBlockState_(x, y, z);
        if ((flags & BlockFlags.SUPRESS_LIGHT_UPDATES) == 0 &&
            gottenState != oldState &&
            (gottenState.getLightBlock_(this, x, y, z) != oldState.getLightBlock_(this, x, y, z) ||
             gottenState.getLightEmission() != oldState.getLightEmission() ||
             gottenState.useShapeForLightOcclusion() ||
             oldState.useShapeForLightOcclusion())) {
            this.getProfiler().push("queueCheckLight");
            this.getChunkSource().getLightEngine().checkBlock_(BlockPos.asLong(x, y, z));
            this.getProfiler().pop();
        }
        if (gottenState == state) {
            if (oldState != gottenState) {
                this.setBlocksDirty_(x, y, z, oldState, gottenState);
            }
            if ((flags & BlockFlags.BLOCK_UPDATE) != 0 &&
                (!this.isClientSide || (flags & BlockFlags.NO_RERENDER) == 0) &&
                (this.isClientSide || chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))) {
                this.sendBlockUpdated_(x, y, z, oldState, state, flags);
            }
            if ((flags & BlockFlags.NOTIFY) != 0) {
                this.blockUpdated_(x, y, z, oldState.getBlock());
                if (!this.isClientSide && state.hasAnalogOutputSignal()) {
                    this.updateNeighbourForOutputSignal_(x, y, z, block);
                }
            }
            if ((flags & BlockFlags.UPDATE_NEIGHBORS) == 0 && limit > 0) {
                int newFlags = flags & ~(BlockFlags.NO_NEIGHBOR_DROPS | BlockFlags.NOTIFY);
                oldState.updateIndirectNeighbourShapes_(this, x, y, z, newFlags, limit - 1);
                state.updateNeighbourShapes_(this, x, y, z, newFlags, limit - 1);
                state.updateIndirectNeighbourShapes_(this, x, y, z, newFlags, limit - 1);
            }
            this.onBlockStateChange_(x, y, z, oldState, gottenState);
        }
        return true;
    }

    @Overwrite
    public void setBlocksDirty(BlockPos pos, BlockState oldState, BlockState newState) {
        Evolution.deprecatedMethod();
        this.setBlocksDirty_(pos.getX(), pos.getY(), pos.getZ(), oldState, newState);
    }

    @Override
    public void setBlocksDirty_(int x, int y, int z, BlockState oldState, BlockState newState) {
    }

    @Overwrite
    public void updateNeighborsAt(BlockPos pos, Block block) {
        Evolution.deprecatedMethod();
        this.updateNeighborsAt_(pos.getX(), pos.getY(), pos.getZ(), block);
    }

    @Override
    public void updateNeighborsAt_(int x, int y, int z, Block block) {
        this.neighborChanged_(x - 1, y, z, block, x, y, z);
        this.neighborChanged_(x + 1, y, z, block, x, y, z);
        this.neighborChanged_(x, y - 1, z, block, x, y, z);
        this.neighborChanged_(x, y + 1, z, block, x, y, z);
        this.neighborChanged_(x, y, z - 1, block, x, y, z);
        this.neighborChanged_(x, y, z + 1, block, x, y, z);
    }

    @Overwrite
    public void updateNeighbourForOutputSignal(BlockPos pos, Block block) {
        Evolution.deprecatedMethod();
        this.updateNeighbourForOutputSignal_(pos.getX(), pos.getY(), pos.getZ(), block);
    }

    @Override
    public void updateNeighbourForOutputSignal_(int x, int y, int z, Block block) {
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            int offX = x + dir.getStepX();
            int offZ = z + dir.getStepZ();
            if (this.hasChunkAt(offX, offZ)) {
                BlockState stateAtOff = this.getBlockState_(offX, y, offZ);
                if (stateAtOff.is(Blocks.COMPARATOR)) {
                    stateAtOff.neighborChanged_((Level) (Object) this, offX, y, offZ, block, x, y, z, false);
                }
                else if (stateAtOff.isRedstoneConductor_(this, offX, y, offZ)) {
                    offX += dir.getStepX();
                    offZ += dir.getStepZ();
                    stateAtOff = this.getBlockState_(offX, y, offZ);
                    if (stateAtOff.is(Blocks.COMPARATOR)) {
                        stateAtOff.neighborChanged_((Level) (Object) this, offX, y, offZ, block, x, y, z, false);
                    }
                }
            }
        }
    }
}
