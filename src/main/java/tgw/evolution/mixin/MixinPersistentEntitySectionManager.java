package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.patches.PatchEntity;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager<T extends EntityAccess> {

    @Unique private final Consumer<T> entityAdder = e -> {
        this.addEntity(e, true);
        ((PatchEntity) e).onAddedToWorld();
    };
    @Shadow @Final private Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses;
    @Shadow @Final private Queue<ChunkEntities<T>> loadingInbox;

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
}
