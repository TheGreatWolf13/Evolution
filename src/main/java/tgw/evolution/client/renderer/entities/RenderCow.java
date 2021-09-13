package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.entities.ModelCow;
import tgw.evolution.entities.EntityCow;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class RenderCow extends MobRenderer<EntityCow, ModelCow> {

    private static final ModelCow MODEL = new ModelCow();
    private static final ResourceLocation DEFAULT = Evolution.getResource("textures/entity/cattle/cow.png");
    private static final ResourceLocation SLEEPING = Evolution.getResource("textures/entity/cattle/cow_sleeping.png");
    private static final ResourceLocation DEAD = Evolution.getResource("textures/entity/cattle/cow_dead.png");
    private static final ResourceLocation SKELETON = Evolution.getResource("textures/entity/cattle/cow_skeleton.png");

    public RenderCow(EntityRendererManager manager) {
        super(manager, MODEL, 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCow entity) {
        if (entity.isDead()) {
            if (entity.isSkeleton()) {
                return SKELETON;
            }
            return DEAD;
        }
        if (entity.isSleeping()) {
            return SLEEPING;
        }
        return DEFAULT;
    }

    @Override
    protected void setupRotations(EntityCow cow, MatrixStack matrices, float ageInTicks, float rotationYaw, float partialTicks) {
        matrices.mulPose(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));
        if (cow.isDead()) {
            float f = (MathHelper.clampMax(cow.getDeathTime(), 20) + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            matrices.mulPose(Vector3f.ZP.rotationDegrees(f * this.getFlipDegrees(cow)));
            matrices.translate(0.32, -0.75, 0);
        }
        else if (cow.isSleeping()) {
            matrices.translate(0, -0.7, 0);
        }
    }
}