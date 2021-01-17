package tgw.evolution.events;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.Timer;
import net.minecraft.util.math.*;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import tgw.evolution.ClientProxy;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.LungeAttackInfo;
import tgw.evolution.client.LungeChargeInfo;
import tgw.evolution.client.MovementInputEvolution;
import tgw.evolution.client.gui.advancements.ScreenAdvancements;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.client.renderer.ambient.LightTextureEv;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.inventory.extendedinventory.EvolutionRecipeBook;
import tgw.evolution.items.IOffhandAttackable;
import tgw.evolution.items.ITwoHanded;
import tgw.evolution.network.*;
import tgw.evolution.potion.EffectDizziness;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.StaticFieldHandler;
import tgw.evolution.world.dimension.DimensionOverworld;

import javax.annotation.Nullable;
import java.util.*;

public class ClientEvents {

    public static final FieldHandler<Minecraft, Integer> LEFT_COUNTER_FIELD = new FieldHandler<>(Minecraft.class, "field_71429_W");
    public static final List<EffectInstance> EFFECTS_TO_ADD = new ArrayList<>();
    public static final List<EffectInstance> EFFECTS = new ArrayList<>();
    public static final Map<Integer, LungeChargeInfo> ABOUT_TO_LUNGE_PLAYERS = new HashMap<>();
    public static final Map<Integer, LungeAttackInfo> LUNGING_PLAYERS = new HashMap<>();
    private static final StaticFieldHandler<SkullTileEntity, PlayerProfileCache> PLAYER_PROF_FIELD = new StaticFieldHandler<>(SkullTileEntity.class,
                                                                                                                              "field_184298_j");
    private static final StaticFieldHandler<SkullTileEntity, MinecraftSessionService> SESSION_FIELD = new StaticFieldHandler<>(SkullTileEntity.class,
                                                                                                                               "field_184299_k");
    private static final FieldHandler<GameRenderer, LightTexture> LIGHTMAP_FIELD = new FieldHandler<>(GameRenderer.class, "field_78513_d");
    private static final FieldHandler<Minecraft, Timer> TIMER_FIELD = new FieldHandler<>(Minecraft.class, "field_71428_T");
    private static final FieldHandler<Timer, Float> TICKRATE_FIELD = new FieldHandler<>(Timer.class, "field_194149_e");
    private static final FieldHandler<EffectInstance, Integer> DURATION_FIELD = new FieldHandler<>(EffectInstance.class, "field_149431_d");
    private static final FieldHandler<ClientPlayerEntity, ClientRecipeBook> RECIPE_BOOK_FIELD = new FieldHandler<>(ClientPlayerEntity.class,
                                                                                                                   "field_192036_cb");
    private static final List<EffectInstance> EFFECTS_TO_TICK = new ArrayList<>();
    private static ClientEvents instance;
    private final Minecraft mc;
    private final ClientRenderer renderer;
    public int effectToAddTicks;
    public int jumpTicks;
    @Nullable
    public Entity leftPointedEntity;
    public int mainhandTimeSinceLastHit;
    public int offhandTimeSinceLastHit;
    @Nullable
    public Entity rightPointedEntity;
    public boolean shouldPassEffectTick;
    private int currentShader;
    private int currentThirdPersonView;
    private boolean inverted;
    private boolean isJumpPressed;
    private boolean isSneakPressed;
    private EntityRayTraceResult leftRayTrace;
    private boolean lunging;
    private GameRenderer oldGameRenderer;
    private boolean previousPressed;
    private boolean proneToggle;
    private EntityRayTraceResult rightRayTrace;
    private boolean skinsLoaded;
    private boolean skyRendererBinded;
    private boolean sneakpreviousPressed;
    private int ticks;
    private float tps = 20.0f;

    public ClientEvents(Minecraft mc) {
        this.mc = mc;
        instance = this;
        this.renderer = new ClientRenderer(mc, this);
    }

    public static ClientEvents getInstance() {
        return instance;
    }

    private static float getRightCooldownPeriod(IOffhandAttackable item) {
        double attackSpeed = item.getAttackSpeed() + PlayerHelper.ATTACK_SPEED;
        return (float) (1 / attackSpeed * 20);
    }

