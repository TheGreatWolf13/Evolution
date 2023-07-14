package tgw.evolution.mixin;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.util.MouseButton;

import java.util.List;
import java.util.Optional;

@Mixin(ContainerEventHandler.class)
public interface MixinContainerEventHandler {

    @Shadow
    List<? extends GuiEventListener> children();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocation
     */
    @Overwrite
    default Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        List<? extends GuiEventListener> children = this.children();
        for (int i = 0, l = children.size(); i < l; i++) {
            GuiEventListener guiEventListener = children.get(i);
            if (guiEventListener.isMouseOver(mouseX, mouseY)) {
                return Optional.of(guiEventListener);
            }
        }
        return Optional.empty();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid iterator allocation
     */
    @Overwrite
    default boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        List<? extends GuiEventListener> children = this.children();
        for (int i = 0, l = children.size(); i < l; i++) {
            GuiEventListener guiEventListener = children.get(i);
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
