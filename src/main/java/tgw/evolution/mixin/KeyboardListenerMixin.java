package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.util.Action;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardListenerMixin {

    @Shadow
    private long debugCrashKeyReportedCount;
    @Shadow
    private long debugCrashKeyReportedTime;
    @Shadow
    private long debugCrashKeyTime;
    @Shadow
    private boolean handledDebugKey;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private boolean sendRepeatsToGui;

    /**
     * @author TheGreatWolf
     * @reason Avoid Allocations
     */
    @Overwrite
    private void charTyped(long windowPointer, int codePoint, @Modifiers int mod) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            Screen screen = this.minecraft.screen;
            if (screen != null && this.minecraft.getOverlay() == null) {
                if (Character.charCount(codePoint) == 1) {
                    outer:
                    try {
                        if (ForgeHooksClient.onScreenCharTypedPre(screen, (char) codePoint, mod)) {
                            break outer;
                        }
                        if (screen.charTyped((char) codePoint, mod)) {
                            break outer;
                        }
                        ForgeHooksClient.onScreenCharTypedPost(screen, (char) codePoint, mod);
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
                        outer:
                        try {
                            if (ForgeHooksClient.onScreenCharTypedPre(screen, c0, mod)) {
                                break outer;
                            }
                            if (screen.charTyped(c0, mod)) {
                                break outer;
                            }
                            ForgeHooksClient.onScreenCharTypedPost(screen, c0, mod);
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
    protected abstract boolean handleDebugKeys(int pKey);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void keyPress(long windowPointer, @Key int key, int scanCode, @Action int action, @Modifiers int mod) {
        if (windowPointer == this.minecraft.getWindow().getWindow()) {
            if (this.debugCrashKeyTime > 0L) {
                if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_C) ||
                    !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3)) {
                    this.debugCrashKeyTime = -1L;
                }
            }
            else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_C) &&
                     InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3)) {
                this.handledDebugKey = true;
                this.debugCrashKeyTime = Util.getMillis();
                this.debugCrashKeyReportedTime = Util.getMillis();
                this.debugCrashKeyReportedCount = 0L;
            }
            Screen screen = this.minecraft.screen;
            if (!(this.minecraft.screen instanceof KeyBindsScreen) || ((KeyBindsScreen) screen).lastKeySelection <= Util.getMillis() - 20L) {
                if (action == GLFW.GLFW_PRESS) {
                    if (this.minecraft.options.keyFullscreen.matches(key, scanCode)) {
                        this.minecraft.getWindow().toggleFullScreen();
                        this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
                        this.minecraft.options.save();
                        return;
                    }
                    if (this.minecraft.options.keyScreenshot.matches(key, scanCode)) {
//                        if (Screen.hasControlDown()) {
//                        }
                        Screenshot.grab(this.minecraft.gameDirectory, this.minecraft.getMainRenderTarget(),
                                        comp -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(comp)));
                        return;
                    }
                }
                else if (action == GLFW.GLFW_RELEASE && this.minecraft.screen instanceof KeyBindsScreen) {
                    ((KeyBindsScreen) this.minecraft.screen).selectedKey = null; //Forge: Unset pure modifiers.
                }
            }
            if (NarratorChatListener.INSTANCE.isActive()) {
                boolean flag = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox) screen.getFocused()).canConsumeInput();
                if (action != GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_B && Screen.hasControlDown() && flag) {
                    boolean flag1 = this.minecraft.options.narratorStatus == NarratorStatus.OFF;
                    this.minecraft.options.narratorStatus = NarratorStatus.byId(this.minecraft.options.narratorStatus.getId() + 1);
                    NarratorChatListener.INSTANCE.updateNarratorStatus(this.minecraft.options.narratorStatus);
                    if (screen instanceof SimpleOptionsSubScreen) {
                        ((SimpleOptionsSubScreen) screen).updateNarratorButton();
                    }
                    if (flag1 && screen != null) {
                        screen.narrationEnabled();
                    }
                }
            }
            if (screen != null) {
                boolean shouldReturn = false;
                try {
                    if (action != GLFW.GLFW_PRESS && (action != GLFW.GLFW_REPEAT || !this.sendRepeatsToGui)) {
                        if (action == GLFW.GLFW_RELEASE) {
                            shouldReturn = ForgeHooksClient.onScreenKeyReleasedPre(screen, key, scanCode, mod);
                            if (!shouldReturn) {
                                shouldReturn = screen.keyReleased(key, scanCode, mod);
                            }
                            if (!shouldReturn) {
                                shouldReturn = ForgeHooksClient.onScreenKeyReleasedPost(screen, key, scanCode, mod);
                            }
                        }
                    }
                    else {
                        screen.afterKeyboardAction();
                        shouldReturn = ForgeHooksClient.onScreenKeyPressedPre(screen, key, scanCode, mod);
                        if (!shouldReturn) {
                            shouldReturn = screen.keyPressed(key, scanCode, mod);
                        }
                        if (!shouldReturn) {
                            shouldReturn = ForgeHooksClient.onScreenKeyPressedPost(screen, key, scanCode, mod);
                        }
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
            if (this.minecraft.screen == null || this.minecraft.screen.passEvents) {
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
                    if (key == GLFW.GLFW_KEY_F4 && this.minecraft.gameRenderer != null) {
                        this.minecraft.gameRenderer.togglePostEffect();
                    }
                    boolean flag3 = false;
                    if (this.minecraft.screen == null) {
                        if (key == GLFW.GLFW_KEY_ESCAPE) {
                            boolean isF3Down = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3);
                            this.minecraft.pauseGame(isF3Down);
                        }
                        flag3 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3) &&
                                this.handleDebugKeys(key);
                        this.handledDebugKey |= flag3;
                        if (key == GLFW.GLFW_KEY_F1) {
                            this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                        }
                    }
                    if (flag3) {
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
            ForgeHooksClient.fireKeyInput(key, scanCode, action, mod);
        }
    }

    @Redirect(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;togglePostEffect()V"))
    private void onKeyPress(GameRenderer gameRenderer) {
        //Do nothing. Disables the ability to remove shaders by pressing F4.
    }
}
