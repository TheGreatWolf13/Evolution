package tgw.evolution;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tgw.evolution.blocks.BlockFire;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ChunkEvents;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.events.WorldEvents;
import tgw.evolution.init.*;
import tgw.evolution.network.IPacketHandler;
import tgw.evolution.network.PacketHandlerClient;
import tgw.evolution.network.PacketHandlerDummy;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.world.EvWorldDefault;
import tgw.evolution.world.EvWorldFlat;
import tgw.evolution.world.dimension.DimensionOverworld;
import tgw.evolution.world.feature.EvolutionFeatures;
import tgw.evolution.world.gen.carver.EvolutionCarvers;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@Mod("evolution")
public final class Evolution {

    public static final String MODID = "evolution";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static final IPacketHandler PACKET_HANDLER = DistExecutor.runForDist(() -> PacketHandlerClient::new, () -> PacketHandlerDummy::new);
    public static final Map<UUID, Boolean> PRONED_PLAYERS = Maps.newConcurrentMap();
    private static final FieldHandler<DimensionType, BiFunction<World, DimensionType, ? extends Dimension>> DIMENSION_FACTORY_FIELD =
            new FieldHandler<>(
            DimensionType.class,
            "field_201038_g");
    public static Evolution instance;

    public Evolution() {
        instance = this;
        EvolutionConfig.register(ModLoadingContext.get());
        EvolutionBlocks.register();
        EvolutionItems.register();
        EvolutionFluids.register();
        EvolutionCarvers.register();
        EvolutionFeatures.register();
        EvolutionEntities.register();
        EvolutionTileEntities.register();
        EvolutionSounds.register();
        EvolutionContainers.register();
        EvolutionEffects.register();
        EvolutionBiomes.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::registerParticleFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::loadComplete);
        MinecraftForge.EVENT_BUS.register(this);
        EvolutionParticles.register();
        ModLoadingContext.get()
                         .registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                                                 () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, b) -> true));
    }

    public static ResourceLocation getResource(String name) {
        return new ResourceLocation(MODID, name);
    }

    private static void loadComplete(FMLLoadCompleteEvent event) {
        EvolutionBiomes.registerBiomes();
        EvolutionEntities.registerEntityWorldSpawns();
        BlockFire.init();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientEvents.onFinishLoading();
        }
    }

    public static void log(boolean log, String message, Object... args) {
        if (log) {
            LOGGER.debug(message, args);
        }
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        EvolutionParticles.registerFactories(Minecraft.getInstance().particles);
    }

    private static void setup(FMLCommonSetupEvent event) {
        new EvWorldDefault();
        new EvWorldFlat();
        PROXY.init();
        EvolutionNetwork.registerMessages();
        EvolutionCapabilities.register();
        MinecraftForge.EVENT_BUS.register(new WorldEvents());
        //        MinecraftForge.EVENT_BUS.register(new FallingEvents());
        MinecraftForge.EVENT_BUS.register(new ChunkEvents());
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        BiFunction<World, DimensionType, ? extends Dimension> dimensionFactory = DimensionOverworld::new;
        DIMENSION_FACTORY_FIELD.set(DimensionType.OVERWORLD, dimensionFactory);
        LOGGER.info("Setup registries done.");
    }

    public static void usingPlaceholder(PlayerEntity player, String obj) {
        player.sendStatusMessage(new StringTextComponent("[DEBUG] Using placeholder " + obj + "!"), false);
    }
}
