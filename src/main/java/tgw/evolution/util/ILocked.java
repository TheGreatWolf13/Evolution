package tgw.evolution.util;

public interface ILocked extends AutoCloseable {

    boolean isLocked();

    void lock();
}
