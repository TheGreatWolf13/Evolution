package tgw.evolution.mixin;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.util.Action;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.events.ClientEvents;

import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    @Shadow private double accumulatedScroll;
    @Shadow private int activeButton;
    @Shadow private int clickDepth;
    @Shadow private int fakeRightMouse;
    @Shadow private boolean ignoreFirstMove;
    @Shadow private boolean isLeftPressed;
    @Shadow private boolean isMiddlePressed;
    @Shadow private boolean isRightPressed;
    @Shadow private @Final Minecraft minecraft;
    @Shadow private boolean mouseGrabbed;
    @Shadow private double mousePressedTime;
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Shadow
    public abstract void grabMouse();

    @Shadow
    public abstract boolean isMouseGrabbed();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private void onMove(long windowPointer, double xPos, double yPos) {
        Window window = this.minecraft.getWindow();
        if (windowPointer != window.getWindow()) {
            return;
        }
        if (this.ignoreFirstMove) {
            this.xpos = xPos;
            this.ypos = yPos;
            this.ignoreFirstMove = false;
        }
        Screen screen = this.minecraft.screen;
        if (screen != null && this.minecraft.getOverlay() == null) {
            double x = xPos * window.getGuiScaledWidth() / window.getScreenWidth();
            double y = yPos * window.getGuiScaledHeight() / window.getScreenHeight();
            try {
                screen.mouseMoved(x, y);
            }
            catch (Throwable t) {
                CrashReport crash = CrashReport.forThrowable(t, "mouseMoved event handler");
                CrashReportCategory category = crash.addCategory("Affected screen");
                category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                throw new ReportedException(crash);
            }
            if (this.activeButton != -1 && this.mousePressedTime > 0) {
                double dx = (xPos - this.xpos) * window.getGuiScaledWidth() / window.getScreenWidth();
                double dz = (yPos - this.ypos) * window.getGuiScaledHeight() / window.getScreenHeight();
                ClientEvents client = ClientEvents.getInstanceNullable();
                outer:
                try {
                    if (client != null) {
                        client.onGUIMouseDragPre(x, y, this.activeButton);
                    }
                    if (screen.mouseDragged(x, y, this.activeButton, dx, dz)) {
                        break outer;
                    }
                }
                catch (Throwable t) {
                    CrashReport crash = CrashReport.forThrowable(t, "mouseDragged event handler");
                    CrashReportCategory category = crash.addCategory("Affected screen");
                    category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                    throw new ReportedException(crash);
                }
            }
            screen.afterMouseMove();
        }
        this.minecraft.getProfiler().push("mouse");
        if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
            this.accumulatedDX += xPos - this.xpos;
            this.accumulatedDY += yPos - this.ypos;
        }
        this.turnPlayer();
        this.xpos = xPos;
        this.ypos = yPos;
        this.minecraft.getProfiler().pop();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private void onPress(long windowPointer, @MouseButton int button, @Action int action, @Modifiers int mod) {
        Window window = this.minecraft.getWindow();
        if (windowPointer != window.getWindow()) {
            return;
        }
        boolean press = action == GLFW.GLFW_PRESS;
        if (Minecraft.ON_OSX && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (press) {
                if ((mod & GLFW_MOD_CONTROL) != 0) {
                    button = GLFW.GLFW_MOUSE_BUTTON_2;
                    ++this.fakeRightMouse;
                }
            }
            else if (this.fakeRightMouse > 0) {
                button = GLFW.GLFW_MOUSE_BUTTON_2;
                --this.fakeRightMouse;
            }
        }
        if (press) {
            if (this.minecraft.options.touchscreen && this.clickDepth++ > 0) {
                return;
            }
            this.activeButton = button;
            this.mousePressedTime = Blaze3D.getTime();
        }
        else if (this.activeButton != -1) {
            if (this.minecraft.options.touchscreen && --this.clickDepth > 0) {
                return;
            }
            this.activeButton = -1;
        }
        boolean shouldReturn = false;
        if (this.minecraft.getOverlay() == null) {
            if (this.minecraft.screen == null) {
                if (!this.mouseGrabbed && press) {
                    this.grabMouse();
                }
            }
            else {
                double dx = this.xpos * window.getGuiScaledWidth() / window.getScreenWidth();
                double dy = this.ypos * window.getGuiScaledHeight() / window.getScreenHeight();
                Screen screen = this.minecraft.screen;
                ClientEvents client = ClientEvents.getInstanceNullable();
                if (press) {
                    screen.afterMouseAction();
                    try {
                        if (client != null) {
                            shouldReturn = client.onGUIMouseClickedPre(dx, dy, button);
                        }
                        if (!shouldReturn) {
                            shouldReturn = this.minecraft.screen.mouseClicked(dx, dy, button);
                        }
                    }
                    catch (Throwable t) {
                        CrashReport crash = CrashReport.forThrowable(t, "mouseClicked event handler");
                        CrashReportCategory category = crash.addCategory("Affected screen");
                        category.setDetail("Screen name", screen.getClass().getCanonicalName());
                        throw new ReportedException(crash);
                    }
                }
                else {
                    try {
                        if (client != null) {
                            shouldReturn = client.onGUIMouseReleasedPre(button);
                        }
                        if (!shouldReturn) {
                            shouldReturn = this.minecraft.screen.mouseReleased(dx, dy, button);
                        }
                    }
                    catch (Throwable t) {
                        CrashReport crashreport = CrashReport.forThrowable(t, "mouseReleased event handler");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Affected screen");
                        crashreportcategory.setDetail("Screen name", screen.getClass().getCanonicalName());
                        throw new ReportedException(crashreport);
                    }
                }
            }
        }
        if (!shouldReturn && (this.minecraft.screen == null || this.minecraft.screen.passEvents) && this.minecraft.getOverlay() == null) {
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_1 -> this.isLeftPressed = press;
                case GLFW.GLFW_MOUSE_BUTTON_2 -> this.isRightPressed = press;
                case GLFW.GLFW_MOUSE_BUTTON_3 -> this.isMiddlePressed = press;
            }
            KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(button), press);
            if (press) {
                assert this.minecraft.player != null;
                if (this.minecraft.player.isSpectator() && button == GLFW.GLFW_MOUSE_BUTTON_3) {
                    this.minecraft.gui.getSpectatorGui().onMouseMiddleClick();
                }
                else {
                    KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(button));
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private void onScroll(long windowPointer, double xOffset, double yOffset) {
        Window window = this.minecraft.getWindow();
        if (windowPointer != window.getWindow()) {
            return;
        }
        // FORGE: Allows for Horizontal Scroll to be recognized as Vertical Scroll - Fixes MC-121772
        double offset = yOffset;
        if (Minecraft.ON_OSX && yOffset == 0) {
            offset = xOffset;
        }
        double d = (this.minecraft.options.discreteMouseScroll ? Math.signum(offset) : offset) * this.minecraft.options.mouseWheelSensitivity;
        if (this.minecraft.getOverlay() == null) {
            if (this.minecraft.screen != null) {
                double x = this.xpos * window.getGuiScaledWidth() / window.getScreenWidth();
                double y = this.ypos * window.getGuiScaledHeight() / window.getScreenHeight();
                this.minecraft.screen.afterMouseAction();
                if (this.minecraft.screen.mouseScrolled(x, y, d)) {
                    return;
                }
                ClientEvents client = ClientEvents.getInstanceNullable();
                if (client != null) {
                    client.onGUIMouseScrollPost(x, y, d);
                }
            }
            else if (this.minecraft.player != null) {
                if (this.accumulatedScroll != 0 && Math.signum(d) != Math.signum(this.accumulatedScroll)) {
                    this.accumulatedScroll = 0;
                }
                this.accumulatedScroll += d;
                int i = (int) this.accumulatedScroll;
                if (i == 0) {
                    return;
                }
                this.accumulatedScroll -= i;
                if (this.minecraft.player.isSpectator()) {
                    if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
                        this.minecraft.gui.getSpectatorGui().onMouseScrolled(-i);
                    }
                    else {
                        float flySpeed = Mth.clamp(this.minecraft.player.getAbilities().getFlyingSpeed() + i * 0.005F, 0.0F, 0.2F);
                        this.minecraft.player.getAbilities().setFlyingSpeed(flySpeed);
                    }
                }
                else {
                    this.minecraft.player.getInventory().swapPaint(i);
                }
            }
        }
    }

    @Shadow
    public abstract void turnPlayer();
}
