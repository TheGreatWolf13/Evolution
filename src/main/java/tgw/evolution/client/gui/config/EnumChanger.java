package tgw.evolution.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.widgets.ButtonReturnable;
import tgw.evolution.client.gui.widgets.OnTooltip;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.config.ConfigEnum;
import tgw.evolution.util.math.MathHelper;

public class EnumChanger extends AbstractWidget {

    private final OnTooltip onTooltip;
    private final Button toggleBtn;
    private int textWidth;

    public EnumChanger(int x, int y, int width, ConfigEnum config, OnTooltip onTooltip, Runnable onChanged) {
        super(x, y, width, 20, config.name());
        this.toggleBtn = new ButtonReturnable(x, y, 50, 20, config.getValueDesc(config.getDirty()), b -> {
            config.cycle(((ButtonReturnable) b).getLastButton() == GLFW.GLFW_MOUSE_BUTTON_1);
            b.setMessage(config.getValueDesc(config.getDirty()));
            onChanged.run();
        });
        this.onTooltip = onTooltip;
        this.updateLayout();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        return this.toggleBtn.mouseClicked(mouseX, mouseY, button);
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
        this.toggleBtn.render(matrices, mouseX, mouseY, partialTicks);
        if (MathHelper.isMouseInArea(mouseX, mouseY, this.x, this.y + 5, this.textWidth, 10)) {
            this.onTooltip.onTooltip(matrices, mouseX, mouseY);
        }
    }

    @Override
    public void setScreen(@Nullable Screen screen) {
        super.setScreen(screen);
        this.toggleBtn.setScreen(screen);
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
        this.toggleBtn.setY(y);
    }

    private void updateLayout() {
        int width = this.width;
        width -= this.toggleBtn.getWidth() + 5;
        this.textWidth = width;
        this.toggleBtn.x = this.x + this.textWidth + 5;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
