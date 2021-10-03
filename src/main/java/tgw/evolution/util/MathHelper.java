package tgw.evolution.util;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.Patterns;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.hitbox.Matrix3d;
import tgw.evolution.util.reflection.FieldHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
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
    private static final Predicate<Entity> PREDICATE = e -> e != null && !e.isSpectator() && e.isPickable();
    private static final FieldHandler<LivingEntity, Float> LAST_SWIM = new FieldHandler<>(LivingEntity.class, "field_205018_bM");
    private static final FieldHandler<LivingEntity, Float> SWIM = new FieldHandler<>(LivingEntity.class, "field_205017_bL");

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
     * Checks whether two {@link ItemStack} are sufficiently equal.
     * This means that they are the same item.
     *
     * @return {@code true} if the {@link ItemStack}s are sufficiently equal, {@code false} otherwise.
     */
    public static boolean areItemStacksSufficientlyEqual(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        return a.getItem() == b.getItem();
    }

    /**
     * Calculates the arc-tangent of a vector given two components.
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
     * Calculates the arc-tangent of a vector given two components.
     *
     * @param y The y component of the vector.
     * @param x The x component of the vector.
     * @return The angle between the vector and the X axis, given in degrees.
     */
    @Degree
    public static double atan2Deg(double y, double x) {
        return Math.toDegrees(net.minecraft.util.math.MathHelper.atan2(y, x));
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
     * Attempts to calculate the size of a file or directory.
     * <p>
     * Since the operation is non-atomic, the returned value may be inaccurate.
     * However, this method is quick and does its best.
     */
    public static long calculateSizeOnDisk(Path path) {
        final AtomicLong size = new AtomicLong(0);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                    if (e != null) {
                        Evolution.LOGGER.warn("Had trouble traversing: " + dir + " (" + e + ")");
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    Evolution.LOGGER.warn("Skipped: " + file + " (" + exc + ")");
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }
        return size.get();
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

    public static float clampAngle(float angle, float sweep, Direction dir) {
        if (sweep >= 360) {
            return angle;
        }
        switch (dir) {
            case SOUTH: {
                if (angle > sweep / 2) {
                    return sweep / 2;
                }
                return Math.max(angle, -sweep / 2);
            }
            case WEST: {
                if (angle >= -90 && angle <= 90) {
                    if (angle < 90 - sweep / 2) {
                        return 90 - sweep / 2;
                    }
                }
                else {
                    if (sweep / 2 < 90) {
                        if (angle > 90 + sweep / 2 || angle < 0) {
                            return 90 + sweep / 2;
                        }
                    }
                    else {
                        if (angle > -270 + sweep / 2 && angle < 0) {
                            return -270 + sweep / 2;
                        }
                    }
                }
                return angle;
            }
            case NORTH: {
                if (angle >= 0) {
                    if (angle < 180 - sweep / 2) {
                        return 180 - sweep / 2;
                    }
                }
                else {
                    if (angle > -180 + sweep / 2) {
                        return -180 + sweep / 2;
                    }
                }
                return angle;
            }
            case EAST: {
                if (angle < 90 && angle > -90) {
                    if (angle > -90 + sweep / 2) {
                        return -90 + sweep / 2;
                    }
                }
                else {
                    if (sweep / 2 <= 90) {
                        if (angle < -90 - sweep / 2 || angle > 0) {
                            return -90 - sweep / 2;
                        }
                    }
                    else {
                        if (angle < 270 - sweep / 2 && angle > 0) {
                            return 270 - sweep / 2;
                        }
                    }
                }
                return angle;
            }
        }
        return angle;
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

    public static int computeNormal(Matrix3f normalMatrix, Direction facing) {
        return ((IMatrix3fPatch) (Object) normalMatrix).computeNormal(facing);
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
        return net.minecraft.util.math.MathHelper.cos(wrapRadians(rad));
    }

    /**
     * Approximates the trigonometric function cosine.
     *
     * @param deg The argument of the cosine, given in degrees.
     * @return An approximation of the cosine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float cosDeg(@Degree float deg) {
        return net.minecraft.util.math.MathHelper.cos(degToRad(wrapDegrees(deg)));
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
     * Calculates the shortest distance between two points in euclidean space.
     *
     * @param x  The x coordinate of the first point.
     * @param y  The y coordinate of the first point.
     * @param z  The z coordinate of the first point.
     * @param x0 The x coordinate of the second point.
     * @param y0 The y coordinate of the second point.
     * @param z0 The z coordinate of the second point.
     * @return The shortest distance between the points as a {@code float} value.
     * The distance will never be negative.
     */
    public static float distance(double x, double y, double z, double x0, double y0, double z0) {
        return sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0));
    }

    public static boolean epsilonEquals(double x, double y) {
        return Math.abs(y - x) < 1.0E-5F;
    }

    /**
     * Approximates a {@code float} value to an {@code int}, rounding down.
     *
     * @param value The value to approximate.
     * @return An {@code int} value rounded down.
     */
    public static int floor(float value) {
        return net.minecraft.util.math.MathHelper.floor(value);
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

    public static VoxelShape generateShapeFromPattern(long pattern) {
        VoxelShape shape = VoxelShapes.empty();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((pattern >> (7 - j) * 8 + 7 - i & 1) != 0) {
                    shape = union(shape, EvolutionHitBoxes.KNAPPING_PART.move(i / 8.0f, 0, j / 8.0f));
                }
            }
        }
        return shape;
    }

    public static float getAgeInTicks(LivingEntity entity, float partialTicks) {
        return entity.tickCount + partialTicks;
    }

    public static float getAngleByDirection(Direction dir) {
        switch (dir) {
            case SOUTH: {
                return 90.0F;
            }
            case NORTH: {
                return 270.0F;
            }
            case EAST: {
                return 180.0F;
            }
            default: {
                return 0.0F;
            }
        }
    }

    public static ArmPose getArmPose(LivingEntity entity, ItemStack mainhandStack, ItemStack offhandStack, Hand hand) {
        ArmPose armPose = ArmPose.EMPTY;
        ItemStack stack = hand == Hand.MAIN_HAND ? mainhandStack : offhandStack;
        if (!stack.isEmpty()) {
            armPose = ArmPose.ITEM;
            if (entity.getTicksUsingItem() > 0 && hand == entity.getUsedItemHand()) {
                UseAction useaction = stack.getUseAnimation();
                switch (useaction) {
                    case BLOCK: {
                        return ArmPose.BLOCK;
                    }
                    case BOW: {
                        return ArmPose.BOW_AND_ARROW;
                    }
                    case SPEAR: {
                        return ArmPose.THROW_SPEAR;
                    }
                    case CROSSBOW: {
                        return ArmPose.CROSSBOW_CHARGE;
                    }
                }
            }
            else {
                boolean mainhandHasCrossbow = mainhandStack.getItem() == Items.CROSSBOW;
                boolean mainhandIsCharged = CrossbowItem.isCharged(mainhandStack);
                boolean offhandHasCrossbow = offhandStack.getItem() == Items.CROSSBOW;
                boolean offhandIsCharged = CrossbowItem.isCharged(offhandStack);
                if (mainhandHasCrossbow && mainhandIsCharged) {
                    armPose = ArmPose.CROSSBOW_HOLD;
                }
                if (offhandHasCrossbow && offhandIsCharged && mainhandStack.getItem().getUseAnimation(mainhandStack) == UseAction.NONE) {
                    return ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return armPose;
    }

    public static float getAttackAnim(LivingEntity entity, float partialTick) {
        float f = entity.attackAnim - entity.oAttackAnim;
        if (f < 0.0F) {
            ++f;
        }
        return entity.oAttackAnim + f * partialTick;
    }

    public static Matrix3d getBodyRotationMatrix(Entity entity, float partialTicks) {
        float angle = -getEntityBodyYaw(entity, partialTicks);
        return new Matrix3d().asYRotation(degToRad(angle));
    }

    public static Vector3d getCameraPosition(Entity entity, float partialTicks) {
        if (entity.getPose() == Pose.SLEEPING) {
            return new Vector3d(entity.getX(), entity.getY() + 0.17, entity.getZ());
        }
        float yaw = entity.getViewYRot(partialTicks);
        float pitch = entity.getViewXRot(partialTicks);
        float cosBodyYaw;
        float sinBodyYaw;
        float sinYaw = sinDeg(yaw);
        float cosYaw = cosDeg(yaw);
        if (entity instanceof LivingEntity) {
            float bodyYaw = lerpAngles(partialTicks, ((LivingEntity) entity).yBodyRotO, ((LivingEntity) entity).yBodyRot);
            cosBodyYaw = cosDeg(bodyYaw);
            sinBodyYaw = sinDeg(bodyYaw);
        }
        else {
            cosBodyYaw = cosYaw;
            sinBodyYaw = sinYaw;
        }
        float sinPitch = sinDeg(pitch);
        float cosPitch = cosDeg(pitch);
        float zOffset = ((INeckPosition) entity).getCameraZOffset();
        float yOffset = ((INeckPosition) entity).getCameraYOffset();
        Vector3d neckPoint = ((INeckPosition) entity).getNeckPoint();
        float actualYOffset = yOffset * cosPitch - zOffset * sinPitch;
        float horizontalOffset = yOffset * sinPitch + zOffset * cosPitch;
        double x = lerp(partialTicks, entity.xo, entity.getX()) - horizontalOffset * sinYaw + neckPoint.x * cosBodyYaw - neckPoint.z * sinBodyYaw;
        double y = lerp(partialTicks, entity.yo, entity.getY()) + neckPoint.y + actualYOffset;
        double z = lerp(partialTicks, entity.zo, entity.getZ()) + horizontalOffset * cosYaw + neckPoint.x * sinBodyYaw + neckPoint.z * cosBodyYaw;
        return new Vector3d(x, y, z);
    }

    public static float getEntityBodyYaw(Entity entity, float partialTicks) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            return partialTicks == 1.0F ? living.yBodyRot : lerp(partialTicks, living.yBodyRotO, living.yBodyRot);
        }
        return partialTicks == 1.0F ? entity.yRot : lerp(partialTicks, entity.yRotO, entity.yRot);
    }

    public static IMatrix4fPatch getExtendedMatrix(Matrix4f matrix) {
        return (IMatrix4fPatch) (Object) matrix;
    }

    public static IMatrix3fPatch getExtendedMatrix(Matrix3f matrix) {
        return (IMatrix3fPatch) (Object) matrix;
    }

    public static HandSide getHandSide(LivingEntity entity) {
        HandSide handSide = entity.getMainArm();
        return entity.swingingArm == Hand.MAIN_HAND ? handSide : handSide.getOpposite();
    }

    public static Matrix3d getHeadRotationMatrix(LivingEntity entity, float partialTick) {
        float yaw = -entity.getViewYRot(partialTick);
        float pitch = -entity.getViewXRot(partialTick);
        Matrix3d xRot = new Matrix3d().asXRotation(degToRad(pitch));
        Matrix3d yRot = new Matrix3d().asYRotation(degToRad(yaw));
        return xRot.multiply(yRot);
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

    public static float getLimbSwing(LivingEntity entity, float partialTicks) {
        float limbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTicks);
        if (entity.isBaby()) {
            limbSwing *= 3.0F;
        }
        return limbSwing;
    }

    public static float getLimbSwingAmount(LivingEntity entity, float partialTicks) {
        float limbSwingAmount = lerp(partialTicks, entity.animationSpeedOld, entity.animationSpeed);
        return clampMax(limbSwingAmount, 1.0F);
    }

    /**
     * Gets the negative {@link Direction} of a given {@link Axis}.
     *
     * @param axis The desired {@link Axis}.
     * @return The negative {@link Direction} on that {@link Axis}.
     */
    public static Direction getNegativeAxis(@Nonnull Direction.Axis axis) {
        switch (axis) {
            case X: {
                return Direction.WEST;
            }
            case Y: {
                return Direction.DOWN;
            }
            case Z: {
                return Direction.NORTH;
            }
            default: {
                throw new IllegalStateException("Unknown axis: " + axis);
            }
        }
    }

    public static float getNetHeadYaw(LivingEntity entity, float partialTicks) {
        float interpYaw = lerpAngles(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = lerpAngles(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        return headYaw - interpYaw;
    }

    /**
     * Gets a {@code long} seed based on a {@link Vector3i}.
     *
     * @param vec The {@link Vector3i} to base the seed in.
     * @return A {@code long} containing a seed to this {@link Vector3i}.
     */
    public static long getPositionRandom(Vector3i vec) {
        return net.minecraft.util.math.MathHelper.getSeed(vec);
    }

    /**
     * Gets the positive {@link Direction} of a given {@link Axis}.
     *
     * @param axis The desired {@link Axis}.
     * @return The positive {@link Direction} on that {@link Axis}.
     */
    public static Direction getPositiveAxis(@Nonnull Direction.Axis axis) {
        switch (axis) {
            case X: {
                return Direction.EAST;
            }
            case Y: {
                return Direction.UP;
            }
            case Z: {
                return Direction.SOUTH;
            }
            default: {
                throw new IllegalStateException("Unknown axis: " + axis);
            }
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

    public static float getSwimAnimation(LivingEntity entity, float partialTicks) {
        return lerp(partialTicks, LAST_SWIM.get(entity), SWIM.get(entity));
    }

    /**
     * Offsets a hit position to be within a bounding box.
     *
     * @param axis      The desired {@link Axis} to offset.
     * @param hit       The position of the hit.
     * @param direction The {@link Direction} from where the hit came from.
     * @return A hit position offset in the desired {@link Direction}
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
    public static float horizontalLength(Vector3d vec) {
        return sqrt(vec.x * vec.x + vec.z * vec.z);
    }

    public static boolean isInInterval(float value, float middlePoint, float length) {
        float start = middlePoint - length / 2;
        float end = middlePoint + length / 2;
        return start <= value && value <= end;
    }

    public static boolean isMouseInsideBox(double mouseX, double mouseY, int x0, int y0, int x1, int y1) {
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
        return !VoxelShapes.joinIsNotEmpty(reference, inside, IBooleanFunction.ONLY_SECOND);
    }

    /**
     * Calculates if the {@link VoxelShape} is totally outside another one.
     *
     * @param outside   The {@link VoxelShape} that should be totally outside the reference.
     * @param reference The reference {@link VoxelShape}.
     * @return {@code true} if the first {@link VoxelShape} is totally outside the reference one, {@code false} otherwise.
     */
    public static boolean isShapeTotallyOutside(VoxelShape outside, VoxelShape reference) {
        return !VoxelShapes.joinIsNotEmpty(reference, outside, IBooleanFunction.AND);
    }

    public static boolean isSitting(LivingEntity entity) {
        return entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
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

    public static float lerpAngles(float partialTicks, float prevAngle, float angle) {
        return prevAngle + partialTicks * wrapDegrees(angle - prevAngle);
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

    @Nonnull
    public static BlockRayTraceResult rayTraceBlocksFromCamera(@Nonnull Entity entity,
                                                               Vector3d cameraPos,
                                                               float partialTicks,
                                                               double distance,
                                                               boolean fluid) {
        Vector3d look = entity.getViewVector(partialTicks);
        Vector3d to = cameraPos.add(look.x * distance, look.y * distance, look.z * distance);
        return entity.level.clip(new RayTraceContext(cameraPos,
                                                     to,
                                                     RayTraceContext.BlockMode.OUTLINE,
                                                     fluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                                                     entity));
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
    public static BlockRayTraceResult rayTraceBlocksFromEyes(@Nonnull Entity entity, float partialTicks, double distance, boolean fluid) {
        Vector3d from = entity.getEyePosition(partialTicks);
        Vector3d look = entity.getViewVector(partialTicks);
        Vector3d to = from.add(look.x * distance, look.y * distance, look.z * distance);
        return entity.level.clip(new RayTraceContext(from,
                                                     to,
                                                     RayTraceContext.BlockMode.OUTLINE,
                                                     fluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                                                     entity));
    }

    /**
     * Casts a ray tracing for {@code Block}s based on the {@link Entity}'s {@link Entity#yRot} and {@link Entity#xRot}.
     * This method is useful for Entities that are projectiles, as {@link MathHelper#rayTraceBlocksFromEyes(Entity, float, double, boolean)}
     * usually do not work on them.
     *
     * @param entity   The {@link Entity} from whose {@code yaw} and {@code pitch} to cast the ray.
     * @param distance The maximum distance this ray will travel.
     * @param fluid    {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockRayTraceResult} containing the {@link BlockPos} of the {@code Block} hit.
     */
    @Nonnull
    public static BlockRayTraceResult rayTraceBlocksFromYawAndPitch(@Nonnull Entity entity, double distance, boolean fluid) {
        Vector3d from = entity.getEyePosition(1.0f);
        float theta = entity.yRot;
        if (theta < 0) {
            theta += 360;
        }
        theta = degToRad(theta);
        float phi = entity.xRot;
        if (phi < 0) {
            phi += 360;
        }
        phi = degToRad(phi);
        Vector3d looking = new Vector3d(sin(theta), sin(phi), cos(theta)).normalize();
        Vector3d to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return entity.level.clip(new RayTraceContext(from,
                                                     to,
                                                     RayTraceContext.BlockMode.OUTLINE,
                                                     fluid ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                                                     entity));
    }

    /**
     * Casts a ray tracing for Entities inside an {@link AxisAlignedBB} from the starting vector to the end vector.
     *
     * @param toExclude       The {@link Entity} to exclude from this raytrace.
     * @param startVec        The {@link Vector3d} representing the start of the raytrace.
     * @param endVec          The {@link Vector3d} representing the end of the raytrace.
     * @param boundingBox     The {@link AxisAlignedBB} to look for entities inside.
     * @param distanceSquared The max distance this raytrace will travel, squared.
     * @return A {@link EntityRayTraceResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3d} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityRayTraceResult} will be {@code null}.
     */
    @Nullable
    public static EntityRayTraceResult rayTraceEntities(@Nonnull Entity toExclude,
                                                        Vector3d startVec,
                                                        Vector3d endVec,
                                                        AxisAlignedBB boundingBox,
                                                        double distanceSquared) {
        World world = toExclude.level;
        double range = distanceSquared;
        Entity entity = null;
        Vector3d vec3d = null;
        for (Entity entityInBoundingBox : world.getEntities(toExclude, boundingBox, PREDICATE)) {
            AxisAlignedBB axisalignedbb = entityInBoundingBox.getBoundingBox();
            Optional<Vector3d> optional = axisalignedbb.clip(startVec, endVec);
            if (axisalignedbb.contains(startVec)) {
                if (range >= 0) {
                    entity = entityInBoundingBox;
                    vec3d = optional.orElse(startVec);
                    range = 0;
                }
            }
            else if (optional.isPresent()) {
                Vector3d hitResult = optional.get();
                double actualDistanceSquared = startVec.distanceToSqr(hitResult);
                if (actualDistanceSquared < range || range == 0) {
                    if (entityInBoundingBox.getRootVehicle() == toExclude.getRootVehicle() && !entityInBoundingBox.canRiderInteract()) {
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
        Vector3d from = entity.getEyePosition(partialTicks);
        Vector3d look = entity.getViewVector(partialTicks).normalize();
        Vector3d to = from.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        return rayTraceEntities(entity, from, to, new AxisAlignedBB(from, to), reachDistance * reachDistance);
    }

    /**
     * Casts a ray tracing for {@code Entities} based on the {@link Entity}'s {@link Entity#yRot} and {@link Entity#xRot}.
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
        Vector3d from = entity.getEyePosition(1.0f);
        float theta = entity.yRot;
        if (theta < 0) {
            theta += 360;
        }
        theta = degToRad(theta);
        float phi = entity.xRot;
        if (phi < 0) {
            phi += 360;
        }
        phi = degToRad(phi);
        Vector3d looking = new Vector3d(sin(theta), sin(phi), cos(theta)).normalize();
        Vector3d to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return rayTraceEntities(entity, from, to, new AxisAlignedBB(from, to), distance * distance);
    }

    /**
     * Casts a ray tracing for Entities starting from the eyes of the desired {@link Entity} and extends to its reach distance.
     * The ray will only collide with OBBs.
     *
     * @param entity        The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks  The partial tick to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param reachDistance The max distance the ray will travel.
     * @return An {@link EntityRayTraceResult} containing the {@link Entity} hit by the ray traced and a {@link Vec3d}
     * containing the position of the hit. If no {@link Entity} was hit by the ray, this {@link EntityRayTraceResult} will be {@code null}.
     */
    @Nullable
    public static EntityRayTraceResult rayTraceOBBEntityFromEyes(@Nonnull Entity entity,
                                                                 @Nullable Vector3d cameraPos,
                                                                 float partialTicks,
                                                                 double reachDistance) {
        Vector3d from = cameraPos != null ? cameraPos : entity.getEyePosition(partialTicks);
        Vector3d look = entity.getViewVector(partialTicks).normalize();
        Vector3d to = from.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        World world = entity.level;
        double rangeSq = reachDistance * reachDistance;
        Entity foundEntity = null;
        Vector3d vec3d = null;
        double[] distance = {1.0};
        Hitbox[] hitbox = new Hitbox[1];
        Hitbox box = null;
        List<Entity> foundEntities = world.getEntities(entity, new AxisAlignedBB(from, to).inflate(1.5), PREDICATE);
        for (Entity entityInBoundingBox : foundEntities) {
            Optional<Vector3d> optional = rayTracingEntityHitboxes(entityInBoundingBox, from, look, reachDistance, partialTicks, distance, hitbox);
            if (optional.isPresent()) {
                Vector3d hitResult = optional.get();
                if (distance[0] < rangeSq || rangeSq == 0) {
                    foundEntity = entityInBoundingBox;
                    vec3d = hitResult;
                    rangeSq = distance[0];
                    box = hitbox[0];
                    if (rangeSq == 0) {
                        break;
                    }
                }
            }
        }
        if (foundEntity == null) {
            return null;
        }
        return new AdvancedEntityRayTraceResult(foundEntity, vec3d, box);
    }

    /**
     * Ray traces an Entity's hitbox. If the Entity has no defined OBBs, it will default to the Bounding Box. If the Entity has OBBs,
     * the ray will only collide with the nearest OBB to its origin.
     *
     * @param entity       The Entity the ray is being cast on.
     * @param from         The origin of the ray.
     * @param look         The direction of the ray.
     * @param reach        How far the ray will extend for.
     * @param partialTicks The partial tick for interpolation.
     * @param distance     Will return the distance squared for the hit result. Must be a double[] with length 1.
     * @param chosenBox    Will return the OBB hit, if any. Must be a Hitbox[] with length 1.
     * @param <T>          The type of the Entity the ray is being cast on.
     * @return An optional with the hit result, if any, or an empty optional.
     */
    @Nonnull
    public static <T extends Entity> Optional<Vector3d> rayTracingEntityHitboxes(T entity,
                                                                                 Vector3d from,
                                                                                 Vector3d look,
                                                                                 double reach,
                                                                                 float partialTicks,
                                                                                 double[] distance,
                                                                                 Hitbox[] chosenBox) {
        chosenBox[0] = null;
        distance[0] = reach * reach;
        if (!((IEntityPatch) entity).hasHitboxes()) {
            Vector3d to = from.add(look.x * reach, look.y * reach, look.z * reach);
            Optional<Vector3d> optional = entity.getBoundingBox().clip(from, to);
            if (optional.isPresent()) {
                double actualDistanceSquared = from.distanceToSqr(optional.get());
                distance[0] = actualDistanceSquared;
                return optional;
            }
            if (entity.getBoundingBox().contains(from)) {
                distance[0] = 0;
                return Optional.of(from);
            }
            return Optional.empty();
        }
        HitboxEntity<T> hitbox = (HitboxEntity<T>) ((IEntityPatch) entity).getHitboxes();
        hitbox.init(entity, partialTicks);
        double posX = lerp(partialTicks, entity.xOld, entity.getX());
        double posY = lerp(partialTicks, entity.yOld, entity.getY());
        double posZ = lerp(partialTicks, entity.zOld, entity.getZ());
        from = from.subtract(posX, posY, posZ);
        Vector3d mainOffset = hitbox.getOffset();
        Matrix3d mainTransform = hitbox.getTransform();
        from = from.subtract(mainOffset);
        from = mainTransform.transform(from);
        look = mainTransform.transform(look);
        mainTransform.transpose();
        Vector3d result = null;
        for (Hitbox box : hitbox.getBoxes()) {
            Vector3d newFrom = from;
            Vector3d newLook = look;
            Vector3d offset = box.getOffset();
            Matrix3d transform = box.getTransformation();
            newFrom = newFrom.subtract(offset);
            newFrom = transform.transform(newFrom);
            newLook = transform.transform(newLook);
            Vector3d to = newFrom.add(newLook.x * reach, newLook.y * reach, newLook.z * reach);
            Optional<Vector3d> optional = box.getAABB().clip(newFrom, to);
            if (optional.isPresent() || box.getAABB().contains(newFrom)) {
                Vector3d hitResult = optional.orElse(newFrom);
                double actualDistanceSquared = newFrom.distanceToSqr(hitResult);
                if (actualDistanceSquared < distance[0]) {
                    distance[0] = actualDistanceSquared;
                    result = transform.transpose().transform(hitResult);
                    result = result.add(offset);
                    result = mainTransform.transform(result);
                    result = result.add(mainOffset);
                    result = result.add(posX, posY, posZ);
                    chosenBox[0] = box;
                    if (actualDistanceSquared == 0) {
                        break;
                    }
                }
            }
        }
        if (result != null) {
            return Optional.of(result);
        }
        return Optional.empty();
    }

    /**
     * Relativizes a value to be between {@code 0.0f} and {@code 1.0f} inside a scale.
     *
     * @param value The value to relativize.
     * @param min   The minimum allowed value, representing the {@code 0.0f} in the scale.
     * @param max   The maximum allowed value, representing the {@code 1.0f} in the scale.
     * @return A value between {@code 0.0f} and {@code 1.0f},
     * where {@code 0.0f} represents the minimum of the scale and {@code 1.0f} represents the maximum.
     */
    public static float relativize(float value, float min, float max) {
        value = clamp(value, min, max);
        value -= min;
        float delta = max - min;
        value /= delta;
        return value;
    }

    /**
     * Relativizes a value to be between {@code 0.0} and {@code 1.0} inside a scale.
     *
     * @param value The value to relativize.
     * @param min   The minimum allowed value, representing the {@code 0.0} in the scale.
     * @param max   The maximum allowed value, representing the {@code 1.0} in the scale.
     * @return A value between {@code 0.0} and {@code 1.0},
     * where {@code 0.0} represents the minimum of the scale and {@code 1.0} represents the maximum.
     */
    public static double relativize(double value, double min, double max) {
        value = clamp(value, min, max);
        value -= min;
        double delta = max - min;
        value /= delta;
        return value;
    }

    /**
     * Resets a {@link Nonnull} {@code long} array (representing a 8x8x8 boolean tensor) starting at the desired index to
     * {@link tgw.evolution.blocks.tileentities.Patterns#MATRIX_FALSE}.
     *
     * @param tensor A {@code long} tensor to reset.
     * @param index  The index to start the reset.
     */
    public static void resetTensor(@Nonnull long[] tensor, int index) {
        for (int i = index; i < tensor.length; i++) {
            tensor[i] = Patterns.MATRIX_FALSE;
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
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            //noinspection ObjectAllocationInLoop
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1],
                                                                                                     VoxelShapes.box(1 - maxZ,
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
     * Sets the rotation angles of a {@link ModelRenderer}.
     *
     * @param model The model to set the rotation angles.
     * @param x     The rotation angle around the x-axis.
     * @param y     The rotation angle around the y-axis.
     * @param z     The rotation angle around the z-axis.
     */
    public static void setRotationAngle(@Nonnull ModelRenderer model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    public static void setRotationPivot(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.x = x;
        modelRenderer.y = y;
        modelRenderer.z = z;
    }

    /**
     * Approximates the trigonometric function sine.
     *
     * @param rad The argument of the sine, given in radians.
     * @return An approximation of the sine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float sin(@Radian float rad) {
        return net.minecraft.util.math.MathHelper.sin(wrapRadians(rad));
    }

    /**
     * Approximates the trigonometric function sine.
     *
     * @param deg The argument of the sine, given in degrees.
     * @return An approximation of the sine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float sinDeg(@Degree float deg) {
        return net.minecraft.util.math.MathHelper.sin(degToRad(wrapDegrees(deg)));
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
     * Subtracts one {@link VoxelShape} from the other.
     *
     * @param A The {@link VoxelShape} to subtract from
     * @param B The {@link VoxelShape} to subtract
     * @return A {@code new} {@link VoxelShape}, consisting of the {@link VoxelShape} A minus B.
     */
    @Nonnull
    public static VoxelShape subtract(@Nonnull VoxelShape A, @Nonnull VoxelShape B) {
        return VoxelShapes.join(A, B, IBooleanFunction.ONLY_FIRST);
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

    public static int transformPackedNormal(int norm, Matrix3f matrix) {
        IMatrix3fPatch mat = getExtendedMatrix(matrix);
        float normX1 = Norm3b.unpackX(norm);
        float normY1 = Norm3b.unpackY(norm);
        float normZ1 = Norm3b.unpackZ(norm);
        float normX2 = mat.transformVecX(normX1, normY1, normZ1);
        float normY2 = mat.transformVecY(normX1, normY1, normZ1);
        float normZ2 = mat.transformVecZ(normX1, normY1, normZ1);
        return Norm3b.pack(normX2, normY2, normZ2);
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
        return VoxelShapes.join(A, B, IBooleanFunction.OR);
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

    /**
     * Wraps the angle given in radians in the range [-pi; pi)
     *
     * @param value The angle value in radians.
     * @return The equivalent angle value wrapped.
     */
    public static double wrapRadians(@Radian double value) {
        double d = value % (Math.PI * 2);
        if (d >= Math.PI) {
            d -= Math.PI * 2;
        }
        if (d < -Math.PI) {
            d += Math.PI * 2;
        }
        return d;
    }

    /**
     * Wraps the angle given in radians in the range [-pi; pi)
     *
     * @param value The angle value in radians.
     * @return The equivalent angle value wrapped.
     */
    public static float wrapRadians(@Radian float value) {
        float d = value % TAU;
        if (d >= PI) {
            d -= TAU;
        }
        if (d < -PI) {
            d += TAU;
        }
        return d;
    }
}
