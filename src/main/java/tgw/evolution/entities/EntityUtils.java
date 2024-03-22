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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.world.util.LevelUtils;

import java.util.UUID;

public final class EntityUtils {

    public static final UUID UUID_ZERO = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final GameProfile EMPTY_PROFILE = new GameProfile(UUID_ZERO, "");

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
