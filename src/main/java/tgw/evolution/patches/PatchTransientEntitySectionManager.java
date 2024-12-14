package tgw.evolution.patches;

public interface PatchTransientEntitySectionManager {

    default void startTicking(int chunkX, int chunkZ) {
        throw new AbstractMethodError();
    }
}
