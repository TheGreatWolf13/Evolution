package tgw.evolution.mixin;

import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IModResource;

@Mixin(targets = {"net.minecraft.server.packs.VanillaPackResources$1"})
public abstract class MixinVanillaPackResources_Resource implements IModResource {

    @Override
    public PackSource getModPackSource() {
        // The default resource pack only contains built-in vanilla resources.
        return PackSource.BUILT_IN;
    }
}
