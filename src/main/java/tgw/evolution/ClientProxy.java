package tgw.evolution;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.ScreenCorpse;
import tgw.evolution.client.gui.ScreenInventory;
import tgw.evolution.client.tooltip.*;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionRenderer;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.stats.EvolutionStatsCounter;
import tgw.evolution.util.constants.SkinType;

import java.util.Map;

public class ClientProxy implements IProxy {

    public static final KeyMapping KEY_BUILDING_ASSIST = new KeyMapping("key.build_assist", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                                                                        GLFW.GLFW_KEY_BACKSLASH,
                                                                        "key.categories.creative");
    /**
     * Value injected from {@link tgw.evolution.mixin.OptionsMixin}
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    public static KeyMapping KEY_CRAWL;

    private static void addOverrides() {
//        ItemProperties.register(EvolutionItems.sword_dev.get(), new ResourceLocation("attack"), (stack, level, entity, seed) -> entity != null &&
//                                                                                                                                (
//                                                                                                                                (ILivingEntityPatch) entity).renderMainhandSpecialAttack() &&
//                                                                                                                                entity
//                                                                                                                                .getMainHandItem
//                                                                                                                                () ==
//                                                                                                                                stack ? 1.0f : 0
//                                                                                                                                .0f);
        ItemProperties.register(EvolutionItems.SHIELD_DEV.get(), new ResourceLocation("blocking"),
                                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    public static void changeWorldOrders() {
        Evolution.warn("Change world order");
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

    private static void registerHUDOverlays() {
        OverlayRegistry.enableOverlay(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, false);
        OverlayRegistry.enableOverlay(ForgeIngameGui.ARMOR_LEVEL_ELEMENT, false);
        OverlayRegistry.enableOverlay(ForgeIngameGui.FOOD_LEVEL_ELEMENT, false);
        OverlayRegistry.enableOverlay(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, false);
        OverlayRegistry.enableOverlay(ForgeIngameGui.CROSSHAIR_ELEMENT, false);
        OverlayRegistry.enableOverlay(ForgeIngameGui.POTION_ICONS_ELEMENT, false);
    }

    private static void registerKeyBinds() {
        ClientRegistry.registerKeyBinding(KEY_CRAWL);
        ClientRegistry.registerKeyBinding(KEY_BUILDING_ASSIST);
    }

    private static void registerScreens() {
        MenuScreens.register(EvolutionContainers.EXTENDED_INVENTORY.get(), ScreenInventory::new);
        MenuScreens.register(EvolutionContainers.CORPSE.get(), ScreenCorpse::new);
    }

    private static void registerTooltips() {
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipCold.class, EvolutionTooltipRenderer.COLD::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipCooldown.class, EvolutionTooltipRenderer.COOLDOWN::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDamage.class, TooltipDamage::setup);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDmgMultiplier.class, TooltipDmgMultiplier::setup);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDrink.class, EvolutionTooltipRenderer.DRINK::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDurability.class, TooltipDurability::setup);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipFollowUp.class, EvolutionTooltipRenderer.FOLLOW_UP::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipFood.class, EvolutionTooltipRenderer.FOOD::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipHeat.class, EvolutionTooltipRenderer.HEAT::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipInfo.class, EvolutionTooltipRenderer.INFO::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipMass.class, TooltipMass::setup);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipMining.class, EvolutionTooltipRenderer.MINING::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipPrecision.class, EvolutionTooltipRenderer.PRECISION::setTooltip);
        MinecraftForgeClient.registerTooltipComponentFactory(TooltipThrowSpeed.class, EvolutionTooltipRenderer.THROW_SPEED::setTooltip);
    }

    @Override
    public void addTextures(TextureStitchEvent.Pre event) {
        for (ResourceLocation resLoc : EvolutionResources.SLOT_EXTENDED) {
            event.addSprite(resLoc);
        }
        for (ResourceLocation resLoc : EvolutionResources.SLOT_ARMOR) {
            event.addSprite(resLoc);
        }
        event.addSprite(EvolutionResources.SLOT_OFFHAND);
    }

    @Override
    public Level getClientLevel() {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level;
    }

    @Override
    public Player getClientPlayer() {
        assert Minecraft.getInstance().player != null;
        return Minecraft.getInstance().player;
    }

    @Override
    public float getPartialTicks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) {
            return mc.pausePartialTick;
        }
        return mc.getFrameTime();
    }

    @Override
    public SkinType getSkinType() {
        return "default".equals(((AbstractClientPlayer) this.getClientPlayer()).getModelName()) ? SkinType.STEVE : SkinType.ALEX;
    }

    @Override
    public void init() {
        IProxy.super.init();
        Minecraft mc = Minecraft.getInstance();
        EvolutionRenderer.registryEntityRenders();
        addOverrides();
        registerScreens();
        ColorManager.registerBlockColorHandlers(mc.getBlockColors());
        ColorManager.registerItemColorHandlers(mc.getItemColors());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEvents::onModelBakeEvent);
        MinecraftForge.EVENT_BUS.register(new ClientEvents(mc));
        registerKeyBinds();
        changeWorldOrders();
//        EvolutionParticles.register();
        registerTooltips();
        registerHUDOverlays();
        mc.getMainRenderTarget().enableStencil();
        Evolution.info("Finished loading!");
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        for (int i = 0, l = EvolutionResources.MODULAR_MODELS.size(); i < l; i++) {
            ForgeModelBakery.addSpecialModel(EvolutionResources.MODULAR_MODELS.get(i));
        }
        //Clear and trim since we are not using it anymore
        EvolutionResources.MODULAR_MODELS.reset();
    }

    @Override
    public void updateStats(Object2LongMap<Stat<?>> statsData) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        EvolutionStatsCounter stats = (EvolutionStatsCounter) player.getStats();
        for (Map.Entry<Stat<?>, Long> entry : statsData.object2LongEntrySet()) {
            Stat<?> stat = entry.getKey();
            long i = entry.getValue();
            stats.setValueLong(stat, i);
        }
        if (Minecraft.getInstance().screen instanceof StatsUpdateListener s) {
            s.onStatsUpdated();
        }
    }
}
