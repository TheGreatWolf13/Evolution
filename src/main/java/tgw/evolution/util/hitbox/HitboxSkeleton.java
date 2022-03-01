package tgw.evolution.util.hitbox;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.items.ICustomAttack;
import tgw.evolution.util.UnregisteredFeatureException;

public class HitboxSkeleton extends HitboxEntity<AbstractSkeleton> {

    public static final Vec3 NECK_STANDING = new Vec3(0, 24 / 16.0, 0);

    @Override
    public Hitbox getEquipmentFor(ICustomAttack.AttackType type, InteractionHand hand) {
        switch (hand) {
            case MAIN_HAND, OFF_HAND -> throw new UnregisteredFeatureException("No hitbox registered for " + type + " on " + hand);
        }
        throw new IllegalStateException("Unknown hand: " + hand);
    }

    @Override
    public void init(AbstractSkeleton entity, float partialTicks) {
        //TODO implementation
    }
}
