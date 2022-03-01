package tgw.evolution.mixin;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin extends GuiComponent {

    @ModifyConstant(method = "getClickedComponentStyleAt", constant = @Constant(doubleValue = 40.0))
    private double modifyGetClickedComponentStyleAt(double original) {
        return original + 10;
    }
}
