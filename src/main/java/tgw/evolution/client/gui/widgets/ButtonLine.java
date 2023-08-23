package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public class ButtonLine extends AbstractWidget {

    private final OList<AbstractWidget> buttons = new OArrayList<>();
    private final int maxButtonWidth;
    private final int spacing;

    public ButtonLine(int x, int y, int width, int height, int spacing, int maxButtonWidth) {
        super(x, y, width, height, EvolutionTexts.EMPTY);
        this.spacing = spacing;
        this.maxButtonWidth = maxButtonWidth;
    }

    public void add(AbstractWidget button) {
        this.buttons.add(button);
        this.updateLayout();
    }

    @Override
    public boolean charTyped(char c, @Modifiers int modifiers) {
        OList<AbstractWidget> buttons = this.buttons;
        for (int i = 0, len = buttons.size(); i < len; ++i) {
            if (buttons.get(i).charTyped(c, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(@Key int key, int scancode, @Modifiers int modifiers) {
        OList<AbstractWidget> buttons = this.buttons;
        for (int i = 0, len = buttons.size(); i < len; ++i) {
            if (buttons.get(i).keyPressed(key, scancode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        OList<AbstractWidget> buttons = this.buttons;
        for (int i = 0, len = buttons.size(); i < len; ++i) {
            if (buttons.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        OList<AbstractWidget> buttons = this.buttons;
        for (int i = 0, len = buttons.size(); i < len; ++i) {
            buttons.get(i).render(matrices, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        super.setScreen(screen);
        OList<AbstractWidget> buttons = this.buttons;
        for (int i = 0, len = buttons.size(); i < len; ++i) {
            buttons.get(i).setScreen(screen);
        }
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        this.updateLayout();
    }

    @Override
    public void setX(int x) {
        this.x = x;
        this.updateLayout();
    }

    @Override
    public void setY(int y) {
        this.y = y;
        OList<AbstractWidget> buttons = this.buttons;
        for (int i = 0, len = buttons.size(); i < len; ++i) {
            buttons.get(i).setY(y);
        }
    }

    private void updateLayout() {
        int width = this.width;
        OList<AbstractWidget> list = this.buttons;
        int buttons = list.size();
        int neededWidth = this.maxButtonWidth * buttons + this.spacing * (buttons - 1);
        if (neededWidth > width) {
            width -= (buttons - 1) * this.spacing;
            int btnWidth = width / buttons;
            int x = this.x;
            for (int i = 0, len = list.size(); i < len; ++i) {
                AbstractWidget button = list.get(i);
                button.setX(x);
                button.setWidth(btnWidth);
                x += btnWidth + this.spacing;
            }
        }
        else {
            int delta = width - neededWidth;
            int x = this.x + delta / 2;
            for (int i = 0, len = list.size(); i < len; ++i) {
                AbstractWidget button = list.get(i);
                button.setX(x);
                button.setWidth(this.maxButtonWidth);
                x += this.maxButtonWidth + this.spacing;
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
