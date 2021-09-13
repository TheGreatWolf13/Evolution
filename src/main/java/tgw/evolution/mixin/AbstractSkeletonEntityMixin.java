package tgw.evolution.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.util.hitbox.HitboxSkeleton;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonEntityMixin extends MonsterEntity implements INeckPosition {

    public AbstractSkeletonEntityMixin(EntityType<? extends MonsterEntity> type, World world) {
        super(type, world);
    }

    @Override
    public float getCameraYOffset() {
        return 4 / 16.0f;
    }

    @Override
    public float getCameraZOffset() {
        return 4 / 16.0f;
    }

    @Override
    public Vector3d getNeckPoint() {
        return HitboxSkeleton.NECK_STANDING;
    }
}
