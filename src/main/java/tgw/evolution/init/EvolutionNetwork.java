package tgw.evolution.init;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import tgw.evolution.entities.IEntitySpawnData;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.entities.projectiles.EntityGenericProjectile;
import tgw.evolution.entities.projectiles.EntitySpear;
import tgw.evolution.network.*;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2IHashMap;
import tgw.evolution.util.collection.maps.R2IMap;

import java.util.function.Function;

public final class EvolutionNetwork {

    private static final R2IMap<Class> ENTITY_DATA_IDS = new R2IHashMap<>();
    private static final OList<Function<FriendlyByteBuf, ? extends IEntitySpawnData.EntityData>> ENTITY_DATA_FACTORY = new OArrayList<>();
    private static int entityDataId;

    private EvolutionNetwork() {
    }

    public static int getId(IEntitySpawnData.EntityData data) {
        int id = ENTITY_DATA_IDS.getOrDefault(data.getClass(), -1);
        if (id == -1) {
            throw new UnregisteredFeatureException("EntityData " + data.getClass().getSimpleName() + " is not registered!");
        }
        return id;
    }

    public static IEntitySpawnData.EntityData readData(int id, FriendlyByteBuf buf) {
        Function<FriendlyByteBuf, ? extends IEntitySpawnData.EntityData> maker = ENTITY_DATA_FACTORY.get(id);
        if (maker == null) {
            throw new UnregisteredFeatureException("No maker registered for EntityData with id: " + id);
        }
        return maker.apply(buf);
    }

    public static ConnectionProtocol.PacketSet<ServerGamePacketListener> registerC2S(ConnectionProtocol.PacketSet<ServerGamePacketListener> set) {
        return set.addPacket(PacketCSSimpleMessage.class, PacketCSSimpleMessage::new)
                  .addPacket(PacketCSSetKnappingType.class, PacketCSSetKnappingType::new)
                  .addPacket(PacketCSSetCrawling.class, PacketCSSetCrawling::new)
                  .addPacket(PacketCSUpdatePuzzle.class, PacketCSUpdatePuzzle::new)
                  .addPacket(PacketCSSetMoldingType.class, PacketCSSetMoldingType::new)
                  .addPacket(PacketCSUpdateSchematicBlock.class, PacketCSUpdateSchematicBlock::new)
                  .addPacket(PacketCSPlayerFall.class, PacketCSPlayerFall::new)
//                  .addPacket(PacketCSChangeBlock.class, PacketCSChangeBlock::new)
                  .addPacket(PacketCSImpactDamage.class, PacketCSImpactDamage::new)
                  .addPacket(PacketCSUpdateBeltBackItem.class, PacketCSUpdateBeltBackItem::new)
                  .addPacket(PacketCSPlaySoundEntityEmitted.class, PacketCSPlaySoundEntityEmitted::new)
                  .addPacket(PacketCSSkinType.class, PacketCSSkinType::new)
//                  .addPacket(PacketCSSyncServerConfig.class, PacketCSSyncServerConfig::new)
                  .addPacket(PacketCSCollision.class, PacketCSCollision::new)
                  .addPacket(PacketCSSpecialHit.class, PacketCSSpecialHit::new)
                  .addPacket(PacketCSSpecialAttackStart.class, PacketCSSpecialAttackStart::new)
                  .addPacket(PacketCSSpecialAttackStop.class, PacketCSSpecialAttackStop::new)
                  .addPacket(PacketCSPlayerAction.class, PacketCSPlayerAction::new)
                  .addPacket(PacketCSEntityInteraction.class, PacketCSEntityInteraction::new);
    }

    public static void registerEntitySpawnData() {
        registerSpawnData(EntityGenericProjectile.ProjectileData.class, EntityGenericProjectile.ProjectileData::new);
        registerSpawnData(EntitySpear.SpearData.class, EntitySpear.SpearData::new);
        registerSpawnData(EntityFallingWeight.FallingWeightData.class, EntityFallingWeight.FallingWeightData::new);
        registerSpawnData(EntityPlayerCorpse.PlayerCorpseData.class, EntityPlayerCorpse.PlayerCorpseData::new);
    }

    public static ConnectionProtocol.PacketSet registerS2C(ConnectionProtocol.PacketSet<ClientGamePacketListener> set) {
        set.addPacket(PacketSCOpenKnappingGui.class, PacketSCOpenKnappingGui::new)
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
        //Apparently the compiler can't handle this much method chaining as it can't identify the type of the generic arguments
        return set.addPacket(PacketSCBlockBreakAck.class, PacketSCBlockBreakAck::new)
                  .addPacket(PacketSCBlockUpdate.class, PacketSCBlockUpdate::new)
                  .addPacket(PacketSCLevelEvent.class, PacketSCLevelEvent::new)
                  .addPacket(PacketSCSectionBlocksUpdate.class, PacketSCSectionBlocksUpdate::new);
    }

    private static <T extends IEntitySpawnData.EntityData> void registerSpawnData(Class<T> clazz, Function<FriendlyByteBuf, T> maker) {
        ENTITY_DATA_IDS.put(clazz, entityDataId++);
        ENTITY_DATA_FACTORY.add(maker);
    }
}
