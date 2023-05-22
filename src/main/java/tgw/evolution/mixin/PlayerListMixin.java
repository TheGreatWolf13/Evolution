package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkHooks;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCChangeTickrate;
import tgw.evolution.network.PacketSCMultiplayerPause;
import tgw.evolution.patches.IClientboundLoginPacketPatch;
import tgw.evolution.patches.IMinecraftServerPatch;
import tgw.evolution.stats.EvolutionServerStatsCounter;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private List<ServerPlayer> players;
    @Shadow
    @Final
    private Map<UUID, ServerPlayer> playersByUUID;
    @Shadow
    @Final
    private RegistryAccess.Frozen registryHolder;
    @Shadow
    @Final
    private MinecraftServer server;
    @Shadow
    private int simulationDistance;
    @Shadow
    private int viewDistance;

    @Shadow
    public abstract boolean addPlayer(ServerPlayer player);

    @Shadow
    public abstract void broadcastAll(Packet<?> pPacket);

    @Shadow
    public abstract void broadcastMessage(Component pMessage, ChatType pChatType, UUID pSenderUuid);

    @Shadow
    public abstract int getMaxPlayers();

    @Redirect(method = "getPlayerStats", at = @At(value = "NEW", target = "net/minecraft/stats/ServerStatsCounter"))
    public ServerStatsCounter getPlayerStatsProxy(MinecraftServer server, File statsFile) {
        return new EvolutionServerStatsCounter(server, statsFile);
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Shadow
    @Nullable
    public abstract CompoundTag load(ServerPlayer pPlayer);

    @Shadow
    public abstract void op(GameProfile pProfile);

    /**
     * @author TheGreatWolf
     * @reason Add data
     */
    @Overwrite
    public void placeNewPlayer(Connection connection, ServerPlayer player) {
        GameProfile gameProfile = player.getGameProfile();
        GameProfileCache profileCache = this.server.getProfileCache();
        Optional<GameProfile> optional = profileCache.get(gameProfile.getId());
        String profileName = optional.isPresent() ? optional.get().getName() : gameProfile.getName();
        profileCache.add(gameProfile);
        CompoundTag tag = this.load(player);
        ResourceKey<Level> dimension = tag != null ?
                                       DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, tag.get("Dimension")))
                                                    .resultOrPartial(LOGGER::error)
                                                    .orElse(Level.OVERWORLD) :
                                       Level.OVERWORLD;
        ServerLevel desiredLevel = this.server.getLevel(dimension);
        ServerLevel level;
        if (desiredLevel == null) {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", dimension);
            level = this.server.overworld();
        }
        else {
            level = desiredLevel;
        }
        player.setLevel(level);
        String s1 = "local";
        //noinspection ConstantConditions
        if (connection.getRemoteAddress() != null) {
            s1 = connection.getRemoteAddress().toString();
        }
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), s1, player.getId(), player.getX(),
                    player.getY(), player.getZ());
        LevelData levelData = level.getLevelData();
        player.loadGameTypes(tag);
        ServerGamePacketListenerImpl packetListener = new ServerGamePacketListenerImpl(this.server, connection, player);
        NetworkHooks.sendMCRegistryPackets(connection, "PLAY_TO_CLIENT");
        GameRules gameRules = level.getGameRules();
        boolean noDeathScreen = gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean reducedDebug = gameRules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        Packet<ClientGamePacketListener> packet = new ClientboundLoginPacket(player.getId(), levelData.isHardcore(),
                                                                             player.gameMode.getGameModeForPlayer(),
                                                                             player.gameMode.getPreviousGameModeForPlayer(),
                                                                             this.server.levelKeys(),
                                                                             this.registryHolder, level.dimensionTypeRegistration(),
                                                                             level.dimension(), BiomeManager.obfuscateSeed(level.getSeed()),
                                                                             this.getMaxPlayers(), this.viewDistance, this.simulationDistance,
                                                                             reducedDebug, !noDeathScreen,
                                                                             level.isDebug(), level.isFlat());
        ((IClientboundLoginPacketPatch) packet).setDaytime(level.getDayTime());
        ((IClientboundLoginPacketPatch) packet).setMotion(player.getDeltaMovement());
        packetListener.send(packet);
        packetListener.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(
                Unpooled.buffer()).writeUtf(this.getServer().getServerModName())));
        packetListener.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        packetListener.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
        packetListener.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
        MinecraftForge.EVENT_BUS.post(new OnDatapackSyncEvent((PlayerList) (Object) this, player));
        packetListener.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
        packetListener.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registryHolder)));
        this.sendPlayerPermissionLevel(player);
        player.getStats().markAllDirty();
        player.getRecipeBook().sendInitialRecipeBook(player);
        this.updateEntireScoreboard(level.getScoreboard(), player);
        this.server.invalidateStatus();
        MutableComponent joinMessage;
        if (player.getGameProfile().getName().equalsIgnoreCase(profileName)) {
            joinMessage = new TranslatableComponent("multiplayer.player.joined", player.getDisplayName());
        }
        else {
            joinMessage = new TranslatableComponent("multiplayer.player.joined.renamed", player.getDisplayName(), profileName);
        }
        this.broadcastMessage(joinMessage.withStyle(ChatFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID);
        packetListener.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        this.addPlayer(player);
        this.playersByUUID.put(player.getUUID(), player);
        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));
        for (int i = 0, len = this.players.size(); i < len; i++) {
            //noinspection ObjectAllocationInLoop
            player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.players.get(i)));
        }
        level.addNewPlayer(player);
        this.server.getCustomBossEvents().onPlayerConnect(player);
        EvolutionNetwork.send(player, new PacketSCChangeTickrate(TickrateChanger.getCurrentTickrate()));
        //noinspection ConstantConditions
        EvolutionNetwork.send(player, new PacketSCMultiplayerPause(((IMinecraftServerPatch) player.getServer()).isMultiplayerPaused()));
        this.sendLevelInfo(player, level);
        if (!this.server.getResourcePack().isEmpty()) {
            player.sendTexturePack(this.server.getResourcePack(), this.server.getResourcePackHash(), this.server.isResourcePackRequired(),
                                   this.server.getResourcePackPrompt());
        }
        for (MobEffectInstance effects : player.getActiveEffects()) {
            //noinspection ObjectAllocationInLoop
            packetListener.send(new ClientboundUpdateMobEffectPacket(player.getId(), effects));
        }
        if (tag != null && tag.contains("RootVehicle", Tag.TAG_COMPOUND)) {
            CompoundTag vehicleTag = tag.getCompound("RootVehicle");
            //noinspection ReturnOfNull
            Entity vehicle = EntityType.loadEntityRecursive(vehicleTag.getCompound("Entity"), level, e -> !level.addWithUUID(e) ? null : e);
            if (vehicle != null) {
                UUID uuid;
                if (vehicleTag.hasUUID("Attach")) {
                    uuid = vehicleTag.getUUID("Attach");
                }
                else {
                    uuid = null;
                }
                if (vehicle.getUUID().equals(uuid)) {
                    player.startRiding(vehicle, true);
                }
                else {
                    for (Entity entity : vehicle.getIndirectPassengers()) {
                        if (entity.getUUID().equals(uuid)) {
                            player.startRiding(entity, true);
                            break;
                        }
                    }
                }
                if (!player.isPassenger()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    vehicle.discard();
                    for (Entity e : vehicle.getIndirectPassengers()) {
                        e.discard();
                    }
                }
            }
        }
        player.initInventoryMenu();
        ForgeEventFactory.firePlayerLoggedIn(player);
    }

    @Shadow
    public abstract void sendLevelInfo(ServerPlayer pPlayer, ServerLevel pLevel);

    @Shadow
    public abstract void sendPlayerPermissionLevel(ServerPlayer pPlayer);

    @Shadow
    protected abstract void updateEntireScoreboard(ServerScoreboard pScoreboard, ServerPlayer pPlayer);
}
