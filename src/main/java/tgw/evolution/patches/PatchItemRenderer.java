package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

public interface PatchItemRenderer {

    default void render_(ItemStack stack, ItemTransforms.TransformType transformType, boolean leftHand, PoseStack matrices, MultiBufferSource buffer, int light, int overlay, BakedModel model, @Nullable BlockAndTintGetter level, int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
