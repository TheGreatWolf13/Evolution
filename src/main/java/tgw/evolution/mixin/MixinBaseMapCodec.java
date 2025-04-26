package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Map;

@Mixin(BaseMapCodec.class)
public interface MixinBaseMapCodec<K, V> {

    @Shadow(remap = false)
    Codec<V> elementCodec();

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite(remap = false)
    default <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        if (input instanceof O2OMap<K, V> map) {
            for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
                prefix.add(this.keyCodec().encodeStart(ops, map.getIterationKey(it)), this.elementCodec().encodeStart(ops, map.getIterationValue(it)));
            }
        }
        else {
            for (final Map.Entry<K, V> entry : input.entrySet()) {
                prefix.add(this.keyCodec().encodeStart(ops, entry.getKey()), this.elementCodec().encodeStart(ops, entry.getValue()));
            }
        }
        return prefix;
    }

    @Shadow(remap = false)
    Codec<K> keyCodec();
}
