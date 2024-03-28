package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Collections;

@Mixin(StringTagVisitor.class)
public abstract class MixinStringTagVisitor implements TagVisitor {

    @Shadow @Final private StringBuilder builder;

    @Shadow
    protected static String handleEscape(String string) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void visitCompound(CompoundTag compoundTag) {
        this.builder.append('{');
        O2OMap<String, Tag> tags = compoundTag.tags();
        if (tags.isEmpty()) {
            this.builder.append('}');
            return;
        }
        OList<String> list = new OArrayList<>(tags.size());
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            list.add(tags.getIterationKey(it));
        }
        Collections.sort(list);
        this.builder.append(handleEscape(list.get(0))).append(':').append(new StringTagVisitor().visit(compoundTag.get(list.get(0))));
        for (int i = 1, len = list.size(); i < len; ++i) {
            String name = list.get(i);
            this.builder.append(',');
            //noinspection ObjectAllocationInLoop
            this.builder.append(handleEscape(name)).append(':').append(new StringTagVisitor().visit(compoundTag.get(name)));
        }
        this.builder.append('}');
    }
}
