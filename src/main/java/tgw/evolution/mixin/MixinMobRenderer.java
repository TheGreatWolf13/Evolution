package tgw.evolution.mixin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.util.hitbox.hms.HMEntity;
import tgw.evolution.util.hitbox.hrs.HRMob;

@Mixin(MobRenderer.class)
public abstract class MixinMobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> implements HRMob<T, HMEntity<T>> {

    public MixinMobRenderer(EntityRendererProvider.Context pContext, M pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }
}
