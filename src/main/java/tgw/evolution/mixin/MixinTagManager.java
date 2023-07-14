package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

import java.util.Collection;
import java.util.List;

@Mixin(TagManager.class)
public abstract class MixinTagManager implements IKeyedReloadListener {

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.TAGS;
    }
}
