package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

public class TextArea extends AbstractWidget {

    private final boolean center;
    private final int color;
    private final Component text;

    public TextArea(int x, int y, int width, Component text, int color) {
        this(x, y, width, text, color, false);
    }

    public TextArea(int x, int y, int width, Component text, int color, boolean center) {
        super(x, y, width, 0, EvolutionTexts.EMPTY);
        this.text = text;
        this.height = Minecraft.getInstance().font.split(this.text, width).size() * 10;
        this.color = color;
        this.center = center;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (!super.clicked(mouseX, mouseY)) {
            return false;
        }
        Screen screen = this.getScreen();
        if (screen != null) {
            int x = (int) (mouseX - this.x);
            if (x < 0) {
                return false;
            }
            int y = (int) (mouseY - this.y);
            if (y < 0) {
                return false;
            }
            int index = y / 10;
            Font font = Minecraft.getInstance().font;
            List<FormattedCharSequence> split = font.split(this.text, this.width);
            if (index >= split.size()) {
                return false;
            }
            Style style = font.getSplitter().componentStyleAtWidth(split.get(index), x);
            return screen.handleComponentClicked(style);
        }
        return false;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        if (this.center) {
            Minecraft.getInstance().font.drawWordWrapCenter(matrices, this.text, this.x, this.y, this.width, this.color, false);
        }
        else {
            Minecraft.getInstance().font.drawWordWrap(this.text, this.x, this.y, this.width, this.color);
        }
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        int oldHeight = this.height;
        this.height = Minecraft.getInstance().font.split(this.text, width).size() * 10;
        if (oldHeight != this.height) {
            AbstractWidget parent = this.getParent();
            if (parent != null) {
                parent.childRequestedUpdate();
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
