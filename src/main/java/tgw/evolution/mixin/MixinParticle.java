package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.renderer.chunk.LevelRenderer;
import tgw.evolution.util.math.AABBMutable;

import java.util.List;

@Mixin(Particle.class)
public abstract class MixinParticle {

    @Shadow @Final private static AABB INITIAL_AABB;
    @Shadow @Final private static double MAXIMUM_COLLISION_VELOCITY_SQUARED;
    @Shadow protected int age;
    @Shadow private AABB bb;
    @Shadow protected float bbHeight;
    @Shadow protected float bbWidth;
    @Shadow protected float friction;
    @Shadow protected float gravity;
    @Shadow protected boolean hasPhysics;
    @Shadow @Final protected ClientLevel level;
    @Shadow protected int lifetime;
    @Shadow protected boolean onGround;
    @Shadow protected boolean speedUpWhenYMotionIsBlocked;
    @Shadow private boolean stoppedByCollision;
    @Shadow protected double x;
    @Shadow protected double xd;
    @Shadow protected double xo;
    @Shadow protected double y;
    @Shadow protected double yd;
    @Shadow protected double yo;
    @Shadow protected double z;
    @Shadow protected double zd;
    @Shadow protected double zo;

    @Shadow
    public abstract AABB getBoundingBox();

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer, delay BlockPos allocation
     */
    @Overwrite
    public int getLightColor(float partialTick) {
        int x = Mth.floor(this.x);
        int z = Mth.floor(this.z);
        return this.level.hasChunkAt(x, z) ? LevelRenderer.getLightColor(this.level, x, Mth.floor(this.y), z) : 0;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible
     */
    @Overwrite
    public void move(double x, double y, double z) {
        if (!this.stoppedByCollision) {
            double oldX = x;
            double oldY = y;
            double oldZ = z;
            if (this.hasPhysics && (x != 0 || y != 0 || z != 0) && x * x + y * y + z * z < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
                Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());
                x = vec3.x;
                y = vec3.y;
                z = vec3.z;
            }
            if (x != 0 || y != 0 || z != 0) {
                ((AABBMutable) this.bb).moveMutable(x, y, z);
                this.setLocationFromBoundingbox();
            }
            if (Math.abs(oldY) >= 1.0E-5F && Math.abs(y) < 1.0E-5F) {
                this.stoppedByCollision = true;
            }
            this.onGround = oldY != y && oldY < 0;
            if (oldX != x) {
                this.xd = 0;
            }
            if (oldZ != z) {
                this.zd = 0;
            }
        }
    }

    @Redirect(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDD)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client" +
                                                                                                                        "/particle/Particle;" +
                                                                                                                        "bb:Lnet/minecraft/world" +
                                                                                                                        "/phys/AABB;", opcode =
            Opcodes.PUTFIELD))
    private void onInit(Particle instance, AABB value) {
        this.bb = new AABBMutable(INITIAL_AABB);
    }

    @Shadow
    public abstract void remove();

    /**
     * @author TheGreatWolf
     * @reason Keep AABBMutable
     */
    @Overwrite
    public void setBoundingBox(AABB bb) {
        ((AABBMutable) this.bb).set(bb);
    }

    @Shadow
    protected abstract void setLocationFromBoundingbox();

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float halfWidth = this.bbWidth / 2.0F;
        float height = this.bbHeight;
        ((AABBMutable) this.bb).setUnchecked(x - halfWidth, y, z - halfWidth, x + halfWidth, y + height, z + halfWidth);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void setSize(float width, float height) {
        if (width != this.bbWidth || height != this.bbHeight) {
            this.bbWidth = width;
            this.bbHeight = height;
            AABB aabb = this.getBoundingBox();
            double minX = (aabb.minX + aabb.maxX - width) / 2.0;
            double minZ = (aabb.minZ + aabb.maxZ - width) / 2.0;
            ((AABBMutable) aabb).setUnchecked(minX, aabb.minY, minZ, minX + this.bbWidth, aabb.minY + this.bbWidth, minZ + this.bbWidth);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd += this.gravity;
        this.move(this.xd, this.yd, this.zd);
        if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
            this.xd *= 1.1;
            this.zd *= 1.1;
        }
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }
}
