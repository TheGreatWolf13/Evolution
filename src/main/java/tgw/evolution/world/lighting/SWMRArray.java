package tgw.evolution.world.lighting;

public abstract class SWMRArray {
    public abstract int getUpdating(int index);

    public abstract boolean isDirty();

    public abstract boolean isInitialisedUpdating();

    public abstract void set(int index, int value);

    public abstract boolean updateVisible();
}
