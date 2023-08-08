package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;

@Mixin(LiquidBlockRenderer.class)
public abstract class Mixin_M_LiquidBlockRenderer {

    @Unique private static final ThreadLocal<Vec3d> VEC = ThreadLocal.withInitial(Vec3d::new);
    @Shadow @Final private TextureAtlasSprite[] lavaIcons;
    @Shadow @Final private TextureAtlasSprite[] waterIcons;
    @Shadow private TextureAtlasSprite waterOverlay;

    private static long addWeightedHeight(long a, float f) {
        if (f >= 0.8F) {
            float f0 = Float.intBitsToFloat((int) (a >> 32));
            float f1 = Float.intBitsToFloat((int) a);
            f0 += f * 10.0F;
            f1 += 10.0F;
            return (long) Float.floatToIntBits(f0) << 32 | Float.floatToIntBits(f1);
        }
        if (f >= 0.0F) {
            float f0 = Float.intBitsToFloat((int) (a >> 32));
            float f1 = Float.intBitsToFloat((int) a);
            f0 += f;
            ++f1;
            return (long) Float.floatToIntBits(f0) << 32 | Float.floatToIntBits(f1);
        }
        return a;
    }

    @Unique
    private static float calculateAverageHeight(BlockGetter level, Fluid fluid, float ownHeight, float heightAtZ, float heightAtX, int x, int y, int z) {
        if (heightAtX < 1.0F && heightAtZ < 1.0F) {
            long a = 0;
            if (heightAtX > 0 || heightAtZ > 0) {
                float i = getHeight(level, fluid, x, y, z);
                if (i >= 1.0F) {
                    return 1.0F;
                }
                a = addWeightedHeight(a, i);
            }
            a = addWeightedHeight(a, ownHeight);
            a = addWeightedHeight(a, heightAtX);
            a = addWeightedHeight(a, heightAtZ);
            return Float.intBitsToFloat((int) (a >> 32)) / Float.intBitsToFloat((int) a);
        }
        return 1.0F;
    }

    @Unique
    private static float getHeight(BlockGetter level, Fluid fluid, int x, int y, int z, BlockState state, FluidState fluidState) {
        if (fluid.isSame(fluidState.getType())) {
            return fluid.isSame(level.getFluidState_(x, y + 1, z).getType()) ? 1.0F : fluidState.getOwnHeight();
        }
        return !state.getMaterial().isSolid() ? 0.0F : -1.0F;
    }

    @Unique
    private static float getHeight(BlockGetter level, Fluid fluid, int x, int y, int z) {
        return getHeight(level, fluid, x, y, z, level.getBlockState_(x, y, z), level.getFluidState_(x, y, z));
    }

    @Unique
    private static int getLightColor(BlockAndTintGetter level, int x, int y, int z) {
        int light = EvLevelRenderer.getLightColor(level, x, y, z);
        int lightAbove = EvLevelRenderer.getLightColor(level, x, y + 1, z);
        int k = light & 255;
        int l = lightAbove & 255;
        int i1 = light >> 16 & 255;
        int j1 = lightAbove >> 16 & 255;
        return Math.max(k, l) | Math.max(i1, j1) << 16;
    }

    @Overwrite
    private static boolean isFaceOccludedByNeighbor(BlockGetter level, BlockPos pos, Direction direction, float height, BlockState state) {
        return isFaceOccludedByState(level, direction, height, pos.getX() + direction.getStepX(), pos.getY() + direction.getStepY(), pos.getZ() + direction.getStepZ(), state);
    }

    @Overwrite
    private static boolean isFaceOccludedBySelf(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        return isFaceOccludedByState(level, direction.getOpposite(), 1.0F, pos.getX(), pos.getY(), pos.getZ(), state);
    }

