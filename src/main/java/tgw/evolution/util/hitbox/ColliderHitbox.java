package tgw.evolution.util.hitbox;

import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class ColliderHitbox extends Hitbox {

    private @Nullable HitboxAttachable attachedTo;

    public ColliderHitbox(HitboxType part, AABB aabb) {
        super(part, aabb, null);
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

    public void setParent(HitboxEntity parent) {
        this.parent = parent;
    }
}
