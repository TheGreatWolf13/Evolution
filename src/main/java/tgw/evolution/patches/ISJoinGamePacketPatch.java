package tgw.evolution.patches;

import net.minecraft.util.math.vector.Vector3d;

public interface ISJoinGamePacketPatch {

    long getDaytime();

    Vector3d getMotion();

    void setDaytime(long daytime);

    void setMotion(Vector3d motion);
}
