package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import tgw.evolution.init.EvolutionTexts;

import java.util.List;

public class ScreenOutOfMemory extends Screen {

    private final boolean alreadyShowed;

    public ScreenOutOfMemory(boolean alreadyShowed) {
        super(EvolutionTexts.GUI_OUT_OF_MEMORY);
        this.alreadyShowed = alreadyShowed;
    }

    @Override
    protected void init() {
        Button mainMenuButton = new Button(this.width / 2 - 155,
                                           this.height / 4 + 132,
                                           150,
                                           20,
                                           EvolutionTexts.GUI_MENU_TO_TITLE,
                                           button -> this.minecraft.setScreen(new MainMenuScreen()));
        if (this.alreadyShowed) {
            mainMenuButton.active = false;
        }
        this.addButton(mainMenuButton);
        this.addButton(new Button(this.width / 2 - 155 + 160,
                                  this.height / 4 + 132,
                                  150,
                                  20,
                                  EvolutionTexts.GUI_MENU_QUIT,
                                  button -> this.minecraft.stop()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, this.height / 4 - 40, 0xff_ffff);
        drawString(matrices, this.font, EvolutionTexts.GUI_OUT_OF_MEMORY_SUMMARY, this.width / 2 - 140, this.height / 4, 0xa0_a0a0);
        List<IReorderingProcessor> list = this.font.split(EvolutionTexts.GUI_OUT_OF_MEMORY_CAUSE, 280);
        for (int i = 0; i < list.size(); i++) {
            this.font.drawShadow(matrices, list.get(i), this.width / 2.0f - 140, this.height / 4.0f + 18 + i * 9, 0xa0_a0a0);
        }
        drawString(matrices, this.font, EvolutionTexts.GUI_OUT_OF_MEMORY_QUIT, this.width / 2 - 140, this.height / 4 + 54, 0xa0_a0a0);
        list = this.font.split(EvolutionTexts.GUI_OUT_OF_MEMORY_INFO, 280);
        for (int i = 0; i < list.size(); i++) {
            this.font.drawShadow(matrices, list.get(i), this.width / 2.0f - 140, this.height / 4.0f + 72 + i * 9, 0xa0_a0a0);
        }
        list = this.font.split(EvolutionTexts.GUI_OUT_OF_MEMORY_RESTART, 280);
        for (int i = 0; i < list.size(); i++) {
            this.font.drawShadow(matrices, list.get(i), this.width / 2.0f - 140, this.height / 4.0f + 108 + i * 9, 0xa0_a0a0);
        }
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
