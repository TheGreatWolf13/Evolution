package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapLike;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.stream.Stream;

@Mixin(targets = "net.minecraft.nbt.NbtOps$1")
public abstract class MixinNbtOps_1 implements MapLike<Tag> {

    @Shadow(aliases = "this$0") @Final NbtOps field_25130;
    @Shadow @Final CompoundTag val$tag;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Stream<Pair<Tag, Tag>> entries() {
        O2OMap<String, Tag> tags = this.val$tag.tags();
        if (tags.isEmpty()) {
            return Stream.empty();
        }
        Stream.Builder<Pair<Tag, Tag>> builder = Stream.builder();
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            String key = tags.getIterationKey(it);
            //noinspection ObjectAllocationInLoop
            builder.add(Pair.of(this.field_25130.createString(key), tags.getIterationValue(it)));
        }
        return builder.build();
    }
}
