package tgw.evolution;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMaps;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import tgw.evolution.util.DataSerializer;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.world.WorldEvolutionDefault;
import tgw.evolution.world.WorldEvolutionFlat;
import tgw.evolution.world.feature.EvolutionFeatures;

@Mod("evolution")
public final class Evolution {

    public static final String MODID = "evolution";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public static final IPacketHandler PACKET_HANDLER = DistExecutor.safeRunForDist(() -> PacketHandlerClient::new, () -> PacketHandlerDummy::new);
    public static final Int2BooleanMap PRONED_PLAYERS = Int2BooleanMaps.synchronize(new Int2BooleanOpenHashMap());
    public static Evolution instance;

    public Evolution() {
        instance = this;
        EvolutionConfig.register(ModLoadingContext.get());
        EvolutionBlocks.register();
        EvolutionItems.register();
        EvolutionFluids.register();
//        EvolutionCarvers.register();
        EvolutionFeatures.register();
        EvolutionEntities.register();
        EvolutionTEs.register();
        EvolutionSounds.register();
        EvolutionContainers.register();
        EvolutionEffects.register();
        EvolutionBiomes.register();
        EvolutionAttributes.register();
        EvolutionStats.register();
        EvolutionParticles.register();
        ForgeWorldType defaultWorldType = new WorldEvolutionDefault();
        defaultWorldType.setRegistryName(getResource("default"));
        ForgeRegistries.WORLD_TYPES.register(defaultWorldType);
        ForgeWorldType flatWorldType = new WorldEvolutionFlat();
        flatWorldType.setRegistryName(getResource("flat"));
        ForgeRegistries.WORLD_TYPES.register(flatWorldType);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::registerParticleFactories);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::loadComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onEntityAttributeCreation);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onEntityAttributeModification);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Evolution::onTexturePreStitch);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(DataSerializerEntry.class, Evolution::registerSerializers);
        MinecraftForge.EVENT_BUS.register(this);
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

    private static void onClientSetup(FMLClientSetupEvent event) {
        EvolutionRenderLayer.setup();
        ClientEvents.fixInputMappings();
        FieldHandler<FontRenderer, Integer> fontHeight = new FieldHandler<>(FontRenderer.class, "field_78288_b");
        fontHeight.set(Minecraft.getInstance().font, 10);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        EvolutionEntities.registerEntityAttribute(event);
    }

    private static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        EvolutionEntities.modifyEntityAttribute(event);
    }

    private static void onTexturePreStitch(TextureStitchEvent.Pre event) {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientProxy.addTextures(event);
        }
    }

    private static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        EvolutionParticles.registerFactories(Minecraft.getInstance().particleEngine);
    }

    private static void registerSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
        DataSerializer.register(event, getResource("item_list"));
    }

    private static void setup(FMLCommonSetupEvent event) {
        PROXY.init();
        EvolutionNetwork.registerMessages();
        EvolutionCapabilities.register();
        MinecraftForge.EVENT_BUS.register(new WorldEvents());
        MinecraftForge.EVENT_BUS.register(new ChunkEvents());
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        LOGGER.info("Setup registries done.");
    }

    public static void usingPlaceholder(PlayerEntity player, String obj) {
        player.displayClientMessage(new StringTextComponent("[DEBUG] Using placeholder " + obj + "!"), false);
    }
}
