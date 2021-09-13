package tgw.evolution.util.hitbox;

import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.vector.Vector3d;
import tgw.evolution.util.MathHelper;

public class HitboxCreeper extends HitboxEntity<CreeperEntity> {

    public static final Vector3d NECK_STANDING = new Vector3d(0, 18 / 16.0, 0);
    protected final Hitbox body = this.addBox(BodyPart.CHEST, aabb(-4, -12, -2, 4, 0, 2));
    protected final Hitbox head = this.addBox(BodyPart.HEAD, HitboxLib.BIPED_HEAD);
    protected final Hitbox legFL = this.addBox(BodyPart.FRONT_LEFT_LEG, HitboxLib.CREEPER_LEG);
    protected final Hitbox legFR = this.addBox(BodyPart.FRONT_RIGHT_LEG, HitboxLib.CREEPER_LEG);
    protected final Hitbox legRL = this.addBox(BodyPart.REAR_LEFT_LEG, HitboxLib.CREEPER_LEG);
    protected final Hitbox legRR = this.addBox(BodyPart.REAR_RIGHT_LEG, HitboxLib.CREEPER_LEG);

    protected float limbSwing;
    protected float limbSwingAmount;

    @Override
    public void init(CreeperEntity entity, float partialTicks) {
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
