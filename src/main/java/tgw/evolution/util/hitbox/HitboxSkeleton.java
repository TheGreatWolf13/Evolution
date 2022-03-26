package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.items.ISpecialAttack;
import tgw.evolution.util.UnregisteredFeatureException;

public class HitboxSkeleton extends HitboxEntity<AbstractSkeleton> {

    public static final Vec3 NECK_STANDING = new Vec3(0, 24 / 16.0, 0);

    public HitboxSkeleton() {
        this.finish();
    }

    @Override
    protected void childFinish() {
        //TODO implementation

    }

    @Override
    public Hitbox getEquipmentFor(ISpecialAttack.IAttackType type, HumanoidArm arm) {
        throw new UnregisteredFeatureException("No hitbox registered for " + type + " on " + arm);
    }

    @Override
    public void init(AbstractSkeleton entity, float partialTicks) {
        //TODO implementation
    }
}
