package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public class TextList extends AbstractWidget {

    private final OList<Component> texts = new OArrayList<>();

    public TextList(int x, int y, int width) {
        super(x, y, width, 0, EvolutionTexts.EMPTY);
    }

    public void add(Component text) {
        this.texts.add(text);
        this.height += 11;
    }

    protected boolean click(int index) {
        return false;
    }

    @Override
    protected final boolean clicked(double mouseX, double mouseY) {
        if (!super.clicked(mouseX, mouseY)) {
            return false;
        }
        int x = (int) (mouseX - this.x - 9);
        if (x < 0) {
            return false;
        }
        int y = (int) (mouseY - this.y);
        if (y < 0) {
            return false;
        }
        int index = y / 11;
        if (index >= this.texts.size()) {
            return false;
        }
        Component text = this.texts.get(index);
        int width = Minecraft.getInstance().font.width(text);
        if (x >= width - 1) {
            return false;
        }
        return this.click(index);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        OList<Component> texts = this.texts;
        int y = this.y;
        Font font = Minecraft.getInstance().font;
        for (int i = 0, len = texts.size(); i < len; ++i) {
            GUIUtils.drawLine(this.x + 2, y + 2, this.x + 2, y + 2, 3, 0xff_ffff);
            drawString(matrices, font, texts.get(i), this.x + 9, y, 0xff_ffff);
            y += 11;
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
