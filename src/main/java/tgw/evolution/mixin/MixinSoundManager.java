package tgw.evolution.mixin;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

import java.util.Collection;
import java.util.List;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager implements IKeyedReloadListener {

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.SOUNDS;
    }
}
