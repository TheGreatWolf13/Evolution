package tgw.evolution.mixin;

import net.minecraft.nbt.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.io.DataInput;
import java.io.IOException;

@Mixin(targets = "net.minecraft.nbt.ListTag$1")
public abstract class MixinListTag_1 implements TagType.VariableSize<ListTag> {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public ListTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
        nbtAccounter.accountBits(296L);
        if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        byte type = dataInput.readByte();
        int size = dataInput.readInt();
        if (type == 0 && size > 0) {
            throw new RuntimeException("Missing type on ListTag");
        }
        nbtAccounter.accountBits(32L * size);
        TagType<?> tagType = TagTypes.getType(type);
        OList<Tag> list = new OArrayList<>(size);
        for (int k = 0; k < size; ++k) {
            list.add(tagType.load(dataInput, i + 1, nbtAccounter));
        }
        return new ListTag(list, type);
    }
}
