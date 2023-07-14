package tgw.evolution.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.resources.ResourcePackSourceTracker;

@Mixin(Pack.class)
public abstract class MixinPack {

    @Shadow @Final private PackSource packSource;

    @Inject(method = "open", at = @At("RETURN"))
    private void onCreateResourcePack(CallbackInfoReturnable<PackResources> info) {
        ResourcePackSourceTracker.setSource(info.getReturnValue(), this.packSource);
    }
}
