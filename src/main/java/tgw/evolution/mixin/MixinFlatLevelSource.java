package tgw.evolution.mixin;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FlatLevelSource.class)
public abstract class MixinFlatLevelSource extends ChunkGenerator {

    @Shadow @Final private FlatLevelGeneratorSettings settings;

    public MixinFlatLevelSource(Registry<StructureSet> registry,
                                Optional<HolderSet<StructureSet>> optional,
                                BiomeSource biomeSource) {
        super(registry, optional, biomeSource);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor,
                                                        Blender blender,
                                                        StructureFeatureManager structureFeatureManager,
                                                        ChunkAccess chunk) {
        List<BlockState> list = this.settings.getLayers();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int i = 0, len = Math.min(chunk.getHeight(), list.size()); i < len; ++i) {
            BlockState state = list.get(i);
            if (state != null) {
                int y = chunk.getMinBuildHeight() + i;
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        chunk.setBlockState_(x, y, z, state, false);
                        oceanFloor.update(x, y, z, state);
                        worldSurface.update(x, y, z, state);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }
}
