package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

@Mixin(TagManager.class)
public abstract class MixinTagManager implements IKeyedReloadListener {

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.TAGS;
    }
}
