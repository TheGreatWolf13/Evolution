//package tgw.evolution.client.models.entities;
//
//import net.minecraft.client.renderer.entity.model.EntityModel;
//import net.minecraft.client.renderer.entity.model.RendererModel;
//import tgw.evolution.entities.EntityShadowHound;
//import tgw.evolution.util.math.MathHelper;
//
//public class ModelShadowHound extends EntityModel<EntityShadowHound> {
//
//    private final RendererModel body;
//    private final RendererModel head;
//    private final RendererModel legLeft;
//    private final RendererModel legRight;
//
//    public ModelShadowHound() {
//        this.textureWidth = 50;
//        this.textureHeight = 36;
//
//        this.body = new RendererModel(this, 0, 14);
//        this.body.setRotationPoint(-0.5F, 14.5F, -4.5F);
//        MathHelper.setRotationAngle(this.body, -0.6981F, 0.0F, 0.0F);
//        this.body.addBox(-3.9F, -1.1585F, 0.5827F, 8, 5, 17, 0.0F);
//
//        this.head = new RendererModel(this, 0, 0);
//        this.head.setRotationPoint(-0.5F, 15.5F, -8.8F);
//        MathHelper.setRotationAngle(this.head, 0.6807F, 0.0F, 0.0F);
//        this.head.addBox(-3.5F, -3.5724F, -3.0041F, 7, 7, 7, 0.0F);
//
//        this.legLeft = new RendererModel(this, 0, 14);
//        this.legLeft.setRotationPoint(5.5F, 0.5F, 4.5F);
//        MathHelper.setRotationAngle(this.legLeft, 0.6981F, 0.0F, 0.0F);
//        this.body.addChild(this.legLeft);
//        this.legLeft.addBox(-1.3F, -4.8F, -1.6122F, 2, 11, 4, 0.0F);
//
//        this.legRight = new RendererModel(this, 0, 14);
//        this.legRight.setRotationPoint(-5.0F, 1.1F, 4.5F);
//        MathHelper.setRotationAngle(this.legRight, 0.6981F, 0.0F, 0.0F);
//        this.body.addChild(this.legRight);
//        this.legRight.addBox(-1.0F, -5.3F, -1.71F, 2, 11, 4, 0.0F);
//    }
//
//    @Override
//    public void render(EntityShadowHound entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
//    float scale) {
//        this.body.render(scale);
//        this.head.render(scale);
//    }
//
//    @Override
//    public void setRotationAngles(EntityShadowHound entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float
//    headPitch, float scaleFactor) {
//        if (entityIn.isDead()) {
//            this.head.rotateAngleX = 0.6807F;
//            this.head.rotateAngleY = 0f;
//            this.legRight.rotateAngleX = 0.6981F;
//            this.legLeft.rotateAngleX = 0.6981F;
//            return;
//        }
//        this.head.rotateAngleX = MathHelper.degToRad(headPitch) + 0.6807F;
//        this.head.rotateAngleY = MathHelper.degToRad(netHeadYaw);
//        this.legRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
//        this.legLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + MathHelper.PI) * 1.4F * limbSwingAmount;
//    }
//}