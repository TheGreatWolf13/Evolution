package tgw.evolution.particle;

import net.minecraft.client.particle.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SleepParticle extends SpriteTexturedParticle {

    private SleepParticle(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn, 0, 0, 0);
        this.motionX *= 0.01F;
        this.motionY *= 0.01F;
        this.motionZ *= 0.01F;
        this.motionY += 0.01F;
        this.particleScale *= 1.5F;
        this.maxAge = 100;
        this.canCollide = false;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getScale(float p_217561_1_) {
        return this.particleScale * MathHelper.clamp((this.age + p_217561_1_) / this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        }
        else {
            this.move(this.motionX, this.motionY, this.motionZ);
            if (this.posY == this.prevPosY) {
                this.motionX *= 1.1;
                this.motionZ *= 1.1;
            }
            if (this.age > 75) {
                this.motionY *= 0.86;
            }
            this.motionX *= 0.86F;
            this.motionZ *= 0.86F;
            if (this.onGround) {
                this.motionX *= 0.7F;
                this.motionZ *= 0.7F;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SleepParticle sleepParticle = new SleepParticle(worldIn, x, y, z);
            sleepParticle.selectSpriteRandomly(this.spriteSet);
            return sleepParticle;
        }
    }
}