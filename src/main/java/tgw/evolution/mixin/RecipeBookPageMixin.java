//package tgw.evolution.mixin;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import com.mojang.blaze3d.vertex.PoseStack;
//import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
//import net.minecraft.client.gui.screens.recipebook.RecipeButton;
//import net.minecraftforge.client.gui.GuiUtils;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@SuppressWarnings("MethodMayBeStatic")
//@Mixin(RecipeBookPage.class)
//public abstract class RecipeBookPageMixin {
//
//    @Shadow
//    private RecipeButton hoveredButton;
//
//    @Inject(method = "renderTooltip", at = @At(value = "TAIL"))
//    private void onRenderTooltipPost(PoseStack p_238926_1_, int p_238926_2_, int p_238926_3_, CallbackInfo ci) {
//        GuiUtils.postItemToolTip();
//    }
//
//    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderComponentTooltip" +
//                                                                          "(Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/util/List;II)V"))
//    private void onRenderTooltipPre(MatrixStack p_238926_1_, int p_238926_2_, int p_238926_3_, CallbackInfo ci) {
//        GuiUtils.preItemToolTip(this.hoveredButton.getRecipe().getResultItem());
//    }
//}
