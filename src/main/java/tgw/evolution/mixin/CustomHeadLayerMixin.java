package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {

    public CustomHeadLayerMixin(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    /**
     * @author TheGreatWolf
     * @reason Fix HMs
     */
    @Overwrite
    public static void translateToHead(PoseStack matrices, boolean noIdea) {
        matrices.translate(0.0, 0.25, 0.0);
        matrices.scale(0.625F, 0.625F, 0.625F);
        if (noIdea) {
            matrices.translate(0.0, 0.187_5, 0.0);
        }
    }
}
