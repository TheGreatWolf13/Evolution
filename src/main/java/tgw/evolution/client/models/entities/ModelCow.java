//package tgw.evolution.client.models.entities;
//
//import net.minecraft.client.model.AgeableListModel;
//import net.minecraft.client.model.geom.ModelPart;
//import tgw.evolution.entities.EntityCow;
//
//import java.util.Collections;
//
//public class ModelCow extends AgeableListModel<EntityCow> {
//
//    //    public final ModelPart body;
////    public final ModelPart breasts;
////    public final ModelPart earLeft;
////    public final ModelPart earRight;
////    public final ModelPart head;
////    public final ModelPart hoofFL;
////    public final ModelPart hoofFR;
////    public final ModelPart hoofRL;
////    public final ModelPart hoofRR;
////    public final ModelPart legFLLower;
////    public final ModelPart legFRLower;
////    public final ModelPart legFrontLeft;
////    public final ModelPart legFrontRight;
////    public final ModelPart legRLLower;
////    public final ModelPart legRRLower;
////    public final ModelPart legRearLeft;
////    public final ModelPart legRearRight;
////    public final ModelPart neck;
////    public final ModelPart nippleFL;
////    public final ModelPart nippleFR;
////    public final ModelPart nippleRL;
////    public final ModelPart nippleRR;
////    public final ModelPart snout;
////    public final ModelPart tailX;
////    public final ModelPart tailZ;
////    private final Iterable<ModelPart> bodyParts;
////    private final Iterable<ModelPart> headParts;
//    private float headRotationAngleX;
//    private float tailRotationAngleX;
//    private float tailRotationAngleZ;
//
//    public ModelCow() {
////        this.texWidth = 64;
////        this.texHeight = 64;
////        //head
////        this.head = new ModelPart(this, 40, 22);
////        this.head.setPos(0.0F, 0.0F, 0.0F);
////        this.head.addBox(-3.0f, -6.0f, -8.0f, 6, 7, 6, 0.0F);
////        MathHelper.setRotationAngle(this.head, -5.585_053_4f, 0, 0);
////        this.snout = new ModelPart(this, 0, 46);
////        this.snout.setPos(0.0F, 0.0F, 0.0F);
////        this.snout.addBox(-2.0f, -5.5f, -10.5f, 4, 4, 4, 0.0F);
////        MathHelper.setRotationAngle(this.snout, -5.323_254f, 0, 0);
////        this.neck = new ModelPart(this, 0, 54);
////        this.neck.setPos(0.0F, 6.0F, -6.0f);
////        this.neck.addBox(-2.5f, -1.5f, -4.0f, 5, 5, 5, 0.0F);
////        MathHelper.setRotationAngle(this.neck, 5.585_053_606_381_854F, 0, 0);
////        this.earRight = new ModelPart(this, 56, 17);
////        this.earRight.setPos(0.0F, 0.0F, 0.0F);
////        this.earRight.addBox(-6.2f, -5.6f, -2.3f, 3, 2, 1, 0.0F);
////        MathHelper.setRotationAngle(this.earRight, -5.235_987_7f, 0, 0.174_532_925_199_432_95F);
////        this.earLeft = new ModelPart(this, 47, 17);
////        this.earLeft.setPos(0.0F, 0.0F, 0.0F);
////        this.earLeft.addBox(3.2F, -5.6f, -2.3f, 3, 2, 1, 0.0F);
////        MathHelper.setRotationAngle(this.earLeft, -5.235_987_7f, 0, 6.108_652_381_980_153_5F);
////        this.neck.addChild(this.head);
////        this.neck.addChild(this.snout);
////        this.neck.addChild(this.earLeft);
////        this.neck.addChild(this.earRight);
////        this.headParts = ImmutableList.of(this.neck);
////        //body
////        this.body = new ModelPart(this, 24, 36);
////        this.body.setPos(1.0F, 5.0F, 2.0F);
////        this.body.addBox(-6.0F, -10.0F, -7.0F, 10, 18, 10, 0.0F);
////        MathHelper.setRotationAngle(this.body, MathHelper.PI_OVER_2, 0, 0);
////        this.breasts = new ModelPart(this, 46, 0);
////        this.breasts.setPos(0.0F, 0.0F, 0.0F);
////        this.breasts.addBox(-4.0f, 2.0F, -10.0F, 6, 6, 3, 0.0F);
////        this.nippleRL = new ModelPart(this, 56, 42);
////        this.nippleRL.setPos(0.0F, 0.0F, 0.0F);
////        this.nippleRL.addBox(-0.5f, 9.8F, 6.0F, 1, 2, 1, 0.0F);
////        MathHelper.setRotationAngle(this.nippleRL, -MathHelper.PI_OVER_2, 0, 0.0F);
////        this.nippleRR = new ModelPart(this, 56, 38);
////        this.nippleRR.setPos(0.0F, 0.0F, 0.0F);
////        this.nippleRR.addBox(-2.5f, 9.8F, 6.0F, 1, 2, 1, 0.0F);
////        MathHelper.setRotationAngle(this.nippleRR, -MathHelper.PI_OVER_2, 0, 0.0F);
////        this.nippleFR = new ModelPart(this, 60, 42);
////        this.nippleFR.setPos(0.0F, 0.0F, 0.0F);
////        this.nippleFR.addBox(-2.5f, 9.8F, 3.0F, 1, 2, 1, 0.0F);
////        MathHelper.setRotationAngle(this.nippleFR, -MathHelper.PI_OVER_2, 0, 0.0F);
////        this.nippleFL = new ModelPart(this, 60, 38);
////        this.nippleFL.setPos(0.0F, 0.0F, 0.0F);
////        this.nippleFL.addBox(-0.5f, 9.8F, 3.0F, 1, 2, 1, 0.0F);
////        MathHelper.setRotationAngle(this.nippleFL, -MathHelper.PI_OVER_2, 0, 0.0F);
////        this.body.addChild(this.breasts);
////        this.body.addChild(this.nippleFL);
////        this.body.addChild(this.nippleFR);
////        this.body.addChild(this.nippleRL);
////        this.body.addChild(this.nippleRR);
////        //legRearRight
////        this.legRearRight = new ModelPart(this, 26, 0);
////        this.legRearRight.setPos(-3.75F, 8.0F, 7.75F);
////        this.legRearRight.addBox(-2.0F, 0.01F, -2.5F, 4, 7, 5, 0.0F);
////        this.legRRLower = new ModelPart(this, 0, 5);
////        this.legRRLower.setPos(0.0F, 5.0F, 0.0F);
////        this.legRRLower.addBox(-1.5F, 2.0F, -2.0F, 3, 7, 4, 0.0F);
////        this.hoofRR = new ModelPart(this, 46, 9);
////        this.hoofRR.setPos(0.0F, 0.0F, 0.0F);
////        this.hoofRR.addBox(-2.0F, 9.0F, -2.5F, 4, 2, 5, 0.0F);
////        this.legRRLower.addChild(this.hoofRR);
////        this.legRearRight.addChild(this.legRRLower);
////        //legRearLeft
////        this.legRearLeft = new ModelPart(this, 26, 12);
////        this.legRearLeft.setPos(3.75F, 8.0F, 7.75F);
////        this.legRearLeft.addBox(-2.0F, 0.01F, -2.5F, 4, 7, 5, 0.0F);
////        this.legRLLower = new ModelPart(this, 0, 35);
////        this.legRLLower.setPos(0.0F, 5.0F, 0.0F);
////        this.legRLLower.addBox(-1.5F, 2.0F, -2.0F, 3, 7, 4, 0.0F);
////        this.hoofRL = new ModelPart(this, 46, 9);
////        this.hoofRL.setPos(0.0F, 0.0F, 0.0F);
////        this.hoofRL.addBox(-2.0F, 9.0F, -2.5F, 4, 2, 5, 0.0F);
////        this.legRLLower.addChild(this.hoofRL);
////        this.legRearLeft.addChild(this.legRLLower);
////        //legFrontRight
////        this.legFrontRight = new ModelPart(this, 22, 24);
////        this.legFrontRight.setPos(-3.25F, 9.0F, -5.75F);
////        this.legFrontRight.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
////        this.legFRLower = new ModelPart(this, 0, 16);
////        this.legFRLower.setPos(0.0F, 5.0F, 0.0F);
////        this.legFRLower.addBox(-1.5F, 1.0F, -1.5F, 3, 7, 3, 0.0F);
////        this.hoofFR = new ModelPart(this, 10, 0);
////        this.hoofFR.setPos(0.0F, 0.0F, 0.0F);
////        this.hoofFR.addBox(-2.0F, 8.0F, -2.0F, 4, 2, 4, 0.0F);
////        this.legFRLower.addChild(this.hoofFR);
////        this.legFrontRight.addChild(this.legFRLower);
////        //legFrontLeft
////        this.legFrontLeft = new ModelPart(this, 16, 35);
////        this.legFrontLeft.setPos(3.25F, 9.0F, -5.75F);
////        this.legFrontLeft.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F);
////        this.legFLLower = new ModelPart(this, 0, 26);
////        this.legFLLower.setPos(0.0F, 5.0F, 0.0F);
////        this.legFLLower.addBox(-1.5F, 1.0F, -1.5F, 3, 7, 3, 0.0F);
////        this.hoofFL = new ModelPart(this, 10, 0);
////        this.hoofFL.setPos(0.0F, 0.0F, 0.0F);
////        this.hoofFL.addBox(-2.0F, 8.0F, -2.0F, 4, 2, 4, 0.0F);
////        this.legFLLower.addChild(this.hoofFL);
////        this.legFrontLeft.addChild(this.legFLLower);
////        //tail
////        this.tailX = new ModelPart(this, 17, 47);
////        this.tailX.setPos(-0.5F, 2.4F, 9.5F);
////        this.tailX.addBox(0.0F, 0.0F, 0.0F, 2, 11, 0, 0.0F);
////        MathHelper.setRotationAngle(this.tailX, 0.261_799_387_799_149_4F, 0, 0);
////        this.tailZ = new ModelPart(this, 16, 21);
////        this.tailZ.setPos(0.0F, 0.0F, 0.0F);
////        this.tailZ.addBox(0.5F, 0.4F, -1.4F, 0, 11, 2, 0.0F);
////        this.tailX.addChild(this.tailZ);
////        this.bodyParts = ImmutableList.of(this.body, this.legRearRight, this.legRearLeft, this.legFrontRight, this.legFrontLeft, this.tailX);
//    }
//
////    @Override
//
//    @Override
//    protected Iterable<ModelPart> bodyParts() {
//        return Collections.emptyList();
//    }
//
//    @Override
//    protected Iterable<ModelPart> headParts() {
//        return Collections.emptyList();
//    }
////    protected Iterable<ModelPart> bodyParts() {
////        return this.bodyParts;
////    }
////
////    @Override
////    protected Iterable<ModelPart> headParts() {
////        return this.headParts;
////    }
//
//    @Override
//    public void prepareMobModel(EntityCow cow, float limbSwing, float limbSwingAmount, float partialTick) {
////        if (cow.isDead()) {
////            this.neck.y = 6.0F;
////            return;
////        }
////        this.neck.y = 6.0F + cow.getHeadRotationPointY(partialTick) * 6.0F;
////        this.headRotationAngleX = cow.getHeadRotationAngleX(partialTick);
////        this.tailRotationAngleX = MathHelper.sin(cow.tailIncX()) / 2.0F;
////        this.tailRotationAngleZ = MathHelper.sin(cow.tailIncZ()) / 2.0F;
//    }
//
//    @Override
//    public void setupAnim(EntityCow cow, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
////        if (cow.isDead()) {
////            this.neck.xRot = 5.585_053_606_381_854F;
////            this.neck.yRot = 0.0F;
////            //TODO
//////            this.body.offsetY = 0.0F;
////            this.legFRLower.xRot = 0.0F;
////            this.legFLLower.xRot = 0.0F;
////            this.legRRLower.xRot = 0.0F;
////            this.legRLLower.xRot = 0.0F;
////            this.legFrontRight.xRot = 0.0F;
////            this.legFrontLeft.xRot = 0.0F;
////            this.legRearRight.xRot = 0.0F;
////            this.legRearLeft.xRot = 0.0F;
////            this.tailX.xRot = 0.261_799_387_799_149_4F;
////            this.tailX.zRot = 0.0F;
////            return;
////        }
////        if (cow.isSleeping()) {
////            this.neck.xRot = 5.585_053_606_381_854F + MathHelper.cos(ageInTicks * 0.027F) / 22.0F;
////            this.neck.yRot = 0.0F;
////            //TODO
//////            this.body.offsetY = MathHelper.cos(ageInTicks * 0.027F) / 22.0F;
////            this.legFrontRight.xRot = -1.309f;
////            this.legFRLower.xRot = 2.617_99F;
////            this.legFrontLeft.xRot = -1.309f;
////            this.legFLLower.xRot = 2.617_99F;
////            this.legRearRight.xRot = 1.309F;
////            this.legRRLower.xRot = -2.617_99f;
////            this.legRearLeft.xRot = 1.309F;
////            this.legRLLower.xRot = -2.617_99f;
////            return;
////        }
////        this.neck.xRot = MathHelper.degToRad(headPitch) + 5.585_053_606_381_854F;
////        this.neck.yRot = MathHelper.degToRad(netHeadYaw);
////        //TODO
//////        this.body.offsetY = 0.0F;
////        this.legFRLower.xRot = 0.0F;
////        this.legFLLower.xRot = 0.0F;
////        this.legRRLower.xRot = 0.0F;
////        this.legRLLower.xRot = 0.0F;
////        this.legFrontRight.xRot = MathHelper.cos(limbSwing * 0.666_2F) * 1.4F * limbSwingAmount;
////        this.legFrontLeft.xRot = MathHelper.cos(limbSwing * 0.666_2F + MathHelper.PI) * 1.4F * limbSwingAmount;
////        this.legRearRight.xRot = MathHelper.cos(limbSwing * 0.666_2F + MathHelper.PI) * 1.4F * limbSwingAmount;
////        this.legRearLeft.xRot = MathHelper.cos(limbSwing * 0.666_2F) * 1.4F * limbSwingAmount;
////        this.neck.xRot = this.headRotationAngleX + 5.585_053_606_381_854F;
////        this.tailX.xRot = this.tailRotationAngleX + 0.261_799_387_799_149_4F;
////        this.tailX.zRot = this.tailRotationAngleZ;
//    }
//}