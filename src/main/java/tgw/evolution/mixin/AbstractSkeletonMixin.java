package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.INeckPosition;
import tgw.evolution.util.hitbox.HitboxSkeleton;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster implements INeckPosition {

    public AbstractSkeletonMixin(EntityType<? extends Monster> type, Level world) {
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
    public Vec3 getNeckPoint() {
        return HitboxSkeleton.NECK_STANDING;
    }
}
