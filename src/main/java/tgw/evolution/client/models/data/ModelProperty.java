package tgw.evolution.client.models.data;

import java.util.function.Predicate;

public class ModelProperty<T> {

    private final Predicate<T> pred;

    public ModelProperty() {
        this(t -> true);
    }

    public ModelProperty(Predicate<T> pred) {
        this.pred = pred;
    }

    public boolean test(T t) {
        return this.pred.test(t);
    }
}
