package tgw.evolution.patches;

import net.minecraft.world.phys.Vec3;

public interface IClientboundLoginPacketPatch {

    long getDaytime();

    Vec3 getMotion();

    void setDaytime(long daytime);

    void setMotion(Vec3 motion);
}
