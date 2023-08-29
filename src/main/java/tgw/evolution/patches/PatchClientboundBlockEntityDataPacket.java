package tgw.evolution.patches;

public interface PatchClientboundBlockEntityDataPacket {

    default int getX() {
        throw new AbstractMethodError();
    }

    default int getY() {
        throw new AbstractMethodError();
    }

    default int getZ() {
        throw new AbstractMethodError();
    }
}
