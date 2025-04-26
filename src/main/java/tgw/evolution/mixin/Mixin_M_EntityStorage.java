package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchEntityType;
import tgw.evolution.patches.mixin.ChunkEntities;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.concurrent.CompletableFuture;

@Mixin(EntityStorage.class)
public abstract class Mixin_M_EntityStorage implements EntityPersistentStorage<Entity> {

    @Shadow @Final private static String ENTITIES_TAG;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static String POSITION_TAG;
    @Shadow @Final private LongSet emptyChunks;
    @Shadow @Final private ProcessorMailbox<Runnable> entityDeserializerQueue;
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private IOWorker worker;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static net.minecraft.world.level.entity.ChunkEntities<Entity> emptyChunk(ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    @Unique
    private static CompoundTag getCompoundTag(ChunkEntities<Entity> chunkEntities) {
        ListTag listTag = new ListTag();
        OList<Entity> entities = chunkEntities.getEntities();
        for (int i = 0, len = entities.size(); i < len; ++i) {
            Entity entity = entities.get(i);
            //noinspection ObjectAllocationInLoop
            CompoundTag compoundTag = new CompoundTag();
            if (entity.save(compoundTag)) {
                listTag.add(compoundTag);
            }
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundTag.put(ENTITIES_TAG, listTag);
        return compoundTag;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static ChunkPos readChunkPos(CompoundTag compoundTag) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static void writeChunkPos(CompoundTag compoundTag, ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public CompletableFuture<net.minecraft.world.level.entity.ChunkEntities<Entity>> loadEntities(ChunkPos chunkPos) {
        Evolution.deprecatedMethod();
        throw new AbstractMethodError();
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities_(long chunkPos) {
        if (this.emptyChunks.contains(chunkPos)) {
            return CompletableFuture.completedFuture(new ChunkEntities<>(chunkPos, OList.emptyList()));
        }
        return this.worker.loadAsync_(chunkPos).thenApplyAsync(compoundTag -> {
            if (compoundTag == null) {
                this.emptyChunks.add(chunkPos);
                return new ChunkEntities<>(chunkPos, OList.emptyList());
            }
            try {
                int[] positions = compoundTag.getIntArray("Position");
                long chunkPos2 = ChunkPos.asLong(positions[0], positions[1]);
                if (chunkPos != chunkPos2) {
                    LOGGER.error("Chunk file at [{}, {}] is in the wrong location. (Expected [{}, {}], got [{}, {}])", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), positions[0], positions[1]);
                }
            }
            catch (Exception e) {
                LOGGER.warn("Failed to parse chunk [{}, {}] position info", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), e);
            }
            CompoundTag tag = this.upgradeChunkTag(compoundTag);
            ListTag listTag = tag.getList("Entities", Tag.TAG_COMPOUND);
            OList<Entity> list = new OArrayList<>();
            PatchEntityType.loadEntitiesRecursive(listTag, this.level, list::add);
            return new ChunkEntities<>(chunkPos, list);
        }, this.entityDeserializerQueue::tell);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void storeEntities(net.minecraft.world.level.entity.ChunkEntities<Entity> chunkEntities) {
        Evolution.deprecatedMethod();
        throw new AbstractMethodError();
    }

    @Override
    public void storeEntities_(ChunkEntities<Entity> chunkEntities) {
        long chunkPos = chunkEntities.getPos();
        if (chunkEntities.isEmpty()) {
            if (this.emptyChunks.add(chunkPos)) {
                this.worker.store_(chunkPos, null);
            }
            return;
        }
        CompoundTag compoundTag = getCompoundTag(chunkEntities);
        compoundTag.put(POSITION_TAG, new IntArrayTag(new int[]{ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos)}));
        this.worker.store_(chunkPos, compoundTag).exceptionally(t -> {
            LOGGER.error("Failed to store chunk [{}, {}]", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), t);
            return null;
        });
        this.emptyChunks.remove(chunkPos);
    }

    @Shadow
    protected abstract CompoundTag upgradeChunkTag(CompoundTag compoundTag);
}
