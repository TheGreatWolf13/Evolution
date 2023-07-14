package tgw.evolution.util.collection;

public interface ICollectionExtension {

    boolean CHECKS = false;

    void clear();

    default void reset() {
        this.clear();
        this.trimCollection();
    }

    void trimCollection();
}
