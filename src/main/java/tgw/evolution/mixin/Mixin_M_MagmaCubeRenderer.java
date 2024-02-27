package tgw.evolution.mixin;

import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.MagmaCube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(MagmaCubeRenderer.class)
public abstract class Mixin_M_MagmaCubeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {

    public Mixin_M_MagmaCubeRenderer(EntityRendererProvider.Context context, LavaSlimeModel<MagmaCube> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Overwrite
    @DeleteMethod
    @Override
    public int getBlockLightLevel(MagmaCube entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return 15;
    }
}
