package tgw.evolution.util.collection.queues;

import tgw.evolution.util.collection.lists.OList;

public interface OQueue<K> extends QueueExtension {

    K dequeue();

    K dequeueLast();

    void enqueue(K k);

    void enqueueFirst(K k);

    void enqueueMany(OList<K> list);

    K getIteration(long it);

    K peek();

    K peekLast();

    long removeIteration(long it);
}
