package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.items.ISpecialAttack;
import tgw.evolution.util.math.MathHelper;

public class HitboxCreeper extends HitboxEntity<Creeper> {

    public static final Vec3 NECK_STANDING = new Vec3(0, 18 / 16.0, 0);
    protected final Hitbox body = this.addBox(HitboxType.CHEST, aabb(-4, -12, -2, 4, 0, 2));
    protected final Hitbox head = this.addBox(HitboxType.HEAD, HitboxLib.BIPED_HEAD);
    protected final Hitbox legFL = this.addBox(HitboxType.FRONT_LEFT_LEG, HitboxLib.CREEPER_LEG);
    protected final Hitbox legFR = this.addBox(HitboxType.FRONT_RIGHT_LEG, HitboxLib.CREEPER_LEG);
    protected final Hitbox legRL = this.addBox(HitboxType.REAR_LEFT_LEG, HitboxLib.CREEPER_LEG);
    protected final Hitbox legRR = this.addBox(HitboxType.REAR_RIGHT_LEG, HitboxLib.CREEPER_LEG);
    protected float limbSwing;
    protected float limbSwingAmount;

    public HitboxCreeper() {
        this.finish();
    }

    @Override
    protected void childFinish() {
    }

    @Override
    public Hitbox getEquipmentFor(ISpecialAttack.IAttackType type, HumanoidArm arm) {
        throw new IllegalStateException("Creepers do not have arms!");
    }

    @Override
    public void init(Creeper entity, float partialTicks) {
        this.reset();
        this.rotationYaw = -MathHelper.getEntityBodyYaw(entity, partialTicks);
        this.rotationPitch = -entity.getViewXRot(partialTicks);
        this.limbSwing = MathHelper.getLimbSwing(entity, partialTicks);
        this.limbSwingAmount = MathHelper.getLimbSwingAmount(entity, partialTicks);
        //Main
        this.rotationY = MathHelper.degToRad(this.rotationYaw);
        //Head
        this.head.pivotY = 18 / 16.0f;
        this.head.rotationX = MathHelper.degToRad(this.rotationPitch);
        this.head.rotationY = MathHelper.degToRad(-entity.getViewYRot(partialTicks) - this.rotationYaw);
        //Body
        this.body.pivotY = 18 / 16.0f;
        //LegFL
        this.legFL.setPivot(2, 6, 4, 1 / 16.0f);
        this.legFL.rotationX = MathHelper.cos(this.limbSwing * 0.666_2F + MathHelper.PI) * 1.4F * this.limbSwingAmount;
        //LegFR
        this.legFR.setPivot(-2, 6, 4, 1 / 16.0f);
        this.legFR.rotationX = MathHelper.cos(this.limbSwing * 0.666_2F) * 1.4F * this.limbSwingAmount;
        //LegRL
        this.legRL.setPivot(2, 6, -4, 1 / 16.0f);
        this.legRL.rotationX = this.legFR.rotationX;
        //LegRR
        this.legRR.setPivot(-2, 6, -4, 1 / 16.0f);
        this.legRR.rotationX = this.legFL.rotationX;
    }
}