    private static void removeEffect(List<EffectInstance> list, Effect effect) {
        Iterator<EffectInstance> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getPotion() == effect) {
                iterator.remove();
                break;
            }
        }
    }

    public static void removePotionEffect(Effect effect) {
        removeEffect(EFFECTS, effect);
        removeEffect(EFFECTS_TO_ADD, effect);
        removeEffect(EFFECTS_TO_TICK, effect);
    }

    public boolean areControlsInverted() {
        return this.inverted;
    }

    public float getMainhandCooledAttackStrength(float partialTicks) {
        return MathHelper.clamp((this.mainhandTimeSinceLastHit + partialTicks) / this.mc.player.getCooldownPeriod(), 0.0F, 1.0F);
    }

    public float getOffhandCooledAttackStrength(Item item, float adjustTicks) {
        if (!(item instanceof IOffhandAttackable)) {
            float cooldown = (float) (1.0 / PlayerHelper.ATTACK_SPEED * 20.0);
            return MathHelper.clamp((this.offhandTimeSinceLastHit + adjustTicks) / cooldown, 0.0F, 1.0F);
        }
        return MathHelper.clamp((this.offhandTimeSinceLastHit + adjustTicks) / getRightCooldownPeriod((IOffhandAttackable) item), 0.0F, 1.0F);
    }

    public int getTickCount() {
        return this.ticks;
    }

    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public void leftMouseClick() {
        float cooldown = this.mc.player.getCooldownPeriod();
        if (this.mainhandTimeSinceLastHit >= cooldown) {
            this.mainhandTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getHitVec().y : Double.NaN;
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.leftPointedEntity, Hand.MAIN_HAND, rayTraceY));
            this.swingArm(Hand.MAIN_HAND);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        //Turn auto-jump off
        this.mc.gameSettings.autoJump = false;
        if (this.mc.player == null) {
            this.skyRendererBinded = false;
            this.skinsLoaded = false;
            EFFECTS.clear();
            EFFECTS_TO_ADD.clear();
            EFFECTS_TO_TICK.clear();
            this.inverted = false;
            ABOUT_TO_LUNGE_PLAYERS.clear();
            LUNGING_PLAYERS.clear();
            return;
        }
        if (this.mc.world == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
            ABOUT_TO_LUNGE_PLAYERS.clear();
            LUNGING_PLAYERS.clear();
        }
        //Bind Sky Renderer
        if (!this.skyRendererBinded) {
            if (this.mc.world.dimension.getType() == DimensionType.OVERWORLD) {
                this.mc.world.dimension.setSkyRenderer(new SkyRenderer(this.mc.worldRenderer));
                this.skyRendererBinded = true;
            }
        }
        //Load skin for corpses
        if (!this.skinsLoaded) {
            PlayerProfileCache playerProfile = PLAYER_PROF_FIELD.get();
            MinecraftSessionService session = SESSION_FIELD.get();
            if (playerProfile != null && session != null) {
                EntityPlayerCorpse.setProfileCache(playerProfile);
                EntityPlayerCorpse.setSessionService(session);
                this.skinsLoaded = true;
            }
        }
        //Runs at the start of each tick
        if (event.phase == TickEvent.Phase.START) {
            //Jump
            if (this.jumpTicks > 0) {
                this.jumpTicks--;
            }
            if (this.mc.player.onGround) {
                this.jumpTicks = 0;
            }
            //Apply shaders
            int shader;
            if (!this.mc.player.isCreative() && !this.mc.player.isSpectator()) {
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
                else {
                    shader = 0;
                }
            }
            else {
                shader = 0;
            }
            if (this.mc.gameSettings.thirdPersonView != this.currentThirdPersonView) {
                this.currentThirdPersonView = this.mc.gameSettings.thirdPersonView;
                this.currentShader = 0;
            }
            if (shader != this.currentShader) {
                this.currentShader = shader;
                switch (shader) {
                    case 0:
                        this.mc.gameRenderer.stopUseShader();
                        break;
                    case 25:
                        this.mc.gameRenderer.loadShader(EvolutionResources.SHADER_DESATURATE_25);
                        break;
                    case 50:
                        this.mc.gameRenderer.loadShader(EvolutionResources.SHADER_DESATURATE_50);
                        break;
                    case 75:
                        this.mc.gameRenderer.loadShader(EvolutionResources.SHADER_DESATURATE_75);
                        break;
                    default:
                        Evolution.LOGGER.warn("Unregistered shader id: {}", shader);
                }
            }
            if (!this.mc.isGamePaused()) {
                if (this.mc.world.dimension instanceof DimensionOverworld) {
                    this.mc.world.dimension.tick();
                }
                //RayTrace entities
                this.leftRayTrace = MathHelper.rayTraceEntityFromEyes(this.mc.player,
                                                                      1.0f,
                                                                      this.mc.player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue());
                this.leftPointedEntity = this.leftRayTrace == null ? null : this.leftRayTrace.getEntity();
                if (this.mc.player.getHeldItemOffhand().getItem() instanceof IOffhandAttackable) {
                    this.rightRayTrace = MathHelper.rayTraceEntityFromEyes(this.mc.player,
                                                                           1.0f,
                                                                           ((IOffhandAttackable) this.mc.player.getHeldItemOffhand()
                                                                                                               .getItem()).getReach() +
                                                                           PlayerHelper.REACH_DISTANCE);
                    this.rightPointedEntity = this.rightRayTrace == null ? null : this.rightRayTrace.getEntity();
                }
                else {
                    this.rightPointedEntity = null;
                }
                this.renderer.tick();
                ABOUT_TO_LUNGE_PLAYERS.entrySet().removeIf(entry -> entry.getValue().shouldBeRemoved());
                ABOUT_TO_LUNGE_PLAYERS.forEach((key, value) -> value.tick());
                LUNGING_PLAYERS.entrySet().removeIf(entry -> entry.getValue().shouldBeRemoved());
                LUNGING_PLAYERS.forEach((key, value) -> value.tick());
            }
            GameRenderer gameRenderer = this.mc.gameRenderer;
            if (gameRenderer != this.oldGameRenderer) {
                this.oldGameRenderer = gameRenderer;
                LIGHTMAP_FIELD.set(this.oldGameRenderer, new LightTextureEv(this.oldGameRenderer));
            }
            //Handle two-handed items
            if (this.mc.player.getHeldItemMainhand().getItem() instanceof ITwoHanded && !this.mc.player.getHeldItemOffhand().isEmpty()) {
                this.mainhandTimeSinceLastHit = 0;
                LEFT_COUNTER_FIELD.set(this.mc, Integer.MAX_VALUE);
                this.mc.player.sendStatusMessage(EvolutionTexts.ACTION_TWO_HANDED, true);
            }
            //Prevents the player from attacking if on cooldown
            if (this.getMainhandCooledAttackStrength(0.0F) != 1 &&
                this.mc.objectMouseOver != null &&
                this.mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
                LEFT_COUNTER_FIELD.set(this.mc, Integer.MAX_VALUE);
            }
            //Handle Disoriented Effect
            if (this.mc.player.isPotionActive(EvolutionEffects.DISORIENTED.get())) {
                if (!this.inverted) {
                    this.mc.player.movementInput = new MovementInputEvolution(this.mc.gameSettings);
                    this.inverted = true;
                }
            }
            else {
                if (this.inverted) {
                    this.inverted = false;
                }
            }
            //Handle Dizziness Effect
            if (!this.mc.player.isPotionActive(EvolutionEffects.DIZZINESS.get())) {
                EffectDizziness.lastMotion = Vec3d.ZERO;
                EffectDizziness.tick = 0;
            }
        }
        //Runs at the end of each tick
        else if (event.phase == TickEvent.Phase.END) {
            if (!this.mc.isGamePaused()) {
                //Remove inactive effects
                if (!EFFECTS.isEmpty() || !EFFECTS_TO_TICK.isEmpty()) {
                    Iterator<EffectInstance> iterator = EFFECTS.iterator();
                    while (iterator.hasNext()) {
                        Effect effect = iterator.next().getPotion();
                        if (!this.mc.player.isPotionActive(effect)) {
                            iterator.remove();
                        }
                    }
                    iterator = EFFECTS_TO_TICK.iterator();
                    while (iterator.hasNext()) {
                        Effect effect = iterator.next().getPotion();
                        if (!this.mc.player.isPotionActive(effect)) {
                            iterator.remove();
                        }
                    }
                    for (EffectInstance effect : EFFECTS_TO_TICK) {
                        DURATION_FIELD.set(effect, effect.getDuration() - 1);
                    }
                }
                if (this.shouldPassEffectTick) {
                    this.effectToAddTicks++;
                    this.shouldPassEffectTick = false;
                }
                //Proning
                boolean pressed = ClientProxy.TOGGLE_PRONE.isKeyDown();
                if (pressed && !this.previousPressed) {
                    this.proneToggle = !this.proneToggle;
                }
                this.previousPressed = pressed;
                this.updateClientProneState(this.mc.player);
                //Sneak on ladders
                if (this.mc.player.isOnLadder()) {
                    if (this.isSneakPressed && !this.sneakpreviousPressed) {
                        this.sneakpreviousPressed = true;
                        this.mc.player.setMotion(Vec3d.ZERO);
                    }
                }
                //Handle creative features
                if (this.mc.player.isCreative() && ClientProxy.BUILDING_ASSIST.isKeyDown()) {
                    this.mc.player.sendStatusMessage(EvolutionTexts.ACTION_INERTIA, true);
                    this.mc.player.setMotion(Vec3d.ZERO);
                    if (this.mc.player.getHeldItemMainhand().getItem() instanceof BlockItem) {
                        if (this.mc.objectMouseOver instanceof BlockRayTraceResult) {
                            BlockPos pos = ((BlockRayTraceResult) this.mc.objectMouseOver).getPos();
                            if (!this.mc.world.getBlockState(pos).isAir(this.mc.world, pos)) {
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSChangeBlock((BlockRayTraceResult) this.mc.objectMouseOver));
                            }
                        }
                    }
                }
                //Handle swing
                this.ticks++;
                if (this.mc.playerController.getIsHittingBlock()) {
                    this.swingArm(Hand.MAIN_HAND);
                }
                this.lunging = false;
            }
        }
    }

    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ClientPlayerEntity && event.getEntity().equals(this.mc.player)) {
            this.mc.player.abilities.setWalkSpeed((float) PlayerHelper.WALK_SPEED);
            RECIPE_BOOK_FIELD.set(this.mc.player, new EvolutionRecipeBook(this.mc.player.world.getRecipeManager()));
        }
    }

    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        this.renderer.renderFog(event);
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof InventoryScreen) {
            event.setCanceled(true);
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSOpenExtendedInventory());
        }
        else if (event.getGui() instanceof AdvancementsScreen) {
            event.setCanceled(true);
            this.mc.displayGuiScreen(new ScreenAdvancements(this.mc.getConnection().getAdvancementManager()));
        }
    }

    @SubscribeEvent
    public void onPlayerInput(InputUpdateEvent event) {
        MovementInput movementInput = event.getMovementInput();
        this.isJumpPressed = movementInput.jump;
        this.isSneakPressed = movementInput.sneak;
        if (!this.isSneakPressed) {
            this.sneakpreviousPressed = false;
        }
        if (this.proneToggle && !this.mc.player.isOnLadder()) {
            movementInput.jump = false;
        }
    }

