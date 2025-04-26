package tgw.evolution.mixin;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Mixin(ListTag.class)
public abstract class Mixin_CF_ListTag extends CollectionTag<Tag> {

    @Shadow @Final private List<Tag> list;

    @Shadow private byte type;

    @ModifyConstructor
    public Mixin_CF_ListTag() {
        this(new OArrayList<>(), (byte) 0);
    }

    @DummyConstructor
    Mixin_CF_ListTag(List<Tag> list, byte b) {

    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Tag> c) {
        return this.list.addAll(c);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public ListTag copy() {
        List<Tag> list = this.list;
        if (TagTypes.getType(this.type).isValue()) {
            return new ListTag(new OArrayList<>(list), this.type);
        }
        OList<Tag> newList = new OArrayList<>(list.size());
        for (int i = 0, len = list.size(); i < len; ++i) {
            newList.add(list.get(i).copy());
        }
        return new ListTag(newList, this.type);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void write(DataOutput dataOutput) throws IOException {
        List<Tag> list = this.list;
        this.type = list.isEmpty() ? (byte) 0 : list.get(0).getId();
        dataOutput.writeByte(this.type);
        dataOutput.writeInt(list.size());
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).write(dataOutput);
        }
    }
}
