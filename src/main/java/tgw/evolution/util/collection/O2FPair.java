package tgw.evolution.util.collection;

import org.apache.commons.lang3.tuple.Pair;

public abstract class O2FPair<L> extends Pair<L, Float> {

    public static <L> O2FPair<L> mutableOf(L left, float right) {
        return new O2FMutablePair<>(left, right);
    }

    public static <L> O2FPair<L> of(L left, float right) {
        return new O2FImmutablePair<>(left, right);
    }

    @Override
    @Deprecated(forRemoval = true)
    public Float getRight() {
        return this.getRightAsFloat();
    }

    public abstract float getRightAsFloat();

    @Override
    @Deprecated(forRemoval = true)
    public Float getValue() {
        return this.getRightAsFloat();
    }

    public abstract L setLeft(L left);

    @Override
    @Deprecated(forRemoval = true)
    public Float setValue(Float value) {
        return this.setValue(value.floatValue());
    }

    public abstract float setValue(float value);
}