    @Overwrite
    @DeleteMethod
    private static boolean isFaceOccludedByState(BlockGetter blockGetter, Direction direction, float f, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Unique
    private static boolean isFaceOccludedByState(BlockGetter level, Direction direction, float height, int x, int y, int z, BlockState state) {
        if (state.canOcclude()) {
            VoxelShape occlusionShape = state.getOcclusionShape_(level, x, y, z);
            VoxelShape voxelShape = Shapes.box(0, 0, 0, 1, height, 1);
            return Shapes.blockOccudes(voxelShape, occlusionShape, direction);
        }
        return false;
    }

    @Shadow
    private static boolean isNeighborSameFluid(FluidState fluidState, FluidState fluidState2) {
        throw new AbstractMethodError();
    }

    @Unique
    private static boolean shouldRenderBackwardUpFace(Fluid fluid, BlockGetter level, int x, int y, int z) {
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                int offX = x + dx;
                int offZ = z + dz;
                FluidState fluidState = level.getFluidState_(offX, y, offZ);
                if (!fluidState.getType().isSame(fluid) && !level.getBlockState_(offX, y, offZ).isSolidRender_(level, offX, y, offZ)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Shadow
    public static boolean shouldRenderFace(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    private void addWeightedHeight(float[] fs, float f) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    private float calculateAverageHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, float f, float g, float h, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer
     */
    @Overwrite
    @DeleteMethod
    private int getLightColor(BlockAndTintGetter level, BlockPos pos) {
        throw new AbstractMethodError();
    }

    @Overwrite
    public boolean tesselate(BlockAndTintGetter level, BlockPos pos, VertexConsumer builder, BlockState state, FluidState fluidState) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        FluidState fluidBelow = level.getFluidState_(x, y - 1, z);
        FluidState fluidAbove = level.getFluidState_(x, y + 1, z);
        FluidState fluidNorth = level.getFluidState_(x, y, z - 1);
        FluidState fluidSouth = level.getFluidState_(x, y, z + 1);
        FluidState fluidWest = level.getFluidState_(x - 1, y, z);
        FluidState fluidEast = level.getFluidState_(x + 1, y, z);
        boolean renderU = !isNeighborSameFluid(fluidState, fluidAbove);
        boolean renderD = shouldRenderFace(level, pos, fluidState, state, Direction.DOWN, fluidBelow) && !isFaceOccludedByNeighbor(level, pos, Direction.DOWN, 0.888_888_9F, level.getBlockState_(x, y - 1, z));
        boolean renderN = shouldRenderFace(level, pos, fluidState, state, Direction.NORTH, fluidNorth);
        boolean renderS = shouldRenderFace(level, pos, fluidState, state, Direction.SOUTH, fluidSouth);
        boolean renderW = shouldRenderFace(level, pos, fluidState, state, Direction.WEST, fluidWest);
        boolean renderE = shouldRenderFace(level, pos, fluidState, state, Direction.EAST, fluidEast);
        if (!renderU && !renderD && !renderE && !renderW && !renderN && !renderS) {
            return false;
        }
        boolean rendered = false;
        float shadeD = level.getShade(Direction.DOWN, true);
        float shadeU = level.getShade(Direction.UP, true);
        Fluid fluid = fluidState.getType();
        float height = getHeight(level, fluid, x, y, z, state, fluidState);
        float heightNE;
        float heightNW;
        float heightSE;
        float heightSW;
        if (height >= 1.0F) {
            heightNE = 1.0F;
            heightNW = 1.0F;
            heightSE = 1.0F;
            heightSW = 1.0F;
        }
        else {
            float heightN = getHeight(level, fluid, x, y, z - 1, level.getBlockState_(x, y, z - 1), fluidNorth);
            float heightS = getHeight(level, fluid, x, y, z + 1, level.getBlockState_(x, y, z + 1), fluidSouth);
            float heightE = getHeight(level, fluid, x + 1, y, z, level.getBlockState_(x + 1, y, z), fluidEast);
            float heightW = getHeight(level, fluid, x - 1, y, z, level.getBlockState_(x - 1, y, z), fluidWest);
            heightNE = calculateAverageHeight(level, fluid, height, heightN, heightE, x + 1, y, z - 1);
            heightNW = calculateAverageHeight(level, fluid, height, heightN, heightW, x - 1, y, z - 1);
            heightSE = calculateAverageHeight(level, fluid, height, heightS, heightE, x + 1, y, z + 1);
            heightSW = calculateAverageHeight(level, fluid, height, heightS, heightW, x - 1, y, z + 1);
        }
        boolean isLava = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] sprites = isLava ? this.lavaIcons : this.waterIcons;
        double vx = x & 15;
        double vy = y & 15;
        double vz = z & 15;
        int color = isLava ? 0xff_ffff : BiomeColors.getAverageWaterColor(level, pos);
        float colorR = (color >> 16 & 255) / 255.0F;
        float colorG = (color >> 8 & 255) / 255.0F;
        float colorB = (color & 255) / 255.0F;
        if (renderU && !isFaceOccludedByNeighbor(level, pos, Direction.UP, Math.min(Math.min(heightNW, heightSW), Math.min(heightSE, heightNE)), level.getBlockState_(x, y + 1, z))) {
            rendered = true;
            heightNW -= 0.001F;
            heightSW -= 0.001F;
            heightSE -= 0.001F;
            heightNE -= 0.001F;
            Vec3 flow = fluid.getFlow(level, x, y, z, fluidState, VEC.get().set(0, 0, 0));
            TextureAtlasSprite sprite;
            float u0;
            float u1;
            float u2;
            float u3;
            float v0;
            float v1;
            float v2;
            float v3;
            if (flow.x == 0 && flow.z == 0) {
                sprite = sprites[0];
                u0 = sprite.getU(0);
                u1 = u0;
                u2 = sprite.getU(16);
                u3 = u2;
                v0 = sprite.getV(0);
                v1 = sprite.getV(16);
                v2 = v1;
                v3 = v0;
            }
            else {
                sprite = sprites[1];
                float angle = (float) Mth.atan2(flow.z, flow.x) - Mth.HALF_PI;
                float sin = Mth.sin(angle) * 0.25F;
                float cos = Mth.cos(angle) * 0.25F;
                float f0 = 8.0F + (-cos - sin) * 16.0F;
                float f1 = 8.0F + (-cos + sin) * 16.0F;
                float f2 = 8.0F + (cos + sin) * 16.0F;
                float f3 = 8.0F + (cos - sin) * 16.0F;
                u0 = sprite.getU(f0);
                u1 = sprite.getU(f1);
                u2 = sprite.getU(f2);
                u3 = sprite.getU(f3);
                v0 = sprite.getV(f1);
                v1 = sprite.getV(f2);
                v2 = sprite.getV(f3);
                v3 = sprite.getV(f0);
            }
            float uAverage = (u0 + u1 + u2 + u3) * 0.25f;
            float vAverage = (v0 + v1 + v2 + v3) * 0.25f;
            float relUSize = sprites[0].getWidth() / (sprites[0].getU1() - sprites[0].getU0());
            float relVSize = sprites[0].getHeight() / (sprites[0].getV1() - sprites[0].getV0());
            float lerp = 4.0F / Math.max(relVSize, relUSize);
            u0 = Mth.lerp(lerp, u0, uAverage);
            u1 = Mth.lerp(lerp, u1, uAverage);
            u2 = Mth.lerp(lerp, u2, uAverage);
            u3 = Mth.lerp(lerp, u3, uAverage);
            v0 = Mth.lerp(lerp, v0, vAverage);
            v1 = Mth.lerp(lerp, v1, vAverage);
            v2 = Mth.lerp(lerp, v2, vAverage);
            v3 = Mth.lerp(lerp, v3, vAverage);
            int lightmapUp = getLightColor(level, x, y, z);
            float r = shadeU * colorR;
            float g = shadeU * colorG;
            float b = shadeU * colorB;
            this.vertex(builder, vx, vy + heightNW, vz, r, g, b, u0, v0, lightmapUp);
            this.vertex(builder, vx, vy + heightSW, vz + 1, r, g, b, u1, v1, lightmapUp);
            this.vertex(builder, vx + 1, vy + heightSE, vz + 1, r, g, b, u2, v2, lightmapUp);
            this.vertex(builder, vx + 1, vy + heightNE, vz, r, g, b, u3, v3, lightmapUp);
            if (shouldRenderBackwardUpFace(fluid, level, x, y + 1, z)) {
                this.vertex(builder, vx, vy + heightNW, vz, r, g, b, u0, v0, lightmapUp);
                this.vertex(builder, vx + 1, vy + heightNE, vz, r, g, b, u3, v3, lightmapUp);
                this.vertex(builder, vx + 1, vy + heightSE, vz + 1, r, g, b, u2, v2, lightmapUp);
                this.vertex(builder, vx, vy + heightSW, vz + 1, r, g, b, u1, v1, lightmapUp);
            }
        }
        float yOffD = renderD ? 0.001F : 0;
        if (renderD) {
            float u0 = sprites[0].getU0();
            float u1 = sprites[0].getU1();
            float v0 = sprites[0].getV0();
            float v1 = sprites[0].getV1();
            int lightmapBelow = getLightColor(level, x, y - 1, z);
            float r = shadeD * colorR;
            float g = shadeD * colorG;
            float b = shadeD * colorB;
            this.vertex(builder, vx, vy + yOffD, vz + 1, r, g, b, u0, v1, lightmapBelow);
            this.vertex(builder, vx, vy + yOffD, vz, r, g, b, u0, v0, lightmapBelow);
            this.vertex(builder, vx + 1, vy + yOffD, vz, r, g, b, u1, v0, lightmapBelow);
            this.vertex(builder, vx + 1, vy + yOffD, vz + 1, r, g, b, u1, v1, lightmapBelow);
            rendered = true;
        }
        int lightmapSide = getLightColor(level, x, y, z);
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            double vx0;
            double vx1;
            double vz0;
            double vz1;
            float h0;
            float h1;
            BlockState stateAtSide;
            switch (dir) {
                case NORTH -> {
                    if (!renderN) {
                        continue;
                    }
                    h0 = heightNW;
                    h1 = heightNE;
                    stateAtSide = level.getBlockStateAtSide(x, y, z, dir);
                    if (isFaceOccludedByNeighbor(level, pos, dir, Math.max(h0, h1), stateAtSide)) {
                        continue;
                    }
                    vx0 = vx;
                    vx1 = vx + 1;
                    vz0 = vz + 0.001;
                    vz1 = vz + 0.001;
                }
                case SOUTH -> {
                    if (!renderS) {
                        continue;
                    }
                    h0 = heightSE;
                    h1 = heightSW;
                    stateAtSide = level.getBlockStateAtSide(x, y, z, dir);
                    if (isFaceOccludedByNeighbor(level, pos, dir, Math.max(h0, h1), stateAtSide)) {
                        continue;
                    }
                    vx0 = vx + 1;
                    vx1 = vx;
                    vz0 = vz + 1 - 0.001;
                    vz1 = vz + 1 - 0.001;
                }
                case WEST -> {
                    if (!renderW) {
                        continue;
                    }
                    h0 = heightSW;
                    h1 = heightNW;
                    stateAtSide = level.getBlockStateAtSide(x, y, z, dir);
                    if (isFaceOccludedByNeighbor(level, pos, dir, Math.max(h0, h1), stateAtSide)) {
                        continue;
                    }
                    vx0 = vx + 0.001;
                    vx1 = vx + 0.001;
                    vz0 = vz + 1;
                    vz1 = vz;
                }
                default -> {
                    if (!renderE) {
                        continue;
                    }
                    h0 = heightNE;
                    h1 = heightSE;
                    stateAtSide = level.getBlockStateAtSide(x, y, z, dir);
                    if (isFaceOccludedByNeighbor(level, pos, dir, Math.max(h0, h1), stateAtSide)) {
                        continue;
                    }
                    vx0 = vx + 1 - 0.001;
                    vx1 = vx + 1 - 0.001;
                    vz0 = vz;
                    vz1 = vz + 1;
                }
            }
            rendered = true;
            TextureAtlasSprite sprite = sprites[1];
            if (!isLava) {
                Block blockAtSide = stateAtSide.getBlock();
                if (blockAtSide instanceof HalfTransparentBlock || blockAtSide instanceof LeavesBlock) {
                    sprite = this.waterOverlay;
                }
            }
            float u0 = sprite.getU(0);
            float u1 = sprite.getU(8);
            float v0 = sprite.getV((1.0F - h0) * 16.0F * 0.5F);
            float v1 = sprite.getV((1.0F - h1) * 16.0F * 0.5F);
            float v2 = sprite.getV(8);
            float shade = level.getShade(dir, true);
            float r = shadeU * shade * colorR;
            float g = shadeU * shade * colorG;
            float b = shadeU * shade * colorB;
            this.vertex(builder, vx0, vy + h0, vz0, r, g, b, u0, v0, lightmapSide);
            this.vertex(builder, vx1, vy + h1, vz1, r, g, b, u1, v1, lightmapSide);
            this.vertex(builder, vx1, vy + yOffD, vz1, r, g, b, u1, v2, lightmapSide);
            this.vertex(builder, vx0, vy + yOffD, vz0, r, g, b, u0, v2, lightmapSide);
            if (sprite != this.waterOverlay) {
                this.vertex(builder, vx0, vy + yOffD, vz0, r, g, b, u0, v2, lightmapSide);
                this.vertex(builder, vx1, vy + yOffD, vz1, r, g, b, u1, v2, lightmapSide);
                this.vertex(builder, vx1, vy + h1, vz1, r, g, b, u1, v1, lightmapSide);
                this.vertex(builder, vx0, vy + h0, vz0, r, g, b, u0, v0, lightmapSide);
            }
        }
        return rendered;
    }

    @Shadow
    protected abstract void vertex(VertexConsumer vertexConsumer, double d, double e, double f, float g, float h, float i, float j, float k, int l);
}
