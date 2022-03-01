//package tgw.evolution.particle;
//
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.particle.Particle;
//import net.minecraft.client.world.ClientWorld;
//import net.minecraft.particles.BasicParticleType;
//import net.minecraft.util.math.MathHelper;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//
//@OnlyIn(Dist.CLIENT)
//public final class SleepParticle extends SpriteTexturedParticle {
//
//    private SleepParticle(ClientLevel world, double posX, double posY, double posZ) {
//        super(world, posX, posY, posZ, 0, 0, 0);
//        this.xd *= 0.01F;
//        this.yd *= 0.01F;
//        this.zd *= 0.01F;
//        this.yd += 0.01F;
//        this.quadSize *= 1.5F;
//        this.lifetime = 100;
//        this.hasPhysics = false;
//    }
//
//    @Override
//    public float getQuadSize(float scale) {
//        return this.quadSize * MathHelper.clamp((this.age + scale) / this.lifetime * 32.0F, 0.0F, 1.0F);
//    }
//
//    @Override
//    public IParticleRenderType getRenderType() {
//        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
//    }
//
//    @Override
//    public void tick() {
//        this.xo = this.x;
//        this.yo = this.y;
//        this.zo = this.z;
//        if (this.age++ >= this.lifetime) {
//            this.remove();
//        }
//        else {
//            this.move(this.xd, this.yd, this.zd);
//            if (this.y == this.yo) {
//                this.xd *= 1.1;
//                this.zd *= 1.1;
//            }
//            if (this.age > 75) {
//                this.yd *= 0.86;
//            }
//            this.xd *= 0.86F;
//            this.xd *= 0.86F;
//            if (this.onGround) {
//                this.xd *= 0.7F;
//                this.xd *= 0.7F;
//            }
//        }
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public static class Factory implements IParticleFactory<BasicParticleType> {
//
//        private final IAnimatedSprite spriteSet;
//
//        public Factory(IAnimatedSprite spriteSet) {
//            this.spriteSet = spriteSet;
//        }
//
//        @Override
//        public Particle createParticle(BasicParticleType type,
//                                       ClientWorld world,
//                                       double x,
//                                       double y,
//                                       double z,
//                                       double xSpeed,
//                                       double ySpeed,
//                                       double zSpeed) {
//            SleepParticle sleepParticle = new SleepParticle(world, x, y, z);
//            sleepParticle.pickSprite(this.spriteSet);
//            return sleepParticle;
//        }
//    }
//}