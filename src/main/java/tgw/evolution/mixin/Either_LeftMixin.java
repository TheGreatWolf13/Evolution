package tgw.evolution.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IEitherPatch;

import java.util.NoSuchElementException;

@Mixin(targets = {"com.mojang.datafixers.util.Either$Left"})
public abstract class Either_LeftMixin<L, R> implements IEitherPatch<L, R> {

    @Shadow
    @Final
    private L value;

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public L left() {
        return this.value;
    }

    @Override
    public R right() {
        throw new NoSuchElementException("This either is left!");
    }
}
