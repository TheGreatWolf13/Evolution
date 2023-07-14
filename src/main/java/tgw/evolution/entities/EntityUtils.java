package tgw.evolution.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import tgw.evolution.world.util.LevelUtils;

import java.util.UUID;

public final class EntityUtils {

    public static final UUID UUID_ZERO = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final GameProfile EMPTY_PROFILE = new GameProfile(UUID_ZERO, "");

    private EntityUtils() {
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
