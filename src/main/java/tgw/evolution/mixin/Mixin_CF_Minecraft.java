package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import net.fabricmc.loader.impl.game.minecraft.Hooks;
import net.minecraft.*;
import net.minecraft.client.Timer;
import net.minecraft.client.*;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.*;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.gui.EvolutionGui;
import tgw.evolution.client.gui.ScreenCrash;
import tgw.evolution.client.gui.ScreenEvolution;
import tgw.evolution.client.gui.ScreenOutOfMemory;
import tgw.evolution.client.gui.advancements.ScreenAdvancements;
import tgw.evolution.client.renderer.ICrashReset;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.renderer.chunk.EvClientMetricsSamplersProvider;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.datagen.DataGenerators;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.ClientHooks;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.items.ICancelableUse;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ItemUtils;
import tgw.evolution.network.Message;
import tgw.evolution.network.PacketCSSimpleMessage;
import tgw.evolution.patches.PatchMinecraft;
import tgw.evolution.util.OptionalMutableBlockPos;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Metric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(Minecraft.class)
public abstract class Mixin_CF_Minecraft extends ReentrantBlockableEventLoop<Runnable> implements PatchMinecraft, WindowEventHandler {

    @Shadow @Final public static boolean ON_OSX;
    @Shadow private static Minecraft instance;
    @Shadow @Final private static ResourceLocation REGIONAL_COMPLIANCIES;
    @Shadow @Final private static Component SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    @Shadow private static int fps;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK;
    @Unique private final OptionalMutableBlockPos lastHoldingPos;
    @Unique private final EvLevelRenderer lvlRenderer;
    @Shadow public @Nullable Entity crosshairPickEntity;
    @Mutable @Shadow @Final @RestoreFinal public DebugRenderer debugRenderer;
    @Mutable @Shadow @Final @RestoreFinal public Font font;
    @Shadow public String fpsString;
    @Mutable @Shadow @Final @RestoreFinal public FrameTimer frameTimer;
    @Mutable @Shadow @Final @RestoreFinal public File gameDirectory;
    @Shadow public @Nullable MultiPlayerGameMode gameMode;
    @Mutable @Shadow @Final @RestoreFinal public GameRenderer gameRenderer;
    @Mutable @Shadow @Final @RestoreFinal public Gui gui;
    @Shadow public @Nullable HitResult hitResult;
    @Mutable @Shadow @Final @RestoreFinal public KeyboardHandler keyboardHandler;
    @Shadow public @Nullable ClientLevel level;
    @Shadow @Final @DeleteField public LevelRenderer levelRenderer;
    @Shadow public int missTime;
    @Mutable @Shadow @Final @RestoreFinal public MouseHandler mouseHandler;
    @Shadow public boolean noRender;
    @Mutable @Shadow @Final @RestoreFinal public Options options;
    @Mutable @Shadow @Final @RestoreFinal public ParticleEngine particleEngine;
    @Shadow public float pausePartialTick;
    @Shadow public @Nullable LocalPlayer player;
    @Shadow public @Nullable Screen screen;
    @Shadow public boolean smartCull;
    @Mutable @Shadow @Final @RestoreFinal public Timer timer;
    @Mutable @Shadow @Final @RestoreFinal private boolean allowsChat;
    @Mutable @Shadow @Final @RestoreFinal private boolean allowsMultiplayer;
    @Unique private boolean attackKeyPressed;
    @Unique private int attackKeyTicks;
    @Mutable @Shadow @Final @RestoreFinal private BlockColors blockColors;
    @Mutable @Shadow @Final @RestoreFinal private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Mutable @Shadow @Final @RestoreFinal private BlockRenderDispatcher blockRenderer;
    @Unique private int cancelUseCooldown;
    @Mutable @Shadow @Final @RestoreFinal private ClientPackSource clientPackSource;
    @Shadow private String debugPath;
    @Shadow private @Nullable Supplier<CrashReport> delayedCrash;
    @Mutable @Shadow @Final @RestoreFinal private boolean demo;
    @Mutable @Shadow @Final @RestoreFinal private UUID deviceSessionId;
    @Mutable @Shadow @Final @RestoreFinal private EntityModelSet entityModels;
    @Mutable @Shadow @Final @RestoreFinal private EntityRenderDispatcher entityRenderDispatcher;
    @Mutable @Shadow @Final @RestoreFinal private DataFixer fixerUpper;
    @Mutable @Shadow @Final @RestoreFinal private FontManager fontManager;
    @Mutable @Shadow @Final @RestoreFinal private ContinuousProfiler fpsPieProfiler;
    @Shadow private @Nullable ProfileResults fpsPieResults;
    @Shadow private int frames;
    @Mutable @Shadow @Final @RestoreFinal private Game game;
    @Shadow private Thread gameThread;
    @Mutable @Shadow @Final @RestoreFinal private GpuWarnlistManager gpuWarnlistManager;
    @Mutable @Shadow @Final @RestoreFinal private HotbarManager hotbarManager;
    @Mutable @Shadow @Final @RestoreFinal private boolean is64bit;
    @Shadow private boolean isLocalServer;
    @Mutable @Shadow @Final @RestoreFinal private ItemColors itemColors;
    @Mutable @Shadow @Final @RestoreFinal private ItemInHandRenderer itemInHandRenderer;
    @Mutable @Shadow @Final @RestoreFinal private ItemRenderer itemRenderer;
    @Mutable @Shadow @Final @RestoreFinal private LanguageManager languageManager;
    @Unique private int lastFrameRateLimit;
    @Shadow private long lastNanoTime;
    @Shadow private long lastTime;
    @Mutable @Shadow @Final @RestoreFinal private String launchedVersion;
    @Mutable @Shadow @Final @RestoreFinal private LevelStorageSource levelSource;
    @Mutable @Shadow @Final @RestoreFinal private RenderTarget mainRenderTarget;
    @Shadow private MetricsRecorder metricsRecorder;
    @Mutable @Shadow @Final @RestoreFinal private MinecraftSessionService minecraftSessionService;
    @Mutable @Shadow @Final @RestoreFinal private MobEffectTextureManager mobEffectTextures;
    @Mutable @Shadow @Final @RestoreFinal private ModelManager modelManager;
    @Unique private boolean multiplayerPause;
    @Mutable @Shadow @Final @RestoreFinal private MusicManager musicManager;
    @Shadow private @Nullable Overlay overlay;
    @Mutable @Shadow @Final @RestoreFinal private PaintingTextureManager paintingTextures;
    @Shadow private boolean pause;
    @Shadow private @Nullable Connection pendingConnection;
    @Shadow private @Nullable CompletableFuture<Void> pendingReload;
    @Mutable @Shadow @Final @RestoreFinal private PlayerSocialManager playerSocialManager;
    @Mutable @Shadow @Final @RestoreFinal private PropertyMap profileProperties;
    @Shadow private ProfilerFiller profiler;
    @Mutable @Shadow @Final @RestoreFinal private AtomicReference<StoringChunkProgressListener> progressListener;
    @Mutable @Shadow @Final @RestoreFinal private Queue<Runnable> progressTasks;
    @Mutable @Shadow @Final @RestoreFinal private Proxy proxy;
    @Mutable @Shadow @Final @RestoreFinal private PeriodicNotificationManager regionalCompliancies;
    @Mutable @Shadow @Final @RestoreFinal private ResourceLoadStateTracker reloadStateTracker;
    @Mutable @Shadow @Final @RestoreFinal private RenderBuffers renderBuffers;
    @Unique private ItemUtils.RepeatedUse repeatedUse;
    @Mutable @Shadow @Final @RestoreFinal private ReloadableResourceManager resourceManager;
    @Mutable @Shadow @Final @RestoreFinal private File resourcePackDirectory;
    @Mutable @Shadow @Final @RestoreFinal private PackRepository resourcePackRepository;
    @Shadow private int rightClickDelay;
    @Shadow private volatile boolean running;
    @Mutable @Shadow @Final @RestoreFinal private SearchRegistry searchRegistry;
    @Shadow private @Nullable IntegratedServer singleplayerServer;
    @Mutable @Shadow @Final @RestoreFinal private SkinManager skinManager;
    @Shadow private @Nullable TutorialToast socialInteractionsToast;
    @Mutable @Shadow @Final @RestoreFinal private SoundManager soundManager;
    @Mutable @Shadow @Final @RestoreFinal private SplashManager splashManager;
    @Mutable @Shadow @Final @RestoreFinal private TextureManager textureManager;
    @Mutable @Shadow @Final @RestoreFinal private ToastComponent toast;
    @Mutable @Shadow @Final @RestoreFinal private Tutorial tutorial;
    /**
     * Bit 0: isHoldingUse; <br>
     * Bit 1: canStartClicking; <br>
     * Bit 2~4: directionOfMovement; <br>
     */
    @Unique private byte useFlags;
    @Mutable @Shadow @Final @RestoreFinal private User user;
    @Mutable @Shadow @Final @RestoreFinal private UserApiService userApiService;
    @Mutable @Shadow @Final @RestoreFinal private String versionType;
    @Mutable @Shadow @Final @RestoreFinal private VirtualScreen virtualScreen;
    @Mutable @Shadow @Final @RestoreFinal private Window window;

