package tgw.evolution.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.blocks.tileentities.TEPuzzle;
import tgw.evolution.client.gui.widgets.CheckBoxAdv;
import tgw.evolution.client.gui.widgets.EditBoxAdv;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSUpdatePuzzle;

@OnlyIn(Dist.CLIENT)
public class ScreenPuzzle extends Screen {

    private final Component textAttachmentType = new TranslatableComponent("evolution.gui.puzzle.attachmentType");
    private final Component textCheckBB = new TranslatableComponent("evolution.gui.puzzle.checkBB");
    private final Component textFinalState = new TranslatableComponent("evolution.gui.puzzle.finalState");
    private final Component textTargetPool = new TranslatableComponent("evolution.gui.puzzle.targetPool");
    private final TEPuzzle tile;
    private EditBoxAdv attachmentTypeEdit;
    private boolean checkBB;
    private Button doneButton;
    private EditBoxAdv finalStateEdit;
    private EditBoxAdv targetPoolEdit;

    public ScreenPuzzle(TEPuzzle tile) {
        super(NarratorChatListener.NO_TITLE);
        this.tile = tile;
    }

    public static void open(TEPuzzle tile) {
        Minecraft.getInstance().setScreen(new ScreenPuzzle(tile));
    }

    protected void checkValid() {
        this.doneButton.active = ResourceLocation.isValidResourceLocation(this.attachmentTypeEdit.getValue()) &
                                 ResourceLocation.isValidResourceLocation(this.targetPoolEdit.getValue());
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 4 - 150,
                                                              210,
                                                              150,
                                                              20,
                                                              EvolutionTexts.GUI_GENERAL_DONE,
                                                              button -> this.sendUpdatesToServer()));
        this.addRenderableWidget(new Button(this.width / 2 + 4, 210, 150, 20, EvolutionTexts.GUI_GENERAL_CANCEL, p_214252_1_ -> this.onClose()));
        this.checkBB = this.tile.getCheckBB();
        this.addRenderableWidget(new CheckBoxAdv(this.width / 2 - 4 - 150 + 1,
                                                 150,
                                                 this.textCheckBB,
                                                 this.checkBB,
                                                 true,
                                                 b -> this.checkBB = !this.checkBB));
        this.targetPoolEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 40, 300, 20, EvolutionTexts.EMPTY);
        this.targetPoolEdit.setMaxLength(128);
        this.targetPoolEdit.setValue(this.tile.getTargetPool().toString());
        this.targetPoolEdit.setResponder(string -> this.checkValid());
        this.addWidget(this.targetPoolEdit);
        this.attachmentTypeEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 80, 300, 20, EvolutionTexts.EMPTY);
        this.attachmentTypeEdit.setMaxLength(128);
        this.attachmentTypeEdit.setValue(this.tile.getAttachmentType().toString());
        this.attachmentTypeEdit.setResponder(string -> this.checkValid());
        this.addWidget(this.attachmentTypeEdit);
        this.finalStateEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 120, 300, 20, EvolutionTexts.EMPTY);
        this.finalStateEdit.setMaxLength(256);
        this.finalStateEdit.setValue(this.tile.getFinalState());
        this.addWidget(this.finalStateEdit);
        this.setInitialFocus(this.targetPoolEdit);
        this.checkValid();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (!this.doneButton.active || keyCode != 257 && keyCode != 335) {
            return false;
        }
        this.sendUpdatesToServer();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.attachmentTypeEdit.setFocus(false);
        this.targetPoolEdit.setFocus(false);
        this.finalStateEdit.setFocus(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(PoseStack matrices, int x, int y, float partialTicks) {
        this.renderBackground(matrices);
        drawString(matrices, this.font, this.textTargetPool, this.width / 2 - 153, 30, 0xa0_a0a0);
        this.targetPoolEdit.render(matrices, x, y, partialTicks);
        drawString(matrices, this.font, this.textAttachmentType, this.width / 2 - 153, 70, 0xa0_a0a0);
        this.attachmentTypeEdit.render(matrices, x, y, partialTicks);
        drawString(matrices, this.font, this.textFinalState, this.width / 2 - 153, 110, 0xa0_a0a0);
        this.finalStateEdit.render(matrices, x, y, partialTicks);
        super.render(matrices, x, y, partialTicks);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String attachmentType = this.attachmentTypeEdit.getValue();
        String targetPool = this.targetPoolEdit.getValue();
        String finalState = this.finalStateEdit.getValue();
        this.init(mc, width, height);
        this.attachmentTypeEdit.setValue(attachmentType);
        this.targetPoolEdit.setValue(targetPool);
        this.finalStateEdit.setValue(finalState);
    }

    private void sendUpdatesToServer() {
        ResourceLocation targetPool = new ResourceLocation(this.targetPoolEdit.getValue());
        ResourceLocation attachmentType = new ResourceLocation(this.attachmentTypeEdit.getValue());
        String finalState = this.finalStateEdit.getValue();
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
        this.attachmentTypeEdit.tick();
        this.targetPoolEdit.tick();
        this.finalStateEdit.tick();
    }
}
