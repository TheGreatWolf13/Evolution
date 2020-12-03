package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSUpdatePuzzle;

public class ScreenPuzzle extends Screen {

    private final TEPuzzle tile;
    private TextFieldWidget attachmentTypeText;
    private boolean checkBB;
    private Button checkBBButton;
    private Button doneButton;
    private TextFieldWidget finalStateText;
    private TextFieldWidget targetPoolText;

    public ScreenPuzzle(TEPuzzle tile) {
        super(NarratorChatListener.EMPTY);
        this.tile = tile;
    }

    public static void open(TEPuzzle tile) {
        Minecraft.getInstance().displayGuiScreen(new ScreenPuzzle(tile));
    }

    protected void checkValid() {
        this.doneButton.active = ResourceLocation.isResouceNameValid(this.attachmentTypeText.getText()) &
                                 ResourceLocation.isResouceNameValid(this.targetPoolText.getText());
    }

    @Override
    protected void init() {
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150,
                                                    210,
                                                    150,
                                                    20,
                                                    I18n.format("gui.done"),
                                                    button -> this.sendUpdatesToServer()));
        this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, I18n.format("gui.cancel"), p_214252_1_ -> this.onClose()));
        this.checkBB = this.tile.getCheckBB();
        this.checkBBButton = this.addButton(new Button(this.width / 2 - 4 - 150, 160, 50, 20, "evolution.puzzle.checkbb", button -> {
            this.checkBB = !this.checkBB;
            this.updateToggleCheckButton();
        }));
        this.updateToggleCheckButton();
        this.targetPoolText = new TextFieldWidget(this.font, this.width / 2 - 152, 40, 300, 20, I18n.format("evolution.puzzle.target_pool"));
        this.targetPoolText.setMaxStringLength(128);
        this.targetPoolText.setText(this.tile.getTargetPool().toString());
        this.targetPoolText.setResponder(string -> this.checkValid());
        this.children.add(this.targetPoolText);
        this.attachmentTypeText = new TextFieldWidget(this.font, this.width / 2 - 152, 80, 300, 20, I18n.format("evolution.puzzle.attachment_type"));
        this.attachmentTypeText.setMaxStringLength(128);
        this.attachmentTypeText.setText(this.tile.getAttachmentType().toString());
        this.attachmentTypeText.setResponder(string -> this.checkValid());
        this.children.add(this.attachmentTypeText);
        this.finalStateText = new TextFieldWidget(this.font, this.width / 2 - 152, 120, 300, 20, I18n.format("evolution.puzzle.final_state"));
        this.finalStateText.setMaxStringLength(256);
        this.finalStateText.setText(this.tile.getFinalState());
        this.children.add(this.finalStateText);
        this.setFocusedDefault(this.targetPoolText);
        this.checkValid();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        }
        if (!this.doneButton.active || p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
            return false;
        }
        this.sendUpdatesToServer();
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.displayGuiScreen(null);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        this.renderBackground();
        this.drawString(this.font, I18n.format("evolution.puzzle.target_pool"), this.width / 2 - 153, 30, 0xa0_a0a0);
        this.targetPoolText.render(x, y, partialTicks);
        this.drawString(this.font, I18n.format("evolution.puzzle.attachment_type"), this.width / 2 - 153, 70, 0xa0_a0a0);
        this.attachmentTypeText.render(x, y, partialTicks);
        this.drawString(this.font, I18n.format("evolution.puzzle.final_state"), this.width / 2 - 153, 110, 0xa0_a0a0);
        this.finalStateText.render(x, y, partialTicks);
        this.drawString(this.font, I18n.format("evolution.puzzle.checkBB"), this.width / 2 - 153, 150, 0xa0_a0a0);
        super.render(x, y, partialTicks);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String s = this.attachmentTypeText.getText();
        String s1 = this.targetPoolText.getText();
        String s2 = this.finalStateText.getText();
        this.init(mc, width, height);
        this.attachmentTypeText.setText(s);
        this.targetPoolText.setText(s1);
        this.finalStateText.setText(s2);
    }

    private void sendUpdatesToServer() {
        ResourceLocation targetPool = new ResourceLocation(this.targetPoolText.getText());
        ResourceLocation attachmentType = new ResourceLocation(this.attachmentTypeText.getText());
        String finalState = this.finalStateText.getText();
        this.tile.setTargetPool(targetPool);
        this.tile.setAttachmentType(attachmentType);
        this.tile.setFinalState(finalState);
        this.tile.setCheckBB(this.checkBB);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdatePuzzle(this.tile.getPos(), attachmentType, targetPool, finalState, this.checkBB));
        this.onClose();
    }

    @Override
    public void tick() {
        this.attachmentTypeText.tick();
        this.targetPoolText.tick();
        this.finalStateText.tick();
    }

    private void updateToggleCheckButton() {
        if (this.checkBB) {
            this.checkBBButton.setMessage(I18n.format("evolution.puzzle.true"));
        }
        else {
            this.checkBBButton.setMessage(I18n.format("evolution.puzzle.false"));
        }
    }
}
