package tgw.evolution.events;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Timer;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.ClientProxy;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.client.gui.*;
import tgw.evolution.client.gui.advancements.ScreenAdvancements;
import tgw.evolution.client.gui.config.ScreenModConfigSelection;
import tgw.evolution.client.gui.controls.ScreenKeyBinds;
import tgw.evolution.client.gui.stats.ScreenStats;
import tgw.evolution.client.gui.toast.ToastCustomRecipe;
import tgw.evolution.client.layers.LayerBack;
import tgw.evolution.client.layers.LayerBelt;
import tgw.evolution.client.models.ModelRegistry;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.renderer.ambient.LightTextureEv;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.client.util.EvolutionInput;
import tgw.evolution.client.util.LungeAttackInfo;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.InputHooks;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.*;
import tgw.evolution.inventory.extendedinventory.EvolutionRecipeBook;
import tgw.evolution.items.*;
import tgw.evolution.network.*;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.stats.EvolutionStatsCounter;
import tgw.evolution.util.AdvancedEntityRayTraceResult;
import tgw.evolution.util.HitInformation;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.constants.OptiFineHelper;
import tgw.evolution.util.hitbox.HitboxType;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.StaticFieldHandler;
import tgw.evolution.util.toast.ToastHolderRecipe;
import tgw.evolution.util.toast.Toasts;
import tgw.evolution.world.dimension.DimensionOverworld;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ClientEvents {

    public static final FieldHandler<Minecraft, Integer> MISS_TIME = new FieldHandler<>(Minecraft.class, "f_91078_");
    public static final ObjectList<ClientEffectInstance> EFFECTS_TO_ADD = new ObjectArrayList<>();
    public static final ObjectList<ClientEffectInstance> EFFECTS = new ObjectArrayList<>();
    public static final Int2ObjectMap<LungeChargeInfo> ABOUT_TO_LUNGE_PLAYERS = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<LungeAttackInfo> LUNGING_PLAYERS = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<ItemStack> BELT_ITEMS = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<ItemStack> BACK_ITEMS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Set<HitboxType>> MAINHAND_HITS = new Int2ObjectOpenHashMap<>();
    private static final BlockHitResult[] MAINHAND_HIT_RESULT = new BlockHitResult[1];
    private static final Map<PlayerRenderer, Object> INJECTED_PLAYER_RENDERERS = new WeakHashMap<>();
    private static final StaticFieldHandler<SkullBlockEntity, GameProfileCache> PROFILE_CACHE = new StaticFieldHandler<>(SkullBlockEntity.class,
                                                                                                                         "f_59755_");
    private static final StaticFieldHandler<SkullBlockEntity, MinecraftSessionService> SESSION_SERVICE =
            new StaticFieldHandler<>(SkullBlockEntity.class,
                                                                                                                                  "f_59756_");
    private static final FieldHandler<GameRenderer, LightTexture> LIGHT_TEXTURE = new FieldHandler<>(GameRenderer.class, "f_109074_");
    private static final FieldHandler<Minecraft, Timer> TIMER = new FieldHandler<>(Minecraft.class, "f_90991_");
    private static final FieldHandler<Timer, Float> MS_PER_TICK = new FieldHandler<>(Timer.class, "f_92521_");
    private static final FieldHandler<LocalPlayer, ClientRecipeBook> RECIPE_BOOK = new FieldHandler<>(LocalPlayer.class, "f_108592_");
    private static final FieldHandler<LocalPlayer, StatsCounter> STATS = new FieldHandler<>(LocalPlayer.class, "f_108591_");
    private static final StaticFieldHandler<DimensionSpecialEffects, Object2ObjectMap<ResourceLocation, DimensionSpecialEffects>> DIMENSION_EFFECTS = new StaticFieldHandler<>(
            DimensionSpecialEffects.class,
            "f_108857_");
    private static final FieldHandler<GameRenderer, Boolean> EFFECT_ACTIVE = new FieldHandler<>(GameRenderer.class, "f_109053_");
    private static ClientEvents instance;
    @Nullable
    private static IGuiScreenHandler handler;
    @Nullable
    private static Slot oldSelectedSlot;
    private static double accumulatedScrollDelta;
    private static boolean canDoLMBDrag;
    private static boolean canDoRMBDrag;
    private final Minecraft mc;
    private final ClientRenderer renderer;
    public int effectToAddTicks;
    public int jumpTicks;
    @Nullable
    public Entity leftPointedEntity;
    public EntityHitResult leftRayTrace;
    public int mainhandTimeSinceLastHit;
    public int offhandTimeSinceLastHit;
    @Nullable
    public Entity rightPointedEntity;
    public EntityHitResult rightRayTrace;
    private Vec3 cameraPos = Vec3.ZERO;
    private int currentShader;
    private int desiredShader;
    private DimensionOverworld dimension;
    private boolean initialized;
    private boolean inverted;
    private boolean isJumpPressed;
    private boolean isMainhandCustomAttacking;
    private boolean isOffhandCustomAttacking;
    private boolean isPreviousProned;
    private boolean isSneakPressed;
    private boolean lunging;
    private GameRenderer oldGameRenderer;
    private CameraType previousCameraType;
    private boolean previousPressed;
    private boolean proneToggle;
    private boolean sneakpreviousPressed;
    private int ticks;
    private float tps = 20.0f;
    private int warmUpTicks;
    /**
     * Bit 0 to 7: whether the player was in water in the last 8 frames, 0 being the most recent, 7 being the least recent.
     */
    private byte wasInWater;

    public ClientEvents(Minecraft mc) {
        this.mc = mc;
        instance = this;
        this.renderer = new ClientRenderer(mc, this);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static void addConfigSetToMap(ModContainer container, ModConfig.Type type, Map<ModConfig.Type, Set<ModConfig>> configMap) {
        if (type == ModConfig.Type.CLIENT && OptiFineHelper.isLoaded() && "forge".equals(container.getModId())) {
            Evolution.info("Ignoring Forge's client config since OptiFine was detected");
            return;
        }
        Set<ModConfig> configSet = getConfigSets().get(type);
        synchronized (configSet) {
            Set<ModConfig> filteredConfigSets = configSet.stream()
                                                         .filter(config -> config.getModId().equals(container.getModId()))
                                                         .collect(Collectors.toSet());
            if (!filteredConfigSets.isEmpty()) {
                configMap.put(type, filteredConfigSets);
            }
        }
    }

    public static void addLungingPlayer(int entityId, InteractionHand hand) {
        LungeChargeInfo lungeCharge = ABOUT_TO_LUNGE_PLAYERS.get(entityId);
        if (lungeCharge != null) {
            lungeCharge.resetHand(hand);
        }
        LungeAttackInfo lungeAttack = LUNGING_PLAYERS.get(entityId);
        if (lungeAttack == null) {
            LUNGING_PLAYERS.put(entityId, new LungeAttackInfo(hand));
        }
        else {
            lungeAttack.addInfo(hand);
        }
    }

    private static boolean areStacksCompatible(ItemStack a, ItemStack b) {
        return a.isEmpty() || b.isEmpty() || a.sameItem(b) && ItemStack.tagMatches(a, b);
    }

    public static boolean containsEffect(List<ClientEffectInstance> list, MobEffect effect) {
        for (ClientEffectInstance instance : list) {
            if (instance.getEffect() == effect) {
                return true;
            }
        }
        return false;
    }

    private static Map<ModConfig.Type, Set<ModConfig>> createConfigMap(ModContainer container) {
        Map<ModConfig.Type, Set<ModConfig>> modConfigMap = new EnumMap<>(ModConfig.Type.class);
        addConfigSetToMap(container, ModConfig.Type.CLIENT, modConfigMap);
        addConfigSetToMap(container, ModConfig.Type.COMMON, modConfigMap);
        addConfigSetToMap(container, ModConfig.Type.SERVER, modConfigMap);
        return modConfigMap;
    }

    @Nullable
    private static IGuiScreenHandler findHandler(Screen currentScreen) {
        if (currentScreen instanceof CreativeModeInventoryScreen creativeScreen) {
            return new GuiContainerCreativeHandler(creativeScreen);
        }
        if (currentScreen instanceof AbstractContainerScreen containerScreen) {
            return new GuiContainerHandler(containerScreen);
        }
        return null;
    }

    public static void fixInputMappings() {
        FieldHandler<InputConstants.Type, BiFunction<Integer, String, Component>> displayTextSupplier = new FieldHandler<>(InputConstants.Type.class,
                                                                                                                           "f_84887_");
        displayTextSupplier.set(InputConstants.Type.KEYSYM, (keyCode, translationKey) -> {
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

    private static EnumMap<ModConfig.Type, Set<ModConfig>> getConfigSets() {
        return ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configSets");
    }

    public static int getIndexAndRemove(List<ClientEffectInstance> list, MobEffect effect) {
        Iterator<ClientEffectInstance> iterator = list.iterator();
        int i = -1;
        while (iterator.hasNext()) {
            i++;
            if (iterator.next().getEffect() == effect) {
                iterator.remove();
                return i;
            }
        }
        return -1;
    }

    public static ClientEvents getInstance() {
        return instance;
    }

    private static float getRightCooldownPeriod(IOffhandAttackable item, ItemStack stack) {
        double attackSpeed = item.getAttackSpeed(stack) + PlayerHelper.ATTACK_SPEED;
        return (float) (1 / attackSpeed * 20);
    }

    public static void onFinishLoading() {
        Evolution.info("Creating config GUI factories...");
        ModList.get().forEachModContainer((modId, container) -> {
            if (container.getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class).isPresent()) {
                return;
            }
            Map<ModConfig.Type, Set<ModConfig>> modConfigMap = createConfigMap(container);
            if (!modConfigMap.isEmpty()) {
                Evolution.info("Registering config factory for mod {}. Found {} client config(s) and {} common config(s)",
                               modId,
                               modConfigMap.getOrDefault(ModConfig.Type.CLIENT, Collections.emptySet()).size(),
                               modConfigMap.getOrDefault(ModConfig.Type.COMMON, Collections.emptySet()).size());
                String displayName = container.getModInfo().getDisplayName();
                container.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                                                 () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new ScreenModConfigSelection(screen,
                                                                                                                                          displayName,
                                                                                                                                          modConfigMap)));
            }
        });
    }

    public static void onGuiOpen(Screen newScreen) {
        handler = null;
        oldSelectedSlot = null;
        accumulatedScrollDelta = 0;
        canDoLMBDrag = false;
        canDoRMBDrag = false;
        if (newScreen != null) {
            handler = findHandler(newScreen);
        }
    }

    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
        ModelRegistry.register(event);
    }

    public static boolean removeEffect(List<ClientEffectInstance> list, MobEffect effect) {
        Iterator<ClientEffectInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getEffect() == effect) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public static void removePotionEffect(MobEffect effect) {
        removeEffect(EFFECTS, effect);
        if (removeEffect(EFFECTS_TO_ADD, effect)) {
            instance.effectToAddTicks = 0;
        }
    }

    private static void rmbTweakNewSlot(Slot selectedSlot, ItemStack stackOnMouse) {
        assert selectedSlot != null;
        assert !stackOnMouse.isEmpty();
        if (handler.isIgnored(selectedSlot)) {
            return;
        }
        if (handler.isCraftingOutput(selectedSlot)) {
            return;
        }
        ItemStack selectedSlotStack = selectedSlot.getItem();
        if (!areStacksCompatible(selectedSlotStack, stackOnMouse)) {
            return;
        }
        if (selectedSlotStack.getCount() == selectedSlotStack.getMaxStackSize()) {
            return;
        }
        handler.clickSlot(selectedSlot, MouseButton.RIGHT, false);
    }

    public void addCustomRecipeToast(int id) {
        for (ToastHolderRecipe toast : Toasts.getHolderForId(id)) {
            ToastCustomRecipe.addOrUpdate(this.mc.getToasts(), toast);
        }
    }

    public boolean areControlsInverted() {
        return this.inverted;
    }

    public void clearMemory() {
        EFFECTS.clear();
        EFFECTS_TO_ADD.clear();
        this.inverted = false;
        ABOUT_TO_LUNGE_PLAYERS.clear();
        LUNGING_PLAYERS.clear();
        if (this.mc.level == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            if (((IMinecraftPatch) this.mc).isMultiplayerPaused()) {
                Evolution.info("Resuming client");
                ((IMinecraftPatch) Minecraft.getInstance()).setMultiplayerPaused(false);
            }
            this.warmUpTicks = 0;
        }
        if (this.dimension != null) {
            this.dimension = null;
            System.gc();
        }
    }

    @Nullable
    private Slot findPullSlot(List<Slot> slots, Slot selectedSlot) {
        int startIndex = 0;
        int endIndex = slots.size();
        int direction = 1;
        ItemStack selectedSlotStack = selectedSlot.getItem();
        boolean findInPlayerInventory = selectedSlot.container != this.mc.player.getInventory();
        for (int i = startIndex; i != endIndex; i += direction) {
            Slot slot = slots.get(i);
            if (handler.isIgnored(slot)) {
                continue;
            }
            boolean slotInPlayerInventory = slot.container == this.mc.player.getInventory();
            if (findInPlayerInventory != slotInPlayerInventory) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            if (!areStacksCompatible(selectedSlotStack, stack)) {
                continue;
            }
            return slot;
        }
        return null;
    }

    @Nullable
    private List<Slot> findPushSlots(List<Slot> slots, Slot selectedSlot, int itemCount, boolean mustDistributeAll) {
        ItemStack selectedSlotStack = selectedSlot.getItem();
        boolean findInPlayerInventory = selectedSlot.container != this.mc.player.getInventory();
        List<Slot> rv = new ArrayList<>();
        List<Slot> goodEmptySlots = new ArrayList<>();
        for (int i = 0; i != slots.size() && itemCount > 0; i++) {
            Slot slot = slots.get(i);
            if (handler.isIgnored(slot)) {
                continue;
            }
            boolean slotInPlayerInventory = slot.container == this.mc.player.getInventory();
            if (findInPlayerInventory != slotInPlayerInventory) {
                continue;
            }
            if (handler.isCraftingOutput(slot)) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                if (slot.mayPlace(selectedSlotStack)) {
                    goodEmptySlots.add(slot);
                }
            }
            else {
                if (areStacksCompatible(selectedSlotStack, stack) && stack.getCount() < stack.getMaxStackSize()) {
                    rv.add(slot);
                    itemCount -= Math.min(itemCount, stack.getMaxStackSize() - stack.getCount());
                }
            }
        }
        for (int i = 0; i != goodEmptySlots.size() && itemCount > 0; i++) {
            Slot slot = goodEmptySlots.get(i);
            rv.add(slot);
            itemCount -= Math.min(itemCount, slot.getItem().getMaxStackSize() - slot.getItem().getCount());
        }
        if (mustDistributeAll && itemCount > 0) {
            return null;
        }
        return rv;
    }

    public Vec3 getCameraPos() {
        return this.cameraPos;
    }

    public float getCurrentItemAttackStrengthDelay() {
        ItemStack stack = this.mc.player.getMainHandItem();
        Item item = stack.getItem();
        if (item instanceof ItemModularTool) {
            return (float) (1 / ((ItemModularTool) item).getAttackSpeed(stack) * 20);
        }
        return (float) (1 / PlayerHelper.ATTACK_SPEED * 20);
    }

    public DimensionOverworld getDimension() {
        return this.dimension;
    }

    public short getDizzinessAmplifier() {
        if (this.isPlayerDizzy()) {
            return (short) this.mc.player.getEffect(EvolutionEffects.DIZZINESS.get()).getAmplifier();
        }
        return 0;
    }

    public float getMainhandCooledAttackStrength(float partialTicks) {
        return MathHelper.clamp((this.mainhandTimeSinceLastHit + partialTicks) / this.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
    }

    public float getOffhandCooledAttackStrength(ItemStack stack, float adjustTicks) {
        if (!(stack.getItem() instanceof IOffhandAttackable offhandAttackable)) {
            float cooldown = (float) (1.0 / PlayerHelper.ATTACK_SPEED * 20.0);
            return MathHelper.clamp((this.offhandTimeSinceLastHit + adjustTicks) / cooldown, 0.0F, 1.0F);
        }
        return MathHelper.clamp((this.offhandTimeSinceLastHit + adjustTicks) / getRightCooldownPeriod(offhandAttackable, stack), 0.0F, 1.0F);
    }

    public ClientRenderer getRenderer() {
        return this.renderer;
    }

    @Nullable
    public ResourceLocation getShader(int shaderId) {
        return switch (shaderId) {
            case 1 -> EvolutionResources.SHADER_MOTION_BLUR;
            case 25 -> EvolutionResources.SHADER_DESATURATE_25;
            case 50 -> EvolutionResources.SHADER_DESATURATE_50;
            case 75 -> EvolutionResources.SHADER_DESATURATE_75;
            default -> null;
        };
    }

    public int getTickCount() {
        return this.ticks;
    }

    public void handleShaderPacket(int shaderId) {
        switch (shaderId) {
            case PacketSCShader.QUERY -> {
                this.mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.query", this.currentShader), false);
                return;
            }
            case PacketSCShader.TOGGLE -> {
                this.mc.gameRenderer.togglePostEffect();
                if (EFFECT_ACTIVE.get(this.mc.gameRenderer)) {
                    this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_ON, false);
                }
                else {
                    this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_OFF, false);
                }
                return;
            }
            case 0 -> {
                this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_RESET, false);
                this.desiredShader = 0;
                return;
            }
        }
        if (this.hasShader(shaderId)) {
            this.mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.success", shaderId), false);
            this.desiredShader = shaderId;
        }
        else {
            this.mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.fail", shaderId).withStyle(ChatFormatting.RED),
                                                false);
        }
    }

    public boolean hasCtrlDown() {
        return Screen.hasControlDown();
    }

    public boolean hasShader(int shaderId) {
        return this.getShader(shaderId) != null;
    }

    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public void init() {
        //Bind Sky Renderer
        this.dimension = new DimensionOverworld();
        Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> renderInfos = DIMENSION_EFFECTS.get();
        DimensionSpecialEffects overworldRenderInfo = renderInfos.get(DimensionType.OVERWORLD_EFFECTS);
        overworldRenderInfo.setSkyRenderHandler(new SkyRenderer(this.dimension));
        //Load skin for corpses
        GameProfileCache playerProfile = PROFILE_CACHE.get();
        MinecraftSessionService session = SESSION_SERVICE.get();
        if (playerProfile != null && session != null) {
            EntityPlayerCorpse.setProfileCache(playerProfile);
            EntityPlayerCorpse.setSessionService(session);
        }
        //Replace MovementInput
        this.mc.player.input = new EvolutionInput(this.mc.options);
    }

    public boolean isMainhandCustomAttacking() {
        return this.isMainhandCustomAttacking;
    }

    public boolean isOffhandCustomAttacking() {
        return this.isOffhandCustomAttacking;
    }

    public boolean isPlayerDizzy() {
        if (this.mc.player != null) {
            return this.mc.player.hasEffect(EvolutionEffects.DIZZINESS.get());
        }
        return false;
    }

    public void leftMouseClick() {
        if (this.mainhandTimeSinceLastHit >= this.getCurrentItemAttackStrengthDelay()) {
            this.mainhandTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getLocation().y : Double.NaN;
            if (this.leftRayTrace instanceof AdvancedEntityRayTraceResult) {
                HitboxType part = HitboxType.ALL;
                if (((AdvancedEntityRayTraceResult) this.leftRayTrace).getHitbox() != null) {
                    part = ((AdvancedEntityRayTraceResult) this.leftRayTrace).getHitbox().getPart();
                }
                Evolution.debug("Part = {}", part);
            }
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.leftPointedEntity, InteractionHand.MAIN_HAND, rayTraceY));
            this.swingArm(InteractionHand.MAIN_HAND);
        }
    }

    @SubscribeEvent
    public void onChatRender(RenderGameOverlayEvent.Chat event) {
        event.setPosY(event.getPosY() - 10);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        this.mc.getProfiler().push("evolution");
        this.mc.getProfiler().push("init");
        //Turn auto-jump off
        this.mc.options.autoJump = false;
        if (this.mc.player == null) {
            this.initialized = false;
            this.clearMemory();
            this.mc.getProfiler().pop();
            this.mc.getProfiler().pop();
            return;
        }
        if (this.mc.level == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            ABOUT_TO_LUNGE_PLAYERS.clear();
            LUNGING_PLAYERS.clear();
        }
        if (!this.initialized) {
            this.init();
            this.initialized = true;
        }
        this.mc.getProfiler().pop();
        //Runs at the start of each tick
        if (event.phase == TickEvent.Phase.START) {
            //Jump
            this.mc.getProfiler().push("jump");
            if (this.jumpTicks > 0) {
                this.jumpTicks--;
            }
            if (this.mc.player.isOnGround()) {
                this.jumpTicks = 0;
            }
            //Apply shaders
            this.mc.getProfiler().popPush("shaders");
            int shader = 0;
            if (this.mc.options.getCameraType() == CameraType.FIRST_PERSON && !this.mc.player.isCreative() && !this.mc.player.isSpectator()) {
                float health = this.mc.player.getHealth();
                if (health <= 12.5f) {
                    shader = 25;
                }
                else if (health <= 25) {
                    shader = 50;
                }
                else if (health <= 50) {
                    shader = 75;
                }
            }
            if (this.desiredShader == 0) {
                if (shader != this.currentShader) {
                    this.currentShader = shader;
                    if (shader == 0) {
                        this.mc.gameRenderer.shutdownEffect();
                    }
                    else {
                        ResourceLocation shaderLoc = this.getShader(shader);
                        if (shaderLoc != null) {
                            this.mc.gameRenderer.loadEffect(shaderLoc);
                        }
                        else {
                            Evolution.warn("Unregistered shader id: {}", shader);
                        }
                    }
                }
            }
            else {
                if (this.mc.options.getCameraType() != this.previousCameraType) {
                    this.previousCameraType = this.mc.options.getCameraType();
                    this.currentShader = 0;
                }
                if (this.desiredShader != this.currentShader) {
                    this.currentShader = this.desiredShader;
                    ResourceLocation shaderLoc = this.getShader(this.desiredShader);
                    if (shaderLoc != null) {
                        this.mc.gameRenderer.loadEffect(shaderLoc);
                    }
                    else {
                        Evolution.warn("Unregistered shader id: {}", this.desiredShader);
                    }
                }
            }
            this.mc.getProfiler().popPush("lightMap");
            GameRenderer gameRenderer = this.mc.gameRenderer;
            if (gameRenderer != this.oldGameRenderer) {
                this.oldGameRenderer = gameRenderer;
                LIGHT_TEXTURE.set(this.oldGameRenderer, new LightTextureEv(this.oldGameRenderer, this.mc));
            }
            this.mc.getProfiler().pop();
            if (!this.mc.isPaused()) {
                this.mc.getProfiler().push("dimension");
                this.dimension.tick();
                this.mc.getProfiler().popPush("renderer");
                this.renderer.startTick();
                this.mc.getProfiler().popPush("lunge");
                ABOUT_TO_LUNGE_PLAYERS.int2ObjectEntrySet().removeIf(entry -> entry.getValue().shouldBeRemoved());
                ABOUT_TO_LUNGE_PLAYERS.forEach((key, value) -> value.tick());
                LUNGING_PLAYERS.int2ObjectEntrySet().removeIf(entry -> entry.getValue().shouldBeRemoved());
                LUNGING_PLAYERS.forEach((key, value) -> value.tick());
                this.mc.getProfiler().pop();
                InputHooks.parryCooldownTick();
                this.mc.getProfiler().push("updateHeld");
                this.updateBeltItem();
                this.updateBackItem();
                this.mc.getProfiler().popPush("prone");
                //Resets cooldown when proning
                boolean isProned = this.mc.player.getPose() == Pose.SWIMMING && !this.mc.player.isInWater();
                if (this.isPreviousProned != isProned) {
                    this.mainhandTimeSinceLastHit = 0;
                    this.offhandTimeSinceLastHit = 0;
                    this.isPreviousProned = isProned;
                }
                this.mc.getProfiler().popPush("effects");
                //Handle Disoriented Effect
                if (this.mc.player.hasEffect(EvolutionEffects.DISORIENTED.get())) {
                    if (!this.inverted) {
                        this.inverted = true;
                    }
                }
                else {
                    if (this.inverted) {
                        this.inverted = false;
                    }
                }
                //Handle two-handed items
                this.mc.getProfiler().popPush("twoHanded");
                ItemStack mainHandStack = this.mc.player.getMainHandItem();
                if (mainHandStack.getItem() instanceof ITwoHanded twoHanded &&
                    twoHanded.isTwoHanded(mainHandStack) &&
                    !this.mc.player.getOffhandItem().isEmpty()) {
                    this.mainhandTimeSinceLastHit = 0;
                    MISS_TIME.set(this.mc, Integer.MAX_VALUE);
                    this.mc.player.displayClientMessage(EvolutionTexts.ACTION_TWO_HANDED, true);
                }
                //Prevents the player from attacking if on cooldown
                this.mc.getProfiler().popPush("cooldown");
                if (this.getMainhandCooledAttackStrength(0.0F) != 1 &&
                    this.mc.hitResult != null &&
                    this.mc.hitResult.getType() != HitResult.Type.BLOCK) {
                    MISS_TIME.set(this.mc, Integer.MAX_VALUE);
                }
                this.mc.getProfiler().pop();
            }
        }
        //Runs at the end of each tick
        else if (event.phase == TickEvent.Phase.END) {
            if (!this.mc.isPaused()) {
                this.warmUpTicks++;
                //Remove inactive effects
                this.mc.getProfiler().push("effects");
                if (!EFFECTS.isEmpty()) {
                    Iterator<ClientEffectInstance> iterator = EFFECTS.iterator();
                    while (iterator.hasNext()) {
                        ClientEffectInstance instance = iterator.next();
                        MobEffect effect = instance.getEffect();
                        if (instance.getDuration() == 0 || !this.mc.player.hasEffect(effect) && this.warmUpTicks >= 100) {
                            iterator.remove();
                        }
                        else {
                            instance.tick();
                        }
                    }
                }
                if (!EFFECTS_TO_ADD.isEmpty()) {
                    this.effectToAddTicks++;
                }
                else {
                    this.renderer.isAddingEffect = false;
                }
                this.mc.getProfiler().popPush("prone");
                //Proning
                boolean pressed = ClientProxy.TOGGLE_PRONE.isDown();
                if (pressed && !this.previousPressed) {
                    this.proneToggle = !this.proneToggle;
                }
                this.previousPressed = pressed;
                this.updateClientProneState(this.mc.player);
                //Sneak on ladders
                this.mc.getProfiler().popPush("ladders");
                if (this.mc.player.onClimbable()) {
                    if (this.isSneakPressed && !this.sneakpreviousPressed) {
                        this.sneakpreviousPressed = true;
                        this.mc.player.setDeltaMovement(Vec3.ZERO);
                    }
                }
                //Handle creative features
                this.mc.getProfiler().popPush("creative");
                if (this.mc.player.isCreative() && ClientProxy.BUILDING_ASSIST.isDown()) {
                    if (this.mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                        if (this.mc.hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos pos = ((BlockHitResult) this.mc.hitResult).getBlockPos();
                            if (!this.mc.level.getBlockState(pos).isAir()) {
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSChangeBlock((BlockHitResult) this.mc.hitResult));
                                this.swingArm(InteractionHand.MAIN_HAND);
                                this.mc.player.swing(InteractionHand.MAIN_HAND);
                            }
                        }
                    }
                }
                //Handle swing
                this.mc.getProfiler().popPush("swing");
                this.ticks++;
                if (this.mc.gameMode.isDestroying()) {
                    this.swingArm(InteractionHand.MAIN_HAND);
                }
                this.lunging = false;
                this.isMainhandCustomAttacking = ((ILivingEntityPatch) this.mc.player).isMainhandCustomAttacking();
                this.isOffhandCustomAttacking = ((ILivingEntityPatch) this.mc.player).isOffhandCustomAttacking();
                if (this.isMainhandCustomAttacking) {
                    List<HitInformation> hitInfos = MathHelper.collideOBBWithCollider(this.mc.player,
                                                                                      ((IEntityPatch) this.mc.player).getHitboxes()
                                                                                                                     .getEquipmentFor(((ILivingEntityPatch) this.mc.player).getMainhandCustomAttackType(),
                                                                                                                                      InteractionHand.MAIN_HAND),
                                                                                      1.0f,
                                                                                      MAINHAND_HIT_RESULT,
                                                                                      ((ILivingEntityPatch) this.mc.player).getMainhandCustomAttackTicks() >
                                                                                      3);
                    if (!hitInfos.isEmpty()) {
                        for (HitInformation hitInfo : hitInfos) {
                            Evolution.info("Collided with {} on {}",
                                           hitInfo.getEntity(),
                                           Arrays.toString(hitInfo.getHitboxes().toArray(HitboxType[]::new)));
                            Set<HitboxType> boxes = MAINHAND_HITS.get(hitInfo.getEntity().getId());
                            if (boxes == null) {
                                boxes = EnumSet.noneOf(HitboxType.class);
                                boxes.addAll(hitInfo.getHitboxes());
                                //noinspection ObjectAllocationInLoop
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSHitInformation(hitInfo.getEntity(),
                                                                                                  InteractionHand.MAIN_HAND,
                                                                                                  hitInfo.getHitboxes().toArray(HitboxType[]::new)));
                            }
                            else {
                                for (HitboxType box : hitInfo.getHitboxes()) {
                                    if (!boxes.contains(box)) {
                                        boxes.add(box);
                                        //noinspection ObjectAllocationInLoop
                                        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSHitInformation(hitInfo.getEntity(),
                                                                                                          InteractionHand.MAIN_HAND,
                                                                                                          box));
                                    }
                                }
                            }
                            MAINHAND_HITS.put(hitInfo.getEntity().getId(), boxes);
                        }
                    }
                    if (MAINHAND_HIT_RESULT[0] != null && MAINHAND_HIT_RESULT[0].getType() != HitResult.Type.MISS) {
                        ((ILivingEntityPatch) this.mc.player).stopMainhandCustomAttack(ICustomAttack.StopReason.HIT_BLOCK);
                    }
                }
                else {
                    MAINHAND_HITS.clear();
                    MAINHAND_HIT_RESULT[0] = null;
                }
                //Ticks renderer
                this.mc.getProfiler().popPush("renderer");
                this.renderer.endTick();
                this.mc.getProfiler().pop();
            }
        }
        this.mc.getProfiler().pop();
    }

    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof LocalPlayer && event.getEntity().equals(this.mc.player)) {
            STATS.set(this.mc.player, new EvolutionStatsCounter());
            RECIPE_BOOK.set(this.mc.player, new EvolutionRecipeBook());
        }
    }

    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        this.renderer.renderFog(event);
    }

    @SubscribeEvent
    public void onGUIMouseClickedPre(ScreenEvent.MouseClickedEvent.Pre event) {
        MouseButton button = MouseButton.fromGLFW(event.getButton());
        if (button != null) {
            if (this.onMouseClicked(event.getMouseX(), event.getMouseY(), button)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGUIMouseDragPre(ScreenEvent.MouseDragEvent.Pre event) {
        MouseButton button = MouseButton.fromGLFW(event.getMouseButton());
        if (button != null) {
            if (this.onMouseDrag(event.getMouseX(), event.getMouseY(), button)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGUIMouseReleasedPre(ScreenEvent.MouseReleasedEvent.Pre event) {
        MouseButton button = MouseButton.fromGLFW(event.getButton());
        if (button != null) {
            if (this.onMouseReleased(button)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGUIMouseScrollPost(ScreenEvent.MouseScrollEvent.Post event) {
        if (this.onMouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGUIOpen(ScreenOpenEvent event) {
        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen) {
            event.setCanceled(true);
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSOpenExtendedInventory());
        }
        else if (screen instanceof AdvancementsScreen) {
            event.setScreen(new ScreenAdvancements(this.mc.getConnection().getAdvancements()));
        }
        else if (screen instanceof KeyBindsScreen && !(screen instanceof ScreenKeyBinds)) {
            event.setScreen(new ScreenKeyBinds((KeyBindsScreen) screen, this.mc.options));
        }
        else if (screen instanceof StatsScreen) {
            event.setScreen(new ScreenStats(this.mc.player.getStats()));
        }
        else if (screen instanceof ModListScreen) {
            event.setScreen(new ScreenModList());
        }
        if (!event.isCanceled()) {
            onGuiOpen(event.getScreen());
        }
    }

    @SubscribeEvent
    public void onGUIPostInit(ScreenEvent.InitScreenEvent.Post event) {
        if (event.getScreen() instanceof PauseScreen) {
            if (!event.getListenersList().isEmpty()) {
                AbstractButton shareToLan = (AbstractButton) event.getListenersList().get(6);
                event.addListener(new Button(shareToLan.x,
                                             shareToLan.y + 24,
                                             shareToLan.getWidth(),
                                             shareToLan.getHeight(),
                                             EvolutionTexts.GUI_MENU_MOD_OPTIONS,
                                             button -> this.mc.setScreen(new ModListScreen(event.getScreen()))));
                shareToLan.x = event.getScreen().width / 2 - 102;
                shareToLan.setWidth(204);
                AbstractButton returnToMenu = (AbstractButton) event.getListenersList().get(7);
                returnToMenu.y += 24;
                AbstractButton menuOptions = (AbstractButton) event.getListenersList().get(5);
                menuOptions.y += 24;
                GuiEventListener feedback = event.getListenersList().get(3);
                GuiEventListener bugs = event.getListenersList().get(4);
                event.removeListener(feedback);
                event.removeListener(bugs);
                String feedbackLink = "https://github.com/MGSchultz-13/Evolution/discussions/categories/feedback";
                feedback = new Button(event.getScreen().width / 2 - 102,
                                      event.getScreen().height / 4 + 72 - 16,
                                      98,
                                      20,
                                      EvolutionTexts.GUI_MENU_SEND_FEEDBACK,
                                      button -> this.mc.setScreen(new ConfirmLinkScreen(b -> {
                                          if (b) {
                                              Util.getPlatform().openUri(feedbackLink);
                                          }
                                          this.mc.setScreen(event.getScreen());
                                      }, feedbackLink, true)));
                String bugsLink = "https://github.com/MGSchultz-13/Evolution/issues";
                bugs = new Button(event.getScreen().width / 2 + 4,
                                  event.getScreen().height / 4 + 72 - 16,
                                  98,
                                  20,
                                  EvolutionTexts.GUI_MENU_REPORT_BUGS,
                                  button -> this.mc.setScreen(new ConfirmLinkScreen(b -> {
                                      if (b) {
                                          Util.getPlatform().openUri(bugsLink);
                                      }
                                      this.mc.setScreen(event.getScreen());
                                  }, bugsLink, true)));
                event.addListener(feedback);
                event.addListener(bugs);
            }
        }
        else if (event.getScreen() instanceof ControlsScreen) {
            GuiEventListener autoJumpButton = event.getListenersList().get(4);
            event.removeListener(autoJumpButton);
        }
    }

    public boolean onMouseClicked(double x, double y, MouseButton button) {
        if (handler == null) {
            return false;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(x, y);
        oldSelectedSlot = selectedSlot;
        ItemStack stackOnMouse = this.mc.player.inventoryMenu.getCarried();
        if (button == MouseButton.LEFT) {
            if (stackOnMouse.isEmpty()) {
                canDoLMBDrag = true;
            }
        }
        else if (button == MouseButton.RIGHT) {
            if (stackOnMouse.isEmpty()) {
                return false;
            }
            canDoRMBDrag = true;
            if (selectedSlot != null) {
                rmbTweakNewSlot(selectedSlot, stackOnMouse);
            }
            return true;
        }
        return false;
    }

    public boolean onMouseDrag(double x, double y, MouseButton button) {
        if (handler == null) {
            return false;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(x, y);
        if (selectedSlot == oldSelectedSlot) {
            return false;
        }
        oldSelectedSlot = selectedSlot;
        if (selectedSlot == null) {
            return false;
        }
        if (handler.isIgnored(selectedSlot)) {
            return false;
        }
        ItemStack stackOnMouse = this.mc.player.inventoryMenu.getCarried();
        if (button == MouseButton.LEFT) {
            if (!canDoLMBDrag) {
                return false;
            }
            ItemStack selectedSlotStack = selectedSlot.getItem();
            if (selectedSlotStack.isEmpty()) {
                return false;
            }
            boolean shiftIsDown = this.hasShiftDown();
            if (stackOnMouse.isEmpty()) {
                if (!shiftIsDown) {
                    return false;
                }
                handler.clickSlot(selectedSlot, MouseButton.LEFT, true);
            }
            else {
                if (!areStacksCompatible(selectedSlotStack, stackOnMouse)) {
                    return false;
                }
                if (shiftIsDown) {
                    handler.clickSlot(selectedSlot, MouseButton.LEFT, true);
                }
                else {
                    if (stackOnMouse.getCount() + selectedSlotStack.getCount() > stackOnMouse.getMaxStackSize()) {
                        return false;
                    }
                    handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                    if (!handler.isCraftingOutput(selectedSlot)) {
                        handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                    }
                }
            }
        }
        else if (button == MouseButton.RIGHT) {
            if (!canDoRMBDrag) {
                return false;
            }
            if (stackOnMouse.isEmpty()) {
                return false;
            }
            rmbTweakNewSlot(selectedSlot, stackOnMouse);
        }
        return false;
    }

    public boolean onMouseReleased(MouseButton button) {
        if (handler == null) {
            return false;
        }
        if (button == MouseButton.LEFT) {
            canDoLMBDrag = false;
        }
        else if (button == MouseButton.RIGHT) {
            if (canDoRMBDrag) {
                canDoRMBDrag = false;
                return true;
            }
        }
        return false;
    }

    public boolean onMouseScrolled(double x, double y, double scrollDelta) {
        if (handler == null) {
            return false;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(x, y);
        if (selectedSlot == null || handler.isIgnored(selectedSlot)) {
            return false;
        }
        double scaledDelta = Math.signum(scrollDelta);
        if (accumulatedScrollDelta != 0 && scaledDelta != Math.signum(accumulatedScrollDelta)) {
            accumulatedScrollDelta = 0;
        }
        accumulatedScrollDelta += scaledDelta;
        int delta = (int) accumulatedScrollDelta;
        accumulatedScrollDelta -= delta;
        if (delta == 0) {
            return true;
        }
        List<Slot> slots = handler.getSlots();
        ItemStack selectedSlotStack = selectedSlot.getItem();
        if (selectedSlotStack.isEmpty()) {
            return true;
        }
        ItemStack stackOnMouse = this.mc.player.inventoryMenu.getCarried();
        int numItemsToMove = Math.abs(delta);
        boolean pushItems = delta < 0;
        if (handler.isCraftingOutput(selectedSlot)) {
            if (!areStacksCompatible(selectedSlotStack, stackOnMouse)) {
                return true;
            }
            if (stackOnMouse.isEmpty()) {
                if (!pushItems) {
                    return true;
                }
                while (numItemsToMove-- > 0) {
                    List<Slot> targetSlots = this.findPushSlots(slots, selectedSlot, selectedSlotStack.getCount(), true);
                    if (targetSlots == null) {
                        break;
                    }
                    handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                    for (int i = 0; i < targetSlots.size(); i++) {
                        Slot slot = targetSlots.get(i);
                        if (i == targetSlots.size() - 1) {
                            handler.clickSlot(slot, MouseButton.LEFT, false);
                        }
                        else {
                            int clickTimes = slot.getItem().getMaxStackSize() - slot.getItem().getCount();
                            while (clickTimes-- > 0) {
                                handler.clickSlot(slot, MouseButton.RIGHT, false);
                            }
                        }
                    }
                }
            }
            else {
                while (numItemsToMove-- > 0) {
                    handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                }
            }
            return true;
        }
        if (!stackOnMouse.isEmpty() && areStacksCompatible(selectedSlotStack, stackOnMouse)) {
            return true;
        }
        if (pushItems) {
            if (!stackOnMouse.isEmpty() && !selectedSlot.mayPlace(stackOnMouse)) {
                return true;
            }
            numItemsToMove = Math.min(numItemsToMove, selectedSlotStack.getCount());
            List<Slot> targetSlots = this.findPushSlots(slots, selectedSlot, numItemsToMove, false);
            assert targetSlots != null;
            if (targetSlots.isEmpty()) {
                return true;
            }
            handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
            for (Slot slot : targetSlots) {
                int clickTimes = slot.getItem().getMaxStackSize() - slot.getItem().getCount();
                clickTimes = Math.min(clickTimes, numItemsToMove);
                numItemsToMove -= clickTimes;
                while (clickTimes-- > 0) {
                    handler.clickSlot(slot, MouseButton.RIGHT, false);
                }
            }
            handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
            return true;
        }
        int maxItemsToMove = selectedSlotStack.getMaxStackSize() - selectedSlotStack.getCount();
        numItemsToMove = Math.min(numItemsToMove, maxItemsToMove);
        while (numItemsToMove > 0) {
            Slot targetSlot = this.findPullSlot(slots, selectedSlot);
            if (targetSlot == null) {
                break;
            }
            int numItemsInTargetSlot = targetSlot.getItem().getCount();
            if (handler.isCraftingOutput(targetSlot)) {
                if (maxItemsToMove < numItemsInTargetSlot) {
                    break;
                }
                maxItemsToMove -= numItemsInTargetSlot;
                if (!stackOnMouse.isEmpty() && !selectedSlot.mayPlace(stackOnMouse)) {
                    break;
                }
                handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                handler.clickSlot(targetSlot, MouseButton.LEFT, false);
                handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
                continue;
            }
            int numItemsToMoveFromTargetSlot = Math.min(numItemsToMove, numItemsInTargetSlot);
            maxItemsToMove -= numItemsToMoveFromTargetSlot;
            numItemsToMove -= numItemsToMoveFromTargetSlot;
            if (!stackOnMouse.isEmpty() && !targetSlot.mayPlace(stackOnMouse)) {
                break;
            }
            handler.clickSlot(targetSlot, MouseButton.LEFT, false);
            if (numItemsToMoveFromTargetSlot == numItemsInTargetSlot) {
                handler.clickSlot(selectedSlot, MouseButton.LEFT, false);
            }
            else {
                for (int i = 0; i < numItemsToMoveFromTargetSlot; i++) {
                    handler.clickSlot(selectedSlot, MouseButton.RIGHT, false);
                }
            }
            handler.clickSlot(targetSlot, MouseButton.LEFT, false);
        }
        return true;
    }

    @SubscribeEvent
    public void onPlayerInput(MovementInputUpdateEvent event) {
        Input movementInput = event.getInput();
        this.isJumpPressed = movementInput.jumping;
        this.isSneakPressed = movementInput.shiftKeyDown;
        if (!this.isSneakPressed) {
            this.sneakpreviousPressed = false;
        }
        if (this.mc.player.getPose() == Pose.SWIMMING && !this.mc.player.isInWater() && !this.mc.player.onClimbable()) {
            movementInput.jumping = false;
        }
        if (this.proneToggle && !this.mc.player.onClimbable() && this.mc.player.isOnGround() && this.isJumpPressed) {
            BlockPos pos = this.mc.player.blockPosition().above();
            if (!this.mc.level.getBlockState(pos).getMaterial().blocksMotion()) {
                this.proneToggle = false;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        this.renderer.isRenderingPlayer = false;
    }

    @SubscribeEvent
    public void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        PlayerRenderer renderer = event.getRenderer();
        if (renderer != null && !INJECTED_PLAYER_RENDERERS.containsKey(renderer)) {
            renderer.addLayer(new LayerBelt(renderer));
            renderer.addLayer(new LayerBack(renderer));
            INJECTED_PLAYER_RENDERERS.put(renderer, null);
        }
        //Hide certain parts of the player model to not clip into the camera in certain situations
        if (this.renderer.isRenderingPlayer) {
            boolean hasNausea = this.mc.player.hasEffect(MobEffects.CONFUSION);
            float swimAnimation = MathHelper.getSwimAnimation(this.mc.player, event.getPartialTick());
            boolean isInSwimAnimation = swimAnimation > 0 && swimAnimation < 1;
            boolean isInWater = this.mc.player.isInWater();
            if (hasNausea || isInSwimAnimation || this.wasPreviousInWater(7) != isInWater || isInWater && swimAnimation > 0) {
                renderer.getModel().head.visible = false;
                renderer.getModel().hat.visible = false;
            }
            if (isInSwimAnimation) {
                renderer.getModel().setAllVisible(false);
                renderer.getModel().rightArm.visible = true;
                renderer.getModel().rightSleeve.visible = true;
                renderer.getModel().leftArm.visible = true;
                renderer.getModel().leftSleeve.visible = true;
            }
            this.wasInWater <<= 1;
            this.wasInWater |= isInWater ? 1 : 0;
        }
    }

    public void onPotionAdded(ClientEffectInstance instance, PacketSCAddEffect.Logic logic) {
        switch (logic) {
            case ADD, REPLACE -> EFFECTS_TO_ADD.add(instance);
            case UPDATE -> {
                removeEffect(EFFECTS, instance.getEffect());
                int index = getIndexAndRemove(EFFECTS_TO_ADD, instance.getEffect());
                if (index != -1) {
                    EFFECTS_TO_ADD.add(index, instance);
                }
                else {
                    EFFECTS.add(instance);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderBlockHighlight(DrawSelectionEvent.HighlightBlock event) {
        this.onRenderHightlight(event);
    }

    @SubscribeEvent
    public void onRenderEntityHighlight(DrawSelectionEvent.HighlightEntity event) {
        this.onRenderHightlight(event);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        event.setCanceled(true);
        boolean sleeping = this.mc.getCameraEntity() instanceof LivingEntity && ((LivingEntity) this.mc.getCameraEntity()).isSleeping();
        if (this.mc.options.getCameraType() == CameraType.FIRST_PERSON &&
            !sleeping &&
            !this.mc.options.hideGui &&
            this.mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.mc.gameRenderer.lightTexture().turnOnLightLayer();
            this.renderer.renderItemInFirstPerson(event.getPoseStack(),
                                                  event.getMultiBufferSource(),
                                                  event.getPackedLight(),
                                                  event.getPartialTicks());
            this.mc.gameRenderer.lightTexture().turnOffLightLayer();
        }
    }

    private void onRenderHightlight(DrawSelectionEvent event) {
        event.setCanceled(true);
        PoseStack matrices = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Camera camera = event.getCamera();
        HitResult rayTrace = this.mc.hitResult;
        if (rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) rayTrace).getBlockPos();
            if (this.mc.level.getWorldBorder().isWithinBounds(hitPos)) {
                Block block = this.mc.level.getBlockState(hitPos).getBlock();
                if (block instanceof BlockKnapping) {
                    TEKnapping tile = (TEKnapping) this.mc.level.getBlockEntity(hitPos);
                    this.renderer.renderOutlines(matrices, buffer, tile.type.getShape(), camera, hitPos);
                }
                else if (block instanceof BlockMolding) {
                    TEMolding tile = (TEMolding) this.mc.level.getBlockEntity(hitPos);
                    this.renderer.renderOutlines(matrices, buffer, tile.molding.getShape(), camera, hitPos);
                }
                this.renderer.renderBlockOutlines(matrices, buffer, camera, hitPos);
            }
        }
        else if (rayTrace != null && rayTrace.getType() == HitResult.Type.ENTITY) {
            if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes() && rayTrace instanceof AdvancedEntityRayTraceResult advRayTrace) {
                if (advRayTrace.getHitbox() != null) {
                    this.renderer.renderHitbox(matrices, buffer, advRayTrace.getEntity(), advRayTrace.getHitbox(), camera, event.getPartialTicks());
                }
            }
        }
        if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes() &&
            (this.mc.player.getMainHandItem().getItem() == EvolutionItems.debug_item.get() ||
             this.mc.player.getOffhandItem().getItem() == EvolutionItems.debug_item.get())) {
            this.renderer.renderHitbox(matrices,
                                       buffer,
                                       this.mc.player,
                                       ((IEntityPatch) this.mc.player).getHitboxes().getBoxes().get(0),
                                       camera,
                                       event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderMissHighlight(DrawSelectionEvent event) {
        this.onRenderHightlight(event);
    }

    public void performLungeMovement() {
        if (!this.lunging && this.mc.player.isOnGround() && this.mc.player.zza > 0) {
            this.lunging = true;
            Vec3 oldMotion = this.mc.player.getDeltaMovement();
            float sinFacing = MathHelper.sinDeg(this.mc.player.getYRot());
            float cosFacing = MathHelper.cosDeg(this.mc.player.getYRot());
            double lungeBoost = 0.15;
            this.mc.player.setDeltaMovement(oldMotion.x - lungeBoost * sinFacing, oldMotion.y, oldMotion.z + lungeBoost * cosFacing);
        }
    }

    public void performMainhandLunge(ItemStack mainhandStack, float strength) {
        this.mainhandTimeSinceLastHit = 0;
        this.renderer.resetFullEquipProgress(InteractionHand.MAIN_HAND);
        double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getLocation().y : Double.NaN;
        int slot = Integer.MIN_VALUE;
        for (int i = 0; i < this.mc.player.getInventory().items.size(); i++) {
            if (this.mc.player.getInventory().items.get(i).equals(mainhandStack, false)) {
                slot = i;
                break;
            }
        }
        if (slot == Integer.MIN_VALUE) {
            if (this.mc.player.getInventory().items.get(0).equals(mainhandStack, false)) {
                slot = -1;
            }
        }
        if (slot != Integer.MIN_VALUE) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLunge(this.leftPointedEntity, InteractionHand.MAIN_HAND, rayTraceY, slot, strength));
        }
        else {
            Evolution.warn("Unable to find lunge stack: {}", mainhandStack);
        }
    }

    public void performOffhandLunge(ItemStack offhandStack, float strength) {
        this.offhandTimeSinceLastHit = 0;
        this.renderer.resetFullEquipProgress(InteractionHand.OFF_HAND);
        double rayTraceY = this.rightRayTrace != null ? this.rightRayTrace.getLocation().y : Double.NaN;
        int slot = Integer.MIN_VALUE;
        if (this.mc.player.getInventory().offhand.get(0).equals(offhandStack, false)) {
            slot = -1;
        }
        if (slot == Integer.MIN_VALUE) {
            for (int i = 0; i < this.mc.player.getInventory().items.size(); i++) {
                if (this.mc.player.getInventory().items.get(i).equals(offhandStack, false)) {
                    slot = i;
                    break;
                }
            }
        }
        if (slot != Integer.MIN_VALUE) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLunge(this.rightPointedEntity, InteractionHand.OFF_HAND, rayTraceY, slot, strength));
        }
        else {
            Evolution.warn("Unable to find lunge stack: {}", offhandStack);
        }
    }

    @SubscribeEvent
    public void renderTooltip(RenderTooltipEvent.GatherComponents event) {
        if (event.isCanceled()) {
            return;
        }
        Item item = event.getItemStack().getItem();
        if (!(item instanceof IEvolutionItem)) {
            return;
        }
        ItemEvents.makeEvolutionTooltip(event.getItemStack(), event.getTooltipElements());
    }

    public void rightMouseClick(IOffhandAttackable item, ItemStack stack) {
        float cooldown = getRightCooldownPeriod(item, stack);
        if (this.offhandTimeSinceLastHit >= cooldown) {
            this.offhandTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getLocation().y : Double.NaN;
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.rightPointedEntity, InteractionHand.OFF_HAND, rayTraceY));
            this.swingArm(InteractionHand.OFF_HAND);
        }
    }

    public void setCameraPos(Vec3 cameraPos) {
        this.cameraPos = cameraPos;
    }

    private boolean shouldBeProbe(Player player) {
        if (player.isInWater()) {
            return false;
        }
        if (player.isInLava()) {
            return false;
        }
        if (player.getVehicle() != null) {
            return false;
        }
        return !player.onClimbable() || !this.isJumpPressed && player.isOnGround();
    }

    public boolean shouldRenderPlayer() {
        return EvolutionConfig.CLIENT.firstPersonRenderer.get();
    }

    @SubscribeEvent
    public void shutDownInternalServer(ServerStoppedEvent event) {
        if (this.inverted) {
            this.inverted = false;
        }
    }

    public void startCustomAttack(ICustomAttack.AttackType attackType, InteractionHand hand) {
        ILivingEntityPatch player = (ILivingEntityPatch) this.mc.player;
        switch (hand) {
            case MAIN_HAND -> {
                if (player.isMainhandCustomAttacking()) {
                    break;
                }
                this.isMainhandCustomAttacking = true;
                this.mainhandTimeSinceLastHit = 0;
                player.startMainhandCustomAttack(attackType);
            }
            case OFF_HAND -> {
                if (player.isOffhandCustomAttacking()) {
                    break;
                }
                this.isOffhandCustomAttacking = true;
                player.startOffhandCustomAttack(attackType);
            }
        }
    }

    public void swingArm(InteractionHand hand) {
        ItemStack stack = this.mc.player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(this.mc.player)) {
            return;
        }
        this.renderer.swingArm(hand);
    }

    private void updateBackItem() {
        ItemStack backStack = ItemStack.EMPTY;
        int priority = Integer.MAX_VALUE;
        int chosen = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().items.get(i);
            if (stack.getItem() instanceof IBackWeapon backWeapon) {
                int stackPriority = backWeapon.getPriority(stack);
                if (stackPriority >= 0 && priority > stackPriority) {
                    backStack = stack;
                    priority = stackPriority;
                    chosen = i;
                    if (priority == 0) {
                        break;
                    }
                }
            }
        }
        if (chosen == this.mc.player.getInventory().selected) {
            backStack = ItemStack.EMPTY;
        }
        BACK_ITEMS.put(this.mc.player.getId(), backStack);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdateBeltBackItem(backStack, true));
    }

    private void updateBeltItem() {
        ItemStack oldStack = BELT_ITEMS.getOrDefault(this.mc.player.getId(), ItemStack.EMPTY);
        ItemStack beltStack = ItemStack.EMPTY;
        int priority = Integer.MAX_VALUE;
        int chosen = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().items.get(i);
            if (stack.getItem() instanceof IBeltWeapon) {
                int stackPriority = ((IBeltWeapon) stack.getItem()).getPriority();
                if (priority > stackPriority) {
                    beltStack = stack;
                    priority = stackPriority;
                    chosen = i;
                    if (priority == 0) {
                        break;
                    }
                }
            }
        }
        if (chosen == this.mc.player.getInventory().selected) {
            beltStack = ItemStack.EMPTY;
        }
        if (!ItemStack.matches(beltStack, oldStack)) {
            if (beltStack.getItem() instanceof ItemSword) {
                this.mc.getSoundManager()
                       .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_SHEATHE.get(), SoundSource.PLAYERS, 0.8f, 1.0f));
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlaySoundEntityEmitted(this.mc.player,
                                                                                          EvolutionSounds.SWORD_SHEATHE.get(),
                                                                                          SoundSource.PLAYERS,
                                                                                          0.8f,
                                                                                          1.0f));
            }
            BELT_ITEMS.put(this.mc.player.getId(), beltStack.copy());
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdateBeltBackItem(beltStack, false));
        }
    }

    private void updateClientProneState(Player player) {
        if (player != null) {
            boolean shouldBeProne = ClientProxy.TOGGLE_PRONE.isDown() != this.proneToggle;
            shouldBeProne = shouldBeProne && this.shouldBeProbe(player);
            BlockPos pos = player.blockPosition().above(2);
            shouldBeProne = shouldBeProne || this.proneToggle && player.onClimbable() && player.level.getBlockState(pos).getMaterial().blocksMotion();
            if (shouldBeProne != Evolution.PRONED_PLAYERS.getOrDefault(player.getId(), false)) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetProne(shouldBeProne));
                Evolution.PRONED_PLAYERS.put(player.getId(), shouldBeProne);
            }
        }
    }

    public void updateClientTickrate(float tickrate) {
        if (this.tps == tickrate) {
            return;
        }
        Evolution.info("Updating client tickrate to " + tickrate);
        this.tps = tickrate;
        Timer timer = TIMER.get(this.mc);
        MS_PER_TICK.set(timer, 1_000.0F / tickrate);
    }

    private boolean wasPreviousInWater(int frame) {
        return (this.wasInWater & 1 << frame) != 0;
    }
}
