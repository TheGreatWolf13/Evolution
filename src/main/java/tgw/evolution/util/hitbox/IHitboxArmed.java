package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.util.math.Vec3d;

public interface IHitboxArmed<T extends LivingEntity> extends IHitboxAccess<T> {

    Hitbox getHand(HumanoidArm arm);

    HitboxAttachable getItemAttach(HumanoidArm arm);

    default Vec3d getOffsetForArm(T entity, float partialTicks, HumanoidArm arm) {
        this.init(entity, partialTicks);
        HitboxAttachable ha = this.getItemAttach(arm);
        Matrix4d transform = ha.adjustedTransform();
        Vec3 origin = ha.getLocalOrigin();
        double x = transform.transformX(origin.x, origin.y, origin.z);
        double y = transform.transformY(origin.x, origin.y, origin.z);
        double z = transform.transformZ(origin.x, origin.y, origin.z);
        return this.helperOffset().set(this.transformX(x, y, z), this.transformY(x, y, z), this.transformZ(x, y, z));
    }
}
