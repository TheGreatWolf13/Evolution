package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.XoRoShiRoRandom;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    private final XoRoShiRoRandom random = new XoRoShiRoRandom();

    @Shadow
    private static void renderQuadList(PoseStack.Pose p_111059_,
                                       VertexConsumer p_111060_,
                                       float p_111061_,
                                       float p_111062_,
                                       float p_111063_,
                                       List<BakedQuad> p_111064_,
                                       int p_111065_,
                                       int p_111066_) {
    }

    /**
     * @reason Avoid allocations
     * @author MGSchultz
     */
    @Overwrite
    public void renderModel(PoseStack.Pose entry,
                            VertexConsumer consumer,
                            @Nullable BlockState state,
                            BakedModel bakedModel,
                            float red,
                            float green,
                            float blue,
                            int light,
                            int overlay,
                            IModelData modelData) {
        XoRoShiRoRandom random = this.random;
        for (Direction direction : DirectionUtil.ALL) {
            renderQuadList(entry,
                           consumer,
                           red,
                           green,
                           blue,
                           bakedModel.getQuads(state, direction, random.setSeedAndReturn(42L), modelData),
                           light,
                           overlay);
        }
        renderQuadList(entry, consumer, red, green, blue, bakedModel.getQuads(state, null, random.setSeedAndReturn(42L), modelData), light, overlay);
    }
}
