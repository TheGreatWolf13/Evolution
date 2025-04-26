package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchSectionStorage;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LLinkedHashSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@Mixin(SectionStorage.class)
public abstract class Mixin_CFM_SectionStorage<R> implements AutoCloseable, PatchSectionStorage<R> {

    @Unique private static final Object NULL = new Object();
    @Shadow @Final private static Logger LOGGER;
    @Mutable @Shadow @Final @RestoreFinal private Function<Runnable, Codec<R>> codec;
    @Shadow @Final @DeleteField private LongLinkedOpenHashSet dirty;
    @Unique private final LLinkedHashSet dirty_;
    @Mutable @Shadow @Final @RestoreFinal private Function<Runnable, R> factory;
    @Mutable @Shadow @Final @RestoreFinal private DataFixer fixerUpper;
    @Mutable @Shadow @Final @RestoreFinal protected LevelHeightAccessor levelHeightAccessor;
    @Shadow @Final @DeleteField private Long2ObjectMap<Optional<R>> storage;
    @Unique private final L2OMap<R> storage_;
    @Mutable @Shadow @Final @RestoreFinal private DataFixTypes type;
    @Mutable @Shadow @Final @RestoreFinal private IOWorker worker;

    @ModifyConstructor
    public Mixin_CFM_SectionStorage(Path path, Function<Runnable, Codec<R>> function, Function<Runnable, R> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl, LevelHeightAccessor levelHeightAccessor) {
        this.storage_ = new L2OHashMap<>();
        this.dirty_ = new LLinkedHashSet();
        this.codec = function;
        this.factory = function2;
        this.fixerUpper = dataFixer;
        this.type = dataFixTypes;
        this.levelHeightAccessor = levelHeightAccessor;
        this.worker = new IOWorker(path, bl, path.getFileName().toString());
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static long getKey(ChunkPos chunkPos, int i) {
        throw new AbstractMethodError();
    }

    @Unique
    private static long getKey_(int chunkX, int chunkZ, int secY) {
        return SectionPos.asLong(chunkX, secY, chunkZ);
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static int getVersion(Dynamic<?> dynamic) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void flush(ChunkPos chunkPos) {
        Evolution.deprecatedMethod();
        this.flush_(chunkPos.toLong());
    }

    @Override
    public void flush_(long chunkPos) {
        if (this.hasWork()) {
            int chunkX = ChunkPos.getX(chunkPos);
            int chunkZ = ChunkPos.getZ(chunkPos);
            for (int secY = this.levelHeightAccessor.getMinSection(); secY < this.levelHeightAccessor.getMaxSection(); ++secY) {
                long secPos = getKey_(chunkX, chunkZ, secY);
                if (this.dirty_.contains(secPos)) {
                    this.writeColumn_(chunkPos);
                    return;
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable Optional<R> get(long secPos) {
        Evolution.deprecatedMethod();
        R r = this.storage_.get(secPos);
        if (r == null) {
            //noinspection OptionalAssignedToNull,ReturnOfNull Yes, I know, this is dumb as fuck
            return null;
        }
        return Optional.ofNullable(r == NULL ? null : r);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public R getOrCreate(long secPos) {
        if (this.outsideStoredRange(secPos)) {
            throw Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        }
        R r = this.getOrLoad_(secPos);
        if (r != null) {
            return r;
        }
        r = this.factory.apply(() -> this.setDirty(secPos));
        this.storage_.put(secPos, r);
        return r;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Optional<R> getOrLoad(long secPos) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.getOrLoad_(secPos));
    }

    @Override
    public @Nullable R getOrLoad_(long secPos) {
        if (this.outsideStoredRange(secPos)) {
            return null;
        }
        R r = this.storage_.get(secPos);
        if (r != null) {
            return r == NULL ? null : r;
        }
        this.readColumn_(ChunkPos.asLong(SectionPos.x(secPos), SectionPos.z(secPos)));
        r = this.storage_.get(secPos);
        if (r == null) {
            throw Util.pauseInIde(new IllegalStateException());
        }
        return r == NULL ? null : r;
    }

    @Override
    public @Nullable R get_(long secPos) {
        R r = this.storage_.get(secPos);
        return r == NULL ? null : r;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean hasWork() {
        return !this.dirty_.isEmpty();
    }

    @Shadow
    protected abstract void onSectionLoad(long l);

    @Shadow
    protected abstract boolean outsideStoredRange(long l);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private void readColumn(ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private <T> void readColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps, @Nullable T object) {
        throw new AbstractMethodError();
    }

    @Unique
    private void readColumn_(long chunkPos) {
        this.readColumn_(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), this.tryRead_(chunkPos));
    }

    @Unique
    private void readColumn_(int chunkX, int chunkZ, @Nullable CompoundTag tag) {
        if (tag == null) {
            for (int secY = this.levelHeightAccessor.getMinSection(); secY < this.levelHeightAccessor.getMaxSection(); ++secY) {
                this.storage_.put(getKey_(chunkX, chunkZ, secY), (R) NULL);
            }
        }
        else {
            Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, tag);
            int version = getVersion(dynamic);
            int worldVersion = SharedConstants.getCurrentVersion().getWorldVersion();
            boolean versionMismatch = version != worldVersion;
            dynamic = this.fixerUpper.update(this.type.getType(), dynamic, version, worldVersion);
            OptionalDynamic<Tag> optionalDynamic = dynamic.get("Sections");
            for (int secY = this.levelHeightAccessor.getMinSection(); secY < this.levelHeightAccessor.getMaxSection(); ++secY) {
                long secPos = getKey_(chunkX, chunkZ, secY);
                //noinspection ObjectAllocationInLoop
                Optional<R> optional = optionalDynamic.get(Integer.toString(secY)).result().flatMap(d -> this.codec.apply(() -> this.setDirty(secPos)).parse(d).resultOrPartial(LOGGER::error));
                this.storage_.put(secPos, optional.orElse((R) NULL));
                if (optional.isPresent()) {
                    this.onSectionLoad(secPos);
                    if (versionMismatch) {
                        this.setDirty(secPos);
                    }
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void setDirty(long secPos) {
        R r = this.storage_.get(secPos);
        if (r != null && r != NULL) {
            this.dirty_.add(secPos);
        }
        else {
            LOGGER.warn("No data for position: [{}, {}, {}]", SectionPos.x(secPos), SectionPos.y(secPos), SectionPos.z(secPos));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void tick(BooleanSupplier hasTime) {
        while (this.hasWork() && hasTime.getAsBoolean()) {
            long secPos = this.dirty.firstLong();
            this.writeColumn_(ChunkPos.asLong(SectionPos.x(secPos), SectionPos.z(secPos)));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private @Nullable CompoundTag tryRead(ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    @Unique
    private @Nullable CompoundTag tryRead_(long chunkPos) {
        try {
            return this.worker.load_(chunkPos);
        }
        catch (IOException e) {
            LOGGER.error("Error reading chunk [{}, {}] data from disk", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), e);
            return null;
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private void writeColumn(ChunkPos chunkPos) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private <T> Dynamic<T> writeColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
        throw new AbstractMethodError();
    }

    @Unique
    private void writeColumn_(long chunkPos) {
        CompoundTag sections = new CompoundTag();
        int chunkX = ChunkPos.getX(chunkPos);
        int chunkZ = ChunkPos.getZ(chunkPos);
        for (int secY = this.levelHeightAccessor.getMinSection(); secY < this.levelHeightAccessor.getMaxSection(); ++secY) {
            long secPos = getKey_(chunkX, chunkZ, secY);
            this.dirty_.remove(secPos);
            R r = this.storage_.get(secPos);
            if (r != null && r != NULL) {
                //noinspection ObjectAllocationInLoop
                DataResult<Tag> dataResult = this.codec.apply(() -> this.setDirty(secPos)).encodeStart(NbtOps.INSTANCE, r);
                //noinspection ObjectAllocationInLoop
                Optional<Tag> resultOrDumbShit = dataResult.resultOrPartial(LOGGER::error);
                if (resultOrDumbShit.isPresent()) {
                    //noinspection ObjectAllocationInLoop
                    sections.put(Integer.toString(secY), resultOrDumbShit.get());
                }
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Sections", sections);
        tag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        this.worker.store_(chunkPos, tag);
    }
}
