package tgw.evolution.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchEither;

import java.util.NoSuchElementException;

@Mixin(targets = "com.mojang.datafixers.util.Either$Right")
public abstract class MixinEither_Right<L, R> implements PatchEither<L, R> {

    @Shadow(remap = false) @Final private R value;

    @Override
    public L getLeft() {
        throw new NoSuchElementException();
    }

    @Override
    public R getRight() {
        return this.value;
    }

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public @Nullable L leftOrNull() {
        return null;
    }

    @Override
    public @Nullable R rightOrNull() {
        return this.value;
    }
}
