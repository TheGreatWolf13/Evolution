package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer extends EntityRenderer<ItemEntity> {

    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow @Final private Random random;

    public MixinItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private int getRenderAmount(ItemStack stack) {
        int count = stack.getCount();
        if (count > 256) {
            return 9;
        }
        if (count > 128) {
            return 8;
        }
        if (count > 64) {
            return 7;
        }
        if (count > 32) {
            return 6;
        }
        if (count > 16) {
            return 5;
        }
        if (count > 8) {
            return 4;
        }
        if (count > 4) {
            return 3;
        }
        if (count >= 2) {
            return 2;
        }
        return 1;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void render(ItemEntity entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        matrices.pushPose();
        ItemStack stack = entity.getItem();
        this.random.setSeed(stack.isEmpty() ? 187 : Item.getId(stack.getItem()) + stack.getDamageValue());
        BakedModel bakedModel = this.itemRenderer.getModel(stack, entity.level, null, entity.getId());
        boolean gui3d = bakedModel.isGui3d();
        int amount = this.getRenderAmount(stack);
        ItemTransforms transforms = bakedModel.getTransforms();
        matrices.translate(0,
                           Mth.sin((entity.getAge() + partialTicks) / 10.0F + entity.bobOffs) * 0.1F + 0.1F + 0.25F * transforms.getTransform(ItemTransforms.TransformType.GROUND).scale.y(),
                           0
        );
        matrices.mulPoseYRad(entity.getSpin(partialTicks));
        Vector3f scale = transforms.ground.scale;
        float scaleX = scale.x();
        float scaleY = scale.y();
        float scaleZ = scale.z();
        if (!gui3d) {
            matrices.translate(-0.0F * (amount - 1) * 0.5F * scaleX,
                               -0.0F * (amount - 1) * 0.5F * scaleY,
                               -0.093_75F * (amount - 1) * 0.5F * scaleZ
            );
        }
        for (int i = 0; i < amount; ++i) {
            matrices.pushPose();
            if (i > 0) {
                if (gui3d) {
                    matrices.translate((this.random.nextFloat() * 2.0F - 1.0F) * 0.15F,
                                       (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F,
                                       (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F
                    );
                }
                else {
                    matrices.translate((this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F,
                                       (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F,
                                       0
                    );
                }
            }
            BlockPos pos = entity.blockPosition();
            this.itemRenderer.render_(stack, ItemTransforms.TransformType.GROUND, false, matrices, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedModel, Minecraft.getInstance().level, pos.getX(), pos.getY(), pos.getZ());
            matrices.popPose();
            if (!gui3d) {
                matrices.translate(0, 0, 0.093_75F * scaleZ);
            }
        }
        matrices.popPose();
        super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
    }
}
