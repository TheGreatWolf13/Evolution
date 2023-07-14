package tgw.evolution.mixin;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.resources.IModResourcePack;
import tgw.evolution.resources.ModResourcePackUtil;
import tgw.evolution.resources.ModdedPackSource;
import tgw.evolution.resources.ProgrammerArtResourcePack;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.io.File;
import java.util.function.Consumer;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ClientPackSource.class)
public abstract class MixinClientPackSource {

    @Unique
    private static OList<IModResourcePack> getProgrammerArtModResourcePacks() {
        OList<IModResourcePack> packs = new OArrayList<>();
        ModResourcePackUtil.appendModResourcePacks(packs, PackType.CLIENT_RESOURCES, "programmer_art");
        return packs;
    }

    // ClientBuiltinResourcePackProvider#method_25454 second lambda.
    @Inject(method = "method_25456", at = @At("RETURN"), cancellable = true)
    private static void onSupplyDirProgrammerArtPack(File file, CallbackInfoReturnable<PackResources> cir) {
        AbstractPackResources originalPack = (AbstractPackResources) cir.getReturnValue();
        cir.setReturnValue(new ProgrammerArtResourcePack(originalPack, getProgrammerArtModResourcePacks()));
    }

    // ClientBuiltinResourcePackProvider#method_25454 first lambda.
    @Inject(method = "method_25457", at = @At("RETURN"), cancellable = true)
    private static void onSupplyZipProgrammerArtPack(File file, CallbackInfoReturnable<PackResources> cir) {
        AbstractPackResources originalPack = (AbstractPackResources) cir.getReturnValue();
        cir.setReturnValue(new ProgrammerArtResourcePack(originalPack, getProgrammerArtModResourcePacks()));
    }

    @Inject(method = "loadPacks", at = @At("RETURN"))
    private void addBuiltinResourcePacks(Consumer<Pack> consumer, Pack.PackConstructor factory, CallbackInfo ci) {
        // Register mod and built-in resource packs after the vanilla built-in resource packs are registered.
        ModdedPackSource.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(consumer, factory);
    }
}
