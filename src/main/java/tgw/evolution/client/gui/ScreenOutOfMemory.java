package tgw.evolution.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IFontPatch;

public class ScreenOutOfMemory extends Screen {

    private final boolean alreadyShowed;
    private final FormattedText textCause = new TranslatableComponent("evolution.gui.outOfMemory.cause");
    private final FormattedText textInfo = new TranslatableComponent("evolution.gui.outOfMemory.info");
    private final Component textQuit = new TranslatableComponent("evolution.gui.outOfMemory.quit");
    private final FormattedText textRestart = new TranslatableComponent("evolution.gui.outOfMemory.restart");
    private final Component textSummary = new TranslatableComponent("evolution.gui.outOfMemory.summary");

    public ScreenOutOfMemory(boolean alreadyShowed) {
        super(new TranslatableComponent("evolution.gui.outOfMemory"));
        this.alreadyShowed = alreadyShowed;
    }

    @Override
    protected void init() {
        assert this.minecraft != null;
        Button mainMenuButton = new Button(this.width / 2 - 155,
                                           this.height / 4 + 132,
                                           150,
                                           20,
                                           EvolutionTexts.GUI_MENU_TO_TITLE,
                                           button -> this.minecraft.setScreen(new TitleScreen()));
        if (this.alreadyShowed) {
            mainMenuButton.active = false;
        }
        this.addRenderableWidget(mainMenuButton);
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                            this.height / 4 + 132,
                                            150,
                                            20,
                                            EvolutionTexts.GUI_MENU_QUIT,
                                            button -> this.minecraft.stop()));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, this.height / 4 - 40, 0xff_ffff);
        drawString(matrices, this.font, this.textSummary, this.width / 2 - 140, this.height / 4, 0xa0_a0a0);
        ((IFontPatch) this.font).drawWordWrap(matrices, this.textCause, this.width / 2.0f - 140, this.height / 4.0f + 18, 280, 0xa0_a0a0, true);
        drawString(matrices, this.font, this.textQuit, this.width / 2 - 140, this.height / 4 + 54, 0xa0_a0a0);
        ((IFontPatch) this.font).drawWordWrap(matrices, this.textInfo, this.width / 2.0f - 140, this.height / 4.0f + 72, 280, 0xa0_a0a0, true);
        ((IFontPatch) this.font).drawWordWrap(matrices, this.textRestart, this.width / 2.0f - 140, this.height / 4.0f + 108, 280, 0xa0_a0a0, true);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
