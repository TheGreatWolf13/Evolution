package tgw.evolution.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.util.collection.maps.R2OHashMap;

import java.util.Map;

@Mixin(ResourceKey.class)
public abstract class MixinResourceKey<T> {

    @Mutable @Shadow @Final private static Map<String, ResourceKey<?>> VALUES;

    static {
        //It was a synchronized map, so we now must synchronize on our on.
        VALUES = new R2OHashMap<>();
    }

    /**
     * @author TheGreatWolf
     * @reason Synchronize externally
     */
    @Overwrite
    private static <T> ResourceKey<T> create(ResourceLocation registry, ResourceLocation location) {
        String string = (registry + ":" + location).intern();
        synchronized (VALUES) {
            ResourceKey<T> r = (ResourceKey<T>) VALUES.get(string);
            if (r == null) {
                r = new ResourceKey<>(registry, location);
                VALUES.put(string, r);
            }
            return r;
        }
    }
}
