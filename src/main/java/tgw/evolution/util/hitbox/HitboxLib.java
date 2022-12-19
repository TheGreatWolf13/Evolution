package tgw.evolution.util.hitbox;

import net.minecraft.world.phys.AABB;

import static tgw.evolution.util.hitbox.hitboxes.HitboxEntity.box;

public final class HitboxLib {

    //Generic
    public static final AABB ZERO = box(0, -4, 0, 0, 0, 0);
    //Humanoid
    public static final AABB HUMANOID_HEAD = box(-4, 0, -4, 8, 8, 8);
    public static final AABB HUMANOID_CHEST = box(-4, -12, -2, 8, 12, 4);
    public static final AABB HUMANOID_RIGHT_SHOULDER = box(-1, -2, -2, 4, 3, 4);
    public static final AABB HUMANOID_RIGHT_ARM = box(-1, -6, -2, 4, 3, 4);
    public static final AABB HUMANOID_RIGHT_FOREARM = box(-2, -3, -2, 4, 3, 4);
    public static final AABB HUMANOID_RIGHT_HAND = box(-2, -6, -2, 4, 3, 4);
    public static final AABB HUMANOID_LEFT_SHOULDER = box(-3, -2, -2, 4, 3, 4);
    public static final AABB HUMANOID_LEFT_ARM = box(-3, -6, -2, 4, 3, 4);
    public static final AABB HUMANOID_LEFT_FOREARM = box(-2, -3, -2, 4, 3, 4);
    public static final AABB HUMANOID_LEFT_HAND = box(-2, -6, -2, 4, 3, 4);
    public static final AABB HUMANOID_LEG = box(-2, -6, -2, 4, 6, 4);
    public static final AABB HUMANOID_FORELEG = box(-2, -4, -2, 4, 4, 4);
    public static final AABB HUMANOID_FOOT = box(-2, -6, -2, 4, 2, 4);
    //Creeper
    public static final AABB CREEPER_LEG = box(-2, -6, -2, 4, 6, 4);
    //Skeleton
    public static final AABB SKELETON_SHOULDER = box(-1, -2, -1, 2, 4, 2);
    public static final AABB SKELETON_ARM = box(-1, -6, -1, 2, 4, 2);
    public static final AABB SKELETON_HAND = box(-1, -10, -1, 2, 4, 2);
    public static final AABB SKELETON_LEG = box(-1, -8, -1, 2, 8, 2);
    public static final AABB SKELETON_FOOT = box(-1, -12, -1, 2, 4, 2);

    private HitboxLib() {
    }
}
