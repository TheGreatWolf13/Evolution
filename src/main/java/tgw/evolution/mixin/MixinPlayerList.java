package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.network.Message;
import tgw.evolution.network.PacketSCChangeTickrate;
import tgw.evolution.network.PacketSCFixRotation;
import tgw.evolution.network.PacketSCSimpleMessage;
import tgw.evolution.util.NBTHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private Map<UUID, PlayerAdvancements> advancements;
    @Shadow @Final private List<ServerPlayer> players;
    @Shadow @Final private Map<UUID, ServerPlayer> playersByUUID;
    @Shadow @Final private RegistryAccess.Frozen registryHolder;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int simulationDistance;
    @Shadow @Final private Map<UUID, ServerStatsCounter> stats;
    @Shadow private int viewDistance;

    @Shadow
    public abstract void broadcastAll(Packet<?> pPacket);

    @Shadow
    public abstract void broadcastMessage(Component pMessage, ChatType pChatType, UUID pSenderUuid);

    @Shadow
    public abstract int getMaxPlayers();

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ServerStatsCounter getPlayerStats(Player player) {
        UUID uuid = player.getUUID();
        ServerStatsCounter statsCounter = this.stats.get(uuid);
        if (statsCounter == null) {
            File statsFolder = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File statsFile = new File(statsFolder, uuid + ".json");
            if (!statsFile.exists()) {
                File oldFile = new File(statsFolder, player.getName().getString() + ".json");
                Path path = oldFile.toPath();
                if (FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path) && path.startsWith(statsFolder.getPath()) && oldFile.isFile()) {
                    //noinspection ResultOfMethodCallIgnored
                    oldFile.renameTo(statsFile);
                }
            }
            statsCounter = new ServerStatsCounter(this.server, statsFile);
            this.stats.put(uuid, statsCounter);
        }
        return statsCounter;
    }

    @Shadow
    public abstract MinecraftServer getServer();

    @Shadow
    @Nullable
    public abstract CompoundTag load(ServerPlayer pPlayer);

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
        ResourceKey<Level> dimension = tag != null ? NBTHelper.decodeResourceKey(Registry.DIMENSION_REGISTRY, tag.get("Dimension"), LOGGER, Level.OVERWORLD) : Level.OVERWORLD;
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
        //noinspection ConstantValue
        if (connection.getRemoteAddress() != null) {
            s1 = connection.getRemoteAddress().toString();
        }
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), s1, player.getId(), player.getX(), player.getY(), player.getZ());
        LevelData levelData = level.getLevelData();
        player.loadGameTypes(tag);
        ServerGamePacketListenerImpl listener = new ServerGamePacketListenerImpl(this.server, connection, player);
        GameRules gameRules = level.getGameRules();
        boolean noDeathScreen = gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean reducedDebug = gameRules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        listener.send(new ClientboundLoginPacket(player.getId(),
                                                 levelData.isHardcore(),
                                                 player.gameMode.getGameModeForPlayer(),
                                                 player.gameMode.getPreviousGameModeForPlayer(),
                                                 this.server.levelKeys(),
                                                 this.registryHolder,
                                                 level.dimensionTypeRegistration(),
                                                 level.dimension(),
                                                 BiomeManager.obfuscateSeed(level.getSeed()),
                                                 this.getMaxPlayers(),
                                                 this.viewDistance,
                                                 this.simulationDistance,
                                                 reducedDebug,
                                                 !noDeathScreen,
                                                 level.isDebug(),
                                                 level.isFlat())
                              .setDaytime(level.getDayTime())
                              .setMotion(player.getDeltaMovement())
        );
        listener.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(this.getServer().getServerModName())));
        listener.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        listener.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
        listener.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
        listener.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
        listener.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registryHolder)));
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
        listener.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        this.players.add(player);
        this.playersByUUID.put(player.getUUID(), player);
        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));
        for (int i = 0, len = this.players.size(); i < len; i++) {
            //noinspection ObjectAllocationInLoop
            player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.players.get(i)));
        }
        level.addNewPlayer(player);
        this.server.getCustomBossEvents().onPlayerConnect(player);
        listener.send(new PacketSCChangeTickrate(TickrateChanger.getCurrentTickrate()));
        //noinspection ConstantConditions
        listener.send(new PacketSCSimpleMessage(player.getServer().isMultiplayerPaused() ? Message.S2C.MULTIPLAYER_PAUSE : Message.S2C.MULTIPLAYER_RESUME));
        this.sendLevelInfo(player, level);
        if (!this.server.getResourcePack().isEmpty()) {
            player.sendTexturePack(this.server.getResourcePack(), this.server.getResourcePackHash(), this.server.isResourcePackRequired(), this.server.getResourcePackPrompt());
        }
        for (MobEffectInstance effects : player.getActiveEffects()) {
            //noinspection ObjectAllocationInLoop
            listener.send(new ClientboundUpdateMobEffectPacket(player.getId(), effects));
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
        EntityEvents.onPlayerLogin(player);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace stats
     */
    @Overwrite
    public void remove(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        player.awardStat(EvolutionStats.LEAVE_GAME);
        this.save(player);
        if (player.isPassenger()) {
            Entity entity = player.getRootVehicle();
            if (entity.hasExactlyOnePlayerPassenger()) {
                LOGGER.debug("Removing player mount");
                player.stopRiding();
                entity.getPassengersAndSelf().forEach(e -> e.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
            }
        }
        player.unRide();
        level.removePlayerImmediately(player, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        player.getAdvancements().stopListening();
        this.players.remove(player);
        this.server.getCustomBossEvents().onPlayerDisconnect(player);
        UUID uuid = player.getUUID();
        ServerPlayer cachedPlayer = this.playersByUUID.get(uuid);
        if (cachedPlayer == player) {
            this.playersByUUID.remove(uuid);
            this.stats.remove(uuid);
            this.advancements.remove(uuid);
        }
        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, player));
    }

    /**
     * @author TheGreatWolf
     * @reason Fix player
     */
    @Overwrite
    public ServerPlayer respawn(ServerPlayer player, boolean keepEverything) {
        this.players.remove(player);
        player.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        BlockPos respawnPos = player.getRespawnPosition();
        float respawnAngle = player.getRespawnAngle();
        boolean isForced = player.isRespawnForced();
        ServerLevel desiredRespawnDim = this.server.getLevel(player.getRespawnDimension());
        Optional<Vec3> properRespawnPos;
        if (desiredRespawnDim != null && respawnPos != null) {
            properRespawnPos = Player.findRespawnPositionAndUseSpawnBlock(desiredRespawnDim, respawnPos, respawnAngle, isForced, keepEverything);
        }
        else {
            properRespawnPos = Optional.empty();
        }
        ServerLevel respawnDim = desiredRespawnDim != null && properRespawnPos.isPresent() ? desiredRespawnDim : this.server.overworld();
        ServerPlayer newPlayer = new ServerPlayer(this.server, respawnDim, player.getGameProfile());
        newPlayer.connection = player.connection;
        newPlayer.restoreFrom(player, keepEverything);
        newPlayer.setId(player.getId());
        newPlayer.setMainArm(player.getMainArm());
        for (String string : player.getTags()) {
            newPlayer.addTag(string);
        }
        boolean bl3 = false;
        if (properRespawnPos.isPresent()) {
            BlockState blockState = respawnDim.getBlockState_(respawnPos);
            boolean bl4 = blockState.is(Blocks.RESPAWN_ANCHOR);
            Vec3 vec3 = properRespawnPos.get();
            float g;
            if (!blockState.is(BlockTags.BEDS) && !bl4) {
                g = respawnAngle;
            }
            else {
                Vec3 vec32 = Vec3.atBottomCenterOf(respawnPos).subtract(vec3).normalize();
                g = (float) Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 57.295_776_367_187_5 - 90.0);
            }

            newPlayer.moveTo(vec3.x, vec3.y, vec3.z, g, 0.0F);
            newPlayer.setRespawnPosition(respawnDim.dimension(), respawnPos, respawnAngle, isForced, false);
            bl3 = !keepEverything && bl4;
        }
        else //noinspection VariableNotUsedInsideIf
            if (respawnPos != null) {
                newPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
            }
        while (!respawnDim.noCollision(newPlayer) && newPlayer.getY() < respawnDim.getMaxBuildHeight()) {
            newPlayer.setPos(newPlayer.getX(), newPlayer.getY() + 1.0D, newPlayer.getZ());
        }
        LevelData levelData = newPlayer.level.getLevelData();
        newPlayer.connection.send(new ClientboundRespawnPacket(newPlayer.level.dimensionTypeRegistration(), newPlayer.level.dimension(),
                                                               BiomeManager.obfuscateSeed(newPlayer.getLevel().getSeed()),
                                                               newPlayer.gameMode.getGameModeForPlayer(),
                                                               newPlayer.gameMode.getPreviousGameModeForPlayer(),
                                                               newPlayer.getLevel().isDebug(), newPlayer.getLevel().isFlat(),
                                                               keepEverything)
        );
        newPlayer.connection.teleport(newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(), newPlayer.getYRot(), newPlayer.getXRot());
        newPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(respawnDim.getSharedSpawnPos(), respawnDim.getSharedSpawnAngle()));
        newPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        this.sendLevelInfo(newPlayer, respawnDim);
        this.sendPlayerPermissionLevel(newPlayer);
        respawnDim.addRespawnedPlayer(newPlayer);
        this.players.add(newPlayer);
        this.playersByUUID.put(newPlayer.getUUID(), newPlayer);
        newPlayer.initInventoryMenu();
        newPlayer.setHealth(newPlayer.getHealth());
        newPlayer.getStats().markAllDirty();
        newPlayer.clearFire();
        for (Player otherPlayer : respawnDim.players()) {
            if (otherPlayer != newPlayer) {
                //noinspection ObjectAllocationInLoop
                newPlayer.connection.send(new PacketSCFixRotation(otherPlayer));
            }
        }
        respawnDim.getChunkSource().broadcast(newPlayer, new PacketSCFixRotation(player));
        if (bl3) {
            newPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(), 1.0F, 1.0F));
        }
        return newPlayer;
    }

    @Shadow
    protected abstract void save(ServerPlayer serverPlayer);

    @Shadow
    public abstract void sendLevelInfo(ServerPlayer pPlayer, ServerLevel pLevel);

    @Shadow
    public abstract void sendPlayerPermissionLevel(ServerPlayer pPlayer);

    @Shadow
    protected abstract void updateEntireScoreboard(ServerScoreboard pScoreboard, ServerPlayer pPlayer);
}
