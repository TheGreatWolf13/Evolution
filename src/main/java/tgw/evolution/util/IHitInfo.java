package tgw.evolution.util;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Range;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.hitbox.Matrix4d;
import tgw.evolution.util.math.Vec3d;

public interface IHitInfo {

    void addHitbox(Entity entity, HitboxType hitbox);

    boolean contains(Entity entity, HitboxType hitbox);

    Vec3d getOrMakeEdge(@Range(from = 0, to = 11) int edge, boolean start);

    Vec3d getOrMakeEdgeInHBVS(@Range(from = 0, to = 11) int edge, boolean start, Matrix4d transform);

    Vec3d getOrMakeVertex(@Range(from = 0, to = 7) int index);

    Vec3d getOrMakeVertexInHBVS(@Range(from = 0, to = 7) int index, Matrix4d transform);

    void prepareInHBVS(HitboxEntity<?> hitboxes, double victimX, double victimY, double victimZ);

    void releaseInHBVS();

    void softRelease();
}
