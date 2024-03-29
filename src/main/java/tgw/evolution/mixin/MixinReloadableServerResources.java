package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class MixinReloadableServerResources {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static <T> void updateRegistryTags(RegistryAccess registryAccess, TagManager.LoadResult<T> loadResult) {
        ResourceKey<? extends Registry<T>> resourceKey = loadResult.key();
        O2OMap<ResourceLocation, Tag<Holder<T>>> tags = (O2OMap<ResourceLocation, Tag<Holder<T>>>) loadResult.tags();
        O2OMap<TagKey<T>, List<Holder<T>>> map = new O2OHashMap<>();
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            ResourceLocation key = tags.getIterationKey(it);
            Tag<Holder<T>> value = tags.getIterationValue(it);
            map.put(TagKey.create(resourceKey, key), value.getValues());
        }
        map.trim();
        registryAccess.registryOrThrow(resourceKey).bindTags(map.view());
    }
}
