package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.IEitherPatch;

@Mixin(Either.class)
public abstract class EitherMixin<L, R> implements IEitherPatch<L, R> {
}
