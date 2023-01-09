package tgw.evolution.init;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.network.*;
import tgw.evolution.util.collection.I2OMap;
import tgw.evolution.util.collection.I2OOpenHashMap;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class EvolutionNetwork {

    private static final SimpleChannel INSTANCE;
    private static final String PROTOCOL_VERSION = "1";
    private static final I2OMap<PacketDistributor.PacketTarget> PACKET_TARGET_CACHE = new I2OOpenHashMap<>();
    private static @Nullable PacketDistributor.PacketTarget all;
    private static int id;

    static {
        INSTANCE = NetworkRegistry.newSimpleChannel(Evolution.getResource("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
                                                    PROTOCOL_VERSION::equals);
    }

    private EvolutionNetwork() {
    }

    private static int increaseId() {
        id++;
        return id;
    }

    public static void playerLogOut(Player player) {
        PACKET_TARGET_CACHE.remove(player.getId());
    }

    private static <T> void register(Class<T> clazz,
                                     BiConsumer<T, FriendlyByteBuf> encode,
                                     Function<FriendlyByteBuf, T> decode,
                                     BiConsumer<T, Supplier<NetworkEvent.Context>> handle) {
        INSTANCE.registerMessage(increaseId(), clazz, encode, decode, handle);
    }

    public static void registerMessages() {
        register(PacketCSOpenExtendedInventory.class, PacketCSOpenExtendedInventory::encode, PacketCSOpenExtendedInventory::decode,
                 PacketCSOpenExtendedInventory::handle);
        register(PacketCSSetKnappingType.class, PacketCSSetKnappingType::encode, PacketCSSetKnappingType::decode, PacketCSSetKnappingType::handle);
        register(PacketSCOpenKnappingGui.class, PacketSCOpenKnappingGui::encode, PacketSCOpenKnappingGui::decode, PacketSCOpenKnappingGui::handle);
        register(PacketCSSetCrawling.class, PacketCSSetCrawling::encode, PacketCSSetCrawling::decode, PacketCSSetCrawling::handle);
        register(PacketCSUpdatePuzzle.class, PacketCSUpdatePuzzle::encode, PacketCSUpdatePuzzle::decode, PacketCSUpdatePuzzle::handle);
        register(PacketSCOpenMoldingGui.class, PacketSCOpenMoldingGui::encode, PacketSCOpenMoldingGui::decode, PacketSCOpenMoldingGui::handle);
        register(PacketCSSetMoldingType.class, PacketCSSetMoldingType::encode, PacketCSSetMoldingType::decode, PacketCSSetMoldingType::handle);
        register(PacketCSUpdateSchematicBlock.class, PacketCSUpdateSchematicBlock::encode, PacketCSUpdateSchematicBlock::decode,
                 PacketCSUpdateSchematicBlock::handle);
        register(PacketCSPlayerFall.class, PacketCSPlayerFall::encode, PacketCSPlayerFall::decode, PacketCSPlayerFall::handle);
        register(PacketCSChangeBlock.class, PacketCSChangeBlock::encode, PacketCSChangeBlock::decode, PacketCSChangeBlock::handle);
        register(PacketCSImpactDamage.class, PacketCSImpactDamage::encode, PacketCSImpactDamage::decode, PacketCSImpactDamage::handle);
        register(PacketSCUpdateCameraTilt.class, PacketSCUpdateCameraTilt::encode, PacketSCUpdateCameraTilt::decode,
                 PacketSCUpdateCameraTilt::handle);
        register(PacketSCChangeTickrate.class, PacketSCChangeTickrate::encode, PacketSCChangeTickrate::decode, PacketSCChangeTickrate::handle);
        register(PacketSCRemoveEffect.class, PacketSCRemoveEffect::encode, PacketSCRemoveEffect::decode, PacketSCRemoveEffect::handle);
        register(PacketSCParrySound.class, PacketSCParrySound::encode, PacketSCParrySound::decode, PacketSCParrySound::handle);
        register(PacketCSUpdateBeltBackItem.class, PacketCSUpdateBeltBackItem::encode, PacketCSUpdateBeltBackItem::decode,
                 PacketCSUpdateBeltBackItem::handle);
        register(PacketSCUpdateBeltBackItem.class, PacketSCUpdateBeltBackItem::encode, PacketSCUpdateBeltBackItem::decode,
                 PacketSCUpdateBeltBackItem::handle);
        register(PacketCSPlaySoundEntityEmitted.class, PacketCSPlaySoundEntityEmitted::encode, PacketCSPlaySoundEntityEmitted::decode,
                 PacketCSPlaySoundEntityEmitted::handle);
        register(PacketSCPlaySoundEntityEmitted.class, PacketSCPlaySoundEntityEmitted::encode, PacketSCPlaySoundEntityEmitted::decode,
                 PacketSCPlaySoundEntityEmitted::handle);
        register(PacketSCThirstData.class, PacketSCThirstData::encode, PacketSCThirstData::decode, PacketSCThirstData::handle);
        register(PacketCSSkinType.class, PacketCSSkinType::encode, PacketCSSkinType::decode, PacketCSSkinType::handle);
        register(PacketSCStatistics.class, PacketSCStatistics::encode, PacketSCStatistics::decode, PacketSCStatistics::handle);
        register(PacketSCAddEffect.class, PacketSCAddEffect::encode, PacketSCAddEffect::decode, PacketSCAddEffect::handle);
        register(PacketSCHitmarker.class, PacketSCHitmarker::encode, PacketSCHitmarker::decode, PacketSCHitmarker::handle);
        register(PacketSCMovement.class, PacketSCMovement::encode, PacketSCMovement::decode, PacketSCMovement::handle);
        register(PacketSCMultiplayerPause.class, PacketSCMultiplayerPause::encode, PacketSCMultiplayerPause::decode,
                 PacketSCMultiplayerPause::handle);
        register(PacketSCFixRotation.class, PacketSCFixRotation::encode, PacketSCFixRotation::decode, PacketSCFixRotation::handle);
        register(PacketSCShader.class, PacketSCShader::encode, PacketSCShader::decode, PacketSCShader::handle);
        register(PacketSCToast.class, PacketSCToast::encode, PacketSCToast::decode, PacketSCToast::handle);
        register(PacketSCHungerData.class, PacketSCHungerData::encode, PacketSCHungerData::decode, PacketSCHungerData::handle);
        register(PacketSCTemperatureData.class, PacketSCTemperatureData::encode, PacketSCTemperatureData::decode, PacketSCTemperatureData::handle);
        register(PacketCSHitInformation.class, PacketCSHitInformation::encode, PacketCSHitInformation::decode, PacketCSHitInformation::handle);
        register(PacketCSStopUsingItem.class, PacketCSStopUsingItem::encode, PacketCSStopUsingItem::decode, PacketCSStopUsingItem::handle);
        register(PacketSCSyncServerConfig.class, PacketSCSyncServerConfig::encode, PacketSCSyncServerConfig::decode,
                 PacketSCSyncServerConfig::handle);
        register(PacketCSSyncServerConfig.class, PacketCSSyncServerConfig::encode, PacketCSSyncServerConfig::decode,
                 PacketCSSyncServerConfig::handle);
        register(PacketSCGC.class, PacketSCGC::encode, PacketSCGC::decode, PacketSCGC::handle);
        register(PacketCSCollision.class, PacketCSCollision::encode, PacketCSCollision::decode, PacketCSCollision::handle);
        register(PacketCSSpecialHit.class, PacketCSSpecialHit::encode, PacketCSSpecialHit::decode, PacketCSSpecialHit::handle);
        register(PacketSCUpdateCameraViewCenter.class, PacketSCUpdateCameraViewCenter::encode, PacketSCUpdateCameraViewCenter::decode,
                 PacketSCUpdateCameraViewCenter::handle);
        register(PacketSCMomentum.class, PacketSCMomentum::encode, PacketSCMomentum::decode, PacketSCMomentum::handle);
        register(PacketCSSpecialAttackStart.class, PacketCSSpecialAttackStart::encode, PacketCSSpecialAttackStart::decode,
                 PacketCSSpecialAttackStart::handle);
        register(PacketSCSpecialAttackStart.class, PacketSCSpecialAttackStart::encode, PacketSCSpecialAttackStart::decode,
                 PacketSCSpecialAttackStart::handle);
        register(PacketCSSpecialAttackStop.class, PacketCSSpecialAttackStop::encode, PacketCSSpecialAttackStop::decode,
                 PacketCSSpecialAttackStop::handle);
        register(PacketSCSpecialAttackStop.class, PacketSCSpecialAttackStop::encode, PacketSCSpecialAttackStop::decode,
                 PacketSCSpecialAttackStop::handle);
    }

    public static void resetCache() {
        PACKET_TARGET_CACHE.reset();
    }

    public static void send(ServerPlayer player, IPacket packet) {
        assert packet.getDestinationSide() == LogicalSide.CLIENT;
        PacketDistributor.PacketTarget target = PACKET_TARGET_CACHE.get(player.getId());
        if (target == null) {
            target = PacketDistributor.PLAYER.with(() -> player);
            PACKET_TARGET_CACHE.put(player.getId(), target);
        }
        INSTANCE.send(target, packet);
    }

    public static void sendToAll(IPacket packet) {
        assert packet.getDestinationSide() == LogicalSide.CLIENT;
        if (all == null) {
            all = PacketDistributor.ALL.noArg();
        }
        INSTANCE.send(all, packet);
    }

    public static void sendToServer(IPacket packet) {
        assert packet.getDestinationSide() == LogicalSide.SERVER;
        INSTANCE.sendToServer(packet);
    }

    public static void sendToTracking(Entity entity, IPacket packet) {
        assert packet.getDestinationSide() == LogicalSide.CLIENT;
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }
}
