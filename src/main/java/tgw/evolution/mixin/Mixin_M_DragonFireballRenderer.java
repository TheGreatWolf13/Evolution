package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.DragonFireballRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.DragonFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(DragonFireballRenderer.class)
public abstract class Mixin_M_DragonFireballRenderer extends EntityRenderer<DragonFireball> {

    public Mixin_M_DragonFireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(DragonFireball entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return 15;
    }
}
