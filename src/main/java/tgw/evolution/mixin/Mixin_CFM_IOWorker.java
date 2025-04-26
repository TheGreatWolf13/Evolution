package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchIOWorker;
import tgw.evolution.patches.replace.RegionFileStorage;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.maps.L2OLinkedHashMap;
import tgw.evolution.util.collection.maps.L2OMap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(IOWorker.class)
public abstract class Mixin_CFM_IOWorker implements ChunkScanAccess, AutoCloseable, PatchIOWorker {

    @Shadow @Final private static Logger LOGGER;
    @Mutable @Shadow @Final @RestoreFinal private ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
    @Shadow @Final @DeleteField private Map<ChunkPos, IOWorker.PendingStore> pendingWrites;
    @Unique private final L2OMap<IOWorker.PendingStore> pendingWrites_;
    @Mutable @Shadow @Final @RestoreFinal private AtomicBoolean shutdownRequested;
    @Shadow @Final @DeleteField private net.minecraft.world.level.chunk.storage.RegionFileStorage storage;
    @Unique private final RegionFileStorage storage_;

    @ModifyConstructor
    public Mixin_CFM_IOWorker(Path path, boolean sync, String string) {
        this.shutdownRequested = new AtomicBoolean();
        this.pendingWrites_ = new L2OLinkedHashMap<>();
        this.storage_ = new RegionFileStorage(path, sync);
        this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(ArrayHelper.PRIORITIES.length), Util.ioPool(), "IOWorker-" + string);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public void close() throws IOException {
        if (!this.shutdownRequested.compareAndSet(false, true)) {
            return;
        }
        this.mailbox.ask(processorHandle -> new StrictQueue.IntRunnable(IOWorker.Priority.SHUTDOWN.ordinal(), () -> processorHandle.tell(Unit.INSTANCE))).join();
        this.mailbox.close();
        try {
            this.storage_.close();
        }
        catch (Exception e) {
            LOGGER.error("Failed to close storage", e);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable CompoundTag load(ChunkPos chunkPos) throws IOException {
        Evolution.deprecatedMethod();
        return this.load_(chunkPos.toLong());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public CompletableFuture<CompoundTag> loadAsync(ChunkPos pos) {
        Evolution.deprecatedMethod();
        return this.loadAsync_(pos.toLong());
    }

    @Override
    public CompletableFuture<CompoundTag> loadAsync_(long chunkPos) {
        return this.submitTask(() -> {
            IOWorker.PendingStore pendingStore = this.pendingWrites_.get(chunkPos);
            if (pendingStore != null) {
                return Either.left(pendingStore.data);
            }
            int chunkX = ChunkPos.getX(chunkPos);
            int chunkZ = ChunkPos.getZ(chunkPos);
            try {
                CompoundTag compoundTag = this.storage_.read(chunkX, chunkZ);
                return Either.left(compoundTag);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to read chunk [{}, {}]", chunkX, chunkZ, e);
                return Either.right(e);
            }
        });
    }

    @Override
    public @Nullable CompoundTag load_(long chunkPos) throws IOException {
        CompletableFuture<CompoundTag> future = this.loadAsync_(chunkPos);
        try {
            return future.join();
        }
        catch (CompletionException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw e;
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private void runStore(ChunkPos chunkPos, IOWorker.PendingStore pendingStore) {
        throw new AbstractMethodError();
    }

    @Unique
    private void runStore_(int chunkX, int chunkZ, IOWorker.PendingStore pendingStore) {
        try {
            this.storage_.write(chunkX, chunkZ, pendingStore.data);
            pendingStore.result.complete(null);
        }
        catch (Exception e) {
            LOGGER.error("Failed to store chunk [{}, {}]", chunkX, chunkZ, e);
            pendingStore.result.completeExceptionally(e);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public CompletableFuture<Void> scanChunk(ChunkPos chunkPos, StreamTagVisitor visitor) {
        Evolution.deprecatedMethod();
        return this.scanChunk_(chunkPos.toLong(), visitor);
    }

    @Override
    public CompletableFuture<Void> scanChunk_(long chunkPos, StreamTagVisitor visitor) {
        return this.submitTask(() -> {
            int chunkX = ChunkPos.getX(chunkPos);
            int chunkZ = ChunkPos.getZ(chunkPos);
            try {
                IOWorker.PendingStore pendingStore = this.pendingWrites_.get(chunkPos);
                if (pendingStore != null) {
                    if (pendingStore.data != null) {
                        pendingStore.data.acceptAsRoot(visitor);
                    }
                }
                else {
                    this.storage_.scanChunk(chunkX, chunkZ, visitor);
                }
                return Either.left(null);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to bulk scan chunk [{}, {}]", chunkX, chunkZ, e);
                return Either.right(e);
            }
        });
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public CompletableFuture<Void> store(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
        Evolution.deprecatedMethod();
        return this.store_(chunkPos.toLong(), compoundTag);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void storePendingChunk() {
        L2OMap<IOWorker.PendingStore> pendingWrites = this.pendingWrites_;
        if (!pendingWrites.isEmpty()) {
            long chunkPos = pendingWrites.getSampleKey();
            IOWorker.PendingStore pendingStore = pendingWrites.getSampleValue();
            pendingWrites.remove(chunkPos);
            this.runStore_(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), pendingStore);
            this.tellStorePending();
        }
    }

    @Override
    public CompletableFuture<Void> store_(long chunkPos, @Nullable CompoundTag tag) {
        return this.submitTask(() -> {
            IOWorker.PendingStore pendingStore = this.pendingWrites_.get(chunkPos);
            if (pendingStore == null) {
                pendingStore = new IOWorker.PendingStore(tag);
                this.pendingWrites_.put(chunkPos, pendingStore);
            }
            else {
                pendingStore.data = tag;
            }
            return Either.left(pendingStore.result);
        }).thenCompose(Function.identity());
    }

    @Shadow
    protected abstract <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> supplier);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public CompletableFuture<Void> synchronize(boolean bl) {
        Supplier<CompletableFuture[]> supplier = () -> {
            L2OMap<IOWorker.PendingStore> pendingWrites = this.pendingWrites_;
            CompletableFuture[] futures = new CompletableFuture[pendingWrites.size()];
            int i = 0;
            for (long it = pendingWrites.beginIteration(); pendingWrites.hasNextIteration(it); it = pendingWrites.nextEntry(it)) {
                futures[i++] = pendingWrites.getIterationValue(it).result;
            }
            return futures;
        };
        CompletableFuture<Void> future = this.submitTask(() -> Either.left(CompletableFuture.allOf(supplier.get()))).thenCompose(Function.identity());
        return bl ? future.thenCompose(v -> this.submitTask(() -> {
            try {
                this.storage_.flush();
                return Either.left(null);
            }
            catch (Exception e) {
                LOGGER.warn("Failed to synchronize chunks", e);
                return Either.right(e);
            }
        })) : future.thenCompose(v -> this.submitTask(() -> Either.left(null)));
    }

    @Shadow
    protected abstract void tellStorePending();
}
