package tgw.evolution.events;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import tgw.evolution.ClientProxy;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockKnapping;
import tgw.evolution.blocks.BlockMolding;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.IOffhandAttackable;
import tgw.evolution.items.ITwoHanded;
import tgw.evolution.network.PacketCSOffhandAttack;
import tgw.evolution.network.PacketCSOpenExtendedInventory;
import tgw.evolution.network.PacketCSSetProne;
import tgw.evolution.potion.EffectDizziness;
import tgw.evolution.util.EvolutionStyles;

import java.util.UUID;

public class ClientEvents {

    private static final ResourceLocation ICONS = Evolution.location("textures/gui/icons.png");
    private static final String TWO_HANDED = "evolution.actionbar.two_handed";
    private static final ITextComponent COMPONENT_TWO_HANDED = new TranslationTextComponent(TWO_HANDED).setStyle(EvolutionStyles.WHITE);
    private final Minecraft mc;
    private boolean inverted;
    private boolean jump;
    private boolean isJumpPressed;
    private boolean renderFood;
    private boolean previousPressed;
    private boolean proneToggle;
    private int timeSinceLastHit;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private float swingProgress;
    private float prevSwingProgress;
    private int swingProgressInt;
    private boolean isSwingInProgress;
    private float equipProgress;
    private float prevEquipProgress;

