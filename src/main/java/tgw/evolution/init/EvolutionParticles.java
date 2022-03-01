//package tgw.evolution.init;
//
//import net.minecraft.client.particle.ParticleEngine;
//import net.minecraft.core.particles.ParticleType;
//import net.minecraft.core.particles.SimpleParticleType;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;
//import tgw.evolution.Evolution;
//import tgw.evolution.particle.SleepParticle;
//
//public final class EvolutionParticles {
//
//    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Evolution.MODID);
//
//    public static final RegistryObject<SimpleParticleType> SLEEP = PARTICLES.register("sleep_particle", () -> new SimpleParticleType(true));
//
//    private EvolutionParticles() {
//    }
//
//    public static void register() {
//        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    public static void registerFactories(ParticleEngine manager) {
//        manager.register(SLEEP.get(), SleepParticle.Factory::new);
//    }
//}
