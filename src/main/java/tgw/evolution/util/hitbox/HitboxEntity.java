package tgw.evolution.util.hitbox;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.items.IMelee;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.hitbox.hms.HMEntity;
import tgw.evolution.util.hitbox.hrs.HR;
import tgw.evolution.util.hitbox.hrs.HREntity;
import tgw.evolution.util.math.Vec3d;

import java.util.List;

public abstract class HitboxEntity<T extends Entity> implements HMEntity<T>, HREntity<T>, HR, IHitboxAccess<T>, IRoot {

    protected final Matrix4d helperColliderTransform = new Matrix4d();
    protected final Vec3d helperOffset = new Vec3d();
    protected final Matrix4d helperTransform = new Matrix4d();
    private final RList<Hitbox> boxes;
    private final Vec3d cachedRenderOffset = new Vec3d();
    private final Matrix4d colliderTransform = new Matrix4d();
    private final Matrix4d transform = new Matrix4d();
    protected float ageInTicks;
    protected float attackTime;
    protected float pivotX;
    protected float pivotY;
    protected float pivotZ;
    protected boolean riding;
    protected float scaleX = 1.0f;
    protected float scaleY = 1.0f;
    protected float scaleZ = 1.0f;
    protected boolean young;
    private @Nullable Hitbox activeEquip;

    public HitboxEntity() {
        this.boxes = new RArrayList<>();
    }

    protected static AABB box(double minX, double minY, double minZ, double dimX, double dimY, double dimZ) {
        return new AABB(minX / 16, minY / 16, minZ / 16, (minX + dimX) / 16, (minY + dimY) / 16, (minZ + dimZ) / 16);
    }

    protected final Hitbox addBox(HitboxType part, AABB aabb, float x, float y, float z, IRoot parent) {
        Hitbox box = new Hitbox(part, aabb, parent);
        this.boxes.add(box);
        box.setPivotX(x);
        box.setPivotY(y);
        box.setPivotZ(z);
        return box;
    }

    protected final Hitbox addBox(HitboxType part, AABB aabb, IRoot parent) {
        return this.addBox(part, aabb, 0, 0, 0, parent);
    }

    protected final HitboxAttachable addBoxAttachable(HitboxType part,
                                                      AABB aabb,
                                                      float x,
                                                      float y,
                                                      float z,
                                                      double localOriginX,
                                                      double localOriginY,
                                                      double localOriginZ,
                                                      IRoot parent) {
        HitboxAttachable box = new HitboxAttachable(part, aabb, parent, localOriginX / 16.0, localOriginY / 16.0, localOriginZ / 16.0);
        this.boxes.add(box);
        box.setPivotX(x);
        box.setPivotY(y);
        box.setPivotZ(z);
        return box;
    }

    @Override
    public float attackTime() {
        return this.attackTime;
    }

    protected abstract @Nullable Hitbox childGetEquipFor(T entity, IMelee.IAttackType type, HumanoidArm arm);

    protected abstract void childInit(T entity, float partialTicks);