    @ModifyConstructor
    public Mixin_CF_Minecraft(GameConfig gameConfig) {
        super("Client");
        this.timer = new Timer(20.0F, 0L);
        this.searchRegistry = new SearchRegistry();
        this.progressListener = new AtomicReference();
        this.frameTimer = new FrameTimer();
        this.lastFrameRateLimit = 60;
        this.regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, Mixin_CF_Minecraft::countryEqualsISO3);
        this.game = new Game((Minecraft) (Object) this);
        this.deviceSessionId = UUID.randomUUID();
        this.lastNanoTime = Util.getNanos();
        this.running = true;
        this.fpsString = "";
        this.smartCull = true;
        this.progressTasks = Queues.newConcurrentLinkedQueue();
        this.profiler = InactiveProfiler.INSTANCE;
        this.fpsPieProfiler = new ContinuousProfiler(Util.timeSource, this::method_16010);
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
        this.reloadStateTracker = new ResourceLoadStateTracker();
        this.debugPath = "root";
        instance = (Minecraft) (Object) this;
        this.gameDirectory = gameConfig.location.gameDirectory;
        File file = gameConfig.location.assetDirectory;
        this.resourcePackDirectory = gameConfig.location.resourcePackDirectory;
        this.launchedVersion = gameConfig.game.launchVersion;
        this.versionType = gameConfig.game.versionType;
        this.profileProperties = gameConfig.user.profileProperties;
        this.clientPackSource = new ClientPackSource(new File(this.gameDirectory, "server-resource-packs"), gameConfig.location.getAssetIndex());
        this.resourcePackRepository = new PackRepository(Mixin_CF_Minecraft::createClientPackAdapter, this.clientPackSource, new FolderRepositorySource(this.resourcePackDirectory, PackSource.DEFAULT));
        this.proxy = gameConfig.user.proxy;
        YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(this.proxy);
        this.minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
        this.userApiService = this.createUserApiService(yggdrasilAuthenticationService, gameConfig);
        this.user = gameConfig.user.user;
        LOGGER.info("Setting user: {}", this.user.getName());
        LOGGER.debug("(Session ID is {})", this.user.getSessionId());
        this.demo = gameConfig.game.demo;
        this.allowsMultiplayer = !gameConfig.game.disableMultiplayer;
        this.allowsChat = !gameConfig.game.disableChat;
        this.is64bit = checkIs64Bit();
        this.singleplayerServer = null;
        KeybindComponent.setKeyResolver(KeyMapping::createNameSupplier);
        this.fixerUpper = DataFixers.getDataFixer();
        this.toast = new ToastComponent((Minecraft) (Object) this);
        Hooks.startClient(this.gameDirectory, this);
        this.gameThread = Thread.currentThread();
        this.options = new Options((Minecraft) (Object) this, this.gameDirectory);
        this.tutorial = new Tutorial((Minecraft) (Object) this, this.options);
        this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
        if ("true".equals(System.clearProperty("evolution.datagen"))) {
            DataGenerators.run();
            System.exit(0);
        }
        LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
        DisplayData displayData;
        if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
            displayData = new DisplayData(this.options.overrideWidth, this.options.overrideHeight, gameConfig.display.fullscreenWidth, gameConfig.display.fullscreenHeight, gameConfig.display.isFullscreen);
        }
        else {
            displayData = gameConfig.display;
        }
        Util.timeSource = RenderSystem.initBackendSystem();
        this.virtualScreen = new VirtualScreen((Minecraft) (Object) this);
        this.window = this.virtualScreen.newWindow(displayData, this.options.fullscreenVideoModeString, this.createTitle());
        this.setWindowActive(true);
        if (!ON_OSX) {
            try {
                InputStream inputStream = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
                InputStream inputStream2 = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
                this.window.setIcon(inputStream, inputStream2);
            }
            catch (IOException var10) {
                LOGGER.error("Couldn't set icon", var10);
            }
        }
        this.window.setFramerateLimit(this.options.framerateLimit);
        this.mouseHandler = new MouseHandler((Minecraft) (Object) this);
        this.mouseHandler.setup(this.window.getWindow());
        this.keyboardHandler = new KeyboardHandler((Minecraft) (Object) this);
        this.keyboardHandler.setup(this.window.getWindow());
        RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
        this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
        this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.mainRenderTarget.clear(ON_OSX);
        this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        this.resourcePackRepository.reload();
        this.options.loadSelectedResourcePacks(this.resourcePackRepository);
        this.languageManager = new LanguageManager(this.options.languageCode);
        this.resourceManager.registerReloadListener(this.languageManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloadListener(this.textureManager);
        this.skinManager = new SkinManager(this.textureManager, new File(file, "skins"), this.minecraftSessionService);
        this.levelSource = new LevelStorageSource(this.gameDirectory.toPath().resolve("saves"), this.gameDirectory.toPath().resolve("backups"), this.fixerUpper);
        this.soundManager = new SoundManager(this.resourceManager, this.options);
        this.resourceManager.registerReloadListener(this.soundManager);
        this.splashManager = new SplashManager(this.user);
        this.resourceManager.registerReloadListener(this.splashManager);
        this.musicManager = new MusicManager((Minecraft) (Object) this);
        this.fontManager = new FontManager(this.textureManager);
        this.font = this.fontManager.createFont();
        this.resourceManager.registerReloadListener(this.fontManager.getReloadListener());
        this.selectMainFont(this.isEnforceUnicode());
        this.resourceManager.registerReloadListener(new GrassColorReloadListener());
        this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
        this.window.setErrorSection("Startup");
        RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
        this.window.setErrorSection("Post startup");
        this.blockColors = BlockColors.createDefault();
        this.itemColors = ItemColors.createDefault(this.blockColors);
        this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels);
        this.resourceManager.registerReloadListener(this.modelManager);
        this.entityModels = new EntityModelSet();
        this.resourceManager.registerReloadListener(this.entityModels);
        this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.font, this.entityModels, this::getBlockRenderer);
        this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
        BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer = new BlockEntityWithoutLevelRenderer(this.blockEntityRenderDispatcher, this.entityModels);
        this.resourceManager.registerReloadListener(blockEntityWithoutLevelRenderer);
        this.itemRenderer = new ItemRenderer(this.textureManager, this.modelManager, this.itemColors, blockEntityWithoutLevelRenderer);
        this.entityRenderDispatcher = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.font, this.options, this.entityModels);
        this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
        this.itemInHandRenderer = new ItemInHandRenderer((Minecraft) (Object) this);
        this.resourceManager.registerReloadListener(this.itemRenderer);
        this.renderBuffers = new RenderBuffers();
        this.gameRenderer = new GameRenderer((Minecraft) (Object) this, this.resourceManager, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.gameRenderer);
        this.playerSocialManager = new PlayerSocialManager((Minecraft) (Object) this, this.userApiService);
        this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), blockEntityWithoutLevelRenderer, this.blockColors);
        this.resourceManager.registerReloadListener(this.blockRenderer);
        this.lvlRenderer = new EvLevelRenderer((Minecraft) (Object) this, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.lvlRenderer);
        this.createSearchTrees();
        this.resourceManager.registerReloadListener(this.searchRegistry);
        this.particleEngine = new ParticleEngine(this.level, this.textureManager);
        this.resourceManager.registerReloadListener(this.particleEngine);
        this.paintingTextures = new PaintingTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.paintingTextures);
        this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.mobEffectTextures);
        this.gpuWarnlistManager = new GpuWarnlistManager();
        this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
        this.resourceManager.registerReloadListener(this.regionalCompliancies);
        this.gui = new EvolutionGui((Minecraft) (Object) this);
        this.debugRenderer = new DebugRenderer((Minecraft) (Object) this);
        RenderSystem.setErrorCallback(this::onFullscreenError);
        if (this.mainRenderTarget.width == this.window.getWidth() && this.mainRenderTarget.height == this.window.getHeight()) {
            if (this.options.fullscreen && !this.window.isFullscreen()) {
                this.window.toggleFullScreen();
                this.options.fullscreen = this.window.isFullscreen();
            }
        }
        else {
            int width = this.window.getWidth();
            StringBuilder stringBuilder = new StringBuilder("Recovering from unsupported resolution (" + width + "x" + this.window.getHeight() + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for " + "instructions).");
            if (GlDebug.isDebugEnabled()) {
                stringBuilder.append("\n\nReported GL debug messages:\n").append(String.join("\n", GlDebug.getLastOpenGlDebugMessages()));
            }
            this.window.setWindowed(this.mainRenderTarget.width, this.mainRenderTarget.height);
            TinyFileDialogs.tinyfd_messageBox("Minecraft", stringBuilder.toString(), "ok", "error", false);
        }
        this.window.updateVsync(this.options.enableVsync);
        this.window.updateRawMouseInput(this.options.rawMouseInput);
        this.window.setDefaultErrorCallback();
        this.resizeDisplay();
        this.gameRenderer.preloadUiShader(this.getClientPackSource().getVanillaPack());
        LoadingOverlay.registerTextures((Minecraft) (Object) this);
        List<PackResources> list = this.resourcePackRepository.openAllSelected();
        this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, list);
        this.setOverlay(new LoadingOverlay((Minecraft) (Object) this, this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list), this::method_24040, false));
        TitleScreen titleScreen = new TitleScreen(true);
        if (EvolutionConfig.needsWelcome()) {
            this.setScreen(new ScreenEvolution(titleScreen, true));
            EvolutionConfig.updateWelcome();
        }
        else {
            this.setScreen(titleScreen);
        }
        EvolutionClient.init((Minecraft) (Object) this);
        this.lastHoldingPos = new OptionalMutableBlockPos();
    }

    @Shadow
    private static boolean checkIs64Bit() {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean countryEqualsISO3(Object object) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void crash(CrashReport crashReport) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static Pack createClientPackAdapter(String string,
                                                Component component,
                                                boolean bl,
                                                Supplier<PackResources> supplier,
                                                PackMetadataSection packMetadataSection,
                                                Pack.Position position,
                                                PackSource packSource) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static SystemReport fillSystemReport(SystemReport pReport,
                                                 @Nullable Minecraft pMinecraft,
                                                 @Nullable LanguageManager pLanguageManager,
                                                 String pLaunchVersion, Options pOptions) {
        throw new AbstractMethodError();
    }

    private static void outputReport(CrashReport report) {
        try {
            if (report.getSaveFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getInstance().renderOnThread() ? "-client" : "-server";
                reportName += ".txt";
                File reportsDir = new File(Minecraft.getInstance().gameDirectory, "crash-reports");
                File reportFile = new File(reportsDir, reportName);
                report.saveToFile(reportFile);
            }
        }
        catch (Throwable e) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed saving report", e);
        }
        LOGGER.error(LogUtils.FATAL_MARKER, "Minecraft ran into a problem! " +
                                            (report.getSaveFile() != null ?
                                             "Report saved to: " + report.getSaveFile() :
                                             "Crash report could not be saved.") +
                                            "\n" +
                                            report.getFriendlyReport());
    }

    @Shadow
    public abstract boolean allowsMultiplayer();

    @Shadow
    protected abstract Path archiveProfilingReport(SystemReport p_167857_, List<Path> p_167858_);

    @Shadow
    public abstract void clearLevel(Screen p_213231_1_);

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Override
    @Overwrite
    public void close() {
        try {
            this.regionalCompliancies.close();
            this.modelManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.lvlRenderer.close();
            this.soundManager.destroy();
            this.resourcePackRepository.close();
            this.particleEngine.close();
            this.mobEffectTextures.close();
            this.paintingTextures.close();
            this.textureManager.close();
            this.resourceManager.close();
            Util.shutdownExecutors();
        }
        catch (Throwable throwable) {
            LOGGER.error("Shutdown failure!", throwable);
            throw throwable;
        }
        finally {
            this.virtualScreen.close();
            this.window.close();
        }
    }

    @Shadow
    protected abstract ProfilerFiller constructProfiler(boolean p_167971_, @Nullable SingleTickProfiler p_167972_);

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's input.
     */
    @Overwrite
    private void continueAttack(boolean leftClick) {
        if (!leftClick || ClientEvents.getInstance().shouldRenderSpecialAttack()) {
            this.missTime = 0;
        }
        assert this.player != null;
        assert this.level != null;
        assert this.gameMode != null;
        boolean destroying = false;
        if (this.missTime <= 0 && !this.player.isUsingItem()) {
            if (leftClick && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                int x = blockRayTrace.posX();
                int y = blockRayTrace.posY();
                int z = blockRayTrace.posZ();
                if (!this.level.isEmptyBlock_(x, y, z) && !this.player.shouldRenderSpecialAttack()) {
                    Direction face = blockRayTrace.getDirection();
                    if (this.gameMode.continueDestroyBlock_(x, y, z, face, blockRayTrace)) {
                        destroying = true;
                        this.particleEngine.crack_(x, y, z, face);
                        this.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
        if (!destroying) {
            this.gameMode.stopDestroyBlock();
        }
    }

    @Shadow
    protected abstract void createSearchTrees();

    @Shadow
    protected abstract String createTitle();

    @Shadow
    protected abstract UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig);

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public boolean debugClientMetricsStart(Consumer<TranslatableComponent> c) {
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsStop();
            return false;
        }
        Consumer<ProfileResults> consumer = p -> {
            int tickDuration = p.getTickDuration();
            double d0 = p.getNanoDuration() / (double) TimeUtil.NANOSECONDS_PER_SECOND;
            this.execute(() -> c.accept(new TranslatableComponent("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d0),
                                                                  tickDuration, String.format(Locale.ROOT, "%.2f", tickDuration / d0))));
        };
        Consumer<Path> consumer1 = p -> {
            Component component = new TextComponent(p.toString())
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, p.toFile().getParent())));
            this.execute(() -> c.accept(new TranslatableComponent("debug.profiling.stop", component)));
        };
        SystemReport systemreport = fillSystemReport(new SystemReport(), (Minecraft) (Object) this, this.languageManager, this.launchedVersion,
                                                     this.options);
        Consumer<List<Path>> consumer2 = p -> {
            Path path = this.archiveProfilingReport(systemreport, p);
            consumer1.accept(path);
        };
        Consumer<Path> consumer3;
        if (this.singleplayerServer == null) {
            consumer3 = p -> consumer2.accept(ImmutableList.of(p));
        }
        else {
            this.singleplayerServer.fillSystemReport(systemreport);
            CompletableFuture<Path> completablefuture = new CompletableFuture<>();
            CompletableFuture<Path> completablefuture1 = new CompletableFuture<>();
            CompletableFuture.allOf(completablefuture,
                                    completablefuture1)
                             .thenRunAsync(() -> consumer2.accept(ImmutableList.of(completablefuture.join(),
                                                                                   completablefuture1.join())), Util.ioPool());
            this.singleplayerServer.startRecordingMetrics(p -> {}, completablefuture1::complete);
            consumer3 = completablefuture::complete;
        }
        this.metricsRecorder = ActiveMetricsRecorder.createStarted(new EvClientMetricsSamplersProvider(Util.timeSource, this.lvlRenderer),
                                                                   Util.timeSource, Util.ioPool(), new MetricsPersister("client"),
                                                                   p_210757_ -> {
                                                                       this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
                                                                       consumer.accept(p_210757_);
                                                                   }, consumer3);
        return true;
    }

    @Shadow
    protected abstract void debugClientMetricsStop();

    @Unique
    private void displayCrashScreen(CrashReport report) {
        try {
            outputReport(report);
            this.options.renderDebug = false;
            this.options.renderDebugCharts = false;
            this.options.renderFpsChart = false;
            this.gui.getChat().clearMessages(true);
            this.setScreen(new ScreenCrash(report));
        }
        catch (Throwable t) {
            LOGGER.error("An uncaught exception occurred while displaying the crash screen, making normal report instead", t);
            crash(report);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replaces freeMemory to better recovery from crashes.
     */
    @Overwrite
    public void emergencySave() {
        try {
            try {
                MemoryReserve.release();
                ((ICrashReset) Tesselator.getInstance().getBuilder()).resetAfterCrash();
                ((ICrashReset) this.renderBuffers().bufferSource()).resetAfterCrash();
            }
            catch (Throwable ignored) {
            }
            try {
                System.gc();
                if (this.isLocalServer && this.singleplayerServer != null) {
                    this.singleplayerServer.halt(true);
                }
                this.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
            }
            catch (Throwable t) {
                LOGGER.error(LogUtils.FATAL_MARKER, "Exception thrown while trying to recover from crash!", t);
                crash(new CrashReport("Exception thrown while trying to recover from crash!", t));
                return;
            }
            if (this.getConnection() != null) {
                this.getConnection().getConnection().disconnect(new TextComponent("Client crashed"));
            }
            this.gameRenderer.shutdownEffect();
            try {
                MemoryReserve.allocate();
            }
            catch (Throwable ignored) {
            }
            System.gc();
        }
        catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
        }
    }

    @Shadow
    public abstract CrashReport fillReport(CrashReport p_71396_1_);

    @Shadow
    protected abstract void finishProfilers(boolean p_91339_, @Nullable SingleTickProfiler p_91340_);

    @Shadow
    public abstract BlockRenderDispatcher getBlockRenderer();

    @Shadow
    public abstract @Nullable Entity getCameraEntity();

    @Shadow
    public abstract ClientPackSource getClientPackSource();

    @Shadow
    public abstract @Nullable ClientPacketListener getConnection();

    @Shadow
    protected abstract int getFramerateLimit();

    @Override
    public ItemColors getItemColors() {
        return this.itemColors;
    }

    @Shadow
    public abstract RenderTarget getMainRenderTarget();

    @Overwrite
    public Music getSituationalMusic() {
        if (this.screen instanceof WinScreen) {
            return Musics.CREDITS;
        }
        if (this.player != null) {
            if (this.player.level.dimension() == Level.END) {
                return this.gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
            }
            Holder<Biome> holder = this.player.level.getBiome_(this.player.blockPosition());
            Biome.BiomeCategory biomeCategory = Biome.getBiomeCategory(holder);
            if (this.musicManager.isPlayingMusic(Musics.UNDER_WATER) ||
                this.player.isUnderWater() && (biomeCategory == Biome.BiomeCategory.OCEAN || biomeCategory == Biome.BiomeCategory.RIVER)) {
                return Musics.UNDER_WATER;
            }
            return this.player.level.dimension() != Level.NETHER &&
                   this.player.getAbilities().instabuild &&
                   this.player.getAbilities().mayfly ?
                   Musics.CREATIVE :
                   holder.value().getBackgroundMusic().orElse(Musics.GAME);
        }
        return Musics.MENU;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    public Component grabPanoramixScreenshot(File directory, int width, int height) {
        int i = this.window.getWidth();
        int j = this.window.getHeight();
        RenderTarget rendertarget = new TextureTarget(width, height, true, ON_OSX);
        assert this.player != null;
        float f = this.player.getXRot();
        float f1 = this.player.getYRot();
        float f2 = this.player.xRotO;
        float f3 = this.player.yRotO;
        this.gameRenderer.setRenderBlockOutline(false);
        try {
            this.gameRenderer.setPanoramicMode(true);
            this.lvlRenderer.graphicsChanged();
            this.window.setWidth(width);
            this.window.setHeight(height);
            for (int k = 0; k < 6; ++k) {
                switch (k) {
                    case 0 -> {
                        this.player.setYRot(f1);
                        this.player.setXRot(0.0F);
                    }
                    case 1 -> {
                        this.player.setYRot((f1 + 90.0F) % 360.0F);
                        this.player.setXRot(0.0F);
                    }
                    case 2 -> {
                        this.player.setYRot((f1 + 180.0F) % 360.0F);
                        this.player.setXRot(0.0F);
                    }
                    case 3 -> {
                        this.player.setYRot((f1 - 90.0F) % 360.0F);
                        this.player.setXRot(0.0F);
                    }
                    case 4 -> {
                        this.player.setYRot(f1);
                        this.player.setXRot(-90.0F);
                    }
                    default -> {
                        this.player.setYRot(f1);
                        this.player.setXRot(90.0F);
                    }
                }
                this.player.yRotO = this.player.getYRot();
                this.player.xRotO = this.player.getXRot();
                rendertarget.bindWrite(true);
                //noinspection ObjectAllocationInLoop
                this.gameRenderer.renderLevel(1.0F, 0L, new PoseStack());
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException ignored) {
                }
                //noinspection ObjectAllocationInLoop
                Screenshot.grab(directory, "panorama_" + k + ".png", rendertarget, p_210769_ -> {
                });
            }
            Component component = new TextComponent(directory.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(
                    s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, directory.getAbsolutePath())));
            return new TranslatableComponent("screenshot.success", component);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't save image", exception);
            return new TranslatableComponent("screenshot.failure", exception.getMessage());
        }
        finally {
            this.player.setXRot(f);
            this.player.setYRot(f1);
            this.player.xRotO = f2;
            this.player.yRotO = f3;
            this.gameRenderer.setRenderBlockOutline(true);
            this.window.setWidth(i);
            this.window.setHeight(j);
            rendertarget.destroyBuffers();
            this.gameRenderer.setPanoramicMode(false);
            this.lvlRenderer.graphicsChanged();
            this.getMainRenderTarget().bindWrite(true);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's input.
     */
    @Overwrite
    private void handleKeybinds() {
        assert this.player != null;
        assert this.gameMode != null;
        assert this.getConnection() != null;
        boolean notPausedNorAttacking = !this.multiplayerPause && !ClientEvents.getInstance().shouldRenderSpecialAttack();
        if (this.options.keyTogglePerspective.consumeAllClicks()) {
            CameraType view = this.options.getCameraType();
            this.options.setCameraType(view.cycle());
            if (view.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
            this.lvlRenderer.needsUpdate();
        }
        if (this.options.keySmoothCamera.consumeAllClicks()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }
        boolean isSaveToolbarDown = this.options.keySaveHotbarActivator.isDown();
        boolean isLoadToolbarDown = this.options.keyLoadHotbarActivator.isDown();
        for (int slot = 0; slot < 9; ++slot) {
            if (this.options.keyHotbarSlots[slot].consumeClick()) {
                this.useFlags = 0;
                if (notPausedNorAttacking) {
                    if (this.player.isSpectator()) {
                        this.gui.getSpectatorGui().onHotbarSelected(slot);
                    }
                    else if (!this.player.isCreative() || this.screen != null || !isLoadToolbarDown && !isSaveToolbarDown) {
                        this.player.getInventory().selected = slot;
                    }
                    else {
                        CreativeModeInventoryScreen.handleHotbarLoadOrSave((Minecraft) (Object) this, slot, isLoadToolbarDown, isSaveToolbarDown);
                    }
                }
            }
        }
        if (this.options.keySocialInteractions.consumeAllClicks()) {
            if (!this.isMultiplayerServer()) {
                this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                NarratorChatListener.INSTANCE.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE.getString());
            }
            else {
                if (this.socialInteractionsToast != null) {
                    this.tutorial.removeTimedToast(this.socialInteractionsToast);
                    this.socialInteractionsToast = null;
                }
                this.setScreen(new SocialInteractionsScreen());
            }
        }
        if (this.options.keyInventory.consumeAllClicks()) {
            if (notPausedNorAttacking) {
                if (this.gameMode.isServerControlledInventory()) {
                    this.player.sendOpenInventory();
                }
                else {
                    this.player.connection.send(new PacketCSSimpleMessage(Message.C2S.OPEN_INVENTORY));
                }
            }
        }
        if (this.options.keyAdvancements.consumeAllClicks()) {
            this.setScreen(new ScreenAdvancements(this.player.connection.getAdvancements()));
        }
        while (this.options.keySwapOffhand.consumeClick()) {
            if (notPausedNorAttacking) {
                this.useFlags = 0;
                if (!this.player.isSpectator()) {
                    //noinspection ObjectAllocationInLoop
                    this.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                }
            }
        }
        while (this.options.keyDrop.consumeClick()) {
            if (notPausedNorAttacking) {
                assert this.player != null;
                if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
                    this.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
        if (this.options.keyChat.consumeAllClicks()) {
            this.openChatScreen("");
        }
        if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeAllClicks()) {
            this.openChatScreen("/");
        }
        boolean shouldContinueAttacking = true;
        boolean isAttackKeyDown;
        boolean startedThisTick = false;
        //Using item
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                if (!this.multiplayerPause) {
                    this.gameMode.releaseUsingItem(this.player);
                }
            }
            if (this.options.keyAttack.consumeAllClicks()) {
                if (!this.multiplayerPause) {
                    ItemStack usedStack = this.player.getUseItem();
                    if (usedStack.getItem() instanceof ICancelableUse cancelable) {
                        if (cancelable.isCancelable(usedStack, this.player) && this.cancelUseCooldown == 0) {
                            ClientEvents.getInstance().resetCooldown(this.player.getUsedItemHand());
                            this.player.stopUsingItem();
                            this.cancelUseCooldown = 20;
                            this.player.connection.send(new PacketCSSimpleMessage(Message.C2S.STOP_USING_ITEM));
                        }
                    }
                    //TODO shield bash
                }
            }
            this.options.keyUse.consumeAllClicks();
            this.options.keyPickItem.consumeAllClicks();
            isAttackKeyDown = this.options.keyAttack.isDown();
        }
        //Not using item
        else {
            if (this.missTime == 0) {
                ItemStack mainhandStack = this.player.getMainHandItem();
                //Holdable Attack
                //Tools that can attack as well as weapons that have a "lunge" attack
                if (mainhandStack.getItem() instanceof IMelee melee && melee.isHoldable(mainhandStack)) {
                    int minAttackTime = melee.getMinAttackTime(mainhandStack);
                    int autoAttackTime = melee.getAutoAttackTime(mainhandStack);
                    if (this.options.keyAttack.isDown()) {
                        if (!this.multiplayerPause) {
                            shouldContinueAttacking = false;
                            IMelee.ChargeAttackType chargeAttackType = melee.getChargeAttackType(mainhandStack);
                            if (chargeAttackType != null) {
                                if (!this.attackKeyPressed && ClientEvents.getInstance().getMainhandIndicatorPercentage(0.0f) >= 1.0f) {
                                    if (++this.attackKeyTicks >= autoAttackTime) {
                                        //Hold Attack
                                        if (this.missTime == 0) {
                                            ClientEvents.getInstance().startChargeAttack(chargeAttackType);
                                            this.attackKeyPressed = true;
                                        }
                                    }
                                }
                                this.options.keyAttack.release();
                            }
                            else {
                                if (++this.attackKeyTicks >= autoAttackTime) {
                                    //Break Block
                                    shouldContinueAttacking = this.startAttack();
                                    this.attackKeyPressed = true;
                                }
                            }
                        }
                    }
                    else {
                        if (!this.multiplayerPause) {
                            if (!this.attackKeyPressed) {
                                if (ClientEvents.getInstance().getMainhandIndicatorPercentage(0.0f) >= 1.0f) {
                                    if (this.attackKeyTicks >= minAttackTime) {
                                        IMelee.ChargeAttackType chargeAttackType = melee.getChargeAttackType(mainhandStack);
                                        if (chargeAttackType != null) {
                                            //Hold Attack
                                            ClientEvents.getInstance().startChargeAttack(chargeAttackType);
                                        }
                                    }
                                    else if (this.attackKeyTicks > 0) {
                                        //Short Attack
                                        if (ClientEvents.getInstance().checkHitResultBeforeShortAttack()) {
                                            ClientEvents.getInstance().startShortAttack(mainhandStack);
                                        }
                                        else {
                                            shouldContinueAttacking = this.startAttack();
                                        }
                                    }
                                }
                            }
                            this.attackKeyTicks = 0;
                            this.options.keyAttack.release();
                            this.attackKeyPressed = false;
                        }
                    }
                }
                else {
                    if (this.options.keyAttack.consumeAllClicks()) {
                        if (!this.multiplayerPause) {
                            shouldContinueAttacking = this.startAttack();
                        }
                    }
                }
            }
            isAttackKeyDown = this.options.keyAttack.isDown();
            if (ClientEvents.getInstance().getMainhandIndicatorPercentage(0.0f) < 1) {
                this.options.keyAttack.consumeAllClicks();
            }
            if (this.options.keyUse.consumeAllClicks()) {
                if (notPausedNorAttacking) {
                    this.useFlags = 2;
                    this.lastHoldingPos.remove();
                    this.startUseItem();
                    this.useFlags &= ~2;
                    startedThisTick = true;
                }
            }
            if (this.options.keyPickItem.consumeAllClicks()) {
                if (!this.multiplayerPause) {
                    this.pickBlock();
                }
            }
        }
        if (this.options.keyUse.isDown() && !this.player.isUsingItem()) {
            if (notPausedNorAttacking) {
                boolean special = false;
                if ((this.useFlags & 1) != 0 && (this.repeatedUse != ItemUtils.RepeatedUse.NOT_ON_FIRST_TICK || !startedThisTick)) {
                    special = this.startUseItemSpecial();
                }
                if (!special && this.rightClickDelay == 0) {
                    this.startUseItem();
                }
            }
        }
        this.continueAttack(this.screen == null && shouldContinueAttacking && isAttackKeyDown && this.mouseHandler.isMouseGrabbed() && !this.multiplayerPause);
    }

    @Shadow
    public abstract boolean hasSingleplayerServer();

    @Shadow
    public abstract boolean isEnforceUnicode();

    @Override
    public boolean isMultiplayerPaused() {
        return this.multiplayerPause;
    }

    @Shadow
    protected abstract boolean isMultiplayerServer();

    @Override
    public EvLevelRenderer lvlRenderer() {
        return this.lvlRenderer;
    }

    @Shadow
    protected abstract int method_16010();

    @Shadow
    protected abstract void method_24040(Optional par1);

    @Shadow
    protected abstract void onFullscreenError(int i, long l);

    @Shadow
    protected abstract void openChatScreen(String defaultText);

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's input.
     */
    @Overwrite
    private void pickBlock() {
        if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
            assert this.player != null;
            assert this.level != null;
            ClientHooks.onPickBlock(this.hitResult, this.player, this.level);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    private CompletableFuture<Void> reloadResourcePacks(boolean p_168020_) {
        if (this.pendingReload != null) {
            return this.pendingReload;
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (!p_168020_ && this.overlay instanceof LoadingOverlay) {
            this.pendingReload = future;
            return future;
        }
        this.resourcePackRepository.reload();
        List<PackResources> list = this.resourcePackRepository.openAllSelected();
        if (!p_168020_) {
            this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, list);
        }
        this.setOverlay(new LoadingOverlay((Minecraft) (Object) this,
                                           this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list),
                                           t -> Util.ifElse(t, this::rollbackResourcePacks, () -> {
                                               this.lvlRenderer.allChanged();
                                               this.reloadStateTracker.finishReload();
                                               future.complete(null);
                                           }), true));
        return future;
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Shadow
    @Deprecated
    public abstract CompletableFuture<Void> reloadResourcePacks();

    @Shadow
    public abstract RenderBuffers renderBuffers();

    /**
     * @author TheGreatWolf
     * @reason Improve performance
     */
    @Overwrite
    private void renderFpsMeter(PoseStack matrices, ProfileResults results) {
        List<ResultField> list = results.getTimes(this.debugPath);
        RenderSystem.clear(256, ON_OSX);
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        Matrix4f ortho = Matrix4f.orthographic(0.0F, this.window.getWidth(), 0.0F, this.window.getHeight(), 1_000.0F, 3_000.0F);
        RenderSystem.setProjectionMatrix(ortho);
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.setIdentity();
        internalMat.translate(0, 0, -2_000);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.disableTexture();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        int x = this.window.getWidth() - 160 - 10;
        int y = this.window.getHeight() - 320;
        RenderSystem.enableBlend();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(x - 176.0F, y - 96.0F - 16.0F, 0).color(200, 0, 0, 0).endVertex();
        builder.vertex(x - 176.0F, y + 320, 0).color(200, 0, 0, 0).endVertex();
        builder.vertex(x + 176.0F, y + 320, 0).color(200, 0, 0, 0).endVertex();
        builder.vertex(x + 176.0F, y - 96.0F - 16.0F, 0).color(200, 0, 0, 0).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        double d0 = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            ResultField result = list.get(i);
            int l = Mth.floor(result.percentage * (1 / 4.0)) + 1;
            builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            int color = result.getColor();
            int r = color >> 16 & 255;
            int g = color >> 8 & 255;
            int b = color & 255;
            builder.vertex(x, y, 0).color(r, g, b, 255).endVertex();
            for (int i2 = l; i2 >= 0; --i2) {
                float f = (float) ((d0 + result.percentage * i2 / l) * (Mth.TWO_PI / 100));
                float f1 = 160.0F * Mth.sin(f);
                float f2 = 160.0F * 0.5F * Mth.cos(f);
                builder.vertex(x + f1, y - f2, 0).color(r, g, b, 255).endVertex();
            }
            tesselator.end();
            builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int l2 = l; l2 >= 0; --l2) {
                float f3 = (float) ((d0 + result.percentage * l2 / l) * (Mth.TWO_PI / 100));
                float f4 = 160.0F * Mth.sin(f3);
                float f5 = 160.0F * 0.5F * Mth.cos(f3);
                if (!(f5 > 0.0F)) {
                    builder.vertex(x + f4, y - f5, 0)
                           .color(r >> 1, g >> 1, b >> 1, 255)
                           .endVertex();
                    builder.vertex(x + f4, y - f5 + 10.0F, 0)
                           .color(r >> 1, g >> 1, b >> 1, 255)
                           .endVertex();
                }
            }
            tesselator.end();
            d0 += result.percentage;
        }
        ResultField result = list.remove(0);
        DecimalFormat format = Metric.PERCENT_TWO_PLACES_FULL;
        RenderSystem.enableTexture();
        String demanglePath = ProfileResults.demanglePath(result.name);
        String s1 = "";
        if (!"unspecified".equals(demanglePath)) {
            s1 += "[0] ";
        }
        if (demanglePath.isEmpty()) {
            s1 += "ROOT ";
        }
        else {
            s1 = s1 + demanglePath + " ";
        }
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(builder);
        Matrix4f matrix = matrices.last().pose();
        this.font.drawInBatch(s1, x - 160, y - 80 - 16, 16_777_215, true, matrix, buffer, false, 0, 15_728_880, false);
        s1 = format.format(result.globalPercentage * (1 / 100.0));
        this.font.drawInBatch(s1, x + 160 - this.font.width(s1), y - 80 - 16, 16_777_215, true, matrix, buffer, false, 0, 15_728_880, false);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, len = list.size(); i < len; i++) {
            stringBuilder.setLength(0);
            ResultField resultField = list.get(i);
            if ("unspecified".equals(resultField.name)) {
                stringBuilder.append("[?] ");
            }
            else {
                stringBuilder.append("[").append(i + 1).append("] ");
            }
            String s2 = stringBuilder.append(resultField.name).toString();
            int y1 = y + 80 + i * 8 + 20;
            int color = resultField.getColor();
            this.font.drawInBatch(s2, x - 160, y1, color, true, matrix, buffer, false, 0, 15_728_880, false);
            s2 = format.format(resultField.percentage * (1 / 100.0));
            this.font.drawInBatch(s2, x + 160 - 50 - this.font.width(s2), y1, color, true, matrix, buffer, false, 0, 15_728_880, false);
            s2 = format.format(resultField.globalPercentage * (1 / 100.0));
            this.font.drawInBatch(s2, x + 160 - this.font.width(s2), y1, color, true, matrix, buffer, false, 0, 15_728_880, false);
        }
        buffer.endBatch();
    }

    @Override
    public void resetUseHeld() {
        this.useFlags = 0;
    }

    @Override
    @Shadow
    public abstract void resizeDisplay();

    @Shadow
    protected abstract void rollbackResourcePacks(Throwable p_91240_);

    /**
     * @author TheGreatWolf
     * @reason Replaces Minecraft's run method to be able to catch more exceptions.
     */
    @Overwrite
    public void run() {
        this.gameThread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() >= 4) {
            this.gameThread.setPriority(10);
        }
        try {
            boolean hasAlreadyBeenOutOfMemory = false;
            while (this.running) {
                if (this.delayedCrash != null) {
                    this.emergencySave();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Exception thrown on another thread!");
                    this.displayCrashScreen(this.delayedCrash.get());
                    this.delayedCrash = null;
                }
                try {
                    SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean debugMode = this.shouldRenderFpsPie();
                    //noinspection ConstantConditions
                    this.profiler = this.constructProfiler(debugMode, singleTickProfiler);
                    this.profiler.startTick();
                    this.metricsRecorder.startTick();
                    this.runTick(true);
                    this.metricsRecorder.endTick();
                    this.profiler.endTick();
                    //noinspection ConstantConditions
                    this.finishProfilers(debugMode, singleTickProfiler);
                }
                catch (OutOfMemoryError outOfMemory) {
                    this.emergencySave();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", outOfMemory);
                    //noinspection ObjectAllocationInLoop
                    this.setScreen(new ScreenOutOfMemory(hasAlreadyBeenOutOfMemory));
                    System.gc();
                    hasAlreadyBeenOutOfMemory = true;
                }
                catch (ReportedException exception) {
                    this.fillReport(exception.getReport());
                    LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", exception);
                    this.emergencySave();
                    this.displayCrashScreen(exception.getReport());
                }
                catch (Throwable t) {
                    //noinspection ObjectAllocationInLoop
                    CrashReport report = this.fillReport(new CrashReport("Unexpected error", t));
                    LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", t);
                    this.emergencySave();
                    this.displayCrashScreen(report);
                }
            }
        }
        catch (ReportedException reportedException) {
            this.fillReport(reportedException.getReport());
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", reportedException);
            this.emergencySave();
            crash(reportedException.getReport());
        }
        catch (Throwable throwable) {
            CrashReport crashReport = this.fillReport(new CrashReport("Unexpected error", throwable));
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", throwable);
            this.emergencySave();
            crash(crashReport);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Overwrite to handle multiplayer pause
     */
    @Overwrite
    private void runTick(boolean shouldRender) {
        this.window.setErrorSection("Pre render");
        long endTickTime = Util.getNanos() + 1_000_000_000L / this.lastFrameRateLimit;
        if (this.window.shouldClose()) {
            this.stop();
        }
        if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
            CompletableFuture<Void> completablefuture = this.pendingReload;
            this.pendingReload = null;
            this.reloadResourcePacks().thenRun(() -> completablefuture.complete(null));
        }
        for (Runnable runnable = this.progressTasks.poll(); runnable != null; runnable = this.progressTasks.poll()) {
            runnable.run();
        }
        if (shouldRender) {
            int j = this.timer.advanceTime(Util.getMillis());
            this.profiler.push("scheduledExecutables");
            this.runAllTasks();
            this.profiler.popPush("tick");
            for (int k = 0; k < Math.min(10, j); ++k) {
                this.profiler.incrementCounter("clientTick");
                this.tick();
            }
            this.profiler.pop();
        }
        this.mouseHandler.turnPlayer();
        this.window.setErrorSection("Render");
        this.profiler.push("sound");
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        this.profiler.popPush("render");
        PoseStack matrices = RenderSystem.getModelViewStack();
        matrices.pushPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(16_640, ON_OSX);
        this.mainRenderTarget.bindWrite(true);
        FogRenderer.setupNoFog();
        this.profiler.push("display");
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        this.profiler.pop();
        if (!this.noRender) {
            this.profiler.popPush("gameRenderer");
            this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, endTickTime, shouldRender);
            this.profiler.popPush("toasts");
            this.toast.render(new PoseStack());
            this.profiler.pop();
        }
        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
            this.profiler.pop();
        }
        this.profiler.push("blit");
        this.mainRenderTarget.unbindWrite();
        matrices.popPose();
        matrices.pushPose();
        RenderSystem.applyModelViewMatrix();
        this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
        matrices.popPose();
        RenderSystem.applyModelViewMatrix();
        this.profiler.popPush("updateDisplay");
        this.window.updateDisplay();
        int fpsLimit = this.getFramerateLimit();
        this.lastFrameRateLimit = Mth.clamp(fpsLimit, 1, 120);
        if (fpsLimit < Option.FRAMERATE_LIMIT.getMaxValue()) {
            RenderSystem.limitDisplayFPS(fpsLimit);
        }
        this.profiler.popPush("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setErrorSection("Post render");
        ++this.frames;
        //noinspection ConstantConditions
        boolean shouldPause = this.hasSingleplayerServer() &&
                              (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen()) &&
                              !this.singleplayerServer.isPublished() ||
                              this.getConnection() != null && this.multiplayerPause && shouldRender; //Added check for multiplayer pause
        if (this.pause != shouldPause) {
            if (this.pause) {
                this.pausePartialTick = this.timer.partialTick;
            }
            else {
                this.timer.partialTick = this.pausePartialTick;
            }
            this.pause = shouldPause;
        }
        long l = Util.getNanos();
        this.frameTimer.logFrameDuration(l - this.lastNanoTime);
        this.lastNanoTime = l;
        this.profiler.push("fpsUpdate");
        while (Util.getMillis() >= this.lastTime + 1_000L) {
            fps = this.frames;
            this.fpsString = String.format("%d fps T: %s%s%s%s B: %d", fps,
                                           this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf" : this.options.framerateLimit,
                                           this.options.enableVsync ? " vsync" : "", this.options.graphicsMode,
                                           this.options.renderClouds == CloudStatus.OFF ?
                                           "" :
                                           this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds",
                                           this.options.biomeBlendRadius);
            this.lastTime += 1_000L;
            this.frames = 0;
        }
        this.profiler.pop();
    }

    @Shadow
    abstract void selectMainFont(boolean bl);

    @Override
    public void setMultiplayerPaused(boolean paused) {
        this.multiplayerPause = paused;
    }

    @Shadow
    public abstract void setOverlay(@Nullable Overlay pLoadingGui);

    /**
     * @author TheGreatWolf
     * @reason Add screen handler
     */
    @Overwrite
    public void setScreen(@Nullable Screen screen) {
        if (SharedConstants.IS_RUNNING_IN_IDE && Thread.currentThread() != this.gameThread) {
            LOGGER.error("setScreen called from non-game thread");
        }
        if (this.screen != null) {
            this.screen.removed();
        }
        if (screen == null && this.level == null) {
            screen = new TitleScreen();
        }
        else {
            if (screen == null) {
                assert this.player != null;
                if (this.player.isDeadOrDying()) {
                    if (this.player.shouldShowDeathScreen()) {
                        screen = new DeathScreen(null, this.level.getLevelData().isHardcore());
                    }
                    else {
                        this.player.respawn();
                    }
                }
            }
        }
        ClientEvents.onGuiOpen(screen);
        this.screen = screen;
        BufferUploader.reset();
        if (screen != null) {
            this.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            screen.init((Minecraft) (Object) this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
            this.noRender = false;
        }
        else {
            this.soundManager.resume();
            this.mouseHandler.grabMouse();
        }
        this.updateTitle();
    }

    @Override
    @Shadow
    public abstract void setWindowActive(boolean bl);

    @Shadow
    protected abstract boolean shouldRenderFpsPie();

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's input.
     */
    @Overwrite
    private boolean startAttack() {
        if (this.missTime > 0) {
            return true;
        }
        assert this.gameMode != null;
        if (this.hitResult == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.gameMode.hasMissTime()) {
                this.missTime = 10;
            }
            return true;
        }
        assert this.player != null;
        assert this.level != null;
        if (this.player.isHandsBusy()) {
            return true;
        }
        boolean shouldContinueAttacking = true;
        boolean swingHand = true;
        switch (this.hitResult.getType()) {
            case ENTITY: {
                assert this.gameMode != null;
                if (this.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                    if (!this.attackKeyPressed) {
                        ClientEvents.getInstance().startShortAttack(this.player.getMainHandItem());
                        swingHand = false;
                    }
                    else {
                        return true;
                    }
                }
                else {
                    assert this.crosshairPickEntity != null;
                    this.gameMode.attack(this.player, this.crosshairPickEntity);
                }
                break;
            }
            case BLOCK: {
                BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                int x = blockRayTrace.posX();
                int y = blockRayTrace.posY();
                int z = blockRayTrace.posZ();
                if (!this.level.isEmptyBlock_(x, y, z) && !this.player.shouldRenderSpecialAttack()) {
                    this.gameMode.startDestroyBlock_(blockRayTrace);
                    if (this.level.isEmptyBlock_(x, y, z)) {
                        shouldContinueAttacking = false;
                    }
                    break;
                }
            }
            case MISS: {
                if (this.gameMode.hasMissTime()) {
                    this.missTime = 10;
                }
                if (this.attackKeyPressed) {
                    return true;
                }
                ItemStack mainHandItem = this.player.getMainHandItem();
                if (!this.player.isCreative() || mainHandItem.isEmpty() || mainHandItem.getItem() instanceof IMelee) {
                    ClientEvents.getInstance().startShortAttack(mainHandItem);
                }
                swingHand = false;
                break;
            }
        }
        if (swingHand) {
            this.player.swing(InteractionHand.MAIN_HAND);
        }
        return shouldContinueAttacking;
    }

    /**
     * @author TheGreatWolf
     * @reason Replace to handle Evolution's input.
     */
    @Overwrite
    private void startUseItem() {
        assert this.gameMode != null;
        assert this.player != null;
        assert this.level != null;
        this.rightClickDelay = 4;
        if (this.player.isHandsBusy()) {
            return;
        }
        if (this.hitResult == null) {
            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
            return;
        }
        if (this.cancelUseCooldown > 0) {
            return;
        }
        for (InteractionHand hand : MathHelper.HANDS_OFF_PRIORITY) {
            ItemStack stack = this.player.getItemInHand(hand);
            if (hand == InteractionHand.OFF_HAND && stack.isEmpty()) {
                continue;
            }
            switch (this.hitResult.getType()) {
                case ENTITY -> {
                    EntityHitResult entityRayTrace = (EntityHitResult) this.hitResult;
                    Entity entity = entityRayTrace.getEntity();
                    InteractionResult actionResult = this.gameMode.interactAt(this.player, entity, entityRayTrace, hand);
                    if (!actionResult.consumesAction()) {
                        actionResult = this.gameMode.interact(this.player, entity, hand);
                    }
                    if (actionResult.consumesAction()) {
                        if (actionResult.shouldSwing()) {
                            this.player.swing(hand);
                        }
                        return;
                    }
                }
                case BLOCK -> {
                    BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                    InteractionResult actResult = this.gameMode.useItemOn(this.player, this.level, hand, blockRayTrace);
                    if (this.gameMode.wasLastInteractionUsedOnBlock()) {
                        this.useFlags = 0;
                    }
                    if (actResult.consumesAction()) {
                        if (actResult.shouldSwing()) {
                            this.player.swing(hand);
                            if ((this.useFlags & 2) != 0 && (this.repeatedUse = ItemUtils.getRepeatedUse(stack)) != ItemUtils.RepeatedUse.NEVER) {
                                this.useFlags |= 1;
                                Direction direction = ClientEvents.getDirectionFromInput(this.player.getDirection(), this.player.input);
                                if (direction != null) {
                                    this.useFlags |= direction.ordinal() + 1 << 2;
                                }
                                this.lastHoldingPos.setWithOffset(blockRayTrace.posX(), blockRayTrace.posY(), blockRayTrace.posZ(), blockRayTrace.getDirection());
                            }
                        }
                        return;
                    }
                    if (actResult == InteractionResult.FAIL) {
                        return;
                    }
                }
            }
        }
        for (InteractionHand hand : MathHelper.HANDS_OFF_PRIORITY) {
            if (hand == InteractionHand.MAIN_HAND) {
                if (ClientEvents.getInstance().getMainhandIndicatorPercentage(0) < 1) {
                    return;
                }
            }
            else {
                if (ClientEvents.getInstance().getOffhandIndicatorPercentage(0) < 1) {
                    return;
                }
            }
            ItemStack stack = this.player.getItemInHand(hand);
            if (!stack.isEmpty()) {
                InteractionResult actionResult = this.gameMode.useItem(this.player, this.level, hand);
                if (actionResult.consumesAction()) {
                    if (actionResult.shouldSwing()) {
                        this.player.swing(hand);
                    }
                    this.gameRenderer.itemInHandRenderer.itemUsed(hand);
                    return;
                }
            }
        }
    }

    @Unique
    private boolean startUseItemSpecial() {
        assert this.gameMode != null;
        assert this.player != null;
        assert this.level != null;
        if (this.player.isHandsBusy()) {
            return true;
        }
        if (this.hitResult == null) {
            LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
            return true;
        }
        if (this.cancelUseCooldown > 0) {
            return true;
        }
        if (!this.lastHoldingPos.isPresent()) {
            return false;
        }
        Direction direction;
        if ((this.useFlags >> 2 & 0b111) == 0) {
            direction = ClientEvents.getDirectionFromInput(this.player.getDirection(), this.player.input);
            if (direction != null) {
                this.useFlags |= direction.ordinal() + 1 << 2;
            }
        }
        else {
            direction = DirectionUtil.ALL[(this.useFlags >> 2 & 0b111) - 1];
        }
        if (this.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
            int x = blockRayTrace.posX();
            int y = blockRayTrace.posY();
            int z = blockRayTrace.posZ();
            if (!this.lastHoldingPos.isSame(x, y, z, blockRayTrace.getDirection(), direction)) {
                return true;
            }
            for (InteractionHand hand : MathHelper.HANDS_OFF_PRIORITY) {
                ItemStack stack = this.player.getItemInHand(hand);
                if (hand == InteractionHand.OFF_HAND && stack.isEmpty()) {
                    continue;
                }
                if (ItemUtils.getRepeatedUse(stack) == ItemUtils.RepeatedUse.NEVER) {
                    continue;
                }
                InteractionResult actResult = this.gameMode.useItemOn(this.player, this.level, hand, blockRayTrace);
                if (this.gameMode.wasLastInteractionUsedOnBlock()) {
                    this.useFlags = 0;
                }
                if (actResult.consumesAction()) {
                    if (actResult.shouldSwing()) {
                        this.player.swing(hand);
                        this.lastHoldingPos.setWithOffset(x, y, z, blockRayTrace.getDirection());
                    }
                    this.rightClickDelay = 4;
                    return true;
                }
                if (actResult == InteractionResult.FAIL) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Shadow
    public abstract void stop();

    /**
     * @author TheGreatWolf
     * @reason Handle multiplayer pause, avoid memory allocation when possible.
     */
    @Overwrite
    public void tick() {
        if (this.rightClickDelay > 0) {
            --this.rightClickDelay;
        }
        this.profiler.push("preTick");
        ClientEvents client = ClientEvents.getInstanceNullable();
        if (client != null) {
            client.preClientTick();
        }
        this.profiler.popPush("gui");
        this.gui.tick(this.pause);
        this.profiler.pop();
        this.profiler.push("gameMode");
        if ((!this.pause || this.multiplayerPause) && this.level != null) { //Added check for multiplayer pause
            assert this.gameMode != null;
            this.gameMode.tick();
            //Added decrement for use cancel cooldown
            if (this.cancelUseCooldown > 0) {
                this.cancelUseCooldown--;
            }
        }
        this.profiler.popPush("textures");
        //noinspection VariableNotUsedInsideIf
        if (this.level != null) {
            this.textureManager.tick();
        }
        if (this.screen == null && this.player != null) {
            if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                this.setScreen(null);
            }
            else if (this.player.isSleeping() && this.level != null) {
                this.setScreen(new InBedChatScreen());
            }
        }
        else {
            if (this.screen instanceof InBedChatScreen bedChatScreen) {
                assert this.player != null;
                if (!this.player.isSleeping()) {
                    bedChatScreen.onPlayerWokeUp();
                }
            }
        }
        //noinspection VariableNotUsedInsideIf
        if (this.screen != null) {
            this.missTime = 10_000;
        }
        if (this.screen != null) {
            try {
                this.screen.tick();
            }
            catch (Throwable t) {
                CrashReport crash = CrashReport.forThrowable(t, "Ticking screen");
                CrashReportCategory category = crash.addCategory("Affected screen");
                category.setDetail("Screen name", this.screen.getClass().getCanonicalName());
                throw new ReportedException(crash);
            }
        }
        if (!this.options.renderDebug) {
            this.gui.clearCache();
        }
        if (this.overlay == null && (this.screen == null || this.screen.passEvents)) {
            this.profiler.popPush("Keybindings");
            this.handleKeybinds();
            //Check for multiplayer pause
            if (!this.multiplayerPause && this.missTime > 0) {
                --this.missTime;
            }
        }
        if (this.level != null) {
            this.profiler.popPush("gameRenderer");
            if (!this.pause) {
                this.gameRenderer.tick();
            }
            this.profiler.popPush("levelRenderer");
            if (!this.pause) {
                this.lvlRenderer.tick();
            }
            this.profiler.popPush("level");
            if (!this.pause) {
                if (this.level.getSkyFlashTime() > 0) {
                    this.level.setSkyFlashTime(this.level.getSkyFlashTime() - 1);
                }
                this.level.tickEntities();
            }
        }
        else if (this.gameRenderer.currentEffect() != null) {
            this.gameRenderer.shutdownEffect();
        }
        if (!this.pause) {
            this.musicManager.tick();
        }
        this.soundManager.tick(this.pause);
        if (this.level != null) {
            if (!this.pause) {
                if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
                    Component title = new TranslatableComponent("tutorial.socialInteractions.title");
                    Component message = new TranslatableComponent("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, title, message, true);
                    this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
                    this.options.joinedFirstServer = true;
                    this.options.save();
                }
                this.tutorial.tick();
                try {
                    this.level.tick(() -> true);
                }
                catch (Throwable t) {
                    CrashReport crash = CrashReport.forThrowable(t, "Exception in world tick");
                    if (this.level == null) {
                        CrashReportCategory category = crash.addCategory("Affected level");
                        category.setDetail("Problem", "Level is null!");
                    }
                    else {
                        this.level.fillReportDetails(crash);
                    }
                    throw new ReportedException(crash);
                }
            }
            this.profiler.popPush("animateTick");
            if (!this.pause && this.level != null) {
                assert this.player != null;
                BlockPos pos = this.player.blockPosition();
                this.level.animateTick(pos.getX(), pos.getY(), pos.getZ());
            }
            this.profiler.popPush("particles");
            if (!this.pause) {
                this.particleEngine.tick();
            }
        }
        else if (this.pendingConnection != null) {
            this.profiler.popPush("pendingConnection");
            this.pendingConnection.tick();
        }
        this.profiler.popPush("keyboard");
        this.keyboardHandler.tick();
        this.profiler.popPush("postTick");
        if (client != null) {
            client.postClientTick();
        }
        this.profiler.pop();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    private void updateLevelInEngines(@Nullable ClientLevel level) {
        this.lvlRenderer.setLevel(level);
        this.particleEngine.setLevel(level);
        this.blockEntityRenderDispatcher.setLevel(level);
        this.updateTitle();
    }

    @Shadow
    public abstract void updateTitle();
}
