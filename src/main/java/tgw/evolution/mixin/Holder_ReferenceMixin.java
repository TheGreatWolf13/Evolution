package tgw.evolution.mixin;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IHolderReferencePatch;

import java.util.Set;

@Mixin(Holder.Reference.class)
public abstract class Holder_ReferenceMixin<T> implements IHolderReferencePatch<T> {

    @Shadow
    private Set<TagKey<T>> tags;

    @Override
    public Set<TagKey<T>> getTags() {
        return this.tags;
    }
}
