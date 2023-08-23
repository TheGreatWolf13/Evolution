package tgw.evolution;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.ScreenCorpse;
import tgw.evolution.client.gui.ScreenInventory;
import tgw.evolution.client.gui.overlays.EvolutionOverlays;
import tgw.evolution.client.gui.overlays.VanillaOverlays;
import tgw.evolution.client.renderer.EvolutionRenderLayer;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionContainers;
import tgw.evolution.init.EvolutionRenderer;
import tgw.evolution.mixin.AccessorInputConstants_Type;
import tgw.evolution.util.constants.SkinType;

import java.util.Locale;

public final class EvolutionClient implements ClientModInitializer {

    public static final KeyMapping KEY_BUILDING_ASSIST;
    public static final KeyMapping KEY_CRAWL;
    private static Minecraft mc;

    static {
        KEY_BUILDING_ASSIST = new KeyMapping("key.build_assist", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "key.categories.creative");
        KEY_CRAWL = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_X, "key.categories.movement", () -> EvolutionConfig.toggleCrawl);
    }

    //    private static void addOverrides() {
//        ItemProperties.register(EvolutionItems.sword_dev.get(), new ResourceLocation("attack"), (stack, level, entity, seed) -> entity != null &&
//                                                                                                                                (
//                                                                                                                                (PatchLivingEntity) entity).renderMainhandSpecialAttack() &&
//                                                                                                                                entity
//                                                                                                                                .getMainHandItem
//                                                                                                                                () ==
//                                                                                                                                stack ? 1.0f : 0
//                                                                                                                                .0f);
//        ItemProperties.register(EvolutionItems.SHIELD_DEV, new ResourceLocation("blocking"),
//                                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0
//                                .0F);
//    }

//    private static void addTextures(TextureStitchEvent.Pre event) {
//        for (ResourceLocation resLoc : EvolutionResources.SLOT_EXTENDED) {
//            event.addSprite(resLoc);
//        }
//        for (ResourceLocation resLoc : EvolutionResources.SLOT_ARMOR) {
//            event.addSprite(resLoc);
//        }
//        event.addSprite(EvolutionResources.SLOT_OFFHAND);
//    }

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

    private static void fixInputMappings() {
        ((AccessorInputConstants_Type) (Object) InputConstants.Type.KEYSYM).setDisplayTextSupplier((keyCode, translationKey) -> {
            String formattedString = I18n.get(translationKey);
            if (formattedString.equals(translationKey)) {
                String s = GLFW.glfwGetKeyName(keyCode, -1);
                if (s != null) {
                    return new TextComponent(s.toUpperCase(Locale.ROOT));
                }
            }
            return new TranslatableComponent(translationKey);
        });
    }

//    private static void registerKeyBinds() {
//        ClientRegistry.registerKeyBinding(KEY_CRAWL);
//        ClientRegistry.registerKeyBinding(KEY_BUILDING_ASSIST);
//    }

    public static Component getAttackKeyText() {
        return mc.options.keyAttack.getTranslatedKeyMessage();
    }

    public static ClientLevel getClientLevel() {
        assert mc.level != null;
        return mc.level;
    }

    public static LocalPlayer getClientPlayer() {
        assert mc.player != null;
        return mc.player;
    }

    public static float getPartialTicks() {
        if (mc.isPaused()) {
            return mc.pausePartialTick;
        }
        return mc.getFrameTime();
    }

    public static SkinType getSkinType() {
        return "default".equals(getClientPlayer().getModelName()) ? SkinType.STEVE : SkinType.ALEX;
    }

//    private static void registerModels(ModelRegistryEvent event) {
//        for (int i = 0, l = EvolutionResources.MODULAR_MODELS.size(); i < l; i++) {
//            ForgeModelBakery.addSpecialModel(EvolutionResources.MODULAR_MODELS.get(i));
//        }
//        //Clear and trim since we are not using it anymore
//        EvolutionResources.MODULAR_MODELS.reset();
//    }

    public static void init(Minecraft minecraft) {
        mc = minecraft;
        mc.getMainRenderTarget().enableStencil();
        ColorManager.registerBlockColorHandlers(mc.getBlockColors());
        ColorManager.registerItemColorHandlers(mc.getItemColors());
        new ClientEvents(mc);
        Evolution.info("Client initialized");
    }

//    private static void registerTooltips() {
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipCold.class, EvolutionTooltipRenderer.COLD::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipCooldown.class, EvolutionTooltipRenderer.COOLDOWN::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDamage.class, TooltipDamage::setup);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDmgMultiplier.class, TooltipDmgMultiplier::setup);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDrink.class, EvolutionTooltipRenderer.DRINK::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipDurability.class, TooltipDurability::setup);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipFollowUp.class, EvolutionTooltipRenderer.FOLLOW_UP::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipFood.class, EvolutionTooltipRenderer.FOOD::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipHeat.class, EvolutionTooltipRenderer.HEAT::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipInfo.class, EvolutionTooltipRenderer.INFO::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipMass.class, TooltipMass::setup);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipMining.class, EvolutionTooltipRenderer.MINING::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipPrecision.class, EvolutionTooltipRenderer.PRECISION::setTooltip);
//        MinecraftForgeClient.registerTooltipComponentFactory(TooltipThrowSpeed.class, EvolutionTooltipRenderer.THROW_SPEED::setTooltip);
//    }

    private static void registerScreens() {
        MenuScreens.register(EvolutionContainers.EXTENDED_INVENTORY, ScreenInventory::new);
        MenuScreens.register(EvolutionContainers.CORPSE, ScreenCorpse::new);
    }

    public static void sendToServer(Packet<ServerGamePacketListener> packet) {
        assert mc.player != null;
        mc.player.connection.send(packet);
    }

    @Override
    public void onInitializeClient() {
        registerScreens();
        EvolutionRenderer.registryEntityRenders();
//        addTextures(event);
        EvolutionRenderLayer.setup();
        fixInputMappings();
//        registerTooltips();
        changeWorldOrders();
        VanillaOverlays.register();
        EvolutionOverlays.register();
//        registerModels(event);
//        ModelRegistry.register(event);
        ClientEvents.onFinishLoading();
    }
}
