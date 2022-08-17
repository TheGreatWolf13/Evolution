package tgw.evolution.patches;

import net.minecraft.core.SectionPos;

public interface IServerPlayerPatch {

    boolean getCameraUnload();

    SectionPos getLastCameraSectionPos();

    void setCameraUnload(boolean shouldUnload);

    void setLastCameraSectionPos(SectionPos pos);
}
