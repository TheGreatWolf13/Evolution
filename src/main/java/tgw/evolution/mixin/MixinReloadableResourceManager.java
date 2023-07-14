package tgw.evolution.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.resources.GroupResourcePack;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ReloadableResourceManager.class)
public abstract class MixinReloadableResourceManager {

    @Inject(method = "method_29491", at = @At("HEAD"), cancellable = true)
    private static void getResourcePackNames(List<PackResources> packs, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(packs.stream().map(pack -> {
            if (pack instanceof GroupResourcePack g) {
                return g.getFullName();
            }
            return pack.getName();
        }).collect(Collectors.joining(", ")));
    }
}
