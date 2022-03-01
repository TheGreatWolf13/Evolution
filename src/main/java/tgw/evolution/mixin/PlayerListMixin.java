package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCChangeTickrate;
import tgw.evolution.network.PacketSCMultiplayerPause;
import tgw.evolution.patches.IClientboundLoginPacketPatch;
import tgw.evolution.patches.IMinecraftServerPatch;
import tgw.evolution.stats.EvolutionServerStatsCounter;

import java.io.File;
import java.util.Optional;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow
    @Final
    private RegistryAccess.RegistryHolder registryHolder;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    private int simulationDistance;
    @Shadow
    private int viewDistance;

    @Shadow
    public abstract int getMaxPlayers();

    @Redirect(method = "getPlayerStats", at = @At(value = "NEW", target = "net/minecraft/stats/ServerStatsCounter"))
    public ServerStatsCounter getPlayerStatsProxy(MinecraftServer server, File statsFile) {
        return new EvolutionServerStatsCounter(server, statsFile);
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;" +
                                                                           "send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), locals =
            LocalCapture.CAPTURE_FAILHARD)
    private void onPlaceNewPlayer(Connection netManager,
                                  ServerPlayer player,
                                  CallbackInfo ci,
                                  GameProfile gameprofile,
                                  GameProfileCache playerprofilecache,
                                  Optional<GameProfile> gameprofile1,
                                  String s,
                                  CompoundTag compoundnbt,
                                  ResourceKey registrykey,
                                  ServerLevel serverworld,
                                  ServerLevel serverworld1,
                                  String s1,
                                  LevelData levelData,
                                  ServerGamePacketListenerImpl serverplaynethandler,
                                  GameRules gamerules,
                                  boolean flag,
                                  boolean flag1) {
        Packet<ClientGamePacketListener> packet = new ClientboundLoginPacket(player.getId(),
                                                                             levelData.isHardcore(),
                                                                             player.gameMode.getGameModeForPlayer(),
                                                                             player.gameMode.getPreviousGameModeForPlayer(),
                                                                             this.server.levelKeys(),
                                                                             this.registryHolder,
                                                                             serverworld1.dimensionType(),
                                                                             serverworld1.dimension(),
                                                                             BiomeManager.obfuscateSeed(serverworld1.getSeed()),
                                                                             this.getMaxPlayers(),
                                                                             this.viewDistance,
                                                                             this.simulationDistance,
                                                                             flag1,
                                                                             !flag,
                                                                             serverworld1.isDebug(),
                                                                             serverworld1.isFlat());
        ((IClientboundLoginPacketPatch) packet).setDaytime(serverworld1.getDayTime());
        ((IClientboundLoginPacketPatch) packet).setMotion(player.getDeltaMovement());
        serverplaynethandler.send(packet);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;" +
                                                                             "send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
    private void placeNewPlayerProxy0(ServerGamePacketListenerImpl serverPlayNetHandler, Packet<?> packet) {

    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/bossevents/CustomBossEvents;onPlayerConnect" +
                                                                             "(Lnet/minecraft/server/level/ServerPlayer;)V", ordinal = 0))
    private void placeNewPlayerProxy1(CustomBossEvents customServerBossInfoManager, ServerPlayer player) {
        customServerBossInfoManager.onPlayerConnect(player);
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCChangeTickrate(TickrateChanger.getCurrentTickrate()));
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                       new PacketSCMultiplayerPause(((IMinecraftServerPatch) player.getServer()).isMultiplayerPaused()));
    }
}
