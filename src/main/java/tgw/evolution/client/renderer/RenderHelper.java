package tgw.evolution.client.renderer;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.util.collection.lists.*;
import tgw.evolution.util.constants.RenderLayer;

import java.util.function.Function;
import java.util.function.Supplier;

public final class RenderHelper {

    public static final ThreadLocal<IList> INT_LIST = ThreadLocal.withInitial(IArrayList::new);
    public static final ThreadLocal<FList> FLOAT_LIST = ThreadLocal.withInitial(FArrayList::new);
    public static final ThreadLocal<float[]> TEMP_UV = ThreadLocal.withInitial(() -> new float[4]);
    public static final String[] SAMPLER_NAMES = {"Sampler0",
                                                  "Sampler1",
                                                  "Sampler2",
                                                  "Sampler3",
                                                  "Sampler4",
                                                  "Sampler5",
                                                  "Sampler6",
                                                  "Sampler7",
                                                  "Sampler8",
                                                  "Sampler9",
                                                  "Sampler10",
                                                  "Sampler11"};
    //Shaders
    public static final Supplier<ShaderInstance> SHADER_PARTICLE = GameRenderer::getParticleShader;
    public static final Supplier<ShaderInstance> SHADER_POSITION = GameRenderer::getPositionShader;
    public static final Supplier<ShaderInstance> SHADER_POSITION_COLOR = GameRenderer::getPositionColorShader;
    public static final Supplier<ShaderInstance> SHADER_POSITION_COLOR_TEX = GameRenderer::getPositionColorTexShader;
    public static final Supplier<ShaderInstance> SHADER_POSITION_TEX_COLOR = GameRenderer::getPositionTexColorShader;
    public static final Supplier<ShaderInstance> SHADER_POSITION_TEX_COLOR_NORMAL = GameRenderer::getPositionTexColorNormalShader;
    public static final Supplier<ShaderInstance> SHADER_POSITION_TEX = GameRenderer::getPositionTexShader;
    //Render Types
    public static final Function<ResourceLocation, RenderType> RENDER_TYPE_ENTITY_CUTOUT_NO_CULL = RenderType::entityCutoutNoCull;
    public static final Function<ResourceLocation, RenderType> RENDER_TYPE_ENTITY_TRANSLUCENT = RenderType::entityTranslucent;
    public static final ThreadLocal<Vector3f> MODEL_FROM = ThreadLocal.withInitial(Vector3f::new);
    public static final ThreadLocal<Vector3f> MODEL_TO = ThreadLocal.withInitial(Vector3f::new);
    public static final ThreadLocal<BlockFaceUV> MODEL_FACE_UV = ThreadLocal.withInitial(() -> new BlockFaceUV(null, 0));
    public static final ThreadLocal<BlockElementFace> MODEL_FACE = ThreadLocal.withInitial(
            () -> new BlockElementFace(null, -1, "", MODEL_FACE_UV.get()));
    public static final ThreadLocal<float[]> MODEL_UV = ThreadLocal.withInitial(() -> new float[4]);
    public static final FaceBakery MODEL_FACE_BAKERY = new FaceBakery();
    public static final ThreadLocal<OList<BakedQuad>> MODEL_QUAD_HOLDER = ThreadLocal.withInitial(OArrayList::new);
    private static String[] auxAssetsNames = {"AuxSize0"};

    private RenderHelper() {
    }

    public static String auxAssetsNames(int i) {
        if (i >= auxAssetsNames.length) {
            remakeAux(i);
        }
        return auxAssetsNames[i];
    }

    private static void remakeAux(int i) {
        String[] newAux = new String[i + 1];
        for (int j = 0; j <= i; ++j) {
            //noinspection ObjectAllocationInLoop
            newAux[j] = "AuxSize" + j;
        }
        auxAssetsNames = newAux;
    }

    public static String renderLayerName(@RenderLayer int renderLayer) {
        return switch (renderLayer) {
            case RenderLayer.SOLID -> "RenderType[Solid]";
            case RenderLayer.CUTOUT_MIPPED -> "RenderType[CutoutMipped]";
            case RenderLayer.CUTOUT -> "RenderType[Cutout]";
            case RenderLayer.TRANSLUCENT -> "RenderType[Translucent]";
            case RenderLayer.TRIPWIRE -> "RenderType[Tripwire]";
            default -> "null";
        };
    }
}
