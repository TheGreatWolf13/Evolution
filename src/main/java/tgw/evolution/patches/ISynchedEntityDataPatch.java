package tgw.evolution.patches;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface ISynchedEntityDataPatch {

    <T> void forceDirty(EntityDataAccessor<T> key);
}
