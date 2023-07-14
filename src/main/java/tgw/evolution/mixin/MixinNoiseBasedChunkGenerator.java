package tgw.evolution.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseBasedChunkGenerator extends ChunkGenerator {

    @Shadow @Final private static BlockState AIR;
    @Shadow @Final protected BlockState defaultBlock;
    @Shadow @Final protected Holder<NoiseGeneratorSettings> settings;
    @Shadow @Final private Aquifer.FluidPicker globalFluidPicker;
    @Shadow @Final private NoiseRouter router;

    public MixinNoiseBasedChunkGenerator(Registry<StructureSet> registry,
                                         Optional<HolderSet<StructureSet>> optional,
                                         BiomeSource biomeSource) {
        super(registry, optional, biomeSource);
    }

    @Shadow
    protected abstract BlockState debugPreliminarySurfaceLevel(NoiseChunk noiseChunk,
                                                               int i,
                                                               int j,
                                                               int k,
                                                               BlockState blockState);

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos versions
     */
    @Overwrite
    private ChunkAccess doFill(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, int i, int j) {
        NoiseGeneratorSettings settings = this.settings.value();
        NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(this.router, () -> {
            return new Beardifier(structureFeatureManager, chunkAccess);
        }, settings, this.globalFluidPicker, blender);
        Heightmap oceanFloor = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess.getPos();
        int x0 = chunkPos.getMinBlockX();
        int z0 = chunkPos.getMinBlockZ();
        Aquifer aquifer = noiseChunk.aquifer();
        noiseChunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        NoiseSettings noiseSettings = settings.noiseSettings();
        int w = noiseSettings.getCellWidth();
        int h = noiseSettings.getCellHeight();
        int w0 = 16 / w;
        for (int q = 0; q < w0; ++q) {
            noiseChunk.advanceCellX(q);
            for (int r = 0; r < w0; ++r) {
                LevelChunkSection section = chunkAccess.getSection(chunkAccess.getSectionsCount() - 1);
                for (int s = j - 1; s >= 0; --s) {
                    noiseChunk.selectCellYZ(s, r);
                    for (int t = h - 1; t >= 0; --t) {
                        int y = (i + s) * h + t;
                        int localY = y & 15;
                        int index = chunkAccess.getSectionIndex(y);
                        if (chunkAccess.getSectionIndex(section.bottomBlockY()) != index) {
                            section = chunkAccess.getSection(index);
                        }
                        double d = t / (double) h;
                        noiseChunk.updateForY(y, d);
                        for (int dfdsfds = 0; dfdsfds < w; ++dfdsfds) {
                            int x = x0 + q * w + dfdsfds;
                            int localX = x & 15;
                            double e = dfdsfds / (double) w;
                            noiseChunk.updateForX(x, e);
                            for (int aa = 0; aa < w; ++aa) {
                                int z = z0 + r * w + aa;
                                double f = aa / (double) w;
                                noiseChunk.updateForZ(z, f);
                                BlockState state = noiseChunk.getInterpolatedState();
                                if (state == null) {
                                    state = this.defaultBlock;
                                }
                                state = this.debugPreliminarySurfaceLevel(noiseChunk, x, y, z, state);
                                if (state != AIR && !SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
                                    if (state.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk protoChunk) {
                                        protoChunk.addLight(mutableBlockPos.set(x, y, z));
                                    }
                                    int localZ = z & 15;
                                    section.setBlockState(localX, localY, localZ, state, false);
                                    oceanFloor.update(localX, y, localZ, state);
                                    worldSurface.update(localX, y, localZ, state);
                                    if (aquifer.shouldScheduleFluidUpdate() && !state.getFluidState().isEmpty()) {
                                        chunkAccess.markPosForPostprocessing_(x, y, z);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            noiseChunk.swapSlices();
        }
        noiseChunk.stopInterpolation();
        return chunkAccess;
    }
}
