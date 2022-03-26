package tgw.evolution.util;

import org.apache.commons.lang3.tuple.Pair;

public class Object2FloatPair<L> extends Pair<L, Float> {

    private final L left;
    private final float right;

    public Object2FloatPair(L left, float right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public L getLeft() {
        return this.left;
    }

    @Override
    @Deprecated(forRemoval = true)
    public Float getRight() {
        return this.right;
    }

    public float getRightAsFloat() {
        return this.right;
    }

    @Override
    public Float setValue(Float value) {
        throw new UnsupportedOperationException();
    }
}
