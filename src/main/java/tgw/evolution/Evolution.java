package tgw.evolution;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tgw.evolution.blocks.BlockFire;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.inventory.PlayerInventoryCapability;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ChunkEvents;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.events.ItemEvents;
import tgw.evolution.events.WorldEvents;
import tgw.evolution.init.*;
import tgw.evolution.world.EvWorldDefault;
import tgw.evolution.world.EvWorldFlat;
import tgw.evolution.world.dimension.DimensionOverworld;
import tgw.evolution.world.feature.EvolutionFeatures;
import tgw.evolution.world.gen.carver.EvolutionCarvers;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@Mod("evolution")
public class Evolution {

    public static final String MODID = "evolution";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static final Map<UUID, Boolean> PRONED_PLAYERS = Maps.newConcurrentMap();
    public static Evolution instance;

    public Evolution() {
        instance = this;
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EvolutionConfig.COMMON_CONFIG, "evolution-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EvolutionConfig.CLIENT_CONFIG, "evolution-client.toml");
        EvolutionBlocks.register();
        EvolutionItems.register();
        EvolutionFluids.register();
        EvolutionCarvers.register();
        EvolutionFeatures.register();
        EvolutionEntities.register();
        EvolutionTileEntities.register();
        EvolutionSounds.register();
        EvolutionContainers.register();
        EvolutionParticles.register();
        EvolutionEffects.register();
        EvolutionBiomes.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::particleRegistry);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(MODID, name);
    }

    private void setup(FMLCommonSetupEvent event) {
        new EvWorldDefault();
        new EvWorldFlat();
        PROXY.init();
        EvolutionConfig.loadConfig(EvolutionConfig.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("evolution-client.toml").toString());
        EvolutionConfig.loadConfig(EvolutionConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("evolution-common.toml").toString());
        EvolutionNetwork.registerMessages();
        ChunkStorageCapability.register();
        PlayerInventoryCapability.register();
        MinecraftForge.EVENT_BUS.register(new WorldEvents());
        //        MinecraftForge.EVENT_BUS.register(new FallingEvents());
        MinecraftForge.EVENT_BUS.register(new ChunkEvents());
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        MinecraftForge.EVENT_BUS.register(new ItemEvents());
        BiFunction<World, DimensionType, ? extends Dimension> dimensionFactory = DimensionOverworld::new;
        ObfuscationReflectionHelper.setPrivateValue(DimensionType.class,
                                                    DimensionType.OVERWORLD,
                                                    dimensionFactory,
                                                    "field_201038_g");
        Evolution.LOGGER.info("Setup registries done.");
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        EvolutionBlocks.setupVariants();
        EvolutionItems.setupVariants();
        EvolutionBiomes.registerBiomes();
        EvolutionEntities.registerEntityWorldSpawns();
        BlockFire.init();
    }

    private void particleRegistry(ParticleFactoryRegisterEvent event) {
        EvolutionParticles.registerFactories(Minecraft.getInstance().particles);
    }
}
