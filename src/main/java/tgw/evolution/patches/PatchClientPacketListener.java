package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import tgw.evolution.entities.IEntityPacket;
import tgw.evolution.network.*;

public interface PatchClientPacketListener {

    void handleAddEffect(PacketSCAddEffect packet);

    void handleBlockDestruction(PacketSCBlockDestruction packet);

    void handleChangeTickrate(PacketSCChangeTickrate packet);

    <T extends Entity & IEntityPacket<T>> void handleCustomEntity(PacketSCCustomEntity<T> packet);

    void handleFixRotation(PacketSCFixRotation packet);

    void handleHungerData(PacketSCHungerData packet);

    void handleMomentum(PacketSCMomentum packet);

    void handleMovement(PacketSCMovement packet);

    void handleOpenKnappingGui(PacketSCOpenKnappingGui packet);

    void handleOpenMoldingGui(PacketSCOpenMoldingGui packet);

    void handlePlaySoundEntityEmitted(PacketSCPlaySoundEntityEmitted packet);

    void handleRemoveEffect(PacketSCRemoveEffect packet);

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
