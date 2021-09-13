package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.client.gui.widgets.GuiCheckBox;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSUpdatePuzzle;

@OnlyIn(Dist.CLIENT)
public class ScreenPuzzle extends Screen {

    private final TEPuzzle tile;
    private TextFieldWidget attachmentTypeText;
    private boolean checkBB;
    private Button doneButton;
    private TextFieldWidget finalStateText;
    private TextFieldWidget targetPoolText;

    public ScreenPuzzle(TEPuzzle tile) {
        super(NarratorChatListener.NO_TITLE);
        this.tile = tile;
    }

    public static void open(TEPuzzle tile) {
        Minecraft.getInstance().setScreen(new ScreenPuzzle(tile));
    }

    protected void checkValid() {
        this.doneButton.active = ResourceLocation.isValidResourceLocation(this.attachmentTypeText.getValue()) &
                                 ResourceLocation.isValidResourceLocation(this.targetPoolText.getValue());
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150,
                                                    210,
                                                    150,
                                                    20,
                                                    EvolutionTexts.GUI_GENERAL_DONE,
                                                    button -> this.sendUpdatesToServer()));
        this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, EvolutionTexts.GUI_GENERAL_CANCEL, p_214252_1_ -> this.onClose()));
        this.checkBB = this.tile.getCheckBB();
        this.addButton(new GuiCheckBox(this.width / 2 - 4 - 150 + 1, 150, EvolutionTexts.GUI_PUZZLE_CHECKBB, this.checkBB, true) {
            @Override
            public void onPress() {
                super.onPress();
                ScreenPuzzle.this.checkBB = !ScreenPuzzle.this.checkBB;
            }
        });
        this.targetPoolText = new TextFieldWidget(this.font, this.width / 2 - 152, 40, 300, 20, EvolutionTexts.GUI_PUZZLE_TARGET_POOL);
        this.targetPoolText.setMaxLength(128);
        this.targetPoolText.setValue(this.tile.getTargetPool().toString());
        this.targetPoolText.setResponder(string -> this.checkValid());
        this.children.add(this.targetPoolText);
        this.attachmentTypeText = new TextFieldWidget(this.font, this.width / 2 - 152, 80, 300, 20, EvolutionTexts.GUI_PUZZLE_ATTACHMENT_TYPE);
        this.attachmentTypeText.setMaxLength(128);
        this.attachmentTypeText.setValue(this.tile.getAttachmentType().toString());
        this.attachmentTypeText.setResponder(string -> this.checkValid());
        this.children.add(this.attachmentTypeText);
        this.finalStateText = new TextFieldWidget(this.font, this.width / 2 - 152, 120, 300, 20, EvolutionTexts.GUI_PUZZLE_FINAL_STATE);
        this.finalStateText.setMaxLength(256);
        this.finalStateText.setValue(this.tile.getFinalState());
        this.children.add(this.finalStateText);
        this.setInitialFocus(this.targetPoolText);
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float partialTicks) {
        this.renderBackground(matrices);
        drawString(matrices, this.font, EvolutionTexts.GUI_PUZZLE_TARGET_POOL, this.width / 2 - 153, 30, 0xa0_a0a0);
        this.targetPoolText.render(matrices, x, y, partialTicks);
        drawString(matrices, this.font, EvolutionTexts.GUI_PUZZLE_ATTACHMENT_TYPE, this.width / 2 - 153, 70, 0xa0_a0a0);
        this.attachmentTypeText.render(matrices, x, y, partialTicks);
        drawString(matrices, this.font, EvolutionTexts.GUI_PUZZLE_FINAL_STATE, this.width / 2 - 153, 110, 0xa0_a0a0);
        this.finalStateText.render(matrices, x, y, partialTicks);
        super.render(matrices, x, y, partialTicks);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String attachmentType = this.attachmentTypeText.getValue();
        String targetPool = this.targetPoolText.getValue();
        String finalState = this.finalStateText.getValue();
        this.init(mc, width, height);
        this.attachmentTypeText.setValue(attachmentType);
        this.targetPoolText.setValue(targetPool);
        this.finalStateText.setValue(finalState);
    }

    private void sendUpdatesToServer() {
        ResourceLocation targetPool = new ResourceLocation(this.targetPoolText.getValue());
        ResourceLocation attachmentType = new ResourceLocation(this.attachmentTypeText.getValue());
        String finalState = this.finalStateText.getValue();
        this.tile.setTargetPool(targetPool);
        this.tile.setAttachmentType(attachmentType);
        this.tile.setFinalState(finalState);
        this.tile.setCheckBB(this.checkBB);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdatePuzzle(this.tile.getBlockPos(),
                                                                        attachmentType,
                                                                        targetPool,
                                                                        finalState,
                                                                        this.checkBB));
        this.onClose();
    }

    @Override
    public void tick() {
        this.attachmentTypeText.tick();
        this.targetPoolText.tick();
        this.finalStateText.tick();
    }
}