    public final void drawAllBoxes(T entity,
                                   float partialTicks,
                                   VertexConsumer buffer,
                                   PoseStack matrices,
                                   float x, float y, float z) {
        this.init(entity, partialTicks);
        Matrix4f pose = matrices.last().pose();
        Matrix3f normal = matrices.last().normal();
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            Hitbox hitbox = this.boxes.get(i);
            hitbox.drawEdges(hitbox.adjustedTransform(), buffer, pose, normal, x, y, z, 1.0f, 1.0f, 0.0f, 1.0f);
        }
        if (this.activeEquip != null) {
            if (entity instanceof ILivingEntityPatch patch && patch.shouldRenderSpecialAttack()) {
                if (this.activeEquip instanceof ColliderHitbox colliderHitbox) {
                    colliderHitbox.setParent(this);
                }
                this.activeEquip.drawEdges(this.activeEquip.adjustedTransform(), buffer, pose, normal, x, y, z, 1.0f, 0.0f, 1.0f, 1.0f);
            }
        }
    }

    public final void drawBox(Hitbox hitbox,
                              T entity,
                              float partialTicks,
                              VertexConsumer buffer,
                              PoseStack matrices,
                              float x, float y, float z, float red, float green, float blue, float alpha) {
        this.init(entity, partialTicks);
        Matrix4f pose = matrices.last().pose();
        Matrix3f normal = matrices.last().normal();
        hitbox.drawEdges(hitbox.adjustedTransform(), buffer, pose, normal, x, y, z, red, green, blue, alpha);
    }

    protected final void finish() {
        this.boxes.trimCollection();
    }

    public final List<Hitbox> getBoxes() {
        return this.boxes;
    }

    public Matrix4d getColliderTransform() {
        return this.colliderTransform.set(this.transform);
    }

    public final @Nullable Hitbox getEquipFor(T entity, @Nullable IMelee.IAttackType type, HumanoidArm arm) {
        if (type == null) {
            return null;
        }
        this.activeEquip = this.childGetEquipFor(entity, type, arm);
        if (this.activeEquip == null) {
            Evolution.warn(
                    "No hitbox registered for AttackType " + type + " on " + (arm == HumanoidArm.RIGHT ? "Right Arm" : "Left Arm") + " of " + this);
        }
        return this.activeEquip;
    }

    public final Vec3d getOffsetForCamera(T entity, float partialTicks) {
        this.init(entity, partialTicks);
        Matrix4d transform = this.headOrRoot().adjustedTransform();
        double x0 = this.relativeHeadOrRootX();
        double y0 = this.relativeHeadOrRootY();
        double z0 = this.relativeHeadOrRootZ();
        double x = transform.transformX(x0, y0, z0);
        double y = transform.transformY(x0, y0, z0);
        double z = transform.transformZ(x0, y0, z0);
        return this.helperOffset.set(this.transformX(x, y, z), this.transformY(x, y, z), this.transformZ(x, y, z));
    }

    protected abstract Hitbox headOrRoot();

    @Override
    public final Matrix4d helperColliderTransform() {
        return this.helperColliderTransform;
    }

    @Override
    public final Vec3d helperOffset() {
        return this.helperOffset;
    }

    @Override
    public final Matrix4d helperTransform() {
        return this.helperTransform;
    }

    @Override
    public final void init(T entity, float partialTicks) {
        this.transform.setIdentity();
        this.setPivot(0, 0, 0);
        this.scaleX = 1;
        this.scaleY = 1;
        this.scaleZ = 1;
        this.childInit(entity, partialTicks);
        this.cachedRenderOffset.set(this.renderOffset(entity, partialTicks));
    }

    protected abstract double relativeHeadOrRootX();

    protected abstract double relativeHeadOrRootY();

    protected abstract double relativeHeadOrRootZ();

    @Override
    public boolean riding() {
        return this.riding;
    }

    @Override
    public final void rotateXHR(float xRot) {
        this.transform.rotateX(xRot);
    }

    @Override
    public final void rotateYHR(float yRot) {
        this.transform.rotateY(yRot);
    }

    @Override
    public final void rotateZHR(float zRot) {
        this.transform.rotateZ(zRot);
    }

    @Override
    public final void scaleHR(float scaleX, float scaleY, float scaleZ) {
        this.scaleX *= scaleX;
        this.scaleY *= scaleY;
        this.scaleZ *= scaleZ;
    }

    @Override
    public final float scaleX() {
        return this.scaleX;
    }

    @Override
    public final float scaleY() {
        return this.scaleY;
    }

    @Override
    public final float scaleZ() {
        return this.scaleZ;
    }

    @Override
    public void setAttackTime(float attackTime) {
        this.attackTime = attackTime;
    }

    protected void setPivot(float x, float y, float z, float scale) {
        this.setPivot(x * scale, y * scale, z * scale);
    }

    protected void setPivot(float x, float y, float z) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotZ = z;
    }

    @Override
    public void setRiding(boolean riding) {
        this.riding = riding;
    }

    @Override
    public void setYoung(boolean young) {
        this.young = young;
    }

    public final Vec3d transform(Vec3d vec) {
        return this.transform.transform(vec);
    }

    @Override
    public final void transformParent(Matrix4d matrix) {
        //Do nothing
    }

    @Override
    public final double transformX(double x, double y, double z) {
        return this.transform.transformX(x, y, z) + this.cachedRenderOffset.x;
    }

    @Override
    public final double transformY(double x, double y, double z) {
        return this.transform.transformY(x, y, z) + this.cachedRenderOffset.y;
    }

    @Override
    public final double transformZ(double x, double y, double z) {
        return this.transform.transformZ(x, y, z) + this.cachedRenderOffset.z;
    }

    @Override
    public final void translateHR(float x, float y, float z) {
        this.transform.translate(x * this.scaleX, y * this.scaleY, z * this.scaleZ);
    }

    @CanIgnoreReturnValue
    public final Vec3d untransform(Vec3d vec) {
        return this.transform.untransform(vec);
    }

    public final double untransformX(double x, double y, double z) {
        return this.transform.untransformX(x, y, z);
    }

    public final double untransformY(double x, double y, double z) {
        return this.transform.untransformY(x, y, z);
    }

    public final double untransformZ(double x, double y, double z) {
        return this.transform.untransformZ(x, y, z);
    }

    @Override
    public boolean young() {
        return this.young;
    }
}
