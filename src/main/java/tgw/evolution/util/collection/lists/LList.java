package tgw.evolution.util.collection.lists;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import tgw.evolution.util.collection.ICollectionExtension;

public interface LList extends LongList, ICollectionExtension {

    LList.EmptyList EMPTY_LIST = new LList.EmptyList();

    class EmptyList extends LongLists.EmptyList implements LList {

        protected EmptyList() {
        }

        @Override
        public void trimCollection() {
        }
    }
}
