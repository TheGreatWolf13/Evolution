package tgw.evolution.mixin;

import com.google.common.collect.EvictingQueue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;
import tgw.evolution.patches.PatchMinecraft;
import tgw.evolution.patches.PatchParticleEngine;

import java.util.*;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine implements PatchParticleEngine {

    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;
    @Shadow protected ClientLevel level;
    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final private Queue<Particle> particlesToAdd;
    @Unique private int renderedParticles;
    @Shadow @Final private TextureManager textureManager;
    @Shadow @Final private Queue<TrackingEmitter> trackingEmitters;

    /**
     * @author TheGreatWolf
     * @reason Use proper loop
     */
    @Overwrite
    public String countParticles() {
        int count = 0;
        for (Queue<Particle> queue : this.particles.values()) {
            count += queue.size();
        }
        return String.valueOf(count);
    }

    @Override
    public int getRenderedParticles() {
        return this.renderedParticles;
    }

    /**
     * @author TheGreatWolf
     * @reason Add occlusion culling
     */
    @Overwrite
    public void render(PoseStack matrices, MultiBufferSource.BufferSource buffer, LightTexture lightTexture, Camera camera, float partialTicks) {
        lightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.mulPoseMatrix(matrices.last().pose());
        RenderSystem.applyModelViewMatrix();
        EvLevelRenderer levelRenderer = ((PatchMinecraft) Minecraft.getInstance()).lvlRenderer();
        this.renderedParticles = 0;
        for (int i = 0, len = RENDER_ORDER.size(); i < len; i++) {
            ParticleRenderType type = RENDER_ORDER.get(i);
            Queue<Particle> queue = this.particles.get(type);
            if (queue == null || queue.isEmpty()) {
                continue;
            }
            AccessorRenderSystem.setShader(GameRenderer.getParticleShader());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            type.begin(builder, this.textureManager);
            for (Particle particle : queue) {
                AABB bb = particle.getBoundingBox();
                if (!levelRenderer.visibleFrustumCulling(bb)) {
                    continue;
                }
                double cx = (bb.minX + bb.maxX) * 0.5;
                double cy = (bb.minY + bb.maxY) * 0.5;
                double cz = (bb.minZ + bb.maxZ) * 0.5;
                if (!levelRenderer.visibleOcclusionCulling(cx, cy, cz)) {
                    continue;
                }
                try {
                    ++this.renderedParticles;
                    particle.render(builder, camera, partialTicks);
                }
                catch (Throwable t) {
                    CrashReport crash = CrashReport.forThrowable(t, "Rendering Particle");
                    CrashReportCategory category = crash.addCategory("Particle being rendered");
                    //noinspection ObjectAllocationInLoop
                    category.setDetail("Particle", particle::toString);
                    //noinspection ObjectAllocationInLoop
                    category.setDetail("Particle Type", type::toString);
                    throw new ReportedException(crash);
                }
            }
            type.end(tesselator);
        }
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations.
     */
    @Overwrite
    public void tick() {
        for (Map.Entry<ParticleRenderType, Queue<Particle>> entry : this.particles.entrySet()) {
            this.level.getProfiler().push(entry.getKey().toString());
            this.tickParticleList(entry.getValue());
            this.level.getProfiler().pop();
        }
        if (!this.trackingEmitters.isEmpty()) {
            Iterator<TrackingEmitter> iterator = this.trackingEmitters.iterator();
            while (iterator.hasNext()) {
                TrackingEmitter emitter = iterator.next();
                emitter.tick();
                if (!emitter.isAlive()) {
                    iterator.remove();
                }
            }
        }
        if (!this.particlesToAdd.isEmpty()) {
            Particle particle;
            while ((particle = this.particlesToAdd.poll()) != null) {
                Queue<Particle> queue = this.particles.get(particle.getRenderType());
                if (queue == null) {
                    queue = EvictingQueue.create(16_384);
                    this.particles.put(particle.getRenderType(), queue);
                }
                queue.add(particle);
            }
        }
    }

    @Shadow
    protected abstract void tickParticle(Particle pParticle);

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations.
     */
    @Overwrite
    private void tickParticleList(Collection<Particle> particles) {
        if (!particles.isEmpty()) {
            Iterator<Particle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                this.tickParticle(particle);
                if (!particle.isAlive()) {
                    Optional<ParticleGroup> particleGroup = particle.getParticleGroup();
                    if (particleGroup.isPresent()) {
                        this.updateCount(particleGroup.get(), -1);
                    }
                    iterator.remove();
                }
            }
        }
    }

    @Shadow
    protected abstract void updateCount(ParticleGroup pGroup, int pCount);
}
