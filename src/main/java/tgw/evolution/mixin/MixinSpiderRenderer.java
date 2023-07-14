package tgw.evolution.mixin;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.world.entity.monster.Spider;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.util.hitbox.hrs.LegacyHRSpider;

@Mixin(SpiderRenderer.class)
public abstract class MixinSpiderRenderer<T extends Spider> extends MobRenderer<T, SpiderModel<T>> implements LegacyHRSpider<T> {

    public MixinSpiderRenderer(EntityRendererProvider.Context pContext,
                               SpiderModel<T> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }
}
