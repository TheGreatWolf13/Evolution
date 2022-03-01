package tgw.evolution.util.hitbox;

import net.minecraft.world.phys.AABB;

import static tgw.evolution.util.hitbox.HitboxEntity.aabb;

public final class HitboxLib {

    public static final AABB BIPED_HEAD = aabb(-4, 0, -4, 4, 8, 4);
    public static final AABB CREEPER_LEG = aabb(-2, -6, -2, 2, 0, 2);
    public static final AABB PLAYER_LEG = aabb(-2, -8, -2, 2, 0, 2, HitboxPlayer.SCALE);
    public static final AABB PLAYER_FOOT = aabb(-2, -12, -2, 2, -8, 2, HitboxPlayer.SCALE);

    private HitboxLib() {
    }
}
