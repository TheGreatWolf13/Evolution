package tgw.evolution.hooks;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

import java.util.Map;

public final class ChunkHooks {

    private ChunkHooks() {}

    /**
     * Fixes MC-194811
     * When a structure mod is removed, this map may contain null keys. This will make the world unable to save if this persists.
     * If we remove a structure from the save data in this way, we then mark the chunk for saving
     */
    public static void fixNullStructureReferences(ChunkAccess chunk, Map<ConfiguredStructureFeature<?, ?>, LongSet> structureReferences) {
        if (structureReferences.remove(null) != null) {
            chunk.setUnsaved(true);
        }
        chunk.setAllReferences(structureReferences);
    }
}
