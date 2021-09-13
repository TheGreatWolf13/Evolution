package tgw.evolution.mixin;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.NewChatGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(NewChatGui.class)
public abstract class NewChatGuiMixin extends AbstractGui {

    @ModifyConstant(method = "getClickedComponentStyleAt", constant = @Constant(doubleValue = 40.0))
    private double modifyGetClickedComponentStyleAt(double original) {
        return original + 10;
    }
}
