package tgw.evolution.patches;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import tgw.evolution.util.collection.sets.LSet;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

public interface PatchEntitySectionStorage<T extends EntityAccess> {

    default LSet getAllChunksWithExistingSections_() {
        throw new AbstractMethodError();
    }

    default void getExistingSectionPositionsInChunk_(long chunkPos, LongConsumer sectionConsumer) {
        throw new AbstractMethodError();
    }

    default void getExistingSectionsInChunk_(long chunkPos, Consumer<EntitySection<T>> consumer) {
        throw new AbstractMethodError();
    }
}
