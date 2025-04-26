package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchHolderReference;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Collection;
import java.util.Set;

@Mixin(Holder.Reference.class)
public abstract class MixinHolder_Reference<T> implements PatchHolderReference<T> {

    @Shadow private Set<TagKey<T>> tags;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void bindTags(Collection<TagKey<T>> collection) {
        this.tags = OSet.of(collection);
    }

    @Override
    public Set<TagKey<T>> getTags() {
        return this.tags;
    }
}
