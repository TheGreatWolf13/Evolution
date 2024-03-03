package tgw.evolution.mixin;

import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(WitherBossRenderer.class)
public abstract class Mixin_M_WitherBossRenderer extends MobRenderer<WitherBoss, WitherBossModel<WitherBoss>> {

    public Mixin_M_WitherBossRenderer(EntityRendererProvider.Context context, WitherBossModel<WitherBoss> entityModel, float f) {
        super(context, entityModel, f);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getBlockLightLevel(WitherBoss entity, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public int getBlockLightLevel_(Entity entity, int x, int y, int z) {
        return 15;
    }
}
