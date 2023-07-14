package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.patches.PatchLevel;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(Level.class)
public abstract class MixinLevel implements LevelAccessor, PatchLevel {

    @Shadow @Final public boolean isClientSide;
    @Shadow protected int randValue;
    @Shadow @Final private Thread thread;

    /**
     * Force override. The default method allocates a {@link net.minecraft.core.BlockPos.MutableBlockPos} which isn't ideal and also has to fetch
     * the chunk every time. This implementation should increase performance for large {@link AABB}s and also actually checks for
     * {@link FluidState}s instead of legacy {@link BlockState#getFluidState()}.
     */
    @Override
    public boolean containsAnyLiquid(AABB bb) {
        int minX = Mth.floor(bb.minX);
        int maxX = Mth.ceil(bb.maxX);
        int minY = Mth.floor(bb.minY);
        int maxY = Mth.ceil(bb.maxY);
        int minZ = Mth.floor(bb.minZ);
        int maxZ = Mth.ceil(bb.maxZ);
        int cachedX = Integer.MAX_VALUE;
        int cachedZ = Integer.MAX_VALUE;
        LevelChunk cachedChunk = null;
        for (int x = minX; x < maxX; ++x) {
            int chunkX = SectionPos.blockToSectionCoord(x);
            for (int z = minZ; z < maxZ; ++z) {
                int chunkZ = SectionPos.blockToSectionCoord(z);
                if (chunkX != cachedX || chunkZ != cachedZ) {
                    cachedX = chunkX;
                    cachedZ = chunkZ;
                    cachedChunk = this.getChunk(chunkX, chunkZ);
                }
                assert cachedChunk != null;
                for (int y = minY; y < maxY; ++y) {
                    if (!cachedChunk.getFluidState(x, y, z).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        if (this.isOutsideBuildHeight(blockPos)) {
            return null;
        }
        return !this.isClientSide && Thread.currentThread() != this.thread ?
               null :
               this.getChunkAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
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
        if (this.isOutsideBuildHeight(y)) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getChunkAt_(x, z).getFluidState(x, y, z);
    }

    @Override
    @Shadow
    public abstract int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ);

    @Override
    public BlockPos getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out) {
        this.randValue = this.randValue * 3 + 0x3c6e_f35f;
        int rand = this.randValue >> 2;
        return out.set(x + (rand & 15), y + (rand >> 16 & mask), z + (rand >> 8 & 15));
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

    /**
     * @author TheGreatWolf
     * @reason Add ticking for sloping blocks
     */
    @Override
    @Overwrite
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        if (!this.isClientSide) {
            BlockUtils.updateSlopingBlocks(this, pos.getX(), pos.getY(), pos.getZ());
        }
        FluidState fluidState = this.getFluidState(pos);
        return this.setBlock(pos, fluidState.createLegacyBlock(),
                             BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE | (isMoving ? BlockFlags.IS_MOVING : 0));
    }
}
