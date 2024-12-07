package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.*;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.PatchEntity;

import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager<T extends EntityAccess> {

    @Shadow @Final private Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses;
    @Unique private final Consumer<T> entityAdder = e -> {
        this.addEntity(e, true);
        ((PatchEntity) e).onAddedToWorld();
    };
    @Shadow @Final private Queue<ChunkEntities<T>> loadingInbox;
    @Shadow @Final private EntityPersistentStorage<T> permanentStorage;
    @Shadow @Final EntitySectionStorage<T> sectionStorage;

    @Shadow
    protected abstract boolean addEntity(T entityAccess, boolean bl);

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    public void addLegacyChunkEntities(Stream<T> stream) {
        stream.forEach(this.entityAdder);
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    public void addWorldGenChunkEntities(Stream<T> stream) {
        stream.forEach(this.entityAdder);
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    private void processPendingLoads() {
        for (ChunkEntities<T> chunkEntities = this.loadingInbox.poll(); chunkEntities != null; chunkEntities = this.loadingInbox.poll()) {
            chunkEntities.getEntities().forEach(this.entityAdder);
            this.chunkLoadStatuses.put(chunkEntities.getPos().toLong(), PersistentEntitySectionManager.ChunkLoadStatus.LOADED);
        }
    }

    @Shadow
    protected abstract void requestChunkLoad(long l);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private boolean storeChunkSections(long pos, Consumer<T> consumer) {
        PersistentEntitySectionManager.ChunkLoadStatus status = this.chunkLoadStatuses.get(pos);
        if (status == PersistentEntitySectionManager.ChunkLoadStatus.PENDING) {
            return false;
        }
        List<T> list = this.sectionStorage.getExistingSectionsInChunk(pos).flatMap(entitySection -> entitySection.getEntities().filter(EntityAccess::shouldBeSaved)).collect(Collectors.toList());
        if (list.isEmpty()) {
            if (status == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
                this.permanentStorage.storeEntities(new ChunkEntities<>(new ChunkPos(pos), ImmutableList.of()));
            }
            return true;
        }
        if (status == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(pos);
            return false;
        }
        this.permanentStorage.storeEntities(new ChunkEntities<>(new ChunkPos(pos), list));
        list.forEach(consumer);
        return true;
    }
}
