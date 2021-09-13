package tgw.evolution.init;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.particle.SleepParticle;

public final class EvolutionParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Evolution.MODID);

    public static final RegistryObject<BasicParticleType> SLEEP = PARTICLES.register("sleep_particle", () -> new BasicParticleType(true));

    private EvolutionParticles() {
    }

    public static void register() {
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerFactories(ParticleManager manager) {
        manager.register(SLEEP.get(), SleepParticle.Factory::new);
    }
}
