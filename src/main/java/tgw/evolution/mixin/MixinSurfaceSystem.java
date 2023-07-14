package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;

import java.util.Objects;

@Mixin(SurfaceSystem.class)
public abstract class MixinSurfaceSystem {

    @Shadow @Final private BlockState defaultBlock;

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos versions
     */
    @Overwrite
    public void buildSurface(BiomeManager biomeManager,
                             Registry<Biome> registry,
                             boolean bl,
                             WorldGenerationContext worldGenerationContext,
                             ChunkAccess chunkAccess,
                             NoiseChunk noiseChunk,
                             SurfaceRules.RuleSource ruleSource) {
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = chunkAccess.getPos();
        int x0 = chunkPos.getMinBlockX();
        int z0 = chunkPos.getMinBlockZ();
        BlockColumn blockColumn = new BlockColumn() {

            @Override
            public BlockState getBlock(int i) {
                Evolution.warn("getBlock(int) should not be called!");
                return this.getBlock_(mutableBlockPos.getX(), i, mutableBlockPos.getZ());
            }

            @Override
            public BlockState getBlock_(int x, int y, int z) {
                return chunkAccess.getBlockState_(x, y, z);
            }

            @Override
            public void setBlock(int i, BlockState blockState) {
                LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
                if (i >= levelHeightAccessor.getMinBuildHeight() && i < levelHeightAccessor.getMaxBuildHeight()) {
                    chunkAccess.setBlockState(mutableBlockPos.setY(i), blockState, false);
                    if (!blockState.getFluidState().isEmpty()) {
                        chunkAccess.markPosForPostprocessing(mutableBlockPos);
                    }
                }

            }

            @Override
            public String toString() {
                return "ChunkBlockColumn " + chunkPos;
            }
        };
        Objects.requireNonNull(biomeManager);
        SurfaceRules.Context context = new SurfaceRules.Context((SurfaceSystem) (Object) this, chunkAccess, noiseChunk, biomeManager::getBiome,
                                                                registry, worldGenerationContext);
        SurfaceRules.SurfaceRule surfaceRule = ruleSource.apply(context);
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; ++dx) {
            for (int dz = 0; dz < 16; ++dz) {
                int x = x0 + dx;
                int z = z0 + dz;
                int o = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz) + 1;
                mutableBlockPos.setX(x).setZ(z);
                Holder<Biome> holder = biomeManager.getBiome_(x, bl ? 0 : o, z);
                if (holder.is(Biomes.ERODED_BADLANDS)) {
                    this.erodedBadlandsExtension(blockColumn, x, z, o, chunkAccess);
                }
                int y1 = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz) + 1;
                context.updateXZ(x, z);
                int q = 0;
                int r = Integer.MIN_VALUE;
                int s = Integer.MAX_VALUE;
                int y0 = chunkAccess.getMinBuildHeight();
                for (int y = y1; y >= y0; --y) {
                    BlockState state = blockColumn.getBlock_(x, y, z);
                    if (state.isAir()) {
                        q = 0;
                        r = Integer.MIN_VALUE;
                    }
                    else if (!state.getFluidState().isEmpty()) {
                        if (r == Integer.MIN_VALUE) {
                            r = y + 1;
                        }
                    }
                    else {
                        int v;
                        BlockState blockState2;
                        if (s >= y) {
                            s = DimensionType.WAY_BELOW_MIN_Y;
                            for (v = y - 1; v >= y0 - 1; --v) {
                                blockState2 = blockColumn.getBlock_(x, v, z);
                                if (!this.isStone(blockState2)) {
                                    s = v + 1;
                                    break;
                                }
                            }
                        }
                        ++q;
                        v = y - s + 1;
                        context.updateY(q, v, r, x, y, z);
                        if (state == this.defaultBlock) {
                            blockState2 = surfaceRule.tryApply(x, y, z);
                            if (blockState2 != null) {
                                blockColumn.setBlock(y, blockState2);
                            }
                        }
                    }
                }
                if (holder.is(Biomes.FROZEN_OCEAN) || holder.is(Biomes.DEEP_FROZEN_OCEAN)) {
                    this.frozenOceanExtension(context.getMinSurfaceLevel(), holder.value(), blockColumn, mutableBlockPos2, x, z, o);
                }
            }
        }
    }

    @Shadow
    protected abstract void erodedBadlandsExtension(BlockColumn blockColumn, int i, int j, int k, LevelHeightAccessor levelHeightAccessor);

    @Shadow
    protected abstract void frozenOceanExtension(int i,
                                                 Biome biome,
                                                 BlockColumn blockColumn,
                                                 BlockPos.MutableBlockPos mutableBlockPos,
                                                 int j,
                                                 int k,
                                                 int l);

    @Shadow
    protected abstract boolean isStone(BlockState blockState);
}
