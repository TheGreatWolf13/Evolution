package tgw.evolution.patches;

public interface PatchDeathScreen {

    default <T extends PatchDeathScreen> T setTimeAlive(long timeAlive) {
        throw new AbstractMethodError();
    }
}
