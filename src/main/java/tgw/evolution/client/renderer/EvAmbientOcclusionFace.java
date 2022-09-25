package tgw.evolution.client.renderer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.BitSet;

public class EvAmbientOcclusionFace {

    public float brightness0;
    public float brightness1;
    public float brightness2;
    public float brightness3;
    public int lightmap0;
    public int lightmap1;
    public int lightmap2;
    public int lightmap3;

    /**
     * @return the ambient occlusion light color
     */
    private static int blend(int lightColor0, int lightColor1, int lightColor2, int lightColor3) {
        if (lightColor0 == 0) {
            lightColor0 = lightColor3;
        }
        if (lightColor1 == 0) {
            lightColor1 = lightColor3;
        }
        if (lightColor2 == 0) {
            lightColor2 = lightColor3;
        }
        return lightColor0 + lightColor1 + lightColor2 + lightColor3 >> 2 & 0xff_00ff;
    }

    private static int blend(int brightness0,
                             int brightness1,
                             int brightness2,
                             int brightness3,
                             float weight0,
                             float weight1,
                             float weight2,
                             float weight3) {
        int i = (int) ((brightness0 >> 16 & 255) * weight0 +
                       (brightness1 >> 16 & 255) * weight1 +
                       (brightness2 >> 16 & 255) * weight2 +
                       (brightness3 >> 16 & 255) * weight3) & 255;
        int j = (int) ((brightness0 & 255) * weight0 +
                       (brightness1 & 255) * weight1 +
                       (brightness2 & 255) * weight2 +
                       (brightness3 & 255) * weight3) & 255;
        return i << 16 | j;
    }

