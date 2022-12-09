package tgw.evolution.events;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.ClientProxy;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.client.gui.GuiContainerCreativeHandler;
import tgw.evolution.client.gui.GuiContainerHandler;
import tgw.evolution.client.gui.IGuiScreenHandler;
import tgw.evolution.client.gui.ScreenModList;
import tgw.evolution.client.gui.advancements.ScreenAdvancements;
import tgw.evolution.client.gui.config.ScreenModConfigSelection;
import tgw.evolution.client.gui.stats.ScreenStats;
import tgw.evolution.client.gui.toast.ToastCustomRecipe;
import tgw.evolution.client.models.ModelRegistry;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.*;
import tgw.evolution.inventory.extendedinventory.EvolutionRecipeBook;
import tgw.evolution.items.*;
import tgw.evolution.network.*;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.IGameRendererPatch;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.stats.EvolutionStatsCounter;
import tgw.evolution.util.AdvancedEntityHitResult;
import tgw.evolution.util.HitInformation;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.collection.*;
import tgw.evolution.util.constants.OptiFineHelper;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.toast.ToastHolderRecipe;
import tgw.evolution.util.toast.Toasts;
import tgw.evolution.world.dimension.DimensionOverworld;

import java.util.*;
import java.util.stream.Collectors;

public class ClientEvents {

