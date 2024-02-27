package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.WitherSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(WitherSkullRenderer.class)
public abstract class Mixin_M_WitherSkullRenderer extends EntityRenderer<WitherSkull> {

    public Mixin_M_WitherSkullRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(WitherSkull entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return 15;
    }
}
