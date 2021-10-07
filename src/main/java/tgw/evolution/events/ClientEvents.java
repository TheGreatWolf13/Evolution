package tgw.evolution.events;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Timer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
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
import tgw.evolution.client.gui.controls.ScreenControls;
import tgw.evolution.client.gui.stats.ScreenStats;
import tgw.evolution.client.gui.toast.ToastCustomRecipe;
import tgw.evolution.client.layers.LayerBack;
import tgw.evolution.client.layers.LayerBelt;
import tgw.evolution.client.models.tile.BakedModelFirewoodPile;
import tgw.evolution.client.models.tile.BakedModelKnapping;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.renderer.ambient.LightTextureEv;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.client.util.LungeAttackInfo;
import tgw.evolution.client.util.LungeChargeInfo;
import tgw.evolution.client.util.MovementInputEvolution;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.InputHooks;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.*;
import tgw.evolution.inventory.extendedinventory.EvolutionRecipeBook;
import tgw.evolution.items.*;
import tgw.evolution.network.*;
import tgw.evolution.patches.IMinecraftPatch;
import tgw.evolution.stats.EvolutionStatisticsManager;
import tgw.evolution.util.*;
import tgw.evolution.util.hitbox.BodyPart;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.StaticFieldHandler;
import tgw.evolution.util.toast.ToastHolderRecipe;
import tgw.evolution.util.toast.Toasts;
import tgw.evolution.world.dimension.DimensionOverworld;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

public class ClientEvents {

    public static final FieldHandler<Minecraft, Integer> LEFT_COUNTER_FIELD = new FieldHandler<>(Minecraft.class, "field_71429_W");
    public static final List<ClientEffectInstance> EFFECTS_TO_ADD = new ArrayList<>();
    public static final List<ClientEffectInstance> EFFECTS = new ArrayList<>();
    public static final Int2ObjectMap<LungeChargeInfo> ABOUT_TO_LUNGE_PLAYERS = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<LungeAttackInfo> LUNGING_PLAYERS = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<ItemStack> BELT_ITEMS = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<ItemStack> BACK_ITEMS = new Int2ObjectOpenHashMap<>();
    private static final Map<PlayerRenderer, Object> INJECTED_PLAYER_RENDERERS = new WeakHashMap<>();
    private static final StaticFieldHandler<SkullTileEntity, PlayerProfileCache> PLAYER_PROF_FIELD = new StaticFieldHandler<>(SkullTileEntity.class,
                                                                                                                              "field_184298_j");
    private static final StaticFieldHandler<SkullTileEntity, MinecraftSessionService> SESSION_FIELD = new StaticFieldHandler<>(SkullTileEntity.class,
                                                                                                                               "field_184299_k");
    private static final FieldHandler<GameRenderer, LightTexture> LIGHTMAP_FIELD = new FieldHandler<>(GameRenderer.class, "field_78513_d");
    private static final FieldHandler<Minecraft, Timer> TIMER_FIELD = new FieldHandler<>(Minecraft.class, "field_71428_T");
    private static final FieldHandler<Timer, Float> TICKRATE_FIELD = new FieldHandler<>(Timer.class, "field_194149_e");
    private static final FieldHandler<ClientPlayerEntity, ClientRecipeBook> RECIPE_BOOK_FIELD = new FieldHandler<>(ClientPlayerEntity.class,
                                                                                                                   "field_192036_cb");
    private static final FieldHandler<ModContainer, EnumMap<ModConfig.Type, ModConfig>> CONFIGS = new FieldHandler<>(ModContainer.class, "configs");
    private static final FieldHandler<ClientPlayerEntity, StatisticsManager> STATS = new FieldHandler<>(ClientPlayerEntity.class, "field_146108_bO");
    private static final StaticFieldHandler<DimensionRenderInfo, Object2ObjectMap<ResourceLocation, DimensionRenderInfo>> RENDER_INFO =
            new StaticFieldHandler<>(
            DimensionRenderInfo.class,
            "field_239208_a_");
    private static final FieldHandler<GameRenderer, Boolean> EFFECT_ACTIVE = new FieldHandler<>(GameRenderer.class, "field_175083_ad");
    private static ClientEvents instance;
    @Nullable
    private static IGuiScreenHandler handler;
    private static boolean disableWheelForThisContainer;
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
    public EntityRayTraceResult leftRayTrace;
    public int mainhandTimeSinceLastHit;
    public int offhandTimeSinceLastHit;
    @Nullable
    public Entity rightPointedEntity;
    public EntityRayTraceResult rightRayTrace;
    private Vector3d cameraPos = Vector3d.ZERO;
    private int currentShader;
    private int desiredShader;
    private DimensionOverworld dimension;
    private boolean initialized;
    private boolean inverted;
    private boolean isJumpPressed;
    private boolean isPreviousProned;
    private boolean isSneakPressed;
    private boolean lunging;
    private GameRenderer oldGameRenderer;
    private PointOfView previousPointOfView;
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

