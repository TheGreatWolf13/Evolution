package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.*;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.gui.ScreenCrash;
import tgw.evolution.client.gui.ScreenOutOfMemory;
import tgw.evolution.client.renderer.ICrashReset;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.renderer.chunk.EvClientMetricsSamplersProvider;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.ICancelableUse;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ItemUtils;
import tgw.evolution.network.PacketCSStopUsingItem;
import tgw.evolution.patches.IKeyMappingPatch;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.OptionalMutableBlockPos;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Metric;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements IMinecraftPatch {

    @Shadow @Final public static boolean ON_OSX;
    @Shadow @Final private static Component SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    @Shadow private static int fps;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private static CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK;
    @Unique private final OptionalMutableBlockPos lastHoldingPos = new OptionalMutableBlockPos();
    @Shadow public @Nullable Entity crosshairPickEntity;
    @Shadow @Final public Font font;
    @Shadow public String fpsString;
    @Shadow @Final public FrameTimer frameTimer;
    @Shadow public @Nullable MultiPlayerGameMode gameMode;
    @Shadow @Final public GameRenderer gameRenderer;
    @Shadow @Final public Gui gui;
    @Shadow public @Nullable HitResult hitResult;
    @Shadow @Final public KeyboardHandler keyboardHandler;
    @Shadow public @Nullable ClientLevel level;
    @Shadow public int missTime;
    @Shadow @Final public MouseHandler mouseHandler;
    @Shadow public boolean noRender;
    @Shadow @Final public Options options;
    @Shadow @Final public ParticleEngine particleEngine;
    @Shadow public float pausePartialTick;
    @Shadow public @Nullable LocalPlayer player;
    @Shadow public @Nullable Screen screen;
    @Shadow @Final public TextureManager textureManager;
    @Shadow @Final public Timer timer;
    @Unique private boolean attackKeyPressed;
    @Unique private int attackKeyTicks;
    @Shadow @Final private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    @Unique private int cancelUseCooldown;
    @Shadow private String debugPath;
    @Shadow private @Nullable Supplier<CrashReport> delayedCrash;
    @Shadow @Final private FontManager fontManager;
    @Shadow private @Nullable ProfileResults fpsPieResults;
    @Shadow private int frames;
    @Shadow private Thread gameThread;
    @Shadow private boolean isLocalServer;
    @Shadow @Final private LanguageManager languageManager;
    @Unique private int lastFrameRateLimit = 60;
    @Shadow private long lastNanoTime;
    @Shadow private long lastTime;
    @Shadow @Final private String launchedVersion;
    @Unique private EvLevelRenderer lvlRenderer;
    @Shadow @Final private RenderTarget mainRenderTarget;
    @Shadow private MetricsRecorder metricsRecorder;
    @Shadow @Final private MobEffectTextureManager mobEffectTextures;
    @Shadow @Final private ModelManager modelManager;
    @Unique private boolean multiplayerPause;
    @Shadow @Final private MusicManager musicManager;
    @Shadow private @Nullable Overlay overlay;
    @Shadow @Final private PaintingTextureManager paintingTextures;
    @Shadow private boolean pause;
    @Shadow private @Nullable Connection pendingConnection;
    @Shadow private @Nullable CompletableFuture<Void> pendingReload;
    @Shadow private ProfilerFiller profiler;
    @Shadow @Final private Queue<Runnable> progressTasks;
    @Shadow @Final private PeriodicNotificationManager regionalCompliancies;
    @Shadow @Final private ResourceLoadStateTracker reloadStateTracker;
    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow @Final private ReloadableResourceManager resourceManager;
    @Shadow @Final private PackRepository resourcePackRepository;
    @Shadow private int rightClickDelay;
    @Shadow private volatile boolean running;
    @Shadow @Final private SearchRegistry searchRegistry;
    @Shadow private @Nullable IntegratedServer singleplayerServer;
    @Shadow private @Nullable TutorialToast socialInteractionsToast;
    @Shadow @Final private SoundManager soundManager;
    @Shadow @Final private ToastComponent toast;
    @Shadow @Final private Tutorial tutorial;
    /**
     * Bit 0: isHoldingUse; <br>
     * Bit 1: canStartClicking; <br>
     * Bit 2~4: directionOfMovement; <br>
     */
    @Unique private byte useFlags;
    @Shadow @Final private VirtualScreen virtualScreen;
    @Shadow @Final private Window window;

    public MinecraftMixin(String name) {
        super(name);
    }

    @Shadow
    public static void crash(CrashReport p_71377_0_) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static SystemReport fillSystemReport(SystemReport pReport,
                                                 @Nullable Minecraft pMinecraft,
                                                 @Nullable LanguageManager pLanguageManager,
                                                 String pLaunchVersion, Options pOptions) {
        throw new AbstractMethodError();
    }

    @SuppressWarnings("ConstantConditions")
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
    protected abstract Path archiveProfilingReport(SystemReport p_167857_, List<Path> p_167858_);

    @Shadow
    public abstract void clearLevel(Screen p_213231_1_);

    @Shadow
    public abstract void clearResourcePacksOnError(Throwable pThrowable, @Nullable Component pErrorMessage);

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
        if (this.missTime <= 0 && !this.player.isUsingItem()) {
            if (leftClick && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                BlockPos hitPos = blockRayTrace.getBlockPos();
                if (!this.level.isEmptyBlock(hitPos) && !((ILivingEntityPatch) this.player).shouldRenderSpecialAttack()) {
                    Direction face = blockRayTrace.getDirection();
                    if (this.gameMode.continueDestroyBlock(hitPos, face)) {
                        this.particleEngine.addBlockHitEffects(hitPos, blockRayTrace);
                        this.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
            else {
                this.gameMode.stopDestroyBlock();
            }
        }
    }

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
            ServerLifecycleHooks.handleExit(report.getSaveFile() != null ? -1 : -2);
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
    public abstract @Nullable Entity getCameraEntity();

    @Shadow
    public abstract @Nullable ClientPacketListener getConnection();

    @Shadow
    protected abstract int getFramerateLimit();

    @Shadow
    public abstract RenderTarget getMainRenderTarget();

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
        if (((IKeyMappingPatch) this.options.keyTogglePerspective).consumeAllClicks()) {
            CameraType view = this.options.getCameraType();
            this.options.setCameraType(view.cycle());
            if (view.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
            this.lvlRenderer.needsUpdate();
        }
        if (((IKeyMappingPatch) this.options.keySmoothCamera).consumeAllClicks()) {
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
        if (((IKeyMappingPatch) this.options.keySocialInteractions).consumeAllClicks()) {
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
        if (((IKeyMappingPatch) this.options.keyInventory).consumeAllClicks()) {
            if (notPausedNorAttacking) {
                if (this.gameMode.isServerControlledInventory()) {
                    this.player.sendOpenInventory();
                }
                else {
                    this.tutorial.onOpenInventory();
                    this.setScreen(new InventoryScreen(this.player));
                }
            }
        }
        if (((IKeyMappingPatch) this.options.keyAdvancements).consumeAllClicks()) {
            this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
        }
        while (this.options.keySwapOffhand.consumeClick()) {
            if (notPausedNorAttacking) {
                this.useFlags = 0;
                if (!this.player.isSpectator()) {
                    //noinspection ObjectAllocationInLoop
                    this.getConnection()
                        .send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO,
                                                                Direction.DOWN));
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
        if (((IKeyMappingPatch) this.options.keyChat).consumeAllClicks()) {
            this.openChatScreen("");
        }
        if (this.screen == null && this.overlay == null && ((IKeyMappingPatch) this.options.keyCommand).consumeAllClicks()) {
            this.openChatScreen("/");
        }
        boolean shouldContinueAttacking = true;
        boolean isAttackKeyDown;
        //Using item
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                if (!this.multiplayerPause) {
                    this.gameMode.releaseUsingItem(this.player);
                }
            }
            if (((IKeyMappingPatch) this.options.keyAttack).consumeAllClicks()) {
                if (!this.multiplayerPause) {
                    ItemStack usedStack = this.player.getUseItem();
                    if (usedStack.getItem() instanceof ICancelableUse cancelable) {
                        if (cancelable.isCancelable(usedStack, this.player) && this.cancelUseCooldown == 0) {
                            ClientEvents.getInstance().resetCooldown(this.player.getUsedItemHand());
                            this.player.stopUsingItem();
                            this.cancelUseCooldown = 20;
                            EvolutionNetwork.sendToServer(new PacketCSStopUsingItem());
                        }
                    }
                    //TODO shield bash
                }
            }
            ((IKeyMappingPatch) this.options.keyUse).consumeAllClicks();
            ((IKeyMappingPatch) this.options.keyPickItem).consumeAllClicks();
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
                    if (((IKeyMappingPatch) this.options.keyAttack).consumeAllClicks()) {
                        if (!this.multiplayerPause) {
                            shouldContinueAttacking = this.startAttack();
                        }
                    }
                }
            }
            isAttackKeyDown = this.options.keyAttack.isDown();
            if (ClientEvents.getInstance().getMainhandIndicatorPercentage(0.0f) < 1) {
                ((IKeyMappingPatch) this.options.keyAttack).consumeAllClicks();
            }
            if (((IKeyMappingPatch) this.options.keyUse).consumeAllClicks()) {
                if (notPausedNorAttacking) {
                    this.useFlags = 2;
                    this.lastHoldingPos.remove();
                    this.startUseItem();
                    this.useFlags &= ~2;
                }
            }
            if (((IKeyMappingPatch) this.options.keyPickItem).consumeAllClicks()) {
                if (!this.multiplayerPause) {
                    this.pickBlock();
                }
            }
        }
        if (this.options.keyUse.isDown() && !this.player.isUsingItem()) {
            if (notPausedNorAttacking) {
                boolean special = false;
                if ((this.useFlags & 1) != 0) {
                    special = this.startUseItemSpecial();
                }
                if (!special && this.rightClickDelay == 0) {
                    this.startUseItem();
                }
            }
        }
        this.continueAttack(
                this.screen == null &&
                shouldContinueAttacking &&
                isAttackKeyDown &&
                this.mouseHandler.isMouseGrabbed() &&
                !this.multiplayerPause);
    }

    @Shadow
    public abstract boolean hasSingleplayerServer();

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

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;" +
                                                                     "registerReloadListener(Lnet/minecraft/server/packs/resources" +
                                                                     "/PreparableReloadListener;)V", ordinal = 15))
    private void onInit(ReloadableResourceManager instance, PreparableReloadListener listener) {
        this.lvlRenderer = new EvLevelRenderer((Minecraft) (Object) this, this.renderBuffers);
        instance.registerReloadListener(this.lvlRenderer);
    }

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
            ForgeHooks.onPickBlock(this.hitResult, this.player, this.level);
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
                    //noinspection ObjectAllocationInLoop
                    this.setScreen(new ScreenOutOfMemory(hasAlreadyBeenOutOfMemory));
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", outOfMemory);
                    hasAlreadyBeenOutOfMemory = true;
                }
                catch (ReportedException exception) {
                    this.fillReport(exception.getReport());
                    this.emergencySave();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", exception);
                    this.displayCrashScreen(exception.getReport());
                }
                catch (Throwable t) {
                    //noinspection ObjectAllocationInLoop
                    CrashReport report = this.fillReport(new CrashReport("Unexpected error", t));
                    this.emergencySave();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", t);
                    this.displayCrashScreen(report);
                }
            }
        }
        catch (ReportedException reportedException) {
            this.fillReport(reportedException.getReport());
            this.emergencySave();
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", reportedException);
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

    @Override
    public void setMultiplayerPaused(boolean paused) {
        this.multiplayerPause = paused;
    }

    @Shadow
    public abstract void setOverlay(@Nullable Overlay pLoadingGui);

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    public abstract boolean shouldEntityAppearGlowing(Entity pEntity);

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
                BlockPos hitPos = blockRayTrace.getBlockPos();
                if (!this.level.isEmptyBlock(hitPos) && !((ILivingEntityPatch) this.player).shouldRenderSpecialAttack()) {
                    this.gameMode.startDestroyBlock(hitPos, blockRayTrace.getDirection());
                    if (this.level.getBlockState(hitPos).isAir()) {
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
        if (this.gameMode.isDestroying()) {
            return;
        }
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
                    if (actResult.consumesAction()) {
                        if (actResult.shouldSwing()) {
                            this.player.swing(hand);
                            if ((this.useFlags & 2) != 0 && ItemUtils.canRepeatUse(stack)) {
                                this.useFlags |= 1;
                                Direction direction = ClientEvents.getDirectionFromInput(this.player.getDirection(), this.player.input);
                                if (direction != null) {
                                    this.useFlags |= direction.ordinal() + 1 << 2;
                                }
                                this.lastHoldingPos.setWithOffset(blockRayTrace.getBlockPos(), blockRayTrace.getDirection());
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
            if (!this.lastHoldingPos.isSame(blockRayTrace.getBlockPos(), blockRayTrace.getDirection(), direction, true)) {
                return true;
            }
            for (InteractionHand hand : MathHelper.HANDS_OFF_PRIORITY) {
                ItemStack stack = this.player.getItemInHand(hand);
                if (hand == InteractionHand.OFF_HAND && stack.isEmpty()) {
                    continue;
                }
                if (!ItemUtils.canRepeatUse(stack)) {
                    continue;
                }
                InteractionResult actResult = this.gameMode.useItemOn(this.player, this.level, hand, blockRayTrace);
                if (actResult.consumesAction()) {
                    if (actResult.shouldSwing()) {
                        this.player.swing(hand);
                        this.lastHoldingPos.setWithOffset(blockRayTrace.getBlockPos(), blockRayTrace.getDirection());
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
        this.tutorial.onLookAt(this.level, this.hitResult);
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
                    Component message = new TranslatableComponent("tutorial.socialInteractions.description",
                                                                  Tutorial.key("socialInteractions"));
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
                this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
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
