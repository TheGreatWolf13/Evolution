package tgw.evolution.mixin;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.social.SocialInteractionsScreen;
import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.LongTickDetector;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends RecursiveEventLoop<Runnable> {

    @Shadow
    public static byte[] reserve;
    @Shadow
    @Final
    private static ITextComponent SOCIAL_INTERACTIONS_NOT_AVAILABLE;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Nullable
    public Entity crosshairPickEntity;
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
    @Nullable
    public ClientWorld level;
    @Shadow
    @Final
    public WorldRenderer levelRenderer;
    @Shadow
    @Final
    public MouseHelper mouseHandler;
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
    protected int missTime;
    @Shadow
    @Nullable
    private CrashReport delayedCrash;
    @Shadow
    private Thread gameThread;
    @Shadow
    private boolean isLocalServer;
    @Shadow
    private IProfiler profiler;
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
    private Tutorial tutorial;

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
            if (this.gameMode.isServerControlledInventory()) {
                this.player.sendOpenInventory();
            }
            else {
                this.tutorial.onOpenInventory();
                //noinspection ObjectAllocationInLoop
                this.setScreen(new InventoryScreen(this.player));
            }
        }
        while (this.options.keyAdvancements.consumeClick()) {
            //noinspection ObjectAllocationInLoop
            this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
        }
        while (this.options.keySwapOffhand.consumeClick()) {
            if (!this.player.isSpectator()) {
                //noinspection ObjectAllocationInLoop
                this.getConnection()
                    .send(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }
        }
        while (this.options.keyDrop.consumeClick()) {
            if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
                this.player.swing(Hand.MAIN_HAND);
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
                this.gameMode.releaseUsingItem(this.player);
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
                else {
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
            else {
                InputHooks.isMainhandLungeInProgress = false;
                InputHooks.releaseAttack(this.options.keyAttack);
                InputHooks.attackKeyReleased = true;
                while (this.options.keyAttack.consumeClick()) {
                    this.startAttack();
                }
            }
            if (offhandItem instanceof ILunge && this.mouseHandler.isMouseGrabbed()) {
                int lungeFullTime = ((ILunge) offhandItem).getFullLungeTime();
                int lungeMinTime = ((ILunge) offhandItem).getMinLungeTime();
                if (this.options.keyUse.isDown()) {
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
                else {
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
            else {
                InputHooks.isOffhandLungeInProgress = false;
                InputHooks.useKeyDownTicks = 0;
                InputHooks.useKeyReleased = true;
                while (this.options.keyUse.consumeClick()) {
                    this.startUseItem();
                }
            }
            while (this.options.keyPickItem.consumeClick()) {
                this.pickBlock();
            }
        }
        if (!(offhandItem instanceof IOffhandAttackable) && this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
            this.startUseItem();
        }
        this.continueAttack(this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
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

    @Shadow
    protected abstract void runTick(boolean p_195542_1_);

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isPublished()Z"))
    private boolean runTickProxy(IntegratedServer integratedServer) {
        return false;
    }

    @Shadow
    public abstract void setScreen(@Nullable Screen p_147108_1_);

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

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V", ordinal = 0))
    private void tickProxy(GameRenderer gameRenderer, float partialTicks) {
        //Do nothing. getMouseOver is called twice every frame for the tutorial, which is not needed.
    }
}
