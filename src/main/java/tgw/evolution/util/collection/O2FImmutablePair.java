package tgw.evolution.util.collection;

public class O2FImmutablePair<L> extends O2FPair<L> {

    private final L left;
    private final float right;

    O2FImmutablePair(L left, float right) {
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
        throw new UnsupportedOperationException("ImmutablePair cannot modify its fields!");
    }

    @Override
    public float setValue(float value) {
        throw new UnsupportedOperationException("ImmutablePair cannot modify its fields!");
    }
}
