package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.collection.maps.O2IHashMap;
import tgw.evolution.util.collection.maps.O2IMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Map;

@Mixin(StructureCheck.class)
public abstract class Mixin_M_StructureCheck {

    @Shadow @Final private Map<ConfiguredStructureFeature<?, ?>, Long2BooleanMap> featureChecks;
    @Shadow @Final private Long2ObjectMap<Object2IntMap<ConfiguredStructureFeature<?, ?>>> loadedChunks;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static Object2IntMap<ConfiguredStructureFeature<?, ?>> deduplicateEmptyMap(Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void onStructureLoad(ChunkPos pos, Map<ConfiguredStructureFeature<?, ?>, StructureStart> map) {
        long chunkPos = pos.toLong();
        O2IMap<ConfiguredStructureFeature<?, ?>> newMap = O2IMap.emptyMap();
        O2OMap<ConfiguredStructureFeature<?, ?>, StructureStart> _map = (O2OMap<ConfiguredStructureFeature<?, ?>, StructureStart>) map;
        for (long it = _map.beginIteration(); _map.hasNextIteration(it); it = _map.nextEntry(it)) {
            StructureStart structureStart = _map.getIterationValue(it);
            if (structureStart.isValid()) {
                if (newMap.isEmpty()) {
                    //noinspection ObjectAllocationInLoop
                    newMap = new O2IHashMap<>();
                }
                newMap.put(_map.getIterationKey(it), structureStart.getReferences());
            }
        }
        this.storeFullResults(chunkPos, newMap);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void storeFullResults(long l, Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap) {
        this.loadedChunks.put(l, object2IntMap);
        this.featureChecks.values().forEach(m -> m.remove(l));
    }
}
