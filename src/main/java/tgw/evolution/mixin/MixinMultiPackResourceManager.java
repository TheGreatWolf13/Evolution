package tgw.evolution.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.patches.PatchMultiPackResourceManager;

import java.util.List;

@Mixin(MultiPackResourceManager.class)
public abstract class MixinMultiPackResourceManager implements PatchMultiPackResourceManager {

    @Unique private PackType type;

    @Override
    public PackType getResourceType() {
        return this.type;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(PackType resourceType, List<PackResources> list, CallbackInfo ci) {
        this.type = resourceType;
    }
}
