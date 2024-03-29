package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import org.spongepowered.asm.mixin.Mixin;

import java.util.NoSuchElementException;

@Mixin(targets = "net.minecraft.core.Registry$1")
public abstract class MixinRegistry_1<T> implements IdMap<Holder<T>> {

    @Override
    public long beginIteration() {
        return 0;
    }

    @Override
    public Holder<T> getIteration(long it) {
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return false;
    }

    @Override
    public long nextEntry(long it) {
        return 0;
    }
}
