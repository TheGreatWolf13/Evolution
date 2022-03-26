package tgw.evolution.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
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
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gui.ScreenCrash;
import tgw.evolution.client.gui.ScreenOutOfMemory;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.InputHooks;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.*;
import tgw.evolution.network.PacketCSStartLunge;
import tgw.evolution.network.PacketCSStopUsingItem;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements IMinecraftPatch {

    @Shadow
    @Final
    public static boolean ON_OSX;
    @Shadow
    @Final
    private static Component SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    private static int fps;
    @Shadow
    @Nullable
    public Entity crosshairPickEntity;
    @Shadow
    public String fpsString;
    @Shadow
    @Final
    public FrameTimer frameTimer;
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Shadow
    @Final
    public GameRenderer gameRenderer;
    @Shadow
    @Final
    public Gui gui;
    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Final
    public KeyboardHandler keyboardHandler;
    @Shadow
    @Nullable
    public ClientLevel level;
    @Shadow
    @Final
    public LevelRenderer levelRenderer;
    @Shadow
    public int missTime;
    @Shadow
    @Final
    public MouseHandler mouseHandler;
    @Shadow
    public boolean noRender;
    @Shadow
    @Final
    public Options options;
    @Shadow
    @Final
    public ParticleEngine particleEngine;
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    @Nullable
    public Screen screen;
    @Shadow
    @Final
    public TextureManager textureManager;
    private int cancelUseCooldown;
    @Shadow
    @Nullable
    private Supplier<CrashReport> delayedCrash;
    @Shadow
    @Nullable
    private ProfileResults fpsPieResults;
    @Shadow
    private int frames;
    @Shadow
    private Thread gameThread;
    @Shadow
    private boolean isLocalServer;
    @Shadow
    private long lastNanoTime;
    @Shadow
    private long lastTime;
    @Shadow
    @Final
    private RenderTarget mainRenderTarget;
    @Shadow
    private MetricsRecorder metricsRecorder;
    private boolean multiplayerPause;
    @Shadow
    @Final
    private MusicManager musicManager;
    @Shadow
    @Nullable
    private Overlay overlay;
    @Shadow
    private boolean pause;
    @Shadow
    private float pausePartialTick;
    @Shadow
    @Nullable
    private Connection pendingConnection;
    @Shadow
    @Nullable
    private CompletableFuture<Void> pendingReload;
    @Shadow
    private ProfilerFiller profiler;
    @Shadow
    @Final
    private Queue<Runnable> progressTasks;
    @Shadow
    private int rightClickDelay;
    @Shadow
    private volatile boolean running;
    @Shadow
    @Nullable
    private IntegratedServer singleplayerServer;
    @Shadow
    @Nullable
    private TutorialToast socialInteractionsToast;
    @Shadow
    @Final
    private SoundManager soundManager;
    @Shadow
    @Final
    private Timer timer;
    @Shadow
    @Final
    private ToastComponent toast;
    @Shadow
    @Final
    private Tutorial tutorial;
    @Shadow
    @Final
    private Window window;

    public MinecraftMixin(String name) {
        super(name);
    }

    @Shadow
    public static void crash(CrashReport p_71377_0_) {

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
            LOGGER.fatal("Failed saving report", e);
        }
        LOGGER.fatal("Minecraft ran into a problem! " +
                     (report.getSaveFile() != null ? "Report saved to: " + report.getSaveFile() : "Crash report could not be saved.") +
                     "\n" +
                     report.getFriendlyReport());
    }

    @Shadow
    public abstract void clearLevel(Screen p_213231_1_);

    @Shadow
    protected abstract ProfilerFiller constructProfiler(boolean p_167971_, @Nullable SingleTickProfiler p_167972_);

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's input.
     */
    @Overwrite
    private void continueAttack(boolean leftClick) {
        if (!leftClick) {
            this.missTime = 0;
        }
        if (this.missTime <= 0 && !this.player.isUsingItem()) {
            if (leftClick && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                BlockPos hitPos = blockRayTrace.getBlockPos();
                if (!this.level.isEmptyBlock(hitPos)) {
                    InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
                    if (inputEvent.isCanceled()) {
                        if (inputEvent.shouldSwingHand()) {
                            this.particleEngine.addBlockHitEffects(hitPos, blockRayTrace);
                            this.player.swing(InteractionHand.MAIN_HAND);
                            ClientEvents.getInstance().swingArm(InteractionHand.MAIN_HAND);
                        }
                        return;
                    }
                    Direction face = blockRayTrace.getDirection();
                    if (this.gameMode.continueDestroyBlock(hitPos, face)) {
                        if (inputEvent.shouldSwingHand()) {
                            this.particleEngine.addBlockHitEffects(hitPos, blockRayTrace);
                            this.player.swing(InteractionHand.MAIN_HAND);
                            ClientEvents.getInstance().swingArm(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
            else {
                this.gameMode.stopDestroyBlock();
            }
        }
    }

    @Shadow
    public abstract boolean debugClientMetricsStart(Consumer<TranslatableComponent> p_167947_);

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
            //noinspection ConstantConditions
            ServerLifecycleHooks.handleExit(report.getSaveFile() != null ? -1 : -2);
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Replaces freeMemory to better recovery from crashes.
     */
    @Overwrite
    public void emergencySave() {
        try {
            try {
                MemoryReserve.release();
                this.levelRenderer.clear();
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
                LOGGER.fatal("Exception thrown while trying to recover from crash!", t);
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
    @Nullable
    public abstract Entity getCameraEntity();

    @Shadow
    @Nullable
    public abstract ClientPacketListener getConnection();

    @Shadow
    protected abstract int getFramerateLimit();

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's input.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Overwrite
    private void handleKeybinds() {
        for (; this.options.keyTogglePerspective.consumeClick(); this.levelRenderer.needsUpdate()) {
            CameraType view = this.options.getCameraType();
            this.options.setCameraType(view.cycle());
            if (view.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
        }
        while (this.options.keySmoothCamera.consumeClick()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }
        boolean isMainhandSpecialAttacking = ClientEvents.getInstance().isMainhandInSpecialAttack();
        boolean isOffhandSpecialAttacking = ClientEvents.getInstance().isOffhandInSpecialAttack();
        for (int slot = 0; slot < 9; slot++) {
            boolean isSaveToolbarDown = this.options.keySaveHotbarActivator.isDown();
            boolean isLoadToolbarDown = this.options.keyLoadHotbarActivator.isDown();
            if (this.options.keyHotbarSlots[slot].consumeClick()) {
                if (!this.multiplayerPause && !isMainhandSpecialAttacking) {
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
        while (this.options.keySocialInteractions.consumeClick()) {
            if (!this.isMultiplayerServer()) {
                this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                NarratorChatListener.INSTANCE.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE.getString());
            }
            else {
                if (this.socialInteractionsToast != null) {
                    this.tutorial.removeTimedToast(this.socialInteractionsToast);
                    this.socialInteractionsToast = null;
                }
                //noinspection ObjectAllocationInLoop
                this.setScreen(new SocialInteractionsScreen());
            }
        }
        while (this.options.keyInventory.consumeClick()) {
            if (!this.multiplayerPause && !isMainhandSpecialAttacking && !isOffhandSpecialAttacking) {
                if (this.gameMode.isServerControlledInventory()) {
                    this.player.sendOpenInventory();
                }
                else {
                    this.tutorial.onOpenInventory();
                    //noinspection ObjectAllocationInLoop
                    this.setScreen(new InventoryScreen(this.player));
                }
            }
        }
        while (this.options.keyAdvancements.consumeClick()) {
            //noinspection ObjectAllocationInLoop
            this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
        }
        while (this.options.keySwapOffhand.consumeClick()) {
            if (!this.multiplayerPause && !isMainhandSpecialAttacking && !isOffhandSpecialAttacking) {
                if (!this.player.isSpectator()) {
                    //noinspection ObjectAllocationInLoop
                    this.getConnection()
                        .send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO,
                                                                Direction.DOWN));
                }
            }
        }
        while (this.options.keyDrop.consumeClick()) {
            if (!this.multiplayerPause && !isMainhandSpecialAttacking) {
                if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
                    this.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
        boolean isChatVisible = this.options.chatVisibility != ChatVisiblity.HIDDEN;
        if (isChatVisible) {
            while (this.options.keyChat.consumeClick()) {
                this.openChatScreen("");
            }
            if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
                this.openChatScreen("/");
            }
        }
        ItemStack offhandStack = this.player.getOffhandItem();
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                if (!this.multiplayerPause) {
                    this.gameMode.releaseUsingItem(this.player);
                }
            }
            while (this.options.keyAttack.consumeClick()) {
                if (!this.multiplayerPause) {
                    ItemStack usedStack = this.player.getUseItem();
                    if (usedStack.getItem() instanceof ICancelableUse cancelable) {
                        if (cancelable.isCancelable(usedStack) && this.cancelUseCooldown == 0) {
                            this.player.stopUsingItem();
                            this.cancelUseCooldown = 20;
                            //noinspection ObjectAllocationInLoop
                            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSStopUsingItem());
                        }
                    }
                    //TODO shield bash
                }
            }
            while (this.options.keyUse.consumeClick()) {
            }
            while (this.options.keyPickItem.consumeClick()) {
            }
        }
        else {
            Item mainhandItem = this.player.getMainHandItem().getItem();
            if (mainhandItem instanceof ILunge) {
                int lungeFullTime = ((ILunge) mainhandItem).getFullLungeTime();
                int lungeMinTime = ((ILunge) mainhandItem).getMinLungeTime();
                if (this.options.keyAttack.isDown()) {
                    if (!this.multiplayerPause) {
                        if (ClientEvents.getInstance().getMainhandCooledAttackStrength(0.0f) >= 1.0f && InputHooks.attackKeyReleased) {
                            InputHooks.attackKeyDown();
                            this.missTime = 1;
                            if (InputHooks.getAttackKeyDownTicks() >= lungeMinTime) {
                                if (!InputHooks.isMainhandLungeInProgress) {
                                    InputHooks.isMainhandLungeInProgress = true;
                                    LungeChargeInfo lunge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(this.player.getId());
                                    if (lunge == null) {
                                        ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(this.player.getId(), new LungeChargeInfo(InteractionHand.MAIN_HAND,
                                                                                                                         this.player.getMainHandItem(),
                                                                                                                         lungeFullTime -
                                                                                                                         lungeMinTime));
                                    }
                                    else {
                                        lunge.addInfo(InteractionHand.MAIN_HAND, this.player.getMainHandItem(), lungeFullTime - lungeMinTime);
                                    }
                                    EvolutionNetwork.INSTANCE.sendToServer(
                                            new PacketCSStartLunge(InteractionHand.MAIN_HAND, lungeFullTime - lungeMinTime));
                                }
                            }
                            if (InputHooks.getAttackKeyDownTicks() >= lungeFullTime) {
                                InputHooks.leftLunge((Minecraft) (Object) this, lungeMinTime, lungeFullTime);
                                InputHooks.releaseAttack(this.options.keyAttack);
                                InputHooks.attackKeyReleased = false;
                            }
                        }
                    }
                }
                else {
                    if (!this.multiplayerPause) {
                        InputHooks.attackKeyReleased = true;
                        if (InputHooks.getAttackKeyDownTicks() > lungeMinTime) {
                            InputHooks.leftLunge((Minecraft) (Object) this, lungeMinTime, lungeFullTime);
                        }
                        else if (InputHooks.getAttackKeyDownTicks() > 0) {
                            InputHooks.isMainhandLungeInProgress = false;
                            this.startAttack();
                        }
                        InputHooks.releaseAttack(this.options.keyAttack);
                    }
                }
            }
            else {
                InputHooks.isMainhandLungeInProgress = false;
                InputHooks.releaseAttack(this.options.keyAttack);
                InputHooks.attackKeyReleased = true;
                while (this.options.keyAttack.consumeClick()) {
                    if (!this.multiplayerPause) {
                        this.startAttack();
                    }
                }
            }
            if (offhandStack.getItem() instanceof ILunge iLunge && this.mouseHandler.isMouseGrabbed()) {
                int lungeFullTime = iLunge.getFullLungeTime();
                int lungeMinTime = iLunge.getMinLungeTime();
                if (this.options.keyUse.isDown()) {
                    if (!this.multiplayerPause) {
                        if (ClientEvents.getInstance().getOffhandCooledAttackStrength(offhandStack, 0.0f) >= 1.0f && InputHooks.useKeyReleased) {
                            InputHooks.useKeyDownTicks++;
                            if (InputHooks.useKeyDownTicks >= lungeMinTime) {
                                if (!InputHooks.isOffhandLungeInProgress) {
                                    InputHooks.isOffhandLungeInProgress = true;
                                    LungeChargeInfo lunge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(this.player.getId());
                                    if (lunge == null) {
                                        ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(this.player.getId(), new LungeChargeInfo(InteractionHand.OFF_HAND,
                                                                                                                         this.player.getOffhandItem(),
                                                                                                                         lungeFullTime -
                                                                                                                         lungeMinTime));
                                    }
                                    else {
                                        lunge.addInfo(InteractionHand.OFF_HAND, this.player.getOffhandItem(), lungeFullTime - lungeMinTime);
                                    }
                                    EvolutionNetwork.INSTANCE.sendToServer(
                                            new PacketCSStartLunge(InteractionHand.OFF_HAND, lungeFullTime - lungeMinTime));
                                }
                            }
                            if (InputHooks.useKeyDownTicks >= lungeFullTime) {
                                InputHooks.rightLunge((Minecraft) (Object) this, lungeMinTime, lungeFullTime);
                                InputHooks.useKeyDownTicks = 0;
                                InputHooks.useKeyReleased = false;
                            }
                        }
                    }
                }
                else {
                    if (!this.multiplayerPause) {
                        InputHooks.useKeyReleased = true;
                        if (InputHooks.useKeyDownTicks > lungeMinTime) {
                            InputHooks.rightLunge((Minecraft) (Object) this, lungeMinTime, lungeFullTime);
                        }
                        else if (InputHooks.useKeyDownTicks > 0) {
                            InputHooks.isOffhandLungeInProgress = false;
                            this.startUseItem();
                        }
                        InputHooks.useKeyDownTicks = 0;
                    }
                }
            }
            else {
                InputHooks.isOffhandLungeInProgress = false;
                InputHooks.useKeyDownTicks = 0;
                InputHooks.useKeyReleased = true;
                while (this.options.keyUse.consumeClick()) {
                    if (!this.multiplayerPause) {
                        this.startUseItem();
                    }
                }
            }
            while (this.options.keyPickItem.consumeClick()) {
                if (!this.multiplayerPause) {
                    this.pickBlock();
                }
            }
        }
        if (!(offhandStack.getItem() instanceof IOffhandAttackable) &&
            this.options.keyUse.isDown() &&
            this.rightClickDelay == 0 &&
            !this.player.isUsingItem()) {
            if (!this.multiplayerPause) {
                this.startUseItem();
            }
        }
        this.continueAttack(this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed() && !this.multiplayerPause);
    }

    @Shadow
    public abstract boolean hasSingleplayerServer();

    @Override
    public boolean isMultiplayerPaused() {
        return this.multiplayerPause;
    }

    @Shadow
    protected abstract boolean isMultiplayerServer();

    @Shadow
    protected abstract void openChatScreen(String defaultText);

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's input.
     */
    @Overwrite
    private void pickBlock() {
        if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
            if (!ForgeHooksClient.onClickInput(2, this.options.keyPickItem, InteractionHand.MAIN_HAND).isCanceled()) {
                ForgeHooks.onPickBlock(this.hitResult, this.player, this.level);
            }
        }
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Shadow
    @Deprecated
    public abstract CompletableFuture<Void> reloadResourcePacks();

    @Shadow
    protected abstract void renderFpsMeter(PoseStack p_91141_, ProfileResults p_91142_);

    /**
     * @author MGSchultz
     * <p>
     * Replaces Minecraft's run method to be able to catch more exceptions.
     */
    @Overwrite
    public void run() {
        this.gameThread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) {
            this.gameThread.setPriority(10);
        }
        try {
            boolean hasAlreadyBeenOutOfMemory = false;
            while (this.running) {
                if (this.delayedCrash != null) {
                    crash(this.delayedCrash.get());
                    return;
                }
                try {
                    SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean debugMode = this.shouldRenderFpsPie();
                    this.profiler = this.constructProfiler(debugMode, singleTickProfiler);
                    this.profiler.startTick();
                    this.metricsRecorder.startTick();
                    this.runTick(true);
                    this.metricsRecorder.endTick();
                    this.profiler.endTick();
                    this.finishProfilers(debugMode, singleTickProfiler);
                }
                catch (OutOfMemoryError outOfMemory) {
                    this.emergencySave();
                    //noinspection ObjectAllocationInLoop
                    this.setScreen(new ScreenOutOfMemory(hasAlreadyBeenOutOfMemory));
                    System.gc();
                    LOGGER.fatal("Out of memory", outOfMemory);
                    hasAlreadyBeenOutOfMemory = true;
                }
                catch (ReportedException exception) {
                    this.fillReport(exception.getReport());
                    this.emergencySave();
                    LOGGER.fatal("Reported exception thrown!", exception);
                    this.displayCrashScreen(exception.getReport());
                }
                catch (Throwable t) {
                    //noinspection ObjectAllocationInLoop
                    CrashReport report = this.fillReport(new CrashReport("Unexpected error", t));
                    this.emergencySave();
                    LOGGER.fatal("Unreported exception thrown!", t);
                    this.displayCrashScreen(report);
                }
            }
        }
        catch (ReportedException reportedException) {
            this.fillReport(reportedException.getReport());
            this.emergencySave();
            LOGGER.fatal("Reported exception thrown!", reportedException);
            crash(reportedException.getReport());
        }
        catch (Throwable throwable) {
            CrashReport crashReport = this.fillReport(new CrashReport("Unexpected error", throwable));
            LOGGER.fatal("Unreported exception thrown!", throwable);
            this.emergencySave();
            crash(crashReport);
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle multiplayer pause
     */
    @Overwrite
    private void runTick(boolean shouldRender) {
        this.window.setErrorSection("Pre render");
        long i = Util.getNanos();
        if (this.window.shouldClose()) {
            this.stop();
        }
        if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
            CompletableFuture<Void> completablefuture = this.pendingReload;
            this.pendingReload = null;
            this.reloadResourcePacks().thenRun(() -> completablefuture.complete(null));
        }
        Runnable runnable;
        while ((runnable = this.progressTasks.poll()) != null) {
            runnable.run();
        }
        if (shouldRender) {
            int j = this.timer.advanceTime(Util.getMillis());
            this.profiler.push("scheduledExecutables");
            this.runAllTasks();
            this.profiler.pop();
            this.profiler.push("tick");
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
        this.profiler.pop();
        this.profiler.push("render");
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
            ForgeEventFactory.onRenderTickStart(this.pause ? this.pausePartialTick : this.timer.partialTick);
            this.profiler.popPush("gameRenderer");
            this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, i, shouldRender);
            this.profiler.popPush("toasts");
            this.toast.render(new PoseStack());
            this.profiler.pop();
            ForgeEventFactory.onRenderTickEnd(this.pause ? this.pausePartialTick : this.timer.partialTick);
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
        int i1 = this.getFramerateLimit();
        if (i1 < Option.FRAMERATE_LIMIT.getMaxValue()) {
            RenderSystem.limitDisplayFPS(i1);
        }
        this.profiler.popPush("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setErrorSection("Post render");
        ++this.frames;
        boolean flag = this.hasSingleplayerServer() &&
                       (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen()) &&
                       !this.singleplayerServer.isPublished() ||
                       this.getConnection() != null && this.multiplayerPause && shouldRender; //Added check for multiplayer pause
        if (this.pause != flag) {
            if (this.pause) {
                this.pausePartialTick = this.timer.partialTick;
            }
            else {
                this.timer.partialTick = this.pausePartialTick;
            }
            this.pause = flag;
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
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    protected abstract boolean shouldRenderFpsPie();

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's input.
     */
    @Overwrite
    private void startAttack() {
        if (this.missTime <= 0) {
            if (this.hitResult == null) {
                LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
                if (this.gameMode.hasMissTime()) {
                    this.missTime = 10;
                }
                return;
            }
            if (!this.player.isHandsBusy()) {
                InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
                ItemStack mainhandStack = this.player.getMainHandItem();
                ISpecialAttack specialAttack = mainhandStack.getItem() instanceof ISpecialAttack special ? special : null;
                if (!inputEvent.isCanceled()) {
                    switch (this.hitResult.getType()) {
                        case ENTITY: {
                            if (this.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                                if (specialAttack == null) {
                                    ClientEvents.getInstance().leftMouseClick();
                                }
                                else {
                                    ClientEvents.getInstance()
                                                .startSpecialAttack(specialAttack.getBasicAttackType(mainhandStack), InteractionHand.MAIN_HAND);
                                }
                            }
                            else {
                                this.gameMode.attack(this.player, this.crosshairPickEntity);
                            }
                            break;
                        }
                        case BLOCK: {
                            BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                            BlockPos hitPos = blockRayTrace.getBlockPos();
                            if (!this.level.isEmptyBlock(hitPos)) {
                                this.gameMode.startDestroyBlock(hitPos, blockRayTrace.getDirection());
                                if (specialAttack != null) {
                                    ClientEvents.getInstance()
                                                .startSpecialAttack(specialAttack.getBasicAttackType(mainhandStack), InteractionHand.MAIN_HAND);
                                }
                                break;
                            }
                        }
                        case MISS: {
                            if (this.gameMode.hasMissTime()) {
                                this.missTime = 10;
                            }
                            if (specialAttack == null) {
                                ClientEvents.getInstance().leftMouseClick();
                                ForgeHooks.onEmptyLeftClick(this.player);
                            }
                            else {
                                ClientEvents.getInstance()
                                            .startSpecialAttack(specialAttack.getBasicAttackType(mainhandStack), InteractionHand.MAIN_HAND);
                            }
                            break;
                        }
                    }
                }
                if (inputEvent.shouldSwingHand()) {
                    if (specialAttack == null) {
                        ClientEvents.getInstance().swingArm(InteractionHand.MAIN_HAND);
                        this.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    /**
     * @author MGSchultz
     * <p>
     * Replace to handle Evolution's input.
     */
    @Overwrite
    private void startUseItem() {
        if (!this.gameMode.isDestroying()) {
            this.rightClickDelay = 4;
            if (!this.player.isHandsBusy()) {
                if (this.hitResult == null) {
                    LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                    return;
                }
                if (this.cancelUseCooldown > 0) {
                    return;
                }
                for (InteractionHand hand : InteractionHand.values()) {
                    //noinspection ObjectAllocationInLoop
                    InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(1, this.options.keyUse, hand);
                    if (inputEvent.isCanceled()) {
                        if (inputEvent.shouldSwingHand()) {
                            this.player.swing(hand);
                            ClientEvents.getInstance().swingArm(hand);
                        }
                        return;
                    }
                    ItemStack stack = this.player.getItemInHand(hand);
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
                                    if (inputEvent.shouldSwingHand()) {
                                        this.player.swing(hand);
                                        ClientEvents.getInstance().swingArm(hand);
                                    }
                                }
                                return;
                            }
                        }
                        case BLOCK -> {
                            BlockHitResult blockRayTrace = (BlockHitResult) this.hitResult;
                            int count = stack.getCount();
                            InteractionResult actResult = this.gameMode.useItemOn(this.player, this.level, hand, blockRayTrace);
                            if (actResult.consumesAction()) {
                                if (actResult.shouldSwing()) {
                                    if (inputEvent.shouldSwingHand()) {
                                        this.player.swing(hand);
                                        ClientEvents.getInstance().swingArm(hand);
                                    }
                                    if (!stack.isEmpty() && (stack.getCount() != count || this.gameMode.hasInfiniteItems())) {
                                        this.gameRenderer.itemInHandRenderer.itemUsed(hand);
                                        ClientRenderer.instance.resetEquipProgress(hand);
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
                ItemStack stackOffhand = this.player.getOffhandItem();
                Item itemOffhand = stackOffhand.getItem();
                if (itemOffhand instanceof IOffhandAttackable) {
                    ClientEvents.getInstance().rightMouseClick((IOffhandAttackable) itemOffhand, stackOffhand);
                    return;
                }
                boolean isLungingMainhand = InputHooks.isMainhandLungeInProgress || InputHooks.isMainhandLunging;
                //TODO use a IShield interface
                //this.player.getOffhandItem().isShield(this.player);
                boolean isOffhandShield = false;
                for (InteractionHand hand : MathHelper.HANDS_LEFT_PRIORITY) {
                    ItemStack stack = this.player.getItemInHand(hand);
                    if (stack.isEmpty() && this.hitResult.getType() == HitResult.Type.MISS) {
                        ForgeHooks.onEmptyClick(this.player, hand);
                    }
                    if (hand == InteractionHand.MAIN_HAND &&
                        (isLungingMainhand || ClientEvents.getInstance().getMainhandCooledAttackStrength(0.0f) < 1.0f)) {
                        return;
                    }
                    if (isLungingMainhand && isOffhandShield) {
                        return;
                    }
                    if (stack.getItem() instanceof IParry) {
                        if (InputHooks.parryCooldown > 0) {
                            return;
                        }
                    }
                    if (!stack.isEmpty()) {
                        InteractionResult actionResult = this.gameMode.useItem(this.player, this.level, hand);
                        if (actionResult.consumesAction()) {
                            if (actionResult.shouldSwing()) {
                                this.player.swing(hand);
                                ClientEvents.getInstance().swingArm(hand);
                                if (stack.getItem() instanceof IParry) {
                                    InputHooks.parryCooldown = 6;
                                    return;
                                }
                            }
                            this.gameRenderer.itemInHandRenderer.itemUsed(hand);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Shadow
    public abstract void stop();

    /**
     * @author MGSchultz
     * <p>
     * Overwrite to handle multiplayer pause.
     */
    @Overwrite
    public void tick() {
        if (this.rightClickDelay > 0) {
            --this.rightClickDelay;
        }
        this.profiler.push("preTick");
        ForgeEventFactory.onPreClientTick();
        this.profiler.popPush("gui");
        this.gui.tick(this.pause);
        this.profiler.pop();
        this.tutorial.onLookAt(this.level, this.hitResult);
        this.profiler.push("gameMode");
        if ((!this.pause || this.pause && this.multiplayerPause) && this.level != null) { //Added check for multiplayer pause
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
            Screen screen = this.screen;
            if (screen instanceof InBedChatScreen bedChatScreen) {
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
            Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
        }
        if (!this.options.renderDebug) {
            this.gui.clearCache();
        }
        if (this.overlay == null && (this.screen == null || this.screen.passEvents)) {
            this.profiler.popPush("Keybindings");
            this.handleKeybinds();
            if (this.missTime > 0) {
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
                this.levelRenderer.tick();
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
                    Component itextcomponent = new TranslatableComponent("tutorial.socialInteractions.title");
                    Component itextcomponent1 = new TranslatableComponent("tutorial.socialInteractions.description",
                                                                          Tutorial.key("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, itextcomponent, itextcomponent1, true);
                    this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
                    this.options.joinedFirstServer = true;
                    this.options.save();
                }
                this.tutorial.tick();
                try {
                    this.level.tick(() -> true);
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception in world tick");
                    if (this.level == null) {
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Affected level");
                        crashreportcategory.setDetail("Problem", "Level is null!");
                    }
                    else {
                        this.level.fillReportDetails(crashreport);
                    }
                    throw new ReportedException(crashreport);
                }
            }
            this.profiler.popPush("animateTick");
            if (!this.pause && this.level != null) {
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
        ForgeEventFactory.onPostClientTick();
        this.profiler.pop();
    }
}
