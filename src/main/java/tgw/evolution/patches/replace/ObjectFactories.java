package tgw.evolution.patches.replace;

import net.minecraft.world.level.entity.ChunkEntities;
import org.jetbrains.annotations.Contract;
import tgw.evolution.util.collection.lists.OList;

public final class ObjectFactories {

    private ObjectFactories() {
    }

    @Contract(value = "_, _ -> new")
    public static <T> ChunkEntities<T> newChunkEntities(long chunkPos, OList<T> entities) {
        //noinspection Contract
        throw new AbstractMethodError();
    }
}
