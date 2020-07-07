package tgw.evolution.util;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class MathHelper {

    public static final float PI = (float) Math.PI;
    public static final Random RANDOM = new Random();
    public static final Direction[] DIRECTIONS_EXCEPT_DOWN = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] DIRECTIONS_HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final Predicate<Entity> PREDICATE = EntityPredicates.CAN_AI_TARGET.and(e -> e != null && e.canBeCollidedWith() && e instanceof LivingEntity && !(e instanceof FakePlayer));

    public static short toShortExact(int value) {
        if ((short) value != value) {
            throw new ArithmeticException("Short overflow " + value);
        }
        return (short) value;
    }

    /**
     * Checks whether or not two {@link ItemStack} are sufficiently equal.
     * This means that they are the same item.
     *
     * @return {@link Boolean#TRUE} if the {@link ItemStack} are sufficiently equal, {@link Boolean#FALSE} otherwise.
     */
    public static boolean areItemStacksSufficientlyEqual(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        return a.getItem() == b.getItem();
    }

    public static byte toByteExact(int value) {
        if ((byte) value != value) {
            throw new ArithmeticException("Byte overflow " + value);
        }
        return (byte) value;
    }

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
     * Gets the roman representation of a number as a {@link String}.
     *
     * @param number The desired number to get the roman representation.
     * @return A String representing the number in roman form.
     */
    public static String getRomanNumber(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
        }
        return String.valueOf(number);
    }

    /**
     * Used to compare boolean matrices of the same size only.
     */
    public static boolean matricesEqual(@Nonnull boolean[][] a, @Nonnull boolean[][] b) {
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
     *
     * @return true if the tensors are equal; otherwise returns false.
     */
    public static boolean tensorsEquals(@Nonnull boolean[][][] a, @Nonnull boolean[][][] b) {
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
     * @return the spherical, shortest distance between two points in 3 dimensional space.
     */
    public static float distance(double x, double y, double z, double x0, double y0, double z0) {
        return net.minecraft.util.math.MathHelper.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0));
    }

    /**
     * Calculates an attack speed given a charge time.
     *
     * @param chargeTimeInSec The time it takes to charge the item, in seconds.
     * @return the corresponding attack speed.
     */
    public static float attackSpeed(float chargeTimeInSec) {
        return 1.0F / chargeTimeInSec;
    }

    /**
     * Calculates the amount to add to the default attack speed (4.0) to change it
     * to the given charge time.
     *
     * @param chargeTimeInSec The time it takes to charge the item.
     * @return the corresponding attack speed, which must be added to the default one.
     */
    public static double attackSpeedAdd(double chargeTimeInSec) {
        return 1.0 / chargeTimeInSec - 4.0;
    }

    public static String numericalValue(@Nonnull String input) {
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
     * Rotates a boolean matrix clockwise.
     *
     * @param input A square boolean matrix.
     * @return A new square boolean matrix rotated 90 degrees clockwise.
     */
    public static boolean[][] rotateClockWise(@Nonnull boolean[][] input) {
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
     * Translates a boolean matrix 1 unit to the right, wrapping values around.
     *
     * @param input A square boolean matrix.
     * @return A new square boolean matrix translated one index to the right and wrapped around.
     */
    public static boolean[][] translateRight(@Nonnull boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            output[i][0] = input[i][input.length - 1];
            System.arraycopy(input[i], 0, output[i], 1, input.length - 1);
        }
        return output;
    }

    /**
     * Mirrors a boolean matrix vertically.
     *
     * @param input A square boolean matrix.
     * @return A new square boolean matrix mirrored vertically.
     */
    public static boolean[][] mirrorVertically(@Nonnull boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < output.length; i++) {
            System.arraycopy(input[input.length - 1 - i], 0, output[i], 0, output[i].length);
        }
        return output;
    }

    /**
     * Turns a boolean matrix in a human-readable format.
     *
     * @param matrix The matrix to print.
     * @return The corresponding String containing the matrix in a human-readable format.
     */
    public static String printBooleanMatrix(@Nonnull boolean[][] matrix) {
        StringBuilder builder = new StringBuilder().append("\n");
        for (boolean[] booleans : matrix) {
            for (boolean aBoolean : booleans) {
                builder.append(aBoolean).append(", ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Resets a boolean vector starting at the desired index to -1.
     *
     * @param array The boolean vector to reset.
     * @param index The index to start the reset.
     */
    public static void resetArray(int[] array, int index) {
        for (int i = index; i < array.length; i++) {
            array[i] = -1;
        }
    }

    /**
     * Resets a boolean tensor starting at the desired index to null.
     *
     * @param tensor A boolean tensor to reset.
     * @param index  The index to start the reset.
     */
    public static void resetTensor(boolean[][][] tensor, int index) {
        for (int i = index; i < tensor.length; i++) {
            tensor[i] = null;
        }
    }

    /**
     * Fills a boolean matrix with true.
     *
     * @param matrix The matrix to fill.
     */
    public static void fillBooleanMatrix(boolean[][] matrix) {
        for (boolean[] vectors : matrix) {
            Arrays.fill(vectors, true);
        }
    }

    /**
     * Converts a float in degrees to radians.
     *
     * @param degrees The value in degrees.
     * @return the corresponding value in radians.
     */
    public static float degToRad(float degrees) {
        return degrees * PI / 180;
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

    @Nullable
    public static EntityRayTraceResult rayTraceEntityFromEyes(Entity entity, float partialTicks, double blockReachDistance) {
        Vec3d from = entity.getEyePosition(partialTicks);
        Vec3d look = entity.getLook(partialTicks);
        Vec3d to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);
        return rayTraceEntities(entity, from, to, new AxisAlignedBB(from, to), blockReachDistance * blockReachDistance);
    }

    @Nullable
    public static EntityRayTraceResult rayTraceEntities(Entity shooter, Vec3d startVec, Vec3d endVec, AxisAlignedBB boundingBox, double distanceSquared) {
        World world = shooter.world;
        double range = distanceSquared;
        Entity entity = null;
        Vec3d vec3d = null;
        for (Entity entityInBoundingBox : world.getEntitiesInAABBexcluding(shooter, boundingBox, PREDICATE)) {
            AxisAlignedBB axisalignedbb = entityInBoundingBox.getBoundingBox();
            Optional<Vec3d> optional = axisalignedbb.rayTrace(startVec, endVec);
            if (axisalignedbb.contains(startVec)) {
                if (range >= 0.0D) {
                    entity = entityInBoundingBox;
                    vec3d = optional.orElse(startVec);
                    range = 0.0D;
                }
            }
            else if (optional.isPresent()) {
                Vec3d hitResult = optional.get();
                double actualDistanceSquared = startVec.squareDistanceTo(hitResult);
                if (actualDistanceSquared < range || range == 0.0D) {
                    if (entityInBoundingBox.getLowestRidingEntity() == shooter.getLowestRidingEntity() && !entityInBoundingBox.canRiderInteract()) {
                        if (range == 0.0D) {
                            entity = entityInBoundingBox;
                            vec3d = hitResult;
                        }
                    }
                    else {
                        entity = entityInBoundingBox;
                        vec3d = hitResult;
                        range = actualDistanceSquared;
                    }
                }
            }
        }
        if (entity == null) {
            return null;
        }
        return new EntityRayTraceResult(entity, vec3d);
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
                throw new IllegalStateException("Unknown hand " + hand);
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
    public static boolean isShapeTotallyInside(@Nonnull VoxelShape inside, @Nonnull VoxelShape reference) {
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
    public static VoxelShape subtract(@Nonnull VoxelShape A, @Nonnull VoxelShape B) {
        return VoxelShapes.combine(A, B, IBooleanFunction.ONLY_FIRST);
    }

    /**
     * @param A The first VoxelShape
     * @param B The second VoxelShape
     * @return A new VoxelShape made of the union of both VoxelShapes.
     */
    public static VoxelShape union(@Nonnull VoxelShape A, @Nonnull VoxelShape B) {
        return VoxelShapes.combine(A, B, IBooleanFunction.OR);
    }

    public static VoxelShape rotateShape(@Nonnull Direction from, @Nonnull Direction to, @Nonnull VoxelShape shape) {
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

    /**
     * Sets the rotation angles of a RendererModel.
     *
     * @param model The model to set the rotation angles.
     * @param x     The rotation angle around the x axis.
     * @param y     The rotation angle around the y axis.
     * @param z     The rotation angle around the z axis.
     */
    public static void setRotationAngle(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    /**
     * Inverts the values of a boolean matrix.
     *
     * @param matrix The boolean matrix to invert.
     * @return a new, inverted boolean matrix.
     */
    @SuppressWarnings("ObjectAllocationInLoop")
    public static boolean[][] invertMatrix(boolean[][] matrix) {
        boolean[][] mat = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            mat[i] = new boolean[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                mat[i][j] = !matrix[i][j];
            }
        }
        return mat;
    }

    public static int floor(double value) {
        return net.minecraft.util.math.MathHelper.floor(value);
    }

    public static float lerp(float partialTicks, float old, float now) {
        return net.minecraft.util.math.MathHelper.lerp(partialTicks, old, now);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }
}
