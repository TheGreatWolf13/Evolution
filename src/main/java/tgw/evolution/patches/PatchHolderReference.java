package tgw.evolution.patches;

import net.minecraft.tags.TagKey;

import java.util.Set;

public interface PatchHolderReference<T> {

    Set<TagKey<T>> getTags();
}
