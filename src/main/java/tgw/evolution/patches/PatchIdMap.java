package tgw.evolution.patches;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface PatchIdMap<T> {

    default long beginIteration() {
        throw new AbstractMethodError();
    }

    default T getIteration(long it) {
        throw new AbstractMethodError();
    }

    default ResourceKey<T> getIterationKey(long it) {
        throw new AbstractMethodError();
    }

    default ResourceLocation getIterationLocation(long it) {
        throw new AbstractMethodError();
    }

    default boolean hasNextIteration(long it) {
        throw new AbstractMethodError();
    }

    default long nextEntry(long it) {
        throw new AbstractMethodError();
    }
}
