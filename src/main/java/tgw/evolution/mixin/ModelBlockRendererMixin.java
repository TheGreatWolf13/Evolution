package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.pipeline.IQuadVertexSink;
import tgw.evolution.client.models.pipeline.IVertexDrain;
import tgw.evolution.client.models.pipeline.VanillaVertexTypes;
import tgw.evolution.client.renderer.EvAmbientOcclusionFace;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.ModelQuadUtil;
import tgw.evolution.patches.IBakedQuadPatch;
import tgw.evolution.util.math.ColorABGR;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.XoRoShiRoRandom;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {

    private static final ThreadLocal<EvAmbientOcclusionFace> AOF = ThreadLocal.withInitial(EvAmbientOcclusionFace::new);
    private static final ThreadLocal<float[]> SHAPES = ThreadLocal.withInitial(() -> new float[12]);
    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private static final ThreadLocal<BitSet> BITSET = ThreadLocal.withInitial(() -> new BitSet(3));
    @Shadow
    @Final
    static Direction[] DIRECTIONS;
    private final XoRoShiRoRandom random = new XoRoShiRoRandom();
    @Shadow
    @Final
    private BlockColors blockColors;

    private static void renderQuad(PoseStack.Pose entry, IQuadVertexSink drain, int defaultColor, List<BakedQuad> list, int light, int overlay) {
        if (list.isEmpty()) {
            return;
        }
        drain.ensureCapacity(list.size() * 4);
        for (int i = 0, l = list.size(); i < l; i++) {
            BakedQuad bakedQuad = list.get(i);
            int color = bakedQuad.isTinted() ? defaultColor : 0xFFFF_FFFF;
            IBakedQuadPatch quad = (IBakedQuadPatch) bakedQuad;
            for (int v = 0; v < 4; v++) {
                drain.writeQuad(entry, quad.getX(v), quad.getY(v), quad.getZ(v), color, quad.getTexU(v), quad.getTexV(v), light, overlay,
                                ModelQuadUtil.getFacingNormal(bakedQuad.getDirection()));
            }
        }
    }

    @Shadow
    protected abstract void calculateShape(BlockAndTintGetter pLevel,
                                           BlockState pState,
                                           BlockPos pPos,
                                           int[] pVertices,
                                           Direction pDirection,
                                           @Nullable float[] pShape,
                                           BitSet pShapeFlags);

    private void putQuadData(BlockAndTintGetter level,
                             BlockState state,
                             BlockPos pos,
                             VertexConsumer consumer,
                             PoseStack.Pose matrix,
                             BakedQuad quad,
                             float brightness0,
                             float brightness1,
                             float brightness2,
                             float brightness3,
                             int lightmap0,
                             int lightmap1,
                             int lightmap2,
                             int lightmap3,
                             int packedOverlay) {
        float r;
        float g;
        float b;
        if (quad.isTinted()) {
            int color = this.blockColors.getColor(state, level, pos, quad.getTintIndex());
            r = (color >> 16 & 255) / 255.0F;
            g = (color >> 8 & 255) / 255.0F;
            b = (color & 255) / 255.0F;
        }
        else {
            r = 1.0F;
            g = 1.0F;
            b = 1.0F;
        }
        int[] lightmap = RenderHelper.LIGHTMAP.get();
        lightmap[0] = lightmap0;
        lightmap[1] = lightmap1;
        lightmap[2] = lightmap2;
        lightmap[3] = lightmap3;
        float[] brightness = RenderHelper.BRIGHTNESS.get();
        brightness[0] = brightness0;
        brightness[1] = brightness1;
        brightness[2] = brightness2;
        brightness[3] = brightness3;
        consumer.putBulkData(matrix, quad, brightness, r, g, b, lightmap, packedOverlay, true);
    }

    /**
     * @reason Avoid allocations
     * @author TheGreatWolf
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
        IQuadVertexSink drain = IVertexDrain.of(consumer).createSink(VanillaVertexTypes.QUADS);
        XoRoShiRoRandom random = this.random;
        red = MathHelper.clamp(red, 0.0F, 1.0F);
        green = MathHelper.clamp(green, 0.0F, 1.0F);
        blue = MathHelper.clamp(blue, 0.0F, 1.0F);
        int defaultColor = ColorABGR.pack(red, green, blue, 1.0F);
        for (Direction direction : DirectionUtil.ALL) {
            List<BakedQuad> quads = bakedModel.getQuads(state, direction, random.setSeedAndReturn(42L));
            if (!quads.isEmpty()) {
                renderQuad(entry, drain, defaultColor, quads, light, overlay);
            }
        }
        List<BakedQuad> quads = bakedModel.getQuads(state, null, random.setSeedAndReturn(42L));
        if (!quads.isEmpty()) {
            renderQuad(entry, drain, defaultColor, quads, light, overlay);
        }
        drain.flush();
    }

    /**
     * @param shape      the array, of length 12, to store the shape bounds in
     * @param shapeFlags the bit set to store the shape flags in. The first bit will be {@code true} if the face should
     *                   be offset, and the second if the face is less than a block in width and height.
     */
    private void renderModelFaceAO(BlockAndTintGetter level,
                                   BlockState state,
                                   BlockPos pos,
                                   PoseStack matrices,
                                   VertexConsumer consumer,
                                   List<BakedQuad> quads,
                                   float[] shape,
                                   BitSet shapeFlags,
                                   EvAmbientOcclusionFace AoFace,
                                   int packedOverlay) {
        for (int i = 0, l = quads.size(); i < l; i++) {
            BakedQuad bakedQuad = quads.get(i);
            this.calculateShape(level, state, pos, bakedQuad.getVertices(), bakedQuad.getDirection(), shape, shapeFlags);
            AoFace.calculate(level, state, pos, bakedQuad.getDirection(), shape, shapeFlags, bakedQuad.isShade());
            this.putQuadData(level, state, pos, consumer, matrices.last(), bakedQuad, AoFace.brightness0, AoFace.brightness1, AoFace.brightness2,
                             AoFace.brightness3, AoFace.lightmap0, AoFace.lightmap1, AoFace.lightmap2, AoFace.lightmap3, packedOverlay);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations via iterator
     */
    @Overwrite
    private void renderModelFaceFlat(BlockAndTintGetter level,
                                     BlockState state,
                                     BlockPos pos,
                                     int light,
                                     int overlay,
                                     boolean repackLight,
                                     PoseStack matrices,
                                     VertexConsumer buffer,
                                     List<BakedQuad> quads,
                                     BitSet shapeFlags) {
        for (int i = 0, l = quads.size(); i < l; i++) {
            BakedQuad bakedQuad = quads.get(i);
            if (repackLight) {
                this.calculateShape(level, state, pos, bakedQuad.getVertices(), bakedQuad.getDirection(), null, shapeFlags);
                BlockPos blockpos = shapeFlags.get(0) ? pos.relative(bakedQuad.getDirection()) : pos;
                light = LevelRenderer.getLightColor(level, state, blockpos);
            }
            float f = level.getShade(bakedQuad.getDirection(), bakedQuad.isShade());
            this.putQuadData(level, state, pos, buffer, matrices.last(), bakedQuad, f, f, f, f, light, light, light, light, overlay);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Use EvAmbientOcclusionFace to avoid array allocations.
     */
    @Overwrite
    public boolean tesselateWithAO(BlockAndTintGetter level,
                                   BakedModel model,
                                   BlockState state,
                                   BlockPos pos,
                                   PoseStack matrices,
                                   VertexConsumer consumer,
                                   boolean checkSides,
                                   Random random,
                                   long seed,
                                   int packedOverlay,
                                   IModelData modelData) {
        boolean hasRendered = false;
        float[] shapes = SHAPES.get();
        BitSet bitset = BITSET.get();
        bitset.clear();
        EvAmbientOcclusionFace ambientOcclusionFace = AOF.get();
        BlockPos.MutableBlockPos mutableBlockPos = MUTABLE_POS.get().set(pos);
        for (Direction direction : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> quads = model.getQuads(state, direction, random, modelData);
            if (!quads.isEmpty()) {
                mutableBlockPos.setWithOffset(pos, direction);
                if (!checkSides || Block.shouldRenderFace(state, level, pos, direction, mutableBlockPos)) {
                    this.renderModelFaceAO(level, state, pos, matrices, consumer, quads, shapes, bitset, ambientOcclusionFace, packedOverlay);
                    hasRendered = true;
                }
            }
        }
        random.setSeed(seed);
        List<BakedQuad> quads = model.getQuads(state, null, random, modelData);
        if (!quads.isEmpty()) {
            this.renderModelFaceAO(level, state, pos, matrices, consumer, quads, shapes, bitset, ambientOcclusionFace, packedOverlay);
            return true;
        }
        return hasRendered;
    }
}
