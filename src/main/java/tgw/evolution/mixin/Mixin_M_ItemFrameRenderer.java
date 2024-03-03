package tgw.evolution.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(ItemFrameRenderer.class)
public abstract class Mixin_M_ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T> {

    public Mixin_M_ItemFrameRenderer(EntityRendererProvider.Context context) {
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
        return entity.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, super.getBlockLightLevel_(entity, x, y, z)) : super.getBlockLightLevel_(entity, x, y, z);
    }
}
