package tgw.evolution.util;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Arrays;
import java.util.Random;

public class MathHelper {

    public static final float PI = (float) Math.PI;
    public static final Random RANDOM = new Random();
    public static final Direction[] DIRECTIONS_EXCEPT_DOWN = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] DIRECTIONS_HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static int clampMin(int value, int min) {
        return Math.max(value, min);
    }

    public static int clampMax(int value, int max) {
        return Math.min(value, max);
    }

    public static long getPositionRandom(Vec3i vec3i) {
        return net.minecraft.util.math.MathHelper.getPositionRandom(vec3i);
    }

    /**
     * Used to compare boolean matrices of the same size only.
     */
    public static boolean matricesEqual(boolean[][] a, boolean[][] b) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j] != b[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static float sin(float rad) {
        return net.minecraft.util.math.MathHelper.sin(rad);
    }

    public static float cos(float rad) {
        return net.minecraft.util.math.MathHelper.cos(rad);
    }

    public static float tan(float rad) {
        return net.minecraft.util.math.MathHelper.sin(rad) / net.minecraft.util.math.MathHelper.cos(rad);
    }

    /**
     * Used to compare boolean tensors of the same size only.
     */
    public static boolean tensorsEquals(boolean[][][] a, boolean[][][] b) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                for (int k = 0; k < a[i][j].length; k++) {
                    if (a[i][j][k] != b[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns the spherical, shortest distance between two points in 3 dimensional space.
     */
    public static float distance(double x, double y, double z, double x0, double y0, double z0) {
        return net.minecraft.util.math.MathHelper.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0));
    }

    public static float attackSpeed(float chargeTimeInSec) {
        return 1.0F / chargeTimeInSec;
    }

    public static double attackSpeedAdd(double chargeTimeInSec) {
        return 1.0 / chargeTimeInSec - 4.0;
    }

    public static String numericalValue(String input) {
        int size = input.length();
        StringBuilder builder = new StringBuilder(input);
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(builder.charAt(i)) && builder.charAt(i) != '.') {
                builder.deleteCharAt(i);
                i--;
                size--;
            }
        }
        return builder.toString();
    }

    /**
     * @param input A square boolean matrix.
     * @return A new square boolean matrix rotated 90 degrees clockwise.
     */
    public static boolean[][] rotateClockWise(boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        int n = input.length;
        for (int ring = 0; ring < input.length / 2; ring++) {
            for (int swap = ring; swap < n - 1 - ring; swap++) {
                output[ring][swap] = input[n - 1 - swap][ring];
                output[swap][n - 1 - ring] = input[ring][swap];
                output[n - 1 - ring][n - 1 - swap] = input[swap][n - 1 - ring];
                output[n - 1 - swap][ring] = input[n - 1 - ring][n - 1 - swap];
            }
        }
        if (n % 2 == 1) {
            output[n / 2][n / 2] = input[n / 2][n / 2];
        }
        return output;
    }

    /**
     * @param input A square boolean matrix.
     * @return A new square boolean matrix translated one index to the right and wrapped around.
     */
    public static boolean[][] translateRight(boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            output[i][0] = input[i][input.length - 1];
            System.arraycopy(input[i], 0, output[i], 1, input.length - 1);
        }
        return output;
    }

    /**
     * @param input A square boolean matrix.
     * @return A new square boolean matrix mirrored vertically.
     */
    public static boolean[][] mirrorVertically(boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < output.length; i++) {
            System.arraycopy(input[input.length - 1 - i], 0, output[i], 0, output[i].length);
        }
        return output;
    }

    public static String printBooleanMatrix(boolean[][] matrix) {
        StringBuilder builder = new StringBuilder().append("\n");
        for (boolean[] booleans : matrix) {
            for (boolean aBoolean : booleans) {
                builder.append(aBoolean).append(", ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public static void resetArray(int[] array, int index) {
        for (int i = index; i < array.length; i++) {
            array[i] = -1;
        }
    }

    public static void resetTensor(boolean[][][] tensor, int index) {
        for (int i = index; i < tensor.length; i++) {
            tensor[i] = null;
        }
    }

    public static void fillBooleanMatrix(boolean[][] matrix) {
        for (boolean[] vectors : matrix) {
            Arrays.fill(vectors, true);
        }
    }

    public static float degToRad(float degrees) {
        return (float) (degrees * Math.PI / 180F);
    }

    public static int getHitIndex(int length, double min, double max, double hitRelativistic) {
        double size = (max - min) / length;
        for (int i = 0; i < length; i++) {
            if (hitRelativistic < min + size * i) {
                return clamp(i - 1, 0, length - 1);
            }
        }
        return clamp(length - 1, 0, length - 1);
    }

    public static double hitOffset(Axis axis, double hit, Direction dir) {
        if (dir.getAxis() != axis) {
            return hit;
        }
        if (dir.getAxisDirection() == AxisDirection.POSITIVE) {
            hit -= 0.1;
            return hit;
        }
        hit += 0.1;
        return hit;
    }

    public static EquipmentSlotType getEquipFromHand(Hand hand) {
        switch (hand) {
            case MAIN_HAND:
                return EquipmentSlotType.MAINHAND;
            case OFF_HAND:
                return EquipmentSlotType.OFFHAND;
            default:
                return null;
        }
    }

    public static boolean rangeInclusive(double value, double start, double end) {
        if (value < start) {
            return false;
        }
        return !(value > end);
    }

    public static float relativize(float value, float min, float max) {
        value = clamp(value, min, max);
        value -= min;
        float delta = max - min;
        value /= delta;
        return value;
    }

    public static BlockRayTraceResult rayTraceBlocks(Entity entity, float partialTicks, float distance, boolean fluid) {
        Vec3d from = entity.getEyePosition(partialTicks);
        Vec3d look = entity.getLook(partialTicks);
        Vec3d to = from.add(look.x * distance, look.y * distance, look.z * distance);
        return entity.world.rayTraceBlocks(new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, fluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, entity));
    }

    public static Direction getNegativeAxis(Direction.Axis axis) {
        switch (axis) {
            case X:
                return Direction.WEST;
            case Y:
                return Direction.DOWN;
            case Z:
                return Direction.NORTH;
            default:
                throw new IllegalStateException("Cannot get the negative direction for axis " + axis);
        }
    }

    public static Direction getPositiveAxis(Direction.Axis axis) {
        switch (axis) {
            case X:
                return Direction.EAST;
            case Y:
                return Direction.UP;
            case Z:
                return Direction.SOUTH;
            default:
                throw new IllegalStateException("Cannot get the positive direction for axis " + axis);
        }
    }

    /**
     * @param inside    The VoxelShape that should be totally inside.
     * @param reference The VoxelShape that should totally encompass the first.
     * @return True if the first VoxelShape is totally encompassed by the second one; false otherwise.
     */
    public static boolean isShapeTotallyInside(VoxelShape inside, VoxelShape reference) {
        return !VoxelShapes.compare(reference, inside, IBooleanFunction.ONLY_SECOND);
    }

    /**
     * @param outside   The VoxelShape that should be totally ouside the reference.
     * @param reference The reference VoxelShape.
     * @return True if the first VoxelShape is totally outside the reference one, false otherwise.
     */
    public static boolean isShapeTotallyOutside(VoxelShape outside, VoxelShape reference) {
        return !VoxelShapes.compare(reference, outside, IBooleanFunction.AND);
    }

    /**
     * @param A The VoxelShape to subtract from
     * @param B The VoxelShape to subtract
     * @return A new VoxelShape, consisting of the VoxelShape A minus B.
     */
    public static VoxelShape subtract(VoxelShape A, VoxelShape B) {
        return VoxelShapes.combine(A, B, IBooleanFunction.ONLY_FIRST);
    }

    /**
     * @param A The first VoxelShape
     * @param B The second VoxelShape
     * @return A new VoxelShape made of the union of both VoxelShapes.
     */
    public static VoxelShape union(VoxelShape A, VoxelShape B) {
        return VoxelShapes.combine(A, B, IBooleanFunction.OR);
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        if (to == from) {
            return shape;
        }
        VoxelShape[] buffer = {shape, VoxelShapes.empty()};
        int times = (to.getHorizontalIndex() - from.getHorizontalIndex() + 4) % 4;
        for (int i = 0; i < times; i++) {
            //noinspection ObjectAllocationInLoop
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1], VoxelShapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }
        return buffer[0];
    }

    public static String printVoxelShape(VoxelShape shape) {
        if (shape == null) {
            return "VoxelShape: null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("VoxelShape:\n");
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> builder.append("            [").append(minX).append(", ").append(minY).append(", ").append(minZ).append(" -> ").append(maxX).append(", ").append(maxY).append(", ").append(maxZ).append("],\n"));
        return builder.toString();
    }
}
