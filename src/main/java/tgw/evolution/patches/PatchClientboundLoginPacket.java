package tgw.evolution.patches;

import net.minecraft.world.phys.Vec3;

public interface PatchClientboundLoginPacket {

    default long getDaytime() {
        throw new AbstractMethodError();
    }

    default double getMotionX() {
        throw new AbstractMethodError();
    }

    default double getMotionY() {
        throw new AbstractMethodError();
    }

    default double getMotionZ() {
        throw new AbstractMethodError();
    }

    default <T extends PatchClientboundLoginPacket> T setDaytime(long daytime) {
        throw new AbstractMethodError();
    }

    default <T extends PatchClientboundLoginPacket> T setMotion(Vec3 motion) {
        throw new AbstractMethodError();
    }
}
