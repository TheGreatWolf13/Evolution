package tgw.evolution.mixin;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private int activeButton;
    @Shadow
    private boolean ignoreFirstMove;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private double mousePressedTime;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Shadow
    public abstract boolean isMouseGrabbed();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private void onMove(long windowPointer, double xPos, double yPos) {
        if (windowPointer == Minecraft.getInstance().getWindow().getWindow()) {
            if (this.ignoreFirstMove) {
                this.xpos = xPos;
                this.ypos = yPos;
                this.ignoreFirstMove = false;
            }
            Screen screen = this.minecraft.screen;
            if (screen != null && this.minecraft.getOverlay() == null) {
                double x = xPos * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getScreenWidth();
                double y = yPos * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getScreenHeight();
                try {
                    screen.mouseMoved(x, y);
                }
                catch (Throwable t) {
                    CrashReport crashReport = CrashReport.forThrowable(t, "mouseMoved event handler");
                    CrashReportCategory category = crashReport.addCategory("Affected screen");
                    category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                    throw new ReportedException(crashReport);
                }
                if (this.activeButton != -1 && this.mousePressedTime > 0) {
                    double d2 = (xPos - this.xpos) * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getScreenWidth();
                    double d3 = (yPos - this.ypos) * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getScreenHeight();
                    outer:
                    try {
                        if (ForgeHooksClient.onScreenMouseDragPre(screen, x, y, this.activeButton, d2, d3)) {
                            break outer;
                        }
                        if (screen.mouseDragged(x, y, this.activeButton, d2, d3)) {
                            break outer;
                        }
                        ForgeHooksClient.onScreenMouseDragPost(screen, x, y, this.activeButton, d2, d3);
                    }
                    catch (Throwable t) {
                        CrashReport crashReport = CrashReport.forThrowable(t, "mouseDragged event handler");
                        CrashReportCategory category = crashReport.addCategory("Affected screen");
                        category.setDetail("Screen name", () -> screen.getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
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
    }

    @Shadow
    public abstract void turnPlayer();
}
