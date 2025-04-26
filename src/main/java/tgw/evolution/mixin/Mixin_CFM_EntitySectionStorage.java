package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchEntitySectionStorage;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LBTreeSet;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Mixin(EntitySectionStorage.class)
public abstract class Mixin_CFM_EntitySectionStorage<T extends EntityAccess> implements PatchEntitySectionStorage<T> {

    @Mutable @Shadow @Final @RestoreFinal private Class<T> entityClass;
    @Mutable @Shadow @Final @RestoreFinal private Long2ObjectFunction<Visibility> intialSectionVisibility;
    @Shadow @Final @DeleteField private LongSortedSet sectionIds;
    @Unique private final LBTreeSet sectionIds_;
    @Shadow @Final @DeleteField private Long2ObjectMap<EntitySection<T>> sections;
    @Unique private final L2OMap<EntitySection> sections_;

    @ModifyConstructor
    public Mixin_CFM_EntitySectionStorage(Class<T> class_, Long2ObjectFunction<Visibility> long2ObjectFunction) {
        this.sections_ = new L2OHashMap<>();
        this.sectionIds_ = new LBTreeSet();
        this.entityClass = class_;
        this.intialSectionVisibility = long2ObjectFunction;
    }

    @Contract(value = "_ -> _", pure = true)
    @Shadow
    private static long getChunkKeyFromSectionKey(long l) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private EntitySection<T> createSection(long l) {
        long m = getChunkKeyFromSectionKey(l);
        Visibility visibility = this.intialSectionVisibility.get(m);
        this.sectionIds_.add(l);
        return new EntitySection<>(this.entityClass, visibility);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void forEachAccessibleNonEmptySection(AABB bb, Consumer<EntitySection> consumer) {
        int minX = SectionPos.posToSectionCoord(bb.minX - 2);
        int minY = SectionPos.posToSectionCoord(bb.minY - 2);
        int minZ = SectionPos.posToSectionCoord(bb.minZ - 2);
        int maxX = SectionPos.posToSectionCoord(bb.maxX + 2);
        int maxY = SectionPos.posToSectionCoord(bb.maxY + 2);
        int maxZ = SectionPos.posToSectionCoord(bb.maxZ + 2);
        for (int secX = minX; secX <= maxX; ++secX) {
            //noinspection ObjectAllocationInLoop
            this.sectionIds_.forEach(secPos -> {
                int secY = SectionPos.y(secPos);
                if (secY >= minY && secY <= maxY) {
                    int secZ = SectionPos.z(secPos);
                    if (secZ >= minZ && secZ <= maxZ) {
                        EntitySection entitySection = this.sections_.get(secPos);
                        if (entitySection != null && !entitySection.isEmpty() && entitySection.getStatus().isAccessible()) {
                            consumer.accept(entitySection);
                        }
                    }
                }
            }, SectionPos.asLong(secX, 0, 0), SectionPos.asLong(secX, -1, -1) + 1L);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public LongSet getAllChunksWithExistingSections() {
        Evolution.deprecatedMethod();
        return this.getAllChunksWithExistingSections_();
    }

    @Override
    public LSet getAllChunksWithExistingSections_() {
        LSet set = new LHashSet();
        L2OMap<EntitySection> sections = this.sections_;
        for (long it = sections.beginIteration(); sections.hasNextIteration(it); it = sections.nextEntry(it)) {
            long secPos = sections.getIterationKey(it);
            set.add(getChunkKeyFromSectionKey(secPos));
        }
        return set;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @DeleteMethod
    @Overwrite
    private LongSortedSet getChunkSections(int i, int j) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public LongStream getExistingSectionPositionsInChunk(long chunkPos) {
        Evolution.deprecatedMethod();
        return LongStream.empty();
    }

    @Override
    public void getExistingSectionPositionsInChunk_(long chunkPos, LongConsumer sectionConsumer) {
        int chunkX = ChunkPos.getX(chunkPos);
        int chunkZ = ChunkPos.getZ(chunkPos);
        this.sectionIds_.forEach(sectionConsumer, SectionPos.asLong(chunkX, 0, chunkZ), SectionPos.asLong(chunkX, -1, chunkZ));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Stream<EntitySection<T>> getExistingSectionsInChunk(long l) {
        Evolution.deprecatedMethod();
        return Stream.empty();
    }

    @Override
    public void getExistingSectionsInChunk_(long chunkPos, Consumer<EntitySection<T>> consumer) {
        this.getExistingSectionPositionsInChunk_(chunkPos, secPos -> consumer.accept(this.sections_.get(secPos)));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public EntitySection<T> getOrCreateSection(long l) {
        EntitySection entitySection = this.sections_.get(l);
        if (entitySection == null) {
            entitySection = this.createSection(l);
            this.sections_.put(l, entitySection);
        }
        return entitySection;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable EntitySection<T> getSection(long l) {
        return this.sections_.get(l);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void remove(long l) {
        this.sections_.remove(l);
        this.sectionIds_.remove(l);
    }
}
