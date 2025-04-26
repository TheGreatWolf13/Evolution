package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Map;

@Mixin(RegistryCodecs.class)
public abstract class MixinRegistryCodecs {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static <E> Codec<Registry<E>> dataPackAwareCodec(ResourceKey<? extends Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
        Codec<Map<ResourceKey<E>, E>> decoder = directCodec(resourceKey, codec);
        Encoder<Registry<E>> encoder = decoder.comap(registry -> {
            O2OMap<ResourceKey<E>, E> map = new O2OHashMap<>();
            for (long it = registry.beginIteration(); registry.hasNextIteration(it); it = registry.nextEntry(it)) {
                map.put(registry.getIterationKey(it), (E) registry.getIteration(it));
            }
            return map.immutable();
        });
        return Codec.of(encoder, dataPackAwareDecoder(resourceKey, codec, decoder, lifecycle), "DataPackRegistryCodec for " + resourceKey);
    }

    @SuppressWarnings("Contract")
    @Contract(value = "_, _, _, _ -> _")
    @Shadow
    private static <E> Decoder<Registry<E>> dataPackAwareDecoder(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, Decoder<Map<ResourceKey<E>, E>> decoder, Lifecycle lifecycle) {
        throw new AbstractMethodError();
    }

    @SuppressWarnings("Contract")
    @Contract(value = "_, _ -> _")
    @Shadow
    private static <T> Codec<Map<ResourceKey<T>, T>> directCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec) {
        throw new AbstractMethodError();
    }
}