    public static final OList<ClientEffectInstance> EFFECTS_TO_ADD = new OArrayList<>();
    public static final OList<ClientEffectInstance> EFFECTS = new OArrayList<>();
    public static final I2OMap<ItemStack> BELT_ITEMS = new I2OOpenHashMap<>();
    public static final I2OMap<ItemStack> BACK_ITEMS = new I2OOpenHashMap<>();
    private static final HitInformation MAINHAND_HITS = new HitInformation();
    private static final BlockHitResult[] MAINHAND_HIT_RESULT = new BlockHitResult[1];
    private static @Nullable ClientEvents instance;
    private static @Nullable IGuiScreenHandler handler;
    private static @Nullable Slot oldSelectedSlot;
    private static double accumulatedScrollDelta;
    private static boolean canDoLMBDrag;
    private static boolean canDoRMBDrag;
    private final Vec3d cameraPos = new Vec3d();
    private final ISet currentShaders = new IOpenHashSet();
    private final ISet desiredShaders = new IOpenHashSet();
    private final ISet forcedShaders = new IOpenHashSet();
    private final Minecraft mc;
    private final ClientRenderer renderer;
    public int effectToAddTicks;
    public @Nullable Entity leftPointedEntity;
    public @Nullable EntityHitResult leftRayTrace;
    public @Nullable Entity rightPointedEntity;
    public @Nullable EntityHitResult rightRayTrace;
    private @Nullable IMelee.IAttackType cachedAttackType;
    private int cameraId = -1;
    private @Nullable DimensionOverworld dimension;
    private boolean initialized;
    private int mainhandCooldownTime;
    private int offhandCooldownTime;
    private @Nullable SkyRenderer skyRenderer;
    private int ticks;
    private float tps = 20.0f;
    private int warmUpTicks;
    private boolean wasSpecialAttacking;

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
        Map<ModConfig.Type, Set<ModConfig>> configSets = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE,
                                                                                                     "configSets");
        if (configSets != null) {
            Set<ModConfig> configSet = configSets.get(type);
            synchronized (configSet) {
                Set<ModConfig> filteredConfigSets = configSet.stream()
                                                             .filter(config -> config.getModId().equals(container.getModId()))
                                                             .collect(Collectors.toSet());
                if (!filteredConfigSets.isEmpty()) {
                    configMap.put(type, filteredConfigSets);
                }
            }
        }
    }

    private static boolean areStacksCompatible(ItemStack a, ItemStack b) {
        return a.isEmpty() || b.isEmpty() || a.sameItem(b) && ItemStack.tagMatches(a, b);
    }

    public static boolean containsEffect(OList<ClientEffectInstance> list, MobEffect effect) {
        for (int i = 0, l = list.size(); i < l; i++) {
            if (list.get(i).getEffect() == effect) {
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

    public static void fixFont() {
        Minecraft.getInstance().font.lineHeight = 10;
    }

    public static void fixInputMappings() {
        InputConstants.Type.KEYSYM.displayTextSupplier = (keyCode, translationKey) -> {
            String formattedString = I18n.get(translationKey);
            if (formattedString.equals(translationKey)) {
                String s = GLFW.glfwGetKeyName(keyCode, -1);
                if (s != null) {
                    return new TextComponent(s.toUpperCase(Locale.ROOT));
                }
            }
            return new TranslatableComponent(translationKey);
        };
    }

    private static int getIndexAndRemove(MobEffect effect) {
        for (int i = 0; i < EFFECTS_TO_ADD.size(); i++) {
            ClientEffectInstance c = EFFECTS_TO_ADD.get(i);
            if (c.getEffect() == effect) {
                EFFECTS_TO_ADD.remove(i);
                return i;
            }
        }
        return -1;
    }

    public static ClientEvents getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ClientEvents has not yet initialized!");
        }
        return instance;
    }

    public static void onFinishLoading() {
        Evolution.info("Creating config GUI factories...");
        ModList.get().forEachModContainer((modId, container) -> {
            if (container.getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class).isPresent()) {
                return;
            }
            Map<ModConfig.Type, Set<ModConfig>> modConfigMap = createConfigMap(container);
            if (!modConfigMap.isEmpty()) {
                Evolution.info("Registering config factory for mod {}. Found {} client config(s) and {} common config(s)", modId,
                               modConfigMap.getOrDefault(ModConfig.Type.CLIENT, Collections.emptySet()).size(),
                               modConfigMap.getOrDefault(ModConfig.Type.COMMON, Collections.emptySet()).size());
                String displayName = container.getModInfo().getDisplayName();
                container.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory(
                        (mc, screen) -> new ScreenModConfigSelection(screen, displayName, modConfigMap)));
            }
        });
    }

    public static void onGuiOpen(@Nullable Screen newScreen) {
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

    public static boolean removeEffect(OList<ClientEffectInstance> list, MobEffect effect) {
        for (Iterator<ClientEffectInstance> it = list.listIterator(0); it.hasNext(); ) {
            if (it.next().getEffect() == effect) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public static void removePotionEffect(MobEffect effect) {
        removeEffect(EFFECTS, effect);
        if (removeEffect(EFFECTS_TO_ADD, effect)) {
            assert instance != null;
            instance.effectToAddTicks = 0;
        }
    }

    private static void rmbTweakNewSlot(Slot selectedSlot, ItemStack stackOnMouse) {
        assert handler != null;
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
        handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_2, false);
    }

    public void addCustomRecipeToast(int id) {
        for (ToastHolderRecipe toast : Toasts.getHolderForId(id)) {
            ToastCustomRecipe.addOrUpdate(this.mc.getToasts(), toast);
        }
    }

    public void clearMemory() {
        EFFECTS.reset();
        EFFECTS_TO_ADD.reset();
        MAINHAND_HITS.clearMemory();
        BELT_ITEMS.reset();
        BACK_ITEMS.reset();
        this.desiredShaders.reset();
        this.currentShaders.reset();
        this.forcedShaders.reset();
        if (this.mc.level == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            if (((IMinecraftPatch) this.mc).isMultiplayerPaused()) {
                Evolution.info("Resuming client");
                ((IMinecraftPatch) Minecraft.getInstance()).setMultiplayerPaused(false);
            }
            this.warmUpTicks = 0;
        }
        if (this.dimension != null || this.skyRenderer != null) {
            this.dimension = null;
            this.skyRenderer = null;
            System.gc();
        }
    }

    @Nullable
    private Slot findPullSlot(List<Slot> slots, Slot selectedSlot) {
        int startIndex = 0;
        int endIndex = slots.size();
        int direction = 1;
        ItemStack selectedSlotStack = selectedSlot.getItem();
        assert this.mc.player != null;
        boolean findInPlayerInventory = selectedSlot.container != this.mc.player.getInventory();
        for (int i = startIndex; i != endIndex; i += direction) {
            Slot slot = slots.get(i);
            assert handler != null;
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
    private RList<Slot> findPushSlots(List<Slot> slots, Slot selectedSlot, int itemCount, boolean mustDistributeAll) {
        ItemStack selectedSlotStack = selectedSlot.getItem();
        assert this.mc.player != null;
        boolean findInPlayerInventory = selectedSlot.container != this.mc.player.getInventory();
        RList<Slot> rv = new RArrayList<>();
        RList<Slot> goodEmptySlots = new RArrayList<>();
        for (int i = 0; i != slots.size() && itemCount > 0; i++) {
            Slot slot = slots.get(i);
            assert handler != null;
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

    public Vec3d getCameraPos() {
        return this.cameraPos;
    }

    public @Nullable DimensionOverworld getDimension() {
        return this.dimension;
    }

    public float getItemCooldown(InteractionHand hand) {
        assert this.mc.player != null;
        ItemStack stack = this.mc.player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item instanceof IMelee melee) {
            return melee.getCooldown(stack);
        }
        return (float) (20 / PlayerHelper.ATTACK_SPEED);
    }

    public float getMainhandIndicatorPercentage(float partialTicks) {
        if (this.mc.player instanceof ILivingEntityPatch patch && (patch.isSpecialAttacking() || patch.isLockedInSpecialAttack())) {
            return patch.getSpecialAttackProgress(partialTicks);
        }
        return MathHelper.clamp((this.mainhandCooldownTime + partialTicks) / this.getItemCooldown(InteractionHand.MAIN_HAND), 0.0F, 1.0F);
    }

    public float getOffhandIndicatorPercentage(float partialTicks) {
        return MathHelper.clamp((this.offhandCooldownTime + partialTicks) / this.getItemCooldown(InteractionHand.OFF_HAND), 0.0F, 1.0F);
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

    @Nullable
    public SkyRenderer getSkyRenderer() {
        return this.skyRenderer;
    }

    public int getTickCount() {
        return this.ticks;
    }

    public void handleShaderPacket(int shaderId) {
        assert this.mc.player != null;
        switch (shaderId) {
            case PacketSCShader.QUERY -> {
                Component message = this.currentShaders.isEmpty() ?
                                    EvolutionTexts.COMMAND_SHADER_NO_SHADER :
                                    new TranslatableComponent("command.evolution.shader.query", this.currentShaders.stream()
                                                                                                                   .sorted(Integer::compareTo)
                                                                                                                   .map(String::valueOf)
                                                                                                                   .collect(
                                                                                                                           Collectors.joining(
                                                                                                                                   ", ")));
                this.mc.player.displayClientMessage(message, false);
                return;
            }
            case PacketSCShader.TOGGLE -> {
                this.mc.gameRenderer.togglePostEffect();
                if (this.mc.gameRenderer.effectActive) {
                    this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_ON, false);
                }
                else {
                    this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_OFF, false);
                }
                return;
            }
            case PacketSCShader.CYCLE -> {
                this.mc.gameRenderer.cycleEffect();
                return;
            }
            case 0 -> {
                this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_RESET, false);
                this.forcedShaders.clear();
                return;
            }
        }
        if (this.hasShader(shaderId)) {
            this.mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.success", shaderId), false);
            this.forcedShaders.add(shaderId);
        }
        else {
            this.mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.fail", shaderId).withStyle(ChatFormatting.RED),
                                                false);
        }
    }

    public boolean hasShader(int shaderId) {
        return this.getShader(shaderId) != null;
    }

    public void incrementCooldown(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandCooldownTime++;
        }
        else {
            this.offhandCooldownTime++;
        }
    }

    public void init() {
        //Bind Sky Renderer
        this.dimension = new DimensionOverworld();
        this.skyRenderer = new SkyRenderer(this.dimension);
        //Load skin for corpses
        GameProfileCache playerProfile = SkullBlockEntity.profileCache;
        MinecraftSessionService session = SkullBlockEntity.sessionService;
        if (playerProfile != null && session != null) {
            EntityPlayerCorpse.setProfileCache(playerProfile);
            EntityPlayerCorpse.setSessionService(session);
        }
        this.mc.options.autoJump = false;
        System.gc();
    }

    @SubscribeEvent
    public void onChatRender(RenderGameOverlayEvent.Chat event) {
        event.setPosY(event.getPosY() - 10);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        this.mc.getProfiler().push("evolution");
        this.mc.getProfiler().push("init");
        if (this.mc.player == null) {
            this.initialized = false;
            this.clearMemory();
            this.mc.getProfiler().pop();
            this.mc.getProfiler().pop();
            return;
        }
        if (this.mc.level == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
        }
        if (!this.initialized) {
            this.init();
            this.initialized = true;
        }
        this.mc.getProfiler().pop();
        //Runs at the start of each tick
        if (event.phase == TickEvent.Phase.START) {
            //Camera
            this.mc.getProfiler().push("camera");
            if (this.cameraId != -1) {
                Entity entity = this.mc.level.getEntity(this.cameraId);
                if (entity != null) {
                    this.cameraId = -1;
                    this.mc.setCameraEntity(entity);
                }
            }
            //Apply shaders
            this.mc.getProfiler().popPush("shaders");
            this.desiredShaders.clear();
            if (this.mc.options.getCameraType() == CameraType.FIRST_PERSON &&
                !this.mc.player.isCreative() &&
                !this.mc.player.isSpectator() &&
                this.mc.player.equals(this.mc.getCameraEntity())) {
                float health = this.mc.player.getHealth();
                if (health <= 12.5f) {
                    this.desiredShaders.add(25);
                }
                else if (health <= 25) {
                    this.desiredShaders.add(50);
                }
                else if (health <= 50) {
                    this.desiredShaders.add(75);
                }
            }
            this.desiredShaders.addAll(this.forcedShaders);
            if (this.desiredShaders.isEmpty()) {
                if (!this.currentShaders.isEmpty()) {
                    this.currentShaders.clear();
                    ((IGameRendererPatch) this.mc.gameRenderer).shutdownAllShaders();
                }
            }
            else {
                if (!this.desiredShaders.containsAll(this.currentShaders)) {
                    for (IntIterator it = this.currentShaders.intIterator(); it.hasNext(); ) {
                        int shader = it.nextInt();
                        if (!this.desiredShaders.contains(shader)) {
                            it.remove();
                            ((IGameRendererPatch) this.mc.gameRenderer).shutdownShader(shader);
                        }
                    }
                }
                if (!this.currentShaders.containsAll(this.desiredShaders)) {
                    for (IntIterator it = this.desiredShaders.intIterator(); it.hasNext(); ) {
                        int shader = it.nextInt();
                        if (this.currentShaders.add(shader)) {
                            ResourceLocation shaderLoc = this.getShader(shader);
                            if (shaderLoc != null) {
                                ((IGameRendererPatch) this.mc.gameRenderer).loadShader(shader, shaderLoc);
                            }
                            else {
                                Evolution.warn("Unregistered shader id: {}", shader);
                            }
                        }
                    }
                }
            }
            this.mc.getProfiler().pop();
            if (!this.mc.isPaused()) {
                this.mc.getProfiler().push("dimension");
                assert this.dimension != null;
                this.dimension.tick();
                this.mc.getProfiler().popPush("renderer");
                this.renderer.startTick();
                this.mc.getProfiler().popPush("updateHeld");
                this.updateBeltItem();
                this.updateBackItem();
                //Handle two-handed items
                this.mc.getProfiler().popPush("twoHanded");
                ItemStack mainHandStack = this.mc.player.getMainHandItem();
                if (mainHandStack.getItem() instanceof ITwoHanded twoHanded &&
                    twoHanded.isTwoHanded(mainHandStack) &&
                    !this.mc.player.getOffhandItem().isEmpty()) {
                    this.mainhandCooldownTime = 0;
                    this.mc.missTime = Integer.MAX_VALUE;
                    this.mc.player.displayClientMessage(EvolutionTexts.ACTION_TWO_HANDED, true);
                }
                //Prevents the player from attacking if on cooldown
                this.mc.getProfiler().popPush("cooldown");
                boolean isSpecialAttacking = ((ILivingEntityPatch) this.mc.player).shouldRenderSpecialAttack();
                if (this.wasSpecialAttacking && !isSpecialAttacking) {
                    this.resetCooldown(InteractionHand.MAIN_HAND);
                }
                if (this.getMainhandIndicatorPercentage(0.0F) != 1 &&
                    this.mc.hitResult != null &&
                    this.mc.hitResult.getType() != HitResult.Type.BLOCK) {
                    this.mc.missTime = Integer.MAX_VALUE;
                }
                this.wasSpecialAttacking = isSpecialAttacking;
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
                    boolean needsRemoving = false;
                    for (int i = 0, l = EFFECTS.size(); i < l; i++) {
                        ClientEffectInstance instance = EFFECTS.get(i);
                        MobEffect effect = instance.getEffect();
                        if (instance.getDuration() == 0 || !this.mc.player.hasEffect(effect) && this.warmUpTicks >= 100) {
                            needsRemoving = true;
                        }
                        else {
                            instance.tick();
                        }
                    }
                    if (needsRemoving) {
                        for (int i = 0; i < EFFECTS.size(); i++) {
                            ClientEffectInstance instance = EFFECTS.get(i);
                            if (instance.getDuration() == 0 || !this.mc.player.hasEffect(instance.getEffect())) {
                                EFFECTS.remove(i--);
                            }
                        }
                    }
                }
                if (!EFFECTS_TO_ADD.isEmpty()) {
                    this.effectToAddTicks++;
                }
                else {
                    this.renderer.isAddingEffect = false;
                }
                //Handle creative features
                this.mc.getProfiler().popPush("creative");
                if (this.mc.player.isCreative() && ClientProxy.KEY_BUILDING_ASSIST.isDown()) {
                    if (this.mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                        assert this.mc.hitResult != null;
                        if (this.mc.hitResult.getType() == HitResult.Type.BLOCK) {
                            BlockPos pos = ((BlockHitResult) this.mc.hitResult).getBlockPos();
                            if (!this.mc.level.getBlockState(pos).isAir()) {
                                EvolutionNetwork.sendToServer(new PacketCSChangeBlock((BlockHitResult) this.mc.hitResult));
                                this.mc.player.swing(InteractionHand.MAIN_HAND);
                            }
                        }
                    }
                }
                //Handle swing
                this.mc.getProfiler().popPush("swing");
                this.ticks++;
                assert this.mc.gameMode != null;
                if (this.mc.player instanceof ILivingEntityPatch patch && patch.isSpecialAttacking()) {
                    this.cachedAttackType = patch.getSpecialAttackType();
                    if (patch.isInHitTicks()) {
                        MathHelper.collideOBBWithCollider(MAINHAND_HITS, this.mc.player, 1.0f, MAINHAND_HIT_RESULT, true, false);
                        if (MAINHAND_HIT_RESULT[0] != null && MAINHAND_HIT_RESULT[0].getType() != HitResult.Type.MISS) {
                            patch.stopSpecialAttack(IMelee.StopReason.HIT_BLOCK);
                            Evolution.info("Collided with {} at {} on {}", this.mc.level.getBlockState(MAINHAND_HIT_RESULT[0].getBlockPos()),
                                           MAINHAND_HIT_RESULT[0].getBlockPos(), MAINHAND_HIT_RESULT[0].getLocation());
                        }
                    }
                }
                else {
                    if (!MAINHAND_HITS.isEmpty() && this.cachedAttackType != null) {
                        MAINHAND_HITS.sendHits(this.cachedAttackType);
                        MAINHAND_HITS.clear();
                    }
                    this.cachedAttackType = null;
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
        if (event.getEntity() instanceof LocalPlayer player && player.equals(this.mc.player)) {
            this.mc.player.stats = new EvolutionStatsCounter();
            this.mc.player.recipeBook = new EvolutionRecipeBook();
        }
    }

    @SubscribeEvent
    public void onGUIMouseClickedPre(ScreenEvent.MouseClickedEvent.Pre event) {
        @MouseButton int button = event.getButton();
        if (this.onMouseClicked(event.getMouseX(), event.getMouseY(), button)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGUIMouseDragPre(ScreenEvent.MouseDragEvent.Pre event) {
        @MouseButton int button = event.getMouseButton();
        this.onMouseDrag(event.getMouseX(), event.getMouseY(), button);
    }

    @SubscribeEvent
    public void onGUIMouseReleasedPre(ScreenEvent.MouseReleasedEvent.Pre event) {
        @MouseButton int button = event.getButton();
        if (this.onMouseReleased(button)) {
            event.setCanceled(true);
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
            EvolutionNetwork.sendToServer(new PacketCSOpenExtendedInventory());
        }
        else if (screen instanceof AdvancementsScreen) {
            assert this.mc.getConnection() != null;
            event.setScreen(new ScreenAdvancements(this.mc.getConnection().getAdvancements()));
        }
        else if (screen instanceof StatsScreen) {
            assert this.mc.player != null;
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
                event.addListener(new Button(shareToLan.x, shareToLan.y + 24, shareToLan.getWidth(), shareToLan.getHeight(),
                                             EvolutionTexts.GUI_MENU_MOD_OPTIONS, button -> this.mc.setScreen(new ModListScreen(event.getScreen()))));
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
                feedback = new Button(event.getScreen().width / 2 - 102, event.getScreen().height / 4 + 72 - 16, 98, 20,
                                      EvolutionTexts.GUI_MENU_SEND_FEEDBACK, button -> this.mc.setScreen(new ConfirmLinkScreen(b -> {
                    if (b) {
                        Util.getPlatform().openUri(feedbackLink);
                    }
                    this.mc.setScreen(event.getScreen());
                }, feedbackLink, true)));
                String bugsLink = "https://github.com/MGSchultz-13/Evolution/issues";
                bugs = new Button(event.getScreen().width / 2 + 4, event.getScreen().height / 4 + 72 - 16, 98, 20,
                                  EvolutionTexts.GUI_MENU_REPORT_BUGS, button -> this.mc.setScreen(new ConfirmLinkScreen(b -> {
                    if (b) {
                        Util.getPlatform().openUri(bugsLink);
                    }
                    this.mc.setScreen(event.getScreen());
                }, bugsLink, true)));
                event.addListener(feedback);
                event.addListener(bugs);
            }
        }
    }

    @SubscribeEvent
    public void onLivingStartUsingItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            if (player.level.isClientSide) {
                ItemStack stack = event.getItem();
                if (stack.getItem() instanceof ICancelableUse cancel && cancel.isCancelable(stack)) {
                    player.displayClientMessage(cancel.getCancelMessage(this.mc.options.keyAttack.getTranslatedKeyMessage()), true);
                }
            }
        }
    }

    public boolean onMouseClicked(double x, double y, @MouseButton int button) {
        if (handler == null) {
            return false;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(x, y);
        oldSelectedSlot = selectedSlot;
        assert this.mc.player != null;
        ItemStack stackOnMouse = this.mc.player.inventoryMenu.getCarried();
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (stackOnMouse.isEmpty()) {
                canDoLMBDrag = true;
            }
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
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

    public void onMouseDrag(double x, double y, @MouseButton int button) {
        if (handler == null) {
            return;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(x, y);
        if (selectedSlot == oldSelectedSlot) {
            return;
        }
        oldSelectedSlot = selectedSlot;
        if (selectedSlot == null) {
            return;
        }
        if (handler.isIgnored(selectedSlot)) {
            return;
        }
        assert this.mc.player != null;
        ItemStack stackOnMouse = this.mc.player.inventoryMenu.getCarried();
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (!canDoLMBDrag) {
                return;
            }
            ItemStack selectedSlotStack = selectedSlot.getItem();
            if (selectedSlotStack.isEmpty()) {
                return;
            }
            boolean shiftIsDown = Screen.hasShiftDown();
            if (stackOnMouse.isEmpty()) {
                if (!shiftIsDown) {
                    return;
                }
                handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, true);
            }
            else {
                if (!areStacksCompatible(selectedSlotStack, stackOnMouse)) {
                    return;
                }
                if (shiftIsDown) {
                    handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, true);
                }
                else {
                    if (stackOnMouse.getCount() + selectedSlotStack.getCount() > stackOnMouse.getMaxStackSize()) {
                        return;
                    }
                    handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                    if (!handler.isCraftingOutput(selectedSlot)) {
                        handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                    }
                }
            }
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (!canDoRMBDrag) {
                return;
            }
            if (stackOnMouse.isEmpty()) {
                return;
            }
            rmbTweakNewSlot(selectedSlot, stackOnMouse);
        }
    }

    public boolean onMouseReleased(@MouseButton int button) {
        if (handler == null) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            canDoLMBDrag = false;
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_2) {
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
        assert this.mc.player != null;
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
                    RList<Slot> targetSlots = this.findPushSlots(slots, selectedSlot, selectedSlotStack.getCount(), true);
                    if (targetSlots == null) {
                        break;
                    }
                    handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                    for (int i = 0; i < targetSlots.size(); i++) {
                        Slot slot = targetSlots.get(i);
                        if (i == targetSlots.size() - 1) {
                            handler.clickSlot(slot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                        }
                        else {
                            int clickTimes = slot.getItem().getMaxStackSize() - slot.getItem().getCount();
                            while (clickTimes-- > 0) {
                                handler.clickSlot(slot, GLFW.GLFW_MOUSE_BUTTON_2, false);
                            }
                        }
                    }
                }
            }
            else {
                while (numItemsToMove-- > 0) {
                    handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
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
            RList<Slot> targetSlots = this.findPushSlots(slots, selectedSlot, numItemsToMove, false);
            assert targetSlots != null;
            if (targetSlots.isEmpty()) {
                return true;
            }
            handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
            for (int i = 0, l = targetSlots.size(); i < l; i++) {
                Slot slot = targetSlots.get(i);
                int clickTimes = slot.getItem().getMaxStackSize() - slot.getItem().getCount();
                clickTimes = Math.min(clickTimes, numItemsToMove);
                numItemsToMove -= clickTimes;
                while (clickTimes-- > 0) {
                    handler.clickSlot(slot, GLFW.GLFW_MOUSE_BUTTON_2, false);
                }
            }
            handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
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
                handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                handler.clickSlot(targetSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
                continue;
            }
            int numItemsToMoveFromTargetSlot = Math.min(numItemsToMove, numItemsInTargetSlot);
            maxItemsToMove -= numItemsToMoveFromTargetSlot;
            numItemsToMove -= numItemsToMoveFromTargetSlot;
            if (!stackOnMouse.isEmpty() && !targetSlot.mayPlace(stackOnMouse)) {
                break;
            }
            handler.clickSlot(targetSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
            if (numItemsToMoveFromTargetSlot == numItemsInTargetSlot) {
                handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
            }
            else {
                for (int i = 0; i < numItemsToMoveFromTargetSlot; i++) {
                    handler.clickSlot(selectedSlot, GLFW.GLFW_MOUSE_BUTTON_2, false);
                }
            }
            handler.clickSlot(targetSlot, GLFW.GLFW_MOUSE_BUTTON_1, false);
        }
        return true;
    }

    public void onPotionAdded(ClientEffectInstance instance, PacketSCAddEffect.Logic logic) {
        switch (logic) {
            case ADD, REPLACE -> EFFECTS_TO_ADD.add(instance);
            case UPDATE -> {
                removeEffect(EFFECTS, instance.getEffect());
                int index = getIndexAndRemove(instance.getEffect());
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
    }

    private void onRenderHightlight(DrawSelectionEvent event) {
        event.setCanceled(true);
        PoseStack matrices = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Camera camera = event.getCamera();
        HitResult rayTrace = this.mc.hitResult;
        if (rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) rayTrace).getBlockPos();
            assert this.mc.level != null;
            if (this.mc.level.getWorldBorder().isWithinBounds(hitPos)) {
                Block block = this.mc.level.getBlockState(hitPos).getBlock();
                if (block instanceof BlockKnapping) {
                    TEKnapping tile = (TEKnapping) this.mc.level.getBlockEntity(hitPos);
                    assert tile != null;
                    this.renderer.renderOutlines(matrices, buffer, tile.type.getShape(), camera, hitPos);
                }
                else if (block instanceof BlockMolding) {
                    TEMolding tile = (TEMolding) this.mc.level.getBlockEntity(hitPos);
                    assert tile != null;
                    this.renderer.renderOutlines(matrices, buffer, tile.molding.getShape(), camera, hitPos);
                }
                this.renderer.renderBlockOutlines(matrices, buffer, camera, hitPos);
            }
        }
        else if (rayTrace != null && rayTrace.getType() == HitResult.Type.ENTITY) {
            if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes() && rayTrace instanceof AdvancedEntityHitResult advRayTrace) {
                if (advRayTrace.getHitbox() != null) {
                    this.renderer.renderHitbox(matrices, buffer, advRayTrace.getEntity(), advRayTrace.getHitbox(), camera, event.getPartialTicks());
                }
            }
        }
        if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            assert this.mc.player != null;
            if (this.mc.player.getMainHandItem().getItem() == EvolutionItems.DEBUG_ITEM.get() ||
                this.mc.player.getOffhandItem().getItem() == EvolutionItems.DEBUG_ITEM.get()) {
                HitboxEntity<? extends Entity> hitboxes = ((IEntityPatch) this.mc.player).getHitboxes();
                if (hitboxes != null) {
                    this.renderer.renderHitbox(matrices, buffer, this.mc.player, hitboxes.getBoxes().get(0), camera, event.getPartialTicks());
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderMissHighlight(DrawSelectionEvent event) {
        this.onRenderHightlight(event);
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
        assert this.mc.player != null;
        ItemEvents.makeEvolutionTooltip(this.mc.player, event.getItemStack(), event.getTooltipElements());
    }

    public void resetCooldown(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandCooldownTime = 0;
        }
        else {
            this.offhandCooldownTime = 0;
        }
    }

    public void resetCooldowns() {
        this.mainhandCooldownTime = 0;
        this.offhandCooldownTime = 0;
    }

    public void setCameraPos(Vec3d cameraPos) {
        assert this.mc.player != null;
        if (this.mc.player.equals(this.mc.getCameraEntity())) {
            this.cameraPos.set(cameraPos);
        }
        else {
            this.cameraPos.set(Vec3d.NULL);
        }
    }

    public void setNotLoadedCameraId(int id) {
        this.cameraId = id;
    }

    public boolean shouldRenderSpecialAttack() {
        if (this.mc.player == null) {
            return false;
        }
        return ((ILivingEntityPatch) this.mc.player).shouldRenderSpecialAttack();
    }

    public void startChargeAttack(IMelee.ChargeAttackType chargeAttack) {
        assert this.mc.player != null;
        ILivingEntityPatch player = (ILivingEntityPatch) this.mc.player;
        if (!player.isLockedInSpecialAttack()) {
            player.startSpecialAttack(chargeAttack);
        }
        else if (player.canPerformFollowUp(chargeAttack)) {
            player.performFollowUp();
        }
    }

    public void startShortAttack(ItemStack stack) {
        assert this.mc.player != null;
        IMelee.IAttackType type = stack.getItem() instanceof IMelee melee ? melee.getBasicAttackType(stack) : IMelee.BARE_HAND_ATTACK;
        ILivingEntityPatch player = (ILivingEntityPatch) this.mc.player;
        if (player.isOnGracePeriod()) {
            if (player.canPerformFollowUp(type)) {
                player.performFollowUp();
            }
        }
        else {
            if (!player.isSpecialAttacking() && !player.isLockedInSpecialAttack()) {
                player.startSpecialAttack(type);
            }
        }
    }

    private void updateBackItem() {
        ItemStack backStack = ItemStack.EMPTY;
        int priority = Integer.MAX_VALUE;
        int chosen = -1;
        for (int i = 0; i < 9; i++) {
            assert this.mc.player != null;
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
        ItemStack oldStack = BACK_ITEMS.put(this.mc.player.getId(), backStack);
        if (oldStack != backStack) {
            EvolutionNetwork.sendToServer(new PacketCSUpdateBeltBackItem(backStack, true));
        }
    }

    private void updateBeltItem() {
        assert this.mc.player != null;
        ItemStack oldStack = BELT_ITEMS.getOrDefault(this.mc.player.getId(), ItemStack.EMPTY);
        ItemStack beltStack = ItemStack.EMPTY;
        int priority = Integer.MAX_VALUE;
        int chosen = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().items.get(i);
            if (stack.getItem() instanceof IBeltWeapon beltWeapon) {
                int stackPriority = beltWeapon.getPriority();
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
        if (!ItemStack.isSame(beltStack, oldStack)) {
            if (beltStack.getItem() instanceof IMelee melee && melee.shouldPlaySheatheSound(beltStack)) {
                this.mc.getSoundManager()
                       .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_SHEATHE.get(), SoundSource.PLAYERS, 0.8f, 1.0f));
                EvolutionNetwork.sendToServer(
                        new PacketCSPlaySoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_SHEATHE.get(), SoundSource.PLAYERS, 0.8f, 1.0f));
            }
            BELT_ITEMS.put(this.mc.player.getId(), beltStack);
            EvolutionNetwork.sendToServer(new PacketCSUpdateBeltBackItem(beltStack, false));
        }
    }

    public void updateClientTickrate(float tickrate) {
        if (this.tps == tickrate) {
            return;
        }
        Evolution.info("Updating client tickrate to " + tickrate);
        this.tps = tickrate;
        this.mc.timer.msPerTick = 1_000.0f / tickrate;
    }
}
