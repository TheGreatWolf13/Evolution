package tgw.evolution.util.hitbox;

import com.mojang.math.Vector3d;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.items.ISpecialAttack;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.math.MathHelper;

import java.util.List;

public abstract class HitboxEntity<T extends Entity> {

    private final RList<Hitbox> boxes;
    private final RList<Hitbox> equipment;
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
        this.boxes = new RArrayList<>();
        this.equipment = new RArrayList<>();
    }

    public static AABB aabb(double x0, double y0, double z0, double x1, double y1, double z1) {
        return new AABB(x0 / 16, y0 / 16, z0 / 16, x1 / 16, y1 / 16, z1 / 16);
    }

    public static AABB aabb(double x0, double y0, double z0, double x1, double y1, double z1, double scale) {
        return aabb(x0 * scale, y0 * scale, z0 * scale, x1 * scale, y1 * scale, z1 * scale);
    }

    protected static HumanoidArm getAttackArm(LivingEntity entity) {
        HumanoidArm handside = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? handside : handside.getOpposite();
    }

    protected static float rotLerpRad(float partialTick, float old, float now) {
        float f = (now - old) % MathHelper.TAU;
        if (f < -MathHelper.PI) {
            f += MathHelper.TAU;
        }
        if (f >= MathHelper.PI) {
            f -= MathHelper.TAU;
        }
        return old + partialTick * f;
    }

    public static boolean shouldPoseArm(LivingEntity entity, HumanoidArm side) {
        if (entity.getMainArm() == side) {
            return entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
        }
        return entity.getUsedItemHand() == InteractionHand.OFF_HAND;
    }

    public static Vector3d v(double x, double y, double z, double scale) {
        return v(x * scale, y * scale, z * scale);
    }

    public static Vector3d v(double x, double y, double z) {
        return new Vector3d(x / 16, y / 16, z / 16);
    }

    protected final Hitbox addBox(HitboxType part, AABB aabb) {
        //Verify that every Hitbox has a unique HitboxType
        //This code is not the fastest, but it is only run when creating the HitboxEntity instance.
        for (Hitbox box : this.boxes) {
            if (box.getPart() == part) {
                throw new IllegalStateException("Duplicate HitboxType: " + part);
            }
        }
        Hitbox box = new Hitbox(part, aabb, this);
        this.boxes.add(box);
        return box;
    }

    protected final Hitbox addEquip(HitboxType part, AABB aabb) {
        //Verify that every Hitbox has a unique HitboxType
        //This code is not the fastest, but it is only run when creating the HitboxEntity instance.
        for (Hitbox box : this.equipment) {
            if (box.getPart() == part) {
                throw new IllegalStateException("Duplicate HitboxType: " + part);
            }
        }
        Hitbox box = new Hitbox(part, aabb, this);
        this.equipment.add(box);
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
        offArm.setRotationY(Mth.lerp(f2, 0.4F, 0.85F) * (rightHanded ? 1 : -1));
        offArm.setRotationX(Mth.lerp(f2, offArm.getRotationX(), -MathHelper.PI_OVER_2));
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
        this.bobModelPart(rightArm, ageInTicks, 1.0F);
        this.bobModelPart(leftArm, ageInTicks, -1.0F);
    }

    protected void bobModelPart(IHitbox box, float ageInTicks, float mul) {
        box.addRotationZ(mul * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F));
        box.addRotationX(-mul * Mth.sin(ageInTicks * 0.067F) * 0.05F);
    }

    protected abstract void childFinish();

    public void doOffset(double[] answer) {
        answer[0] += this.pivotX;
        answer[1] += this.pivotY;
        answer[2] += this.pivotZ;
    }

    protected final void finish() {
        this.boxes.trimCollection();
        this.equipment.trimCollection();
        this.childFinish();
    }

    public List<Hitbox> getBoxes() {
        return this.boxes;
    }

    public List<Hitbox> getEquipment() {
        return this.equipment;
    }

    public abstract Hitbox getEquipmentFor(ISpecialAttack.IAttackType type, HumanoidArm arm);

    public Vec3 getOffset() {
        return new Vec3(this.pivotX, this.pivotY, this.pivotZ);
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
        for (Hitbox box : this.equipment) {
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
