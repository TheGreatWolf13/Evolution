package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchCompoundTag;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

@Mixin(CompoundTag.class)
public abstract class Mixin_C_CompoundTag implements PatchCompoundTag {

    @Shadow @Final private Map<String, Tag> tags;

    @ModifyConstructor
    public Mixin_C_CompoundTag() {
        this(new O2OHashMap<>());
    }

    public Mixin_C_CompoundTag(Map<String, Tag> map) {

    }

    @Shadow
    private static void writeNamedTag(String string, Tag tag, DataOutput dataOutput) {
        throw new AbstractMethodError();
    }

    @Override
    public void clear() {
        this.tags.clear();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public CompoundTag copy() {
        O2OMap<String, Tag> map = new O2OHashMap<>(this.tags.size());
        O2OMap<String, Tag> tags = (O2OMap<String, Tag>) this.tags;
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            map.put(tags.getIterationKey(it), tags.getIterationValue(it).copy());
        }
        return new CompoundTag(map);
    }

    @Override
    public O2OMap<String, Tag> tags() {
        return ((O2OMap<String, Tag>) this.tags).view();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void write(DataOutput dataOutput) throws IOException {
        O2OMap<String, Tag> tags = (O2OMap<String, Tag>) this.tags;
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            writeNamedTag(tags.getIterationKey(it), tags.getIterationValue(it), dataOutput);
        }
        dataOutput.writeByte(Tag.TAG_END);
    }
}
