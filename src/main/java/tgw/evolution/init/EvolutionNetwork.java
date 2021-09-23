package tgw.evolution.init;

import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tgw.evolution.Evolution;
import tgw.evolution.network.*;

public final class EvolutionNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(Evolution.getResource("main"),
                                                                                  () -> PROTOCOL_VERSION,
                                                                                  PROTOCOL_VERSION::equals,
                                                                                  PROTOCOL_VERSION::equals);

    private static int id;

    private EvolutionNetwork() {
    }

    private static int increaseId() {
        id++;
        return id;
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCUpdateChunkStorage.class,
                                 PacketSCUpdateChunkStorage::encode,
                                 PacketSCUpdateChunkStorage::decode,
                                 PacketSCUpdateChunkStorage::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCHandAnimation.class,
                                 PacketSCHandAnimation::encode,
                                 PacketSCHandAnimation::decode,
                                 PacketSCHandAnimation::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSOpenExtendedInventory.class,
                                 PacketCSOpenExtendedInventory::encode,
                                 PacketCSOpenExtendedInventory::decode,
                                 PacketCSOpenExtendedInventory::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSPlayerAttack.class,
                                 PacketCSPlayerAttack::encode,
                                 PacketCSPlayerAttack::decode,
                                 PacketCSPlayerAttack::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSSetKnappingType.class,
                                 PacketCSSetKnappingType::encode,
                                 PacketCSSetKnappingType::decode,
                                 PacketCSSetKnappingType::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCOpenKnappingGui.class,
                                 PacketSCOpenKnappingGui::encode,
                                 PacketSCOpenKnappingGui::decode,
                                 PacketSCOpenKnappingGui::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSSetProne.class, PacketCSSetProne::encode, PacketCSSetProne::decode, PacketCSSetProne::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSUpdatePuzzle.class,
                                 PacketCSUpdatePuzzle::encode,
                                 PacketCSUpdatePuzzle::decode,
                                 PacketCSUpdatePuzzle::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCOpenMoldingGui.class,
                                 PacketSCOpenMoldingGui::encode,
                                 PacketSCOpenMoldingGui::decode,
                                 PacketSCOpenMoldingGui::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSSetMoldingType.class,
                                 PacketCSSetMoldingType::encode,
                                 PacketCSSetMoldingType::decode,
                                 PacketCSSetMoldingType::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSUpdateSchematicBlock.class,
                                 PacketCSUpdateSchematicBlock::encode,
                                 PacketCSUpdateSchematicBlock::decode,
                                 PacketCSUpdateSchematicBlock::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSPlayerFall.class,
                                 PacketCSPlayerFall::encode,
                                 PacketCSPlayerFall::decode,
                                 PacketCSPlayerFall::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSChangeBlock.class,
                                 PacketCSChangeBlock::encode,
                                 PacketCSChangeBlock::decode,
                                 PacketCSChangeBlock::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSImpactDamage.class,
                                 PacketCSImpactDamage::encode,
                                 PacketCSImpactDamage::decode,
                                 PacketCSImpactDamage::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCUpdateCameraTilt.class,
                                 PacketSCUpdateCameraTilt::encode,
                                 PacketSCUpdateCameraTilt::decode,
                                 PacketSCUpdateCameraTilt::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCChangeTickrate.class,
                                 PacketSCChangeTickrate::encode,
                                 PacketSCChangeTickrate::decode,
                                 PacketSCChangeTickrate::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCRemoveEffect.class,
                                 PacketSCRemoveEffect::encode,
                                 PacketSCRemoveEffect::decode,
                                 PacketSCRemoveEffect::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSLunge.class, PacketCSLunge::encode, PacketCSLunge::decode, PacketCSLunge::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSStartLunge.class,
                                 PacketCSStartLunge::encode,
                                 PacketCSStartLunge::decode,
                                 PacketCSStartLunge::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCStartLunge.class,
                                 PacketSCStartLunge::encode,
                                 PacketSCStartLunge::decode,
                                 PacketSCStartLunge::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSLungeAnim.class,
                                 PacketCSLungeAnim::encode,
                                 PacketCSLungeAnim::decode,
                                 PacketCSLungeAnim::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCLungeAnim.class,
                                 PacketSCLungeAnim::encode,
                                 PacketSCLungeAnim::decode,
                                 PacketSCLungeAnim::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCParrySound.class,
                                 PacketSCParrySound::encode,
                                 PacketSCParrySound::decode,
                                 PacketSCParrySound::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSUpdateBeltBackItem.class,
                                 PacketCSUpdateBeltBackItem::encode,
                                 PacketCSUpdateBeltBackItem::decode,
                                 PacketCSUpdateBeltBackItem::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCUpdateBeltBackItem.class,
                                 PacketSCUpdateBeltBackItem::encode,
                                 PacketSCUpdateBeltBackItem::decode,
                                 PacketSCUpdateBeltBackItem::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketCSPlaySoundEntityEmitted.class,
                                 PacketCSPlaySoundEntityEmitted::encode,
                                 PacketCSPlaySoundEntityEmitted::decode,
                                 PacketCSPlaySoundEntityEmitted::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCPlaySoundEntityEmitted.class,
                                 PacketSCPlaySoundEntityEmitted::encode,
                                 PacketSCPlaySoundEntityEmitted::decode,
                                 PacketSCPlaySoundEntityEmitted::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCThirstData.class,
                                 PacketSCThirstData::encode,
                                 PacketSCThirstData::decode,
                                 PacketSCThirstData::handle);
        INSTANCE.registerMessage(increaseId(), PacketCSSkinType.class, PacketCSSkinType::encode, PacketCSSkinType::decode, PacketCSSkinType::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCStatistics.class,
                                 PacketSCStatistics::encode,
                                 PacketSCStatistics::decode,
                                 PacketSCStatistics::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCAddEffect.class,
                                 PacketSCAddEffect::encode,
                                 PacketSCAddEffect::decode,
                                 PacketSCAddEffect::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCHitmarker.class,
                                 PacketSCHitmarker::encode,
                                 PacketSCHitmarker::decode,
                                 PacketSCHitmarker::handle);
        INSTANCE.registerMessage(increaseId(), PacketSCMovement.class, PacketSCMovement::encode, PacketSCMovement::decode, PacketSCMovement::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCMultiplayerPause.class,
                                 PacketSCMultiplayerPause::encode,
                                 PacketSCMultiplayerPause::decode,
                                 PacketSCMultiplayerPause::handle);
        INSTANCE.registerMessage(increaseId(),
                                 PacketSCFixRotation.class,
                                 PacketSCFixRotation::encode,
                                 PacketSCFixRotation::decode,
                                 PacketSCFixRotation::handle);
    }
}
