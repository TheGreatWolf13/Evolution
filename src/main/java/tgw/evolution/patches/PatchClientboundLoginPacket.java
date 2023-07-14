package tgw.evolution.patches;

import net.minecraft.world.phys.Vec3;

public interface PatchClientboundLoginPacket {

    default long getDaytime() {
        throw new AbstractMethodError();
    }

    default Vec3 getMotion() {
        throw new AbstractMethodError();
    }

    default void setDaytime(long daytime) {
        throw new AbstractMethodError();
    }

    default void setMotion(Vec3 motion) {
        throw new AbstractMethodError();
    }
}
