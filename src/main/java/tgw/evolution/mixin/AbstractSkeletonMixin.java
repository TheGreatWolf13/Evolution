package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.hitbox.LegacyEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster implements ILivingEntityPatch<AbstractSkeleton> {

    public AbstractSkeletonMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public @Nullable HitboxEntity<AbstractSkeleton> getHitboxes() {
        return LegacyEntityHitboxes.SKELETON.get((AbstractSkeleton) (Object) this);
    }
}
