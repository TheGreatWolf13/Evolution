package tgw.evolution.patches.replace;

import tgw.evolution.util.collection.lists.OList;

public class ChunkEntities<T> {

    private final OList<T> entities;
    private final long pos;

    public ChunkEntities(long chunkPos, OList<T> entities) {
        this.pos = chunkPos;
        this.entities = entities;
    }

    public OList<T> getEntities() {
        return this.entities.view();
    }

    public long getPos() {
        return this.pos;
    }

    public boolean isEmpty() {
        return this.entities.isEmpty();
    }
}
