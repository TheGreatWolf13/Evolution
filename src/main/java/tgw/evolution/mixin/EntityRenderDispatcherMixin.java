package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

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
        if (stateBelow.getRenderShape() != RenderShape.INVISIBLE && level.getMaxLocalRawBrightness(pos) > 3) {
            if (stateBelow.isCollisionShapeFullBlock(level, posBelow)) {
                VoxelShape shape = stateBelow.getShape(level, posBelow);
                if (!shape.isEmpty()) {
                    float f = (float) ((weight - (y - pos.getY()) / 2.0) * 0.5 * level.getBrightness(pos));
                    if (f >= 0.0F) {
                        if (f > 1.0F) {
                            f = 1.0F;
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
                        float f6 = -dMinX / 2.0F / size + 0.5F;
                        float f7 = -dMaxX / 2.0F / size + 0.5F;
                        float f8 = -dMinZ / 2.0F / size + 0.5F;
                        float f9 = -dMaxZ / 2.0F / size + 0.5F;
                        shadowVertex(pose, buffer, f, dMinX, dy, dMinZ, f6, f8);
                        shadowVertex(pose, buffer, f, dMinX, dy, dMaxZ, f6, f9);
                        shadowVertex(pose, buffer, f, dMaxX, dy, dMaxZ, f7, f9);
                        shadowVertex(pose, buffer, f, dMaxX, dy, dMinZ, f7, f8);
                    }
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