    public ClientEvents(Minecraft mc) {
        this.mc = mc;
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

    private static float getCooldownPeriod(IOffhandAttackable item) {
        float attackSpeed = item.getAttackSpeed() + 4;
        return 1 / attackSpeed * 20;
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
                GL11.glFogi(34138, 34139);
            }
            event.setDensity(2.0F);
            event.setCanceled(true);
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
                //TODO clay molding outlines
            }
        }
    }

    private void renderOutlines(VoxelShape shape, ActiveRenderInfo info, BlockPos pos) {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.lineWidth(Math.max(2.5F, this.mc.mainWindow.getFramebufferWidth() / 1920.0F * 2.5F));
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.0F, 1.0F, 0.999F);
        double projX = info.getProjectedView().x;
        double projY = info.getProjectedView().y;
        double projZ = info.getProjectedView().z;
        WorldRenderer.drawShape(shape, pos.getX() - projX, pos.getY() - projY, pos.getZ() - projZ, 1.0F, 1.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        //Turn auto-jump off
        this.mc.gameSettings.autoJump = false;
        if (this.mc.player == null) {
            return;
        }
        //Jump calculation
        if (this.jump) {
            PlayerEntity player = this.mc.player;
            if (player.getMotion().y > 0 && !player.isOnLadder() && !player.abilities.isFlying) {
                player.setMotion(player.getMotion().x, player.getMotion().y - 0.03, player.getMotion().z);
            }
            else {
                this.jump = false;
                if (!player.isOnLadder() && !player.abilities.isFlying) {
                    player.setMotion(player.getMotion().x, player.getMotion().y - 0.055, player.getMotion().z);
                }
            }
        }
        //Runs at the start of each tick
        if (event.phase == TickEvent.Phase.START) {
            //Handle two-handed items
            if (this.mc.player.getHeldItemMainhand().getItem() instanceof ITwoHanded && !this.mc.player.getHeldItemOffhand().isEmpty()) {
                ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, this.mc, Integer.MAX_VALUE, "field_71429_W");
                if (this.mc.gameSettings.keyBindAttack.isPressed()) {
                    this.mc.player.sendStatusMessage(COMPONENT_TWO_HANDED, true);
                }
            }
            //Handle main hand cooldown
            else if (this.mc.player.getCooledAttackStrength(0.5f) < 1) {
                ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, this.mc, Integer.MAX_VALUE, "field_71429_W");
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
            //Proning
            boolean pressed = ClientProxy.TOGGLE_PRONE.isKeyDown();
            if (pressed && !this.previousPressed) {
                this.proneToggle = !this.proneToggle;
            }
            this.previousPressed = pressed;
            this.updateClientProneState(this.mc.player);
            //Handle Offhand swing
            this.prevSwingProgress = this.swingProgress;
            this.updateArmSwingProgress();
            this.prevEquipProgress = this.equipProgress;
            if (this.mc.player.isRowingBoat()) {
                this.equipProgress = MathHelper.clamp(this.equipProgress - 0.4F, 0.0F, 1.0F);
            }
            else {
                float cooledAttackStrength = this.getCooledAttackStrength(this.mc.player.getHeldItemOffhand().getItem(), 1);
                this.equipProgress += MathHelper.clamp(cooledAttackStrength * cooledAttackStrength * cooledAttackStrength - this.equipProgress, -0.4F, 0.4F);
            }
            ItemStack stack = this.mc.player.getHeldItemOffhand();
            if (ItemStack.areItemStacksEqual(stack, this.offhandStack)) {
                this.timeSinceLastHit++;
            }
            else {
                this.timeSinceLastHit = 0;
                this.offhandStack = stack;
            }
        }
    }

    private void updateClientProneState(PlayerEntity player) {
        if (player != null) {
            UUID uuid = player.getUniqueID();
            boolean shouldBeProne = ClientProxy.TOGGLE_PRONE.isKeyDown() != this.proneToggle;
            shouldBeProne = shouldBeProne && !player.isInWater() && !player.isInLava() && (!player.isOnLadder() || !this.isJumpPressed && player.onGround);
            shouldBeProne = shouldBeProne && (!player.isOnLadder() || !this.isJumpPressed && player.onGround);
            BlockPos pos = player.getPosition().up(2);
            shouldBeProne = shouldBeProne || this.proneToggle && player.isOnLadder() && !player.world.getBlockState(pos).getCollisionShape(player.world, pos, null).isEmpty();
            if (shouldBeProne != Evolution.PRONED_PLAYERS.getOrDefault(uuid, false)) {
                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetProne(shouldBeProne));
            }
            Evolution.PRONED_PLAYERS.put(uuid, shouldBeProne);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD) {
            this.renderFood = true;
        }
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
            this.renderAttackIndicator();
        }
        if (!event.isCancelable() || !this.renderFood) {
            return;
        }
        if (this.mc.player.getRidingEntity() != null) {
            ForgeIngameGui.renderFood = true;
        }
    }

    private void updateArmSwingProgress() {
        int i = this.getArmSwingAnimationEnd();
        if (this.isSwingInProgress) {
            ++this.swingProgressInt;
            if (this.swingProgressInt >= i) {
                this.swingProgressInt = 0;
                this.isSwingInProgress = false;
            }
        }
        else {
            this.swingProgressInt = 0;
        }
        this.swingProgress = (float) this.swingProgressInt / (float) i;
    }

    public void swingArm(Hand hand) {
        ItemStack stack = this.mc.player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(this.mc.player)) {
            return;
        }
        if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
            this.swingProgressInt = -1;
            this.isSwingInProgress = true;
        }
    }

    private int getArmSwingAnimationEnd() {
        if (EffectUtils.hasMiningSpeedup(this.mc.player)) {
            return 6 - (1 + EffectUtils.getMiningSpeedup(this.mc.player));
        }
        return this.mc.player.isPotionActive(Effects.MINING_FATIGUE) ? 6 + (1 + this.mc.player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
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
                    GlStateManager.translatef((float) (scaledWidth / 2), (float) (scaledHeight / 2), (float) blitOffset);
                    ActiveRenderInfo activerenderinfo = this.mc.gameRenderer.getActiveRenderInfo();
                    GlStateManager.rotatef(activerenderinfo.getPitch(), -1.0F, 0.0F, 0.0F);
                    GlStateManager.rotatef(activerenderinfo.getYaw(), 0.0F, 1.0F, 0.0F);
                    GlStateManager.scalef(-1.0F, -1.0F, -1.0F);
                    GLX.renderCrosshair(10);
                    GlStateManager.popMatrix();
                }
                else {
                    GlStateManager.enableBlend();
                    GlStateManager.enableAlphaTest();
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    this.blit((scaledWidth - 15) / 2, (scaledHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.mc.gameSettings.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float leftCooledAttackStrength = this.mc.player.getCooledAttackStrength(0);
                        boolean shouldShowLeftAttackIndicator = false;
                        if (this.mc.pointedEntity != null && this.mc.pointedEntity instanceof LivingEntity && leftCooledAttackStrength >= 1) {
                            shouldShowLeftAttackIndicator = this.mc.player.getCooldownPeriod() > 5;
                            shouldShowLeftAttackIndicator = shouldShowLeftAttackIndicator & this.mc.pointedEntity.isAlive();
                        }
                        int x = scaledWidth / 2 - 8;
                        x = offhandValid ? x + 10 : x;
                        int y = scaledHeight / 2 - 7 + 16;
                        if (shouldShowLeftAttackIndicator) {
                            this.blit(x, y, 68, 94, 16, 16);
                        }
                        else if (leftCooledAttackStrength < 1.0F) {
                            int l = (int) (leftCooledAttackStrength * 17.0F);
                            this.blit(x, y, 36, 94, 16, 4);
                            this.blit(x, y, 52, 94, l, 4);
                        }
                        if (offhandValid) {
                            boolean shouldShowRightAttackIndicator = false;
                            float rightCooledAttackStrength = this.getCooledAttackStrength(this.mc.player.getHeldItemOffhand().getItem(), 0);
                            if (this.mc.pointedEntity != null && this.mc.pointedEntity instanceof LivingEntity && rightCooledAttackStrength >= 1) {
                                shouldShowRightAttackIndicator = this.mc.pointedEntity.isAlive();
                            }
                            x -= 20;
                            if (shouldShowRightAttackIndicator) {
                                this.blit(x, y, 68, 110, 16, 16);
                            }
                            else if (rightCooledAttackStrength < 1.0F) {
                                int l = (int) (rightCooledAttackStrength * 17.0F);
                                this.blit(x, y, 36, 110, 16, 4);
                                this.blit(x, y, 52, 110, l, 4);
                            }
                        }
                        GlStateManager.disableAlphaTest();
                    }
                }
            }
        }
    }

    private void blit(int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        AbstractGui.blit(x, y, 20, (float) textureX, (float) textureY, sizeX, sizeY, 256, 256);
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

    @SubscribeEvent
    public void onRenderHand(RenderSpecificHandEvent event) {
        if (event.getHand() == Hand.OFF_HAND && this.mc.player.getHeldItemOffhand().getItem() instanceof IOffhandAttackable) {
            event.setCanceled(true);
            float partialTicks = event.getPartialTicks();
            float pitch = event.getInterpolatedPitch();
            float swingProgress = this.getSwingProgress(partialTicks);
            FirstPersonRenderer renderer = this.mc.getFirstPersonRenderer();
            float equipProgress = 1.0F - MathHelper.lerp(partialTicks, this.prevEquipProgress, this.equipProgress);
            renderer.renderItemInFirstPerson(this.mc.player, partialTicks, pitch, Hand.OFF_HAND, swingProgress, this.mc.player.getHeldItemOffhand(), equipProgress);
        }
    }

    private float getSwingProgress(float partialTickTime) {
        float f = this.swingProgress - this.prevSwingProgress;
        if (f < 0.0F) {
            ++f;
        }
        return this.prevSwingProgress + f * partialTickTime;
    }

    @SubscribeEvent
    public void onPlayerInput(InputUpdateEvent event) {
        this.isJumpPressed = event.getMovementInput().jump;
        if (!this.jump && this.mc.player.onGround && this.isJumpPressed && !this.proneToggle) {
            this.jump = true;
            return;
        }
        if (this.proneToggle && !this.mc.player.isOnLadder()) {
            event.getMovementInput().jump = false;
        }
    }

    @SubscribeEvent
    public void shutDownInternalServer(FMLServerStoppedEvent event) {
        if (this.inverted) {
            this.inverted = false;
            swapControls(this.mc);
        }
    }

    private float getCooledAttackStrength(Item item, float adjustTicks) {
        if (!(item instanceof IOffhandAttackable)) {
            return 0;
        }
        return MathHelper.clamp(((float) this.timeSinceLastHit + adjustTicks) / getCooldownPeriod((IOffhandAttackable) item), 0.0F, 1.0F);
    }

    @SubscribeEvent
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof InventoryScreen) {
            event.setCanceled(true);
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSOpenExtendedInventory());
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        //        if (!event.getMap().getBasePath().equals("textures")) {
        //            return;
        //        }
        //        event.addSprite(new ResourceLocation(Evolution.MODID, "block/clay"));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.FANCYBLOCK.get().getRegistryName(), ""), new FancyBakedModel(DefaultVertexFormats.BLOCK));
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.MOLDING.get().getRegistryName(), "layers=1"), new ModelTEMolding(DefaultVertexFormats.BLOCK));
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.MOLDING.get().getRegistryName(), "layers=2"), new ModelTEMolding(DefaultVertexFormats.BLOCK));
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.MOLDING.get().getRegistryName(), "layers=3"), new ModelTEMolding(DefaultVertexFormats.BLOCK));
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.MOLDING.get().getRegistryName(), "layers=4"), new ModelTEMolding(DefaultVertexFormats.BLOCK));
        //        event.getModelRegistry().put(new ModelResourceLocation(EvolutionBlocks.MOLDING.get().getRegistryName(), "layers=5"), new ModelTEMolding(DefaultVertexFormats.BLOCK));
    }

    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseInputEvent event) {
        KeyBinding useItem = this.mc.gameSettings.keyBindUseItem;
        if (useItem.isPressed()) {
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, this.mc, 0, "field_71467_ac");
            this.onRightMouseClick();
        }
    }

    //Handle offhand attack
    private void onRightMouseClick() {
        Item offhandStack = this.mc.player.getHeldItemOffhand().getItem();
        if (!(offhandStack instanceof IOffhandAttackable)) {
            return;
        }
        ItemStack mainHandStack = this.mc.player.getHeldItemMainhand();
        float cooldown = getCooldownPeriod((IOffhandAttackable) offhandStack);
        if (offhandStack instanceof IOffhandAttackable && this.timeSinceLastHit >= cooldown && mainHandStack.getUseAction() == UseAction.NONE) {
            this.timeSinceLastHit = 0;
            Evolution.LOGGER.debug("attack");
            EvolutionNetwork.INSTANCE.sendToServer(new PacketCSOffhandAttack());
            this.swingArm(Hand.OFF_HAND);
        }
    }
}
