package tgw.evolution.mixin;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gui.widgets.Area;

@Mixin(AbstractContainerEventHandler.class)
public abstract class MixinAbstractContainerEventHandler extends GuiComponent implements ContainerEventHandler {

    @Shadow private @Nullable GuiEventListener focused;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void setFocused(@Nullable GuiEventListener widget) {
        while (widget instanceof Area a) {
            AbstractWidget focused = a.getFocused();
            if (focused != null) {
                widget = focused;
            }
            else {
                break;
            }
        }
        this.focused = widget;
    }
}
