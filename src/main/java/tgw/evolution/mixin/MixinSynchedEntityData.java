package tgw.evolution.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.patches.PatchSynchedEntityData;

@Mixin(SynchedEntityData.class)
public abstract class MixinSynchedEntityData implements PatchSynchedEntityData {

    @Shadow @Final private Entity entity;
    @Shadow private boolean isDirty;

    @Override
    @Unique
    public <T> void forceDirty(EntityDataAccessor<T> key) {
        SynchedEntityData.DataItem<T> dataItem = this.getItem(key);
        this.entity.onSyncedDataUpdated(key);
        dataItem.setDirty(true);
        this.isDirty = true;
    }

    @Shadow
    protected abstract <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> pKey);
}
