package tgw.evolution.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.gui.widgets.AdvEditBox;
import tgw.evolution.client.gui.widgets.OnTooltip;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.config.ConfigInteger;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.math.MathHelper;

public class IntChanger extends AbstractWidget {

    private final AdvEditBox editBox;
    private final OnTooltip onTooltip;
    private int textWidth;

    public IntChanger(int x, int y, int width, ConfigInteger config, OnTooltip onTooltip, Runnable onChanged) {
        super(x, y, width, 20, config.name());
        this.editBox = new AdvEditBox(Minecraft.getInstance().font, x, y, 50, 20, EvolutionTexts.EMPTY) {
            @Override
            public void setFocus(boolean focus) {
                super.setFocus(focus);
                if (!focus && this.isInvalid()) {
                    this.setValue(String.valueOf(config.getDirty()));
                }
            }
        };
        this.editBox.setValue(String.valueOf(config.getDirty()));
        this.editBox.setResponder(s -> {
            try {
                int i = Integer.parseInt(s);
                if (config.getMinValue() <= i && i <= config.getMaxValue()) {
                    this.editBox.setInvalid(false);
                    config.set(i);
                    this.editBox.setTextColor(0xff_ffff);
                    onChanged.run();
                }
                else {
                    this.editBox.setInvalid(true);
                    this.editBox.setTextColor(0xff_0000);
                    config.set(config.get());
                    onChanged.run();
                }
            }
            catch (NumberFormatException e) {
                this.editBox.setInvalid(true);
                this.editBox.setTextColor(0xff_0000);
                config.set(config.get());
                onChanged.run();
            }
        });
        this.onTooltip = onTooltip;
        this.updateLayout();
    }

    @Override
    public boolean charTyped(char c, @Modifiers int modifiers) {
        return this.editBox.charTyped(c, modifiers);
    }

    @Override
    public boolean keyPressed(@Key int key, int scancode, @Modifiers int modifiers) {
        return this.editBox.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        return this.editBox.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        drawString(matrices, Minecraft.getInstance().font, this.getMessage(), this.x, this.y + 5, 0xff_ffff);
        this.editBox.render(matrices, mouseX, mouseY, partialTicks);
        if (MathHelper.isMouseInArea(mouseX, mouseY, this.x, this.y + 5, this.textWidth, 10)) {
            this.onTooltip.onTooltip(matrices, mouseX, mouseY);
        }
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        super.setScreen(screen);
        this.editBox.setScreen(screen);
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
        this.editBox.setY(y);
    }

    private void updateLayout() {
        int width = this.width;
        width -= this.editBox.getWidth() + 5;
        this.textWidth = width;
        this.editBox.x = this.x + this.textWidth + 5;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
