package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.world.entity.monster.CaveSpider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.hitbox.hrs.LegacyHRCaveSpider;

@Mixin(CaveSpiderRenderer.class)
public abstract class MixinCaveSpiderRenderer extends SpiderRenderer<CaveSpider> implements LegacyHRCaveSpider {

    public MixinCaveSpiderRenderer(EntityRendererProvider.Context p_174401_) {
        super(p_174401_);
    }

    /**
     * @author TheGreatWolf
     * @reason Use HRs.
     */
    @Override
    @Overwrite
    public void scale(CaveSpider entity, PoseStack matrices, float partialTicks) {
        this.setScale(entity, matrices, partialTicks);
    }
}
