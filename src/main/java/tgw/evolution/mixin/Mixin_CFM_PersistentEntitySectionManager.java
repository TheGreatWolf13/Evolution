package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchPersistentEntitySectionManager;
import tgw.evolution.patches.replace.ChunkEntities;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;
import tgw.evolution.util.collection.sets.OHashSet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(PersistentEntitySectionManager.class)
public abstract class Mixin_CFM_PersistentEntitySectionManager<T extends EntityAccess> implements PatchPersistentEntitySectionManager {

    @Shadow @Final static Logger LOGGER;
    @Mutable @Shadow @Final @RestoreFinal LevelCallback<T> callbacks;
    @Shadow @Final @DeleteField private Long2ObjectMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses;
    @Unique private final L2OMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses_;
    @Shadow @Final @DeleteField private Long2ObjectMap<Visibility> chunkVisibility;
    @Unique private final L2OMap<Visibility> chunkVisibility_;
    @Shadow @Final @DeleteField private LongSet chunksToUnload;
    @Unique private final LSet chunksToUnload_;
    @Mutable @Shadow @Final @RestoreFinal private LevelEntityGetter<T> entityGetter;
    @Mutable @Shadow @Final @RestoreFinal Set<UUID> knownUuids;
    @Shadow @Final @DeleteField private Queue<net.minecraft.world.level.entity.ChunkEntities<T>> loadingInbox;
    @Unique private final Queue<ChunkEntities<T>> loadingInbox_;
    @Mutable @Shadow @Final @RestoreFinal private EntityPersistentStorage<T> permanentStorage;
    @Mutable @Shadow @Final @RestoreFinal EntitySectionStorage<T> sectionStorage;
    @Mutable @Shadow @Final @RestoreFinal private EntityLookup<T> visibleEntityStorage;

    @ModifyConstructor
    public Mixin_CFM_PersistentEntitySectionManager(Class<T> clazz, LevelCallback<T> levelCallback, EntityPersistentStorage<T> persistentStorage) {
        this.knownUuids = new OHashSet<>();
        this.visibleEntityStorage = new EntityLookup<>();
        this.chunkVisibility_ = new L2OHashMap<>();
        this.chunkLoadStatuses_ = new L2OHashMap<>();
        this.chunksToUnload_ = new LHashSet();
        this.loadingInbox_ = new ConcurrentLinkedQueue<>();
        this.sectionStorage = new EntitySectionStorage<>(clazz, this.chunkVisibility_);
        this.chunkVisibility_.defaultReturnValue(Visibility.HIDDEN);
        this.chunkLoadStatuses_.defaultReturnValue(PersistentEntitySectionManager.ChunkLoadStatus.FRESH);
        this.callbacks = levelCallback;
        this.permanentStorage = persistentStorage;
        this.entityGetter = new LevelEntityGetterAdapter<>(this.visibleEntityStorage, this.sectionStorage);
    }