//    @SubscribeEvent
//    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
//        if (this.mc.currentScreen != null || this.mc.player == null) {
//            this.isLeftPressed = false;
//            this.isRightPressed = false;
//            return;
//        }
//        KeyBinding attack = this.mc.gameSettings.keyBindAttack;
//        KeyBinding use = this.mc.gameSettings.keyBindUseItem;
//        if (attack.getKey().getType() == InputMappings.Type.KEYSYM) {
//            if (attack.getKey().getKeyCode() == event.getKey()) {
//                if (event.getAction() == GLFW.GLFW_PRESS) {
//                    this.isLeftPressed = true;
//                    if (!this.isRightPressed) {
//                        this.onLeftMouseClick();
//                    }
//                }
//                else {
//                    this.isLeftPressed = false;
//                }
//            }
//        }
//        if (use.getKey().getType() == InputMappings.Type.KEYSYM) {
//            if (use.getKey().getKeyCode() == event.getKey()) {
//                if (event.getAction() == GLFW.GLFW_PRESS) {
//                    this.isRightPressed = true;
//                    if (!this.isLeftPressed) {
//                        this.onRightMouseClick();
//                    }
//                }
//                else {
//                    this.isRightPressed = false;
//                }
//            }
//        }
//    }

//    //Handle mainhand attack
//    private void onLeftMouseClick() {
//        if (!this.isRightPressed) {
//            float cooldown = this.mc.player.getCooldownPeriod();
//            if (this.mainhandTimeSinceLastHit >= cooldown && this.mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
//                this.requiresReequiping = true;
//                this.mainhandTimeSinceLastHit = 0;
//                double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getHitVec().y : Double.NaN;
//                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.leftPointedEntity, Hand.MAIN_HAND, rayTraceY));
//                this.swingArm(Hand.MAIN_HAND);
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onMouseEvent(InputEvent.MouseInputEvent event) {
//        if (this.mc.currentScreen != null || this.mc.player == null) {
//            this.isLeftPressed = false;
//            this.isRightPressed = false;
//            return;
//        }
//        KeyBinding attack = this.mc.gameSettings.keyBindAttack;
//        KeyBinding use = this.mc.gameSettings.keyBindUseItem;
//        if (attack.getKey().getType() == InputMappings.Type.MOUSE) {
//            if (attack.getKey().getKeyCode() == event.getButton()) {
//                if (event.getAction() == GLFW.GLFW_PRESS) {
//                    this.isLeftPressed = true;
//                    this.onLeftMouseClick();
//                }
//                else {
//                    this.isLeftPressed = false;
//                }
//            }
//        }
//        if (use.getKey().getType() == InputMappings.Type.MOUSE) {
//            if (use.getKey().getKeyCode() == event.getButton()) {
//                if (event.getAction() == GLFW.GLFW_PRESS) {
//                    this.isRightPressed = true;
//                    this.onRightMouseClick();
//                }
//                else {
//                    this.isRightPressed = false;
//                }
//            }
//        }
//    }

    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (!event.getEntityLiving().world.isRemote) {
            return;
        }
        if (event.getEntityLiving() != this.mc.player) {
            return;
        }
        if (event.getOldPotionEffect() == null) {
            EFFECTS_TO_ADD.add(event.getPotionEffect());
        }
        else {
            removeEffect(EFFECTS, event.getOldPotionEffect().getPotion());
            removeEffect(EFFECTS_TO_TICK, event.getOldPotionEffect().getPotion());
            removeEffect(EFFECTS_TO_ADD, event.getOldPotionEffect().getPotion());
            EffectInstance newEffect = new EffectInstance(event.getOldPotionEffect());
            newEffect.combine(event.getPotionEffect());
            EFFECTS.add(newEffect);
            EFFECTS_TO_TICK.add(newEffect);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (this.mc.player.getRidingEntity() != null) {
            ForgeIngameGui.renderFood = true;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
            this.renderer.renderAttackIndicator();
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH) {
            event.setCanceled(true);
            this.renderer.renderHealth();
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.POTION_ICONS) {
            event.setCanceled(true);
            this.renderer.renderPotionIcons(event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        event.setCanceled(true);
        boolean sleeping = this.mc.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity) this.mc.getRenderViewEntity()).isSleeping();
        if (this.mc.gameSettings.thirdPersonView == 0 &&
            !sleeping &&
            !this.mc.gameSettings.hideGUI &&
            this.mc.playerController.getCurrentGameType() != GameType.SPECTATOR) {
            this.mc.gameRenderer.enableLightmap();
            this.renderer.renderItemInFirstPerson(event.getPartialTicks());
            this.mc.gameRenderer.disableLightmap();
        }
    }

    @SubscribeEvent
    public void onRenderOutlines(DrawBlockHighlightEvent event) {
        if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockRayTraceResult) event.getTarget()).getPos();
            if (!this.mc.world.getWorldBorder().contains(hitPos)) {
                return;
            }
            if (this.mc.world.getBlockState(hitPos).getBlock() instanceof BlockKnapping) {
                TEKnapping tile = (TEKnapping) this.mc.world.getTileEntity(hitPos);
                this.renderer.renderOutlines(tile.type.getShape(), event.getInfo(), hitPos);
                return;
            }
            if (this.mc.world.getBlockState(hitPos).getBlock() instanceof BlockMolding) {
                TEMolding tile = (TEMolding) this.mc.world.getTileEntity(hitPos);
                this.renderer.renderOutlines(tile.molding.getShape(), event.getInfo(), hitPos);
            }
        }
    }

    public void performLungeMovement() {
        if (!this.lunging && this.mc.player.onGround && this.mc.player.moveForward > 0) {
            this.lunging = true;
            Vec3d oldMotion = this.mc.player.getMotion();
            float sinFacing = MathHelper.sinDeg(this.mc.player.rotationYaw);
            float cosFacing = MathHelper.cosDeg(this.mc.player.rotationYaw);
            double lungeBoost = 0.15;
            this.mc.player.setMotion(oldMotion.x - lungeBoost * sinFacing, oldMotion.y, oldMotion.z + lungeBoost * cosFacing);
        }
    }

    public void performMainhandLunge(ItemStack mainhandStack, float strength) {
        this.mainhandTimeSinceLastHit = 0;
        this.renderer.resetFullEquipProgress(Hand.MAIN_HAND);
        double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getHitVec().y : Double.NaN;
        Evolution.LOGGER.debug("lunged with {}", mainhandStack);
        int slot = Integer.MIN_VALUE;
        for (int i = 0; i < this.mc.player.inventory.mainInventory.size(); i++) {
            if (this.mc.player.inventory.mainInventory.get(i).equals(mainhandStack, false)) {
                slot = i;
                break;
            }
        }
        if (slot == Integer.MIN_VALUE) {
            if (this.mc.player.inventory.offHandInventory.get(0).equals(mainhandStack, false)) {
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
        double rayTraceY = this.rightRayTrace != null ? this.rightRayTrace.getHitVec().y : Double.NaN;
        Evolution.LOGGER.debug("lunged with {}", offhandStack);
        int slot = Integer.MIN_VALUE;
        if (this.mc.player.inventory.offHandInventory.get(0).equals(offhandStack, false)) {
            slot = -1;
        }
        if (slot == Integer.MIN_VALUE) {
            for (int i = 0; i < this.mc.player.inventory.mainInventory.size(); i++) {
                if (this.mc.player.inventory.mainInventory.get(i).equals(offhandStack, false)) {
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

//    //Handle offhand attack
//    private void onRightMouseClick() {
//        Item offhandItem = this.mc.player.getHeldItemOffhand().getItem();
//        if (!(offhandItem instanceof IOffhandAttackable)) {
//            return;
//        }
//        if (!this.isLeftPressed) {
//            ItemStack mainHandStack = this.mc.player.getHeldItemMainhand();
//            float cooldown = getRightCooldownPeriod((IOffhandAttackable) offhandItem);
//            if (this.offhandTimeSinceLastHit >= cooldown &&
//                mainHandStack.getUseAction() == UseAction.NONE &&
//                this.mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
//                this.offhandTimeSinceLastHit = 0;
//                double rayTraceY = this.rightRayTrace != null ? this.rightRayTrace.getHitVec().y : Double.NaN;
//                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.rightPointedEntity, Hand.OFF_HAND, rayTraceY));
//                this.swingArm(Hand.OFF_HAND);
//            }
//        }
//    }

    public void rightMouseClick(IOffhandAttackable item) {
        float cooldown = getRightCooldownPeriod(item);
        if (this.offhandTimeSinceLastHit >= cooldown) {
            this.offhandTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getHitVec().y : Double.NaN;
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.rightPointedEntity, Hand.OFF_HAND, rayTraceY));
            this.swingArm(Hand.OFF_HAND);
        }
    }

    @SubscribeEvent
    public void shutDownInternalServer(FMLServerStoppedEvent event) {
        if (this.inverted) {
            this.inverted = false;
        }
    }

    public void swingArm(Hand hand) {
        ItemStack stack = this.mc.player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(this.mc.player)) {
            return;
        }
        this.renderer.swingArm(hand);
    }

    private void updateClientProneState(PlayerEntity player) {
        if (player != null) {
            UUID uuid = player.getUniqueID();
            boolean shouldBeProne = ClientProxy.TOGGLE_PRONE.isKeyDown() != this.proneToggle;
            shouldBeProne = shouldBeProne &&
                            !player.isInWater() &&
                            !player.isInLava() &&
                            (!player.isOnLadder() || !this.isJumpPressed && player.onGround);
            shouldBeProne = shouldBeProne && (!player.isOnLadder() || !this.isJumpPressed && player.onGround);
            BlockPos pos = player.getPosition().up(2);
            //noinspection ConstantConditions
            shouldBeProne = shouldBeProne ||
                            this.proneToggle &&
                            player.isOnLadder() &&
                            !player.world.getBlockState(pos).getCollisionShape(player.world, pos, null).isEmpty();
            if (shouldBeProne != Evolution.PRONED_PLAYERS.getOrDefault(uuid, false)) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetProne(shouldBeProne));
            }
            Evolution.PRONED_PLAYERS.put(uuid, shouldBeProne);
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
}