    /**
     * @param shape      the array, of length 12, containing the shape bounds
     * @param shapeFlags the bit set to store the shape flags in. The first bit will be {@code true} if the face
     *                   should be offset, and the second if the face is less than a block in width and height.
     */
    public void calculate(BlockAndTintGetter level,
                          BlockState state,
                          BlockPos pos,
                          Direction direction,
                          float[] shape,
                          BitSet shapeFlags,
                          boolean shade) {
        BlockPos blockpos = shapeFlags.get(0) ? pos.relative(direction) : pos;
        ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        ModelBlockRenderer.Cache cache = ModelBlockRenderer.CACHE.get();
        mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[0]);
        BlockState blockstate = level.getBlockState(mutableBlockPos);
        int i = cache.getLightColor(blockstate, level, mutableBlockPos);
        float f = cache.getShadeBrightness(blockstate, level, mutableBlockPos);
        mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[1]);
        BlockState blockstate1 = level.getBlockState(mutableBlockPos);
        int j = cache.getLightColor(blockstate1, level, mutableBlockPos);
        float f1 = cache.getShadeBrightness(blockstate1, level, mutableBlockPos);
        mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[2]);
        BlockState blockstate2 = level.getBlockState(mutableBlockPos);
        int k = cache.getLightColor(blockstate2, level, mutableBlockPos);
        float f2 = cache.getShadeBrightness(blockstate2, level, mutableBlockPos);
        mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[3]);
        BlockState blockstate3 = level.getBlockState(mutableBlockPos);
        int l = cache.getLightColor(blockstate3, level, mutableBlockPos);
        float f3 = cache.getShadeBrightness(blockstate3, level, mutableBlockPos);
        BlockState blockstate4 = level.getBlockState(mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[0]).move(direction));
        boolean flag = !blockstate4.isViewBlocking(level, mutableBlockPos) || blockstate4.getLightBlock(level, mutableBlockPos) == 0;
        BlockState blockstate5 = level.getBlockState(mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[1]).move(direction));
        boolean flag1 = !blockstate5.isViewBlocking(level, mutableBlockPos) || blockstate5.getLightBlock(level, mutableBlockPos) == 0;
        BlockState blockstate6 = level.getBlockState(mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[2]).move(direction));
        boolean flag2 = !blockstate6.isViewBlocking(level, mutableBlockPos) || blockstate6.getLightBlock(level, mutableBlockPos) == 0;
        BlockState blockstate7 = level.getBlockState(mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[3]).move(direction));
        boolean flag3 = !blockstate7.isViewBlocking(level, mutableBlockPos) || blockstate7.getLightBlock(level, mutableBlockPos) == 0;
        float f4;
        int i1;
        if (!flag2 && !flag) {
            f4 = f;
            i1 = i;
        }
        else {
            mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
            BlockState blockstate8 = level.getBlockState(mutableBlockPos);
            f4 = cache.getShadeBrightness(blockstate8, level, mutableBlockPos);
            i1 = cache.getLightColor(blockstate8, level, mutableBlockPos);
        }
        float f5;
        int j1;
        if (!flag3 && !flag) {
            f5 = f;
            j1 = i;
        }
        else {
            mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
            BlockState blockstate10 = level.getBlockState(mutableBlockPos);
            f5 = cache.getShadeBrightness(blockstate10, level, mutableBlockPos);
            j1 = cache.getLightColor(blockstate10, level, mutableBlockPos);
        }
        float f6;
        int k1;
        if (!flag2 && !flag1) {
            f6 = f;
            k1 = i;
        }
        else {
            mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
            BlockState blockstate11 = level.getBlockState(mutableBlockPos);
            f6 = cache.getShadeBrightness(blockstate11, level, mutableBlockPos);
            k1 = cache.getLightColor(blockstate11, level, mutableBlockPos);
        }
        float f7;
        int l1;
        if (!flag3 && !flag1) {
            f7 = f;
            l1 = i;
        }
        else {
            mutableBlockPos.setWithOffset(blockpos, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
            BlockState blockstate12 = level.getBlockState(mutableBlockPos);
            f7 = cache.getShadeBrightness(blockstate12, level, mutableBlockPos);
            l1 = cache.getLightColor(blockstate12, level, mutableBlockPos);
        }
        int i3 = cache.getLightColor(state, level, pos);
        mutableBlockPos.setWithOffset(pos, direction);
        BlockState blockstate9 = level.getBlockState(mutableBlockPos);
        if (shapeFlags.get(0) || !blockstate9.isSolidRender(level, mutableBlockPos)) {
            i3 = cache.getLightColor(blockstate9, level, mutableBlockPos);
        }
        float f8 = shapeFlags.get(0) ?
                   cache.getShadeBrightness(level.getBlockState(blockpos), level, blockpos) :
                   cache.getShadeBrightness(level.getBlockState(pos), level, pos);
        ModelBlockRenderer.AmbientVertexRemap ambientVertexRemap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(direction);
        if (shapeFlags.get(1) && adjacencyInfo.doNonCubicWeight) {
            float f29 = (f3 + f + f5 + f8) * 0.25F;
            float f31 = (f2 + f + f4 + f8) * 0.25F;
            float f32 = (f2 + f1 + f6 + f8) * 0.25F;
            float f33 = (f3 + f1 + f7 + f8) * 0.25F;
            float f13 = shape[adjacencyInfo.vert0Weights[0].shape] * shape[adjacencyInfo.vert0Weights[1].shape];
            float f14 = shape[adjacencyInfo.vert0Weights[2].shape] * shape[adjacencyInfo.vert0Weights[3].shape];
            float f15 = shape[adjacencyInfo.vert0Weights[4].shape] * shape[adjacencyInfo.vert0Weights[5].shape];
            float f16 = shape[adjacencyInfo.vert0Weights[6].shape] * shape[adjacencyInfo.vert0Weights[7].shape];
            float f17 = shape[adjacencyInfo.vert1Weights[0].shape] * shape[adjacencyInfo.vert1Weights[1].shape];
            float f18 = shape[adjacencyInfo.vert1Weights[2].shape] * shape[adjacencyInfo.vert1Weights[3].shape];
            float f19 = shape[adjacencyInfo.vert1Weights[4].shape] * shape[adjacencyInfo.vert1Weights[5].shape];
            float f20 = shape[adjacencyInfo.vert1Weights[6].shape] * shape[adjacencyInfo.vert1Weights[7].shape];
            float f21 = shape[adjacencyInfo.vert2Weights[0].shape] * shape[adjacencyInfo.vert2Weights[1].shape];
            float f22 = shape[adjacencyInfo.vert2Weights[2].shape] * shape[adjacencyInfo.vert2Weights[3].shape];
            float f23 = shape[adjacencyInfo.vert2Weights[4].shape] * shape[adjacencyInfo.vert2Weights[5].shape];
            float f24 = shape[adjacencyInfo.vert2Weights[6].shape] * shape[adjacencyInfo.vert2Weights[7].shape];
            float f25 = shape[adjacencyInfo.vert3Weights[0].shape] * shape[adjacencyInfo.vert3Weights[1].shape];
            float f26 = shape[adjacencyInfo.vert3Weights[2].shape] * shape[adjacencyInfo.vert3Weights[3].shape];
            float f27 = shape[adjacencyInfo.vert3Weights[4].shape] * shape[adjacencyInfo.vert3Weights[5].shape];
            float f28 = shape[adjacencyInfo.vert3Weights[6].shape] * shape[adjacencyInfo.vert3Weights[7].shape];
            this.setBrightness(ambientVertexRemap.vert0, f29 * f13 + f31 * f14 + f32 * f15 + f33 * f16);
            this.setBrightness(ambientVertexRemap.vert1, f29 * f17 + f31 * f18 + f32 * f19 + f33 * f20);
            this.setBrightness(ambientVertexRemap.vert2, f29 * f21 + f31 * f22 + f32 * f23 + f33 * f24);
            this.setBrightness(ambientVertexRemap.vert3, f29 * f25 + f31 * f26 + f32 * f27 + f33 * f28);
            int i2 = blend(l, i, j1, i3);
            int j2 = blend(k, i, i1, i3);
            int k2 = blend(k, j, k1, i3);
            int l2 = blend(l, j, l1, i3);
            this.setLightmap(ambientVertexRemap.vert0, blend(i2, j2, k2, l2, f13, f14, f15, f16));
            this.setLightmap(ambientVertexRemap.vert1, blend(i2, j2, k2, l2, f17, f18, f19, f20));
            this.setLightmap(ambientVertexRemap.vert2, blend(i2, j2, k2, l2, f21, f22, f23, f24));
            this.setLightmap(ambientVertexRemap.vert3, blend(i2, j2, k2, l2, f25, f26, f27, f28));
        }
        else {
            float f9 = (f3 + f + f5 + f8) * 0.25F;
            float f10 = (f2 + f + f4 + f8) * 0.25F;
            float f11 = (f2 + f1 + f6 + f8) * 0.25F;
            float f12 = (f3 + f1 + f7 + f8) * 0.25F;
            this.setLightmap(ambientVertexRemap.vert0, blend(l, i, j1, i3));
            this.setLightmap(ambientVertexRemap.vert1, blend(k, i, i1, i3));
            this.setLightmap(ambientVertexRemap.vert2, blend(k, j, k1, i3));
            this.setLightmap(ambientVertexRemap.vert3, blend(l, j, l1, i3));
            this.setBrightness(ambientVertexRemap.vert0, f9);
            this.setBrightness(ambientVertexRemap.vert1, f10);
            this.setBrightness(ambientVertexRemap.vert2, f11);
            this.setBrightness(ambientVertexRemap.vert3, f12);
        }
        float f30 = level.getShade(direction, shade);
        this.brightness0 *= f30;
        this.brightness1 *= f30;
        this.brightness2 *= f30;
        this.brightness3 *= f30;
    }

    public void setBrightness(int index, float brightness) {
        switch (index) {
            case 0 -> this.brightness0 = brightness;
            case 1 -> this.brightness1 = brightness;
            case 2 -> this.brightness2 = brightness;
            case 3 -> this.brightness3 = brightness;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        }
    }

    public void setLightmap(int index, int lightmap) {
        switch (index) {
            case 0 -> this.lightmap0 = lightmap;
            case 1 -> this.lightmap1 = lightmap;
            case 2 -> this.lightmap2 = lightmap;
            case 3 -> this.lightmap3 = lightmap;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        }
    }
}
