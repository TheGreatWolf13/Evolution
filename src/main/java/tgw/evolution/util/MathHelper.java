package tgw.evolution.util;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.HangingEntity;
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
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class MathHelper {

    public static final float PI = (float) Math.PI;
    public static final float TAU = 2.0f * PI;
    public static final float PI_OVER_2 = PI / 2.0f;
    public static final float SQRT_2 = sqrt(2.0f);
    public static final float SQRT_2_OVER_2 = SQRT_2 / 2.0f;
    public static final Random RANDOM = new Random();
    public static final Direction[] DIRECTIONS_EXCEPT_DOWN = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] DIRECTIONS_EXCEPT_UP = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final Direction[] DIRECTIONS_HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    public static final DirectionDiagonal[][] DIAGONALS = {{DirectionDiagonal.NORTH_WEST, DirectionDiagonal.NORTH_EAST},
                                                           {DirectionDiagonal.SOUTH_WEST, DirectionDiagonal.SOUTH_EAST}};
    public static final Direction[] DIRECTIONS_X = {Direction.WEST, Direction.EAST};
    public static final Direction[] DIRECTIONS_Z = {Direction.NORTH, Direction.SOUTH};
    public static final Hand[] HANDS_LEFT_PRIORITY = {Hand.OFF_HAND, Hand.MAIN_HAND};
    private static final Predicate<Entity> PREDICATE = EntityPredicates.CAN_AI_TARGET.and(e -> e != null &&
                                                                                               e.canBeCollidedWith() &&
                                                                                               (e instanceof LivingEntity ||
                                                                                                e instanceof HangingEntity) &&
                                                                                               !(e instanceof FakePlayer));

    private MathHelper() {
    }

    /**
     * Approximates the function arc cosine.
     * The max error is 0.017 rad.
     * This method is much faster than the standard {@link Math#acos(double)}
     *
     * @param value The arc cosine argument.
     * @return The angle represented by this arc, in radians.
     */
    @Radian
    public static double arcCos(double value) {
        double a = -0.939_115_566_365_855;
        double b = 0.921_784_152_891_457_3;
        double c = -1.284_590_624_469_083_7;
        double d = 0.295_624_144_969_963_174;
        return Math.PI / 2.0 + (a * value + b * value * value * value) / (1 + c * value * value + d * value * value * value * value);
    }

    /**
     * Approximates the function arc cosine.
     * The max error is 0.96 degrees.
     * This method is much faster than the standard {@link Math#acos(double)}
     *
     * @param value The arc cosine argument.
     * @return The angle represented by this arc, in degrees.
     */
    @Degree
    public static float arcCosDeg(double value) {
        return (float) (arcCos(value) * 180 / Math.PI);
    }

    /**
     * Checks whether or not two {@link ItemStack} are sufficiently equal.
     * This means that they are the same item.
     *
     * @return {@code true} if the {@link ItemStack}s are sufficiently equal, {@code false} otherwise.
     */
    public static boolean areItemStacksSufficientlyEqual(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.getCount() != b.getCount()) {
            return false;
        }
        return a.getItem() == b.getItem();
    }

    /**
     * Returns whether two {@code float} values have the same sign.
     *
     * @param a The first value
     * @param b The second value
     * @return {@code true} if they have the same sign, {@code false} otherwise.
     */
    public static boolean areSameSign(float a, float b) {
        if (a >= 0 && b >= 0) {
            return true;
        }
        return a < 0 && b < 0;
    }

    /**
     * Calculates the Arctangent of a vector given two components.
     *
     * @param y The y component of the vector.
     * @param x The x component of the vector.
     * @return The angle between the vector and the X axis, given in radians.
     */
    @Radian
    public static double atan2(double y, double x) {
        return net.minecraft.util.math.MathHelper.atan2(y, x);
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
     * Approximates a {@code double} value to an {@code int}, rounding up.
     *
     * @param value The value to approximate.
     * @return An {@code int} value rounded up.
     */
    public static int ceil(double value) {
        return net.minecraft.util.math.MathHelper.ceil(value);
    }

    /**
     * Caps the bounds of a value to be within an inclusive range.
     *
     * @param value The value to cap.
     * @param min   The lower limit.
     * @param max   The upper limit.
     * @return The value if it is within the range, otherwise returns the limit it is closer to.
     */
    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    /**
     * Caps the bounds of a value to be within an inclusive range.
     *
     * @param value The value to cap.
     * @param min   The lower limit.
     * @param max   The upper limit.
     * @return The value if it is within the range, otherwise returns the limit it is closer to.
     */
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    /**
     * Caps the bounds of a value to be within an inclusive range.
     *
     * @param value The value to cap.
     * @param min   The lower limit.
     * @param max   The upper limit.
     * @return The value if it is within the range, otherwise returns the limit it is closer to.
     */
    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    /**
     * Caps the upper limit of a value.
     *
     * @param value The value to cap.
     * @param max   The upper limit.
     * @return The original value, if it is not greater than the {@code max}, otherwise returns the {@code max}.
     */
    public static int clampMax(int value, int max) {
        return Math.min(value, max);
    }

    /**
     * Caps the upper limit of a value.
     *
     * @param value The value to cap.
     * @param max   The upper limit.
     * @return The original value, if it is not greater than the {@code max}, otherwise returns the {@code max}.
     */
    public static float clampMax(float value, float max) {
        return Math.min(value, max);
    }

    /**
     * Caps the lower limit of a value.
     *
     * @param value The value to cap.
     * @param min   The lower limit.
     * @return The original value, if it is not less than the {@code min}, otherwise returns the {@code min}.
     */
    public static int clampMin(int value, int min) {
        return Math.max(value, min);
    }

    /**
     * Caps the lower limit of a value.
     *
     * @param value The value to cap.
     * @param min   The lower limit.
     * @return The original value, if it is not less than the {@code min}, otherwise returns the {@code min}.
     */
    public static float clampMin(float value, float min) {
        return Math.max(value, min);
    }

    /**
     * Caps the lower limit of a value.
     *
     * @param value The value to cap.
     * @param min   The lower limit.
     * @return The original value, if it is not less than the {@code min}, otherwise returns the {@code min}.
     */
    public static double clampMin(double value, double min) {
        return Math.max(value, min);
    }

    /**
     * Converts the given acceleration to Minecraft values.
     *
     * @param mPerSSq The acceleration in metres / second^2.
     * @return The acceleration in metres / tick^2.
     */
    public static double convertAcceleration(double mPerSSq) {
        return mPerSSq / 400;
    }

    /**
     * Converts the given force to Minecraft values.
     *
     * @param newtons The force in newtons (kg * m/s^2).
     * @return The force in kg * m/t^2.
     */
    public static double convertForce(double newtons) {
        return newtons / 400;
    }

    /**
     * Converts the given speed to Minecraft values.
     *
     * @param mPerS The speed in metres / second.
     * @return The speed in metres / tick.
     */
    public static double convertSpeed(double mPerS) {
        return mPerS / 20;
    }

    /**
     * Approximates the trigonometric function cosine.
     *
     * @param rad The argument of the cosine, given in radians.
     * @return An approximation of the cosine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float cos(@Radian float rad) {
        return net.minecraft.util.math.MathHelper.cos(rad);
    }

    /**
     * Approximates the trigonometric function cosine.
     *
     * @param deg The argument of the cosine, given in degrees.
     * @return An approximation of the cosine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float cosDeg(@Degree float deg) {
        return net.minecraft.util.math.MathHelper.cos(degToRad(deg));
    }

    /**
     * Converts a {@code float} value from degrees to radians.
     *
     * @param degrees The value in degrees.
     * @return the corresponding value in radians.
     */
    @Radian
    public static float degToRad(@Degree float degrees) {
        return degrees * PI / 180;
    }

    /**
     * Calculates the shortest distance between two points in euclidian space.
     *
     * @param x  The x coordinate of the first point.
     * @param y  The y coordinate of the first point.
     * @param z  The z coordinate of the first point.
     * @param x0 The x coordinate of the second point.
     * @param y0 The y coordiante of the second point.
     * @param z0 The z coordinate of the second point.
     * @return The shortest distance between the points as a {@code float} value.
     * The distance will never be negative.
     */
    public static float distance(double x, double y, double z, double x0, double y0, double z0) {
        return sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0));
    }

    /**
     * Calculates the entity between two entities in euclidian space, squared.
     *
     * @param entity1 The first entity.
     * @param entity2 The second entity.
     * @return The square of the distance between the entities.
     */
    public static double distanceSquared(Entity entity1, Entity entity2) {
        double deltaX = entity1.posX - entity2.posX;
        double deltaY = entity1.posY - entity2.posY;
        double deltaZ = entity1.posZ - entity2.posZ;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    /**
     * Fills a {@link Nonnull} {@code boolean} matrix with {@code true}.
     *
     * @param matrix The matrix to fill.
     */
    public static void fillBooleanMatrix(@Nonnull boolean[][] matrix) {
        for (boolean[] vectors : matrix) {
            Arrays.fill(vectors, true);
        }
    }

    /**
     * Approximates a {@code double} value to an {@code int}, rounding down.
     *
     * @param value The value to approximate.
     * @return An {@code int} value rounded down.
     */
    public static int floor(double value) {
        return net.minecraft.util.math.MathHelper.floor(value);
    }

    /**
     * Converts a {@link Hand} to {@link EquipmentSlotType}.
     *
     * @param hand The {@link Hand} to convert.
     * @return The corresponding {@link EquipmentSlotType}.
     */
    public static EquipmentSlotType getEquipFromHand(@Nonnull Hand hand) {
        switch (hand) {
            case MAIN_HAND:
                return EquipmentSlotType.MAINHAND;
            case OFF_HAND:
                return EquipmentSlotType.OFFHAND;
            default:
                throw new IllegalStateException("Unknown hand " + hand);
        }
    }

    /**
     * Calculates the index of a number given a range.
     *
     * @param maxIndex The maximum allowed value for the index to be, exclusive. Must be greater than {@code 0}.
     * @param min      The start of the range.
     * @param max      The end of the range.
     * @param value    The value to calculate the index of.
     * @return An {@code int} value representing the index of the number,
     * between {@code 0} (inclusive) and {@code maxIndex} (exclusive).
     */
    public static int getIndex(int maxIndex, double min, double max, double value) {
        value = clamp(value, min, max);
        double size = (max - min) / maxIndex;
        int i = (int) ((value - min) / size);
        return clamp(i, 0, maxIndex - 1);
    }

    /**
     * Gets the negative {@link Direction} of a given {@link Axis}.
     *
     * @param axis The desired {@link Axis}.
     * @return The negative {@link Direction} on that {@link Axis}.
     */
    public static Direction getNegativeAxis(@Nonnull Direction.Axis axis) {
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

    /**
     * Gets a {@code long} seed based on a {@link Vec3i}.
     *
     * @param vec The {@link Vec3i} to base the seed in.
     * @return A {@code long} containing a seed to this {@link Vec3i}.
     */
    public static long getPositionRandom(Vec3i vec) {
        return net.minecraft.util.math.MathHelper.getPositionRandom(vec);
    }

    /**
     * Gets the positive {@link Direction} of a given {@link Axis}.
     *
     * @param axis The desired {@link Axis}.
     * @return The positive {@link Direction} on that {@link Axis}.
     */
    public static Direction getPositiveAxis(@Nonnull Direction.Axis axis) {
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
     * Calculates the relative strength of a throw based on the charge.
     *
     * @param charge The time the item has been charged for, in ticks.
     * @return A {@code float} from {@code 0.0f} to {@code 1.0f} representing the relative charge.
     */
    public static float getRelativeChargeStrength(int charge) {
        float f = charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    /**
     * Gets the roman representation of a number as a {@link String}.
     *
     * @param number The desired number to get the roman representation.
     * @return A {@link String} representing the number in roman form.
     */
    @Nonnull
    public static String getRomanNumber(int number) {
        if (number <= 0 || number > 3_999) {
            return String.valueOf(number);
        }
        StringBuilder builder = new StringBuilder();
        while (number > 0) {
            if (number >= 1_000) {
                number -= 1_000;
                builder.append("M");
            }
            else if (number >= 900) {
                number -= 900;
                builder.append("CM");
            }
            else if (number >= 500) {
                number -= 500;
                builder.append("D");
            }
            else if (number >= 400) {
                number -= 400;
                builder.append("CD");
            }
            else if (number >= 100) {
                number -= 100;
                builder.append("C");
            }
            else if (number >= 90) {
                number -= 90;
                builder.append("XC");
            }
            else if (number >= 50) {
                number -= 50;
                builder.append("L");
            }
            else if (number >= 40) {
                number -= 40;
                builder.append("XL");
            }
            else if (number >= 10) {
                number -= 10;
                builder.append("X");
            }
            else if (number >= 9) {
                number -= 9;
                builder.append("IX");
            }
            else if (number >= 5) {
                number -= 5;
                builder.append("V");
            }
            else if (number >= 4) {
                number -= 4;
                builder.append("IV");
            }
            else {
                number -= 1;
                builder.append("I");
            }
        }
        return builder.toString();
    }

    /**
     * Offsets a hit position to be within a bounding box.
     *
     * @param axis      The desired {@link Axis} to offset.
     * @param hit       The position of the hit.
     * @param direction The {@link Direction} from where the hit came from.
     * @return A hit position offset in the derired {@link Direction}
     * if the {@link Direction} is in the desired {@link Axis}.
     * If it is not, the position will not change.
     */
    public static double hitOffset(Axis axis, double hit, @Nonnull Direction direction) {
        if (direction.getAxis() != axis) {
            return hit;
        }
        if (direction.getAxisDirection() == AxisDirection.POSITIVE) {
            hit -= 0.1;
            return hit;
        }
        hit += 0.1;
        return hit;
    }

    /**
     * Calculates the horizontal length of a given vector, given x and z components.
     *
     * @param x The x component
     * @param z The z component
     * @return The horizontal length of said vector.
     */
    public static float horizontalLength(double x, double z) {
        return sqrt(x * x + z * z);
    }

    /**
     * Calculates the horizontal length of a given vector.
     *
     * @param vec The corresponding vector.
     * @return The horizontal length of said vector.
     */
    public static float horizontalLength(Vec3d vec) {
        return sqrt(vec.x * vec.x + vec.z * vec.z);
    }

    /**
     * Inverts the values of a {@code boolean} matrix.
     *
     * @param matrix The {@code boolean} matrix to invert.
     * @return A {@code new}, inverted {@code boolean} matrix.
     */
    @Nonnull
    @SuppressWarnings("ObjectAllocationInLoop")
    public static boolean[][] invertMatrix(@Nonnull boolean[][] matrix) {
        boolean[][] mat = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            mat[i] = new boolean[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                mat[i][j] = !matrix[i][j];
            }
        }
        return mat;
    }

    public static boolean isInInterval(float value, float middlePoint, float length) {
        float start = middlePoint - length / 2;
        float end = middlePoint + length / 2;
        return start <= value && value <= end;
    }

    public static boolean isMouseInsideBox(int mouseX, int mouseY, int x0, int y0, int x1, int y1) {
        if (x0 <= mouseX && mouseX <= x1) {
            return y0 <= mouseY && mouseY <= y1;
        }
        return false;
    }

    /**
     * Calculates if a {@link VoxelShape} is totally inside another one.
     *
     * @param inside    The {@link VoxelShape} that should be totally inside.
     * @param reference The {@link VoxelShape} that should totally encompass the first.
     * @return {@code true} if the first {@link VoxelShape} is totally encompassed by the second one; {@code false} otherwise.
     */
    public static boolean isShapeTotallyInside(@Nonnull VoxelShape inside, @Nonnull VoxelShape reference) {
        return !VoxelShapes.compare(reference, inside, IBooleanFunction.ONLY_SECOND);
    }

    /**
     * Calculates if the {@link VoxelShape} is totally outside another one.
     *
     * @param outside   The {@link VoxelShape} that should be totally ouside the reference.
     * @param reference The reference {@link VoxelShape}.
     * @return {@code true} if the first {@link VoxelShape} is totally outside the reference one, {@code false} otherwise.
     */
    public static boolean isShapeTotallyOutside(VoxelShape outside, VoxelShape reference) {
        return !VoxelShapes.compare(reference, outside, IBooleanFunction.AND);
    }

    /**
     * Iterates through a {@link List} in reverse order.
     *
     * @param list     The {@link List} to iterate.
     * @param consumer The action to perform with the parameter.
     * @param <T>      The Type parameter of the {@link List}
     */
    public static <T> void iterateReverse(@Nonnull List<T> list, Consumer<T> consumer) {
        for (int i = list.size() - 1; i >= 0; i--) {
            consumer.accept(list.get(i));
        }
    }

    /**
     * Interpolates two numbers.
     *
     * @param partialTicks The partial ticks to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param old          The old value.
     * @param now          The newest value.
     * @return The interpolated value.
     */
    public static float lerp(float partialTicks, float old, float now) {
        return net.minecraft.util.math.MathHelper.lerp(partialTicks, old, now);
    }

    /**
     * Interpolates two numbers.
     *
     * @param partialTicks The partial ticks to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param old          The old value.
     * @param now          The newest value.
     * @return The interpolated value.
     */
    public static double lerp(double partialTicks, double old, double now) {
        return net.minecraft.util.math.MathHelper.lerp(partialTicks, old, now);
    }

    /**
     * Used to compare two {@link Nonnull} {@code boolean} matrices of the same size.
     *
     * @param a The first matrix.
     * @param b The second matrix.
     * @return {@code true} if the matrices are equal, {@code false} otherwise.
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

    /**
     * Mirrors a {@link Nonnull} square {@code boolean} matrix vertically.
     *
     * @param input A square {@code boolean} matrix.
     * @return A {@code new} square {@code boolean} matrix mirrored vertically.
     */
    @Nonnull
    public static boolean[][] mirrorVertically(@Nonnull boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < output.length; i++) {
            System.arraycopy(input[input.length - 1 - i], 0, output[i], 0, output[i].length);
        }
        return output;
    }

    /**
     * Converts a {@code float} value from radians to degrees.
     *
     * @param radians The value in radians.
     * @return the corresponding value in degrees.
     */
    @Degree
    public static float radToDeg(@Radian float radians) {
        return radians * 180 / PI;
    }

    /**
     * Verifies if a value is within a certain inclusive range.
     *
     * @param value The value to verify.
     * @param start The start of the range.
     * @param end   The end of the range.
     * @return {@code true} if the value is in the interval {@code [start, end]}, {@code false} otherwise.
     */
    public static boolean rangeInclusive(double value, double start, double end) {
        if (value < start) {
            return false;
        }
        return !(value > end);
    }

    /**
     * Casts a ray tracing for {@code Block}s starting from the eyes of the desired {@link Entity}.
     *
     * @param entity       The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks The partial ticks to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param distance     The maximum distance the ray will travel.
     * @param fluid        {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockRayTraceResult} containing the {@link BlockPos} of the {@code Block} hit.
     */
    @Nonnull
    public static BlockRayTraceResult rayTraceBlocksFromEyes(@Nonnull Entity entity, float partialTicks, float distance, boolean fluid) {
        Vec3d from = entity.getEyePosition(partialTicks);
        Vec3d look = entity.getLook(partialTicks);
        Vec3d to = from.add(look.x * distance, look.y * distance, look.z * distance);
        return entity.world.rayTraceBlocks(new RayTraceContext(from,
                                                               to,
                                                               RayTraceContext.BlockMode.OUTLINE,
                                                               fluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                                                               entity));
    }

    /**
     * Casts a ray tracing for {@code Block}s based on the {@link Entity}'s {@link Entity#rotationYaw} and {@link Entity#rotationPitch}.
     * This method is useful for Entities that are projectiles, as {@link MathHelper#rayTraceBlocksFromEyes(Entity, float, float, boolean)}
     * usually do not work on them.
     *
     * @param entity   The {@link Entity} from whose {@code yaw} and {@code pitch} to cast the ray.
     * @param distance The maximum distance this ray will travel.
     * @param fluid    {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockRayTraceResult} containing the {@link BlockPos} of the {@code Block} hit.
     */
    @Nonnull
    public static BlockRayTraceResult rayTraceBlocksFromYawAndPitch(@Nonnull Entity entity, double distance, boolean fluid) {
        Vec3d from = entity.getEyePosition(1.0f);
        float theta = entity.rotationYaw;
        if (theta < 0) {
            theta += 360;
        }
        theta = degToRad(theta);
        float phi = entity.rotationPitch;
        if (phi < 0) {
            phi += 360;
        }
        phi = degToRad(phi);
        Vec3d looking = new Vec3d(sin(theta), sin(phi), cos(theta)).normalize();
        Vec3d to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return entity.world.rayTraceBlocks(new RayTraceContext(from,
                                                               to,
                                                               RayTraceContext.BlockMode.OUTLINE,
                                                               fluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                                                               entity));
    }

    /**
     * Casts a ray tracing for Entities inside an {@link AxisAlignedBB} from the starting vector to the end vector.
     *
     * @param toExclude       The {@link Entity} to exclude from this raytrace.
     * @param startVec        The {@link Vec3d} representing the start of the raytrace.
     * @param endVec          The {@link Vec3d} representing the end of the raytrace.
     * @param boundingBox     The {@link AxisAlignedBB} to look for entities inside.
     * @param distanceSquared The max distance this raytrace will travel, squared.
     * @return A {@link EntityRayTraceResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3d} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityRayTraceResult} will be {@code null}.
     */
    @Nullable
    public static EntityRayTraceResult rayTraceEntities(@Nonnull Entity toExclude,
                                                        Vec3d startVec,
                                                        Vec3d endVec,
                                                        AxisAlignedBB boundingBox,
                                                        double distanceSquared) {
        World world = toExclude.world;
        double range = distanceSquared;
        Entity entity = null;
        Vec3d vec3d = null;
        for (Entity entityInBoundingBox : world.getEntitiesInAABBexcluding(toExclude, boundingBox, PREDICATE)) {
            AxisAlignedBB axisalignedbb = entityInBoundingBox.getBoundingBox();
            Optional<Vec3d> optional = axisalignedbb.rayTrace(startVec, endVec);
            if (axisalignedbb.contains(startVec)) {
                if (range >= 0) {
                    entity = entityInBoundingBox;
                    vec3d = optional.orElse(startVec);
                    range = 0;
                }
            }
            else if (optional.isPresent()) {
                Vec3d hitResult = optional.get();
                double actualDistanceSquared = startVec.squareDistanceTo(hitResult);
                if (actualDistanceSquared < range || range == 0) {
                    if (entityInBoundingBox.getLowestRidingEntity() == toExclude.getLowestRidingEntity() && !entityInBoundingBox.canRiderInteract()) {
                        if (range == 0) {
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

    /**
     * Casts a ray tracing for Entities starting from the eyes of the desired {@link Entity}.
     *
     * @param entity        The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks  The partial tick to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param reachDistance The max distance the ray will travel.
     * @return An {@link EntityRayTraceResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3d} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityRayTraceResult} will be {@code null}.
     */
    @Nullable
    public static EntityRayTraceResult rayTraceEntityFromEyes(@Nonnull Entity entity, float partialTicks, double reachDistance) {
        Vec3d from = entity.getEyePosition(partialTicks);
        Vec3d look = entity.getLook(partialTicks);
        Vec3d to = from.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        return rayTraceEntities(entity, from, to, new AxisAlignedBB(from, to), reachDistance * reachDistance);
    }

    /**
     * Casts a ray tracing for {@code Entities} based on the {@link Entity}'s {@link Entity#rotationYaw} and {@link Entity#rotationPitch}.
     * This method is useful for Entities that are projectiles, as {@link MathHelper#rayTraceEntityFromEyes(Entity, float, double)}
     * usually do not work on them.
     *
     * @param entity   The {@link Entity} from whose {@code yaw} and {@code pitch} to cast the ray.
     * @param distance The maximum distance this ray will travel.
     * @return An {@link EntityRayTraceResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3d} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityRayTraceResult} will be {@code null}.
     */
    @Nullable
    public static EntityRayTraceResult rayTraceEntityFromPitchAndYaw(@Nonnull Entity entity, double distance) {
        Vec3d from = entity.getEyePosition(1.0f);
        float theta = entity.rotationYaw;
        if (theta < 0) {
            theta += 360;
        }
        theta = degToRad(theta);
        float phi = entity.rotationPitch;
        if (phi < 0) {
            phi += 360;
        }
        phi = degToRad(phi);
        Vec3d looking = new Vec3d(sin(theta), sin(phi), cos(theta)).normalize();
        Vec3d to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return rayTraceEntities(entity, from, to, new AxisAlignedBB(from, to), distance * distance);
    }

    /**
     * Relativizes a value to be between {@code 0.0f} and {@code 1.0f} inside a scale.
     *
     * @param value The value to relativize.
     * @param min   The mininum allowed value, representing the {@code 0.0f} in the scale.
     * @param max   The maximum allowed value, representing the {code 1.0f} in the scale.
     * @return A value between {@code 0.0f} and {@code 1.0f},
     * where {@code 0.0f} represents the mininum of the scale and {@code 1.0f} represents the maximum.
     */
    public static float relativize(float value, float min, float max) {
        value = clamp(value, min, max);
        value -= min;
        float delta = max - min;
        value /= delta;
        return value;
    }

    /**
     * Resets a {@link Nonnull} {@code int} vector starting at the desired index to {@code -1}.
     *
     * @param array The {@code int} vector to reset.
     * @param index The index to start the reset.
     */
    public static void resetArray(@Nonnull int[] array, int index) {
        for (int i = index; i < array.length; i++) {
            array[i] = -1;
        }
    }

    /**
     * Resets a {@link Nonnull} {@code boolean} tensor starting at the desired index to {@code null}.
     *
     * @param tensor A {@code boolean} tensor to reset.
     * @param index  The index to start the reset.
     */
    public static void resetTensor(@Nonnull boolean[][][] tensor, int index) {
        for (int i = index; i < tensor.length; i++) {
            tensor[i] = null;
        }
    }

    /**
     * Rotates a {@link Nonnull} square {@code boolean} matrix clockwise.
     *
     * @param input A square {@code boolean} matrix.
     * @return A {@code new} square {@code boolean} matrix rotated 90 degrees clockwise.
     */
    @Nonnull
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
     * Rotates a {@link VoxelShape}.
     *
     * @param from  The {@link Direction} the {@link VoxelShape} is originally facing.
     * @param to    The desired {@link Direction} for the {@link VoxelShape} to face.
     * @param shape The {@link VoxelShape} to rotate.
     * @return A {@code new} {@link VoxelShape} rotated if the {@link Direction}s are different,
     * otherwise returns the same {@link VoxelShape}.
     */
    public static VoxelShape rotateShape(@Nonnull Direction from, @Nonnull Direction to, @Nonnull VoxelShape shape) {
        if (to == from) {
            return shape;
        }
        VoxelShape[] buffer = {shape, VoxelShapes.empty()};
        int times = (to.getHorizontalIndex() - from.getHorizontalIndex() + 4) % 4;
        for (int i = 0; i < times; i++) {
            //noinspection ObjectAllocationInLoop
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1],
                                                                                                    VoxelShapes.create(1 - maxZ,
                                                                                                                       minY,
                                                                                                                       minX,
                                                                                                                       1 - minZ,
                                                                                                                       maxY,
                                                                                                                       maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }
        return buffer[0];
    }

    /**
     * Sets the rotation angles of a {@link RendererModel}.
     *
     * @param model The model to set the rotation angles.
     * @param x     The rotation angle around the x axis.
     * @param y     The rotation angle around the y axis.
     * @param z     The rotation angle around the z axis.
     */
    public static void setRotationAngle(@Nonnull RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public static void setRotationPivot(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotationPointX = x;
        modelRenderer.rotationPointY = y;
        modelRenderer.rotationPointZ = z;
    }

    /**
     * Approximates the trigonometric function sine.
     *
     * @param rad The argument of the sine, given in radians.
     * @return An approximation of the sine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float sin(@Radian float rad) {
        return net.minecraft.util.math.MathHelper.sin(rad);
    }

    /**
     * Approximates the trigonometric function sine.
     *
     * @param deg The argument of the sine, given in degrees.
     * @return An approximation of the sine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float sinDeg(@Degree float deg) {
        return net.minecraft.util.math.MathHelper.sin(degToRad(deg));
    }

    /**
     * Calculates the square root.
     *
     * @param value The value to calculate the square root from.
     * @return A {@code float} value of the resulting square root.
     */
    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    /**
     * Calculates the square root.
     *
     * @param value The value to calculate the square root from.
     * @return A {@code float} value of the resulting square root.
     */
    public static float sqrt(double value) {
        return (float) Math.sqrt(value);
    }

    /**
     * Substracts one {@link VoxelShape} from the other.
     *
     * @param A The {@link VoxelShape} to subtract from
     * @param B The {@link VoxelShape} to subtract
     * @return A {@code new} {@link VoxelShape}, consisting of the {@link VoxelShape} A minus B.
     */
    @Nonnull
    public static VoxelShape subtract(@Nonnull VoxelShape A, @Nonnull VoxelShape B) {
        return VoxelShapes.combine(A, B, IBooleanFunction.ONLY_FIRST);
    }

    /**
     * Approximates the trigonometric function tangent.
     *
     * @param rad The argument of the tangent, given in radians.
     * @return An approximation of the tangent of the given argument.
     * The returned value will be between {@link Float#NEGATIVE_INFINITY} and {@link Float#POSITIVE_INFINITY}.
     */
    public static float tan(@Radian float rad) {
        return net.minecraft.util.math.MathHelper.sin(rad) / net.minecraft.util.math.MathHelper.cos(rad);
    }

    /**
     * Used to compare two {@link Nonnull} {@code boolean} tensors of the same size.
     *
     * @param a The first tensor.
     * @param b The second tensor.
     * @return {@code true} if the tensors are equal, {@code false} otherwise.
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
     * Converts an {@code int} value to {@code byte}.
     *
     * @param value The value to convert.
     * @return The value converted to {@code byte}.
     * @throws ArithmeticException if the value cannot be represented as a {@code byte}.
     */
    public static byte toByteExact(int value) {
        if ((byte) value != value) {
            throw new ArithmeticException("Byte overflow " + value);
        }
        return (byte) value;
    }

    /**
     * Converts an {@code int} value to {@code short}.
     *
     * @param value The value to convert.
     * @return The value converted to {@code short}.
     * @throws ArithmeticException if the value cannot be represented as a {@code short}.
     */
    public static short toShortExact(int value) {
        if ((short) value != value) {
            throw new ArithmeticException("Short overflow " + value);
        }
        return (short) value;
    }

    /**
     * Translates a {@link Nonnull} square {@code boolean} matrix 1 unit to the right, wrapping values around.
     *
     * @param input A square {@code boolean} matrix.
     * @return A {@code new} square {@code boolean} matrix translated one index to the right and wrapped around.
     */
    @Nonnull
    public static boolean[][] translateRight(@Nonnull boolean[][] input) {
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            output[i][0] = input[i][input.length - 1];
            System.arraycopy(input[i], 0, output[i], 1, input.length - 1);
        }
        return output;
    }

    /**
     * Unites two {@link VoxelShape}s.
     *
     * @param A The first {@link VoxelShape}.
     * @param B The second {@link VoxelShape}.
     * @return A {@code new} {@link VoxelShape} made of the union of both {@link VoxelShape}s.
     */
    @Nonnull
    public static VoxelShape union(@Nonnull VoxelShape A, @Nonnull VoxelShape B) {
        return VoxelShapes.combine(A, B, IBooleanFunction.OR);
    }

    /**
     * Wraps the angle given in degrees in the range [-180º; 180º)
     *
     * @param value The angle value in degrees.
     * @return The equivalent angle value wrapped.
     */
    public static double wrapDegrees(@Degree double value) {
        return net.minecraft.util.math.MathHelper.wrapDegrees(value);
    }

    /**
     * Wraps the angle given in degrees in the range [-180º; 180º)
     *
     * @param value The angle value in degrees.
     * @return The equivalent angle value wrapped.
     */
    public static float wrapDegrees(@Degree float value) {
        return net.minecraft.util.math.MathHelper.wrapDegrees(value);
    }
}
