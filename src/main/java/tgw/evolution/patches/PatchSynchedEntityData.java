package tgw.evolution.patches;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface PatchSynchedEntityData {

    <T> void forceDirty(EntityDataAccessor<T> key);
}
