package tgw.evolution.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IEitherPatch;

import java.util.NoSuchElementException;

@Mixin(targets = {"com.mojang.datafixers.util.Either$Right"})
public abstract class Either_RightMixin<L, R> implements IEitherPatch<L, R> {

    @Shadow
    @Final
    private R value;

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public L left() {
        throw new NoSuchElementException("This either is right!");
    }

    @Override
    public R right() {
        return this.value;
    }
}
