package tgw.evolution.patches;

import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import tgw.evolution.resources.IModResource;

public interface PatchSimpleResource extends Resource, IModResource {

    default void setPackSource(PackSource packSource) {
        throw new AbstractMethodError();
    }
}
