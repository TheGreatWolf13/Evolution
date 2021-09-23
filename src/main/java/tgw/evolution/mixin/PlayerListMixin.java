package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.fml.network.PacketDistributor;
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
import tgw.evolution.patches.IMinecraftServerPatch;
import tgw.evolution.patches.ISJoinGamePacketPatch;
import tgw.evolution.stats.EvolutionServerStatisticsManager;

import java.io.File;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow
    @Final
    private DynamicRegistries.Impl registryHolder;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    private int viewDistance;

    @Shadow
    public abstract int getMaxPlayers();

    @Redirect(method = "getPlayerStats", at = @At(value = "NEW", target = "net/minecraft/stats/ServerStatisticsManager"))
    public ServerStatisticsManager getPlayerStatsProxy(MinecraftServer server, File statsFile) {
        return new EvolutionServerStatisticsManager(server, statsFile);
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/ServerPlayNetHandler;" +
                                                                           "send(Lnet/minecraft/network/IPacket;)V", ordinal = 0), locals =
            LocalCapture.CAPTURE_FAILHARD)
    private void onPlaceNewPlayer(NetworkManager netManager,
                                  ServerPlayerEntity player,
                                  CallbackInfo ci,
                                  GameProfile gameprofile,
                                  PlayerProfileCache playerprofilecache,
                                  GameProfile gameprofile1,
                                  String s,
                                  CompoundNBT compoundnbt,
                                  RegistryKey registrykey,
                                  ServerWorld serverworld,
                                  ServerWorld serverworld1,
                                  String s1,
                                  IWorldInfo iworldinfo,
                                  ServerPlayNetHandler serverplaynethandler,
                                  GameRules gamerules,
                                  boolean flag,
                                  boolean flag1) {
        SJoinGamePacket packet = new SJoinGamePacket(player.getId(),
                                                     player.gameMode.getGameModeForPlayer(),
                                                     player.gameMode.getPreviousGameModeForPlayer(),
                                                     BiomeManager.obfuscateSeed(serverworld1.getSeed()),
                                                     iworldinfo.isHardcore(),
                                                     this.server.levelKeys(),
                                                     this.registryHolder,
                                                     serverworld1.dimensionType(),
                                                     serverworld1.dimension(),
                                                     this.getMaxPlayers(),
                                                     this.viewDistance,
                                                     flag,
                                                     !flag,
                                                     serverworld1.isDebug(),
                                                     serverworld1.isFlat());
        ((ISJoinGamePacketPatch) packet).setDaytime(serverworld1.getDayTime());
        ((ISJoinGamePacketPatch) packet).setMotion(player.getDeltaMovement());
        serverplaynethandler.send(packet);
    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/ServerPlayNetHandler;" +
                                                                             "send(Lnet/minecraft/network/IPacket;)V", ordinal = 0))
    private void placeNewPlayerProxy0(ServerPlayNetHandler serverPlayNetHandler, IPacket<?> packet) {

    }

    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/CustomServerBossInfoManager;onPlayerConnect" +
                                                                             "(Lnet/minecraft/entity/player/ServerPlayerEntity;)V", ordinal = 0))
    private void placeNewPlayerProxy1(CustomServerBossInfoManager customServerBossInfoManager, ServerPlayerEntity player) {
        customServerBossInfoManager.onPlayerConnect(player);
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCChangeTickrate(TickrateChanger.getCurrentTickrate()));
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                                       new PacketSCMultiplayerPause(((IMinecraftServerPatch) player.getServer()).isMultiplayerPaused()));
    }
}
