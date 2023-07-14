package tgw.evolution.mixin;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchEither;

@Mixin(Either.class)
public abstract class MixinEither<L, R> implements PatchEither<L, R> {

}
