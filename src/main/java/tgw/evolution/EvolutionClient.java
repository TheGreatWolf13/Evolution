package tgw.evolution;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.blocks.BlockGeneric;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.client.gui.*;
import tgw.evolution.client.gui.overlays.EvolutionOverlays;
import tgw.evolution.client.gui.overlays.VanillaOverlays;
import tgw.evolution.client.gui.toast.ToastCustomRecipe;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.renderer.DimensionOverworld;
import tgw.evolution.client.renderer.RenderLayer;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.client.renderer.chunk.ClientIntegrityStorage;
import tgw.evolution.client.tooltip.TooltipManager;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.client.util.Shader;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.*;
import tgw.evolution.items.IBackWeapon;
import tgw.evolution.items.IBeltWeapon;
import tgw.evolution.items.IMelee;
import tgw.evolution.items.ITwoHanded;
import tgw.evolution.mixin.AccessorInputConstants_Type;
import tgw.evolution.network.*;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.HitInformation;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.I2OHashMap;
import tgw.evolution.util.collection.maps.I2OMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.toast.ToastHolderRecipe;
import tgw.evolution.util.toast.Toasts;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class EvolutionClient implements ClientModInitializer {

    public static final O2OMap<String, KeyMapping> ALL_KEYMAPPING = new O2OHashMap<>();
    public static final KeyMapping KEY_BUILDING_ASSIST;
    public static final KeyMapping KEY_CRAWL;
    public static final OList<ClientEffectInstance> EFFECTS_TO_ADD = new OArrayList<>();
    public static final OList<ClientEffectInstance> EFFECTS = new OArrayList<>();

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
    public static final I2OMap<ItemStack> BELT_ITEMS = new I2OHashMap<>();
    public static final I2OMap<ItemStack> BACK_ITEMS = new I2OHashMap<>();
    public static final ClientIntegrityStorage CLIENT_INTEGRITY_STORAGE = new ClientIntegrityStorage();
    private static final HitInformation MAINHAND_HITS = new HitInformation();
    private static final BlockHitResult[] MAINHAND_HIT_RESULT = new BlockHitResult[1];
    private static final Matrix4f PROJ_MATRIX = new Matrix4f();
    private static final Matrix4f MODEL_VIEW_MATRIX = new Matrix4f();
    private static final ISet CURRENT_SHADERS = new IHashSet();
    private static final ISet FORCED_SHADERS = new IHashSet();
    private static final ISet DESIRED_SHADERS = new IHashSet();
    private static double accumulatedScrollDelta;
    private static @Nullable IMelee.IAttackType cachedAttackType;
    private static int cameraId = -1;
    private static boolean canDoLMBDrag;
    private static boolean canDoRMBDrag;
    private static @Nullable DimensionOverworld dimension;
    private static @Nullable DynamicLights dynamicLights;
    public static int effectToAddTicks;
    public static float fov;
    private static @Nullable IGuiScreenHandler handler;
    private static boolean initialized;
    private static @Range(from = 0, to = 1) int lastInventoryTab;
    public static @Nullable Entity leftPointedEntity;
    public static @Nullable EntityHitResult leftRayTrace;
    private static int mainhandCooldownTime;
    private static Minecraft mc;
    private static int offhandCooldownTime;
    private static @Nullable Slot oldSelectedSlot;
    private static ClientRenderer renderer;
    public static @Nullable Entity rightPointedEntity;
    public static @Nullable EntityHitResult rightRayTrace;
    private static @Nullable SkyRenderer skyRenderer;
    private static int ticks;
    public static int ticksToLoseConscious;
    private static float tps = 20.0f;
    private static boolean wasSpecialAttacking;

    static {
        KEY_BUILDING_ASSIST = new KeyMapping("key.build_assist", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "key.categories.creative");
        KEY_CRAWL = new ToggleKeyMapping("key.crawl", GLFW.GLFW_KEY_X, "key.categories.movement", () -> EvolutionConfig.toggleCrawl);
    }

    public static void addCustomRecipeToast(int id) {
        for (ToastHolderRecipe toast : Toasts.getHolderForId(id)) {
            ToastCustomRecipe.addOrUpdate(mc.getToasts(), toast);
        }
    }

    public static void allChanged() {
        if (dynamicLights != null) {
            dynamicLights.clear();
        }
        CURRENT_SHADERS.clear();
        mc.gameRenderer.shutdownAllShaders();
    }

    private static boolean areStacksCompatible(ItemStack a, ItemStack b) {
        return a.isEmpty() || b.isEmpty() || a.sameItem(b) && ItemStack.tagMatches(a, b);
    }

    /**
     * @return Whether the current hit result situation allows for short attacking, i.e. if the player is holding an axe while looking at a
     * chopping block that has a log, the player won't short attack, so return false.
     */
    public static boolean checkHitResultBeforeShortAttack() {
        if (mc.hitResult instanceof BlockHitResult blockHitResult) {
            assert mc.level != null;
            assert mc.player != null;
            int x = blockHitResult.posX();
            int y = blockHitResult.posY();
            int z = blockHitResult.posZ();
            BlockState state = mc.level.getBlockState_(x, y, z);
            return !(state.getBlock() instanceof BlockGeneric block) || !block.preventsShortAttacking(mc.level, x, y, z, state, mc.player);
        }
        return true;
    }

    public static void clearMemory() {
        EFFECTS.reset();
        EFFECTS_TO_ADD.reset();
        MAINHAND_HITS.clearMemory();
        BELT_ITEMS.reset();
        BACK_ITEMS.reset();
        CLIENT_INTEGRITY_STORAGE.clear();
        DESIRED_SHADERS.reset();
        CURRENT_SHADERS.reset();
        FORCED_SHADERS.reset();
        if (mc.level == null) {
            updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            if (mc.isMultiplayerPaused()) {
                Evolution.info("Resuming client");
                mc.setMultiplayerPaused(false);
            }
            ticks = 0;
        }
        dynamicLights = null;
        if (dimension != null || skyRenderer != null) {
            if (skyRenderer != null) {
                skyRenderer.clearMemory();
                skyRenderer = null;
            }
            dimension = null;
        }
    }

    public static boolean containsEffect(OList<ClientEffectInstance> list, MobEffect effect) {
        for (int i = 0, l = list.size(); i < l; i++) {
            if (list.get(i).getEffect() == effect) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable IGuiScreenHandler findHandler(Screen currentScreen) {
        if (currentScreen instanceof CreativeModeInventoryScreen creativeScreen) {
            return new GuiContainerCreativeHandler(creativeScreen);
        }
        if (currentScreen instanceof AbstractContainerScreen containerScreen) {
            return new GuiContainerHandler(containerScreen);
        }
        return null;
    }

    private static @Nullable Slot findPullSlot(List<Slot> slots, Slot selectedSlot) {
        int startIndex = 0;
        int endIndex = slots.size();
        int direction = 1;
        ItemStack selectedSlotStack = selectedSlot.getItem();
        assert mc.player != null;
        boolean findInPlayerInventory = selectedSlot.container != mc.player.getInventory();
        for (int i = startIndex; i != endIndex; i += direction) {
            Slot slot = slots.get(i);
            assert handler != null;
            if (handler.isIgnored(slot)) {
                continue;
            }
            boolean slotInPlayerInventory = slot.container == mc.player.getInventory();
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

    private static @Nullable OList<Slot> findPushSlots(List<Slot> slots, Slot selectedSlot, int itemCount, boolean mustDistributeAll) {
        ItemStack selectedSlotStack = selectedSlot.getItem();
        assert mc.player != null;
        boolean findInPlayerInventory = selectedSlot.container != mc.player.getInventory();
        OList<Slot> rv = new OArrayList<>();
        OList<Slot> goodEmptySlots = new OArrayList<>();
        for (int i = 0; i != slots.size() && itemCount > 0; i++) {
            Slot slot = slots.get(i);
            assert handler != null;
            if (handler.isIgnored(slot)) {
                continue;
            }
            boolean slotInPlayerInventory = slot.container == mc.player.getInventory();
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

    public static Component getAttackKeyText() {
        return mc.options.keyAttack.getTranslatedKeyMessage();
    }

    private static ItemStack getBeltStack() {
        assert mc.player != null;
        ItemStack beltStack = ItemStack.EMPTY;
        int priority = Integer.MAX_VALUE;
        int chosen = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().items.get(i);
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
        if (chosen == mc.player.getInventory().selected) {
            return ItemStack.EMPTY;
        }
        return beltStack;
    }

    public static ClientLevel getClientLevel() {
        assert mc.level != null;
        return mc.level;
    }

    public static LocalPlayer getClientPlayer() {
        assert mc.player != null;
        return mc.player;
    }

    private static ISet getDesiredShaders() {
        assert mc.player != null;
        ISet desiredShaders = DESIRED_SHADERS;
        desiredShaders.clear();
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            if (!mc.player.isCreative() && !mc.player.isSpectator() && mc.player.equals(mc.getCameraEntity())) {
                float health = mc.player.getHealth();
                if (health <= 12.5f) {
                    desiredShaders.add(Shader.DESATURATE_25);
                }
                else if (health <= 25) {
                    desiredShaders.add(Shader.DESATURATE_50);
                }
                else if (health <= 50) {
                    desiredShaders.add(Shader.DESATURATE_75);
                }
            }
        }
        return desiredShaders;
    }

    public static @Nullable DimensionOverworld getDimension() {
        return dimension;
    }

    public static @Nullable Direction getDirectionFromInput(Direction facing, Input input) {
        Direction movement = null;
        if (input.leftImpulse != 0) {
            if (input.leftImpulse > 0) {
                movement = Direction.WEST;
            }
            else {
                movement = Direction.EAST;
            }
        }
        else if (input.forwardImpulse != 0) {
            if (input.forwardImpulse > 0) {
                movement = Direction.NORTH;
            }
            else {
                movement = Direction.SOUTH;
            }
        }
        else if (input.jumping) {
            movement = Direction.UP;
        }
        else if (input.shiftKeyDown) {
            movement = Direction.DOWN;
        }
        if (movement == null) {
            return null;
        }
        if (movement.getAxis() == Direction.Axis.Y) {
            return movement;
        }
        return DirectionUtil.fromLocalToAbs(movement, facing);
    }

    public static DynamicLights getDynamicLights() {
        assert dynamicLights != null;
        return dynamicLights;
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

    public static float getItemCooldown(InteractionHand hand) {
        assert mc.player != null;
        ItemStack stack = mc.player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item instanceof IMelee melee) {
            return melee.getCooldown(stack);
        }
        return (float) (20 / PlayerHelper.ATTACK_SPEED);
    }

    public static @Range(from = 0, to = 1) int getLastInventoryTab() {
        return lastInventoryTab;
    }

    public static float getMainhandIndicatorPercentage(float partialTicks) {
        assert mc.player != null;
        if (mc.player.isSpecialAttacking() || mc.player.isLockedInSpecialAttack()) {
            return mc.player.getSpecialAttackProgress(partialTicks);
        }
        return MathHelper.clamp((mainhandCooldownTime + partialTicks) / getItemCooldown(InteractionHand.MAIN_HAND), 0.0F, 1.0F);
    }

    public static float getOffhandIndicatorPercentage(float partialTicks) {
        return MathHelper.clamp((offhandCooldownTime + partialTicks) / getItemCooldown(InteractionHand.OFF_HAND), 0.0F, 1.0F);
    }

    public static float getPartialTicks() {
        if (mc.isPaused()) {
            return mc.pausePartialTick;
        }
        return mc.getFrameTime();
    }

    public static float getPitchMul() {
        if (!isInitialized()) {
            return 1.0f;
        }
        assert mc.player != null;
        if (mc.player.isDeadOrDying()) {
            float f = 1.0f - Mth.cos(Mth.HALF_PI * ticksToLoseConscious / 80.0f);
            return f * f * 0.5f + 0.5f;
        }
        return 1.0f;
    }

    public static ClientRenderer getRenderer() {
        return renderer;
    }

    public static @Nullable ResourceLocation getShader(@Shader int shaderId) {
        return switch (shaderId) {
            case Shader.MOTION_BLUR -> EvolutionResources.SHADER_MOTION_BLUR;
            case Shader.DESATURATE_25 -> EvolutionResources.SHADER_DESATURATE_25;
            case Shader.DESATURATE_50 -> EvolutionResources.SHADER_DESATURATE_50;
            case Shader.DESATURATE_75 -> EvolutionResources.SHADER_DESATURATE_75;
            case Shader.TEST -> EvolutionResources.SHADER_TEST;
            default -> null;
        };
    }

    public static SkinType getSkinType() {
        return "default".equals(getClientPlayer().getModelName()) ? SkinType.STEVE : SkinType.ALEX;
    }

    public static @Nullable SkyRenderer getSkyRenderer() {
        return skyRenderer;
    }

    public static int getTicks() {
        return ticks;
    }

    public static float getVolumeMultiplier() {
        if (!isInitialized()) {
            return 1.0f;
        }
        assert mc.player != null;
        if (mc.player.isDeadOrDying()) {
            float f = 1.0f - Mth.cos(Mth.HALF_PI * ticksToLoseConscious / 80.0f);
            return f * f;
        }
        return 1.0f;
    }

    public static void handleShaderPacket(@Shader int shaderId) {
        assert mc.player != null;
        switch (shaderId) {
            case Shader.QUERY -> {
                Component message = CURRENT_SHADERS.isEmpty() ? EvolutionTexts.COMMAND_SHADER_NO_SHADER : new TranslatableComponent("command.evolution.shader.query", CURRENT_SHADERS.stream()
                                                                                                                                                                                     .sorted(Integer::compareTo)
                                                                                                                                                                                     .map(String::valueOf)
                                                                                                                                                                                     .collect(Collectors.joining(", "))
                );
                mc.player.displayClientMessage(message, false);
                return;
            }
            case Shader.TOGGLE -> {
                mc.gameRenderer.togglePostEffect();
                if (mc.gameRenderer.effectActive) {
                    mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_ON, false);
                }
                else {
                    mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_TOGGLE_OFF, false);
                }
                return;
            }
            case Shader.CYCLE -> {
                mc.gameRenderer.cycleEffect();
                return;
            }
            case Shader.CLEAR -> {
                mc.player.displayClientMessage(EvolutionTexts.COMMAND_SHADER_RESET, false);
                FORCED_SHADERS.clear();
                return;
            }
        }
        if (hasShader(shaderId)) {
            mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.success", shaderId), false);
            FORCED_SHADERS.add(shaderId);
        }
        else {
            mc.player.displayClientMessage(new TranslatableComponent("command.evolution.shader.fail", shaderId).withStyle(ChatFormatting.RED), false);
        }
    }

    public static boolean hasShader(@Shader int shaderId) {
        return getShader(shaderId) != null;
    }

    public static void incrementCooldown(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            mainhandCooldownTime++;
        }
        else {
            offhandCooldownTime++;
        }
    }

    public static void init(Minecraft minecraft) {
        mc = minecraft;
        mc.getMainRenderTarget().enableStencil();
        ColorManager.registerBlockColorHandlers(mc.getBlockColors());
        ColorManager.registerItemColorHandlers(mc.getItemColors());
        renderer = new ClientRenderer(mc);
        Evolution.info("Client initialized");
    }

    public static void init() {
        assert mc.level != null;
        //Bind Sky Renderer
        dimension = new DimensionOverworld();
        skyRenderer = new SkyRenderer(dimension);
        //Load skin for corpses
        GameProfileCache playerProfile = SkullBlockEntity.profileCache;
        MinecraftSessionService session = SkullBlockEntity.sessionService;
        if (playerProfile != null && session != null) {
            EntityPlayerCorpse.setProfileCache(playerProfile);
            EntityPlayerCorpse.setSessionService(session);
        }
        mc.options.autoJump = false;
        dynamicLights = new DynamicLights(mc.level);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * @return Whether the MouseHandler should return early.
     */
    public static boolean onGUIMouseClickedPre(double mouseX, double mouseY, @MouseButton int button) {
        if (handler == null) {
            return false;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(mouseX, mouseY);
        oldSelectedSlot = selectedSlot;
        assert mc.player != null;
        ItemStack stackOnMouse = mc.player.inventoryMenu.getCarried();
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

    public static void onGUIMouseDragPre(double mouseX, double mouseY, @MouseButton int button) {
        if (handler == null) {
            return;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(mouseX, mouseY);
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
        assert mc.player != null;
        ItemStack stackOnMouse = mc.player.inventoryMenu.getCarried();
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

    /**
     * @return Whether the MouseHandler should return early.
     */
    public static boolean onGUIMouseReleasedPre(@MouseButton int button) {
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

    public static void onGUIMouseScrollPost(double mouseX, double mouseY, double scrollDelta) {
        if (handler == null) {
            return;
        }
        Slot selectedSlot = handler.getSlotUnderMouse(mouseX, mouseY);
        if (selectedSlot == null || handler.isIgnored(selectedSlot)) {
            return;
        }
        double scaledDelta = Math.signum(scrollDelta);
        if (accumulatedScrollDelta != 0 && scaledDelta != Math.signum(accumulatedScrollDelta)) {
            accumulatedScrollDelta = 0;
        }
        accumulatedScrollDelta += scaledDelta;
        int delta = (int) accumulatedScrollDelta;
        accumulatedScrollDelta -= delta;
        if (delta == 0) {
            return;
        }
        List<Slot> slots = handler.getSlots();
        ItemStack selectedSlotStack = selectedSlot.getItem();
        if (selectedSlotStack.isEmpty()) {
            return;
        }
        assert mc.player != null;
        ItemStack stackOnMouse = mc.player.inventoryMenu.getCarried();
        int numItemsToMove = Math.abs(delta);
        boolean pushItems = delta < 0;
        if (handler.isCraftingOutput(selectedSlot)) {
            if (!areStacksCompatible(selectedSlotStack, stackOnMouse)) {
                return;
            }
            if (stackOnMouse.isEmpty()) {
                if (!pushItems) {
                    return;
                }
                while (numItemsToMove-- > 0) {
                    OList<Slot> targetSlots = findPushSlots(slots, selectedSlot, selectedSlotStack.getCount(), true);
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
            return;
        }
        if (!stackOnMouse.isEmpty() && areStacksCompatible(selectedSlotStack, stackOnMouse)) {
            return;
        }
        if (pushItems) {
            if (!stackOnMouse.isEmpty() && !selectedSlot.mayPlace(stackOnMouse)) {
                return;
            }
            numItemsToMove = Math.min(numItemsToMove, selectedSlotStack.getCount());
            OList<Slot> targetSlots = findPushSlots(slots, selectedSlot, numItemsToMove, false);
            assert targetSlots != null;
            if (targetSlots.isEmpty()) {
                return;
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
            return;
        }
        int maxItemsToMove = selectedSlotStack.getMaxStackSize() - selectedSlotStack.getCount();
        numItemsToMove = Math.min(numItemsToMove, maxItemsToMove);
        while (numItemsToMove > 0) {
            Slot targetSlot = findPullSlot(slots, selectedSlot);
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

    public static void onPotionAdded(ClientEffectInstance instance, PacketSCAddEffect.Logic logic) {
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

    public static void postClientTick() {
        if (tickStart()) {
            return;
        }
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        assert level != null;
        assert player != null;
        if (!mc.isPaused()) {
            //Remove inactive effects
            mc.getProfiler().push("effects");
            if (!EFFECTS.isEmpty()) {
                boolean needsRemoving = false;
                for (int i = 0, l = EFFECTS.size(); i < l; i++) {
                    ClientEffectInstance instance = EFFECTS.get(i);
                    MobEffect effect = instance.getEffect();
                    if (instance.getDuration() == 0 || !player.hasEffect(effect) && ticks >= 100) {
                        needsRemoving = true;
                    }
                    else {
                        instance.tick();
                    }
                }
                if (needsRemoving) {
                    for (int i = 0; i < EFFECTS.size(); i++) {
                        ClientEffectInstance instance = EFFECTS.get(i);
                        if (instance.getDuration() == 0 || !player.hasEffect(instance.getEffect())) {
                            EFFECTS.remove(i--);
                        }
                    }
                }
            }
            if (!EFFECTS_TO_ADD.isEmpty()) {
                effectToAddTicks++;
            }
            else {
                renderer.isAddingEffect = false;
            }
            //Handle creative features
            mc.getProfiler().popPush("creative");
            if (player.isCreative() && KEY_BUILDING_ASSIST.isDown()) {
                if (player.getMainHandItem().getItem() instanceof BlockItem) {
                    assert mc.hitResult != null;
                    if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult hitResult = (BlockHitResult) mc.hitResult;
                        if (!level.getBlockState_(hitResult.posX(), hitResult.posY(), hitResult.posZ()).isAir()) {
                            player.displayClientMessage(new TextComponent("Not implemented"), false);
//                            this.mc.player.connection.send(new PacketCSChangeBlock((BlockHitResult) this.mc.hitResult));
                            player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
            //Handle swing
            mc.getProfiler().popPush("swing");
            assert mc.gameMode != null;
            if (player.isSpecialAttacking()) {
                cachedAttackType = player.getSpecialAttackType();
                if (player.isInHitTicks()) {
                    MathHelper.collideOBBWithCollider(MAINHAND_HITS, player, 1.0f, MAINHAND_HIT_RESULT, true, false);
                    BlockHitResult hitResult = MAINHAND_HIT_RESULT[0];
                    if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                        player.stopSpecialAttack(IMelee.StopReason.HIT_BLOCK);
                        Evolution.info("Collided with {} at [{}, {}, {}] on [{}, {}, {}]", level.getBlockState_(hitResult.posX(), hitResult.posY(), hitResult.posZ()), hitResult.posX(), hitResult.posY(), hitResult.posZ(), hitResult.x(), hitResult.y(), hitResult.z());
                    }
                }
            }
            else {
                if (!MAINHAND_HITS.isEmpty() && cachedAttackType != null) {
                    MAINHAND_HITS.sendHits(cachedAttackType);
                    MAINHAND_HITS.clear();
                }
                cachedAttackType = null;
                MAINHAND_HIT_RESULT[0] = null;
            }
            //Ticks renderer
            mc.getProfiler().popPush("renderer");
            renderer.endTick();
            mc.getProfiler().pop();
        }
    }

    public static void preClientTick() {
        if (tickStart()) {
            return;
        }
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        assert level != null;
        assert player != null;
        ProfilerFiller profiler = mc.getProfiler();
        //Camera
        profiler.push("camera");
        if (cameraId != -1) {
            Entity entity = level.getEntity(cameraId);
            if (entity != null) {
                cameraId = -1;
                mc.setCameraEntity(entity);
            }
        }
        //Apply shaders
        profiler.popPush("shaders");
        ISet desiredShaders = getDesiredShaders();
        desiredShaders.addAll(FORCED_SHADERS);
        ISet currentShaders = CURRENT_SHADERS;
        if (desiredShaders.isEmpty()) {
            if (!currentShaders.isEmpty()) {
                currentShaders.clear();
                mc.gameRenderer.shutdownAllShaders();
            }
        }
        else {
            if (!desiredShaders.containsAll(currentShaders)) {
                for (long it = currentShaders.beginIteration(); currentShaders.hasNextIteration(it); it = currentShaders.nextEntry(it)) {
                    //noinspection MagicConstant
                    @Shader int shader = currentShaders.getIteration(it);
                    if (!desiredShaders.contains(shader)) {
                        it = currentShaders.removeIteration(it);
                        mc.gameRenderer.shutdownShader(shader);
                    }
                }
            }
            if (!currentShaders.containsAll(desiredShaders)) {
                for (long it = desiredShaders.beginIteration(); desiredShaders.hasNextIteration(it); it = desiredShaders.nextEntry(it)) {
                    //noinspection MagicConstant
                    @Shader int shader = desiredShaders.getIteration(it);
                    if (currentShaders.add(shader)) {
                        ResourceLocation shaderLoc = getShader(shader);
                        if (shaderLoc != null) {
                            mc.gameRenderer.loadShader(shader, shaderLoc);
                        }
                        else {
                            Evolution.warn("Unregistered shader id: {}", shader);
                        }
                    }
                }
            }
        }
        profiler.pop();
        if (!mc.isPaused()) {
            assert dynamicLights != null;
            ++ticks;
            if (player.isDeadOrDying()) {
                if (ticksToLoseConscious > 0) {
                    --ticksToLoseConscious;
                }
            }
            profiler.push("dimension");
            assert dimension != null;
            dimension.tick();
            profiler.popPush("renderer");
            renderer.startTick();
            profiler.popPush("updateHeld");
            updateBeltItem();
            updateBackItem();
            //Handle two-handed items
            profiler.popPush("twoHanded");
            ItemStack mainHandStack = player.getMainHandItem();
            if (mainHandStack.getItem() instanceof ITwoHanded twoHanded &&
                twoHanded.isTwoHanded(mainHandStack) &&
                !player.getOffhandItem().isEmpty()) {
                mainhandCooldownTime = 0;
                mc.missTime = Integer.MAX_VALUE;
                player.displayClientMessage(EvolutionTexts.ACTION_TWO_HANDED, true);
            }
            //Prevents the player from attacking if on cooldown
            profiler.popPush("cooldown");
            boolean isSpecialAttacking = player.shouldRenderSpecialAttack();
            if (wasSpecialAttacking && !isSpecialAttacking) {
                resetCooldown(InteractionHand.MAIN_HAND);
            }
            if (getMainhandIndicatorPercentage(0.0F) != 1 &&
                mc.hitResult != null &&
                mc.hitResult.getType() != HitResult.Type.BLOCK) {
                mc.missTime = Integer.MAX_VALUE;
            }
            wasSpecialAttacking = isSpecialAttacking;
            if (EvolutionConfig.DL_ENABLED.get() && ticks > 20) {
                profiler.popPush("dynamicLight");
                int tickrate = EvolutionConfig.DL_TICKRATE.get();
                if (tickrate == 1 || ticks % tickrate == 0) {
                    dynamicLights.tickStart();
                    boolean items = EvolutionConfig.DL_ITEMS.get();
                    boolean entities = EvolutionConfig.DL_ENTITIES.get();
                    for (Entity entity : level.entitiesForRendering()) {
                        if (updateDynamicLight(entity, items, entities)) {
                            dynamicLights.update(entity);
                        }
                    }
                    dynamicLights.tickEnd();
                }
                profiler.pop();
            }
        }
    }

    private static void registerScreens() {
        MenuScreens.register(EvolutionContainers.EXTENDED_INVENTORY, ScreenInventory::new);
        MenuScreens.register(EvolutionContainers.CORPSE, ScreenCorpse::new);
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
            effectToAddTicks = 0;
        }
    }

    public static void removeVanillaLevelGenerators() {
        List<WorldPreset> presets = WorldPreset.PRESETS;
        presets.clear();
        presets.add(WorldPreset.FLAT);
        presets.add(WorldPreset.DEBUG);
    }

    public static void resetCooldown(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            mainhandCooldownTime = 0;
        }
        else {
            offhandCooldownTime = 0;
        }
        assert mc.player != null;
        if (mc.player.isUsingItem() && mc.player.getUsedItemHand() == hand) {
            mc.player.stopUsingItem();
            mc.player.connection.send(new PacketCSSimpleMessage(Message.C2S.STOP_USING_ITEM));
        }
    }

    public static void resetCooldowns() {
        mainhandCooldownTime = 0;
        offhandCooldownTime = 0;
        assert mc.player != null;
        if (mc.player.isUsingItem()) {
            mc.player.stopUsingItem();
            mc.player.connection.send(new PacketCSSimpleMessage(Message.C2S.STOP_USING_ITEM));
        }
    }

    public static float retrieveFov() {
        return fov;
    }

    public static Matrix4f retrieveModelViewMatrix() {
        return MODEL_VIEW_MATRIX;
    }

    public static Matrix4f retrieveProjMatrix() {
        return PROJ_MATRIX;
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

    public static void sendToServer(Packet<ServerGamePacketListener> packet) {
        assert mc.player != null;
        mc.player.connection.send(packet);
    }

    public static void setLastInventoryTab(@Range(from = 0, to = 1) int tab) {
        lastInventoryTab = tab;
    }

    public static void setNotLoadedCameraId(int id) {
        cameraId = id;
    }

    public static boolean shouldRenderSpecialAttack() {
        if (mc.player == null) {
            return false;
        }
        return mc.player.shouldRenderSpecialAttack();
    }

    public static void startChargeAttack(IMelee.ChargeAttackType chargeAttack) {
        assert mc.player != null;
        assert mc.gameMode != null;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            if (mc.crosshairPickEntity != null) {
                mc.gameMode.attack(mc.player, mc.crosshairPickEntity);
            }
            return;
        }
        PatchLivingEntity player = mc.player;
        if (!player.isLockedInSpecialAttack()) {
            if (mc.player.getSwimAmount(getPartialTicks()) != 0) {
                mc.player.displayClientMessage(EvolutionTexts.ACTION_ATTACK_POSE, true);
                return;
            }
            player.startSpecialAttack(chargeAttack);
        }
        else if (player.canPerformFollowUp(chargeAttack)) {
            if (mc.player.getSwimAmount(getPartialTicks()) != 0) {
                mc.player.displayClientMessage(EvolutionTexts.ACTION_ATTACK_POSE, true);
                return;
            }
            player.performFollowUp();
        }
    }

    public static void startShortAttack(ItemStack stack) {
        assert mc.player != null;
        assert mc.gameMode != null;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            if (mc.crosshairPickEntity != null) {
                mc.gameMode.attack(mc.player, mc.crosshairPickEntity);
            }
            return;
        }
        IMelee.IAttackType type = stack.getItem() instanceof IMelee melee ? melee.getBasicAttackType(stack) : IMelee.BARE_HAND_ATTACK;
        if (type == null) {
            type = IMelee.BARE_HAND_ATTACK;
        }
        LocalPlayer player = mc.player;
        if (player.isOnGracePeriod()) {
            if (player.canPerformFollowUp(type)) {
                if (mc.player.getSwimAmount(getPartialTicks()) != 0) {
                    mc.player.displayClientMessage(EvolutionTexts.ACTION_ATTACK_POSE, true);
                    return;
                }
                player.performFollowUp();
            }
        }
        else {
            if (!player.isSpecialAttacking() && !player.isLockedInSpecialAttack()) {
                if (mc.player.getSwimAmount(getPartialTicks()) != 0) {
                    mc.player.displayClientMessage(EvolutionTexts.ACTION_ATTACK_POSE, true);
                    return;
                }
                player.startSpecialAttack(type);
            }
        }
    }

//    private static void registerKeyBinds() {
//        ClientRegistry.registerKeyBinding(KEY_CRAWL);
//        ClientRegistry.registerKeyBinding(KEY_BUILDING_ASSIST);
//    }

    public static void startToLoseConscious() {
        ticksToLoseConscious = 80;
    }

    public static void storeModelViewMatrix(Matrix4f modelViewMatrix) {
        MODEL_VIEW_MATRIX.load(modelViewMatrix);
    }

    public static void storeProjMatrix(Matrix4f projectionMatrix) {
        PROJ_MATRIX.load(projectionMatrix);
    }

    /**
     * @return Whether the main tick method should return prematurely.
     */
    private static boolean tickStart() {
        mc.getProfiler().push("init");
        if (mc.player == null) {
            initialized = false;
            clearMemory();
            mc.getProfiler().pop();
            return true;
        }
        if (mc.level == null) {
            updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            mc.getProfiler().pop();
            return true;
        }
        if (!initialized) {
            init();
            initialized = true;
        }
        mc.getProfiler().pop();
        return false;
    }

    private static void updateBackItem() {
        ItemStack backStack = ItemStack.EMPTY;
        int priority = Integer.MAX_VALUE;
        int chosen = -1;
        for (int i = 0; i < 9; i++) {
            assert mc.player != null;
            ItemStack stack = mc.player.getInventory().items.get(i);
            if (stack.getItem() instanceof IBackWeapon backWeapon) {
                int stackPriority = backWeapon.getBackPriority(stack);
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
        if (chosen == mc.player.getInventory().selected) {
            backStack = ItemStack.EMPTY;
        }
        ItemStack oldStack = BACK_ITEMS.put(mc.player.getId(), backStack);
        if (oldStack != backStack) {
            mc.player.connection.send(new PacketCSUpdateBeltBackItem(backStack, true));
        }
    }

//    private static void registerModels(ModelRegistryEvent event) {
//        for (int i = 0, l = EvolutionResources.MODULAR_MODELS.size(); i < l; i++) {
//            ForgeModelBakery.addSpecialModel(EvolutionResources.MODULAR_MODELS.get(i));
//        }
//        //Clear and trim since we are not using it anymore
//        EvolutionResources.MODULAR_MODELS.reset();
//    }

    private static void updateBeltItem() {
        assert mc.player != null;
        ItemStack oldStack = BELT_ITEMS.getOrDefault(mc.player.getId(), ItemStack.EMPTY);
        ItemStack beltStack = getBeltStack();
        if (!ItemStack.isSame(beltStack, oldStack)) {
            if (beltStack.getItem() instanceof IMelee melee && melee.shouldPlaySheatheSound(beltStack)) {
                mc.getSoundManager().play(new SoundEntityEmitted(mc.player, EvolutionSounds.SWORD_SHEATHE, SoundSource.PLAYERS, 0.8f, 1.0f));
                mc.player.connection.send(new PacketCSPlaySoundEntityEmitted(EvolutionSounds.SWORD_SHEATHE, SoundSource.PLAYERS, 0.8f, 1.0f));
            }
            BELT_ITEMS.put(mc.player.getId(), beltStack);
            mc.player.connection.send(new PacketCSUpdateBeltBackItem(beltStack, false));
        }
    }

    public static void updateClientTickrate(float tickrate) {
        if (tps == tickrate) {
            return;
        }
        Evolution.info("Updating client tickrate to " + tickrate);
        tps = tickrate;
        mc.timer.msPerTick = 1_000.0f / tickrate;
    }

    private static boolean updateDynamicLight(Entity entity, boolean items, boolean entities) {
        if (entity instanceof Player) {
            return true;
        }
        if (entity instanceof ItemEntity) {
            return items;
        }
        return entities;
    }

    @Override
    public void onInitializeClient() {
        registerScreens();
        EvolutionRenderer.registryEntityRenders();
//        addTextures(event);
        RenderLayer.setup();
        fixInputMappings();
        TooltipManager.registerTooltipFactories();
        removeVanillaLevelGenerators();
        VanillaOverlays.register();
        EvolutionOverlays.register();
//        registerModels(event);
//        ModelRegistry.register(event);
    }
}
