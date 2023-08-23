package tgw.evolution.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.Boolean2BooleanFunction;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.util.Key;
import tgw.evolution.client.util.Modifiers;

import java.util.List;

public class ScreenConfirmation extends Screen {

    private final Boolean2BooleanFunction handler;
    private final Component message;
    private final Screen parent;
    private Component negativeText = CommonComponents.GUI_NO;
    private Component positiveText = CommonComponents.GUI_YES;

    public ScreenConfirmation(Screen parent, Component message, Boolean2BooleanFunction handler) {
        super(message);
        this.parent = parent;
        this.message = message;
        this.handler = handler;
    }

    @Override
    protected void init() {
        List<FormattedCharSequence> lines = this.font.split(this.message, 300);
        int messageOffset = lines.size() * (this.font.lineHeight + 2) / 2;
        this.addRenderableWidget(new Button(this.width / 2 - 105, this.height / 2 + messageOffset, 100, 20, this.positiveText, button -> {
            if (this.handler.get(true)) {
                assert this.minecraft != null;
                this.minecraft.setScreen(this.parent);
            }
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 2 + messageOffset, 100, 20, this.negativeText, button -> {
            if (this.handler.get(false)) {
                assert this.minecraft != null;
                this.minecraft.setScreen(this.parent);
            }
        }));
    }

    @Override
    public boolean keyPressed(@Key int key, int scanCode, @Modifiers int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (this.handler.get(false)) {
                assert this.minecraft != null;
                this.minecraft.setScreen(this.parent);
            }
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        List<FormattedCharSequence> lines = this.font.split(this.message, 300);
        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = this.font.width(lines.get(i));
            this.font.draw(poseStack, lines.get(i), this.width / 2.0f - lineWidth / 2.0f, this.height / 2.0f - 20 - lines.size() * (this.font.lineHeight + 2) / 2.0f + i * (this.font.lineHeight + 2), 0xFF_FFFF);
        }
    }

    public void setNegativeText(Component negativeText) {
        this.negativeText = negativeText;
    }

    public void setPositiveText(Component positiveText) {
        this.positiveText = positiveText;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
