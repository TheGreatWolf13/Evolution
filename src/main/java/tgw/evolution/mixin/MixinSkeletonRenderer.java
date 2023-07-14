package tgw.evolution.mixin;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.hitbox.hrs.LegacyHRSkeleton;

@Mixin(SkeletonRenderer.class)
public abstract class MixinSkeletonRenderer extends HumanoidMobRenderer<AbstractSkeleton, SkeletonModel<AbstractSkeleton>>
        implements LegacyHRSkeleton {

    public MixinSkeletonRenderer(EntityRendererProvider.Context pContext,
                                 SkeletonModel<AbstractSkeleton> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    /**
     * @author TheGreatWolf
     * @reason Use HRs.
     */
    @Override
    @Overwrite
    public boolean isShaking(AbstractSkeleton entity) {
        return this.shaking(entity);
    }
}
