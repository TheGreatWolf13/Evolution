package tgw.evolution.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import tgw.evolution.config.ConfigFolder;
import tgw.evolution.init.EvolutionTexts;

public class ConfigPath extends AbstractWidget {

    public ConfigPath(int x, int y, int width, int height) {
        super(x, y, width, height, EvolutionTexts.EMPTY);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        drawCenteredString(matrices, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y, 0xff_ffff);
    }

    public void set(ConfigFolder folder) {
        Font font = Minecraft.getInstance().font;
        int maxWidth = this.width;
        ConfigFolder f = folder;
        MutableComponent comp = EvolutionTexts.EMPTY.copy();
        while (f != null && !f.isRoot()) {
            MutableComponent c = f.name().copy().append(" >> ").append(comp);
            f = f.parent();
            if (font.width(c) > maxWidth) {
                comp = new TextComponent("... >> ").append(comp);
                break;
            }
            comp = c;
        }
        this.setMessage(comp);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
