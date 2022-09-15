package tgw.evolution.mixin;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.util.MouseButton;

import org.jetbrains.annotations.Nullable;
import java.util.List;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {

    @Shadow
    List<? extends GuiEventListener> children();

    /**
     * @author TheGreatWolf
     * @reason Avoid iterator allocation
     */
    @Overwrite
    default boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        for (int i = 0, l = this.children().size(); i < l; i++) {
            GuiEventListener guiEventListener = this.children().get(i);
            if (guiEventListener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guiEventListener);
                if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                    this.setDragging(true);
                }
                return true;
            }
        }
        return false;
    }

    @Shadow
    void setDragging(boolean pIsDragging);

    @Shadow
    void setFocused(@Nullable GuiEventListener pFocused);
}
