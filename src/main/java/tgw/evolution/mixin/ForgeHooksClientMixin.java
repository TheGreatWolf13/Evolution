package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import java.util.List;
import java.util.Optional;

@Mixin(ForgeHooksClient.class)
public abstract class ForgeHooksClientMixin {

    private static final ThreadLocal<PoseStack> STACK = ThreadLocal.withInitial(PoseStack::new);
    @Shadow
    @Final
    private static Matrix4f flipX;
    @Shadow
    @Final
    private static Matrix3f flipXNormal;

    /**
     * @author TheGreatWolf
     * @reason Avoid some allocations.
     */
    @Overwrite
    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack,
                                                                       List<? extends FormattedText> textElements,
                                                                       Optional<TooltipComponent> itemComponent,
                                                                       int mouseX,
                                                                       int screenWidth,
                                                                       int screenHeight,
                                                                       @Nullable Font forcedFont,
                                                                       Font fallbackFont) {
        Font font = getTooltipFont(forcedFont, stack, fallbackFont);
        OList<Either<FormattedText, TooltipComponent>> elements = new OArrayList<>(textElements.size());
        for (int i = 0, len = textElements.size(); i < len; i++) {
            //noinspection ObjectAllocationInLoop
            elements.add(Either.left(textElements.get(i)));
        }
        if (itemComponent.isPresent()) {
            elements.add(1, Either.right(itemComponent.get()));
        }
        ClientEvents.getInstance().renderTooltip(stack, elements);
        // text wrapping
        int tooltipTextWidth = 0;
        for (int i = 0, len = elements.size(); i < len; i++) {
            Optional<FormattedText> left = elements.get(i).left();
            int w = left.isPresent() ? font.width(left.get()) : 0;
            if (w > tooltipTextWidth) {
                tooltipTextWidth = w;
            }
        }
        boolean needsWrap = false;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
                if (mouseX > screenWidth / 2) {
                    tooltipTextWidth = mouseX - 12 - 8;
                }
                else {
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                }
                needsWrap = true;
            }
        }
        OList<ClientTooltipComponent> list = new OArrayList<>();
        if (needsWrap) {
            for (int i = 0, len = elements.size(); i < len; i++) {
                Either<FormattedText, TooltipComponent> either = elements.get(i);
                Optional<FormattedText> left = either.left();
                if (left.isPresent()) {
                    List<FormattedCharSequence> split = font.split(left.get(), tooltipTextWidth);
                    for (int j = 0, len1 = split.size(); j < len1; j++) {
                        //noinspection ObjectAllocationInLoop
                        list.add(ClientTooltipComponent.create(split.get(j)));
                    }
                }
                else {
                    list.add(ClientTooltipComponent.create(either.right().get()));
                }
            }
            return list;
        }
        for (int i = 0, len = elements.size(); i < len; i++) {
            Either<FormattedText, TooltipComponent> either = elements.get(i);
            Optional<FormattedText> left = either.left();
            if (left.isPresent()) {
                FormattedText text = left.get();
                //noinspection ObjectAllocationInLoop
                list.add(ClientTooltipComponent.create(
                        text instanceof Component comp ? comp.getVisualOrderText() : Language.getInstance().getVisualOrder(text)));
            }
            else {
                list.add(ClientTooltipComponent.create(either.right().get()));
            }
        }
        return list;
    }

    @Shadow
    public static Font getTooltipFont(@Nullable Font forcedFont, @NotNull ItemStack stack, Font fallbackFont) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    public static BakedModel handleCameraTransforms(PoseStack matrices,
                                                    BakedModel model,
                                                    ItemTransforms.TransformType cameraTransformType,
                                                    boolean leftHandHackery) {
        PoseStack stack = STACK.get();
        while (!stack.clear()) {
            stack.popPose();
        }
        stack.setIdentity();
        model = model.handlePerspective(cameraTransformType, stack);
        // If the stack is not empty, the code has added a matrix for us to use.
        if (!stack.clear()) {
            // Apply the transformation to the real matrix stack, flipping for left hand
            Matrix4f tMat = stack.last().pose();
            Matrix3f nMat = stack.last().normal();
            if (leftHandHackery) {
                tMat.multiplyBackward(flipX);
                tMat.multiply(flipX);
                nMat.multiplyBackward(flipXNormal);
                nMat.mul(flipXNormal);
            }
            matrices.last().pose().multiply(tMat);
            matrices.last().normal().mul(nMat);
        }
        return model;
    }

    /**
     * @author TheGreatWolf
     * @reason Shutdown shaders it no shader found
     */
    @Overwrite
    public static void loadEntityShader(@Nullable Entity entity, GameRenderer gameRenderer) {
        if (entity != null) {
            ResourceLocation shader = ClientRegistry.getEntityShader(entity.getClass());
            if (shader != null) {
                gameRenderer.loadEffect(shader);
            }
            else {
                gameRenderer.shutdownEffect();
            }
        }
    }
}
