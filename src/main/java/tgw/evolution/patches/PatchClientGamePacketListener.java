package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import tgw.evolution.entities.IEntitySpawnData;
import tgw.evolution.network.*;

public interface PatchClientGamePacketListener {

    void handleAddEffect(PacketSCAddEffect packet);

    void handleBlockBreakAck(PacketSCBlockBreakAck packet);

    void handleBlockDestruction(PacketSCBlockDestruction packet);

    void handleBlockUpdate(PacketSCBlockUpdate packet);

    void handleChangeTickrate(PacketSCChangeTickrate packet);

    <T extends Entity & IEntitySpawnData> void handleCustomEntity(PacketSCCustomEntity<T> packet);

    void handleFixRotation(PacketSCFixRotation packet);

    void handleHungerData(PacketSCHungerData packet);

    default void handleLevelEvent(PacketSCLevelEvent packet) {
        throw new AbstractMethodError();
    }

    default void handleLoadFactor(PacketSCLoadFactor packet) {
        throw new AbstractMethodError();
    }

    void handleMomentum(PacketSCMomentum packet);

    void handleMovement(PacketSCMovement packet);

    void handleOpenKnappingGui(PacketSCOpenKnappingGui packet);

    void handleOpenMoldingGui(PacketSCOpenMoldingGui packet);

    void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet);

    void handleRemoveEffect(PacketSCRemoveEffect packet);

    void handleSectionBlocksUpdate(PacketSCSectionBlocksUpdate packet);

    void handleShader(PacketSCShader packet);

    void handleSimpleMessage(PacketSCSimpleMessage packet);

    void handleSpecialAttackStart(PacketSCSpecialAttackStart packet);

    void handleSpecialAttackStop(PacketSCSpecialAttackStop packet);

    void handleStatistics(PacketSCStatistics packet);

    void handleTemperatureData(PacketSCTemperatureData packet);

    void handleThirstData(PacketSCThirstData packet);

    void handleToast(PacketSCToast packet);

    void handleUpdateBeltBackItem(PacketSCUpdateBeltBackItem packet);

    void handleUpdateCameraTilt(PacketSCUpdateCameraTilt packet);

    void handleUpdateCameraViewCenter(PacketSCUpdateCameraViewCenter packet);
}
