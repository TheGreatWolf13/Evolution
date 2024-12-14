package tgw.evolution.util.collection.queues;

import tgw.evolution.util.collection.lists.IList;

public interface IQueue extends QueueExtension {

    int dequeue();

    int dequeueLast();

    void enqueue(int k);

    void enqueueFirst(int k);

    void enqueueMany(IList list);

    int getIteration(long it);

    int peek();

    int peekLast();

    long removeIteration(long it);
}
