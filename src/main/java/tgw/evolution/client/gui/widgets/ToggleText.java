package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.math.MathHelper;

public class ToggleText extends AbstractWidget {

    private final int color;
    private final Component name;
    private final int nameLength;
    private final TextArea text;
    private boolean isOpen;

    public ToggleText(int x, int y, int width, Component name, int nameColor, Component text, int textColor) {
        super(x, y, width, 0, EvolutionTexts.EMPTY);
        this.height = 10;
        this.color = nameColor;
        this.name = name;
        this.text = new TextArea(x, y + 15, width, text, textColor);
        this.nameLength = Minecraft.getInstance().font.width(EvolutionTexts.toggle(this.name, false));
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (!super.clicked(mouseX, mouseY)) {
            return false;
        }
        if (MathHelper.isMouseInArea(mouseX, mouseY, this.x, this.y, this.nameLength + 10, 10)) {
            this.toggle();
            return true;
        }
        return false;
    }

    private void recomputeHeight() {
        int oldHeight = this.height;
        this.height = 10;
        if (this.isOpen) {
            this.height += this.text.getHeight();
        }
        if (oldHeight != this.height) {
            AbstractWidget parent = this.getParent();
            if (parent != null) {
                parent.childRequestedUpdate();
            }
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        GUIUtils.drawEquilateralTriangle(this.x, this.y, 7, this.color, this.isOpen ? Direction.SOUTH : Direction.EAST);
        Minecraft.getInstance().font.draw(matrices, EvolutionTexts.toggle(this.name, MathHelper.isMouseInArea(mouseX, mouseY, this.x, this.y, this.nameLength + 9, 9)), this.x + 10, this.y, this.color);
        if (this.isOpen) {
            this.text.render(matrices, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        super.setScreen(screen);
        this.text.setScreen(screen);
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        this.text.setWidth(width);
        this.recomputeHeight();
    }

    @Override
    public void setX(int x) {
        this.x = x;
        this.text.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
        this.text.y = y + 15;
    }

    public void toggle() {
        this.isOpen = !this.isOpen;
        this.recomputeHeight();
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
