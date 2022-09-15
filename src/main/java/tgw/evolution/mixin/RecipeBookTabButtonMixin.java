package tgw.evolution.mixin;

import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.client.util.MouseButton;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookTabButtonMixin extends StateSwitchingButton {

    public RecipeBookTabButtonMixin(int p_i51128_1_, int p_i51128_2_, int p_i51128_3_, int p_i51128_4_, boolean p_i51128_5_) {
        super(p_i51128_1_, p_i51128_2_, p_i51128_3_, p_i51128_4_, p_i51128_5_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean clicked = this.clicked(mouseX, mouseY);
                if (clicked) {
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
