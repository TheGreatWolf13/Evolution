package tgw.evolution.mixin;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    protected ClientLevel level;
    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;
    @Shadow
    @Final
    private Queue<Particle> particlesToAdd;
    @Shadow
    @Final
    private Queue<TrackingEmitter> trackingEmitters;

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