    @Shadow
    protected abstract boolean addEntity(T entityAccess, boolean bl);

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    public void addLegacyChunkEntities(Stream<T> stream) {
        stream.forEach(e -> {
            this.addEntity(e, true);
            e.onAddedToWorld();
        });
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    public void addWorldGenChunkEntities(Stream<T> stream) {
        stream.forEach(e -> {
            this.addEntity(e, true);
            e.onAddedToWorld();
        });
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean areEntitiesLoaded(long chunkPos) {
        return this.chunkLoadStatuses_.get(chunkPos) == PersistentEntitySectionManager.ChunkLoadStatus.LOADED;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void autoSave() {
        LSet set = this.getAllChunksToSave_();
        for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
            long pos = set.getIteration(it);
            if (this.chunkVisibility_.get(pos) == Visibility.HIDDEN) {
                this.processChunkUnload(pos);
            }
            else {
                this.storeChunkSections(pos, e -> {});
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    public boolean canPositionTick(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.canPositionTick_BlockPos(pos.getX(), pos.getZ());
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    public boolean canPositionTick(ChunkPos pos) {
        Evolution.deprecatedMethod();
        return this.canPositionTick_ChunkPos(pos.toLong());
    }

    @Override
    public boolean canPositionTick_BlockPos(int x, int z) {
        return this.chunkVisibility_.get(ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z))).isTicking();
    }

    @Override
    public boolean canPositionTick_ChunkPos(long chunkPos) {
        return this.chunkVisibility_.get(chunkPos).isTicking();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void dumpSections(Writer writer) throws IOException {
        CsvOutput csvOutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("visibility").addColumn("load_status").addColumn("entity_count").build(writer);
        LSet set = this.sectionStorage.getAllChunksWithExistingSections_();
        for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
            long chunkPos = set.getIteration(it);
            PersistentEntitySectionManager.ChunkLoadStatus chunkLoadStatus = this.chunkLoadStatuses_.get(chunkPos);
            //noinspection ObjectAllocationInLoop
            this.sectionStorage.getExistingSectionPositionsInChunk_(chunkPos, secPos -> {
                EntitySection<T> entitySection = this.sectionStorage.getSection(secPos);
                if (entitySection != null) {
                    try {
                        csvOutput.writeRow(SectionPos.x(secPos), SectionPos.y(secPos), SectionPos.z(secPos), entitySection.getStatus(), chunkLoadStatus, entitySection.size());
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void ensureChunkQueuedForLoad(long chunkPos) {
        if (this.chunkLoadStatuses_.get(chunkPos) == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(chunkPos);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @VisibleForDebug
    public String gatherStats() {
        return this.knownUuids.size() + "," + this.visibleEntityStorage.count() + "," + this.sectionStorage.count() + "," + this.chunkLoadStatuses_.size() + "," + this.chunkVisibility_.size() + "," + this.loadingInbox_.size() + "," + this.chunksToUnload_.size();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private LongSet getAllChunksToSave() {
        throw new AbstractMethodError();
    }

    @Unique
    private LSet getAllChunksToSave_() {
        LSet set = this.sectionStorage.getAllChunksWithExistingSections_();
        L2OMap<PersistentEntitySectionManager.ChunkLoadStatus> chunkLoadStatuses = this.chunkLoadStatuses_;
        for (long it = chunkLoadStatuses.beginIteration(); chunkLoadStatuses.hasNextIteration(it); it = chunkLoadStatuses.nextEntry(it)) {
            if (chunkLoadStatuses.getIterationValue(it) == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
                set.add(chunkLoadStatuses.getIterationKey(it));
            }
        }
        return set;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private boolean processChunkUnload(long chunkPos) {
        if (!this.storeChunkSections(chunkPos, e -> e.getPassengersAndSelf().forEach(this::unloadEntity))) {
            return false;
        }
        this.chunkLoadStatuses_.remove(chunkPos);
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Call onAddedToWorld on entities.
     */
    @Overwrite
    private void processPendingLoads() {
        Queue<ChunkEntities<T>> loadingInbox = this.loadingInbox_;
        for (ChunkEntities<T> chunkEntities = loadingInbox.poll(); chunkEntities != null; chunkEntities = loadingInbox.poll()) {
            OList<T> entities = chunkEntities.getEntities();
            for (int i = 0, len = entities.size(); i < len; ++i) {
                T t = entities.get(i);
                this.addEntity(t, true);
                t.onAddedToWorld();
            }
            this.chunkLoadStatuses_.put(chunkEntities.getPos(), PersistentEntitySectionManager.ChunkLoadStatus.LOADED);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void processUnloads() {
        LSet chunksToUnload = this.chunksToUnload_;
        for (long it = chunksToUnload.beginIteration(); chunksToUnload.hasNextIteration(it); it = chunksToUnload.nextEntry(it)) {
            long pos = chunksToUnload.getIteration(it);
            if (this.chunkVisibility_.get(pos) != Visibility.HIDDEN || this.processChunkUnload(pos)) {
                it = chunksToUnload.removeIteration(it);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void requestChunkLoad(long chunkPos) {
        this.chunkLoadStatuses_.put(chunkPos, PersistentEntitySectionManager.ChunkLoadStatus.PENDING);
        this.permanentStorage.loadEntities_(chunkPos).thenAccept(this.loadingInbox_::add).exceptionally(throwable -> {
            LOGGER.error("Failed to read chunk at [{}, {}]", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), throwable);
            return null;
        });
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void saveAll() {
        LSet set = this.getAllChunksToSave_();
        while (!set.isEmpty()) {
            this.permanentStorage.flush(false);
            this.processPendingLoads();
            for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
                long pos = set.getIteration(it);
                if (this.chunkVisibility_.get(pos) == Visibility.HIDDEN) {
                    if (this.processChunkUnload(pos)) {
                        it = set.removeIteration(it);
                    }
                }
                else {
                    if (this.storeChunkSections(pos, e -> {})) {
                        it = set.removeIteration(it);
                    }
                }

            }
        }
        this.permanentStorage.flush(true);
    }

    @Shadow
    abstract void startTicking(T entityAccess);

    @Shadow
    abstract void startTracking(T entityAccess);

    @Shadow
    abstract void stopTicking(T entityAccess);

    @Shadow
    abstract void stopTracking(T entityAccess);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private boolean storeChunkSections(long pos, Consumer<T> consumer) {
        PersistentEntitySectionManager.ChunkLoadStatus status = this.chunkLoadStatuses_.get(pos);
        if (status == PersistentEntitySectionManager.ChunkLoadStatus.PENDING) {
            return false;
        }
        OList<T> list = new OArrayList<>();
        this.sectionStorage.getExistingSectionsInChunk_(pos, section -> {
            //noinspection OverlyStrongTypeCast
            OList<T> entities = ((EntitySection<T>) section).getEntities_();
            for (int i = 0, len = entities.size(); i < len; ++i) {
                T t = entities.get(i);
                if (t.shouldBeSaved()) {
                    list.add(t);
                }
            }
        });
        if (list.isEmpty()) {
            if (status == PersistentEntitySectionManager.ChunkLoadStatus.LOADED) {
                this.permanentStorage.storeEntities_(new ChunkEntities<>(pos, OList.emptyList()));
            }
            return true;
        }
        if (status == PersistentEntitySectionManager.ChunkLoadStatus.FRESH) {
            this.requestChunkLoad(pos);
            return false;
        }
        this.permanentStorage.storeEntities_(new ChunkEntities<>(pos, list));
        for (int i = 0, len = list.size(); i < len; ++i) {
            consumer.accept(list.get(i));
        }
        return true;
    }

    @Shadow
    protected abstract void unloadEntity(EntityAccess entityAccess);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void updateChunkStatus(ChunkPos pos, Visibility visibility) {
        Evolution.deprecatedMethod();
        this.updateChunkStatus_(pos.toLong(), visibility);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void updateChunkStatus(ChunkPos pos, ChunkHolder.FullChunkStatus fullChunkStatus) {
//        Evolution.deprecatedMethod();
        this.updateChunkStatus_(pos.toLong(), fullChunkStatus);
    }

    @Override
    public void updateChunkStatus_(long chunkPos, ChunkHolder.FullChunkStatus fullChunkStatus) {
        this.updateChunkStatus_(chunkPos, Visibility.fromFullChunkStatus(fullChunkStatus));
    }

    @Override
    public void updateChunkStatus_(long chunkPos, Visibility visibility) {
        if (visibility == Visibility.HIDDEN) {
            this.chunkVisibility_.remove(chunkPos);
            this.chunksToUnload_.add(chunkPos);
        }
        else {
            this.chunkVisibility_.put(chunkPos, visibility);
            this.chunksToUnload_.remove(chunkPos);
            this.ensureChunkQueuedForLoad(chunkPos);
        }
        this.sectionStorage.getExistingSectionsInChunk_(chunkPos, s -> {
            EntitySection<T> section = (EntitySection<T>) s;
            Visibility newVisibility = section.updateChunkStatus(visibility);
            boolean isAccessible = newVisibility.isAccessible();
            boolean wasAccessible = visibility.isAccessible();
            boolean isTicking = newVisibility.isTicking();
            boolean wasTicking = visibility.isTicking();
            OList<T> entities = section.getEntities_();
            if (isTicking && !wasTicking) {
                for (int i = 0, len = entities.size(); i < len; ++i) {
                    T t = entities.get(i);
                    if (!t.isAlwaysTicking()) {
                        this.stopTicking(t);
                    }
                }
            }
            if (isAccessible && !wasAccessible) {
                for (int i = 0, len = entities.size(); i < len; ++i) {
                    T t = entities.get(i);
                    if (!t.isAlwaysTicking()) {
                        this.stopTracking(t);
                    }
                }
            }
            else if (!isAccessible && wasAccessible) {
                for (int i = 0, len = entities.size(); i < len; ++i) {
                    T t = entities.get(i);
                    if (!t.isAlwaysTicking()) {
                        this.startTracking(t);
                    }
                }
            }
            if (!isTicking && wasTicking) {
                for (int i = 0, len = entities.size(); i < len; ++i) {
                    T t = entities.get(i);
                    if (!t.isAlwaysTicking()) {
                        this.startTicking(t);
                    }
                }
            }
        });
    }
}
