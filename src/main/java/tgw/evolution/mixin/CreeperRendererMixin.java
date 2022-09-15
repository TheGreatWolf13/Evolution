package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.hitbox.hrs.HR;
import tgw.evolution.util.hitbox.hrs.HRCreeper;

@Mixin(CreeperRenderer.class)
public abstract class CreeperRendererMixin extends MobRenderer<Creeper, CreeperModel<Creeper>> implements HRCreeper {

    public CreeperRendererMixin(EntityRendererProvider.Context pContext,
                                CreeperModel<Creeper> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    /**
     * @author TheGreatWolf
     * @reason Use HRs
     */
    @Override
    @Overwrite
    protected void scale(Creeper entity, PoseStack matrices, float partialTicks) {
        this.setScale(entity, (HR) matrices, partialTicks);
    }
}
