package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.entities.EntityBull;

@OnlyIn(Dist.CLIENT)
public class ModelBull extends EntityModel<EntityBull> {

    public final RendererModel body;
    //    public RendererModel breasts;
    //    public RendererModel nippleRR;
    //    public RendererModel nippleRL;
    //    public RendererModel nippleFL;
    //    public RendererModel nippleFR;
    public final RendererModel head;
    public final RendererModel neck;
    public final RendererModel snout;
    public final RendererModel hornRight;
    public final RendererModel hornLeft;
    public final RendererModel earLeft;
    public final RendererModel earRight;
    public final RendererModel tailX;
    public final RendererModel tailZ;
    public final RendererModel legRearRight;
    public final RendererModel legRRLower;
    public final RendererModel hoofRR;
    public final RendererModel legRearLeft;
    public final RendererModel legRLLower;
    public final RendererModel hoofRL;
    public final RendererModel legFrontLeft;
    public final RendererModel legFLLower;
    public final RendererModel hoofFL;
    public final RendererModel legFrontRight;
    public final RendererModel legFRLower;
    public final RendererModel hoofFR;
    private float headRotationAngleX;
    private float tailRotationAngleX;
    private float tailRotationAngleZ;

    public ModelBull() {
        this.textureWidth = 64;
        this.textureHeight = 64;
        //head
        this.head = new RendererModel(this, 40, 22);
        this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.head.addBox(-3.0F, -6.0F, -8.0F, 6, 7, 6, 0.0F);
        ModelBull.setRotateAngle(this.head, -5.585053606381854F, 0.0F, 0.0F);
        this.snout = new RendererModel(this, 0, 46);
        this.snout.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.snout.addBox(-2.0F, -5.5F, -10.5F, 4, 4, 4, 0.0F);
        ModelBull.setRotateAngle(this.snout, -5.32325421858F, 0.0F, 0.0F);
        this.neck = new RendererModel(this, 0, 54);
        this.neck.setRotationPoint(0.0F, 6F, -6F);
        this.neck.addBox(-2.5F, -1.5F, -4F, 5, 5, 5, 0.0F);
        ModelBull.setRotateAngle(this.neck, 5.585053606381854F, 0.0F, 0.0F);
        this.earRight = new RendererModel(this, 56, 17);
        this.earRight.setRotationPoint(0F, 0F, 0F);
        this.earRight.addBox(-6.2F, -5.6F, -2.3F, 3, 2, 1, 0.0F);
        ModelBull.setRotateAngle(this.earRight, -5.23598775598F, 0.0F, 0.17453292519943295F);
        this.earLeft = new RendererModel(this, 47, 17);
        this.earLeft.setRotationPoint(0F, 0F, 0F);
        this.earLeft.addBox(3.2F, -5.6F, -2.3F, 3, 2, 1, 0.0F);
        ModelBull.setRotateAngle(this.earLeft, -5.23598775598F, 0.0F, 6.1086523819801535F);
        this.hornRight = new RendererModel(this, 22, 23);
        this.hornRight.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.hornRight.addBox(-3F, -8F, -2.5F, 1, 3, 1, 0.0F);
        ModelBull.setRotateAngle(this.hornRight, -5.585053606381854F, 0.0F, 5.93411945678072F);
        this.hornLeft = new RendererModel(this, 22, 19);
        this.hornLeft.setRotationPoint(0F, 0F, 0F);
        this.hornLeft.addBox(2F, -8.0F, -2.5F, 1, 3, 1, 0.0F);
        ModelBull.setRotateAngle(this.hornLeft, -5.585053606381854F, 0.0F, 0.3490658503988659F);
        this.neck.addChild(this.head);
        this.neck.addChild(this.snout);
        this.neck.addChild(this.earLeft);
        this.neck.addChild(this.earRight);
        this.neck.addChild(this.hornLeft);
        this.neck.addChild(this.hornRight);
        //body
        this.body = new RendererModel(this, 24, 36);
        this.body.setRotationPoint(1.0F, 5.0F, 2.0F);
        this.body.addBox(-6F, -10F, -7F, 10, 18, 10, 0.0F);
        ModelBull.setRotateAngle(this.body, 1.5707963267948966F, 0.0F, 0.0F);
        //        this.breasts = new RendererModel(this, 46, 0);
        //        this.breasts.setRotationPoint(0F, 0F, 0F);
        //        this.breasts.addBox(-4F, 2F, -10F, 6, 6, 3, 0.0F);
        //        this.nippleRL = new RendererModel(this, 56, 42);
        //        this.nippleRL.setRotationPoint(0F, 0F, 0F);
        //        this.nippleRL.addBox(-0.5F, 9.8F, 6F, 1, 2, 1, 0.0F);
        //        this.setRotateAngle(nippleRL, -1.5707963267948966F, 0F, 0F);
        //        this.nippleRR = new RendererModel(this, 56, 38);
        //        this.nippleRR.setRotationPoint(0F, 0F, 0F);
        //        this.nippleRR.addBox(-2.5F, 9.8F, 6F, 1, 2, 1, 0.0F);
        //        this.setRotateAngle(nippleRR, -1.5707963267948966F, 0F, 0F);
        //        this.nippleFR = new RendererModel(this, 60, 42);
        //        this.nippleFR.setRotationPoint(0F, 0F, 0F);
        //        this.nippleFR.addBox(-2.5F, 9.8F, 3F, 1, 2, 1, 0.0F);
        //        this.setRotateAngle(nippleFR, -1.5707963267948966F, 0F, 0F);
        //        this.nippleFL = new RendererModel(this, 60, 38);
        //        this.nippleFL.setRotationPoint(0F, 0F, 0F);
        //        this.nippleFL.addBox(-0.5F, 9.8F, 3F, 1, 2, 1, 0.0F);
        //        this.setRotateAngle(nippleFL, -1.5707963267948966F, 0F, 0F);
        //        this.body.addChild(breasts);
        //        this.body.addChild(nippleFL);
        //        this.body.addChild(nippleFR);
        //        this.body.addChild(nippleRL);
        //        this.body.addChild(nippleRR);
        //legRearRight
        this.legRearRight = new RendererModel(this, 26, 0);
        this.legRearRight.setRotationPoint(-3.75F, 8.0F, 7.75F);
        this.legRearRight.addBox(-2.0F, 0.01F, -2.5F, 4, 7, 5, 0.0F);
        this.legRRLower = new RendererModel(this, 0, 5);
        this.legRRLower.setRotationPoint(0F, 0F, 0F);
        this.legRRLower.addBox(-1.5F, 7F, -2F, 3, 7, 4, 0.0F);
        this.hoofRR = new RendererModel(this, 46, 9);
        this.hoofRR.setRotationPoint(0F, 0F, 0F);
        this.hoofRR.addBox(-2F, 14F, -2.5F, 4, 2, 5, 0.0F);
        this.legRearRight.addChild(this.legRRLower);
        this.legRearRight.addChild(this.hoofRR);
        //legRearLeft
        this.legRearLeft = new RendererModel(this, 26, 12);
        this.legRearLeft.setRotationPoint(3.75F, 8.0F, 7.75F);
        this.legRearLeft.addBox(-2.0F, 0.01F, -2.5F, 4, 7, 5, 0.0F);
        this.legRLLower = new RendererModel(this, 0, 35);
        this.legRLLower.setRotationPoint(0F, 0F, 0F);
        this.legRLLower.addBox(-1.5F, 7F, -2F, 3, 7, 4, 0.0F);
        this.hoofRL = new RendererModel(this, 46, 9);
        this.hoofRL.setRotationPoint(0F, 0F, 0F);
        this.hoofRL.addBox(-2F, 13.99F, -2.5F, 4, 2, 5, 0.0F);
        this.legRearLeft.addChild(this.legRLLower);
        this.legRearLeft.addChild(this.hoofRL);
        //legFrontRight
        this.legFrontRight = new RendererModel(this, 22, 24);
        this.legFrontRight.setRotationPoint(-3.25F, 9.0F, -5.75F);
        this.legFrontRight.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
        this.legFRLower = new RendererModel(this, 0, 16);
        this.legFRLower.setRotationPoint(0F, 0F, 0F);
        this.legFRLower.addBox(-1.5F, 6F, -1.5F, 3, 7, 3, 0.0F);
        this.hoofFR = new RendererModel(this, 10, 0);
        this.hoofFR.setRotationPoint(0F, 0F, 0F);
        this.hoofFR.addBox(-2F, 13F, -2F, 4, 2, 4, 0.0F);
        this.legFrontRight.addChild(this.legFRLower);
        this.legFrontRight.addChild(this.hoofFR);
        //legFrontLeft
        this.legFrontLeft = new RendererModel(this, 16, 35);
        this.legFrontLeft.setRotationPoint(3.25F, 9.0F, -5.75F);
        this.legFrontLeft.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
        this.legFLLower = new RendererModel(this, 0, 26);
        this.legFLLower.setRotationPoint(0F, 0F, 0F);
        this.legFLLower.addBox(-1.5F, 6F, -1.5F, 3, 7, 3, 0.0F);
        this.hoofFL = new RendererModel(this, 10, 0);
        this.hoofFL.setRotationPoint(0F, 0F, 0F);
        this.hoofFL.addBox(-2F, 13F, -2F, 4, 2, 4, 0.0F);
        this.legFrontLeft.addChild(this.legFLLower);
        this.legFrontLeft.addChild(this.hoofFL);
        //tail
        this.tailX = new RendererModel(this, 17, 47);
        this.tailX.setRotationPoint(-0.5F, 2.4F, 9.5F);
        this.tailX.addBox(0F, 0F, 0F, 2, 11, 0, 0.0F);
        ModelBull.setRotateAngle(this.tailX, 0.2617993877991494F, 0.0F, 0.0F);
        this.tailZ = new RendererModel(this, 16, 21);
        this.tailZ.setRotationPoint(0F, 0F, 0F);
        this.tailZ.addBox(0.5F, 0.4F, -1.4F, 0, 11, 2, 0.0F);
        this.tailX.addChild(this.tailZ);
    }

