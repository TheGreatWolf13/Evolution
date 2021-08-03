package tgw.evolution.client.gui;

import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.init.EvolutionTexts;

public class ScreenCrash extends Screen {

    protected final CrashReport report;
    private final String comment;
    private int clickWidth;
    private String modListString;
    private ITextComponent textComp;

    public ScreenCrash(CrashReport report) {
        super(EvolutionTexts.GUI_CRASH);
        this.report = report;
        this.comment = getWittyComment();
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
            return comments[(int) (Util.nanoTime() % comments.length)];
        }
        catch (Throwable ignored) {
            return "Witty comment unavailable :(";
        }
    }

    @Override
    public void init() {
        this.addButton(new Button(this.width / 2 - 155,
                                  this.height / 4 + 132,
                                  150,
                                  20,
                                  I18n.format("gui.toTitle"),
                                  button -> this.minecraft.displayGuiScreen(new MainMenuScreen())));
        this.addButton(new Button(this.width / 2 - 155 + 160,
                                  this.height / 4 + 132,
                                  150,
                                  20,
                                  I18n.format("menu.reportBugs"),
                                  button -> this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen(b -> {
                                      if (b) {
                                          Util.getOSType().openURI("https://github.com/MGSchultz-13/Evolution/issues");
                                      }
                                      this.minecraft.displayGuiScreen(this);
                                  }, "https://github.com/MGSchultz-13/Evolution/issues", true))));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseY >= this.height / 4.0 + 60 && mouseY <= this.height / 4.0 + 69) {
                if (mouseX >= this.width / 2.0 - this.clickWidth / 2.0 && mouseX <= this.width / 2.0 + this.clickWidth / 2.0) {
                    this.handleComponentClicked(this.textComp);
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, this.height / 4 - 40, 0xff_ffff);
        this.drawString(this.font, this.comment, this.width / 2 - 140, this.height / 4 - 13, 0xFF_FF00);
        int textColor = 0xD0_D0D0;
        this.font.drawSplitString(I18n.format("evolution.gui.crash.summary"), this.width / 2 - 140, this.height / 4 + 15, 280, textColor);
        this.font.drawSplitString(I18n.format("evolution.gui.crash.report"), this.width / 2 - 140, this.height / 4 + 35, 280, textColor);
        String file;
        int fileColor = 0x00_FF00;
        if (this.report.getFile() != null) {
            this.textComp = new StringTextComponent(this.report.getFile().getName()).applyTextStyle(TextFormatting.UNDERLINE)
                                                                                    .applyTextStyle(style -> style.setClickEvent(new ClickEvent(
                                                                                            ClickEvent.Action.OPEN_FILE,
                                                                                            this.report.getFile().getAbsolutePath())));
            file = this.textComp.getFormattedText();
            this.clickWidth = this.font.getStringWidth(file);
        }
        else {
            fileColor = 0xFF_0000;
            this.clickWidth = 0;
            file = new TranslationTextComponent("evolution.gui.crash.reportSaveFailed").setStyle(EvolutionStyles.DAMAGE).getFormattedText();
        }
        this.drawCenteredString(this.font, file, this.width / 2, this.height / 4 + 60, fileColor);
        this.font.drawSplitString(I18n.format("evolution.gui.crash.conclusion"), this.width / 2 - 140, this.height / 4 + 85, 280, textColor);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
