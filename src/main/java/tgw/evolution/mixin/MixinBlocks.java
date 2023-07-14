package tgw.evolution.mixin;

import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.init.EvolutionBlocks;

@Mixin(Blocks.class)
public abstract class MixinBlocks {

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private static void onClinit(CallbackInfo ci) {
        EvolutionBlocks.register();
    }
}
