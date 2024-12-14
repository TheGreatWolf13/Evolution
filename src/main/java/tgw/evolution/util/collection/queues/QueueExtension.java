package tgw.evolution.util.collection.queues;

public interface QueueExtension {

    long beginIteration();

    void clear();

    /**
     * Use when fast iteration is interrupted early (like when using a {@code break} on a loop).
     */
    void endIteration();

    default boolean hasNextIteration(long it) {
        return it != 0;
    }

    default boolean isEmpty() {
        return this.size() == 0;
    }
    
    long nextEntry(long it);

    int size();

    void trim();
}
