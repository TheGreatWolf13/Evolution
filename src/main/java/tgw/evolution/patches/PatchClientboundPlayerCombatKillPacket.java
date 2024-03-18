package tgw.evolution.patches;

public interface PatchClientboundPlayerCombatKillPacket {

    default long getTimeAlive() {
        throw new AbstractMethodError();
    }

    default <T extends PatchClientboundPlayerCombatKillPacket> T setTimeAlive(long timeAlive) {
        throw new AbstractMethodError();
    }
}
