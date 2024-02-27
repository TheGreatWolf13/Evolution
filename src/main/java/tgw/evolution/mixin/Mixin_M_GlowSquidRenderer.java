package tgw.evolution.mixin;

import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GlowSquidRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.GlowSquid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(GlowSquidRenderer.class)
public abstract class Mixin_M_GlowSquidRenderer extends SquidRenderer<GlowSquid> {

    public Mixin_M_GlowSquidRenderer(EntityRendererProvider.Context context, SquidModel<GlowSquid> squidModel) {
        super(context, squidModel);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(GlowSquid entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        int i = (int) Mth.clampedLerp(0.0F, 15.0F, 1.0F - ((GlowSquid) entity).getDarkTicksRemaining() / 10.0F);
        return i == 15 ? 15 : Math.max(i, super.getBlockLightLevel_(entity, x, y, z));
    }
}
