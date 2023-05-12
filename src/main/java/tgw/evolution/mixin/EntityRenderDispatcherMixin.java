package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ambient.LightTextureEv;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    @Final
    private static RenderType SHADOW_RENDER_TYPE;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible
     */
    @Overwrite
    private static void renderBlockShadow(PoseStack.Pose pose,
                                          VertexConsumer buffer,
                                          LevelReader level,
                                          BlockPos pos,
                                          double x,
                                          double y,
                                          double z,
                                          float size,
                                          float weight) {
        BlockPos posBelow = pos.below();
        BlockState stateBelow = level.getBlockState(posBelow);
        if (stateBelow.getRenderShape() == RenderShape.INVISIBLE || level.getMaxLocalRawBrightness(pos) <= 3) {
            return;
        }
        if (!stateBelow.isCollisionShapeFullBlock(level, posBelow)) {
            return;
        }
        VoxelShape shape = stateBelow.getShape(level, posBelow);
        if (shape.isEmpty()) {
            return;
        }
        float f = LightTextureEv.getLightBrightness(level, level.getMaxLocalRawBrightness(pos));
        float alpha = weight * 0.5f * f;
        if (alpha >= 0.0F) {
            if (alpha > 1.0F) {
                alpha = 1.0F;
            }
            AABB aabb = shape.bounds();
            double minX = pos.getX() + aabb.minX;
            double maxX = pos.getX() + aabb.maxX;
            double minY = pos.getY() + aabb.minY;
            double minZ = pos.getZ() + aabb.minZ;
            double maxZ = pos.getZ() + aabb.maxZ;
            float dMinX = (float) (minX - x);
            float dMaxX = (float) (maxX - x);
            float dy = (float) (minY - y);
            float dMinZ = (float) (minZ - z);
            float dMaxZ = (float) (maxZ - z);
            float minU = -dMinX / 2.0F / size + 0.5F;
            float maxU = -dMaxX / 2.0F / size + 0.5F;
            float minV = -dMinZ / 2.0F / size + 0.5F;
            float maxV = -dMaxZ / 2.0F / size + 0.5F;
            shadowVertex(pose, buffer, alpha, dMinX, dy, dMinZ, minU, minV);
            shadowVertex(pose, buffer, alpha, dMinX, dy, dMaxZ, minU, maxV);
            shadowVertex(pose, buffer, alpha, dMaxX, dy, dMaxZ, maxU, maxV);
            shadowVertex(pose, buffer, alpha, dMaxX, dy, dMinZ, maxU, minV);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Improve performance
     */
    @Overwrite
    private static void renderShadow(PoseStack matrices,
                                     MultiBufferSource buffer,
                                     Entity entity,
                                     float weight,
                                     float partialTicks,
                                     LevelReader level,
                                     float size) {
        float actualSize = size;
        if (entity instanceof Mob mob && mob.isBaby()) {
            actualSize = size * 0.5F;
        }
        double entityX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double entityZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        int minX = Mth.floor(entityX - actualSize);
        int maxX = Mth.floor(entityX + actualSize);
        int minY = Mth.floor(entityY - Math.min(weight / 0.5f, actualSize));
        int maxY = Mth.floor(entityY);
        int minZ = Mth.floor(entityZ - actualSize);
        int maxZ = Mth.floor(entityZ + actualSize);
        PoseStack.Pose pose = matrices.last();
        VertexConsumer consumer = buffer.getBuffer(SHADOW_RENDER_TYPE);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                mutable.set(x, 0, z);
                for (int y = minY; y <= maxY; ++y) {
                    mutable.setY(y);
                    float r = weight - (float) (entityY - mutable.getY()) * 0.5f;
                    renderBlockShadow(pose, consumer, level, mutable, entityX, entityY, entityZ, actualSize, r);
                }
            }
        }
    }

    @Shadow
    private static void shadowVertex(PoseStack.Pose pMatrixEntry,
                                     VertexConsumer pBuffer,
                                     float pAlpha,
                                     float pX,
                                     float pY,
                                     float pZ,
                                     float pTexU,
                                     float pTexV) {
        throw new AbstractMethodError();
    }
}
