package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;
import tgw.evolution.util.collection.sets.OSet;

import java.util.function.Function;

public interface PatchTextureAtlasHolder {

    default OSet<ResourceLocation> getResourcesToLoad_(Function<ResourceLocation, ResourceLocation> mapping) {
        return OSet.emptySet();
    }
}
