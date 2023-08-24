package tgw.evolution.mixin;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.util.Action;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;

import java.util.Locale;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {

    @Shadow private long debugCrashKeyReportedCount;
    @Shadow private long debugCrashKeyReportedTime;
    @Shadow private long debugCrashKeyTime;
    @Shadow private boolean handledDebugKey;
    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean sendRepeatsToGui;

    @Overwrite
    private void charTyped(long windowPointer, int codePoint, @Modifiers int mod) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            Screen screen = this.minecraft.screen;
            if (screen != null && this.minecraft.getOverlay() == null) {
                if (Character.charCount(codePoint) == 1) {
                    try {
                        screen.charTyped((char) codePoint, mod);
                    }
                    catch (Throwable t) {
                        CrashReport crashReport = CrashReport.forThrowable(t, "charTyped event handler");
                        CrashReportCategory category = crashReport.addCategory("Affected screen");
                        category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
                    }
                }
                else {
                    for (char c0 : Character.toChars(codePoint)) {
                        try {
                            screen.charTyped(c0, mod);
                        }
                        catch (Throwable t) {
                            CrashReport crashReport = CrashReport.forThrowable(t, "charTyped event handler");
                            CrashReportCategory category = crashReport.addCategory("Affected screen");
                            //noinspection ObjectAllocationInLoop
                            category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                            throw new ReportedException(crashReport);
                        }
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract void copyRecreateCommand(boolean pPrivileged, boolean pAskServer);

    @Shadow
    protected abstract void debugFeedbackComponent(Component p_167823_);

    @Shadow
    protected abstract void debugFeedbackTranslated(String pMessage, Object... pArgs);

    @Overwrite
    private boolean handleDebugKeys(@Key int key) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return true;
        }
        return switch (key) {
            case GLFW.GLFW_KEY_A -> {
                this.minecraft.lvlRenderer().allChanged();
                this.debugFeedbackTranslated("debug.reload_chunks.message");
                yield true;
            }
            case GLFW.GLFW_KEY_B -> {
                boolean flag = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(flag);
                this.debugFeedbackTranslated(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                yield true;
            }
            case GLFW.GLFW_KEY_C -> {
                assert this.minecraft.player != null;
                if (this.minecraft.player.isReducedDebugInfo()) {
                    yield false;
                }
                this.debugFeedbackTranslated("debug.copy_location.message");
                this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.minecraft.player.level.dimension().location(), this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), this.minecraft.player.getYRot(), this.minecraft.player.getXRot()));
                yield true;
            }
            case GLFW.GLFW_KEY_D -> {
                this.minecraft.gui.getChat().clearMessages(false);
                yield true;
            }
            case GLFW.GLFW_KEY_F -> {
                Option.RENDER_DISTANCE.set(this.minecraft.options, Mth.clamp(this.minecraft.options.renderDistance + (Screen.hasShiftDown() ? -1 : 1), Option.RENDER_DISTANCE.getMinValue(), Option.RENDER_DISTANCE.getMaxValue()));
                this.debugFeedbackTranslated("debug.cycle_renderdistance.message", this.minecraft.options.renderDistance);
                yield true;
            }
            case GLFW.GLFW_KEY_G -> {
                this.debugFeedbackTranslated(this.minecraft.debugRenderer.switchRenderChunkborder() ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                yield true;
            }
            case GLFW.GLFW_KEY_H -> {
                this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
                this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                this.minecraft.options.save();
                yield true;
            }
            case GLFW.GLFW_KEY_I -> {
                assert this.minecraft.player != null;
                if (!this.minecraft.player.isReducedDebugInfo()) {
                    this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
                }
                yield true;
            }
            case GLFW.GLFW_KEY_L -> {
                if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
                    this.debugFeedbackTranslated("debug.profiling.start", 10);
                }
                yield true;
            }
            case GLFW.GLFW_KEY_N -> {
                assert this.minecraft.player != null;
                if (!this.minecraft.player.hasPermissions(2)) {
                    this.debugFeedbackTranslated("debug.creative_spectator.error");
                }
                else if (!this.minecraft.player.isSpectator()) {
                    this.minecraft.player.chat("/gamemode spectator");
                }
                else {
                    assert this.minecraft.gameMode != null;
                    this.minecraft.player.chat("/gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName());
                }
                yield true;
            }
            case GLFW.GLFW_KEY_P -> {
                this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
                this.minecraft.options.save();
                this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                yield true;
            }
            case GLFW.GLFW_KEY_Q -> {
                this.debugFeedbackTranslated("debug.help.message");
                ChatComponent chat = this.minecraft.gui.getChat();
                chat.addMessage(new TranslatableComponent("debug.reload_chunks.help"));
                chat.addMessage(new TranslatableComponent("debug.show_hitboxes.help"));
                chat.addMessage(new TranslatableComponent("debug.copy_location.help"));
                chat.addMessage(new TranslatableComponent("debug.clear_chat.help"));
                chat.addMessage(new TranslatableComponent("debug.cycle_renderdistance.help"));
                chat.addMessage(new TranslatableComponent("debug.chunk_boundaries.help"));
                chat.addMessage(new TranslatableComponent("debug.advanced_tooltips.help"));
                chat.addMessage(new TranslatableComponent("debug.inspect.help"));
                chat.addMessage(new TranslatableComponent("debug.profiling.help"));
                chat.addMessage(new TranslatableComponent("debug.creative_spectator.help"));
                chat.addMessage(new TranslatableComponent("debug.pause_focus.help"));
                chat.addMessage(new TranslatableComponent("debug.help.help"));
                chat.addMessage(new TranslatableComponent("debug.reload_resourcepacks.help"));
                chat.addMessage(new TranslatableComponent("debug.pause.help"));
                chat.addMessage(new TranslatableComponent("debug.gamemodes.help"));
                yield true;
            }
            case GLFW.GLFW_KEY_T -> {
                this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
                this.minecraft.reloadResourcePacks();
                yield true;
            }
            case GLFW.GLFW_KEY_F4 -> {
                assert this.minecraft.player != null;
                if (!this.minecraft.player.hasPermissions(2)) {
                    this.debugFeedbackTranslated("debug.gamemodes.error");
                }
                else {
                    this.minecraft.setScreen(new GameModeSwitcherScreen());
                }
                yield true;
            }
            default -> false;
        };
    }

    @Overwrite
    public void keyPress(long windowPointer, @Key int key, int scanCode, @Action int action, @Modifiers int mod) {
        long window = this.minecraft.getWindow().getWindow();
        if (windowPointer == window) {
            if (this.debugCrashKeyTime > 0L) {
                if (!InputConstants.isKeyDown(window, GLFW.GLFW_KEY_C) || !InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3)) {
                    this.debugCrashKeyTime = -1L;
                }
            }
            else if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_C) && InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3)) {
                this.handledDebugKey = true;
                this.debugCrashKeyTime = Util.getMillis();
                this.debugCrashKeyReportedTime = Util.getMillis();
                this.debugCrashKeyReportedCount = 0L;
            }
            Screen screen = this.minecraft.screen;
            if (!(screen instanceof KeyBindsScreen) || ((KeyBindsScreen) screen).lastKeySelection <= Util.getMillis() - 20L) {
                if (action == GLFW.GLFW_PRESS) {
                    if (this.minecraft.options.keyFullscreen.matches(key, scanCode)) {
                        this.minecraft.getWindow().toggleFullScreen();
                        this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
                        this.minecraft.options.save();
                        return;
                    }
                    if (this.minecraft.options.keyScreenshot.matches(key, scanCode)) {
                        Screenshot.grab(this.minecraft.gameDirectory, this.minecraft.getMainRenderTarget(),
                                        comp -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(comp)));
                        return;
                    }
                }
                else if (action == GLFW.GLFW_RELEASE && screen instanceof KeyBindsScreen) {
                    ((KeyBindsScreen) screen).selectedKey = null;
                }
            }
            if (screen != null) {
                boolean shouldReturn = false;
                try {
                    if (action != GLFW.GLFW_PRESS && (action != GLFW.GLFW_REPEAT || !this.sendRepeatsToGui)) {
                        if (action == GLFW.GLFW_RELEASE) {
                            shouldReturn = screen.keyReleased(key, scanCode, mod);
                        }
                    }
                    else {
                        screen.afterKeyboardAction();
                        shouldReturn = screen.keyPressed(key, scanCode, mod);
                    }
                }
                catch (Throwable t) {
                    CrashReport crashReport = CrashReport.forThrowable(t, "keyPressed event handler");
                    CrashReportCategory category = crashReport.addCategory("Affected screen");
                    category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                    throw new ReportedException(crashReport);
                }
                if (shouldReturn) {
                    return;
                }
            }
            if (screen == null || screen.passEvents) {
                InputConstants.Key input = InputConstants.getKey(key, scanCode);
                if (action == GLFW.GLFW_RELEASE) {
                    KeyMapping.set(input, false);
                    if (key == GLFW.GLFW_KEY_F3) {
                        if (this.handledDebugKey) {
                            this.handledDebugKey = false;
                        }
                        else {
                            this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                            this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
                            this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();
                        }
                    }
                }
                else {
                    boolean handleDebug = false;
                    if (screen == null) {
                        boolean isF3Down = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3);
                        if (key == GLFW.GLFW_KEY_ESCAPE) {
                            this.minecraft.pauseGame(isF3Down);
                        }
                        handleDebug = isF3Down && this.handleDebugKeys(key);
                        this.handledDebugKey |= handleDebug;
                        if (key == GLFW.GLFW_KEY_F1) {
                            this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                        }
                    }
                    if (handleDebug) {
                        KeyMapping.set(input, false);
                    }
                    else {
                        KeyMapping.set(input, true);
                        KeyMapping.click(input);
                    }
                    if (this.minecraft.options.renderDebugCharts && key >= 48 && key <= 57) {
                        this.minecraft.debugFpsMeterKeyPress(key - 48);
                    }
                }
            }
        }
    }

    @Shadow
    public abstract void setClipboard(String pString);
}