    @Override
    public void render(EntityBull entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        if (this.isChild) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 8.0F * scale, 4.0F * scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            GlStateManager.translatef(0.0F, 24.0F * scale, 0.0F);
            this.neck.render(scale);
            this.body.render(scale);
            this.legFrontLeft.render(scale);
            this.legFrontRight.render(scale);
            this.legRearLeft.render(scale);
            this.legRearRight.render(scale);
            this.tailX.render(scale);
            GlStateManager.popMatrix();
        }
        else {
            this.neck.render(scale);
            this.body.render(scale);
            this.legFrontLeft.render(scale);
            this.legFrontRight.render(scale);
            this.legRearLeft.render(scale);
            this.legRearRight.render(scale);
            this.tailX.render(scale);
        }
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public static void setRotateAngle(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(EntityBull entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (entityIn.isDead()) {
            this.neck.rotateAngleX = 5.585053606381854F;
            this.neck.rotateAngleY = 0F;
            this.legFrontRight.rotateAngleX = 0F;
            this.legFrontLeft.rotateAngleX = 0F;
            this.legRearRight.rotateAngleX = 0F;
            this.legRearLeft.rotateAngleX = 0F;
            this.neck.rotateAngleX = 5.585053606381854F;
            this.tailX.rotateAngleX = 0.2617993877991494F;
            this.tailX.rotateAngleZ = 0F;
            return;
        }
        this.neck.rotateAngleX = headPitch * ((float) Math.PI / 180F) + 5.585053606381854F;
        this.neck.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F);
        this.legFrontRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legFrontLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.legRearRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.legRearLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.neck.rotateAngleX = this.headRotationAngleX + 5.585053606381854F;
        this.tailX.rotateAngleX = this.tailRotationAngleX + 0.2617993877991494F;
        this.tailX.rotateAngleZ = this.tailRotationAngleZ;
    }

    @Override
    public void setLivingAnimations(EntityBull entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        if (entityIn.isDead()) {
            this.neck.rotationPointY = 6F;
            return;
        }
        this.neck.rotationPointY = 6F + entityIn.getHeadRotationPointY(partialTick) * 9F;
        this.headRotationAngleX = entityIn.getHeadRotationAngleX(partialTick);
        this.tailRotationAngleX = MathHelper.sin(entityIn.tailIncX()) / 2F;
        this.tailRotationAngleZ = MathHelper.sin(entityIn.tailIncZ()) / 2F;
    }

}