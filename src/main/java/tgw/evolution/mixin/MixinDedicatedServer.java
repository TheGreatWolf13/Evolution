package tgw.evolution.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.patches.obj.ServerConsoleThread;
import tgw.evolution.util.math.Metric;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer implements ServerInterface {

    @Shadow @Final static Logger LOGGER;
    @Shadow private @Nullable QueryThreadGs4 queryThreadGs4;
    @Shadow private @Nullable RconThread rconThread;
    @Shadow @Final private DedicatedServerSettings settings;

    public MixinDedicatedServer(Thread thread,
                                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                PackRepository packRepository,
                                WorldStem worldStem,
                                Proxy proxy,
                                DataFixer dataFixer,
                                @Nullable MinecraftSessionService minecraftSessionService,
                                @Nullable GameProfileRepository gameProfileRepository,
                                @Nullable GameProfileCache gameProfileCache,
                                ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, minecraftSessionService, gameProfileRepository,
              gameProfileCache,
              chunkProgressListenerFactory);
    }

    @Shadow
    protected abstract boolean convertOldUsers();

    @Override
    @Shadow
    public abstract String getLevelIdName();

    @Shadow
    public abstract long getMaxTickLength();

    @Shadow
    public abstract String getPackHash();

    /**
     * @author TheGreatWolf
     * @reason Freeze capabilities
     */
    @Override
    @Overwrite
    public boolean initServer() throws IOException {
        Thread thread = new ServerConsoleThread((DedicatedServer) (Object) this, LOGGER);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
        LOGGER.info("Starting minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        if (Runtime.getRuntime().maxMemory() / 1_024L / 1_024L < 512L) {
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }
        LOGGER.info("Loading properties");
        DedicatedServerProperties properties = this.settings.getProperties();
        if (this.isSingleplayer()) {
            this.setLocalIp("127.0.0.1");
        }
        else {
            this.setUsesAuthentication(properties.onlineMode);
            this.setPreventProxyConnections(properties.preventProxyConnections);
            this.setLocalIp(properties.serverIp);
        }
        this.setPvpAllowed(properties.pvp);
        this.setFlightAllowed(properties.allowFlight);
        this.setResourcePack(properties.resourcePack, this.getPackHash());
        this.setMotd(properties.motd);
        super.setPlayerIdleTimeout(properties.playerIdleTimeout.get());
        this.setEnforceWhitelist(properties.enforceWhitelist);
        this.worldData.setGameType(properties.gamemode);
        LOGGER.info("Default game type: {}", properties.gamemode);
        InetAddress inetAddress = null;
        if (!this.getLocalIp().isEmpty()) {
            inetAddress = InetAddress.getByName(this.getLocalIp());
        }
        if (this.getPort() < 0) {
            this.setPort(properties.serverPort);
        }
        this.initializeKeyPair();
        LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());
        try {
            assert this.getConnection() != null;
            this.getConnection().startTcpServerListener(inetAddress, this.getPort());
        }
        catch (IOException e) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", e.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
        }
        if (!this.usesAuthentication()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn(
                    "While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with " +
                    "any username they choose.");
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }
        if (this.convertOldUsers()) {
            this.getProfileCache().save();
        }
        if (!OldUsersConverter.serverReadyAfterUserconversion(this)) {
            return false;
        }
        this.setPlayerList(new DedicatedPlayerList((DedicatedServer) (Object) this, this.registryAccess(), this.playerDataStorage));
        long startLoad = Util.getNanos();
        SkullBlockEntity.setup(this.getProfileCache(), this.getSessionService(), this);
        GameProfileCache.setUsesAuthentication(this.usesAuthentication());
        EntityPlayerCorpse.setProfileCache(this.getProfileCache());
        EntityPlayerCorpse.setSessionService(this.getSessionService());
        LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
        this.loadLevel();
        long endLoad = Util.getNanos() - startLoad;
        LOGGER.info("Done ({})! For help, type \"help\"", Metric.time(Metric.fromMetric(endLoad - startLoad, Metric.NANO), 3));
        if (properties.announcePlayerAchievements != null) {
            this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(properties.announcePlayerAchievements, this);
        }
        if (properties.enableQuery) {
            LOGGER.info("Starting GS4 status listener");
            this.queryThreadGs4 = QueryThreadGs4.create(this);
        }
        if (properties.enableRcon) {
            LOGGER.info("Starting remote control listener");
            this.rconThread = RconThread.create(this);
        }
        if (this.getMaxTickLength() > 0L) {
            Thread watchdog = new Thread(new ServerWatchdog((DedicatedServer) (Object) this));
            watchdog.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
            watchdog.setName("Server Watchdog");
            watchdog.setDaemon(true);
            watchdog.start();
        }
        Items.AIR.fillItemCategory(CreativeModeTab.TAB_SEARCH, NonNullList.create());
        if (properties.enableJmxMonitoring) {
            MinecraftServerStatistics.registerJmxMonitoring(this);
            LOGGER.info("JMX monitoring enabled");
        }
        return true;
    }

    //Force override
    @Override
    public void tickServer(BooleanSupplier booleanSupplier) {
        List<ServerPlayer> players = this.getPlayerList().getPlayers();
        for (int i = 0, len = players.size(); i < len; i++) {
            players.get(i).awardStat(EvolutionStats.TIME_WITH_WORLD_OPEN);
        }
        super.tickServer(booleanSupplier);
    }
}
