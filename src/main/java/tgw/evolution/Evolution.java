package tgw.evolution;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMaps;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tgw.evolution.blocks.BlockFire;
import tgw.evolution.client.renderer.EvolutionRenderLayer;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ChunkEvents;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.events.EntityEvents;
import tgw.evolution.events.WorldEvents;
import tgw.evolution.init.*;
import tgw.evolution.network.IPacketHandler;
import tgw.evolution.network.PacketHandlerClient;
import tgw.evolution.network.PacketHandlerDummy;
import tgw.evolution.util.EvolutionDataSerializers;
import tgw.evolution.util.reflection.FieldHandler;

@Mod("evolution")
public final class Evolution {

    public static final String MODID = "evolution";
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static final IPacketHandler PACKET_HANDLER = DistExecutor.safeRunForDist(() -> PacketHandlerClient::new, () -> PacketHandlerDummy::new);
    public static final Int2BooleanMap PRONED_PLAYERS = Int2BooleanMaps.synchronize(new Int2BooleanOpenHashMap());
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker MARKER = MarkerManager.getMarker("Evolution");
    public static Evolution instance;

    public Evolution() {
        instance = this;
        EvolutionConfig.register(ModLoadingContext.get());
        EvolutionBlocks.register();
        EvolutionItems.register();
        EvolutionFluids.register();
//        EvolutionCarvers.register();
//        EvolutionFeatures.register();
        EvolutionEntities.register();
        EvolutionTEs.register();
        EvolutionSounds.register();
        EvolutionContainers.register();
        EvolutionEffects.register();
        EvolutionBiomes.register();
        EvolutionAttributes.register();
        EvolutionStats.register();
//        EvolutionParticles.register();
//        ForgeWorldType defaultWorldType = new WorldEvolutionDefault();
//        defaultWorldType.setRegistryName(getResource("default"));
//        ForgeRegistries.WORLD_TYPES.register(defaultWorldType);
//        ForgeWorldType flatWorldType = new WorldEvolutionFlat();
//        flatWorldType.setRegistryName(getResource("flat"));
//        ForgeRegistries.WORLD_TYPES.register(flatWorldType);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::registerParticleFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::registerCapabilities);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onEntityAttributeCreation);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onEntityAttributeModification);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                                     () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onTexturePreStitch));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                                     () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onModelRegistry));
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(DataSerializerEntry.class, Evolution::registerSerializers);
        MinecraftForge.EVENT_BUS.register(this);
//        ModLoadingContext.get()
//                         .registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
//                                                 () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, b) -> true));
    }

    public static void debug(String message, Object... objects) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.debug(MARKER, "[" + clazz + "]: " + message, objects);
    }

    public static void debug(String message) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.debug(MARKER, "[" + clazz + "]: " + message);
    }

    public static void error(String message, Object... objects) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.error(MARKER, "[" + clazz + "]: " + message, objects);
    }

    public static void error(String message) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.error(MARKER, "[" + clazz + "]: " + message);
    }

    public static ResourceLocation getResource(String name) {
        return new ResourceLocation(MODID, name);
    }

    public static void info(String message, Object... objects) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.info(MARKER, "[" + clazz + "]: " + message, objects);
    }

    public static void info(String message) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.info(MARKER, "[" + clazz + "]: " + message);
    }

    private static void loadComplete(FMLLoadCompleteEvent event) {
//        EvolutionBiomes.registerBiomes();
        EvolutionEntities.registerEntityWorldSpawns();
        BlockFire.init();
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientEvents.onFinishLoading();
        }
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        EvolutionRenderLayer.setup();
        ClientEvents.fixInputMappings();
        FieldHandler<Font, Integer> fontHeight = new FieldHandler<>(Font.class, "f_92710_");
        fontHeight.set(Minecraft.getInstance().font, 10);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        EvolutionEntities.registerEntityAttribute(event);
    }

    private static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        EvolutionEntities.modifyEntityAttribute(event);
    }

    private static void onModelRegistry(ModelRegistryEvent event) {
        PROXY.registerModels(event);
    }

    private static void onTexturePreStitch(TextureStitchEvent.Pre event) {
        PROXY.addTextures(event);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        EvolutionCapabilities.register(event);
    }

    private static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
//        EvolutionParticles.registerFactories(Minecraft.getInstance().particleEngine);
    }

    private static void registerSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
        EvolutionDataSerializers.register(event, getResource("item_list"));
    }

    private static void setup(FMLCommonSetupEvent event) {
        PROXY.init();
        EvolutionNetwork.registerMessages();
        MinecraftForge.EVENT_BUS.register(new WorldEvents());
        MinecraftForge.EVENT_BUS.register(new ChunkEvents());
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        LOGGER.info(MARKER, "Setup registries done.");
    }

    public static void usingPlaceholder(Player player, String obj) {
        player.displayClientMessage(new TextComponent("[DEBUG] Using placeholder " + obj + "!"), false);
    }

    public static void warn(String message) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.warn(MARKER, "[" + clazz + "]: " + message);
    }

    public static void warn(String message, Object... objects) {
        String clazz = Thread.currentThread().getStackTrace()[2].getClassName();
        LOGGER.warn(MARKER, "[" + clazz + "]: " + message, objects);
    }
}
