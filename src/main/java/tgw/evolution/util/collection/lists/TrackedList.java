package tgw.evolution.util.collection.lists;

import net.minecraft.client.gui.components.AbstractSelectionList;

public final class TrackedList<E extends AbstractSelectionList.Entry<E>> extends OArrayList<E> {

    private final AbstractSelectionList<E> parent;

    public TrackedList(AbstractSelectionList<E> parent) {
        this.parent = parent;
    }

    @Override
    public void add(int index, E entry) {
        super.add(index, entry);
        this.parent.bindEntryToSelf(entry);
    }

    @Override
    public E set(int index, E entry) {
        E e = super.set(index, entry);
        this.parent.bindEntryToSelf(entry);
        return e;
    }
}
