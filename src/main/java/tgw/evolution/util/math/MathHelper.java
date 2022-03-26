package tgw.evolution.util.math;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.Patterns;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.patches.*;
import tgw.evolution.util.AdvancedEntityRayTraceResult;
import tgw.evolution.util.HitInformation;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.hitbox.HitboxType;
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
import java.text.Collator;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class MathHelper {

    public static final float PI = (float) Math.PI;
    public static final float TAU = 2.0f * PI;
    public static final float PI_OVER_2 = PI / 2.0f;
    public static final float SQRT_2 = sqrt(2.0f);
    public static final float SQRT_2_OVER_2 = SQRT_2 / 2.0f;
    public static final Random RANDOM = new Random();
    public static final DirectionDiagonal[][] DIAGONALS = {{DirectionDiagonal.NORTH_WEST, DirectionDiagonal.NORTH_EAST},
                                                           {DirectionDiagonal.SOUTH_WEST, DirectionDiagonal.SOUTH_EAST}};
    public static final InteractionHand[] HANDS = InteractionHand.values();
    public static final InteractionHand[] HANDS_LEFT_PRIORITY = {InteractionHand.OFF_HAND, InteractionHand.MAIN_HAND};
    private static final Predicate<Entity> PREDICATE = e -> e != null && !e.isSpectator() && e.isPickable();
    private static final FieldHandler<LivingEntity, Float> SWIM_AMOUNT0 = new FieldHandler<>(LivingEntity.class, "f_20932_");
    private static final FieldHandler<LivingEntity, Float> SWIM_AMOUNT = new FieldHandler<>(LivingEntity.class, "f_20931_");
    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

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
    public static float arcCosDeg(double value) {
        return (float) (arcCos(value) * 180 / Math.PI);
    }

    public static double arcSin(double value) {
        return Math.atan2(value, Math.sqrt(1 - value * value));
    }

    public static double arcSinDeg(double value) {
        return Math.toDegrees(arcSin(value));
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
     * @return The angle between the vector and the X axis, given in degrees.
     */
    public static double atan2Deg(double y, double x) {
        return Math.toDegrees(Mth.atan2(y, x));
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
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                    if (e != null) {
                        Evolution.warn("Had trouble traversing: " + dir + " (" + e + ")");
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
                    Evolution.warn("Skipped: " + file + " (" + exc + ")");
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
            case SOUTH -> {
                if (angle > sweep / 2) {
                    return sweep / 2;
                }
                return Math.max(angle, -sweep / 2);
            }
            case WEST -> {
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
            case NORTH -> {
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
            case EAST -> {
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

    public static void collideOBBWithCollider(@Nonnull HitInformation hits,
                                              @Nonnull Entity entity,
                                              @Nonnull Hitbox collider,
                                              float partialTicks,
                                              BlockHitResult[] hitResult,
                                              boolean checkBlocks) {
        Level world = entity.level;
        collider.getParent().init(entity, partialTicks);
        List<Entity> foundEntities = world.getEntities(entity, entity.getBoundingBox().inflate(2.5), PREDICATE);
        for (Entity entityInBoundingBox : foundEntities) {
            collidingEntityHitboxes(hits, entity, entityInBoundingBox, collider, partialTicks);
        }
        if (checkBlocks) {
            hitResult[0] = collideWithBlocks(entity, collider, partialTicks);
        }
    }

    @Nullable
    public static BlockHitResult collideWithBlocks(Entity hitter, Hitbox collider, float partialTicks) {
        AABB aabb = collider.getAABB();
        Matrix3d transform = collider.getTransformation().transpose();
        Vec3 offset = collider.getOffset();
        Matrix3d mainTransform = collider.getParent().getTransform().transpose();
        Vec3 mainOffset = collider.getParent().getOffset();
        double hitterPosX = Mth.lerp(partialTicks, hitter.xOld, hitter.getX());
        double hitterPosY = Mth.lerp(partialTicks, hitter.yOld, hitter.getY());
        double hitterPosZ = Mth.lerp(partialTicks, hitter.zOld, hitter.getZ());
        for (int v = 0; v < 4; v++) {
            double x = v < 2 ? aabb.minX : aabb.maxX;
            double y = v % 2 == 0 ? aabb.minY : aabb.maxY;
            double z = v == 0 || v == 3 ? aabb.minZ : aabb.maxZ;
            double fromX = transform.transformX(x, y, z);
            double fromY = transform.transformY(x, y, z);
            double fromZ = transform.transformZ(x, y, z);
            fromX += offset.x;
            fromY += offset.y;
            fromZ += offset.z;
            double fromX0 = mainTransform.transformX(fromX, fromY, fromZ);
            double fromY0 = mainTransform.transformY(fromX, fromY, fromZ);
            double fromZ0 = mainTransform.transformZ(fromX, fromY, fromZ);
            fromX0 += mainOffset.x;
            fromY0 += mainOffset.y;
            fromZ0 += mainOffset.z;
            for (int i = 0; i < 3; i++) {
                double x0 = x;
                double y0 = y;
                double z0 = z;
                switch (i) {
                    case 0 -> x0 = x0 == aabb.minX ? aabb.maxX : aabb.minX;//Flip X
                    case 1 -> y0 = y0 == aabb.minY ? aabb.maxY : aabb.minY;//Flip Y
                    case 2 -> z0 = z0 == aabb.minZ ? aabb.maxZ : aabb.minZ;//Flip Z
                }
                double toX = transform.transformX(x0, y0, z0);
                double toY = transform.transformY(x0, y0, z0);
                double toZ = transform.transformZ(x0, y0, z0);
                toX += offset.x;
                toY += offset.y;
                toZ += offset.z;
                double toX0 = mainTransform.transformX(toX, toY, toZ);
                double toY0 = mainTransform.transformY(toX, toY, toZ);
                double toZ0 = mainTransform.transformZ(toX, toY, toZ);
                toX0 += mainOffset.x;
                toY0 += mainOffset.y;
                toZ0 += mainOffset.z;
                fromX0 += hitterPosX;
                fromY0 += hitterPosY;
                fromZ0 += hitterPosZ;
                toX0 += hitterPosX;
                toY0 += hitterPosY;
                toZ0 += hitterPosZ;
                if (Math.abs(fromX0 - toX0) > aabb.getXsize()) {
                    continue;
                }
                if (Math.abs(fromY0 - toY0) > aabb.getYsize()) {
                    continue;
                }
                if (Math.abs(fromZ0 - toZ0) > aabb.getZsize()) {
                    continue;
                }
                //noinspection ObjectAllocationInLoop
                Vec3 from = new Vec3(fromX0, fromY0, fromZ0);
                //noinspection ObjectAllocationInLoop
                Vec3 to = new Vec3(toX0, toY0, toZ0);
                //noinspection ObjectAllocationInLoop
                BlockHitResult hitResult = hitter.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                if (hitResult.getType() != HitResult.Type.MISS) {
                    return hitResult;
                }
            }
        }
        return null;
    }

    public static <T extends Entity> void collidingEntityHitboxes(HitInformation hits, Entity hitter, T entity, Hitbox collider, float partialTicks) {
        if (hits.areAllChecked(entity)) {
            return;
        }
        AABB aabb = collider.getAABB();
        Matrix3d transform = collider.getTransformation().transpose();
        Vec3 offset = collider.getOffset();
        Matrix3d mainTransform = collider.getParent().getTransform().transpose();
        Vec3 mainOffset = collider.getParent().getOffset();
        double hitterPosX = Mth.lerp(partialTicks, hitter.xOld, hitter.getX());
        double hitterPosY = Mth.lerp(partialTicks, hitter.yOld, hitter.getY());
        double hitterPosZ = Mth.lerp(partialTicks, hitter.zOld, hitter.getZ());
        double posX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double posY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double posZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        HitboxEntity<T> hitbox = null;
        if (((IEntityPatch) entity).hasHitboxes()) {
            hitbox = (HitboxEntity<T>) ((IEntityPatch) entity).getHitboxes();
            hitbox.init(entity, partialTicks);
        }
        //Check for the four main vertices of the collider box
        for (int v = 0; v < 4; v++) {
            double x = v < 2 ? aabb.minX : aabb.maxX;
            double y = v % 2 == 0 ? aabb.minY : aabb.maxY;
            double z = v == 0 || v == 3 ? aabb.minZ : aabb.maxZ;
            double fromX = transform.transformX(x, y, z);
            double fromY = transform.transformY(x, y, z);
            double fromZ = transform.transformZ(x, y, z);
            fromX += offset.x;
            fromY += offset.y;
            fromZ += offset.z;
            double fromX0 = mainTransform.transformX(fromX, fromY, fromZ);
            double fromY0 = mainTransform.transformY(fromX, fromY, fromZ);
            double fromZ0 = mainTransform.transformZ(fromX, fromY, fromZ);
            fromX0 += mainOffset.x;
            fromY0 += mainOffset.y;
            fromZ0 += mainOffset.z;
            //For every vertex, check the three edges connected to it
            for (int i = 0; i < 3; i++) {
                double x0 = x;
                double y0 = y;
                double z0 = z;
                switch (i) {
                    case 0 -> x0 = x0 == aabb.minX ? aabb.maxX : aabb.minX;//Flip X
                    case 1 -> y0 = y0 == aabb.minY ? aabb.maxY : aabb.minY;//Flip Y
                    case 2 -> z0 = z0 == aabb.minZ ? aabb.maxZ : aabb.minZ;//Flip Z
                }
                double toX = transform.transformX(x0, y0, z0);
                double toY = transform.transformY(x0, y0, z0);
                double toZ = transform.transformZ(x0, y0, z0);
                toX += offset.x;
                toY += offset.y;
                toZ += offset.z;
                double toX0 = mainTransform.transformX(toX, toY, toZ);
                double toY0 = mainTransform.transformY(toX, toY, toZ);
                double toZ0 = mainTransform.transformZ(toX, toY, toZ);
                toX0 += mainOffset.x;
                toY0 += mainOffset.y;
                toZ0 += mainOffset.z;
                fromX0 += hitterPosX;
                fromY0 += hitterPosY;
                fromZ0 += hitterPosZ;
                toX0 += hitterPosX;
                toY0 += hitterPosY;
                toZ0 += hitterPosZ;
                if (!((IEntityPatch) entity).hasHitboxes()) {
                    //Sanity checks
                    if (Math.abs(fromX0 - toX0) > aabb.getXsize()) {
                        //Invalid hit, try another
                        continue;
                    }
                    if (Math.abs(fromY0 - toY0) > aabb.getYsize()) {
                        //Invalid hit, try another
                        continue;
                    }
                    if (Math.abs(fromZ0 - toZ0) > aabb.getZsize()) {
                        //Invalid hit, try another
                        continue;
                    }
                    Vec3 from = new Vec3(fromX0, fromY0, fromZ0);
                    Vec3 to = new Vec3(toX0, toY0, toZ0);
                    Optional<Vec3> optional = entity.getBoundingBox().clip(from, to);
                    if (optional.isPresent() || entity.getBoundingBox().contains(from) || entity.getBoundingBox().contains(to)) {
                        //Edge hit the entity BB
                        hits.addHitbox(entity, HitboxType.ALL);
                        return;
                    }
                    //Edge did not hit, try another
                    continue;
                }
                fromX0 -= posX;
                fromY0 -= posY;
                fromZ0 -= posZ;
                toX0 -= posX;
                toY0 -= posY;
                toZ0 -= posZ;
                Vec3 mainOffsetIns = hitbox.getOffset();
                Matrix3d mainTransformIns = hitbox.getTransform();
                fromX0 -= mainOffsetIns.x;
                fromY0 -= mainOffsetIns.y;
                fromZ0 -= mainOffsetIns.z;
                double fromX1 = mainTransformIns.transformX(fromX0, fromY0, fromZ0);
                double fromY1 = mainTransformIns.transformY(fromX0, fromY0, fromZ0);
                double fromZ1 = mainTransformIns.transformZ(fromX0, fromY0, fromZ0);
                toX0 -= mainOffsetIns.x;
                toY0 -= mainOffsetIns.y;
                toZ0 -= mainOffsetIns.z;
                double toX1 = mainTransformIns.transformX(toX0, toY0, toZ0);
                double toY1 = mainTransformIns.transformY(toX0, toY0, toZ0);
                double toZ1 = mainTransformIns.transformZ(toX0, toY0, toZ0);
                mainTransformIns.transpose();
                //noinspection ObjectAllocationInLoop
                Vec3 from = new Vec3(fromX1, fromY1, fromZ1);
                //noinspection ObjectAllocationInLoop
                Vec3 to = new Vec3(toX1, toY1, toZ1);
                boolean hasAdded = false;
                //Check if the edge hits every Hitbox
                for (Hitbox box : hitbox.getBoxes()) {
                    if (hits.contains(entity, box.getPart())) {
                        continue;
                    }
                    Vec3 newFrom = from;
                    Vec3 newTo = to;
                    Vec3 offsetIns = box.getOffset();
                    Matrix3d transformIns = box.getTransformation();
                    newFrom = newFrom.subtract(offsetIns);
                    newFrom = transformIns.transform(newFrom);
                    newTo = newTo.subtract(offsetIns);
                    newTo = transformIns.transform(newTo);
                    Optional<Vec3> optional = box.getAABB().clip(newFrom, newTo);
                    if (optional.isPresent()) {
                        hasAdded = true;
                        hits.addHitbox(entity, box.getPart());
                    }
                }
                if (hasAdded && hits.areAllChecked(entity)) {
                    return;
                }
            }
        }
    }

    /**
     * Compares two strings, ignoring case and accentuation.
     */
    public static int compare(String a, String b) {
        Collator collator = Collator.getInstance(Locale.ROOT);
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(a, b);
    }

    public static int computeNormal(Matrix3f normalMatrix, Direction facing) {
        return ((IMatrix3fPatch) (Object) normalMatrix).computeNormal(facing);
    }

    /**
     * Whether string a contains string b, ignoring case and accentuation.
     */
    public static boolean contains(String a, String b, @Nullable StringBuilder builder) {
        StringBuilder sb = builder == null ? new StringBuilder() : builder;
        return stripAccents(a, sb).toLowerCase(Locale.ROOT).contains(stripAccents(b, sb).toLowerCase(Locale.ROOT));
    }

    private static void convertRemainingAccentCharacters(StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); ++i) {
            if (decomposed.charAt(i) == 321) {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'L');
            }
            else if (decomposed.charAt(i) == 322) {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'l');
            }
        }
    }

    /**
     * Approximates the trigonometric function cosine.
     *
     * @param rad The argument of the cosine, given in radians.
     * @return An approximation of the cosine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float cos(float rad) {
        return Mth.cos(wrapRadians(rad));
    }

    /**
     * Approximates the trigonometric function cosine.
     *
     * @param deg The argument of the cosine, given in degrees.
     * @return An approximation of the cosine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float cosDeg(float deg) {
        return Mth.cos(degToRad(Mth.wrapDegrees(deg)));
    }

    /**
     * Converts a {@code float} value from degrees to radians.
     *
     * @param degrees The value in degrees.
     * @return the corresponding value in radians.
     */
    public static float degToRad(float degrees) {
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

    public static VoxelShape generateShapeFromPattern(long pattern) {
        VoxelShape shape = Shapes.empty();
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
        return switch (dir) {
            case SOUTH -> 90.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

//    public static ArmPose getArmPose(LivingEntity entity, ItemStack mainhandStack, ItemStack offhandStack, InteractionHand hand) {
//        ArmPose armPose = ArmPose.EMPTY;
//        ItemStack stack = hand == InteractionHand.MAIN_HAND ? mainhandStack : offhandStack;
//        if (!stack.isEmpty()) {
//            armPose = ArmPose.ITEM;
//            if (entity.getTicksUsingItem() > 0 && hand == entity.getUsedItemHand()) {
//                UseAnim useaction = stack.getUseAnimation();
//                switch (useaction) {
//                    case BLOCK -> {
//                        return ArmPose.BLOCK;
//                    }
//                    case BOW -> {
//                        return ArmPose.BOW_AND_ARROW;
//                    }
//                    case SPEAR -> {
//                        return ArmPose.THROW_SPEAR;
//                    }
//                    case CROSSBOW -> {
//                        return ArmPose.CROSSBOW_CHARGE;
//                    }
//                }
//            }
//            else {
//                boolean mainhandHasCrossbow = mainhandStack.getItem() == Items.CROSSBOW;
//                boolean mainhandIsCharged = CrossbowItem.isCharged(mainhandStack);
//                boolean offhandHasCrossbow = offhandStack.getItem() == Items.CROSSBOW;
//                boolean offhandIsCharged = CrossbowItem.isCharged(offhandStack);
//                if (mainhandHasCrossbow && mainhandIsCharged) {
//                    armPose = ArmPose.CROSSBOW_HOLD;
//                }
//                if (offhandHasCrossbow && offhandIsCharged && mainhandStack.getItem().getUseAnimation(mainhandStack) == UseAnim.NONE) {
//                    return ArmPose.CROSSBOW_HOLD;
//                }
//            }
//        }
//        return armPose;
//    }

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

    public static Vec3 getCameraPosition(Entity entity, float partialTicks) {
        if (entity.getPose() == Pose.SLEEPING) {
            return new Vec3(entity.getX(), entity.getY() + 0.17, entity.getZ());
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
        Vec3 neckPoint = ((INeckPosition) entity).getNeckPoint();
        float actualYOffset = yOffset * cosPitch - zOffset * sinPitch;
        float horizontalOffset = yOffset * sinPitch + zOffset * cosPitch;
        double x = Mth.lerp(partialTicks, entity.xo, entity.getX()) - horizontalOffset * sinYaw + neckPoint.x * cosBodyYaw - neckPoint.z * sinBodyYaw;
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY()) + neckPoint.y + actualYOffset;
        double z = Mth.lerp(partialTicks, entity.zo, entity.getZ()) + horizontalOffset * cosYaw + neckPoint.x * sinBodyYaw + neckPoint.z * cosBodyYaw;
        return new Vec3(x, y, z);
    }

    public static float getEntityBodyYaw(Entity entity, float partialTicks) {
        if (entity instanceof LivingEntity living) {
            return partialTicks == 1.0F ? living.yBodyRot : Mth.lerp(partialTicks, living.yBodyRotO, living.yBodyRot);
        }
        return partialTicks == 1.0F ? entity.getYRot() : Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
    }

    public static IPoseStackPatch getExtendedMatrix(PoseStack matrices) {
        return (IPoseStackPatch) matrices;
    }

    public static IMatrix4fPatch getExtendedMatrix(Matrix4f matrix) {
        return (IMatrix4fPatch) (Object) matrix;
    }

    public static IMatrix3fPatch getExtendedMatrix(Matrix3f matrix) {
        return (IMatrix3fPatch) (Object) matrix;
    }

    public static IQuaternionPatch getExtendedQuaternion(Quaternion quaternion) {
        return (IQuaternionPatch) (Object) quaternion;
    }

    public static HumanoidArm getHandSide(LivingEntity entity) {
        HumanoidArm handSide = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? handSide : handSide.getOpposite();
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
        float limbSwingAmount = Mth.lerp(partialTicks, entity.animationSpeedOld, entity.animationSpeed);
        return Math.min(limbSwingAmount, 1.0F);
    }

    /**
     * Gets the negative {@link Direction} of a given {@link Direction.Axis}.
     *
     * @param axis The desired {@link Direction.Axis}.
     * @return The negative {@link Direction} on that {@link Direction.Axis}.
     */
    public static Direction getNegativeAxis(@Nonnull Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.WEST;
            case Y -> Direction.DOWN;
            case Z -> Direction.NORTH;
        };
    }

    public static float getNetHeadYaw(LivingEntity entity, float partialTicks) {
        float interpYaw = lerpAngles(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = lerpAngles(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        return headYaw - interpYaw;
    }

    /**
     * Gets the positive {@link Direction} of a given {@link Direction.Axis}.
     *
     * @param axis The desired {@link Direction.Axis}.
     * @return The positive {@link Direction} on that {@link Direction.Axis}.
     */
    public static Direction getPositiveAxis(@Nonnull Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
        };
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
        return Mth.lerp(partialTicks, SWIM_AMOUNT0.get(entity), SWIM_AMOUNT.get(entity));
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
    public static double hitOffset(Direction.Axis axis, double hit, @Nonnull Direction direction) {
        if (direction.getAxis() != axis) {
            return hit;
        }
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
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
        return !Shapes.joinIsNotEmpty(reference, inside, BooleanOp.ONLY_SECOND);
    }

    /**
     * Calculates if the {@link VoxelShape} is totally outside another one.
     *
     * @param outside   The {@link VoxelShape} that should be totally outside the reference.
     * @param reference The reference {@link VoxelShape}.
     * @return {@code true} if the first {@link VoxelShape} is totally outside the reference one, {@code false} otherwise.
     */
    public static boolean isShapeTotallyOutside(VoxelShape outside, VoxelShape reference) {
        return !Shapes.joinIsNotEmpty(reference, outside, BooleanOp.AND);
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

    public static float lerpAngles(float partialTicks, float prevAngle, float angle) {
        return prevAngle + partialTicks * Mth.wrapDegrees(angle - prevAngle);
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
     * Converts a {@code double} value from radians to degrees.
     *
     * @param radians The value in radians.
     * @return the corresponding value in degrees.
     */
    public static double radToDeg(double radians) {
        return radians * 180 / Math.PI;
    }

    /**
     * Converts a {@code float} value from radians to degrees.
     *
     * @param radians The value in radians.
     * @return the corresponding value in degrees.
     */
    public static float radToDeg(float radians) {
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
    public static BlockHitResult rayTraceBlocksFromCamera(@Nonnull Entity entity,
                                                          Vec3 cameraPos,
                                                          float partialTicks,
                                                          double distance,
                                                          boolean fluid) {
        Vec3 look = entity.getViewVector(partialTicks);
        Vec3 to = cameraPos.add(look.x * distance, look.y * distance, look.z * distance);
        return entity.level.clip(
                new ClipContext(cameraPos, to, ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
    }

    /**
     * Casts a ray tracing for {@code Block}s starting from the eyes of the desired {@link Entity}.
     *
     * @param entity       The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks The partial ticks to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param distance     The maximum distance the ray will travel.
     * @param fluid        {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockHitResult} containing the {@link net.minecraft.core.BlockPos} of the {@code Block} hit.
     */
    @Nonnull
    public static BlockHitResult rayTraceBlocksFromEyes(@Nonnull Entity entity, float partialTicks, double distance, boolean fluid) {
        Vec3 from = entity.getEyePosition(partialTicks);
        Vec3 look = entity.getViewVector(partialTicks);
        Vec3 to = from.add(look.x * distance, look.y * distance, look.z * distance);
        return entity.level.clip(
                new ClipContext(from, to, ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
    }

    /**
     * Casts a ray tracing for {@code Block}s based on the {@link Entity}'s {@link Entity#yRot} and {@link Entity#xRot}.
     * This method is useful for Entities that are projectiles, as {@link MathHelper#rayTraceBlocksFromEyes(Entity, float, double, boolean)}
     * usually do not work on them.
     *
     * @param entity   The {@link Entity} from whose {@code yaw} and {@code pitch} to cast the ray.
     * @param distance The maximum distance this ray will travel.
     * @param fluid    {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockHitResult} containing the {@link BlockPos} of the {@code Block} hit.
     */
    @Nonnull
    public static BlockHitResult rayTraceBlocksFromYawAndPitch(@Nonnull Entity entity, double distance, boolean fluid) {
        Vec3 from = entity.getEyePosition(1.0f);
        float theta = entity.getYRot();
        if (theta < 0) {
            theta += 360;
        }
        theta = degToRad(theta);
        float phi = entity.getXRot();
        if (phi < 0) {
            phi += 360;
        }
        phi = degToRad(phi);
        Vec3 looking = new Vec3(sin(theta), sin(phi), cos(theta)).normalize();
        Vec3 to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return entity.level.clip(
                new ClipContext(from, to, ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
    }

    /**
     * Casts a ray tracing for Entities inside an {@link AABB} from the starting vector to the end vector.
     *
     * @param toExclude       The {@link Entity} to exclude from this raytrace.
     * @param startVec        The {@link Vector3d} representing the start of the raytrace.
     * @param endVec          The {@link Vector3d} representing the end of the raytrace.
     * @param boundingBox     The {@link AABB} to look for entities inside.
     * @param distanceSquared The max distance this raytrace will travel, squared.
     * @return A {@link EntityHitResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityHitResult} will be {@code null}.
     */
    @Nullable
    public static EntityHitResult rayTraceEntities(@Nonnull Entity toExclude, Vec3 startVec, Vec3 endVec, AABB boundingBox, double distanceSquared) {
        Level level = toExclude.level;
        double range = distanceSquared;
        Entity entity = null;
        Vec3 vec3d = null;
        for (Entity entityInBoundingBox : level.getEntities(toExclude, boundingBox, PREDICATE)) {
            AABB aabb = entityInBoundingBox.getBoundingBox();
            Optional<Vec3> optional = aabb.clip(startVec, endVec);
            if (aabb.contains(startVec)) {
                if (range >= 0) {
                    entity = entityInBoundingBox;
                    vec3d = optional.orElse(startVec);
                    range = 0;
                }
            }
            else if (optional.isPresent()) {
                Vec3 hitResult = optional.get();
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
        return new EntityHitResult(entity, vec3d);
    }

    /**
     * Casts a ray tracing for Entities starting from the eyes of the desired {@link Entity}.
     *
     * @param entity        The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks  The partial tick to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param reachDistance The max distance the ray will travel.
     * @return An {@link EntityHitResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityHitResult} will be {@code null}.
     */
    @Nullable
    public static EntityHitResult rayTraceEntityFromEyes(@Nonnull Entity entity, float partialTicks, double reachDistance) {
        Vec3 from = entity.getEyePosition(partialTicks);
        Vec3 look = entity.getViewVector(partialTicks).normalize();
        Vec3 to = from.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        return rayTraceEntities(entity, from, to, new AABB(from, to), reachDistance * reachDistance);
    }

    /**
     * Casts a ray tracing for {@code Entities} based on the {@link Entity}'s {@link Entity#yRot} and {@link Entity#xRot}.
     * This method is useful for Entities that are projectiles, as {@link MathHelper#rayTraceEntityFromEyes(Entity, float, double)}
     * usually do not work on them.
     *
     * @param entity   The {@link Entity} from whose {@code yaw} and {@code pitch} to cast the ray.
     * @param distance The maximum distance this ray will travel.
     * @return An {@link EntityHitResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityHitResult} will be {@code null}.
     */
    @Nullable
    public static EntityHitResult rayTraceEntityFromPitchAndYaw(@Nonnull Entity entity, double distance) {
        Vec3 from = entity.getEyePosition(1.0f);
        float theta = entity.getYRot();
        if (theta < 0) {
            theta += 360;
        }
        theta = degToRad(theta);
        float phi = entity.getXRot();
        if (phi < 0) {
            phi += 360;
        }
        phi = degToRad(phi);
        Vec3 looking = new Vec3(sin(theta), sin(phi), cos(theta)).normalize();
        Vec3 to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return rayTraceEntities(entity, from, to, new AABB(from, to), distance * distance);
    }

    /**
     * Casts a ray tracing for Entities starting from the eyes of the desired {@link Entity} and extends to its reach distance.
     * The ray will only collide with OBBs.
     *
     * @param entity        The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks  The partial tick to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param reachDistance The max distance the ray will travel.
     * @return An {@link EntityHitResult} containing the {@link Entity} hit by the ray traced and a {@link Vector3d}
     * containing the position of the hit. If no {@link Entity} was hit by the ray, this {@link EntityHitResult} will be {@code null}.
     */
    @Nullable
    public static EntityHitResult rayTraceOBBEntityFromEyes(@Nonnull Entity entity,
                                                            @Nullable Vec3 cameraPos,
                                                            float partialTicks,
                                                            double reachDistance) {
        Vec3 from = cameraPos != null ? cameraPos : entity.getEyePosition(partialTicks);
        Vec3 look = entity.getViewVector(partialTicks).normalize();
        Vec3 to = from.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        Level level = entity.level;
        double rangeSq = reachDistance * reachDistance;
        Entity foundEntity = null;
        Vec3 hitVector = null;
        double[] distance = {1.0};
        Hitbox[] hitbox = new Hitbox[1];
        Hitbox box = null;
        List<Entity> foundEntities = level.getEntities(entity, new AABB(from, to).inflate(1.5), PREDICATE);
        for (Entity entityInBoundingBox : foundEntities) {
            Vec3 hitResult = rayTracingEntityHitboxes(entityInBoundingBox, from, look, reachDistance, partialTicks, distance, hitbox);
            if (hitResult != null) {
                if (distance[0] < rangeSq || rangeSq == 0) {
                    foundEntity = entityInBoundingBox;
                    hitVector = hitResult;
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
        return new AdvancedEntityRayTraceResult(foundEntity, hitVector, box);
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
     * @return The hit result, if any, or null.
     */
    @Nullable
    public static <T extends Entity> Vec3 rayTracingEntityHitboxes(T entity,
                                                                   Vec3 from,
                                                                   Vec3 look,
                                                                   double reach,
                                                                   float partialTicks,
                                                                   double[] distance,
                                                                   Hitbox[] chosenBox) {
        chosenBox[0] = null;
        distance[0] = reach * reach;
        if (!((IEntityPatch) entity).hasHitboxes()) {
            Vec3 to = from.add(look.x * reach, look.y * reach, look.z * reach);
            Optional<Vec3> optional = entity.getBoundingBox().clip(from, to);
            if (optional.isPresent()) {
                double actualDistanceSquared = from.distanceToSqr(optional.get());
                distance[0] = actualDistanceSquared;
                return optional.get();
            }
            if (entity.getBoundingBox().contains(from)) {
                distance[0] = 0;
                return from;
            }
            return null;
        }
        HitboxEntity<T> hitbox = (HitboxEntity<T>) ((IEntityPatch) entity).getHitboxes();
        hitbox.init(entity, partialTicks);
        double posX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double posY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double posZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        from = from.subtract(posX, posY, posZ);
        Vec3 mainOffset = hitbox.getOffset();
        Matrix3d mainTransform = hitbox.getTransform();
        from = from.subtract(mainOffset);
        from = mainTransform.transform(from);
        look = mainTransform.transform(look);
        mainTransform.transpose();
        Vec3 result = null;
        for (Hitbox box : hitbox.getBoxes()) {
            Vec3 newFrom = from;
            Vec3 newLook = look;
            Vec3 offset = box.getOffset();
            Matrix3d transform = box.getTransformation();
            newFrom = newFrom.subtract(offset);
            newFrom = transform.transform(newFrom);
            newLook = transform.transform(newLook);
            Vec3 to = newFrom.add(newLook.x * reach, newLook.y * reach, newLook.z * reach);
            Optional<Vec3> optional = box.getAABB().clip(newFrom, to);
            if (optional.isPresent() || box.getAABB().contains(newFrom)) {
                Vec3 hitResult = optional.orElse(newFrom);
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
                        return result;
                    }
                }
            }
        }
        return result;
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
        VoxelShape[] buffer = {shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            //noinspection ObjectAllocationInLoop
            buffer[0].forAllBoxes(
                    (minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
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
    public static void setRotationAngle(@Nonnull ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    public static void setRotationPivot(ModelPart modelRenderer, float x, float y, float z) {
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
    public static float sin(float rad) {
        return Mth.sin(wrapRadians(rad));
    }

    /**
     * Approximates the trigonometric function sine.
     *
     * @param deg The argument of the sine, given in degrees.
     * @return An approximation of the sine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float sinDeg(float deg) {
        return Mth.sin(degToRad(Mth.wrapDegrees(deg)));
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

    @Nullable
    public static String stripAccents(String input, @Nullable StringBuilder builder) {
        if (input == null) {
            return null;
        }
        StringBuilder decomposed;
        if (builder == null) {
            decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        }
        else {
            decomposed = builder;
            builder.setLength(0);
            builder.append(Normalizer.normalize(input, Normalizer.Form.NFD));
        }
        convertRemainingAccentCharacters(decomposed);
        return DIACRITICAL_MARKS.matcher(decomposed).replaceAll("");
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
        return Shapes.join(A, B, BooleanOp.ONLY_FIRST);
    }

    /**
     * Approximates the trigonometric function tangent.
     *
     * @param rad The argument of the tangent, given in radians.
     * @return An approximation of the tangent of the given argument.
     * The returned value will be between {@link Float#NEGATIVE_INFINITY} and {@link Float#POSITIVE_INFINITY}.
     */
    public static float tan(float rad) {
        return Mth.sin(rad) / Mth.cos(rad);
    }

    public static float tanDeg(float deg) {
        return tan(degToRad(deg));
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
        return Shapes.join(A, B, BooleanOp.OR);
    }

    /**
     * Wraps the angle given in radians in the range [-pi; pi)
     *
     * @param value The angle value in radians.
     * @return The equivalent angle value wrapped.
     */
    public static double wrapRadians(double value) {
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
    public static float wrapRadians(float value) {
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
