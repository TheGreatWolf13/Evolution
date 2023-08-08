package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;

@Mixin(ItemPickupParticle.class)
public abstract class MixinItemPickupParticle extends Particle {

    @Unique private static final PoseStack MATRICES = new PoseStack();
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow @Final private Entity itemEntity;
    @Shadow private int life;
    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow @Final private Entity target;

    public MixinItemPickupParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Override
    @Overwrite
    public void render(VertexConsumer builder, Camera camera, float partialTicks) {
        float itemLerp = (this.life + partialTicks) / 3.0F;
        itemLerp *= itemLerp;
        double targetX = Mth.lerp(partialTicks, this.target.xOld, this.target.getX());
        double targetY = Mth.lerp(partialTicks, this.target.yOld, this.target.getY()) + this.target.getBbHeight() * 0.125;
        double targetZ = Mth.lerp(partialTicks, this.target.zOld, this.target.getZ());
        double x = Mth.lerp(itemLerp, this.itemEntity.getX(), targetX);
        double y = Mth.lerp(itemLerp, this.itemEntity.getY(), targetY);
        double z = Mth.lerp(itemLerp, this.itemEntity.getZ(), targetZ);
        MultiBufferSource.BufferSource buffer = this.renderBuffers.bufferSource();
        Vec3 camPos = camera.getPosition();
        this.entityRenderDispatcher.render(this.itemEntity, x - camPos.x(), y - camPos.y(), z - camPos.z(), this.itemEntity.getYRot(), partialTicks, MATRICES, buffer, this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, partialTicks));
        buffer.endBatch();
    }
}
