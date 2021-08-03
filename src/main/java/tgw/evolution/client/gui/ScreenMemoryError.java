package tgw.evolution.client.gui;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import tgw.evolution.init.EvolutionTexts;

public class ScreenMemoryError extends Screen {

    private final boolean alreadyShowed;

    public ScreenMemoryError(boolean alreadyShowed) {
        super(EvolutionTexts.GUI_OUT_OF_MEMORY);
        this.alreadyShowed = alreadyShowed;
    }

    @Override
    protected void init() {
        Button mainMenuButton = new Button(this.width / 2 - 155,
                                           this.height / 4 + 132,
                                           150,
                                           20,
                                           I18n.format("gui.toTitle"),
                                           button -> this.minecraft.displayGuiScreen(new MainMenuScreen()));
        if (this.alreadyShowed) {
            mainMenuButton.active = false;
        }
        this.addButton(mainMenuButton);
        this.addButton(new Button(this.width / 2 - 155 + 160,
                                  this.height / 4 + 132,
                                  150,
                                  20,
                                  I18n.format("menu.quit"),
                                  button -> this.minecraft.shutdown()));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, this.height / 4 - 40, 0xff_ffff);
        this.drawString(this.font, I18n.format("evolution.gui.outOfMemory.summary"), this.width / 2 - 140, this.height / 4, 0xa0_a0a0);
        this.font.drawSplitString(I18n.format("evolution.gui.outOfMemory.cause"), this.width / 2 - 140, this.height / 4 + 18, 280, 0xa0_a0a0);
        this.drawString(this.font, I18n.format("evolution.gui.outOfMemory.quit"), this.width / 2 - 140, this.height / 4 + 54, 0xa0_a0a0);
        this.font.drawSplitString(I18n.format("evolution.gui.outOfMemory.info"), this.width / 2 - 140, this.height / 4 + 72, 280, 0xa0_a0a0);
        this.font.drawSplitString(I18n.format("evolution.gui.outOfMemory.restart"), this.width / 2 - 140, this.height / 4 + 108, 280, 0xa0_a0a0);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
