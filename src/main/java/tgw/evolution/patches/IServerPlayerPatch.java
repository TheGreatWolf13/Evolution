package tgw.evolution.patches;

import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Nullable;

public interface IServerPlayerPatch {

    boolean getCameraUnload();

    @Nullable SectionPos getLastCameraSectionPos();

    void setCameraUnload(boolean shouldUnload);

    void setLastCameraSectionPos(@Nullable SectionPos pos);
}
