package tgw.evolution.client.models.data;

import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class GenericModelData<P> implements IModelData {

    private final ModelProperty<P> property;
    private P data;

    public GenericModelData(ModelProperty<P> property) {
        this.property = property;
    }

    @Override
    public <T> @Nullable T getData(ModelProperty<T> prop) {
        if (prop == this.property) {
            return (T) this.data;
        }
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == this.property;
    }

    @Override
    public <T> void setData(ModelProperty<T> prop, T data) {
        if (this.property == prop) {
            this.data = (P) data;
        }
        else {
            throw new NoSuchElementException();
        }
    }
}
