package tgw.evolution.mixin;

import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IModResource;

@Mixin(Resource.class)
public interface MixinResource extends IModResource {
}
