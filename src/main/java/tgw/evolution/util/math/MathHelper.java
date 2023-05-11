package tgw.evolution.util.math;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.Patterns;
import tgw.evolution.capabilities.modular.IModularTool;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.modular.ItemModularTool;
import tgw.evolution.patches.*;
import tgw.evolution.util.*;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.hitbox.Matrix4d;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Collator;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class MathHelper {

    public static final Random RANDOM = new Random();
    public static final DirectionDiagonal[][] DIAGONALS = {{DirectionDiagonal.NORTH_WEST, DirectionDiagonal.NORTH_EAST},
                                                           {DirectionDiagonal.SOUTH_WEST, DirectionDiagonal.SOUTH_EAST}};
    public static final InteractionHand[] HANDS_MAIN_PRIORITY = {InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
    public static final InteractionHand[] HANDS_OFF_PRIORITY = {InteractionHand.OFF_HAND, InteractionHand.MAIN_HAND};
    private static final Predicate<Entity> PICKABLE_ENTITIES = e -> e != null && !e.isSpectator() && e.isPickable();
    private static final Predicate<Entity> ALIVE_ENTITIES = e -> e != null && !e.isSpectator() && e.isPickable() && e.isAlive();
    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private MathHelper() {
    }

    public static float animInterval(float progress, float start, float end) {
        assert start >= 0;
        assert end <= 1;
        assert end > start;
        assert 0 <= progress && progress <= 1;
        progress -= start;
        progress /= end - start;
        return progress;
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
        double valueSqr = value * value;
        return Math.PI / 2.0 + (a * value + b * valueSqr * value) / (1 + c * valueSqr + d * valueSqr * valueSqr);
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
        return (float) (arcCos(value) * (180 / Math.PI));
    }

    public static double arcSin(double value) {
        return Mth.atan2(value, Math.sqrt(1 - value * value));
    }

    public static double arcSinDeg(double value) {
        return Math.toDegrees(arcSin(value));
    }

    public static boolean areStacksSimilar(ItemStack a, ItemStack b) {
        if (a == b) {
            return true;
        }
        boolean aEmpty = a.isEmpty();
        boolean bEmpty = b.isEmpty();
        if (aEmpty && bEmpty) {
            return true;
        }
        if (aEmpty || bEmpty) {
            return false;
        }
        Item itemA = a.getItem();
        Item itemB = b.getItem();
        if (itemA != itemB) {
            return false;
        }
        if (itemA instanceof ItemModularTool) {
            return IModularTool.get(a).isSimilar(IModularTool.get(b));
        }
        return a.sameItem(b);
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
                public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException e) {
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

    /**
     * @return The least distance between {@code from} and the {@link AABB}, given the ray clips, otherwise {@link Double#NaN}.
     */
    public static double clipDist(AABB bb, Vec3 from, Vec3 to) {
        return clipDist(bb, from.x, from.y, from.z, to.x, to.y, to.z);
    }

    /**
     * @return The least distance between {@code (x0, y0, z0)} and the {@link AABB}, given the ray clips, otherwise {@link Double#NaN}.
     */
    public static double clipDist(AABB bb, double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        return getMinDist(bb, x0, y0, z0, dx, dy, dz);
    }

    public static double clipPoint(double minDist,
                                   double distSide,
                                   double distA,
                                   double distB,
                                   double minSide,
                                   double minA,
                                   double maxA,
                                   double minB,
                                   double maxB,
                                   double startSide,
                                   double startA,
                                   double startB) {
        double d0 = (minSide - startSide) / distSide;
        double d1 = startA + d0 * distA;
        double d2 = startB + d0 * distB;
        double comp = Double.isNaN(minDist) ? 1.0 : minDist;
        if (0 < d0 && d0 < comp && minA - 1.0E-7 < d1 && d1 < maxA + 1.0E-7 && minB - 1.0E-7 < d2 && d2 < maxB + 1.0E-7) {
            return d0;
        }
        return minDist;
    }

    public static void collideOBBWithCollider(HitInformation hits,
                                              LivingEntity entity,
                                              float partialTicks,
                                              BlockHitResult[] hitResult,
                                              boolean checkBlocks, boolean onlyBlocks) {
        HitboxEntity<LivingEntity> hitboxes = ((IEntityPatch) entity).getHitboxes();
        if (hitboxes == null) {
            return;
        }
        Hitbox collider = hitboxes.getEquipFor(entity, ((ILivingEntityPatch) entity).getSpecialAttackType(), entity.getMainArm());
        if (collider == null) {
            return;
        }
        boolean initialized = false;
        double radiusX = 2.5;
        double radiusY = 2.5;
        double radiusZ = 2.5;
        if (checkBlocks || onlyBlocks) {
            hitboxes.init(entity, partialTicks);
            Matrix4d colliderTransform = collider.adjustedColliderTransform();
            Matrix4d transform = hitboxes.getColliderTransform();
            double hitterPosX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double hitterPosY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double hitterPosZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            hits.prepare(collider, colliderTransform, transform, hitterPosX, hitterPosY, hitterPosZ);
            initialized = true;
            BlockHitResult result = collideWithBlocks(hits, entity);
            hitResult[0] = result;
            if (onlyBlocks) {
                return;
            }
            if (result != null && result.getType() != HitResult.Type.MISS) {
                int mul = result.getDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE ? -1 : 1;
                switch (result.getDirection().getAxis()) {
                    case X -> radiusX = Math.abs(result.getLocation().x - entity.getX() + mul * entity.getBbWidth() / 2);
                    case Z -> radiusZ = Math.abs(result.getLocation().z - entity.getZ() + mul * entity.getBbWidth() / 2);
                }
            }
        }
        List<Entity> foundEntities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(radiusX, radiusY, radiusZ), ALIVE_ENTITIES);
        if (foundEntities.isEmpty()) {
            return;
        }
        if (!initialized) {
            hitboxes.init(entity, partialTicks);
            Matrix4d colliderTransform = collider.adjustedColliderTransform();
            Matrix4d transform = hitboxes.getColliderTransform();
            double hitterPosX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
            double hitterPosY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
            double hitterPosZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
            hits.prepare(collider, colliderTransform, transform, hitterPosX, hitterPosY, hitterPosZ);
        }
        for (int i = 0, l = foundEntities.size(); i < l; i++) {
            Entity possibleVictim = foundEntities.get(i);
            HitboxEntity<Entity> victimHitboxes = ((IEntityPatch) possibleVictim).getHitboxes();
            if (victimHitboxes == null) {
                collidingWithEntityBB(hits, possibleVictim);
            }
            else {
                collidingWithEntityHitboxesInTheirVS(hits, possibleVictim, victimHitboxes, partialTicks);
            }
        }
        hits.release();
    }

    public static void collideOBBWithProjectile(ProjectileHitInformation hits, float partialTicks, Entity victim) {
        HitboxEntity<Entity> victimHitboxes = ((IEntityPatch) victim).getHitboxes();
        if (victimHitboxes == null) {
            collidingWithEntityBB(hits, victim);
        }
        else {
            collidingWithEntityHitboxesInTheirVS(hits, victim, victimHitboxes, partialTicks);
        }
    }

    @Nullable
    public static BlockHitResult collideWithBlocks(HitInformation hits, Entity hitter) {
        Level level = hitter.level;
        for (int e = 0; e < 12; e++) {
            BlockHitResult clip = level.clip(hits.getClipContext(e));
            if (clip.getType() != HitResult.Type.MISS) {
                return clip;
            }
        }
        return null;
    }

    private static void collidingWithEntityBB(IHitInfo hits, Entity victim) {
        AABB bb = victim.getBoundingBox();
        for (int v = 0; v < 8; v++) {
            if (bb.contains(hits.getOrMakeVertex(v))) {
                hits.addHitbox(victim, HitboxType.ALL);
                return;
            }
        }
        for (int e = 0; e < 12; e++) {
            if (!Double.isNaN(clipDist(bb, hits.getOrMakeEdge(e, true), hits.getOrMakeEdge(e, false)))) {
                hits.addHitbox(victim, HitboxType.ALL);
                return;
            }
        }
    }

    private static void collidingWithEntityHitboxesInTheirVS(IHitInfo hits,
                                                             Entity victim,
                                                             HitboxEntity<Entity> victimHitboxes,
                                                             float partialTicks) {
        double victimX = Mth.lerp(partialTicks, victim.xo, victim.getX());
        double victimY = Mth.lerp(partialTicks, victim.yo, victim.getY());
        double victimZ = Mth.lerp(partialTicks, victim.zo, victim.getZ());
        victimHitboxes.init(victim, partialTicks);
        List<Hitbox> boxes = victimHitboxes.getBoxes();
        hits.prepareInHBVS(victimHitboxes, victimX, victimY, victimZ);
        boxes:
        for (int i = 0, l = boxes.size(); i < l; i++) {
            Hitbox hitbox = boxes.get(i);
            HitboxType part = hitbox.getPart();
            if (part == HitboxType.NONE) {
                continue;
            }
            if (hits.contains(victim, part)) {
                continue;
            }
            hits.softRelease();
            Matrix4d transform = hitbox.adjustedTransform();
            for (int v = 0; v < 8; v++) {
                if (hitbox.contains(hits.getOrMakeVertexInHBVS(v, transform))) {
                    hits.addHitbox(victim, part);
                    continue boxes;
                }
            }
            for (int e = 0; e < 12; e++) {
                if (!Double.isNaN(hitbox.clipDist(hits.getOrMakeEdgeInHBVS(e, true, transform), hits.getOrMakeEdgeInHBVS(e, false, transform)))) {
                    hits.addHitbox(victim, part);
                    continue boxes;
                }
            }
        }
        hits.releaseInHBVS();
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
     * @param deg The argument of the cosine, given in degrees.
     * @return An approximation of the cosine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float cosDeg(float deg) {
        return Mth.cos(deg * Mth.DEG_TO_RAD);
    }

    /**
     * Calculates the shortest distance between two points in euclidean space.
     */
    public static float distance(double x0, double y0, double z0, double x1, double y1, double z1) {
        return sqrt(distanceSqr(x0, y0, z0, x1, y1, z1));
    }

    public static double distanceSqr(double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x0 - x1;
        double dy = y0 - y1;
        double dz = z0 - z1;
        return dx * dx + dy * dy + dz * dz;
    }

    public static HumanoidArm fromHand(LivingEntity victim, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? victim.getMainArm() : victim.getMainArm().getOpposite();
    }

    public static VoxelShape generateShapeFromPattern(long pattern) {
        VoxelShape shape = Shapes.empty();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((pattern & 1L << 8 * j + i) != 0) {
                    shape = union(shape, EvolutionShapes.KNAPPING_PART.move(i / 8.0f, 0, j / 8.0f));
                }
            }
        }
        return shape;
    }

    public static float getAgeInTicks(LivingEntity victim, float partialTicks) {
        return victim.tickCount + partialTicks;
    }

    public static float getAttackAnim(LivingEntity victim, float partialTick) {
        float f = victim.attackAnim - victim.oAttackAnim;
        if (f < 0.0F) {
            ++f;
        }
        return victim.oAttackAnim + f * partialTick;
    }

    public static <T extends Entity> Vec3d getCameraPosition(T victim, float partialTicks) {
        double x = Mth.lerp(partialTicks, victim.xo, victim.getX());
        double y = Mth.lerp(partialTicks, victim.yo, victim.getY());
        double z = Mth.lerp(partialTicks, victim.zo, victim.getZ());
        if (!(victim instanceof LivingEntity living && living.isDeadOrDying())) {
            HitboxEntity<T> hitboxes = ((IEntityPatch<T>) victim).getHitboxes();
            if (hitboxes != null) {
                return hitboxes.getOffsetForCamera(victim, partialTicks).addMutable(x, y, z);
            }
        }
        return new Vec3d(x, y + victim.getEyeHeight(), z);
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

    public static float getLimbSwing(LivingEntity victim, float partialTicks) {
        float limbSwing = victim.animationPosition - victim.animationSpeed * (1.0F - partialTicks);
        if (victim.isBaby()) {
            limbSwing *= 3.0F;
        }
        return limbSwing;
    }

    public static float getLimbSwingAmount(LivingEntity victim, float partialTicks) {
        float limbSwingAmount = Mth.lerp(partialTicks, victim.animationSpeedOld, victim.animationSpeed);
        return Math.min(limbSwingAmount, 1.0F);
    }

    private static double getMinDist(AABB bb, double x0, double y0, double z0, double dx, double dy, double dz) {
        double minDist = Double.NaN;
        if (dx > 1.0E-7) {
            minDist = clipPoint(minDist, dx, dy, dz, bb.minX, bb.minY, bb.maxY, bb.minZ, bb.maxZ, x0, y0, z0);
        }
        else if (dx < -1.0E-7) {
            minDist = clipPoint(minDist, dx, dy, dz, bb.maxX, bb.minY, bb.maxY, bb.minZ, bb.maxZ, x0, y0, z0);
        }
        if (dy > 1.0E-7) {
            minDist = clipPoint(minDist, dy, dz, dx, bb.minY, bb.minZ, bb.maxZ, bb.minX, bb.maxX, y0, z0, x0);
        }
        else if (dy < -1.0E-7) {
            minDist = clipPoint(minDist, dy, dz, dx, bb.maxY, bb.minZ, bb.maxZ, bb.minX, bb.maxX, y0, z0, x0);
        }
        if (dz > 1.0E-7) {
            return clipPoint(minDist, dz, dx, dy, bb.minZ, bb.minX, bb.maxX, bb.minY, bb.maxY, z0, x0, y0);
        }
        if (dz < -1.0E-7) {
            return clipPoint(minDist, dz, dx, dy, bb.maxZ, bb.minX, bb.maxX, bb.minY, bb.maxY, z0, x0, y0);
        }
        return minDist;
    }

    /**
     * Gets the negative {@link Direction} of a given {@link Direction.Axis}.
     *
     * @param axis The desired {@link Direction.Axis}.
     * @return The negative {@link Direction} on that {@link Direction.Axis}.
     */
    public static Direction getNegativeAxis(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.WEST;
            case Y -> Direction.DOWN;
            case Z -> Direction.NORTH;
        };
    }

    /**
     * Gets the positive {@link Direction} of a given {@link Direction.Axis}.
     *
     * @param axis The desired {@link Direction.Axis}.
     * @return The positive {@link Direction} on that {@link Direction.Axis}.
     */
    public static Direction getPositiveAxis(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
        };
    }

    @Nullable
    public static MultipleEntityHitResult getProjectileHitResult(EntityGetter level,
                                                                 Entity projectile,
                                                                 Vec3 start,
                                                                 Vec3 end,
                                                                 Predicate<Entity> filter,
                                                                 double inflation) {
        List<Entity> foundEntities = level.getEntities(projectile, new AABBMutable(start, end).inflateMutable(inflation), filter);
        AABBMutable tempBB = null;
        MultipleEntityHitResult hitResult = null;
        for (int i = 0, l = foundEntities.size(); i < l; i++) {
            Entity probableVictim = foundEntities.get(i);
            if (tempBB == null) {
                tempBB = new AABBMutable();
            }
            tempBB.set(probableVictim.getBoundingBox()).inflateMutable(inflation);
            if (tempBB.contains(start)) {
                if (hitResult == null) {
                    hitResult = new MultipleEntityHitResult(probableVictim, start, end);
                }
                hitResult.add(probableVictim, 0);
                continue;
            }
            double dist = clipDist(tempBB, start, end);
            if (!Double.isNaN(dist)) {
                if (hitResult == null) {
                    hitResult = new MultipleEntityHitResult(probableVictim, start, end);
                }
                hitResult.add(probableVictim, dist);
            }
        }
        if (hitResult != null) {
            hitResult.finish();
        }
        return hitResult;
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
     * @return A hit position offset in the desired {@link Direction}
     * if the {@link Direction} is in the desired {@link Axis}.
     * If it is not, the position will not change.
     */
    public static double hitOffset(Direction.Axis axis, double hit, Direction direction) {
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

    public static boolean isMouseInArea(double mouseX, double mouseY, int x, int y, int dx, int dy) {
        return isMouseInRange(mouseX, mouseY, x, y, x + dx, y + dy);
    }

    public static boolean isMouseInRange(double mouseX, double mouseY, int x0, int y0, int x1, int y1) {
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
    public static boolean isShapeTotallyInside(VoxelShape inside, VoxelShape reference) {
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

    public static boolean isSitting(LivingEntity victim) {
        return victim.isPassenger() && victim.getVehicle() != null && victim.getVehicle().shouldRiderSit();
    }

    /**
     * Compare two {@link JsonElement}s, taking into account nulls and number conversions.
     */
    public static boolean jsonEquals(JsonElement a, JsonElement b) {
        if (a == b) {
            return true;
        }
        //If one JsonObject has a key that the other one doesn't, verify that its value is not null.
        //If it's null, then it doesn't matter that the other one doesn't have the key.
        if (a instanceof JsonObject oa && b instanceof JsonObject ob) {
            Set<Map.Entry<String, JsonElement>> aEntries = oa.entrySet();
            for (Map.Entry<String, JsonElement> aEntry : aEntries) {
                String key = aEntry.getKey();
                if (!ob.has(key)) {
                    if (aEntry.getValue().isJsonNull()) {
                        continue;
                    }
                    return false;
                }
                if (!jsonEquals(ob.get(key), aEntry.getValue())) {
                    return false;
                }
            }
            Set<Map.Entry<String, JsonElement>> bEntries = oa.entrySet();
            for (Map.Entry<String, JsonElement> bEntry : bEntries) {
                String key = bEntry.getKey();
                if (!oa.has(key)) {
                    if (bEntry.getValue().isJsonNull()) {
                        continue;
                    }
                    return false;
                }
                if (!jsonEquals(oa.get(key), bEntry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        //JsonArrays contain JsonElements, so ensure that equality is checked using this method.
        if (a instanceof JsonArray aa && b instanceof JsonArray ab) {
            if (aa.size() != ab.size()) {
                return false;
            }
            for (int i = 0, len = aa.size(); i < len; i++) {
                if (!jsonEquals(aa.get(i), ab.get(i))) {
                    return false;
                }
            }
            return true;
        }
        //In theory, JsonPrimitives shouldn't have a problem when comparing, since can't have keys with null values.
        //However, if one JsonPrimitive is saved using a Float and the other is saved using a LazilyParsedNumber, the internal comparison upcasts
        //it into a double, which causes the original float number to acquire rounding errors. E.g. (double)0.05f = 0.05000000074505806.
        //So we have to ensure that, in this case, the comparison is made using floats.
        if (a instanceof JsonPrimitive pa && b instanceof JsonPrimitive pb) {
            if (pa.isNumber() && pb.isNumber()) {
                Number aNumber = pa.getAsNumber();
                Number bNumber = pb.getAsNumber();
                if (aNumber instanceof LazilyParsedNumber && bNumber instanceof Float) {
                    return aNumber.floatValue() == bNumber.floatValue();
                }
                if (bNumber instanceof LazilyParsedNumber && aNumber instanceof Float) {
                    return aNumber.floatValue() == bNumber.floatValue();
                }
            }
        }
        return a.equals(b);
    }

    public static float lerpDeg(float partialTicks, float prevAngle, float angle, boolean wrap) {
        float delta = wrap ? Mth.wrapDegrees(angle - prevAngle) : angle - prevAngle;
        return prevAngle + partialTicks * delta;
    }

    public static float lerpRad(float partialTicks, float prevAngle, float angle, boolean wrap) {
        float delta = wrap ? wrapRadians(angle - prevAngle) : angle - prevAngle;
        return prevAngle + partialTicks * delta;
    }

    /**
     * Used to compare two {@code boolean} matrices of the same size.
     *
     * @param a The first matrix.
     * @param b The second matrix.
     * @return {@code true} if the matrices are equal, {@code false} otherwise.
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

    public static BlockHitResult rayTraceBlocksFromCamera(Entity victim,
                                                          Vec3d cameraPos,
                                                          float partialTicks,
                                                          double distance,
                                                          boolean fluid) {
        Vec3 from = cameraPos.isNull() ? victim.getEyePosition(partialTicks) : cameraPos.asImmutable();
        Vec3 look = victim.getViewVector(partialTicks);
        Vec3 to = from.add(look.x * distance, look.y * distance, look.z * distance);
        return victim.level.clip(
                new ClipContext(from, to, ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, victim));
    }

    /**
     * Casts a ray tracing for {@code Block}s starting from the eyes of the desired {@link Entity}.
     *
     * @param victim       The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks The partial ticks to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param distance     The maximum distance the ray will travel.
     * @param fluid        {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockHitResult} containing the {@link net.minecraft.core.BlockPos} of the {@code Block} hit.
     */
    public static BlockHitResult rayTraceBlocksFromEyes(Entity victim, float partialTicks, double distance, boolean fluid) {
        Vec3 from = victim.getEyePosition(partialTicks);
        Vec3 look = victim.getViewVector(partialTicks);
        Vec3 to = from.add(look.x * distance, look.y * distance, look.z * distance);
        return victim.level.clip(
                new ClipContext(from, to, ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, victim));
    }

    /**
     * Casts a ray tracing for {@code Block}s based on the {@link Entity}'s {@link Entity#yRot} and {@link Entity#xRot}.
     * This method is useful for Entities that are projectiles, as {@link MathHelper#rayTraceBlocksFromEyes(Entity, float, double, boolean)}
     * usually do not work on them.
     *
     * @param victim   The {@link Entity} from whose {@code yaw} and {@code pitch} to cast the ray.
     * @param distance The maximum distance this ray will travel.
     * @param fluid    {@code true} if the ray can hit fluids, {@code false} if the ray can go through them.
     * @return A {@link BlockHitResult} containing the {@link BlockPos} of the {@code Block} hit.
     */
    public static BlockHitResult rayTraceBlocksFromYawAndPitch(Entity victim, double distance, boolean fluid) {
        Vec3 from = victim.getEyePosition(1.0f);
        float theta = victim.getYRot();
        if (theta < 0) {
            theta += 360;
        }
        theta *= Mth.DEG_TO_RAD;
        float phi = victim.getXRot();
        if (phi < 0) {
            phi += 360;
        }
        phi *= Mth.DEG_TO_RAD;
        Vec3 looking = new Vec3(Mth.sin(theta), Mth.sin(phi), Mth.cos(theta)).normalize();
        Vec3 to = from.add(looking.x * distance, looking.y * distance, looking.z * distance);
        return victim.level.clip(
                new ClipContext(from, to, ClipContext.Block.OUTLINE, fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, victim));
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
    public static EntityHitResult rayTraceEntities(Entity toExclude, Vec3 startVec, Vec3 endVec, AABB boundingBox, double distanceSquared) {
        Level level = toExclude.level;
        double range = distanceSquared;
        Entity entity = null;
        Vec3 vec3d = null;
        for (Entity entityInBoundingBox : level.getEntities(toExclude, boundingBox, PICKABLE_ENTITIES)) {
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
     * Casts a ray tracing for Entities starting from the eyes of the desired {@link Entity} and extends to its reach distance.
     * The ray will only collide with OBBs.
     *
     * @param victim       The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks The partial tick to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param reach        The max distance the ray will travel.
     * @return An {@link EntityHitResult} containing the {@link Entity} hit by the ray traced and a {@link Vector3d}
     * containing the position of the hit. If no {@link Entity} was hit by the ray, this {@link EntityHitResult} will be {@code null}.
     */
    @Nullable
    public static EntityHitResult rayTraceEntitiesFromEyes(Entity victim, Vec3d cameraPos, float partialTicks, final double reach) {
        //From vector
        final double fromX;
        final double fromY;
        final double fromZ;
        if (cameraPos.isNull()) {
            //Eye position
            fromX = Mth.lerp(partialTicks, victim.xo, victim.getX());
            fromY = Mth.lerp(partialTicks, victim.yo, victim.getY());
            fromZ = Mth.lerp(partialTicks, victim.zo, victim.getZ());
        }
        else {
            //Camera position
            fromX = cameraPos.x();
            fromY = cameraPos.y();
            fromZ = cameraPos.z();
        }
        //Entity view vector (won't result in new allocations as we already dealt with those)
        Vec3 look = victim.getViewVector(partialTicks);
        //To vector
        final double toX = fromX + look.x * reach;
        final double toY = fromY + look.y * reach;
        final double toZ = fromZ + look.z * reach;
        //Promising variables
        Entity promEntity = null;
        Hitbox promBox = null;
        double promDist = reach;
        //Scan for entities nearby
        List<Entity> foundEntities = victim.level.getEntities(victim, new AABB(fromX, fromY, fromZ, toX, toY, toZ).inflate(2.5), PICKABLE_ENTITIES);
        if (!foundEntities.isEmpty()) {
            Hitbox[] boxHolder = new Hitbox[1];
            //Iterate over the entities
            for (int i = 0, l = foundEntities.size(); i < l; i++) {
                Entity entityInBB = foundEntities.get(i);
                double dist = rayTracingEntityHitboxes(entityInBB, fromX, fromY, fromZ, look.x, look.y, look.z, promDist, partialTicks,
                                                       boxHolder);
                if (!Double.isNaN(dist)) {
                    //Collided with victim, checking if the results are better than what we have
                    if (dist < promDist) {
                        promDist = dist;
                        promEntity = entityInBB;
                        promBox = boxHolder[0];
                        if (dist == 0) {
                            break;
                        }
                    }
                }
            }
        }
        if (promEntity == null) {
            return null;
        }
        //Create vector from distance
        double hitX = fromX + promDist * (toX - fromX);
        double hitY = fromY + promDist * (toY - fromY);
        double hitZ = fromZ + promDist * (toZ - fromZ);
        return new AdvancedEntityHitResult(promEntity, new Vec3(hitX, hitY, hitZ), promBox);
    }

    /**
     * Casts a ray tracing for Entities starting from the eyes of the desired {@link Entity}.
     *
     * @param victim        The {@link Entity} from whose eyes to cast the ray off.
     * @param partialTicks  The partial tick to interpolate, between {@code 0.0f} and {@code 1.0f}.
     * @param reachDistance The max distance the ray will travel.
     * @return An {@link EntityHitResult} containing the {@link Entity} hit by the ray traced
     * and a {@link Vec3} containing the position of the hit. If no {@link Entity} was hit by the ray,
     * this {@link EntityHitResult} will be {@code null}.
     */
    @Nullable
    public static EntityHitResult rayTraceEntityFromEyes(Entity victim, float partialTicks, double reachDistance) {
        Vec3 from = victim.getEyePosition(partialTicks);
        Vec3 look = victim.getViewVector(partialTicks).normalize();
        Vec3 to = from.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        return rayTraceEntities(victim, from, to, new AABB(from, to), reachDistance * reachDistance);
    }

    /**
     * Ray traces an Entity's hitboxes. If the Entity has no defined OBBs, it will default to the Bounding Box. If the Entity has OBBs,
     * the ray will only collide with the nearest OBB to its origin.
     *
     * @return The distance between the ray origin and the hit box, if it hits, or {@link Double#NaN} otherwise.
     */
    private static <T extends Entity> double rayTracingEntityHitboxes(T victim,
                                                                      final double fromX, final double fromY, final double fromZ,
                                                                      final double lookX, final double lookY, final double lookZ,
                                                                      final double reach,
                                                                      float partialTicks,
                                                                      Hitbox[] chosenBox) {
        chosenBox[0] = null;
        double oldDist = reach;
        final double toX = fromX + lookX * reach;
        final double toY = fromY + lookY * reach;
        final double toZ = fromZ + lookZ * reach;
        HitboxEntity<T> hitbox = (HitboxEntity<T>) ((IEntityPatch) victim).getHitboxes();
        if (hitbox == null) {
            AABB bb = victim.getBoundingBox();
            if (bb.contains(fromX, fromY, fromZ)) {
                return 0;
            }
            return clipDist(bb, fromX, fromY, fromZ, toX, toY, toZ);
        }
        hitbox.init(victim, partialTicks);
        final double posX = Mth.lerp(partialTicks, victim.xOld, victim.getX());
        final double posY = Mth.lerp(partialTicks, victim.yOld, victim.getY());
        final double posZ = Mth.lerp(partialTicks, victim.zOld, victim.getZ());
        double fromX0 = fromX - posX;
        double fromY0 = fromY - posY;
        double fromZ0 = fromZ - posZ;
        double fromXa = hitbox.preUntransformX(fromX0);
        double fromYa = hitbox.preUntransformY(fromY0);
        double fromZa = hitbox.preUntransformZ(fromZ0);
        fromX0 = hitbox.postUntransformX(fromXa, fromYa, fromZa);
        fromY0 = hitbox.postUntransformY(fromXa, fromYa, fromZa);
        fromZ0 = hitbox.postUntransformZ(fromXa, fromYa, fromZa);
        double toX0 = toX - posX;
        double toY0 = toY - posY;
        double toZ0 = toZ - posZ;
        double toXa = hitbox.preUntransformX(toX0);
        double toYa = hitbox.preUntransformY(toY0);
        double toZa = hitbox.preUntransformZ(toZ0);
        toX0 = hitbox.postUntransformX(toXa, toYa, toZa);
        toY0 = hitbox.postUntransformY(toXa, toYa, toZa);
        toZ0 = hitbox.postUntransformZ(toXa, toYa, toZa);
        double clipDist = Double.NaN;
        for (int i = 0, l = hitbox.getBoxes().size(); i < l; i++) {
            Hitbox box = hitbox.getBoxes().get(i);
            Matrix4d transform = box.adjustedTransform();
            fromXa = transform.preUntransformX(fromX0);
            fromYa = transform.preUntransformY(fromY0);
            fromZa = transform.preUntransformZ(fromZ0);
            double newFromX = transform.postUntransformX(fromXa, fromYa, fromZa);
            double newFromY = transform.postUntransformY(fromXa, fromYa, fromZa);
            double newFromZ = transform.postUntransformZ(fromXa, fromYa, fromZa);
            if (box.contains(newFromX, newFromY, newFromZ)) {
                chosenBox[0] = box;
                return 0;
            }
            toXa = transform.preUntransformX(toX0);
            toYa = transform.preUntransformY(toY0);
            toZa = transform.preUntransformZ(toZ0);
            double newToX = transform.postUntransformX(toXa, toYa, toZa);
            double newToY = transform.postUntransformY(toXa, toYa, toZa);
            double newToZ = transform.postUntransformZ(toXa, toYa, toZa);
            double dist = box.clipDist(newFromX, newFromY, newFromZ, newToX, newToY, newToZ);
            if (!Double.isNaN(dist)) {
                if (dist < oldDist) {
                    oldDist = clipDist = dist;
                    chosenBox[0] = box;
                    if (dist == 0) {
                        return 0;
                    }
                }
            }
        }
        return clipDist;
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
     * Resets a {@code long} array (representing a 8x8x8 boolean tensor) starting at the desired index to
     * {@link tgw.evolution.blocks.tileentities.Patterns#MATRIX_FALSE}.
     *
     * @param tensor A {@code long} tensor to reset.
     * @param index  The index to start the reset.
     */
    public static void resetTensor(long[] tensor, int index) {
        for (int i = index; i < tensor.length; i++) {
            tensor[i] = Patterns.MATRIX_FALSE;
        }
    }

    /**
     * Rotates a square {@code boolean} matrix clockwise.
     *
     * @param input A square {@code boolean} matrix.
     * @return A {@code new} square {@code boolean} matrix rotated 90 degrees clockwise.
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
     * Rotates a {@link VoxelShape}.
     *
     * @param from  The {@link Direction} the {@link VoxelShape} is originally facing.
     * @param to    The desired {@link Direction} for the {@link VoxelShape} to face.
     * @param shape The {@link VoxelShape} to rotate.
     * @return A {@code new} {@link VoxelShape} rotated if the {@link Direction}s are different,
     * otherwise returns the same {@link VoxelShape}.
     */
    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        if (to == from) {
            return shape;
        }
        VoxelShape[] buffer = {shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            //noinspection ObjectAllocationInLoop
            buffer[0].forAllBoxes(
                    (minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
                                                                                  Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
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
    public static void setRotationAngle(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    /**
     * Approximates the trigonometric function sine.
     *
     * @param deg The argument of the sine, given in degrees.
     * @return An approximation of the sine of the given argument.
     * The returned value will be between {@code 0.0f} and {@code 1.0f}, inclusive.
     */
    public static float sinDeg(float deg) {
        return Mth.sin(deg * Mth.DEG_TO_RAD);
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

    public static String stripAccents(String input, @Nullable StringBuilder builder) {
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
    public static VoxelShape subtract(VoxelShape A, VoxelShape B) {
        return Shapes.join(A, B, BooleanOp.ONLY_FIRST);
    }

    public static float tanDeg(float angle) {
        angle *= Mth.DEG_TO_RAD;
        return Mth.sin(angle) / Mth.cos(angle);
    }

    /**
     * Converts an {@code int} value to {@code byte}.
     *
     * @param value The value to convert.
     * @return The value converted to {@code byte}.
     * @throws ArithmeticException if the value cannot be represented as a {@code byte}.
     */
    public static byte toByteExact(int value) {
        byte b = (byte) value;
        if (b != value) {
            throw new ArithmeticException("Byte overflow " + value);
        }
        return b;
    }

    /**
     * Converts an {@code int} value to {@code short}.
     *
     * @param value The value to convert.
     * @return The value converted to {@code short}.
     * @throws ArithmeticException if the value cannot be represented as a {@code short}.
     */
    public static short toShortExact(int value) {
        short s = (short) value;
        if (s != value) {
            throw new ArithmeticException("Short overflow " + value);
        }
        return s;
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
    public static VoxelShape union(VoxelShape A, VoxelShape B) {
        return Shapes.join(A, B, BooleanOp.OR);
    }

    /**
     * Wraps the angle given in radians in the range [-pi; pi)
     *
     * @param value The angle value in radians.
     * @return The equivalent angle value wrapped.
     */
    public static float wrapRadians(float value) {
        float d = value % Mth.TWO_PI;
        if (d >= Mth.PI) {
            d -= Mth.TWO_PI;
        }
        if (d < -Mth.PI) {
            d += Mth.TWO_PI;
        }
        return d;
    }
}
