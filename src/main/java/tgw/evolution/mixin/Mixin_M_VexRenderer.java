package tgw.evolution.mixin;

import net.minecraft.client.model.VexModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(VexRenderer.class)
public abstract class Mixin_M_VexRenderer extends HumanoidMobRenderer<Vex, VexModel> {

    public Mixin_M_VexRenderer(EntityRendererProvider.Context context, VexModel humanoidModel, float f) {
        super(context, humanoidModel, f);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(Vex entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return 15;
    }
}
