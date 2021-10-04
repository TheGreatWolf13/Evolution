package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.DirectionUtil;
import tgw.evolution.util.XoRoShiRoRandom;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {

    private final XoRoShiRoRandom random = new XoRoShiRoRandom();

    @Shadow
    private static void renderQuadList(MatrixStack.Entry p_228803_0_,
                                       IVertexBuilder p_228803_1_,
                                       float p_228803_2_,
                                       float p_228803_3_,
                                       float p_228803_4_,
                                       List<BakedQuad> p_228803_5_,
                                       int p_228803_6_,
                                       int p_228803_7_) {
    }

    /**
     * @reason Avoid allocations
     * @author MGSchultz
     */
    @Overwrite
    public void renderModel(MatrixStack.Entry entry,
                            IVertexBuilder builder,
                            @Nullable BlockState state,
                            IBakedModel bakedModel,
                            float red,
                            float green,
                            float blue,
                            int light,
                            int overlay,
                            IModelData modelData) {
        XoRoShiRoRandom random = this.random;
        for (Direction direction : DirectionUtil.ALL) {
            renderQuadList(entry,
                           builder,
                           red,
                           green,
                           blue,
                           bakedModel.getQuads(state, direction, random.setSeedAndReturn(42L), modelData),
                           light,
                           overlay);
        }
        renderQuadList(entry, builder, red, green, blue, bakedModel.getQuads(state, null, random.setSeedAndReturn(42L), modelData), light, overlay);
    }
}
