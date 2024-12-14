package tgw.evolution.patches;

import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.sets.LHashSet;

public interface PatchStorage {

    default void cameraDrop(int index, LevelChunk chunk) {
        throw new AbstractMethodError();
    }

    default void cameraReplace(int index, @Nullable LevelChunk chunk) {
        throw new AbstractMethodError();
    }

    default void drop(int index, LevelChunk chunk) {
        throw new AbstractMethodError();
    }

    default int getCamViewCenterX() {
        throw new AbstractMethodError();
    }

    default int getCamViewCenterZ() {
        throw new AbstractMethodError();
    }

    default @Nullable LevelChunk getCameraChunk(int index) {
        throw new AbstractMethodError();
    }

    default int getCameraChunksLength() {
        throw new AbstractMethodError();
    }

    default int getCameraIndex(int x, int z) {
        throw new AbstractMethodError();
    }

    default LHashSet getLoadedEmptySections() {
        throw new AbstractMethodError();
    }

    default boolean inCameraRange(int x, int z) {
        throw new AbstractMethodError();
    }

    default void onSectionEmptinessChanged(int secX, int secY, int secZ, boolean empty) {
        throw new AbstractMethodError();
    }

    default void setCamViewCenter(int x, int z) {
        throw new AbstractMethodError();
    }
}
