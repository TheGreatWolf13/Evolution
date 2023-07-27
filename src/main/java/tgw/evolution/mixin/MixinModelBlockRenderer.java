package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.client.renderer.EvAmbientOcclusionFace;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.patches.PatchModelBlockRenderer;
import tgw.evolution.patches.PatchVertexConsumer;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.XoRoShiRoRandom;

import java.util.List;
import java.util.Random;

@Mixin(ModelBlockRenderer.class)
public abstract class MixinModelBlockRenderer implements PatchModelBlockRenderer {

    @Unique private static final ThreadLocal<EvAmbientOcclusionFace> AOF = ThreadLocal.withInitial(EvAmbientOcclusionFace::new);
    @Unique private static final ThreadLocal<float[]> SHAPES = ThreadLocal.withInitial(() -> new float[12]);
    @Unique private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    @Shadow @Final static Direction[] DIRECTIONS;
    @Unique private final XoRoShiRoRandom random = new XoRoShiRoRandom();
    @Shadow @Final private BlockColors blockColors;

    @Unique
    private static byte calculateShape(BlockGetter level,
                                       BlockState state,
                                       BlockPos pos,
                                       int[] vertices,
                                       Direction direction,
                                       float[] shape) {
        float x = Float.intBitsToFloat(vertices[0]);
        float y = Float.intBitsToFloat(vertices[1]);
        float z = Float.intBitsToFloat(vertices[2]);
        float minX = x;
        float minY = y;
        float minZ = z;
        float maxX = x;
        float maxY = y;
        float maxZ = z;
        for (int vertex = 1; vertex < 4; ++vertex) {
            int offset = vertex * 8;
            x = Float.intBitsToFloat(vertices[offset]);
            y = Float.intBitsToFloat(vertices[offset + 1]);
            z = Float.intBitsToFloat(vertices[offset + 2]);
            if (x < minX) {
                minX = x;
            }
            else if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            else if (y > maxY) {
                maxY = y;
            }
            if (z < minZ) {
                minZ = z;
            }
            else if (z > maxZ) {
                maxZ = z;
            }
        }
        shape[0] = minY;
        shape[1] = maxY;
        shape[2] = minZ;
        shape[3] = maxZ;
        shape[4] = minX;
        shape[5] = maxX;
        shape[6] = 1.0F - minY;
        shape[7] = 1.0F - maxY;
        shape[8] = 1.0F - minZ;
        shape[9] = 1.0F - maxZ;
        shape[10] = 1.0F - minX;
        shape[11] = 1.0F - maxX;
        byte flags = 0;
        switch (direction) {
            case DOWN -> {
                if (minY == maxY && (minY < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos))) {
                    flags = 1;
                }
                if (minX >= 1.0E-4F || minZ >= 1.0E-4F || maxX <= 0.999_9F || maxZ <= 0.999_9F) {
                    flags |= 2;
                }
            }
            case UP -> {
                if (minY == maxY && (maxY > 0.999_9F || state.isCollisionShapeFullBlock(level, pos))) {
                    flags = 1;
                }
                if (minX >= 1.0E-4F || minZ >= 1.0E-4F || maxX <= 0.999_9F || maxZ <= 0.999_9F) {
                    flags |= 2;
                }
            }
            case NORTH -> {
                if (minZ == maxZ && (minZ < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos))) {
                    flags = 1;
                }
                if (minX >= 1.0E-4F || minY >= 1.0E-4F || maxX <= 0.999_9F || maxY <= 0.999_9F) {
                    flags |= 2;
                }
            }
            case SOUTH -> {
                if (minZ == maxZ && (maxZ > 0.999_9F || state.isCollisionShapeFullBlock(level, pos))) {
                    flags = 1;
                }
                if (minX >= 1.0E-4F || minY >= 1.0E-4F || maxX <= 0.999_9F || maxY <= 0.999_9F) {
                    flags |= 2;
                }
            }
            case WEST -> {
                if (minX == maxX && (minX < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos))) {
                    flags = 1;
                }
                if (minY >= 1.0E-4F || minZ >= 1.0E-4F || maxY <= 0.999_9F || maxZ <= 0.999_9F) {
                    flags |= 2;
                }
            }
            case EAST -> {
                if (minX == maxX && (maxX > 0.999_9F || state.isCollisionShapeFullBlock(level, pos))) {
                    flags = 1;
                }
                if (minY >= 1.0E-4F || minZ >= 1.0E-4F || maxY <= 0.999_9F || maxZ <= 0.999_9F) {
                    flags |= 2;
                }
            }
        }
        return flags;
    }

    @Unique
    private static boolean calculateShape(BlockGetter level,
                                          BlockState state,
                                          BlockPos pos,
                                          int[] vertices,
                                          Direction direction) {
        int offset = switch (direction.getAxis()) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
        float p = Float.intBitsToFloat(vertices[offset]);
        float min = p;
        float max = p;
        for (int i = 1; i < 4; ++i) {
            p = Float.intBitsToFloat(vertices[i * 8 + offset]);
            if (p < min) {
                min = p;
            }
            else if (p > max) {
                max = p;
            }
        }
        if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            return min == max && (min < 1.0E-4F || state.isCollisionShapeFullBlock(level, pos));
        }
        return min == max && (max > 0.999_9F || state.isCollisionShapeFullBlock(level, pos));
    }

    private static void renderQuad(PoseStack.Pose entry, VertexConsumer consumer, int defaultColor, List<BakedQuad> list, int light, int overlay) {
        if (list.isEmpty()) {
            return;
        }
        Matrix4f matrix = entry.pose();
        for (int i = 0, l = list.size(); i < l; i++) {
            BakedQuad quad = list.get(i);
            int[] vertices = quad.getVertices();
            int color = quad.isTinted() ? defaultColor : 0xFFFF_FFFF;
            Vec3i normal = quad.getDirection().getNormal();
            float nx = normal.getX();
            float ny = normal.getY();
            float nz = normal.getZ();
            for (int vertex = 0; vertex < 4; ++vertex) {
                int offset = vertex * 8;
                consumer.vertex(matrix,
                                Float.intBitsToFloat(vertices[offset]),
                                Float.intBitsToFloat(vertices[offset + 1]),
                                Float.intBitsToFloat(vertices[offset + 2]))
                        .color(color)
                        .uv(Float.intBitsToFloat(vertices[offset + 4]), Float.intBitsToFloat(vertices[offset + 5]))
                        .overlayCoords(overlay)
                        .uv2(light)
                        .normal(nx, ny, nz)
                        .endVertex();
            }
        }
    }

    @Overwrite
    private void putQuadData(BlockAndTintGetter level,
                             BlockState state,
                             BlockPos pos,
                             VertexConsumer consumer,
                             PoseStack.Pose entry,
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
        ((PatchVertexConsumer) consumer).putBulkData(entry,
                                                     quad,
                                                     brightness0, brightness1, brightness2, brightness3,
                                                     r, g, b,
                                                     lightmap0, lightmap1, lightmap2, lightmap3,
                                                     packedOverlay);
    }

    /**
     * @reason Avoid allocations
     * @author TheGreatWolf
     */
    @Overwrite
    //Deleted IModelData as last parameter
    public void renderModel(PoseStack.Pose entry,
                            VertexConsumer consumer,
                            @Nullable BlockState state,
                            BakedModel bakedModel,
                            float red,
                            float green,
                            float blue,
                            int light,
                            int overlay) {
        XoRoShiRoRandom random = this.random;
        red = MathHelper.clamp(red, 0.0F, 1.0F);
        green = MathHelper.clamp(green, 0.0F, 1.0F);
        blue = MathHelper.clamp(blue, 0.0F, 1.0F);
        int defaultColor = 0xff << 24 | (int) (blue * 255) << 16 | (int) (green * 255) << 8 | (int) (red * 255);
        for (Direction direction : DirectionUtil.ALL) {
            List<BakedQuad> quads = bakedModel.getQuads(state, direction, random.setSeedAndReturn(42L));
            if (!quads.isEmpty()) {
                renderQuad(entry, consumer, defaultColor, quads, light, overlay);
            }
        }
        List<BakedQuad> quads = bakedModel.getQuads(state, null, random.setSeedAndReturn(42L));
        if (!quads.isEmpty()) {
            renderQuad(entry, consumer, defaultColor, quads, light, overlay);
        }
    }

    /**
     * @param shape the array, of length 12, to store the shape bounds in
     */
    @Unique
    private void renderModelFaceAO(BlockAndTintGetter level,
                                   BlockState state,
                                   BlockPos pos,
                                   PoseStack matrices,
                                   VertexConsumer consumer,
                                   List<BakedQuad> quads,
                                   float[] shape,
                                   EvAmbientOcclusionFace AoFace,
                                   int packedOverlay) {
        for (int i = 0, l = quads.size(); i < l; i++) {
            BakedQuad bakedQuad = quads.get(i);
            AoFace.calculate(level, state, pos, bakedQuad.getDirection(), shape,
                             calculateShape(level, state, pos, bakedQuad.getVertices(), bakedQuad.getDirection(), shape), bakedQuad.isShade());
            this.putQuadData(level, state, pos, consumer, matrices.last(), bakedQuad, AoFace.brightness0, AoFace.brightness1, AoFace.brightness2,
                             AoFace.brightness3, AoFace.lightmap0, AoFace.lightmap1, AoFace.lightmap2, AoFace.lightmap3, packedOverlay);
        }
    }

    @Unique
    private void renderModelFaceFlat(BlockAndTintGetter level,
                                     BlockState state,
                                     BlockPos pos,
                                     int light,
                                     int overlay,
                                     boolean repackLight,
                                     PoseStack matrices,
                                     VertexConsumer buffer,
                                     List<BakedQuad> quads) {
        for (int i = 0, l = quads.size(); i < l; i++) {
            BakedQuad quad = quads.get(i);
            if (repackLight) {
                BlockPos blockpos = calculateShape(level, state, pos, quad.getVertices(), quad.getDirection()) ?
                                    MUTABLE_POS.get().setWithOffset(pos, quad.getDirection()) :
                                    pos;
                light = EvLevelRenderer.getLightColor(level, state, blockpos);
            }
            float bright = level.getShade(quad.getDirection(), quad.isShade());
            this.putQuadData(level, state, pos, buffer, matrices.last(), quad, bright, bright, bright, bright, light, light, light, light, overlay);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Don't use
     */
    @Overwrite
    public boolean tesselateBlock(BlockAndTintGetter blockAndTintGetter,
                                  BakedModel bakedModel,
                                  BlockState blockState,
                                  BlockPos blockPos,
                                  PoseStack poseStack,
                                  VertexConsumer vertexConsumer,
                                  boolean bl,
                                  Random random,
                                  long l,
                                  int i) {
        Evolution.warn("wrong tesselateBlock called! Call the version with IModelData!");
        return false;
    }

    @Override
    public boolean tesselateBlock(BlockAndTintGetter level,
                                  BakedModel model,
                                  BlockState state,
                                  BlockPos pos,
                                  PoseStack matrices,
                                  VertexConsumer builder,
                                  boolean checkSides,
                                  Random random,
                                  long seed,
                                  int packedOverlay,
                                  IModelData modelData) {
        boolean ao = Minecraft.useAmbientOcclusion() && state.getLightEmission() == 0 && model.useAmbientOcclusion();
        Vec3 vec3 = state.getOffset(level, pos);
        matrices.translate(vec3.x, vec3.y, vec3.z);
        modelData = model.getModelData(level, pos.getX(), pos.getY(), pos.getZ(), state, modelData);
        try {
            return ao ?
                   this.tesselateWithAO(level, model, state, pos, matrices, builder, checkSides, random, seed, packedOverlay, modelData) :
                   this.tesselateWithoutAO(level, model, state, pos, matrices, builder, checkSides, random, seed, packedOverlay, modelData);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Tessellating block model");
            CrashReportCategory category = crash.addCategory("Block model being tessellated");
            CrashReportCategory.populateBlockDetails(category, level, pos, state);
            category.setDetail("Using AO", ao);
            throw new ReportedException(crash);
        }
    }

    @Unique
    private boolean tesselateWithAO(BlockAndTintGetter level,
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
        EvAmbientOcclusionFace ambientOcclusionFace = AOF.get();
        BlockPos.MutableBlockPos mutableBlockPos = MUTABLE_POS.get().set(pos);
        for (Direction direction : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> quads = model.getQuads(state, direction, random, modelData);
            if (!quads.isEmpty()) {
                mutableBlockPos.setWithOffset(pos, direction);
                if (!checkSides || Block.shouldRenderFace(state, level, pos, direction, mutableBlockPos)) {
                    this.renderModelFaceAO(level, state, pos, matrices, consumer, quads, shapes, ambientOcclusionFace, packedOverlay);
                    hasRendered = true;
                }
            }
        }
        random.setSeed(seed);
        List<BakedQuad> quads = model.getQuads(state, null, random, modelData);
        if (!quads.isEmpty()) {
            this.renderModelFaceAO(level, state, pos, matrices, consumer, quads, shapes, ambientOcclusionFace, packedOverlay);
            return true;
        }
        return hasRendered;
    }

    /**
     * @author TheGreatWolf
     * @reason Don't use
     */
    @Overwrite
    public boolean tesselateWithAO(BlockAndTintGetter blockAndTintGetter,
                                   BakedModel bakedModel,
                                   BlockState blockState,
                                   BlockPos blockPos,
                                   PoseStack poseStack,
                                   VertexConsumer vertexConsumer,
                                   boolean bl,
                                   Random random,
                                   long l,
                                   int i) {
        Evolution.warn("Wrong tessellateWithAO called! Call the version with IModelData!");
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason Don't use
     */
    @Overwrite
    public boolean tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter,
                                      BakedModel bakedModel,
                                      BlockState blockState,
                                      BlockPos blockPos,
                                      PoseStack poseStack,
                                      VertexConsumer vertexConsumer,
                                      boolean bl,
                                      Random random,
                                      long l,
                                      int i) {
        Evolution.warn("wrong tessellateWithoutAO called! Call the version with IModelData!");
        return false;
    }

    @Unique
    private boolean tesselateWithoutAO(BlockAndTintGetter level,
                                       BakedModel model,
                                       BlockState state,
                                       BlockPos pos,
                                       PoseStack matrices,
                                       VertexConsumer builder,
                                       boolean checkSides,
                                       Random random,
                                       long seed,
                                       int packedOverlay,
                                       IModelData modelData) {
        boolean hasAnything = false;
        BlockPos.MutableBlockPos mutablePos = MUTABLE_POS.get();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(seed);
            List<BakedQuad> quads = model.getQuads(state, direction, random, modelData);
            if (!quads.isEmpty()) {
                mutablePos.setWithOffset(pos, direction);
                if (!checkSides || Block.shouldRenderFace(state, level, pos, direction, mutablePos)) {
                    int light = EvLevelRenderer.getLightColor(level, state, mutablePos);
                    this.renderModelFaceFlat(level, state, pos, light, packedOverlay, false, matrices, builder, quads);
                    hasAnything = true;
                }
            }
        }
        random.setSeed(seed);
        List<BakedQuad> quads = model.getQuads(state, null, random, modelData);
        if (!quads.isEmpty()) {
            this.renderModelFaceFlat(level, state, pos, 0xffff_ffff, packedOverlay, true, matrices, builder, quads);
            return true;
        }
        return hasAnything;
    }
}