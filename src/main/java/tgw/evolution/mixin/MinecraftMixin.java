package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.*;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.social.SocialInteractionsScreen;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.profiler.IProfileResult;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.LongTickDetector;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.*;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.gui.ScreenCrash;
import tgw.evolution.client.gui.ScreenOutOfMemory;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.InputHooks;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.ILunge;
import tgw.evolution.items.IOffhandAttackable;
import tgw.evolution.items.IParry;
import tgw.evolution.network.PacketCSStartLunge;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends RecursiveEventLoop<Runnable> implements IMinecraftPatch {

    @Shadow
    public static byte[] reserve;
    @Shadow
    @Final
    public static boolean ON_OSX;
    @Shadow
    @Final
    private static ITextComponent SOCIAL_INTERACTIONS_NOT_AVAILABLE;
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
    public PlayerController gameMode;
    @Shadow
    @Final
    public GameRenderer gameRenderer;
    @Shadow
    @Final
    public IngameGui gui;
    @Shadow
    @Nullable
    public RayTraceResult hitResult;
    @Shadow
    @Final
    public KeyboardListener keyboardHandler;
    @Shadow
    @Nullable
    public ClientWorld level;
    @Shadow
    @Final
    public WorldRenderer levelRenderer;
    @Shadow
    @Final
    public MouseHelper mouseHandler;
    @Shadow
    public boolean noRender;
    @Shadow
    @Final
    public GameSettings options;
    @Shadow
    @Nullable
    public LoadingGui overlay;
    @Shadow
    @Final
    public ParticleManager particleEngine;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Nullable
    public Screen screen;
    @Shadow
    @Final
    public TextureManager textureManager;
    @Shadow
    protected int missTime;
    @Shadow
    @Nullable
    private CrashReport delayedCrash;
    @Shadow
    @Nullable
    private IProfileResult fpsPieResults;
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
    private Framebuffer mainRenderTarget;
    private boolean multiplayerPause;
    @Shadow
    @Final
    private MusicTicker musicManager;
    @Shadow
    private boolean pause;
    @Shadow
    private float pausePartialTick;
    @Shadow
    @Nullable
    private NetworkManager pendingConnection;
    @Shadow
    @Nullable
    private CompletableFuture<Void> pendingReload;
    @Shadow
    private IProfiler profiler;
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
    @Final
    private Snooper snooper;
    @Shadow
    @Nullable
    private TutorialToast socialInteractionsToast;
    @Shadow
    @Final
    private SoundHandler soundManager;
    @Shadow
    @Final
    private Timer timer;
    @Shadow
    @Final
    private ToastGui toast;
    @Shadow
    @Final
    private Tutorial tutorial;
    @Shadow
    @Final
    private MainWindow window;

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
            if (leftClick && this.hitResult != null && this.hitResult.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) this.hitResult;
                BlockPos hitPos = blockRayTrace.getBlockPos();
                if (!this.level.isEmptyBlock(hitPos)) {
                    InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(0, this.options.keyAttack, Hand.MAIN_HAND);
                    if (inputEvent.isCanceled()) {
                        if (inputEvent.shouldSwingHand()) {
                            this.particleEngine.addBlockHitEffects(hitPos, blockRayTrace);
                            this.player.swing(Hand.MAIN_HAND);
                            ClientEvents.getInstance().swingArm(Hand.MAIN_HAND);
                        }
                        return;
                    }
                    Direction face = blockRayTrace.getDirection();
                    if (this.gameMode.continueDestroyBlock(hitPos, face)) {
                        if (inputEvent.shouldSwingHand()) {
                            this.particleEngine.addBlockHitEffects(hitPos, blockRayTrace);
                            this.player.swing(Hand.MAIN_HAND);
                            ClientEvents.getInstance().swingArm(Hand.MAIN_HAND);
                        }
                    }
                }
            }
            else {
                this.gameMode.stopDestroyBlock();
            }
        }
    }

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
                if (reserve != null) {
                    reserve = null;
                }
                this.levelRenderer.clear();
            }
            catch (Throwable ignored) {
            }
            try {
                System.gc();
                if (this.isLocalServer && this.singleplayerServer != null) {
                    this.singleplayerServer.halt(true);
                }
                this.clearLevel(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
            }
            catch (Throwable ignored) {
            }
            if (this.getConnection() != null) {
                this.getConnection().getConnection().disconnect(new StringTextComponent("Client crashed"));
            }
            this.gameRenderer.shutdownEffect();
            try {
                reserve = new byte[10_485_760];
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
    protected abstract void finishProfilers(boolean p_238210_1_, @Nullable LongTickDetector p_238210_2_);

    @Shadow
    @Nullable
    public abstract Entity getCameraEntity();

    @Shadow
    @Nullable
    public abstract ClientPlayNetHandler getConnection();

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
            PointOfView view = this.options.getCameraType();
            this.options.setCameraType(view.cycle());
            if (view.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
        }
        while (this.options.keySmoothCamera.consumeClick()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }
        for (int slot = 0; slot < 9; slot++) {
            boolean isSaveToolbarDown = this.options.keySaveHotbarActivator.isDown();
            boolean isLoadToolbarDown = this.options.keyLoadHotbarActivator.isDown();
            if (this.options.keyHotbarSlots[slot].consumeClick()) {
                if (!this.multiplayerPause) {
                    if (this.player.isSpectator()) {
                        this.gui.getSpectatorGui().onHotbarSelected(slot);
                    }
                    else if (!this.player.isCreative() || this.screen != null || !isLoadToolbarDown && !isSaveToolbarDown) {
                        this.player.inventory.selected = slot;
                    }
                    else {
                        CreativeScreen.handleHotbarLoadOrSave((Minecraft) (Object) this, slot, isLoadToolbarDown, isSaveToolbarDown);
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
            if (!this.multiplayerPause) {
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
            if (!this.multiplayerPause) {
                if (!this.player.isSpectator()) {
                    //noinspection ObjectAllocationInLoop
                    this.getConnection()
                        .send(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                }
            }
        }
        while (this.options.keyDrop.consumeClick()) {
            if (!this.multiplayerPause) {
                if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
                    this.player.swing(Hand.MAIN_HAND);
                }
            }
        }
        boolean isChatVisible = this.options.chatVisibility != ChatVisibility.HIDDEN;
        if (isChatVisible) {
            while (this.options.keyChat.consumeClick()) {
                this.openChatScreen("");
            }
            if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
                this.openChatScreen("/");
            }
        }
        Item offhandItem = this.player.getOffhandItem().getItem();
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                if (!this.multiplayerPause) {
                    this.gameMode.releaseUsingItem(this.player);
                }
            }
            while (this.options.keyAttack.consumeClick()) {
                //TODO shield bash
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
                                        ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(this.player.getId(),
                                                                                new LungeChargeInfo(Hand.MAIN_HAND,
                                                                                                    this.player.getMainHandItem(),
                                                                                                    lungeFullTime - lungeMinTime));
                                    }
                                    else {
                                        lunge.addInfo(Hand.MAIN_HAND, this.player.getMainHandItem(), lungeFullTime - lungeMinTime);
                                    }
                                    EvolutionNetwork.INSTANCE.sendToServer(new PacketCSStartLunge(Hand.MAIN_HAND, lungeFullTime - lungeMinTime));
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
            if (offhandItem instanceof ILunge && this.mouseHandler.isMouseGrabbed()) {
                int lungeFullTime = ((ILunge) offhandItem).getFullLungeTime();
                int lungeMinTime = ((ILunge) offhandItem).getMinLungeTime();
                if (this.options.keyUse.isDown()) {
                    if (!this.multiplayerPause) {
                        if (ClientEvents.getInstance().getOffhandCooledAttackStrength(offhandItem, 0.0f) >= 1.0f && InputHooks.useKeyReleased) {
                            InputHooks.useKeyDownTicks++;
                            if (InputHooks.useKeyDownTicks >= lungeMinTime) {
                                if (!InputHooks.isOffhandLungeInProgress) {
                                    InputHooks.isOffhandLungeInProgress = true;
                                    LungeChargeInfo lunge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(this.player.getId());
                                    if (lunge == null) {
                                        ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(this.player.getId(),
                                                                                new LungeChargeInfo(Hand.OFF_HAND,
                                                                                                    this.player.getOffhandItem(),
                                                                                                    lungeFullTime - lungeMinTime));
                                    }
                                    else {
                                        lunge.addInfo(Hand.OFF_HAND, this.player.getOffhandItem(), lungeFullTime - lungeMinTime);
                                    }
                                    EvolutionNetwork.INSTANCE.sendToServer(new PacketCSStartLunge(Hand.OFF_HAND, lungeFullTime - lungeMinTime));
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
        if (!(offhandItem instanceof IOffhandAttackable) && this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
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
        if (this.hitResult != null && this.hitResult.getType() != RayTraceResult.Type.MISS) {
            if (!ForgeHooksClient.onClickInput(2, this.options.keyPickItem, Hand.MAIN_HAND).isCanceled()) {
                ForgeHooks.onPickBlock(this.hitResult, this.player, this.level);
            }
        }
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Shadow
    @Deprecated
    public abstract CompletableFuture<Void> reloadResourcePacks();

    @Shadow
    protected abstract void renderFpsMeter(MatrixStack p_238183_1_, IProfileResult p_238183_2_);

    /**
     * @author MGSchultz
     * <p>
     * Replaces Minecraft's run method to be able to catch more exceptions.
     */
    @Overwrite
    public void run() {
        this.gameThread = Thread.currentThread();
        try {
            boolean hasAlreadyBeenOutOfMemory = false;
            while (this.running) {
                if (this.delayedCrash != null) {
                    crash(this.delayedCrash);
                    return;
                }
                try {
                    LongTickDetector longTickDetector = LongTickDetector.createTickProfiler("Renderer");
                    boolean debugMode = this.shouldRenderFpsPie();
                    this.startProfilers(debugMode, longTickDetector);
                    this.profiler.startTick();
                    this.runTick(true);
                    this.profiler.endTick();
                    this.finishProfilers(debugMode, longTickDetector);
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
        if (this.pendingReload != null && !(this.overlay instanceof ResourceLoadProgressGui)) {
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
        RenderSystem.pushMatrix();
        RenderSystem.clear(16_640, ON_OSX);
        this.mainRenderTarget.bindWrite(true);
        FogRenderer.setupNoFog();
        this.profiler.push("display");
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        this.profiler.pop();
        if (!this.noRender) {
            BasicEventHooks.onRenderTickStart(this.pause ? this.pausePartialTick : this.timer.partialTick);
            this.profiler.popPush("gameRenderer");
            this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, i, shouldRender);
            this.profiler.popPush("toasts");
            this.toast.render(new MatrixStack());
            this.profiler.pop();
            BasicEventHooks.onRenderTickEnd(this.pause ? this.pausePartialTick : this.timer.partialTick);
        }
        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            this.renderFpsMeter(new MatrixStack(), this.fpsPieResults);
            this.profiler.pop();
        }
        this.profiler.push("blit");
        this.mainRenderTarget.unbindWrite();
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
        RenderSystem.popMatrix();
        this.profiler.popPush("updateDisplay");
        this.window.updateDisplay();
        int i1 = this.getFramerateLimit();
        if (i1 < AbstractOption.FRAMERATE_LIMIT.getMaxValue()) {
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
            this.fpsString = String.format("%d fps T: %s%s%s%s B: %d",
                                           fps,
                                           this.options.framerateLimit == AbstractOption.FRAMERATE_LIMIT.getMaxValue() ?
                                           "inf" :
                                           this.options.framerateLimit,
                                           this.options.enableVsync ? " vsync" : "",
                                           this.options.graphicsMode.toString(),
                                           this.options.renderClouds == CloudOption.OFF ?
                                           "" :
                                           this.options.renderClouds == CloudOption.FAST ? " fast-clouds" : " fancy-clouds",
                                           this.options.biomeBlendRadius);
            this.lastTime += 1_000L;
            this.frames = 0;
            this.snooper.prepare();
            if (!this.snooper.isStarted()) {
                this.snooper.start();
            }
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
                InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(0, this.options.keyAttack, Hand.MAIN_HAND);
                if (!inputEvent.isCanceled()) {
                    switch (this.hitResult.getType()) {
                        case ENTITY: {
                            if (this.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                                ClientEvents.getInstance().leftMouseClick();
                            }
                            else {
                                this.gameMode.attack(this.player, this.crosshairPickEntity);
                            }
                            break;
                        }
                        case BLOCK: {
                            BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) this.hitResult;
                            BlockPos hitPos = blockRayTrace.getBlockPos();
                            if (!this.level.isEmptyBlock(hitPos)) {
                                this.gameMode.startDestroyBlock(hitPos, blockRayTrace.getDirection());
                                break;
                            }
                        }
                        case MISS: {
                            if (this.gameMode.hasMissTime()) {
                                this.missTime = 10;
                            }
                            ClientEvents.getInstance().leftMouseClick();
                            ForgeHooks.onEmptyLeftClick(this.player);
                            break;
                        }
                    }
                }
                if (inputEvent.shouldSwingHand()) {
                    ClientEvents.getInstance().swingArm(Hand.MAIN_HAND);
                    this.player.swing(Hand.MAIN_HAND);
                }
            }
        }
    }

    @Shadow
    protected abstract void startProfilers(boolean p_238201_1_, @Nullable LongTickDetector p_238201_2_);

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
                for (Hand hand : Hand.values()) {
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
                        case ENTITY: {
                            EntityRayTraceResult entityRayTrace = (EntityRayTraceResult) this.hitResult;
                            Entity entity = entityRayTrace.getEntity();
                            ActionResultType actionResult = this.gameMode.interactAt(this.player, entity, entityRayTrace, hand);
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
                            break;
                        }
                        case BLOCK: {
                            BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) this.hitResult;
                            int count = stack.getCount();
                            ActionResultType actResult = this.gameMode.useItemOn(this.player, this.level, hand, blockRayTrace);
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
                            if (actResult == ActionResultType.FAIL) {
                                return;
                            }
                        }
                    }
                }
                ItemStack stackOffhand = this.player.getOffhandItem();
                Item itemOffhand = stackOffhand.getItem();
                if (itemOffhand instanceof IOffhandAttackable) {
                    ClientEvents.getInstance().rightMouseClick((IOffhandAttackable) itemOffhand);
                    return;
                }
                boolean isLungingMainhand = InputHooks.isMainhandLungeInProgress || InputHooks.isMainhandLunging;
                boolean isOffhandShield = this.player.getOffhandItem().isShield(this.player);
                for (Hand hand : MathHelper.HANDS_LEFT_PRIORITY) {
                    ItemStack stack = this.player.getItemInHand(hand);
                    if (stack.isEmpty() && this.hitResult.getType() == RayTraceResult.Type.MISS) {
                        ForgeHooks.onEmptyClick(this.player, hand);
                    }
                    if (hand == Hand.MAIN_HAND && (isLungingMainhand || ClientEvents.getInstance().getMainhandCooledAttackStrength(0.0f) < 1.0f)) {
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
                        ActionResultType actionResult = this.gameMode.useItem(this.player, this.level, hand);
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
        BasicEventHooks.onPreClientTick();
        this.profiler.popPush("gui");
        if (!this.pause) {
            this.gui.tick();
        }
        this.profiler.pop();
        this.gameRenderer.pick(1.0F);
        this.tutorial.onLookAt(this.level, this.hitResult);
        this.profiler.push("gameMode");
        if ((!this.pause || this.pause && this.multiplayerPause) && this.level != null) { //Added check for multiplayer pause
            this.gameMode.tick();
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
                this.setScreen(new SleepInMultiplayerScreen());
            }
        }
        else if (this.screen != null && this.screen instanceof SleepInMultiplayerScreen && !this.player.isSleeping()) {
            this.setScreen(null);
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
                    ITextComponent itextcomponent = new TranslationTextComponent("tutorial.socialInteractions.title");
                    ITextComponent itextcomponent1 = new TranslationTextComponent("tutorial.socialInteractions.description",
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
                this.level.animateTick(MathHelper.floor(this.player.getX()),
                                       MathHelper.floor(this.player.getY()),
                                       MathHelper.floor(this.player.getZ()));
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
        BasicEventHooks.onPostClientTick();
        this.profiler.pop();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V", ordinal = 0))
    private void tickProxy(GameRenderer gameRenderer, float partialTicks) {
        //Do nothing. getMouseOver is called twice every frame for the tutorial, which is not needed.
    }
}
