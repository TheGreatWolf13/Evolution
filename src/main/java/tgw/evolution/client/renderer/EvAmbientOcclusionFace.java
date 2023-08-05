package tgw.evolution.client.renderer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

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
     * @param shape the array, of length 12, containing the shape bounds
     * @param flags the bit set to store the shape flags in. The first bit will be {@code true} if the face
     *              should be offset, and the second if the face is less than a block in width and height.
     */
    public void calculate(BlockAndTintGetter level,
                          BlockState state,
                          final int px, final int py, final int pz,
                          Direction direction,
                          float[] shape,
                          byte flags,
                          boolean shade) {
        boolean offset = (flags & 1) != 0;
        int x = px;
        int y = py;
        int z = pz;
        if (offset) {
            x += direction.getStepX();
            y += direction.getStepY();
            z += direction.getStepZ();
        }
        ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
        ModelBlockRenderer.Cache cache = ModelBlockRenderer.CACHE.get();
        Direction dir0 = adjacencyInfo.corners[0];
        int x0 = x + dir0.getStepX();
        int y0 = y + dir0.getStepY();
        int z0 = z + dir0.getStepZ();
        BlockState state0 = level.getBlockState_(x0, y0, z0);
        int color0 = cache.getLightColor_(state0, level, x0, y0, z0);
        float bright0 = cache.getShadeBrightness_(state0, level, x0, y0, z0);
        Direction dir1 = adjacencyInfo.corners[1];
        int x1 = x + dir1.getStepX();
        int y1 = y + dir1.getStepY();
        int z1 = z + dir1.getStepZ();
        BlockState state1 = level.getBlockState_(x1, y1, z1);
        int light1 = cache.getLightColor_(state1, level, x1, y1, z1);
        float bright1 = cache.getShadeBrightness_(state1, level, x1, y1, z1);
        Direction dir2 = adjacencyInfo.corners[2];
        int x2 = x + dir2.getStepX();
        int y2 = y + dir2.getStepY();
        int z2 = z + dir2.getStepZ();
        BlockState state2 = level.getBlockState_(x2, y2, z2);
        int color2 = cache.getLightColor_(state2, level, x2, y2, z2);
        float bright2 = cache.getShadeBrightness_(state2, level, x2, y2, z2);
        Direction dir3 = adjacencyInfo.corners[3];
        int x3 = x + dir3.getStepX();
        int y3 = y + dir3.getStepY();
        int z3 = z + dir3.getStepZ();
        BlockState state3 = level.getBlockState_(x3, y3, z3);
        int color3 = cache.getLightColor_(state3, level, x3, y3, z3);
        float bright3 = cache.getShadeBrightness_(state3, level, x3, y3, z3);
        int x4 = x0 + direction.getStepX();
        int y4 = y0 + direction.getStepY();
        int z4 = z0 + direction.getStepZ();
        BlockState state4 = level.getBlockState_(x4, y4, z4);
        boolean flag0 = !state4.isViewBlocking_(level, x4, y4, z4) || state4.getLightBlock_(level, x4, y4, z4) == 0;
        int x5 = x1 + direction.getStepX();
        int y5 = y1 + direction.getStepY();
        int z5 = z1 + direction.getStepZ();
        BlockState state5 = level.getBlockState_(x5, y5, z5);
        boolean flag1 = !state5.isViewBlocking_(level, x5, y5, z5) || state5.getLightBlock_(level, x5, y5, z5) == 0;
        x2 += direction.getStepX();
        y2 += direction.getStepY();
        z2 += direction.getStepZ();
        BlockState state6 = level.getBlockState_(x2, y2, z2);
        boolean flag2 = !state6.isViewBlocking_(level, x2, y2, z2) || state6.getLightBlock_(level, x2, y2, z2) == 0;
        x3 += direction.getStepX();
        y3 += direction.getStepY();
        z3 += direction.getStepZ();
        BlockState state7 = level.getBlockState_(x3, y3, z3);
        boolean flag3 = !state7.isViewBlocking_(level, x3, y3, z3) || state7.getLightBlock_(level, x3, y3, z3) == 0;
        float f0;
        int c0;
        if (!flag2 && !flag0) {
            f0 = bright0;
            c0 = color0;
        }
        else {
            int xx = x0 + dir2.getStepX();
            int yy = y0 + dir2.getStepY();
            int zz = z0 + dir2.getStepZ();
            BlockState state8 = level.getBlockState_(xx, yy, zz);
            f0 = cache.getShadeBrightness_(state8, level, xx, yy, zz);
            c0 = cache.getLightColor_(state8, level, xx, yy, zz);
        }
        float f1;
        int j1;
        if (!flag3 && !flag0) {
            f1 = bright0;
            j1 = color0;
        }
        else {
            int xx = x0 + dir3.getStepX();
            int yy = y0 + dir3.getStepY();
            int zz = z0 + dir3.getStepZ();
            BlockState blockstate10 = level.getBlockState_(xx, yy, zz);
            f1 = cache.getShadeBrightness_(blockstate10, level, xx, yy, zz);
            j1 = cache.getLightColor_(blockstate10, level, xx, yy, zz);
        }
        float f2;
        int k1;
        if (!flag2 && !flag1) {
            f2 = bright0;
            k1 = color0;
        }
        else {
            int xx = x1 + dir2.getStepX();
            int yy = y1 + dir2.getStepY();
            int zz = z1 + dir2.getStepZ();
            BlockState blockstate11 = level.getBlockState_(xx, yy, zz);
            f2 = cache.getShadeBrightness_(blockstate11, level, xx, yy, zz);
            k1 = cache.getLightColor_(blockstate11, level, xx, yy, zz);
        }
        float f3;
        int l1;
        if (!flag3 && !flag1) {
            f3 = bright0;
            l1 = color0;
        }
        else {
            int xx = x1 + dir3.getStepX();
            int yy = y1 + dir3.getStepY();
            int zz = z1 + dir3.getStepZ();
            BlockState blockstate12 = level.getBlockState_(xx, yy, zz);
            f3 = cache.getShadeBrightness_(blockstate12, level, xx, yy, zz);
            l1 = cache.getLightColor_(blockstate12, level, xx, yy, zz);
        }
        int i3 = cache.getLightColor_(state, level, px, py, pz);
        int mx = px + direction.getStepX();
        int my = py + direction.getStepY();
        int mz = pz + direction.getStepZ();
        BlockState blockstate9 = level.getBlockState_(mx, my, mz);
        if (offset || !blockstate9.isSolidRender_(level, mx, my, mz)) {
            i3 = cache.getLightColor_(blockstate9, level, mx, my, mz);
        }
        float f8 = offset ?
                   cache.getShadeBrightness_(level.getBlockState_(x, y, z), level, x, y, z) :
                   cache.getShadeBrightness_(level.getBlockState_(px, py, pz), level, px, py, pz);
        ModelBlockRenderer.AmbientVertexRemap ambientVertexRemap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(direction);
        if ((flags & 2) != 0 && adjacencyInfo.doNonCubicWeight) {
            float f29 = (bright3 + bright0 + f1 + f8) * 0.25F;
            float f31 = (bright2 + bright0 + f0 + f8) * 0.25F;
            float f32 = (bright2 + bright1 + f2 + f8) * 0.25F;
            float f33 = (bright3 + bright1 + f3 + f8) * 0.25F;
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
            int j2 = blend(color2, color0, c0, i3);
            int k2 = blend(color2, light1, k1, i3);
            int l2 = blend(color3, light1, l1, i3);
            this.setLightmap(ambientVertexRemap.vert0, blend(i2, j2, k2, l2, f13, f14, f15, f16));
            this.setLightmap(ambientVertexRemap.vert1, blend(i2, j2, k2, l2, f17, f18, f19, f20));
            this.setLightmap(ambientVertexRemap.vert2, blend(i2, j2, k2, l2, f21, f22, f23, f24));
            this.setLightmap(ambientVertexRemap.vert3, blend(i2, j2, k2, l2, f25, f26, f27, f28));
        }
        else {
            float f9 = (bright3 + bright0 + f1 + f8) * 0.25F;
            float f10 = (bright2 + bright0 + f0 + f8) * 0.25F;
            float f11 = (bright2 + bright1 + f2 + f8) * 0.25F;
            float f12 = (bright3 + bright1 + f3 + f8) * 0.25F;
            this.setLightmap(ambientVertexRemap.vert0, blend(color3, color0, j1, i3));
            this.setLightmap(ambientVertexRemap.vert1, blend(color2, color0, c0, i3));
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
