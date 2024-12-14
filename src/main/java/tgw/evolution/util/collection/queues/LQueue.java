package tgw.evolution.util.collection.queues;

import tgw.evolution.util.collection.lists.LList;

public interface LQueue extends QueueExtension {

    long dequeue();

    long dequeueLast();

    void enqueue(long k);

    void enqueueFirst(long k);

    void enqueueMany(LList list);

    long getIteration(long it);

    long peek();

    long peekLast();

    long removeIteration(long it);
}
