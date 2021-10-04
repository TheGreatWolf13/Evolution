package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.XoRoShiRoRandom;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private final XoRoShiRoRandom random = new XoRoShiRoRandom();

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    public void renderModelLists(IBakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, IVertexBuilder builder) {
        XoRoShiRoRandom random = this.random;
        for (Direction direction : DirectionUtil.ALL) {
            this.renderQuadList(matrices, builder, model.getQuads(null, direction, random.setSeedAndReturn(42L)), stack, light, overlay);
        }
        this.renderQuadList(matrices, builder, model.getQuads(null, null, random.setSeedAndReturn(42L)), stack, light, overlay);
    }

    @Shadow
    public abstract void renderQuadList(MatrixStack matrices, IVertexBuilder builder, List<BakedQuad> quads, ItemStack stack, int light, int overlay);
}
