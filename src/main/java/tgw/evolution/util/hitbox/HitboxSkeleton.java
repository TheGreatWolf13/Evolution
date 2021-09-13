package tgw.evolution.util.hitbox;

import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.util.math.vector.Vector3d;

public class HitboxSkeleton extends HitboxEntity<AbstractSkeletonEntity> {

    public static final Vector3d NECK_STANDING = new Vector3d(0, 24 / 16.0, 0);

    @Override
    public void init(AbstractSkeletonEntity entity, float partialTicks) {
        //TODO implementation
    }
}
