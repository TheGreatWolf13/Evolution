package tgw.evolution.client.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.widgets.EditBoxAdv;
import tgw.evolution.init.EvolutionTexts;

import java.util.function.Consumer;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ScreenEditString extends Screen {
    private final Consumer<String> onSave;
    private final Screen parent;
    private final Function<String, Boolean> validator;
    private final String value;
    private Button doneBtn;
    private EditBox editBox;

    protected ScreenEditString(Screen parent, Component component, String value, Function<String, Boolean> validator, Consumer<String> onSave) {
        super(component);
        this.parent = parent;
        this.value = value;
        this.validator = validator;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        this.editBox = new EditBoxAdv(this.font, this.width / 2 - 150, this.height / 2 - 25, 300, 20, EvolutionTexts.EMPTY);
        this.editBox.setValue(this.value);
        this.editBox.setMaxLength(32_500);
        this.editBox.setResponder(s -> this.updateValidation());
        this.addRenderableWidget(this.editBox);
        this.doneBtn = this.addRenderableWidget(new Button(this.width / 2 - 1 - 150,
                                                           this.height / 2 + 3,
                                                           148,
                                                           20,
                                                           EvolutionTexts.GUI_GENERAL_DONE,
                                                           button -> {
                                                               String text = this.editBox.getValue();
                                                               if (this.validator.apply(text)) {
                                                                   this.onSave.accept(text);
                                                                   this.minecraft.setScreen(this.parent);
                                                               }
                                                           }));
        this.addRenderableWidget(new Button(this.width / 2 + 3,
                                            this.height / 2 + 3,
                                            148,
                                            20,
                                            EvolutionTexts.GUI_GENERAL_CANCEL,
                                            button -> this.minecraft.setScreen(this.parent)));
        this.updateValidation();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        this.editBox.render(matrices, mouseX, mouseY, partialTicks);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFF_FFFF);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void updateValidation() {
        boolean valid = this.validator.apply(this.editBox.getValue());
        this.doneBtn.active = valid;
        this.editBox.setTextColor(valid || this.editBox.getValue().isEmpty() ? ChatFormatting.WHITE.getColor() : ChatFormatting.RED.getColor());
    }
}
