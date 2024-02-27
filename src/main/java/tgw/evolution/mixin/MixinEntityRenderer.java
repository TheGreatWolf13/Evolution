package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchEntityRenderer;
import tgw.evolution.util.hitbox.hrs.HREntity;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> implements HREntity<T>, PatchEntityRenderer<T> {

    @Shadow public float shadowRadius;

    @Overwrite
    public int getBlockLightLevel(T entity, BlockPos pos) {
        Evolution.deprecatedMethod();
        return 0;
    }

    @Overwrite
    public final int getPackedLightCoords(T entity, float partialTicks) {
        Vec3 lightProbePosition = entity.getLightProbePosition(partialTicks);
        int x = Mth.floor(lightProbePosition.x);
        int y = Mth.floor(lightProbePosition.y);
        int z = Mth.floor(lightProbePosition.z);
        int blockLight = this.getBlockLightLevel_(entity, x, y, z);
        return blockLight | this.getSkyLightLevel_(entity, x, y, z) << 16;
    }

    /**
     * @author TheGreatWolf
     * @reason Use HRs
     */
    @Overwrite
    public Vec3 getRenderOffset(T entity, float partialTicks) {
        return this.renderOffset(entity, partialTicks);
    }

    @Overwrite
    public int getSkyLightLevel(T entity, BlockPos pos) {
        Evolution.deprecatedMethod();
        return 0;
    }

    /**
     * Prevents name tags from being visible though walls.
     */
    @ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch" +
                                                                             "(Lnet/minecraft/network/chat/Component;" +
                                                                             "FFIZLcom/mojang/math/Matrix4f;" +
                                                                             "Lnet/minecraft/client/renderer/MultiBufferSource;ZII)I", ordinal = 0), index = 7)
    private boolean modifyRenderName(boolean renderThroughWalls) {
        return false;
    }

    @Override
    public void setShadowRadius(float radius) {
        this.shadowRadius = radius;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
        if (!entity.shouldRender(camX, camY, camZ)) {
            return false;
        }
        if (entity.noCulling) {
            return true;
        }
        AABB aabb = entity.getBoundingBoxForCulling();
        double minX = aabb.minX - 0.5;
        double minY = aabb.minY - 0.5;
        double minZ = aabb.minZ - 0.5;
        double maxX = aabb.maxX + 0.5;
        double maxY = aabb.maxY + 0.5;
        double maxZ = aabb.maxZ + 0.5;
        if (aabb.hasNaN()) {
            minX = entity.getX() - 2;
            minY = entity.getY() - 2;
            minZ = entity.getZ() - 2;
            maxX = entity.getX() + 2;
            maxY = entity.getY() + 2;
            maxZ = entity.getZ() + 2;
        }
        if (!frustum.cubeInFrustum(minX, minY, minZ, maxX, maxY, maxZ)) {
            return false;
        }
        return Minecraft.getInstance().lvlRenderer().visibleOcclusionCulling(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
