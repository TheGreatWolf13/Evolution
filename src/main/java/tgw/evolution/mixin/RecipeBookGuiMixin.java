package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(RecipeBookGui.class)
public abstract class RecipeBookGuiMixin extends AbstractGui {

    @Inject(method = "renderGhostRecipeTooltip", at = @At(value = "TAIL"))
    private void onRenderGhostRecipeTooltipPost(MatrixStack p_238925_1_,
                                                int p_238925_2_,
                                                int p_238925_3_,
                                                int p_238925_4_,
                                                int p_238925_5_,
                                                CallbackInfo ci) {
        GuiUtils.postItemToolTip();
    }

    @Inject(method = "renderGhostRecipeTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;" +
                                                                                     "renderComponentTooltip" +
                                                                                     "(Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/util/List;II)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRenderGhostRecipeTooltipPre(MatrixStack matrices,
                                               int p_238925_2_,
                                               int p_238925_3_,
                                               int p_238925_4_,
                                               int p_238925_5_,
                                               CallbackInfo ci,
                                               ItemStack stack) {
        GuiUtils.preItemToolTip(stack);
    }
}
