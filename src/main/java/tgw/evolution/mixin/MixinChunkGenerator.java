package tgw.evolution.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator implements BiomeManager.NoiseBiomeSource {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void createReferences(WorldGenLevel level, StructureFeatureManager featureManager, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int x = pos.x;
        int z = pos.z;
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        SectionPos bottomSection = SectionPos.bottomOf(chunk);
        for (int dx = x - 8; dx <= x + 8; ++dx) {
            for (int dz = z - 8; dz <= z + 8; ++dz) {
                long newPos = ChunkPos.asLong(dx, dz);
                for (StructureStart structureStart : level.getChunk(dx, dz).getAllStarts().values()) {
                    try {
                        if (structureStart.isValid() && structureStart.getBoundingBox().intersects(minX, minZ, minX + 15, minZ + 15)) {
                            featureManager.addReferenceForFeature(bottomSection, structureStart.getFeature(), newPos, chunk);
                            DebugPackets.sendStructurePacket(level, structureStart);
                        }
                    }
                    catch (Exception e) {
                        CrashReport crash = CrashReport.forThrowable(e, "Generating structure reference");
                        CrashReportCategory report = crash.addCategory("Structure");
                        Optional<? extends Registry<ConfiguredStructureFeature<?, ?>>> optional = level.registryAccess().registry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                        report.setDetail("Id", () -> optional.map(registry -> registry.getKey(structureStart.getFeature()).toString()).orElse("UNKNOWN"));
                        report.setDetail("Name", () -> Registry.STRUCTURE_FEATURE.getKey(structureStart.getFeature().feature).toString());
                        report.setDetail("Class", () -> structureStart.getFeature().getClass().getCanonicalName());
                        throw new ReportedException(crash);
                    }
                }
            }
        }
    }
}
