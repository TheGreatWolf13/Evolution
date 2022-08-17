package tgw.evolution.util.collection;

public interface ICollectionExtension {

    void clear();

    default void reset() {
        this.clear();
        this.trimCollection();
    }

    void trimCollection();
}
