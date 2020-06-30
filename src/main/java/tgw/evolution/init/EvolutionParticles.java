package tgw.evolution.init;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.particle.SleepParticle;

@EventBusSubscriber
public class EvolutionParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = new DeferredRegister<>(ForgeRegistries.PARTICLE_TYPES, Evolution.MODID);

    public static final RegistryObject<BasicParticleType> SLEEP = PARTICLES.register("sleep_particle", () -> register(false));

    public static void register() {
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private static BasicParticleType register(boolean alwaysShow) {
        return new BasicParticleType(alwaysShow);
    }

    public static void registerFactories(ParticleManager manager) {
        manager.registerFactory(SLEEP.get(), SleepParticle.Factory::new);
    }
}
