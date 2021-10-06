package tgw.evolution.util.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import tgw.evolution.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class HitboxEntity<T extends Entity> {

    private final List<Hitbox> boxes;
    protected float ageInTicks;
    protected float pivotX;
    protected float pivotY;
    protected float pivotZ;
    protected float rotationPitch;
    protected float rotationX;
    protected float rotationY;
    protected float rotationYaw;
    protected float rotationZ;

    public HitboxEntity() {
        this.boxes = new ArrayList<>();
    }

    public static AxisAlignedBB aabb(double x0, double y0, double z0, double x1, double y1, double z1) {
        return new AxisAlignedBB(x0 / 16, y0 / 16, z0 / 16, x1 / 16, y1 / 16, z1 / 16);
    }

    public static AxisAlignedBB aabb(double x0, double y0, double z0, double x1, double y1, double z1, double scale) {
        return aabb(x0 * scale, y0 * scale, z0 * scale, x1 * scale, y1 * scale, z1 * scale);
    }

    public static boolean shouldPoseArm(LivingEntity entity, HandSide side) {
        if (entity.getMainArm() == side) {
            return entity.getUsedItemHand() == Hand.MAIN_HAND;
        }
        return entity.getUsedItemHand() == Hand.OFF_HAND;
    }

    public static Vector3d v(double x, double y, double z) {
        return new Vector3d(x / 16, y / 16, z / 16);
    }

    public static Vector3d v(double x, double y, double z, double scale) {
        return v(x * scale, y * scale, z * scale);
    }

    protected Hitbox addBox(BodyPart part, AxisAlignedBB aabb) {
        Hitbox box = new Hitbox(part, aabb, this);
        this.boxes.add(box);
        return box;
    }

    public void animateCrossbowCharge(IHitbox rightArm, IHitbox leftArm, LivingEntity entity, boolean rightHanded) {
        IHitbox mainArm = rightHanded ? rightArm : leftArm;
        IHitbox offArm = rightHanded ? leftArm : rightArm;
        mainArm.setRotationY(rightHanded ? -0.8F : 0.8F);
        mainArm.setRotationX(-0.970_796_35F);
        offArm.setRotationX(mainArm.getRotationX());
        float f = CrossbowItem.getChargeDuration(entity.getUseItem());
        float f1 = MathHelper.clamp(entity.getTicksUsingItem(), 0.0F, f);
        float f2 = f1 / f;
        offArm.setRotationY(MathHelper.lerp(f2, 0.4F, 0.85F) * (rightHanded ? 1 : -1));
        offArm.setRotationX(MathHelper.lerp(f2, offArm.getRotationX(), -MathHelper.PI_OVER_2));
    }

    public void animateCrossbowHold(IHitbox rightArm, IHitbox leftArm, IHitbox head, boolean rightHanded) {
        IHitbox mainArm = rightHanded ? rightArm : leftArm;
        IHitbox offArm = rightHanded ? leftArm : rightArm;
        mainArm.setRotationY((rightHanded ? -0.3F : 0.3F) + head.getRotationY());
        offArm.setRotationY((rightHanded ? 0.6F : -0.6F) + head.getRotationY());
        mainArm.setRotationX(-MathHelper.PI_OVER_2 + head.getRotationX() + 0.1F);
        offArm.setRotationX(-1.5F + head.getRotationX());
    }

    public void bobArms(IHitbox rightArm, IHitbox leftArm, float ageInTicks) {
        rightArm.addRotationZ(MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        leftArm.addRotationZ(-MathHelper.cos(ageInTicks * 0.09F) * 0.05F - 0.05F);
        rightArm.addRotationX(-MathHelper.sin(ageInTicks * 0.067F) * 0.05F);
        leftArm.addRotationX(MathHelper.sin(ageInTicks * 0.067F) * 0.05F);
    }

    public void doOffset(double[] answer) {
        answer[0] += this.pivotX;
        answer[1] += this.pivotY;
        answer[2] += this.pivotZ;
    }

    public List<Hitbox> getBoxes() {
        return this.boxes;
    }

    public Vector3d getOffset() {
        return new Vector3d(this.pivotX, this.pivotY, this.pivotZ);
    }

    public Matrix3d getTransform() {
        Matrix3d xRot = new Matrix3d().asXRotation(this.rotationX);
        Matrix3d yRot = new Matrix3d().asYRotation(this.rotationY);
        Matrix3d zRot = new Matrix3d().asZRotation(this.rotationZ);
        return xRot.multiply(yRot).multiply(zRot);
    }

    public abstract void init(T entity, float partialTicks);

    protected void reset() {
        this.setPivot(0, 0, 0);
        this.setRotation(0, 0, 0);
        for (Hitbox box : this.boxes) {
            box.reset();
        }
    }

    protected void setPivot(float x, float y, float z, float scale) {
        this.setPivot(x * scale, y * scale, z * scale);
    }

    protected void setPivot(float x, float y, float z) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotZ = z;
    }

    protected void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
    }
}
