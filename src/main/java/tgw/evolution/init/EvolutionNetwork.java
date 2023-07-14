package tgw.evolution.init;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.network.*;

public final class EvolutionNetwork {

    private EvolutionNetwork() {
    }

    public static ConnectionProtocol.PacketSet<ServerGamePacketListener> registerC2S(ConnectionProtocol.PacketSet<ServerGamePacketListener> set) {
        return set.addPacket(PacketCSSimpleMessage.class, PacketCSSimpleMessage::new)
                  .addPacket(PacketCSSetKnappingType.class, PacketCSSetKnappingType::new)
                  .addPacket(PacketCSSetCrawling.class, PacketCSSetCrawling::new)
                  .addPacket(PacketCSUpdatePuzzle.class, PacketCSUpdatePuzzle::new)
                  .addPacket(PacketCSSetMoldingType.class, PacketCSSetMoldingType::new)
                  .addPacket(PacketCSUpdateSchematicBlock.class, PacketCSUpdateSchematicBlock::new)
                  .addPacket(PacketCSPlayerFall.class, PacketCSPlayerFall::new)
                  .addPacket(PacketCSChangeBlock.class, PacketCSChangeBlock::new)
                  .addPacket(PacketCSImpactDamage.class, PacketCSImpactDamage::new)
                  .addPacket(PacketCSUpdateBeltBackItem.class, PacketCSUpdateBeltBackItem::new)
                  .addPacket(PacketCSPlaySoundEntityEmitted.class, PacketCSPlaySoundEntityEmitted::new)
                  .addPacket(PacketCSSkinType.class, PacketCSSkinType::new)
//                  .addPacket(PacketCSSyncServerConfig.class, PacketCSSyncServerConfig::new)
                  .addPacket(PacketCSCollision.class, PacketCSCollision::new)
                  .addPacket(PacketCSSpecialHit.class, PacketCSSpecialHit::new)
                  .addPacket(PacketCSSpecialAttackStart.class, PacketCSSpecialAttackStart::new)
                  .addPacket(PacketCSSpecialAttackStop.class, PacketCSSpecialAttackStop::new);
    }

    public static ConnectionProtocol.PacketSet registerS2C(ConnectionProtocol.PacketSet<ClientGamePacketListener> set) {
        return set.addPacket(PacketSCOpenKnappingGui.class, PacketSCOpenKnappingGui::new)
                  .addPacket(PacketSCOpenMoldingGui.class, PacketSCOpenMoldingGui::new)
                  .addPacket(PacketSCUpdateCameraTilt.class, PacketSCUpdateCameraTilt::new)
                  .addPacket(PacketSCChangeTickrate.class, PacketSCChangeTickrate::new)
                  .addPacket(PacketSCRemoveEffect.class, PacketSCRemoveEffect::new)
                  .addPacket(PacketSCUpdateBeltBackItem.class, PacketSCUpdateBeltBackItem::new)
                  .addPacket(PacketSCPlaySoundEntityEmitted.class, PacketSCPlaySoundEntityEmitted::new)
                  .addPacket(PacketSCThirstData.class, PacketSCThirstData::new)
                  .addPacket(PacketSCStatistics.class, PacketSCStatistics::new)
                  .addPacket(PacketSCAddEffect.class, PacketSCAddEffect::new)
                  .addPacket(PacketSCMovement.class, PacketSCMovement::new)
                  .addPacket(PacketSCFixRotation.class, PacketSCFixRotation::new)
                  .addPacket(PacketSCShader.class, PacketSCShader::new)
                  .addPacket(PacketSCToast.class, PacketSCToast::new)
                  .addPacket(PacketSCHungerData.class, PacketSCHungerData::new)
                  .addPacket(PacketSCTemperatureData.class, PacketSCTemperatureData::new)
//                  .addPacket(PacketSCSyncServerConfig.class, PacketSCSyncServerConfig::new)
                  .addPacket(PacketSCSimpleMessage.class, PacketSCSimpleMessage::new)
                  .addPacket(PacketSCUpdateCameraViewCenter.class, PacketSCUpdateCameraViewCenter::new)
                  .addPacket(PacketSCMomentum.class, PacketSCMomentum::new)
                  .addPacket(PacketSCSpecialAttackStart.class, PacketSCSpecialAttackStart::new)
                  .addPacket(PacketSCSpecialAttackStop.class, PacketSCSpecialAttackStop::new)
                  .addPacket(PacketSCBlockDestruction.class, PacketSCBlockDestruction::new)
                  .addPacket(PacketSCCustomEntity.class, PacketSCCustomEntity::new);
    }
}
