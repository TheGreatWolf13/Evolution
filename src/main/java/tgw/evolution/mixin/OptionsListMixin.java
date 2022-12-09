package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.OptionsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(OptionsList.class)
public abstract class OptionsListMixin extends ContainerObjectSelectionList<OptionsList.Entry> {

    public OptionsListMixin(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    @Nullable
    public AbstractWidget findOption(Option option) {
        List<OptionsList.Entry> children = this.children();
        for (int i = 0, l = children.size(); i < l; i++) {
            AbstractWidget abstractWidget = children.get(i).options.get(option);
            if (abstractWidget != null) {
                return abstractWidget;
            }
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public Optional<AbstractWidget> getMouseOver(double mouseX, double mouseY) {
        List<OptionsList.Entry> children = this.children();
        for (int i = 0, l = children.size(); i < l; i++) {
            OptionsList.Entry entry = children.get(i);
            for (AbstractWidget abstractwidget : entry.children) {
                if (abstractwidget.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(abstractwidget);
                }
            }
        }
        return Optional.empty();
    }
}
