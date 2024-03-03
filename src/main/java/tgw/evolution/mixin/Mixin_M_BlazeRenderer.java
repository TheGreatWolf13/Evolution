package tgw.evolution.mixin;

import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(BlazeRenderer.class)
public abstract class Mixin_M_BlazeRenderer extends MobRenderer<Blaze, BlazeModel<Blaze>> {

    public Mixin_M_BlazeRenderer(EntityRendererProvider.Context context, BlazeModel<Blaze> entityModel, float f) {
        super(context, entityModel, f);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(Blaze entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return 15;
    }
}
