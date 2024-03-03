package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ThrownItemRenderer.class)
public abstract class Mixin_M_ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {

    @Shadow @Final private boolean fullBright;

    public Mixin_M_ThrownItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(T entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return this.fullBright ? 15 : super.getBlockLightLevel_(entity, x, y, z);
    }
}
