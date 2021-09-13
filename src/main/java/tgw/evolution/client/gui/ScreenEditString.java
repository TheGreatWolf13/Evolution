package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.init.EvolutionTexts;

import java.util.function.Consumer;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class ScreenEditString extends Screen {
    private final Consumer<String> onSave;
    private final Screen parent;
    private final Function<Object, Boolean> validator;
    private final String value;
    private TextFieldWidget textField;

    protected ScreenEditString(Screen parent, ITextComponent component, String value, Function<Object, Boolean> validator, Consumer<String> onSave) {
        super(component);
        this.parent = parent;
        this.value = value;
        this.validator = validator;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        this.textField = new TextFieldWidget(this.font, this.width / 2 - 150, this.height / 2 - 25, 300, 20, EvolutionTexts.EMPTY);
        this.textField.setValue(this.value);
        this.textField.setMaxLength(32_500);
        this.children.add(this.textField);
        this.addButton(new Button(this.width / 2 - 1 - 150, this.height / 2 + 3, 148, 20, EvolutionTexts.GUI_GENERAL_DONE, button -> {
            String text = this.textField.getValue();
            if (this.validator.apply(text)) {
                this.onSave.accept(text);
                this.minecraft.setScreen(this.parent);
            }
        }));
        this.addButton(new Button(this.width / 2 + 3,
                                  this.height / 2 + 3,
                                  148,
                                  20,
                                  EvolutionTexts.GUI_GENERAL_CANCEL,
                                  button -> this.minecraft.setScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        this.textField.render(matrices, mouseX, mouseY, partialTicks);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFF_FFFF);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }
}
