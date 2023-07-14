package tgw.evolution.mixin;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

import java.util.Collection;
import java.util.List;

@Mixin(ModelManager.class)
public abstract class MixinModelManager implements IKeyedReloadListener {

    private static final List<ResourceLocation> DEPENDENCY = List.of(ReloadListernerKeys.TEXTURES);

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.MODELS;
    }
}
