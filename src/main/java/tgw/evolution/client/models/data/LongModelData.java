package tgw.evolution.client.models.data;

import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;

import java.util.NoSuchElementException;

public class LongModelData implements IModelData {

    private final ModelProperty<Long> property;
    private long value;

    public LongModelData(ModelProperty<Long> property) {
        this.property = property;
    }

    @Override
    public <T> @Nullable T getData(ModelProperty<T> prop) {
        Evolution.deprecatedMethod();
        if (this.property == prop) {
            return (T) Long.valueOf(this.value);
        }
        throw new NoSuchElementException();
    }

    @Override
    public long getLongData(ModelProperty<Long> prop) {
        if (this.property == prop) {
            return this.value;
        }
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return this.property == prop;
    }

    @Override
    public <T> void setData(ModelProperty<T> prop, T data) {
        Evolution.deprecatedMethod();
        if (this.property == prop) {
            this.value = (long) data;
        }
        else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void setData(ModelProperty<Long> prop, long data) {
        if (this.property == prop) {
            this.value = data;
        }
        else {
            throw new NoSuchElementException();
        }
    }
}
