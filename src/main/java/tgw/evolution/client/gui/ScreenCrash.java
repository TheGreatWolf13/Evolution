package tgw.evolution.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.*;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.init.EvolutionTexts;

import static tgw.evolution.init.EvolutionStyles.DARK_RED;

public class ScreenCrash extends Screen {

    protected final CrashReport report;
    private final String comment;
    private final Component textComp;
    private final FormattedText textConclusion = new TranslatableComponent("evolution.gui.crash.conclusion");
    private final FormattedText textReport = new TranslatableComponent("evolution.gui.crash.report");
    private final Component textReportSaveFailed = new TranslatableComponent("evolution.gui.crash.reportSaveFailed").setStyle(DARK_RED);
    private final FormattedText textSummary = new TranslatableComponent("evolution.gui.crash.summary");
    private int clickWidth;

    public ScreenCrash(CrashReport report) {
        super(new TranslatableComponent("evolution.gui.crash"));
        this.report = report;
        this.comment = getWittyComment();
        if (this.report.getSaveFile() != null) {
            this.textComp = new TextComponent(this.report.getSaveFile().getName()).withStyle(ChatFormatting.UNDERLINE)
                                                                                  .withStyle(style -> style.withClickEvent(
                                                                                          new ClickEvent(ClickEvent.Action.OPEN_FILE,
                                                                                                         this.report.getSaveFile()
                                                                                                                    .getAbsolutePath())));
        }
        else {
            this.textComp = EvolutionTexts.EMPTY;
        }
    }

    private static String getWittyComment() {
        try {
            String[] comments = {"Who set us up the TNT?",
                                 "Everything's going to plan. No, really, that was supposed to happen.",
                                 "Uh... Did I do that?",
                                 "Oops.",
                                 "Why did you do that?",
                                 "I feel sad now :(",
                                 "My bad.",
                                 "I'm sorry, Dave.",
                                 "I let you down. Sorry :(",
                                 "On the bright side, I bought you a teddy bear!",
                                 "Daisy, daisy...",
                                 "Oh - I know what I did wrong!",
                                 "Hey, that tickles! Hehehe!",
                                 "I blame Dinnerbone.",
                                 "You should try our sister game, Minceraft!",
                                 "Don't be sad. I'll do better next time, I promise!",
                                 "Don't be sad, have a hug! <3",
                                 "I just don't know what went wrong :(",
                                 "Shall we play a game?",
                                 "Quite honestly, I wouldn't worry myself about that.",
                                 "I bet Cylons wouldn't have this problem.",
                                 "Sorry :(",
                                 "Surprise! Haha. Well, this is awkward.",
                                 "Would you like a cupcake?",
                                 "Hi. I'm Minecraft, and I'm a crashaholic.",
                                 "Ooh. Shiny.",
                                 "This doesn't make any sense!",
                                 "Why is it breaking :(",
                                 "Don't do that.",
                                 "Ouch. That hurt :(",
                                 "You're mean.",
                                 "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]",
                                 "There are four lights!",
                                 "But it works on my machine."};
            return comments[(int) (Util.getNanos() % comments.length)];
        }
        catch (Throwable ignored) {
            return "Witty comment unavailable :(";
        }
    }

    @Override
    public void init() {
        assert this.minecraft != null;
        this.addRenderableWidget(new Button(this.width / 2 - 155,
                                            this.height / 4 + 132,
                                            150,
                                            20,
                                            EvolutionTexts.GUI_MENU_TO_TITLE,
                                            button -> this.minecraft.setScreen(new TitleScreen())));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160,
                                            this.height / 4 + 132,
                                            150,
                                            20,
                                            EvolutionTexts.GUI_MENU_REPORT_BUGS,
                                            button -> this.minecraft.setScreen(new ConfirmLinkScreen(b -> {
                                                if (b) {
                                                    Util.getPlatform().openUri("https://github.com/TheGreatWolf13/Evolution/issues");
                                                }
                                                this.minecraft.setScreen(this);
                                            }, "https://github.com/TheGreatWolf13/Evolution/issues", true))));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (mouseY >= this.height / 4.0 + 60 && mouseY <= this.height / 4.0 + 69) {
                if (mouseX >= this.width / 2.0 - this.clickWidth / 2.0 && mouseX <= this.width / 2.0 + this.clickWidth / 2.0) {
                    this.handleComponentClicked(this.textComp.getStyle());
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, this.height / 4 - 40, 0xff_ffff);
        drawString(matrices, this.font, this.comment, this.width / 2 - 140, this.height / 4 - 13, 0xFF_FF00);
        int textColor = 0xD0_D0D0;
        this.font.drawWordWrap(this.textSummary, this.width / 2 - 140, this.height / 4 + 15, 280, textColor);
        this.font.drawWordWrap(this.textReport, this.width / 2 - 140, this.height / 4 + 35, 280, textColor);
        String file;
        int fileColor = 0x00_FF00;
        if (this.report.getSaveFile() != null) {
            file = this.textComp.getString();
            this.clickWidth = this.font.width(file);
        }
        else {
            fileColor = 0xFF_0000;
            this.clickWidth = 0;
            file = this.textReportSaveFailed.getString();
        }
        drawCenteredString(matrices, this.font, file, this.width / 2, this.height / 4 + 60, fileColor);
        this.font.drawWordWrap(this.textConclusion, this.width / 2 - 140, this.height / 4 + 85, 280, textColor);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
