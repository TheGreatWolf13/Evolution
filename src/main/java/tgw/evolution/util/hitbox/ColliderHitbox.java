package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.math.AABBMutable;

public class ColliderHitbox extends Hitbox {

    private final boolean shouldSwitch;
    private @Nullable HitboxAttachable attachedTo;
    private boolean rightHanded = true;

    public ColliderHitbox(HitboxType part, double x0, double y0, double z0, double x1, double y1, double z1) {
        this(part, x0, y0, z0, x1, y1, z1, true);
    }

    public ColliderHitbox(HitboxType part, double x0, double y0, double z0, double x1, double y1, double z1, boolean shouldSwitch) {
        super(part, AABBMutable.block(x0, y0, z0, x1, y1, z1), null);
        this.shouldSwitch = shouldSwitch;
    }

    public ColliderHitbox adjust(HumanoidArm arm) {
        if (this.shouldSwitch) {
            if (arm == HumanoidArm.RIGHT) {
                if (!this.rightHanded) {
                    ((AABBMutable) this.aabb).setX(-super.minX(), -super.maxX());
                    this.rightHanded = true;
                }
            }
            else {
                if (this.rightHanded) {
                    ((AABBMutable) this.aabb).setX(-super.minX(), -super.maxX());
                    this.rightHanded = false;
                }
            }
        }
        return this;
    }

    @Override
    public Matrix4d adjustedColliderTransform() {
        assert this.attachedTo != null;
        return this.attachedTo.adjustedColliderTransform();
    }

    @Override
    public Matrix4d adjustedTransform() {
        assert this.attachedTo != null;
        return this.attachedTo.adjustedTransform();
    }

    public ColliderHitbox attach(HitboxAttachable hitbox) {
        this.attachedTo = hitbox;
        return this;
    }

    @Override
    public double maxX() {
        assert this.attachedTo != null;
        return this.attachedTo.getLocalOrigin().x + super.maxX();
    }

    @Override
    public double maxY() {
        assert this.attachedTo != null;
        return this.attachedTo.getLocalOrigin().y + super.maxY();
    }

    @Override
    public double maxZ() {
        assert this.attachedTo != null;
        return this.attachedTo.getLocalOrigin().z + super.maxZ();
    }

    @Override
    public double minX() {
        assert this.attachedTo != null;
        return this.attachedTo.getLocalOrigin().x + super.minX();
    }

    @Override
    public double minY() {
        assert this.attachedTo != null;
        return this.attachedTo.getLocalOrigin().y + super.minY();
    }

    @Override
    public double minZ() {
        assert this.attachedTo != null;
        return this.attachedTo.getLocalOrigin().z + super.minZ();
    }

    public void setParent(IRoot parent) {
        this.parent = parent;
    }
}
