package tgw.evolution.mixin;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(MobEffectTextureManager.class)
public abstract class MixinMobEffectTextureManager extends TextureAtlasHolder {

    public MixinMobEffectTextureManager(TextureManager textureManager, ResourceLocation resourceLocation, String string) {
        super(textureManager, resourceLocation, string);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Stream<ResourceLocation> getResourcesToLoad() {
        Evolution.deprecatedMethod();
        return Stream.empty();
    }

    @Override
    public OSet<ResourceLocation> getResourcesToLoad_(Function<ResourceLocation, ResourceLocation> mapping) {
        OSet<ResourceLocation> set = new OHashSet<>();
        for (long it = Registry.MOB_EFFECT.beginIteration(); Registry.MOB_EFFECT.hasNextIteration(it); it = Registry.MOB_EFFECT.nextEntry(it)) {
            set.add(mapping.apply(Registry.MOB_EFFECT.getIterationLocation(it)));
        }
        return set.immutable();
    }
}
