package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ExperienceOrbRenderer.class)
public abstract class Mixin_M_ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {

    public Mixin_M_ExperienceOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(ExperienceOrb experienceOrb, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return Mth.clamp(super.getBlockLightLevel_(entity, x, y, z) + 7, 0, 15);
    }
}
