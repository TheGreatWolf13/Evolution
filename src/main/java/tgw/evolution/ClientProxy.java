package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.stats.Stat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.ScreenCorpse;
import tgw.evolution.client.gui.ScreenInventoryExtended;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.events.ItemEvents;
import tgw.evolution.init.*;
import tgw.evolution.stats.EvolutionStatisticsManager;
import tgw.evolution.util.RockVariant;
import tgw.evolution.util.SkinType;

import java.util.Map;

public class ClientProxy implements IProxy {

    public static final KeyBinding TOGGLE_PRONE = new KeyBinding("key.prone.toggle",
                                                                 KeyConflictContext.IN_GAME,
                                                                 InputMappings.Type.KEYSYM,
                                                                 GLFW.GLFW_KEY_X,
                                                                 "key.categories.movement");
    public static final KeyBinding BUILDING_ASSIST = new KeyBinding("key.build_assist",
                                                                    KeyConflictContext.IN_GAME,
                                                                    InputMappings.Type.KEYSYM,
                                                                    GLFW.GLFW_KEY_BACKSLASH,
                                                                    "key.categories.creative");

    private static void addOverrides() {
        ItemModelsProperties.register(EvolutionItems.shield_dev.get(),
                                      new ResourceLocation("blocking"),
                                      (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        ResourceLocation throwing = new ResourceLocation("throwing");
        for (RockVariant variant : RockVariant.VALUES) {
            Item item;
            try {
                item = variant.getJavelin();
            }
            catch (IllegalStateException t) {
                item = null;
            }
            if (item != null) {
                ItemModelsProperties.register(item,
                                              throwing,
                                              (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ?
                                                                        1.0F :
                                                                        0.0F);
            }
        }
    }

    public static void changeWorldOrders() {
        Evolution.LOGGER.warn("Change world order");
//        int evId = 0;
//        for (WorldType worldType : WorldType.WORLD_TYPES) {
//            if (worldType != null && "ev_default".equals(worldType.getName())) {
//                evId = worldType.getId();
//                break;
//            }
//        }
//        WorldType evWorld = WorldType.WORLD_TYPES[evId];
//        System.arraycopy(WorldType.WORLD_TYPES, 0, WorldType.WORLD_TYPES, 1, evId);
//        WorldType.WORLD_TYPES[0] = evWorld;
    }

    @Override
    public void addTextures(TextureStitchEvent.Pre event) {
        for (ResourceLocation resLoc : EvolutionResources.SLOT_EXTENDED) {
            event.addSprite(resLoc);
        }
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Override
    public SkinType getSkinType() {
        return "default".equals(((AbstractClientPlayerEntity) this.getClientPlayer()).getModelName()) ? SkinType.STEVE : SkinType.ALEX;
    }

    @Override
    public void init() {
        IProxy.super.init();
        EvolutionRenderer.registryEntityRenders();
        addOverrides();
        ScreenManager.register(EvolutionContainers.EXTENDED_INVENTORY.get(), ScreenInventoryExtended::new);
        ScreenManager.register(EvolutionContainers.CORPSE.get(), ScreenCorpse::new);
        ColorManager.registerBlockColorHandlers(Minecraft.getInstance().getBlockColors());
        ColorManager.registerItemColorHandlers(Minecraft.getInstance().getItemColors());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEvents::onModelBakeEvent);
        MinecraftForge.EVENT_BUS.register(new ClientEvents(Minecraft.getInstance()));
        MinecraftForge.EVENT_BUS.register(new ItemEvents());
        ClientRegistry.registerKeyBinding(TOGGLE_PRONE);
        ClientRegistry.registerKeyBinding(BUILDING_ASSIST);
        changeWorldOrders();
        EvolutionParticles.register();
        Evolution.LOGGER.info("ClientProxy: Finished loading!");
    }

    @Override
    public void updateStats(Object2LongMap<Stat<?>> statsData) {
        for (Map.Entry<Stat<?>, Long> entry : statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            long i = entry.getValue();
            ((EvolutionStatisticsManager) Minecraft.getInstance().player.getStats()).setValueLong(stat, i);
        }
        if (Minecraft.getInstance().screen instanceof IProgressMeter) {
            ((IProgressMeter) Minecraft.getInstance().screen).onStatsUpdated();
        }
    }
}
