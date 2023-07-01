package tgw.evolution.client.renderer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class EvAmbientOcclusionFace {

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
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
     * @param shape the array, of length 12, containing the shape bounds
     * @param flags the bit set to store the shape flags in. The first bit will be {@code true} if the face
     *              should be offset, and the second if the face is less than a block in width and height.
     */
    public void calculate(BlockAndTintGetter level,
                          BlockState state,
                          BlockPos pos,
                          Direction direction,
                          float[] shape,
                          byte flags,
                          boolean shade) {
        boolean offset = (flags & 1) != 0;
        BlockPos blockpos = offset ? pos.relative(direction) : pos;
        ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
        ModelBlockRenderer.Cache cache = ModelBlockRenderer.CACHE.get();
        BlockState state0 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[0]));
        int color0 = cache.getLightColor(state0, level, this.mutablePos);
        float bright0 = cache.getShadeBrightness(state0, level, this.mutablePos);
        BlockState state1 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[1]));
        int light1 = cache.getLightColor(state1, level, this.mutablePos);
        float bright1 = cache.getShadeBrightness(state1, level, this.mutablePos);
        BlockState state2 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[2]));
        int color2 = cache.getLightColor(state2, level, this.mutablePos);
        float bright2 = cache.getShadeBrightness(state2, level, this.mutablePos);
        BlockState state3 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[3]));
        int color3 = cache.getLightColor(state3, level, this.mutablePos);
        float bright3 = cache.getShadeBrightness(state3, level, this.mutablePos);
        BlockState state4 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[0]).move(direction));
        boolean flag0 = !state4.isViewBlocking(level, this.mutablePos) || state4.getLightBlock(level, this.mutablePos) == 0;
        BlockState state5 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[1]).move(direction));
        boolean flag1 = !state5.isViewBlocking(level, this.mutablePos) || state5.getLightBlock(level, this.mutablePos) == 0;
        BlockState state6 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[2]).move(direction));
        boolean flag2 = !state6.isViewBlocking(level, this.mutablePos) || state6.getLightBlock(level, this.mutablePos) == 0;
        BlockState state7 = level.getBlockState(this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[3]).move(direction));
        boolean flag3 = !state7.isViewBlocking(level, this.mutablePos) || state7.getLightBlock(level, this.mutablePos) == 0;
        float f4;
        int i1;
        if (!flag2 && !flag0) {
            f4 = bright0;
            i1 = color0;
        }
        else {
            this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
            BlockState state8 = level.getBlockState(this.mutablePos);
            f4 = cache.getShadeBrightness(state8, level, this.mutablePos);
            i1 = cache.getLightColor(state8, level, this.mutablePos);
        }
        float f5;
        int j1;
        if (!flag3 && !flag0) {
            f5 = bright0;
            j1 = color0;
        }
        else {
            this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
            BlockState blockstate10 = level.getBlockState(this.mutablePos);
            f5 = cache.getShadeBrightness(blockstate10, level, this.mutablePos);
            j1 = cache.getLightColor(blockstate10, level, this.mutablePos);
        }
        float f6;
        int k1;
        if (!flag2 && !flag1) {
            f6 = bright0;
            k1 = color0;
        }
        else {
            this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
            BlockState blockstate11 = level.getBlockState(this.mutablePos);
            f6 = cache.getShadeBrightness(blockstate11, level, this.mutablePos);
            k1 = cache.getLightColor(blockstate11, level, this.mutablePos);
        }
        float f7;
        int l1;
        if (!flag3 && !flag1) {
            f7 = bright0;
            l1 = color0;
        }
        else {
            this.mutablePos.setWithOffset(blockpos, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
            BlockState blockstate12 = level.getBlockState(this.mutablePos);
            f7 = cache.getShadeBrightness(blockstate12, level, this.mutablePos);
            l1 = cache.getLightColor(blockstate12, level, this.mutablePos);
        }
        int i3 = cache.getLightColor(state, level, pos);
        this.mutablePos.setWithOffset(pos, direction);
        BlockState blockstate9 = level.getBlockState(this.mutablePos);
        if (offset || !blockstate9.isSolidRender(level, this.mutablePos)) {
            i3 = cache.getLightColor(blockstate9, level, this.mutablePos);
        }
        float f8 = offset ?
                   cache.getShadeBrightness(level.getBlockState(blockpos), level, blockpos) :
                   cache.getShadeBrightness(level.getBlockState(pos), level, pos);
        ModelBlockRenderer.AmbientVertexRemap ambientVertexRemap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(direction);
        if ((flags & 2) != 0 && adjacencyInfo.doNonCubicWeight) {
            float f29 = (bright3 + bright0 + f5 + f8) * 0.25F;
            float f31 = (bright2 + bright0 + f4 + f8) * 0.25F;
            float f32 = (bright2 + bright1 + f6 + f8) * 0.25F;
            float f33 = (bright3 + bright1 + f7 + f8) * 0.25F;
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
            int i2 = blend(color3, color0, j1, i3);
            int j2 = blend(color2, color0, i1, i3);
            int k2 = blend(color2, light1, k1, i3);
            int l2 = blend(color3, light1, l1, i3);
            this.setLightmap(ambientVertexRemap.vert0, blend(i2, j2, k2, l2, f13, f14, f15, f16));
            this.setLightmap(ambientVertexRemap.vert1, blend(i2, j2, k2, l2, f17, f18, f19, f20));
            this.setLightmap(ambientVertexRemap.vert2, blend(i2, j2, k2, l2, f21, f22, f23, f24));
            this.setLightmap(ambientVertexRemap.vert3, blend(i2, j2, k2, l2, f25, f26, f27, f28));
        }
        else {
            float f9 = (bright3 + bright0 + f5 + f8) * 0.25F;
            float f10 = (bright2 + bright0 + f4 + f8) * 0.25F;
            float f11 = (bright2 + bright1 + f6 + f8) * 0.25F;
            float f12 = (bright3 + bright1 + f7 + f8) * 0.25F;
            this.setLightmap(ambientVertexRemap.vert0, blend(color3, color0, j1, i3));
            this.setLightmap(ambientVertexRemap.vert1, blend(color2, color0, i1, i3));
            this.setLightmap(ambientVertexRemap.vert2, blend(color2, light1, k1, i3));
            this.setLightmap(ambientVertexRemap.vert3, blend(color3, light1, l1, i3));
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
