package tgw.evolution.mixin;

import net.minecraft.client.Options;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.resources.ModPackResources;
import tgw.evolution.resources.ModdedPackSource;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(Options.class)
public abstract class MixinOptions {

    @Shadow public List<String> resourcePacks;

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        // Add built-in resource packs if they are enabled by default only if the options file is blank.
        if (this.resourcePacks.isEmpty()) {
            List<Pack> profiles = new ArrayList<>();
            ModdedPackSource.CLIENT_RESOURCE_PACK_PROVIDER.register(profiles::add);
            this.resourcePacks = new ArrayList<>();
            for (Pack profile : profiles) {
                PackResources pack = profile.open();
                if (profile.getPackSource() == ModdedPackSource.RESOURCE_PACK_SOURCE ||
                    pack instanceof ModPackResources p && p.getActivationType().isEnabledByDefault()) {
                    this.resourcePacks.add(profile.getId());
                }
            }
        }
    }

    @Inject(method = "processOptions", at = @At("TAIL"))
    private void onProcessOptions(Options.FieldAccess acc, CallbackInfo ci) {
        EvolutionConfig.processOptions(acc);
    }
}
