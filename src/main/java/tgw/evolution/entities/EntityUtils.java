package tgw.evolution.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.VectorUtil;
import tgw.evolution.world.util.CollisionShapeCalculator;
import tgw.evolution.world.util.LevelUtils;

import java.util.Map;
import java.util.UUID;

public final class EntityUtils {

    public static final UUID UUID_ZERO = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final GameProfile EMPTY_PROFILE = new GameProfile(UUID_ZERO, "");
    public static final Map<UUID, SkinType> SKIN_TYPE = new O2OHashMap<>();

    private EntityUtils() {
    }

    public static @Nullable Entity create(CompoundTag nbt, Level level) {
        String id = nbt.getString("id");
        EntityType<?> entityType = (EntityType<?>) Registry.ENTITY_TYPE.getNullable(new ResourceLocation(id));
        if (entityType == null) {
            Evolution.warn("Skipping Entity with id {}", id);
            return null;
        }
        Entity entity = entityType.create(level);
        if (entity == null) {
            Evolution.warn("Skipping Entity with id {}", id);
            return null;
        }
        entity.load(nbt);
        return entity;
    }

    public static @Nullable Vec3 findFreePosition(CollisionGetter level, @Nullable Entity entity, VoxelShape shape, double centerX, double centerY, double centerZ, double sizeX, double sizeY, double sizeZ) {
        if (shape.isEmpty()) {
            return null;
        }
        double minX = shape.min(Direction.Axis.X) - sizeX;
        double minY = shape.min(Direction.Axis.Y) - sizeY;
        double minZ = shape.min(Direction.Axis.Z) - sizeZ;
        double maxX = shape.max(Direction.Axis.X) + sizeX;
        double maxY = shape.max(Direction.Axis.Y) + sizeY;
        double maxZ = shape.max(Direction.Axis.Z) + sizeZ;
        WorldBorder worldBorder = level.getWorldBorder();
        try (CollisionShapeCalculator calculator = CollisionShapeCalculator.getInstance(level, entity, minX, minY, minZ, maxX, maxY, maxZ, false)) {
            VoxelShape globalShape = Shapes.empty();
            for (VoxelShape voxelShape : calculator) {
                if (worldBorder.isWithinBounds_(shape.min(Direction.Axis.X), shape.min(Direction.Axis.Z), shape.max(Direction.Axis.X), shape.max(Direction.Axis.Z))) {
                    OList<AABB> aabbs = voxelShape.cachedBoxes();
                    for (int i = 0, len = aabbs.size(); i < len; ++i) {
                        AABB aabb = aabbs.get(i);
                        globalShape = Shapes.or(globalShape, Shapes.create(aabb.minX - sizeX * 0.5, aabb.minY - sizeY * 0.5, aabb.minZ - sizeZ * 0.5, aabb.maxX + sizeX * 0.5, aabb.maxY + sizeY * 0.5, aabb.maxZ + sizeZ * 0.5));
                    }
                }
            }
            globalShape = Shapes.join(shape, globalShape, BooleanOp.ONLY_FIRST);
            if (globalShape.isEmpty()) {
                return null;
            }
            Vec3d vec = new Vec3d(Vec3d.NULL);
            globalShape.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
                double clampX = Mth.clamp(centerX, x0, x1);
                double clampY = Mth.clamp(centerY, y0, y1);
                double clampZ = Mth.clamp(centerZ, z0, z1);
                if (vec.isNull() || VectorUtil.distSqr(centerX, centerY, centerZ, clampX, clampY, clampZ) < vec.distanceToSqr(centerX, centerY, centerZ)) {
                    vec.set(clampX, clampY, clampZ);
                }
            });
            if (vec.isNull()) {
                return null;
            }
            return vec;
        }
    }

    public static boolean isAbove(Entity entity, VoxelShape shape, int y) {
        return entity.getY() > y + shape.max(Direction.Axis.Y) - 1e-5;
    }

    public static boolean isPlayerNearUnloadedChunks(Player player) {
        AABB bb = player.getBoundingBox();
        int minX = Mth.floor(bb.minX - 1);
        int maxX = Mth.ceil(bb.maxX + 1);
        int minZ = Mth.floor(bb.minZ - 1);
        int maxZ = Mth.ceil(bb.maxZ + 1);
        return !LevelUtils.forceHasChunksAt(player.level, minX, minZ, maxX, maxZ);
    }
}
