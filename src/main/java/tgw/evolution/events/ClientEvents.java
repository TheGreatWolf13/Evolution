package tgw.evolution.events;

import com.google.common.collect.Multimap;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import tgw.evolution.ClientProxy;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.client.renderer.ambient.LightTextureEv;
import tgw.evolution.client.renderer.ambient.SkyRenderer;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.hooks.TickrateChanger;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.*;
import tgw.evolution.network.PacketCSChangeBlock;
import tgw.evolution.network.PacketCSOpenExtendedInventory;
import tgw.evolution.network.PacketCSPlayerAttack;
import tgw.evolution.network.PacketCSSetProne;
import tgw.evolution.potion.EffectDizziness;
import tgw.evolution.util.EvolutionStyles;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.PlayerHelper;
import tgw.evolution.util.reflection.FieldHandler;
import tgw.evolution.util.reflection.StaticFieldHandler;
import tgw.evolution.world.dimension.DimensionOverworld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ClientEvents {

    public static final ResourceLocation ICONS = Evolution.location("textures/gui/icons.png");
    private static final String TWO_HANDED = "evolution.actionbar.two_handed";
    private static final String INERTIA = "evolution.actionbar.inertia";
    private static final ITextComponent COMPONENT_TWO_HANDED = new TranslationTextComponent(TWO_HANDED).setStyle(EvolutionStyles.WHITE);
    private static final ITextComponent COMPONENT_INERTIA = new TranslationTextComponent(INERTIA).setStyle(EvolutionStyles.WHITE);
    private static final ResourceLocation DESATURATE_25 = Evolution.location("shaders/post/saturation25.json");
    private static final ResourceLocation DESATURATE_50 = Evolution.location("shaders/post/saturation50.json");
    private static final ResourceLocation DESATURATE_75 = Evolution.location("shaders/post/saturation75.json");
    private static final StaticFieldHandler<SkullTileEntity, PlayerProfileCache> PLAYER_PROF_FIELD = new StaticFieldHandler<>(SkullTileEntity.class,
                                                                                                                              "field_184298_j");
    private static final StaticFieldHandler<SkullTileEntity, MinecraftSessionService> SESSION_FIELD = new StaticFieldHandler<>(SkullTileEntity.class,
                                                                                                                               "field_184299_k");
    private static final FieldHandler<GameRenderer, LightTexture> LIGHTMAP_FIELD = new FieldHandler<>(GameRenderer.class, "field_78513_d");
    private static final FieldHandler<Minecraft, Integer> LEFT_COUNTER_FIELD = new FieldHandler<>(Minecraft.class, "field_71429_W");
    private static final FieldHandler<Minecraft, Timer> TIMER_FIELD = new FieldHandler<>(Minecraft.class, "field_71428_T");
    private static final FieldHandler<Timer, Float> TICKRATE_FIELD = new FieldHandler<>(Timer.class, "field_194149_e");
    private static ClientEvents instance;
    private final Minecraft mc;
    private final Random rand = new Random();
    public int jumpTicks;
    private int currentShader;
    private int currentThirdPersonView;
    private long healthUpdateCounter;
    private boolean inverted;
    private boolean isJumpPressed;
    private boolean isLeftPressed;
    private boolean isRightPressed;
    private boolean isSneakPressed;
    private float lastPlayerHealth;
    private long lastSystemTime;
    private float leftEquipProgress;
    private boolean leftIsSwingInProgress;
    @Nullable
    private Entity leftPointedEntity;
    private float leftPrevEquipProgress;
    private float leftPrevSwingProgress;
    private EntityRayTraceResult leftRayTrace;
    private float leftSwingProgress;
    private int leftSwingProgressInt;
    private int leftTimeSinceLastHit;
    private ItemStack mainhandStack = ItemStack.EMPTY;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private GameRenderer oldGameRenderer;
    private float playerHealth;
    private boolean previousPressed;
    private boolean proneToggle;
    private boolean requiresReequiping;
    private float rightEquipProgress;
    private boolean rightIsSwingInProgress;
    @Nullable
    private Entity rightPointedEntity;
    private float rightPrevEquipProgress;
    private float rightPrevSwingProgress;
    private EntityRayTraceResult rightRayTrace;
    private float rightSwingProgress;
    private int rightSwingProgressInt;
    private int rightTimeSinceLastHit;
    private boolean skinsLoaded;
    private boolean skyRendererBinded;
    private boolean sneakpreviousPressed;
    private int ticks;
    private float tps = 20.0f;

    public ClientEvents(Minecraft mc) {
        this.mc = mc;
        instance = this;
    }

    private static void blit(int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        AbstractGui.blit(x, y, 20, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    public static ClientEvents getInstance() {
        return instance;
    }

    private static float getRightCooldownPeriod(IOffhandAttackable item) {
        double attackSpeed = item.getAttackSpeed() + PlayerHelper.ATTACK_SPEED;
        return (float) (1 / attackSpeed * 20);
    }

    private static float roundToHearts(float currentHealth) {
        return MathHelper.floor(currentHealth * 0.4F) / 0.4F;
    }

    public static int shiftTextByLines(List<String> lines, int y) {
        for (int i = 1; i < lines.size(); i++) {
            String s = lines.get(i);
            s = TextFormatting.getTextWithoutFormattingCodes(s);
            if (s != null && s.trim().isEmpty()) {
                y += 10 * (i - 1) + 1;
                return y;
            }
        }
        return y;
    }

    public static int shiftTextByLines(int desiredLine, int y) {
        return y + 10 * (desiredLine - 1) + 1;
    }

    private static void swapControls(Minecraft mc) {
        swapKeybinds(mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSneak);
        swapKeybinds(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack);
        swapKeybinds(mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight);
        mc.gameSettings.saveOptions();
        mc.gameSettings.loadOptions();
    }

    private static void swapKeybinds(KeyBinding a, KeyBinding b) {
        InputMappings.Input temp = a.getKey();
        a.bind(b.getKey());
        b.bind(temp);
    }

    private int getArmSwingAnimationEnd() {
        if (EffectUtils.hasMiningSpeedup(this.mc.player)) {
            return 6 - (1 + EffectUtils.getMiningSpeedup(this.mc.player));
        }
        return this.mc.player.isPotionActive(Effects.MINING_FATIGUE) ?
               6 + (1 + this.mc.player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2 :
               6;
    }

    private float getLeftCooledAttackStrength(float adjustTicks) {
        return MathHelper.clamp((this.leftTimeSinceLastHit + adjustTicks) / this.mc.player.getCooldownPeriod(), 0.0F, 1.0F);
    }

    private float getLeftSwingProgress(float partialTickTime) {
        float f = this.leftSwingProgress - this.leftPrevSwingProgress;
        if (f < 0.0F) {
            ++f;
        }
        return this.leftPrevSwingProgress + f * partialTickTime;
    }

    private float getRightCooledAttackStrength(Item item, float adjustTicks) {
        if (!(item instanceof IOffhandAttackable)) {
            return 0;
        }
        return MathHelper.clamp((this.rightTimeSinceLastHit + adjustTicks) / getRightCooldownPeriod((IOffhandAttackable) item), 0.0F, 1.0F);
    }

    private float getRightSwingProgress(float partialTickTime) {
        float f = this.rightSwingProgress - this.rightPrevSwingProgress;
        if (f < 0.0F) {
            ++f;
        }
        return this.rightPrevSwingProgress + f * partialTickTime;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        //Turn auto-jump off
        this.mc.gameSettings.autoJump = false;
        if (this.mc.player == null) {
            this.skyRendererBinded = false;
            this.skinsLoaded = false;
            return;
        }
        if (this.mc.world == null) {
            this.updateClientTickrate(TickrateChanger.DEFAULT_TICKRATE);
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
                        this.mc.gameRenderer.loadShader(DESATURATE_25);
                        break;
                    case 50:
                        this.mc.gameRenderer.loadShader(DESATURATE_50);
                        break;
                    case 75:
                        this.mc.gameRenderer.loadShader(DESATURATE_75);
                        break;
                    default:
                        Evolution.LOGGER.warn("Unregistered shader id: {}", shader);
                }
            }
            //RayTrace entities
            if (!this.mc.isGamePaused()) {
                if (this.mc.world.dimension instanceof DimensionOverworld) {
                    this.mc.world.dimension.tick();
                }
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
            }
            GameRenderer gameRenderer = this.mc.gameRenderer;
            if (gameRenderer != this.oldGameRenderer) {
                this.oldGameRenderer = gameRenderer;
                LIGHTMAP_FIELD.set(this.oldGameRenderer, new LightTextureEv(this.oldGameRenderer));
            }
            //Handle two-handed items
            if (this.mc.player.getHeldItemMainhand().getItem() instanceof ITwoHanded && !this.mc.player.getHeldItemOffhand().isEmpty()) {
                this.leftTimeSinceLastHit = 0;
                this.requiresReequiping = true;
                LEFT_COUNTER_FIELD.set(this.mc, Integer.MAX_VALUE);
                this.mc.player.sendStatusMessage(COMPONENT_TWO_HANDED, true);
            }
            //Prevents the player from attacking if on cooldown
            if (this.getLeftCooledAttackStrength(0) != 1 &&
                this.mc.objectMouseOver != null &&
                this.mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
                LEFT_COUNTER_FIELD.set(this.mc, Integer.MAX_VALUE);
            }
            //Handle Disoriented Effect
            if (this.mc.player.isPotionActive(EvolutionEffects.DISORIENTED.get())) {
                if (!this.inverted) {
                    this.inverted = true;
                    swapControls(this.mc);
                }
            }
            else {
                if (this.inverted) {
                    this.inverted = false;
                    swapControls(this.mc);
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
                    this.mc.player.sendStatusMessage(COMPONENT_INERTIA, true);
                    this.mc.player.setMotion(Vec3d.ZERO);
                    if (this.mc.player.getHeldItemMainhand().getItem() instanceof BlockItem) {
                        if (this.mc.objectMouseOver instanceof BlockRayTraceResult) {
                            BlockPos pos = ((BlockRayTraceResult) this.mc.objectMouseOver).getPos();
                            if (this.mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSChangeBlock((BlockRayTraceResult) this.mc.objectMouseOver));
                            }
                        }
                    }
                }
                //Handle swing
                this.ticks++;
                if (this.mc.playerController.getIsHittingBlock()) {
                    this.requiresReequiping = false;
                }
                this.rightPrevSwingProgress = this.rightSwingProgress;
                this.rightPrevEquipProgress = this.rightEquipProgress;
                this.leftPrevSwingProgress = this.leftSwingProgress;
                this.leftPrevEquipProgress = this.leftEquipProgress;
                this.updateArmSwingProgress();
                if (this.mc.player.isRowingBoat()) {
                    this.leftEquipProgress = MathHelper.clamp(this.leftEquipProgress - 0.4F, 0.0F, 1.0F);
                    this.rightEquipProgress = MathHelper.clamp(this.rightEquipProgress - 0.4F, 0.0F, 1.0F);
                }
                else {
                    float cooledAttackStrength = this.getRightCooledAttackStrength(this.mc.player.getHeldItemOffhand().getItem(), 1);
                    this.rightEquipProgress += MathHelper.clamp(cooledAttackStrength * cooledAttackStrength * cooledAttackStrength -
                                                                this.rightEquipProgress, -0.4f, 0.4F);
                    cooledAttackStrength = this.getLeftCooledAttackStrength(1);
                    this.leftEquipProgress += MathHelper.clamp(cooledAttackStrength * cooledAttackStrength * cooledAttackStrength -
                                                               this.leftEquipProgress, -0.4f, 0.4F);
                }
                ItemStack stackOffhand = this.mc.player.getHeldItemOffhand();
                if (MathHelper.areItemStacksSufficientlyEqual(stackOffhand, this.offhandStack)) {
                    this.rightTimeSinceLastHit++;
                }
                else {
                    this.rightTimeSinceLastHit = 0;
                    this.offhandStack = stackOffhand;
                }
                ItemStack stackMainhand = this.mc.player.getHeldItemMainhand();
                if (MathHelper.areItemStacksSufficientlyEqual(stackMainhand, this.mainhandStack)) {
                    this.leftTimeSinceLastHit++;
                }
                else {
                    this.leftTimeSinceLastHit = 0;
                    this.mainhandStack = stackMainhand;
                }
            }
        }
    }

    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        //Render Blindness fog
        if (this.mc.player != null && this.mc.player.isPotionActive(Effects.BLINDNESS)) {
            float f1 = 5.0F;
            int duration = this.mc.player.getActivePotionEffect(Effects.BLINDNESS).getDuration();
            int amplifier = this.mc.player.getActivePotionEffect(Effects.BLINDNESS).getAmplifier() + 1;
            if (duration < 20) {
                f1 = 5.0F + (this.mc.gameSettings.renderDistanceChunks * 16 - 5.0F) * (1.0F - duration / 20.0F);
            }
            GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
            float multiplier = 0.25F / amplifier;
            GlStateManager.fogStart(f1 * multiplier);
            GlStateManager.fogEnd(f1 * multiplier * 4.0F);
            if (GL.getCapabilities().GL_NV_fog_distance) {
                GL11.glFogi(34_138, 34_139);
            }
            event.setDensity(2.0F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof InventoryScreen) {
            event.setCanceled(true);
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSOpenExtendedInventory());
        }
        //Auto-Respawn for debugging purposes only
        else if (event.getGui() instanceof DeathScreen) {
            this.mc.player.respawnPlayer();
        }
    }

    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        if (this.mc.currentScreen != null || this.mc.player == null) {
            return;
        }
        KeyBinding attack = this.mc.gameSettings.keyBindAttack;
        KeyBinding use = this.mc.gameSettings.keyBindUseItem;
        if (attack.getKey().getType() == InputMappings.Type.KEYSYM) {
            if (attack.getKey().getKeyCode() == event.getKey()) {
                if (event.getAction() == GLFW.GLFW_PRESS && !this.isRightPressed) {
                    this.isLeftPressed = true;
                    this.onLeftMouseClick();
                }
                else {
                    this.isLeftPressed = false;
                }
            }
        }
        if (use.getKey().getType() == InputMappings.Type.KEYSYM) {
            if (use.getKey().getKeyCode() == event.getKey()) {
                if (event.getAction() == GLFW.GLFW_PRESS && !this.isLeftPressed) {
                    this.isRightPressed = true;
                    this.onRightMouseClick();
                }
                else {
                    this.isRightPressed = false;
                }
            }
        }
    }

    //Handle mainhand attack
    private void onLeftMouseClick() {
        float cooldown = this.mc.player.getCooldownPeriod();
        if (this.leftTimeSinceLastHit >= cooldown && this.mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
            this.requiresReequiping = true;
            this.leftTimeSinceLastHit = 0;
            double rayTraceY = this.leftRayTrace != null ? this.leftRayTrace.getHitVec().y : Double.NaN;
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.leftPointedEntity, Hand.MAIN_HAND, rayTraceY));
            this.swingArm(Hand.MAIN_HAND);
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.FANCYBLOCK.get().getRegistryName(), ""), new
        //        FancyBakedModel(DefaultVertexFormats.BLOCK));
    }

    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseInputEvent event) {
        if (this.mc.currentScreen != null || this.mc.player == null) {
            return;
        }
        KeyBinding attack = this.mc.gameSettings.keyBindAttack;
        KeyBinding use = this.mc.gameSettings.keyBindUseItem;
        if (attack.getKey().getType() == InputMappings.Type.MOUSE) {
            if (attack.getKey().getKeyCode() == event.getButton()) {
                if (event.getAction() == GLFW.GLFW_PRESS && !this.isRightPressed) {
                    this.isLeftPressed = true;
                    this.onLeftMouseClick();
                }
                else {
                    this.isLeftPressed = false;
                }
            }
        }
        if (use.getKey().getType() == InputMappings.Type.MOUSE) {
            if (use.getKey().getKeyCode() == event.getButton()) {
                if (event.getAction() == GLFW.GLFW_PRESS && !this.isLeftPressed) {
                    this.isRightPressed = true;
                    this.onRightMouseClick();
                }
                else {
                    this.isRightPressed = false;
                }
            }
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

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (this.mc.player.getRidingEntity() != null) {
            ForgeIngameGui.renderFood = true;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
            this.renderAttackIndicator();
            return;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH) {
            event.setCanceled(true);
            this.renderHealth();
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderSpecificHandEvent event) {
        if (event.getHand() == Hand.OFF_HAND && this.mc.player.getHeldItemOffhand().getItem() instanceof IOffhandAttackable) {
            event.setCanceled(true);
            float partialTicks = event.getPartialTicks();
            float pitch = event.getInterpolatedPitch();
            float rightSwingProgress = this.getRightSwingProgress(partialTicks);
            FirstPersonRenderer renderer = this.mc.getFirstPersonRenderer();
            float rightEquipProgress = 1.0F - MathHelper.lerp(partialTicks, this.rightPrevEquipProgress, this.rightEquipProgress);
            renderer.renderItemInFirstPerson(this.mc.player,
                                             partialTicks,
                                             pitch,
                                             Hand.OFF_HAND,
                                             rightSwingProgress,
                                             this.mc.player.getHeldItemOffhand(),
                                             rightEquipProgress);
            return;
        }
        if (event.getHand() == Hand.MAIN_HAND && this.requiresReequiping) {
            event.setCanceled(true);
            float partialTicks = event.getPartialTicks();
            float pitch = event.getInterpolatedPitch();
            float leftSwingProgress = this.getLeftSwingProgress(partialTicks);
            FirstPersonRenderer renderer = this.mc.getFirstPersonRenderer();
            float leftEquipProgress = 1.0F - MathHelper.lerp(partialTicks, this.leftPrevEquipProgress, this.leftEquipProgress);
            if (this.leftEquipProgress == 1 && this.leftPrevEquipProgress == 1 && this.leftTimeSinceLastHit != 0) {
                this.requiresReequiping = false;
            }
            renderer.renderItemInFirstPerson(this.mc.player,
                                             partialTicks,
                                             pitch,
                                             Hand.MAIN_HAND,
                                             leftSwingProgress,
                                             this.mc.player.getHeldItemMainhand(),
                                             leftEquipProgress);
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
                this.renderOutlines(tile.type.getShape(), event.getInfo(), hitPos);
                return;
            }
            if (this.mc.world.getBlockState(hitPos).getBlock() instanceof BlockMolding) {
                TEMolding tile = (TEMolding) this.mc.world.getTileEntity(hitPos);
                this.renderOutlines(tile.molding.getShape(), event.getInfo(), hitPos);
            }
        }
    }

    //Handle offhand attack
    private void onRightMouseClick() {
        Item offhandItem = this.mc.player.getHeldItemOffhand().getItem();
        if (!(offhandItem instanceof IOffhandAttackable)) {
            return;
        }
        ItemStack mainHandStack = this.mc.player.getHeldItemMainhand();
        float cooldown = getRightCooldownPeriod((IOffhandAttackable) offhandItem);
        if (this.rightTimeSinceLastHit >= cooldown &&
            mainHandStack.getUseAction() == UseAction.NONE &&
            this.mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
            this.rightTimeSinceLastHit = 0;
            double rayTraceY = this.rightRayTrace != null ? this.rightRayTrace.getHitVec().y : Double.NaN;
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlayerAttack(this.rightPointedEntity, Hand.OFF_HAND, rayTraceY));
            this.swingArm(Hand.OFF_HAND);
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        //        if (!event.getMap().getBasePath().equals("textures")) {
        //            return;
        //        }
        //        event.addSprite(new ResourceLocation(Evolution.MODID, "block/clay"));
    }

    private boolean rayTraceMouse(RayTraceResult rayTraceResult) {
        if (rayTraceResult == null) {
            return false;
        }
        if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
            return ((EntityRayTraceResult) rayTraceResult).getEntity() instanceof INamedContainerProvider;
        }
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult) rayTraceResult).getPos();
            World world = this.mc.world;
            return world.getBlockState(blockpos).getContainer(world, blockpos) != null;
        }
        return false;
    }

    private void renderAttackIndicator() {
        GameSettings gamesettings = this.mc.gameSettings;
        boolean offhandValid = this.mc.player.getHeldItemOffhand().getItem() instanceof IOffhandAttackable;
        this.mc.getTextureManager().bindTexture(ICONS);
        int scaledWidth = this.mc.mainWindow.getScaledWidth();
        int scaledHeight = this.mc.mainWindow.getScaledHeight();
        if (gamesettings.thirdPersonView == 0) {
            if (this.mc.playerController.getCurrentGameType() != GameType.SPECTATOR || this.rayTraceMouse(this.mc.objectMouseOver)) {
                if (gamesettings.showDebugInfo && !gamesettings.hideGUI && !this.mc.player.hasReducedDebug() && !gamesettings.reducedDebugInfo) {
                    GlStateManager.pushMatrix();
                    int blitOffset = 0;
                    GlStateManager.translatef(scaledWidth / 2.0F, scaledHeight / 2.0F, blitOffset);
                    ActiveRenderInfo activerenderinfo = this.mc.gameRenderer.getActiveRenderInfo();
                    GlStateManager.rotatef(activerenderinfo.getPitch(), -1.0f, 0.0F, 0.0F);
                    GlStateManager.rotatef(activerenderinfo.getYaw(), 0.0F, 1.0F, 0.0F);
                    GlStateManager.scalef(-1.0f, -1.0f, -1.0f);
                    GLX.renderCrosshair(10);
                    GlStateManager.popMatrix();
                }
                else {
                    GlStateManager.enableBlend();
                    GlStateManager.enableAlphaTest();
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                                     GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                                     GlStateManager.SourceFactor.ONE,
                                                     GlStateManager.DestFactor.ZERO);
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                                                     GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                                                     GlStateManager.SourceFactor.ONE,
                                                     GlStateManager.DestFactor.ZERO);
                    blit((scaledWidth - 15) / 2, (scaledHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float leftCooledAttackStrength = this.getLeftCooledAttackStrength(0);
                        boolean shouldShowLeftAttackIndicator = false;
                        if (this.leftPointedEntity instanceof LivingEntity && leftCooledAttackStrength >= 1) {
                            shouldShowLeftAttackIndicator = this.mc.player.getCooldownPeriod() > 5;
                            shouldShowLeftAttackIndicator &= this.leftPointedEntity.canBeAttackedWithItem();
                            if (this.mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
                                shouldShowLeftAttackIndicator = false;
                            }
                        }
                        int x = scaledWidth / 2 - 8;
                        x = offhandValid ? x + 10 : x;
                        int y = scaledHeight / 2 - 7 + 16;
                        if (shouldShowLeftAttackIndicator) {
                            blit(x, y, 68, 94, 16, 16);
                        }
                        else if (leftCooledAttackStrength < 1.0F) {
                            int l = (int) (leftCooledAttackStrength * 17.0F);
                            blit(x, y, 36, 94, 16, 4);
                            blit(x, y, 52, 94, l, 4);
                        }
                        if (offhandValid) {
                            boolean shouldShowRightAttackIndicator = false;
                            float rightCooledAttackStrength = this.getRightCooledAttackStrength(this.mc.player.getHeldItemOffhand().getItem(), 0);
                            if (this.rightPointedEntity instanceof LivingEntity && rightCooledAttackStrength >= 1) {
                                shouldShowRightAttackIndicator = this.rightPointedEntity.canBeAttackedWithItem();
                                if (this.mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
                                    shouldShowRightAttackIndicator = false;
                                }
                            }
                            x -= 20;
                            if (shouldShowRightAttackIndicator) {
                                blit(x, y, 68, 110, 16, 16);
                            }
                            else if (rightCooledAttackStrength < 1.0F) {
                                int l = (int) (rightCooledAttackStrength * 17.0F);
                                blit(x, y, 36, 110, 16, 4);
                                blit(x, y, 52, 110, l, 4);
                            }
                        }
                        GlStateManager.disableAlphaTest();
                    }
                }
            }
        }
    }

    private void renderHealth() {
        this.mc.getTextureManager().bindTexture(ICONS);
        int width = this.mc.mainWindow.getScaledWidth();
        int height = this.mc.mainWindow.getScaledHeight();
        this.mc.getProfiler().startSection("health");
        GlStateManager.enableBlend();
        PlayerEntity player = (PlayerEntity) this.mc.getRenderViewEntity();
        float currentHealth = player.getHealth();
        boolean updateHealth = false;
        //Take damage
        if (currentHealth - this.playerHealth <= -2.5f && player.hurtResistantTime > 0) {
            this.lastSystemTime = Util.milliTime();
            this.healthUpdateCounter = this.ticks + 20;
            updateHealth = true;
        }
        //Regen Health
        else if (currentHealth - this.playerHealth > 2.5f && player.hurtResistantTime > 0) {
            this.lastSystemTime = Util.milliTime();
            this.healthUpdateCounter = this.ticks + 10;
            updateHealth = true;
        }
        //Update variables every 1s
        if (Util.milliTime() - this.lastSystemTime > 1_000L) {
            this.lastPlayerHealth = currentHealth;
            this.lastSystemTime = Util.milliTime();
        }
        if (updateHealth) {
            this.playerHealth = roundToHearts(currentHealth);
        }
        float healthMax = (float) player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getValue();
        if (currentHealth > healthMax - 2.5F && this.playerHealth == healthMax - 2.5F) {
            this.playerHealth = healthMax;
        }
        //The health bar flashes
        boolean highlight = this.healthUpdateCounter > this.ticks && (this.healthUpdateCounter - this.ticks) / 3L % 2L != 0L;
        float healthLast = this.lastPlayerHealth;
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());
        int healthRows = MathHelper.ceil((healthMax + absorb) / 100.0F);
        int top = height - ForgeIngameGui.left_height;
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        ForgeIngameGui.left_height += healthRows * rowHeight;
        if (rowHeight != 10) {
            ForgeIngameGui.left_height += 10 - rowHeight;
        }
        int regen = -1;
        if (player.isPotionActive(Effects.REGENERATION)) {
            regen = this.ticks % 25;
        }
        final int heartTextureYPos = this.mc.world.getWorldInfo().isHardcore() ? 45 : 0;
        final int heartBackgroundXPos = highlight ? 25 : 16;
        int margin = 16;
        if (player.isPotionActive(Effects.POISON)) {
            margin += 72;
        }
        int absorbRemaining = MathHelper.ceil(absorb);
        int left = width / 2 - 91;
        for (int currentHeart = MathHelper.ceil((healthMax + absorb) / 10.0F) - 1; currentHeart >= 0; --currentHeart) {
            int row = MathHelper.ceil((currentHeart + 1) / 10.0F) - 1;
            int x = left + currentHeart % 10 * 8;
            int y = top - row * rowHeight;
            //Shake hearts if 20% HP
            if (currentHealth <= 20.0F) {
                y += this.rand.nextInt(2);
            }
            if (currentHeart == regen) {
                y -= 2;
            }
            blit(x, y, heartBackgroundXPos, heartTextureYPos, 9, 9);
            if (highlight) {
                if (currentHeart * 10.0F + 7.5F < healthLast) {
                    //Faded full heart
                    blit(x, y, margin + 54, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 5.0F < healthLast) {
                    //Faded 3/4 heart
                    blit(x, y, margin + 63, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 2.5F < healthLast) {
                    //Faded half heart
                    blit(x, y, margin + 72, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F < healthLast) {
                    //Faded 1/4 heart
                    blit(x, y, margin + 81, heartTextureYPos, 9, 9);
                }
            }
            if (absorbRemaining > 0) {
                int absorbHeart = absorbRemaining % 10;
                switch (absorbHeart) {
                    case 1:
                    case 2:
                        blit(x, y, 205, heartTextureYPos, 9, 9);
                        break;
                    case 3:
                    case 4:
                    case 5:
                        blit(x, y, 196, heartTextureYPos, 9, 9);
                        break;
                    case 6:
                    case 7:
                        blit(x, y, 187, heartTextureYPos, 9, 9);
                        break;
                    case 8:
                    case 9:
                    case 0:
                        blit(x, y, 178, heartTextureYPos, 9, 9);
                        break;
                }
                if (absorbHeart == 0) {
                    absorbRemaining -= 10;
                }
                else {
                    absorbRemaining -= absorbHeart;
                }
            }
            else {
                if (currentHeart * 10.0F + 7.5F < currentHealth) {
                    //Full heart
                    blit(x, y, margin + 18, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 5.0F < currentHealth) {
                    //3/4 heart
                    blit(x, y, margin + 27, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 2.5F < currentHealth) {
                    //Half heart
                    blit(x, y, margin + 36, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F < currentHealth) {
                    //1/4 heart
                    blit(x, y, margin + 45, heartTextureYPos, 9, 9);
                }
            }
        }
        GlStateManager.disableBlend();
        this.mc.getProfiler().endSection();
    }

    private void renderOutlines(VoxelShape shape, ActiveRenderInfo info, BlockPos pos) {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                         GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                         GlStateManager.SourceFactor.ONE,
                                         GlStateManager.DestFactor.ZERO);
        GlStateManager.lineWidth(Math.max(2.5F, this.mc.mainWindow.getFramebufferWidth() / 1_920.0F * 2.5F));
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.matrixMode(5_889);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.0F, 1.0F, 0.999F);
        double projX = info.getProjectedView().x;
        double projY = info.getProjectedView().y;
        double projZ = info.getProjectedView().z;
        WorldRenderer.drawShape(shape, pos.getX() - projX, pos.getY() - projY, pos.getZ() - projZ, 1.0F, 1.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5_888);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    @SubscribeEvent
    public void renderTooltip(RenderTooltipEvent.PostText event) {
        ItemStack stack = event.getStack();
        Item item = stack.getItem();
        if (stack.isFood()) {
            Food food = item.getFood();
            if (food != null) {
                GlStateManager.pushMatrix();
                GlStateManager.color3f(1.0F, 1.0F, 1.0F);
                Minecraft mc = Minecraft.getInstance();
                mc.getTextureManager().bindTexture(ICONS);
                int pips = food.getHealing();
                boolean poison = false;
                for (Pair<EffectInstance, Float> effect : food.getEffects()) {
                    if (effect.getLeft().getPotion().getEffectType() == EffectType.HARMFUL) {
                        poison = true;
                        break;
                    }
                }
                int count = MathHelper.ceil((double) pips / 2);
                int y = shiftTextByLines(event.getLines(), event.getY() + 10);
                for (int i = 0; i < count; i++) {
                    int x = event.getX() + i * 9 - 1;
                    int textureX = 16;
                    if (poison) {
                        textureX += 117;
                    }
                    int textureY = 27;
                    blit(x, y, textureX, textureY, 9, 9);
                    textureX = 52;
                    if (pips % 2 != 0 && i == 0) {
                        textureX += 9;
                    }
                    if (poison) {
                        textureX += 36;
                    }
                    blit(x, y, textureX, textureY, 9, 9);
                }
                GlStateManager.popMatrix();
            }
        }
        else if (item instanceof IEvolutionItem) {
            GlStateManager.pushMatrix();
            GlStateManager.color3f(1.0F, 1.0F, 1.0F);
            Minecraft mc = Minecraft.getInstance();
            mc.getTextureManager().bindTexture(ICONS);
            boolean hasMass = false;
            boolean hasDamage = false;
            boolean hasSpeed = false;
            boolean hasReach = false;
            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                Multimap<String, AttributeModifier> multimap = stack.getAttributeModifiers(slot);
                if (!multimap.isEmpty()) {
                    for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_DAMAGE_MODIFIER) == 0) {
                            hasDamage = true;
                            continue;
                        }
                        if (attributemodifier.getID().compareTo(EvolutionAttributes.ATTACK_SPEED_MODIFIER) == 0) {
                            hasSpeed = true;
                            continue;
                        }
                        if (attributemodifier.getID() == EvolutionAttributes.REACH_DISTANCE_MODIFIER) {
                            hasReach = true;
                            continue;
                        }
                        if (attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER ||
                            attributemodifier.getID() == EvolutionAttributes.MASS_MODIFIER_OFFHAND) {
                            hasMass = true;
                        }
                    }
                    break;
                }
            }
            int line = 1;
            if (item instanceof IFireAspect) {
                line++;
            }
            if (item instanceof IHeavyAttack) {
                line++;
            }
            if (item instanceof IKnockback) {
                line++;
            }
            if (item instanceof ISweepAttack) {
                line++;
            }
            if (stack.hasTag()) {
                if (stack.getTag().contains("display", 10)) {
                    CompoundNBT display = stack.getTag().getCompound("display");
                    if (display.contains("color", 3)) {
                        line++;
                    }
                    if (display.getTagId("Lore") == 9) {
                        for (int j = 0; j < display.getList("Lore", 8).size(); ++j) {
                            line++;
                        }
                    }
                }
            }
            if (hasMass) {
                line++;
                //Mass line
                int x = event.getX();
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 0;
                int textureY = 247;
                blit(x, y, textureX, textureY, 9, 9);
                line++;
            }
            boolean hasAddedLine = false;
            //Two Handed line
            if (item instanceof ITwoHanded) {
                line += 2;
                hasAddedLine = true;
            }
            //Throwable line
            if (item instanceof IThrowable) {
                if (hasAddedLine) {
                    line++;
                }
                else {
                    line += 2;
                }
                hasAddedLine = true;
            }
            boolean hasAttributes = hasSpeed || hasDamage || hasReach;
            if (!hasAddedLine) {
                if (hasAttributes) {
                    line++;
                }
            }
            if (hasAttributes) {
                //Slot name
                line++;
            }
            //Attack Speed
            if (hasSpeed) {
                int x = event.getX() + 4;
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 63;
                int textureY = 247;
                blit(x, y, textureX, textureY, 9, 9);
                line++;
            }
            //Attack Damage
            if (hasDamage) {
                int textureX = 54;
                if (item instanceof IMelee) {
                    switch (((IMelee) item).getDamageType()) {
                        case CRUSHING:
                            textureX = 36;
                            break;
                        case PIERCING:
                            textureX = 45;
                            break;
                        case SLASHING:
                            textureX = 27;
                            break;
                    }
                }
                int x = event.getX() + 4;
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureY = 247;
                blit(x, y, textureX, textureY, 9, 9);
                line++;
            }
            //Reach distance
            if (hasReach) {
                int x = event.getX() + 4;
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 18;
                int textureY = 247;
                blit(x, y, textureX, textureY, 9, 9);
                line++;
            }
            //Mining Speed
            if (item instanceof ItemGenericTool) {
                int x = event.getX() + 4;
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 9;
                int textureY = 247;
                blit(x, y, textureX, textureY, 9, 9);
                line++;
            }
            //Unbreakable
            if (stack.hasTag() && stack.getTag().getBoolean("Unbreakable")) {
                line++;
            }
            //Durability
            if (stack.getItem() instanceof IDurability) {
                int x = event.getX();
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 72;
                int textureY = 247;
                blit(x, y, textureX, textureY, 9, 9);
            }
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void shutDownInternalServer(FMLServerStoppedEvent event) {
        if (this.inverted) {
            this.inverted = false;
            swapControls(this.mc);
        }
    }

    public void swingArm(Hand hand) {
        ItemStack stack = this.mc.player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(this.mc.player)) {
            return;
        }
        if (hand == Hand.OFF_HAND) {
            if (!this.rightIsSwingInProgress || this.rightSwingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.rightSwingProgressInt < 0) {
                this.rightSwingProgressInt = -1;
                this.rightIsSwingInProgress = true;
            }
        }
        else {
            if (!this.leftIsSwingInProgress || this.leftSwingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.leftSwingProgressInt < 0) {
                this.leftSwingProgressInt = -1;
                this.leftIsSwingInProgress = true;
            }
        }
    }

    private void updateArmSwingProgress() {
        int i = this.getArmSwingAnimationEnd();
        if (this.rightIsSwingInProgress) {
            ++this.rightSwingProgressInt;
            if (this.rightSwingProgressInt >= i) {
                this.rightSwingProgressInt = 0;
                this.rightIsSwingInProgress = false;
            }
        }
        else {
            this.rightSwingProgressInt = 0;
        }
        this.rightSwingProgress = this.rightSwingProgressInt / (float) i;
        if (this.leftIsSwingInProgress) {
            ++this.leftSwingProgressInt;
            if (this.leftSwingProgressInt >= i) {
                this.leftSwingProgressInt = 0;
                this.leftIsSwingInProgress = false;
            }
        }
        else {
            this.leftSwingProgressInt = 0;
        }
        this.leftSwingProgress = this.leftSwingProgressInt / (float) i;
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
