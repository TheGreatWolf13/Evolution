package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.items.IMelee;

public class HitboxSkeleton extends HitboxEntity<AbstractSkeleton> {

    public HitboxSkeleton() {
        this.finish();
    }

    @Override
    protected void childFinish() {
        //TODO implementation

    }

    @Override
    protected @Nullable Hitbox childGetEquipFor(IMelee.@Nullable IAttackType type, HumanoidArm arm) {
        return null;
    }

    @Override
    public void childInit(AbstractSkeleton entity, float partialTicks) {
        //TODO implementation
    }

    @Override
    protected Hitbox headOrRoot() {
        //TODO implementation
        return null;
    }

    @Override
    protected double relativeHeadOrRootX() {
        //TODO implementation
        return 0;
    }

    @Override
    protected double relativeHeadOrRootY() {
        //TODO implementation
        return 0;
    }

    @Override
    protected double relativeHeadOrRootZ() {
        //TODO implementation
        return 0;
    }
}
