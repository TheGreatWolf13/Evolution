package tgw.evolution.util.collection;

public class O2FMutablePair<L> extends O2FPair<L> {

    private L left;
    private float right;

    O2FMutablePair(L left, float right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return this.left;
    }

    @Override
    public float getRightAsFloat() {
        return this.right;
    }

    @Override
    public L setLeft(L left) {
        L prev = this.left;
        this.left = left;
        return prev;
    }

    @Override
    public float setValue(float value) {
        float prev = this.right;
        this.right = value;
        return prev;
    }
}
