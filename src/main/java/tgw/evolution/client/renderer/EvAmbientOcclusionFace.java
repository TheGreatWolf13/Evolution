package tgw.evolution.client.renderer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EvAmbientOcclusionFace {

    public float brightness0;
    public float brightness1;
    public float brightness2;
    public float brightness3;
    public int lightmap0;
    public int lightmap1;
    public int lightmap2;
    public int lightmap3;

    private static int blend(int lightColor0, int lightColor1, int lightColor2, int lightColor3) {
        if (lightColor0 == -1) {
            if (lightColor1 == -1) {
                if (lightColor2 == -1) {
                    return lightColor3;
                }
                return blend2(lightColor2, lightColor3);
            }
            if (lightColor2 == -1) {
                return blend2(lightColor1, lightColor3);
            }
            return blend3(lightColor1, lightColor2, lightColor3);
        }
        if (lightColor1 == -1) {
            if (lightColor2 == -1) {
                return blend2(lightColor0, lightColor3);
            }
            return blend3(lightColor0, lightColor2, lightColor3);
        }
        if (lightColor2 == -1) {
            return blend3(lightColor0, lightColor1, lightColor3);
        }
        return (lightColor0 & 0xf) + (lightColor1 & 0xf) + (lightColor2 & 0xf) + (lightColor3 & 0xf) >> 2 & 0xf |
               ((lightColor0 & 0x10) + (lightColor1 & 0x10) + (lightColor2 & 0x10) + (lightColor3 & 0x10) >= 0x20 ? 0x10 : 0) |
               (lightColor0 & 0x1e0) + (lightColor1 & 0x1e0) + (lightColor2 & 0x1e0) + (lightColor3 & 0x1e0) >> 2 & 0x1e0 |
               ((lightColor0 & 0x200) + (lightColor1 & 0x200) + (lightColor2 & 0x200) + (lightColor3 & 0x200) >= 0x400 ? 0x200 : 0) |
               (lightColor0 & 0xf_0000) + (lightColor1 & 0xf_0000) + (lightColor2 & 0xf_0000) + (lightColor3 & 0xf_0000) >> 2 & 0xf_0000 |
               (lightColor0 & 0xf0_0000) + (lightColor1 & 0xf0_0000) + (lightColor2 & 0xf0_0000) + (lightColor3 & 0xf0_0000) >> 2 & 0xf0_0000 |
               ((lightColor0 & 0x100_0000) + (lightColor1 & 0x100_0000) + (lightColor2 & 0x100_0000) + (lightColor3 & 0x100_0000) >= 0x200_0000 ? 0x100_0000 : 0);
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

    private static int blend2(int lightColor0, int lightColor1) {
        return (lightColor0 & 0xf) + (lightColor1 & 0xf) >> 1 & 0xf |
               ((lightColor0 & 0x10) != 0 || (lightColor1 & 0x10) != 0 ? 0x10 : 0) |
               (lightColor0 & 0x1e0) + (lightColor1 & 0x1e0) >> 1 & 0x1e0 |
               ((lightColor0 & 0x200) != 0 || (lightColor1 & 0x200) != 0 ? 0x200 : 0) |
               (lightColor0 & 0xf_0000) + (lightColor1 & 0xf_0000) >> 1 & 0xf_0000 |
               (lightColor0 & 0xf0_0000) + (lightColor1 & 0xf0_0000) >> 1 & 0xf0_0000 |
               ((lightColor0 & 0x100_0000) != 0 || (lightColor1 & 0x100_0000) != 0 ? 0x100_0000 : 0);
    }

    private static int blend3(int lightColor0, int lightColor1, int lightColor2) {
        return ((lightColor0 & 0xf) + (lightColor1 & 0xf) + (lightColor2 & 0xf)) / 3 & 0xf |
               ((lightColor0 & 0x10) + (lightColor1 & 0x10) + (lightColor2 & 0x10) >= 0x20 ? 0x10 : 0) |
               ((lightColor0 & 0x1e0) + (lightColor1 & 0x1e0) + (lightColor2 & 0x1e0)) / 3 & 0x1e0 |
               ((lightColor0 & 0x200) + (lightColor1 & 0x200) + (lightColor2 & 0x200) >= 0x400 ? 0x200 : 0) |
               ((lightColor0 & 0xf_0000) + (lightColor1 & 0xf_0000) + (lightColor2 & 0xf_0000)) / 3 & 0xf_0000 |
               ((lightColor0 & 0xf0_0000) + (lightColor1 & 0xf0_0000) + (lightColor2 & 0xf0_0000)) / 3 & 0xf0_0000 |
               ((lightColor0 & 0x100_0000) + (lightColor1 & 0x100_0000) + (lightColor2 & 0x100_0000) >= 0x200_0000 ? 0x100_0000 : 0);

    }

    /**
     * @param shape the array, of length 12, containing the shape bounds. Can only be null if the second flag is false.
     * @param flags the bit set to store the shape flags in. The first bit will be {@code true} if the face
     *              should be offset, and the second if the face is less than a block in width and height.
     */
    public void calculate(BlockAndTintGetter level, BlockState state, final int px, final int py, final int pz, Direction direction, float @Nullable [] shape, byte flags, boolean shadeDirection) {
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
        Direction dirE = adjacencyInfo.corners[0];
        int xE = x + dirE.getStepX();
        int yE = y + dirE.getStepY();
        int zE = z + dirE.getStepZ();
        BlockState stateE = level.getBlockState_(xE, yE, zE);
        int colorE = cache.getLightColor_(stateE, level, xE, yE, zE);
        float brightE = cache.getShadeBrightness_(stateE, level, xE, yE, zE);
        Direction dirW = adjacencyInfo.corners[1];
        int xW = x + dirW.getStepX();
        int yW = y + dirW.getStepY();
        int zW = z + dirW.getStepZ();
        BlockState stateW = level.getBlockState_(xW, yW, zW);
        int colorW = cache.getLightColor_(stateW, level, xW, yW, zW);
        float brightW = cache.getShadeBrightness_(stateW, level, xW, yW, zW);
        Direction dirN = adjacencyInfo.corners[2];
        int xN = x + dirN.getStepX();
        int yN = y + dirN.getStepY();
        int zN = z + dirN.getStepZ();
        BlockState stateN = level.getBlockState_(xN, yN, zN);
        int colorN = cache.getLightColor_(stateN, level, xN, yN, zN);
        float brightN = cache.getShadeBrightness_(stateN, level, xN, yN, zN);
        Direction dirS = adjacencyInfo.corners[3];
        int xS = x + dirS.getStepX();
        int yS = y + dirS.getStepY();
        int zS = z + dirS.getStepZ();
        BlockState stateS = level.getBlockState_(xS, yS, zS);
        int colorS = cache.getLightColor_(stateS, level, xS, yS, zS);
        float brightS = cache.getShadeBrightness_(stateS, level, xS, yS, zS);
        boolean freeE;
        boolean freeW;
        boolean freeN;
        boolean freeS;
        if (offset) {
            freeE = !stateE.isViewBlocking_(level, xE, yE, zE) || stateE.getLightBlock_(level, xE, yE, zE) == 0;
            freeW = !stateW.isViewBlocking_(level, xW, yW, zW) || stateW.getLightBlock_(level, xW, yW, zW) == 0;
            freeN = !stateN.isViewBlocking_(level, xN, yN, zN) || stateN.getLightBlock_(level, xN, yN, zN) == 0;
            freeS = !stateS.isViewBlocking_(level, xS, yS, zS) || stateS.getLightBlock_(level, xS, yS, zS) == 0;
        }
        else {
            int xEU = xE + direction.getStepX();
            int yEU = yE + direction.getStepY();
            int zEU = zE + direction.getStepZ();
            BlockState stateEU = level.getBlockState_(xEU, yEU, zEU);
            freeE = !stateEU.isViewBlocking_(level, xEU, yEU, zEU) || stateEU.getLightBlock_(level, xEU, yEU, zEU) == 0;
            int xWU = xW + direction.getStepX();
            int yWU = yW + direction.getStepY();
            int zWU = zW + direction.getStepZ();
            BlockState stateWU = level.getBlockState_(xWU, yWU, zWU);
            freeW = !stateWU.isViewBlocking_(level, xWU, yWU, zWU) || stateWU.getLightBlock_(level, xWU, yWU, zWU) == 0;
            xN += direction.getStepX();
            yN += direction.getStepY();
            zN += direction.getStepZ();
            BlockState stateNU = level.getBlockState_(xN, yN, zN);
            freeN = !stateNU.isViewBlocking_(level, xN, yN, zN) || stateNU.getLightBlock_(level, xN, yN, zN) == 0;
            xS += direction.getStepX();
            yS += direction.getStepY();
            zS += direction.getStepZ();
            BlockState stateSU = level.getBlockState_(xS, yS, zS);
            freeS = !stateSU.isViewBlocking_(level, xS, yS, zS) || stateSU.getLightBlock_(level, xS, yS, zS) == 0;
        }
        float brightNE;
        int colorNE;
        if (!freeN && !freeE) {
            brightNE = brightE;
            colorNE = colorE;
        }
        else {
            int xNE = xE + dirN.getStepX();
            int yNE = yE + dirN.getStepY();
            int zNE = zE + dirN.getStepZ();
            BlockState stateNE = level.getBlockState_(xNE, yNE, zNE);
            brightNE = cache.getShadeBrightness_(stateNE, level, xNE, yNE, zNE);
            colorNE = cache.getLightColor_(stateNE, level, xNE, yNE, zNE);
        }
        float brightSE;
        int colorSE;
        if (!freeS && !freeE) {
            brightSE = brightE;
            colorSE = colorE;
        }
        else {
            int xSE = xE + dirS.getStepX();
            int ySE = yE + dirS.getStepY();
            int zSE = zE + dirS.getStepZ();
            BlockState stateSE = level.getBlockState_(xSE, ySE, zSE);
            brightSE = cache.getShadeBrightness_(stateSE, level, xSE, ySE, zSE);
            colorSE = cache.getLightColor_(stateSE, level, xSE, ySE, zSE);
        }
        float brightNW;
        int colorNW;
        if (!freeN && !freeW) {
            brightNW = brightW;
            colorNW = colorW;
        }
        else {
            int xNW = xW + dirN.getStepX();
            int yNW = yW + dirN.getStepY();
            int zNW = zW + dirN.getStepZ();
            BlockState stateNW = level.getBlockState_(xNW, yNW, zNW);
            brightNW = cache.getShadeBrightness_(stateNW, level, xNW, yNW, zNW);
            colorNW = cache.getLightColor_(stateNW, level, xNW, yNW, zNW);
        }
        float brightSW;
        int colorSW;
        if (!freeS && !freeW) {
            brightSW = brightW;
            colorSW = colorW;
        }
        else {
            int xSW = xW + dirS.getStepX();
            int ySW = yW + dirS.getStepY();
            int zSW = zW + dirS.getStepZ();
            BlockState stateSW = level.getBlockState_(xSW, ySW, zSW);
            brightSW = cache.getShadeBrightness_(stateSW, level, xSW, ySW, zSW);
            colorSW = cache.getLightColor_(stateSW, level, xSW, ySW, zSW);
        }
        int color;
        if (offset /*|| !stateU.isSolidRender_(level, xU, yU, zU)*/) { //Disable second condition to fix wrong lighting on carpets, but maybe it's necessary for some other blocks?
            int xU = px + direction.getStepX();
            int yU = py + direction.getStepY();
            int zU = pz + direction.getStepZ();
            BlockState stateU = level.getBlockState_(xU, yU, zU);
            color = cache.getLightColor_(stateU, level, xU, yU, zU);
        }
        else {
            color = cache.getLightColor_(state, level, px, py, pz);
        }
        float bright = offset ?
                       cache.getShadeBrightness_(level.getBlockState_(x, y, z), level, x, y, z) :
                       cache.getShadeBrightness_(level.getBlockState_(px, py, pz), level, px, py, pz);
        AmbientVertexRemap remap = AmbientVertexRemap.VALUES[direction.ordinal()];
        if ((flags & 2) != 0 && adjacencyInfo.doNonCubicWeight) {
            assert shape != null : "Shape can only be null if the second flag is false";
            float f29 = (brightS + brightE + brightSE + bright) * 0.25F;
            float f31 = (brightN + brightE + brightNE + bright) * 0.25F;
            float f32 = (brightN + brightW + brightNW + bright) * 0.25F;
            float f33 = (brightS + brightW + brightSW + bright) * 0.25F;
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
            this.setBrightness(remap.vert0, f29 * f13 + f31 * f14 + f32 * f15 + f33 * f16);
            this.setBrightness(remap.vert1, f29 * f17 + f31 * f18 + f32 * f19 + f33 * f20);
            this.setBrightness(remap.vert2, f29 * f21 + f31 * f22 + f32 * f23 + f33 * f24);
            this.setBrightness(remap.vert3, f29 * f25 + f31 * f26 + f32 * f27 + f33 * f28);
            int blendColorSE = blend(colorS, colorE, colorSE, color);
            int blendColorNE = blend(colorN, colorE, colorNE, color);
            int blendColorNW = blend(colorN, colorW, colorNW, color);
            int blendColorSW = blend(colorS, colorW, colorSW, color);
            this.setLightmap(remap.vert0, blend(blendColorSE, blendColorNE, blendColorNW, blendColorSW, f13, f14, f15, f16));
            this.setLightmap(remap.vert1, blend(blendColorSE, blendColorNE, blendColorNW, blendColorSW, f17, f18, f19, f20));
            this.setLightmap(remap.vert2, blend(blendColorSE, blendColorNE, blendColorNW, blendColorSW, f21, f22, f23, f24));
            this.setLightmap(remap.vert3, blend(blendColorSE, blendColorNE, blendColorNW, blendColorSW, f25, f26, f27, f28));
        }
        else {
            float combBrightSE = (brightS + brightE + brightSE + bright) * 0.25F;
            float combBrightNE = (brightN + brightE + brightNE + bright) * 0.25F;
            float combBrightNW = (brightN + brightW + brightNW + bright) * 0.25F;
            float combBrightSW = (brightS + brightW + brightSW + bright) * 0.25F;
            this.setLightmap(remap.vert0, blend(colorS, colorE, colorSE, color));
            this.setLightmap(remap.vert1, blend(colorN, colorE, colorNE, color));
            this.setLightmap(remap.vert2, blend(colorN, colorW, colorNW, color));
            this.setLightmap(remap.vert3, blend(colorS, colorW, colorSW, color));
            this.setBrightness(remap.vert0, combBrightSE);
            this.setBrightness(remap.vert1, combBrightNE);
            this.setBrightness(remap.vert2, combBrightNW);
            this.setBrightness(remap.vert3, combBrightSW);
        }
        float shade = level.getShade(direction, shadeDirection);
        this.brightness0 *= shade;
        this.brightness1 *= shade;
        this.brightness2 *= shade;
        this.brightness3 *= shade;
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

    enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        static final AmbientVertexRemap[] VALUES = values();
        public final int vert0;
        public final int vert1;
        public final int vert2;
        public final int vert3;

        AmbientVertexRemap(int j, int k, int l, int m) {
            this.vert0 = j;
            this.vert1 = k;
            this.vert2 = l;
            this.vert3 = m;
        }
    }
}
