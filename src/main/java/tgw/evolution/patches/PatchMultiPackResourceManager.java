package tgw.evolution.patches;

import net.minecraft.server.packs.PackType;

public interface PatchMultiPackResourceManager {

    default PackType getResourceType() {
        throw new AbstractMethodError();
    }
}
