package tgw.evolution.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchEither;

import java.util.NoSuchElementException;

@Mixin(targets = "com.mojang.datafixers.util.Either$Left")
public abstract class MixinEither_Left<L, R> implements PatchEither<L, R> {

    @Shadow(remap = false) @Final private L value;

    @Override
    public L getLeft() {
        return this.value;
    }

    @Override
    public R getRight() {
        throw new NoSuchElementException();
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public @Nullable L leftOrNull() {
        return this.value;
    }

    @Override
    public @Nullable R rightOrNull() {
        return null;
    }
}