    public static void addLungingPlayer(int entityId, Hand hand) {
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

    public static boolean containsEffect(List<ClientEffectInstance> list, Effect effect) {
        for (ClientEffectInstance instance : list) {
            if (instance.getEffect() == effect) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static IGuiScreenHandler findHandler(Screen currentScreen) {
        if (currentScreen instanceof CreativeScreen) {
            return new GuiContainerCreativeHandler((CreativeScreen) currentScreen);
        }
        if (currentScreen instanceof ContainerScreen) {
            return new GuiContainerHandler((ContainerScreen<?>) currentScreen);
        }
        return null;
    }

    public static void fixAccessibilityScreen() {
        StaticFieldHandler<AccessibilityScreen, AbstractOption[]> options = new StaticFieldHandler<>(AccessibilityScreen.class,
                                                                                                     "field_212986_a",
                                                                                                     true);
        options.set(new AbstractOption[]{AbstractOption.NARRATOR,
                                         AbstractOption.SHOW_SUBTITLES,
                                         AbstractOption.TEXT_BACKGROUND_OPACITY,
                                         AbstractOption.TEXT_BACKGROUND,
                                         AbstractOption.CHAT_OPACITY,
                                         AbstractOption.CHAT_LINE_SPACING,
                                         AbstractOption.CHAT_DELAY,
                                         AbstractOption.TOGGLE_CROUCH,
                                         AbstractOption.TOGGLE_SPRINT,
                                         AbstractOption.SCREEN_EFFECTS_SCALE,
                                         AbstractOption.FOV_EFFECTS_SCALE});
    }

    public static void fixInputMappings() {
        FieldHandler<InputMappings.Type, BiFunction<Integer, String, ITextComponent>> function = new FieldHandler<>(InputMappings.Type.class,
                                                                                                                    "field_237522_f_");
        function.set(InputMappings.Type.KEYSYM, (keyCode, translationKey) -> {
            String formattedString = I18n.get(translationKey);
            if (formattedString.equals(translationKey)) {
                String s = GLFW.glfwGetKeyName(keyCode, -1);
                if (s != null) {
                    return new StringTextComponent(s.toUpperCase(Locale.ROOT));
                }
            }
            return new TranslationTextComponent(translationKey);
        });
    }

    public static int getIndexAndRemove(List<ClientEffectInstance> list, Effect effect) {
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

    private static float getRightCooldownPeriod(IOffhandAttackable item) {
        double attackSpeed = item.getAttackSpeed() + PlayerHelper.ATTACK_SPEED;
        return (float) (1 / attackSpeed * 20);
    }

    public static void onFinishLoading() {
        Evolution.LOGGER.info("Creating config GUI factories...");
        ModList.get().forEachModContainer((modId, container) -> {
            // Ignore mods that already implement their own custom factory
            if (container.getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).isPresent()) {
                return;
            }
            EnumMap<ModConfig.Type, ModConfig> configs = CONFIGS.get(container);
            ModConfig clientConfig = configs.get(ModConfig.Type.CLIENT);
            // Optifine basically breaks Forge's client config, so it's simply not added
            if (OptiFineHelper.isLoaded() && "forge".equals(modId)) {
                Evolution.LOGGER.info("Ignoring Forge's client config since OptiFine was detected");
                clientConfig = null;
            }
            ModConfig commonConfig = configs.get(ModConfig.Type.COMMON);
            ForgeConfigSpec clientSpec = clientConfig != null ? clientConfig.getSpec() : null;
            ForgeConfigSpec commonSpec = commonConfig != null ? commonConfig.getSpec() : null;
            if (clientSpec != null || commonSpec != null) {// Only add if at least one config exists
                Evolution.LOGGER.info("Registering config factory for mod {} (client: {}, common: {})",
                                      modId,
                                      clientSpec != null,
                                      commonSpec != null);
                String displayName = container.getModInfo().getDisplayName();
                container.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                                                 () -> (mc, screen) -> new ScreenConfig(screen,
                                                                                        modId,
                                                                                        displayName,
                                                                                        clientSpec,
                                                                                        commonSpec,
                                                                                        AbstractGui.BACKGROUND_LOCATION));
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
            if (handler == null) {
                return;
            }
            boolean disableForThisContainer = handler.isMouseTweaksDisabled();
            disableWheelForThisContainer = handler.isWheelTweakDisabled();
            if (disableForThisContainer) {
                handler = null;
            }
        }
    }

    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
        for (RockVariant variant : RockVariant.VALUES) {
            Block block;
            try {
                block = variant.getKnapping();
            }
            catch (IllegalStateException e) {
                block = null;
            }
            if (block != null) {
                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    //noinspection ObjectAllocationInLoop
                    ModelResourceLocation variantMRL = BlockModelShapes.stateToModelLocation(state);
                    IBakedModel existingModel = event.getModelRegistry().get(variantMRL);
                    if (existingModel == null) {
                        Evolution.LOGGER.warn("Did not find the expected vanilla baked model(s) for BlockKnapping in registry");
                    }
                    else if (existingModel instanceof BakedModelKnapping) {
                        Evolution.LOGGER.warn("Tried to replace BakedModelKnapping twice");
                    }
                    else {
                        //noinspection ObjectAllocationInLoop
                        IBakedModel customModel = new BakedModelKnapping(existingModel, variant);
                        event.getModelRegistry().put(variantMRL, customModel);
                    }
                }
            }
        }
        for (BlockState state : EvolutionBlocks.FIREWOOD_PILE.get().getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation variantMRL = BlockModelShapes.stateToModelLocation(state);
            IBakedModel existingModel = event.getModelRegistry().get(variantMRL);
            if (existingModel == null) {
                Evolution.LOGGER.warn("Did not find the expected vanilla baked model(s) for BlockFirewoodPile in registry");
            }
            else if (existingModel instanceof BakedModelKnapping) {
                Evolution.LOGGER.warn("Tried to replace BakedModelFirewoodPile twice");
            }
            else {
                //noinspection ObjectAllocationInLoop
                IBakedModel firewoodPileModel = new BakedModelFirewoodPile(existingModel);
                event.getModelRegistry().put(variantMRL, firewoodPileModel);
            }
        }
    }

    public static boolean removeEffect(List<ClientEffectInstance> list, Effect effect) {
        Iterator<ClientEffectInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getEffect() == effect) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public static void removePotionEffect(Effect effect) {
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
        this.dimension = null;
        ABOUT_TO_LUNGE_PLAYERS.clear();
        LUNGING_PLAYERS.clear();
        if (this.mc.level == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            if (((IMinecraftPatch) this.mc).isMultiplayerPaused()) {
                Evolution.LOGGER.info("Resuming client");
                ((IMinecraftPatch) Minecraft.getInstance()).setMultiplayerPaused(false);
            }
            this.warmUpTicks = 0;
        }
    }

    @Nullable
    private Slot findPullSlot(List<Slot> slots, Slot selectedSlot) {
        int startIndex = 0;
        int endIndex = slots.size();
        int direction = 1;
        ItemStack selectedSlotStack = selectedSlot.getItem();
        boolean findInPlayerInventory = selectedSlot.container != this.mc.player.inventory;
        for (int i = startIndex; i != endIndex; i += direction) {
            Slot slot = slots.get(i);
            if (handler.isIgnored(slot)) {
                continue;
            }
            boolean slotInPlayerInventory = slot.container == this.mc.player.inventory;
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
        boolean findInPlayerInventory = selectedSlot.container != this.mc.player.inventory;
        List<Slot> rv = new ArrayList<>();
        List<Slot> goodEmptySlots = new ArrayList<>();
        for (int i = 0; i != slots.size() && itemCount > 0; i++) {
            Slot slot = slots.get(i);
            if (handler.isIgnored(slot)) {
                continue;
            }
            boolean slotInPlayerInventory = slot.container == this.mc.player.inventory;
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

    public Vector3d getCameraPos() {
        return this.cameraPos;
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
        return MathHelper.clamp((this.mainhandTimeSinceLastHit + partialTicks) / this.mc.player.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
    }

    public float getOffhandCooledAttackStrength(Item item, float adjustTicks) {
        if (!(item instanceof IOffhandAttackable)) {
            float cooldown = (float) (1.0 / PlayerHelper.ATTACK_SPEED * 20.0);
            return MathHelper.clamp((this.offhandTimeSinceLastHit + adjustTicks) / cooldown, 0.0F, 1.0F);
        }
        return MathHelper.clamp((this.offhandTimeSinceLastHit + adjustTicks) / getRightCooldownPeriod((IOffhandAttackable) item), 0.0F, 1.0F);
    }

    public ClientRenderer getRenderer() {
        return this.renderer;
    }

    @Nullable
    public ResourceLocation getShader(int shaderId) {
        switch (shaderId) {
            case 1: {
                return EvolutionResources.SHADER_MOTION_BLUR;
            }
            case 25: {
                return EvolutionResources.SHADER_DESATURATE_25;
            }
            case 50: {
                return EvolutionResources.SHADER_DESATURATE_50;
            }
            case 75: {
                return EvolutionResources.SHADER_DESATURATE_75;
            }
        }
        return null;
    }

    public int getTickCount() {
        return this.ticks;
    }

    public void handleShaderPacket(int shaderId) {
        switch (shaderId) {
            case PacketSCShader.QUERY: {
                this.mc.player.displayClientMessage(new TranslationTextComponent("command.evolution.shader.query", this.currentShader), false);
                return;
            }
            case PacketSCShader.TOGGLE: {
                this.mc.gameRenderer.togglePostEffect();
                if (EFFECT_ACTIVE.get(this.mc.gameRenderer)) {
                    this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_ON, false);
                }
                else {
                    this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_OFF, false);
                }
                return;
            }
            case 0: {
                this.mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_RESET, false);
                this.desiredShader = 0;
                return;
            }
        }
        if (this.hasShader(shaderId)) {
            this.mc.player.displayClientMessage(new TranslationTextComponent("command.evolution.shader.success", shaderId), false);
            this.desiredShader = shaderId;
        }
        else {
            this.mc.player.displayClientMessage(new TranslationTextComponent("command.evolution.shader.fail", shaderId).withStyle(TextFormatting.RED),
                                                false);
        }
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
        Object2ObjectMap<ResourceLocation, DimensionRenderInfo> renderInfos = RENDER_INFO.get();
        DimensionRenderInfo overworldRenderInfo = renderInfos.get(DimensionType.OVERWORLD_EFFECTS);
        overworldRenderInfo.setSkyRenderHandler(new SkyRenderer(this.mc.levelRenderer, this.dimension));
        //Load skin for corpses
        PlayerProfileCache playerProfile = PLAYER_PROF_FIELD.get();
        MinecraftSessionService session = SESSION_FIELD.get();
        if (playerProfile != null && session != null) {
            EntityPlayerCorpse.setProfileCache(playerProfile);
            EntityPlayerCorpse.setSessionService(session);
        }
        //Replace MovementInput
        this.mc.player.input = new MovementInputEvolution(this.mc.options);
    }

    public boolean isPlayerDizzy() {
        if (this.mc.player != null) {
            return this.mc.player.hasEffect(EvolutionEffects.DIZZINESS.get());
        }
        return false;
    }

    public void leftMouseClick() {
        float cooldown = this.mc.player.getCurrentItemAttackStrengthDelay();
        if (this.mainhandTimeSinceLastHit >= cooldown) {
            this.mainhandTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getLocation().y : Double.NaN;
            if (this.leftRayTrace instanceof AdvancedEntityRayTraceResult) {
                BodyPart part = BodyPart.ALL;
                if (((AdvancedEntityRayTraceResult) this.leftRayTrace).getHitbox() != null) {
                    part = ((AdvancedEntityRayTraceResult) this.leftRayTrace).getHitbox().getPart();
                }
                Evolution.LOGGER.debug("Part = {}", part);
            }
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.leftPointedEntity, Hand.MAIN_HAND, rayTraceY));
            this.swingArm(Hand.MAIN_HAND);
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
            if (this.mc.options.getCameraType() == PointOfView.FIRST_PERSON && !this.mc.player.isCreative() && !this.mc.player.isSpectator()) {
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
                            Evolution.LOGGER.warn("Unregistered shader id: {}", shader);
                        }
                    }
                }
            }
            else {
                if (this.mc.options.getCameraType() != this.previousPointOfView) {
                    this.previousPointOfView = this.mc.options.getCameraType();
                    this.currentShader = 0;
                }
                if (this.desiredShader != this.currentShader) {
                    this.currentShader = this.desiredShader;
                    ResourceLocation shaderLoc = this.getShader(this.desiredShader);
                    if (shaderLoc != null) {
                        this.mc.gameRenderer.loadEffect(shaderLoc);
                    }
                    else {
                        Evolution.LOGGER.warn("Unregistered shader id: {}", this.desiredShader);
                    }
                }
            }
            this.mc.getProfiler().popPush("lightMap");
            GameRenderer gameRenderer = this.mc.gameRenderer;
            if (gameRenderer != this.oldGameRenderer) {
                this.oldGameRenderer = gameRenderer;
                LIGHTMAP_FIELD.set(this.oldGameRenderer, new LightTextureEv(this.oldGameRenderer, this.mc));
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
                if (this.mc.player.getMainHandItem().getItem() instanceof ITwoHanded && !this.mc.player.getOffhandItem().isEmpty()) {
                    this.mainhandTimeSinceLastHit = 0;
                    LEFT_COUNTER_FIELD.set(this.mc, Integer.MAX_VALUE);
                    this.mc.player.displayClientMessage(EvolutionTexts.ACTION_TWO_HANDED, true);
                }
                //Prevents the player from attacking if on cooldown
                this.mc.getProfiler().popPush("cooldown");
                if (this.getMainhandCooledAttackStrength(0.0F) != 1 &&
                    this.mc.hitResult != null &&
                    this.mc.hitResult.getType() != RayTraceResult.Type.BLOCK) {
                    LEFT_COUNTER_FIELD.set(this.mc, Integer.MAX_VALUE);
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
                        Effect effect = instance.getEffect();
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
                        this.mc.player.setDeltaMovement(Vector3d.ZERO);
                    }
                }
                //Handle creative features
                this.mc.getProfiler().popPush("creative");
                if (this.mc.player.isCreative() && ClientProxy.BUILDING_ASSIST.isDown()) {
                    if (this.mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                        if (this.mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {
                            BlockPos pos = ((BlockRayTraceResult) this.mc.hitResult).getBlockPos();
                            if (!this.mc.level.getBlockState(pos).isAir(this.mc.level, pos)) {
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSChangeBlock((BlockRayTraceResult) this.mc.hitResult));
                                this.swingArm(Hand.MAIN_HAND);
                                this.mc.player.swing(Hand.MAIN_HAND);
                            }
                        }
                    }
                }
                //Handle swing
                this.mc.getProfiler().popPush("swing");
                this.ticks++;
                if (this.mc.gameMode.isDestroying()) {
                    this.swingArm(Hand.MAIN_HAND);
                }
                this.lunging = false;
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
        if (event.getEntity() instanceof ClientPlayerEntity && event.getEntity().equals(this.mc.player)) {
            STATS.set(this.mc.player, new EvolutionStatisticsManager());
            RECIPE_BOOK_FIELD.set(this.mc.player, new EvolutionRecipeBook());
        }
    }

    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        this.renderer.renderFog(event);
    }

    @SubscribeEvent
    public void onGUIMouseClickedPre(GuiScreenEvent.MouseClickedEvent.Pre event) {
        MouseButton button = MouseButton.fromGLFW(event.getButton());
        if (button != null) {
            if (this.onMouseClicked(event.getMouseX(), event.getMouseY(), button)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGUIMouseDragPre(GuiScreenEvent.MouseDragEvent.Pre event) {
        MouseButton button = MouseButton.fromGLFW(event.getMouseButton());
        if (button != null) {
            if (this.onMouseDrag(event.getMouseX(), event.getMouseY(), button)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGUIMouseReleasedPre(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        MouseButton button = MouseButton.fromGLFW(event.getButton());
        if (button != null) {
            if (this.onMouseReleased(button)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onGUIMouseScrollPost(GuiScreenEvent.MouseScrollEvent.Post event) {
        if (this.onMouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        Screen screen = event.getGui();
        if (screen instanceof InventoryScreen) {
            event.setCanceled(true);
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSOpenExtendedInventory());
        }
        else if (screen instanceof AdvancementsScreen) {
            event.setCanceled(true);
            this.mc.setScreen(new ScreenAdvancements(this.mc.getConnection().getAdvancements()));
        }
        else if (screen instanceof ControlsScreen && !(screen instanceof ScreenControls)) {
            event.setCanceled(true);
            this.mc.setScreen(new ScreenControls((ControlsScreen) event.getGui(), this.mc.options));
        }
        else if (screen instanceof StatsScreen) {
            event.setCanceled(true);
            this.mc.setScreen(new ScreenStats(this.mc.player.getStats()));
        }
        if (!event.isCanceled()) {
            onGuiOpen(event.getGui());
        }
    }

    @SubscribeEvent
    public void onGUIPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof IngameMenuScreen) {
            if (!event.getWidgetList().isEmpty()) {
                event.addWidget(new Button(event.getWidgetList().get(6).x,
                                           event.getWidgetList().get(6).y + 24,
                                           event.getWidgetList().get(6).getWidth(),
                                           event.getWidgetList().get(6).getHeight(),
                                           EvolutionTexts.GUI_MENU_MOD_OPTIONS,
                                           button -> this.mc.setScreen(new ModListScreen(event.getGui()))));
                Widget shareToLan = event.getWidgetList().get(6);
                shareToLan.x = event.getGui().width / 2 - 102;
                shareToLan.setWidth(204);
                Widget returnToMenu = event.getWidgetList().get(7);
                returnToMenu.y += 24;
                Widget menuOptions = event.getWidgetList().get(5);
                menuOptions.y += 24;
                Widget feedback = event.getWidgetList().get(3);
                Widget bugs = event.getWidgetList().get(4);
                event.removeWidget(feedback);
                event.removeWidget(bugs);
                String feedbackLink = "https://github.com/MGSchultz-13/Evolution/discussions/categories/feedback";
                feedback = new Button(event.getGui().width / 2 - 102,
                                      event.getGui().height / 4 + 72 - 16,
                                      98,
                                      20,
                                      EvolutionTexts.GUI_MENU_SEND_FEEDBACK,
                                      button -> this.mc.setScreen(new ConfirmOpenLinkScreen(b -> {
                                          if (b) {
                                              Util.getPlatform().openUri(feedbackLink);
                                          }
                                          this.mc.setScreen(event.getGui());
                                      }, feedbackLink, true)));
                String bugsLink = "https://github.com/MGSchultz-13/Evolution/issues";
                bugs = new Button(event.getGui().width / 2 + 4,
                                  event.getGui().height / 4 + 72 - 16,
                                  98,
                                  20,
                                  EvolutionTexts.GUI_MENU_REPORT_BUGS,
                                  button -> this.mc.setScreen(new ConfirmOpenLinkScreen(b -> {
                                      if (b) {
                                          Util.getPlatform().openUri(bugsLink);
                                      }
                                      this.mc.setScreen(event.getGui());
                                  }, bugsLink, true)));
                event.addWidget(feedback);
                event.addWidget(bugs);
            }
        }
    }

    public boolean onMouseClicked(double x, double y, MouseButton button) {
        if (handler == null) {
            return false;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(x, y);
        oldSelectedSlot = selectedSlot;
        ItemStack stackOnMouse = this.mc.player.inventory.getCarried();
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
        ItemStack stackOnMouse = this.mc.player.inventory.getCarried();
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
        if (handler == null || disableWheelForThisContainer) {
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
        ItemStack stackOnMouse = this.mc.player.inventory.getCarried();
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
    public void onPlayerInput(InputUpdateEvent event) {
        MovementInput movementInput = event.getMovementInput();
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
            boolean hasNausea = this.mc.player.hasEffect(Effects.CONFUSION);
            float swimAnimation = MathHelper.getSwimAnimation(this.mc.player, event.getPartialRenderTick());
            Pose pose = this.mc.player.getPose();
            boolean isGettingUpFromCrawling = pose != Pose.SWIMMING && swimAnimation > 0;
            boolean isInWater = this.mc.player.isInWater();
            boolean isGoingCrawling = pose == Pose.SWIMMING && !isInWater && swimAnimation < 1;
            if (hasNausea || isGettingUpFromCrawling || isGoingCrawling || this.wasPreviousInWater(7) != isInWater) {
                renderer.getModel().head.visible = false;
                renderer.getModel().hat.visible = false;
            }
            if (isGettingUpFromCrawling || isGoingCrawling) {
                renderer.getModel().body.visible = false;
                renderer.getModel().jacket.visible = false;
            }
            this.wasInWater <<= 1;
            this.wasInWater |= isInWater ? 1 : 0;
        }
    }

    public void onPotionAdded(ClientEffectInstance instance, PacketSCAddEffect.Logic logic) {
        switch (logic) {
            case ADD:
            case REPLACE: {
                EFFECTS_TO_ADD.add(instance);
                break;
            }
            case UPDATE: {
                removeEffect(EFFECTS, instance.getEffect());
                int index = getIndexAndRemove(EFFECTS_TO_ADD, instance.getEffect());
                if (index != -1) {
                    EFFECTS_TO_ADD.add(index, instance);
                }
                else {
                    EFFECTS.add(instance);
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderBlockHighlight(DrawHighlightEvent.HighlightBlock event) {
        this.onRenderHightlight(event);
    }

    @SubscribeEvent
    public void onRenderEntityHighlight(DrawHighlightEvent.HighlightEntity event) {
        this.onRenderHightlight(event);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (this.mc.player.getVehicle() != null) {
            ForgeIngameGui.renderFood = true;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
            this.renderer.renderCrosshair(event.getMatrixStack(), event.getPartialTicks());
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH) {
            event.setCanceled(true);
            this.renderer.renderHealth(event.getMatrixStack());
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS) {
            event.setCanceled(true);
            this.renderer.renderPotionIcons(event.getMatrixStack(), event.getPartialTicks());
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD) {
            event.setCanceled(true);
            this.renderer.renderFoodAndThirst(event.getMatrixStack());
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        event.setCanceled(true);
        boolean sleeping = this.mc.getCameraEntity() instanceof LivingEntity && ((LivingEntity) this.mc.getCameraEntity()).isSleeping();
        if (this.mc.options.getCameraType() == PointOfView.FIRST_PERSON &&
            !sleeping &&
            !this.mc.options.hideGui &&
            this.mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.mc.gameRenderer.lightTexture().turnOnLightLayer();
            this.renderer.renderItemInFirstPerson(event.getMatrixStack(), event.getBuffers(), event.getLight(), event.getPartialTicks());
            this.mc.gameRenderer.lightTexture().turnOffLightLayer();
        }
    }

    private void onRenderHightlight(DrawHighlightEvent event) {
        event.setCanceled(true);
        MatrixStack matrices = event.getMatrix();
        IRenderTypeBuffer buffer = event.getBuffers();
        ActiveRenderInfo renderInfo = event.getInfo();
        RayTraceResult rayTrace = this.mc.hitResult;
        if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockRayTraceResult) rayTrace).getBlockPos();
            if (this.mc.level.getWorldBorder().isWithinBounds(hitPos)) {
                Block block = this.mc.level.getBlockState(hitPos).getBlock();
                if (block instanceof BlockKnapping) {
                    TEKnapping tile = (TEKnapping) this.mc.level.getBlockEntity(hitPos);
                    this.renderer.renderOutlines(matrices, buffer, tile.type.getShape(), renderInfo, hitPos);
                }
                else if (block instanceof BlockMolding) {
                    TEMolding tile = (TEMolding) this.mc.level.getBlockEntity(hitPos);
                    this.renderer.renderOutlines(matrices, buffer, tile.molding.getShape(), renderInfo, hitPos);
                }
                this.renderer.renderBlockOutlines(matrices, buffer, renderInfo, hitPos);
            }
        }
        else if (rayTrace != null && rayTrace.getType() == RayTraceResult.Type.ENTITY) {
            if (this.mc.getEntityRenderDispatcher().shouldRenderHitBoxes() && rayTrace instanceof AdvancedEntityRayTraceResult) {
                AdvancedEntityRayTraceResult advRayTrace = (AdvancedEntityRayTraceResult) rayTrace;
                if (advRayTrace.getHitbox() != null) {
                    this.renderer.renderHitbox(matrices,
                                               buffer,
                                               advRayTrace.getEntity(),
                                               advRayTrace.getHitbox(),
                                               renderInfo,
                                               event.getPartialTicks());
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
                                       renderInfo,
                                       event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderMissHighlight(DrawHighlightEvent event) {
        this.onRenderHightlight(event);
    }

    public void performLungeMovement() {
        if (!this.lunging && this.mc.player.isOnGround() && this.mc.player.zza > 0) {
            this.lunging = true;
            Vector3d oldMotion = this.mc.player.getDeltaMovement();
            float sinFacing = MathHelper.sinDeg(this.mc.player.yRot);
            float cosFacing = MathHelper.cosDeg(this.mc.player.yRot);
            double lungeBoost = 0.15;
            this.mc.player.setDeltaMovement(oldMotion.x - lungeBoost * sinFacing, oldMotion.y, oldMotion.z + lungeBoost * cosFacing);
        }
    }

    public void performMainhandLunge(ItemStack mainhandStack, float strength) {
        this.mainhandTimeSinceLastHit = 0;
        this.renderer.resetFullEquipProgress(Hand.MAIN_HAND);
        double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getLocation().y : Double.NaN;
        int slot = Integer.MIN_VALUE;
        for (int i = 0; i < this.mc.player.inventory.items.size(); i++) {
            if (this.mc.player.inventory.items.get(i).equals(mainhandStack, false)) {
                slot = i;
                break;
            }
        }
        if (slot == Integer.MIN_VALUE) {
            if (this.mc.player.inventory.items.get(0).equals(mainhandStack, false)) {
                slot = -1;
            }
        }
        if (slot != Integer.MIN_VALUE) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLunge(this.leftPointedEntity, Hand.MAIN_HAND, rayTraceY, slot, strength));
        }
        else {
            Evolution.LOGGER.warn("Unable to find lunge stack: {}", mainhandStack);
        }
    }

    public void performOffhandLunge(ItemStack offhandStack, float strength) {
        this.offhandTimeSinceLastHit = 0;
        this.renderer.resetFullEquipProgress(Hand.OFF_HAND);
        double rayTraceY = this.rightRayTrace != null ? this.rightRayTrace.getLocation().y : Double.NaN;
        int slot = Integer.MIN_VALUE;
        if (this.mc.player.inventory.offhand.get(0).equals(offhandStack, false)) {
            slot = -1;
        }
        if (slot == Integer.MIN_VALUE) {
            for (int i = 0; i < this.mc.player.inventory.items.size(); i++) {
                if (this.mc.player.inventory.items.get(i).equals(offhandStack, false)) {
                    slot = i;
                    break;
                }
            }
        }
        if (slot != Integer.MIN_VALUE) {
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLunge(this.rightPointedEntity, Hand.OFF_HAND, rayTraceY, slot, strength));
        }
        else {
            Evolution.LOGGER.warn("Unable to find lunge stack: {}", offhandStack);
        }
    }

    @SubscribeEvent
    public void renderTooltip(RenderTooltipEvent.PostText event) {
        this.renderer.renderTooltip(event);
    }

    public void rightMouseClick(IOffhandAttackable item) {
        float cooldown = getRightCooldownPeriod(item);
        if (this.offhandTimeSinceLastHit >= cooldown) {
            this.offhandTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getLocation().y : Double.NaN;
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.rightPointedEntity, Hand.OFF_HAND, rayTraceY));
            this.swingArm(Hand.OFF_HAND);
        }
    }

    public void setCameraPos(Vector3d cameraPos) {
        this.cameraPos = cameraPos;
    }

    private boolean shouldBeProbe(PlayerEntity player) {
        if (player.isInWater()) {
            return false;
        }
        if (player.isInLava()) {
            return false;
        }
        return !player.onClimbable() || !this.isJumpPressed && player.isOnGround();
    }

    public boolean shouldRenderPlayer() {
        return EvolutionConfig.CLIENT.firstPersonRenderer.get();
    }

    @SubscribeEvent
    public void shutDownInternalServer(FMLServerStoppedEvent event) {
        if (this.inverted) {
            this.inverted = false;
        }
    }

    public void swingArm(Hand hand) {
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
            ItemStack stack = this.mc.player.inventory.items.get(i);
            if (stack.getItem() instanceof IBackWeapon) {
                int stackPriority = ((IBackWeapon) stack.getItem()).getPriority();
                if (priority > stackPriority) {
                    backStack = stack;
                    priority = stackPriority;
                    chosen = i;
                    if (priority == 0) {
                        break;
                    }
                }
            }
        }
        if (chosen == this.mc.player.inventory.selected) {
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
            ItemStack stack = this.mc.player.inventory.items.get(i);
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
        if (chosen == this.mc.player.inventory.selected) {
            beltStack = ItemStack.EMPTY;
        }
        if (!ItemStack.matches(beltStack, oldStack)) {
            if (beltStack.getItem() instanceof ItemSword) {
                this.mc.getSoundManager()
                       .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_SHEATHE.get(), SoundCategory.PLAYERS, 0.8f, 1.0f));
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlaySoundEntityEmitted(this.mc.player,
                                                                                          EvolutionSounds.SWORD_SHEATHE.get(),
                                                                                          SoundCategory.PLAYERS,
                                                                                          0.8f,
                                                                                          1.0f));
            }
            BELT_ITEMS.put(this.mc.player.getId(), beltStack.copy());
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdateBeltBackItem(beltStack, false));
        }
    }

    private void updateClientProneState(PlayerEntity player) {
        if (player != null) {
            boolean shouldBeProne = ClientProxy.TOGGLE_PRONE.isDown() != this.proneToggle;
            shouldBeProne = shouldBeProne && this.shouldBeProbe(player);
            BlockPos pos = player.blockPosition().above(2);
            shouldBeProne = shouldBeProne || this.proneToggle && player.onClimbable() && player.level.getBlockState(pos).getMaterial().blocksMotion();
            if (shouldBeProne != Evolution.PRONED_PLAYERS.getOrDefault(player.getId(), false)) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetProne(shouldBeProne));
            }
            Evolution.PRONED_PLAYERS.put(player.getId(), shouldBeProne);
        }
    }

    public void updateClientTickrate(float tickrate) {
        if (this.tps == tickrate) {
            return;
        }
        Evolution.LOGGER.info("Updating client tickrate to " + tickrate);
        this.tps = tickrate;
        Timer timer = TIMER_FIELD.get(this.mc);
        TICKRATE_FIELD.set(timer, 1_000.0F / tickrate);
    }

    private boolean wasPreviousInWater(int frame) {
        return (this.wasInWater & 1 << frame) != 0;
    }
}
