package tgw.evolution.client.renderer;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeIngameGui;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.InputHooks;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.items.*;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientRenderer {

    public static ClientRenderer instance;
    private static int slotMainHand;
    private final ClientEvents client;
    private final ItemRenderer itemRenderer;
    private final Minecraft mc;
    private final Random rand = new Random();
    private final EntityRendererManager renderManager;
    private long healthUpdateCounter;
    private float lastPlayerHealth;
    private long lastSystemTime;
    private ItemStack mainHandStack = ItemStack.EMPTY;
    private float mainhandEquipProgress;
    private boolean mainhandIsSwingInProgress;
    private int mainhandLungingTicks;
    private float mainhandPrevEquipProgress;
    private float mainhandPrevSwingProgress;
    private float mainhandSwingProgress;
    private int mainhandSwingProgressInt;
    private float offhandEquipProgress;
    private boolean offhandIsSwingInProgress;
    private int offhandLungingTicks;
    private float offhandPrevEquipProgress;
    private float offhandPrevSwingProgress;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private float offhandSwingProgress;
    private int offhandSwingProgressInt;
    private float playerHealth;

    public ClientRenderer(Minecraft mc, ClientEvents client) {
        instance = this;
        this.mc = mc;
        this.client = client;
        this.renderManager = mc.getRenderManager();
        this.itemRenderer = mc.getItemRenderer();
    }

    private static void blit(int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        AbstractGui.blit(x, y, 20, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    private static void blit(int x, int y, int textureX, int textureY, int sizeX, int sizeY, int blitOffset) {
        AbstractGui.blit(x, y, blitOffset, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    private static float getMapAngleFromPitch(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * MathHelper.PI) * 0.5F + 0.5F;
        return f;
    }

    private static void rotateArroundXAndY(float angle, float angleY) {
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(angleY, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static float roundToHearts(float currentHealth) {
        return MathHelper.floor(currentHealth * 0.4F) / 0.4F;
    }

    private static int shiftTextByLines(int desiredLine, int y) {
        return y + 10 * (desiredLine - 1) + 1;
    }

    private static int shiftTextByLines(List<String> lines, int y) {
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

    public static boolean shouldCauseReequipAnimation(@Nonnull ItemStack from, @Nonnull ItemStack to, int slot) {
        boolean fromInvalid = from.isEmpty();
        boolean toInvalid = to.isEmpty();
        if (fromInvalid && toInvalid) {
            return false;
        }
        if (fromInvalid || toInvalid) {
            return true;
        }
        boolean changed = false;
        if (slot != -1) {
            changed = slot != slotMainHand;
            slotMainHand = slot;
        }
        if (changed) {
            return true;
        }
        return !MathHelper.areItemStacksSufficientlyEqual(from, to);
    }

    private static void transformFirstPerson(HandSide hand, float swingProgress) {
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * MathHelper.PI);
        GlStateManager.rotatef(sideOffset * (45.0F + f * -20.0F), 0.0F, 1.0F, 0.0F);
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
        GlStateManager.rotatef(sideOffset * f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotatef(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * -45.0F, 0.0F, 1.0F, 0.0F);
    }

    private static void transformLungeFirstPerson(float partialTicks, Hand hand, HandSide handSide, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ILunge) {
            float lungeTime = InputHooks.getLungeTime(hand) + partialTicks;
            int minLunge = ((ILunge) item).getMinLungeTime();
            int fullLunge = ((ILunge) item).getFullLungeTime();
            float relativeLunge = MathHelper.relativize(lungeTime, minLunge, fullLunge);
            GlStateManager.rotatef(handSide == HandSide.RIGHT ? -5.0F : 5.0F, 0.0F, 1.0F, 0.0F);
            float relativeRotation = MathHelper.clamp(relativeLunge * 3.0f, 0.0f, 1.0f);
            GlStateManager.rotatef(-100.0F * relativeRotation, 1.0F, 0.0F, 0.0F);
            if (relativeLunge > 0.333f) {
                float relativeTranslation = MathHelper.relativize(relativeLunge, 0.333f, 1.0f);
                GlStateManager.translatef(0.0f, -0.6f * relativeTranslation, 0.0f);
            }
        }
    }

    private static void transformSideFirstPerson(HandSide hand, float equippedProg) {
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        GlStateManager.translatef(sideOffset * 0.56F, -0.52F + equippedProg * -0.6F, -0.72F);
    }

    private static void transformSideFirstPersonParry(HandSide hand, float equippedProg) {
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        GlStateManager.translatef(sideOffset * 0.56F, -0.52F + equippedProg * -0.6F, -0.72F);
        GlStateManager.translatef(sideOffset * -0.141_421_36F, 0.08F, 0.141_421_36F);
        GlStateManager.rotatef(-102.25F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * 13.365F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * 78.05F, 0.0F, 0.0F, 1.0F);
    }

    private int getArmSwingAnimationEnd() {
        if (EffectUtils.hasMiningSpeedup(this.mc.player)) {
            return 6 - (1 + EffectUtils.getMiningSpeedup(this.mc.player));
        }
        return this.mc.player.isPotionActive(Effects.MINING_FATIGUE) ?
               6 + (1 + this.mc.player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2 :
               6;
    }

    private float getMainhandSwingProgress(float partialTickTime) {
        float swingedProgress = this.mainhandSwingProgress - this.mainhandPrevSwingProgress;
        if (swingedProgress < 0.0F) {
            ++swingedProgress;
        }
        return this.mainhandPrevSwingProgress + swingedProgress * partialTickTime;
    }

    private float getOffhandSwingProgress(float partialTickTime) {
        float swingedProgress = this.offhandSwingProgress - this.offhandPrevSwingProgress;
        if (swingedProgress < 0.0F) {
            ++swingedProgress;
        }
        return this.offhandPrevSwingProgress + swingedProgress * partialTickTime;
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

    private void renderArm(HandSide side) {
        this.mc.getTextureManager().bindTexture(this.mc.player.getLocationSkin());
        EntityRenderer<AbstractClientPlayerEntity> renderer = this.renderManager.getRenderer(this.mc.player);
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        GlStateManager.pushMatrix();
        float sideOffset = side == HandSide.RIGHT ? 1.0F : -1.0F;
        GlStateManager.rotatef(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * -41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translatef(sideOffset * 0.3F, -1.1F, 0.45F);
        if (side == HandSide.RIGHT) {
            playerRenderer.renderRightArm(this.mc.player);
        }
        else {
            playerRenderer.renderLeftArm(this.mc.player);
        }
        GlStateManager.popMatrix();
    }

    public void renderArmFirstPerson(float equippedProgress, float swingProgress, HandSide side) {
        boolean right = side == HandSide.RIGHT;
        float sideOffset = right ? 1.0F : -1.0F;
        float sqrtSwingProgress = MathHelper.sqrt(swingProgress);
        float f2 = -0.3F * MathHelper.sin(sqrtSwingProgress * MathHelper.PI);
        float f3 = 0.4F * MathHelper.sin(sqrtSwingProgress * MathHelper.TAU);
        float f4 = -0.4F * MathHelper.sin(swingProgress * MathHelper.PI);
        GlStateManager.translatef(sideOffset * (f2 + 0.640_000_05F), f3 - 0.6F + equippedProgress * -0.6F, f4 - 0.719_999_97F);
        GlStateManager.rotatef(sideOffset * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(swingProgress * swingProgress * MathHelper.PI);
        float f6 = MathHelper.sin(sqrtSwingProgress * MathHelper.PI);
        GlStateManager.rotatef(sideOffset * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayerEntity player = this.mc.player;
        this.mc.getTextureManager().bindTexture(player.getLocationSkin());
        GlStateManager.translatef(sideOffset * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotatef(sideOffset * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotatef(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(sideOffset * 5.6F, 0.0F, 0.0F);
        PlayerRenderer playerRenderer = this.renderManager.getRenderer(player);
        GlStateManager.disableCull();
        if (right) {
            playerRenderer.renderRightArm(player);
        }
        else {
            playerRenderer.renderLeftArm(player);
        }
        GlStateManager.enableCull();
    }

    private void renderArms() {
        if (!this.mc.player.isInvisible()) {
            GlStateManager.disableCull();
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
            this.renderArm(HandSide.RIGHT);
            this.renderArm(HandSide.LEFT);
            GlStateManager.popMatrix();
            GlStateManager.enableCull();
        }
    }

    public void renderAttackIndicator() {
        GameSettings gamesettings = this.mc.gameSettings;
        boolean offhandValid = this.mc.player.getHeldItemOffhand().getItem() instanceof IOffhandAttackable;
        this.mc.getTextureManager().bindTexture(EvolutionResources.GUI_ICONS);
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
                        float leftCooledAttackStrength = this.client.getMainhandCooledAttackStrength(0);
                        boolean shouldShowLeftAttackIndicator = false;
                        if (this.client.leftPointedEntity instanceof LivingEntity && leftCooledAttackStrength >= 1) {
                            shouldShowLeftAttackIndicator = this.mc.player.getCooldownPeriod() > 5;
                            shouldShowLeftAttackIndicator &= this.client.leftPointedEntity.canBeAttackedWithItem();
                            if (this.mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
                                shouldShowLeftAttackIndicator = false;
                            }
                        }
                        int sideOffset = this.mc.player.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
                        int x = scaledWidth / 2 - 8;
                        x = offhandValid ? x + 10 * sideOffset : x;
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
                            float rightCooledAttackStrength = this.client.getOffhandCooledAttackStrength(this.mc.player.getHeldItemOffhand()
                                                                                                                       .getItem(), 0);
                            if (this.client.rightPointedEntity instanceof LivingEntity && rightCooledAttackStrength >= 1) {
                                shouldShowRightAttackIndicator = this.client.rightPointedEntity.canBeAttackedWithItem();
                                if (this.mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
                                    shouldShowRightAttackIndicator = false;
                                }
                            }
                            x -= 20 * sideOffset;
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

    public void renderFog(EntityViewRenderEvent.FogDensity event) {
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

    public void renderHealth() {
        this.mc.getTextureManager().bindTexture(EvolutionResources.GUI_ICONS);
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
            this.healthUpdateCounter = this.client.getTickCount() + 20;
            updateHealth = true;
        }
        //Regen Health
        else if (currentHealth - this.playerHealth > 2.5f && player.hurtResistantTime > 0) {
            this.lastSystemTime = Util.milliTime();
            this.healthUpdateCounter = this.client.getTickCount() + 10;
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
        boolean highlight = this.healthUpdateCounter > this.client.getTickCount() &&
                            (this.healthUpdateCounter - this.client.getTickCount()) / 3L % 2L != 0L;
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
            regen = this.client.getTickCount() % 25;
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

    public void renderItemInFirstPerson(float partialTicks) {
        AbstractClientPlayerEntity player = this.mc.player;
        float interpPitch = MathHelper.lerp(partialTicks, player.prevRotationPitch, player.rotationPitch);
        float interpYaw = MathHelper.lerp(partialTicks, player.prevRotationYaw, player.rotationYaw);
        boolean mainHand = true;
        boolean offHand = true;
        if (player.isHandActive()) {
            ItemStack activeStack = player.getActiveItemStack();
            if (activeStack.getItem() instanceof ShootableItem) {
                mainHand = player.getActiveHand() == Hand.MAIN_HAND;
                offHand = !mainHand;
            }
            Hand activeHand = player.getActiveHand();
            if (activeHand == Hand.MAIN_HAND) {
                ItemStack offHandStack = player.getHeldItemOffhand();
                if (offHandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offHandStack)) {
                    offHand = false;
                }
            }
        }
        else {
            ItemStack mainHandStack = player.getHeldItemMainhand();
            ItemStack offhandStack = player.getHeldItemOffhand();
            if (mainHandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(mainHandStack)) {
                offHand = false;
            }
            if (offhandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offhandStack)) {
                mainHand = !mainHandStack.isEmpty();
                offHand = !mainHand;
            }
        }
        rotateArroundXAndY(interpPitch, interpYaw);
        this.setLightmap();
        this.rotateArm(partialTicks);
        GlStateManager.enableRescaleNormal();
        if (mainHand) {
            float swingProg = this.getMainhandSwingProgress(partialTicks);
            float equippedProgress = 1.0F - MathHelper.lerp(partialTicks, this.mainhandPrevEquipProgress, this.mainhandEquipProgress);
            this.renderItemInFirstPerson(player, partialTicks, interpPitch, Hand.MAIN_HAND, swingProg, this.mainHandStack, equippedProgress);
        }
        if (offHand) {
            float swingProg = this.getOffhandSwingProgress(partialTicks);
            float equippedProgress = 1.0F - MathHelper.lerp(partialTicks, this.offhandPrevEquipProgress, this.offhandEquipProgress);
            this.renderItemInFirstPerson(player, partialTicks, interpPitch, Hand.OFF_HAND, swingProg, this.offhandStack, equippedProgress);
        }
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    public void renderItemInFirstPerson(AbstractClientPlayerEntity player,
                                        float partialTicks,
                                        float pitch,
                                        Hand hand,
                                        float swingProgress,
                                        ItemStack stack,
                                        float equippedProgress) {
        boolean mainHand = hand == Hand.MAIN_HAND;
        HandSide handSide = mainHand ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        GlStateManager.pushMatrix();
        if (stack.isEmpty()) {
            if (mainHand && !player.isInvisible()) {
                this.renderArmFirstPerson(equippedProgress, swingProgress, handSide);
            }
        }
        else if (stack.getItem() instanceof FilledMapItem) {
            if (mainHand && this.offhandStack.isEmpty()) {
                this.renderMapFirstPerson(pitch, equippedProgress, swingProgress);
            }
            else {
                this.renderMapFirstPersonSide(equippedProgress, handSide, swingProgress, stack);
            }
        }
        else if (stack.getItem() == Items.CROSSBOW) {
            boolean isCrossbowCharged = CrossbowItem.isCharged(stack);
            boolean isRightSide = handSide == HandSide.RIGHT;
            int sideOffset = isRightSide ? 1 : -1;
            if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand) {
                transformSideFirstPerson(handSide, equippedProgress);
                GlStateManager.translatef(sideOffset * -0.478_568_2F, -0.094_387F, 0.057_315_31F);
                GlStateManager.rotatef(-11.935F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(sideOffset * 65.3F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(sideOffset * -9.785F, 0.0F, 0.0F, 1.0F);
                float f9 = stack.getUseDuration() - (this.mc.player.getItemInUseCount() - partialTicks + 1.0F);
                float f13 = f9 / CrossbowItem.getChargeTime(stack);
                if (f13 > 1.0F) {
                    f13 = 1.0F;
                }
                if (f13 > 0.1F) {
                    float f16 = MathHelper.sin((f9 - 0.1F) * 1.3F);
                    float f3 = f13 - 0.1F;
                    float f4 = f16 * f3;
                    GlStateManager.translatef(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
                }
                GlStateManager.translatef(f13 * 0.0F, f13 * 0.0F, f13 * 0.04F);
                GlStateManager.scalef(1.0F, 1.0F, 1.0F + f13 * 0.2F);
                GlStateManager.rotatef(sideOffset * 45.0F, 0.0F, -1.0F, 0.0F);
            }
            else {
                float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
                float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.TAU);
                float f2 = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
                GlStateManager.translatef(sideOffset * f, f1, f2);
                transformSideFirstPerson(handSide, equippedProgress);
                transformFirstPerson(handSide, swingProgress);
                if (isCrossbowCharged && swingProgress < 0.001F) {
                    GlStateManager.translatef(sideOffset * -0.641_864F, 0.0F, 0.0F);
                    GlStateManager.rotatef(sideOffset * 10.0F, 0.0F, 1.0F, 0.0F);
                }
            }
            this.renderItemSide(player,
                                stack,
                                isRightSide ?
                                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                                ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !isRightSide);
        }
        else {
            boolean rightSide = handSide == HandSide.RIGHT;
            if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand) {
                int sideOffset = rightSide ? 1 : -1;
                switch (stack.getUseAction()) {
                    case NONE:
                        transformSideFirstPerson(handSide, equippedProgress);
                        break;
                    case BLOCK:
                        if (stack.getItem() instanceof IParry) {
                            transformSideFirstPersonParry(handSide, equippedProgress);
                        }
                        else {
                            transformSideFirstPerson(handSide, equippedProgress);
                        }
                        break;
                    case EAT:
                    case DRINK:
                        this.transformEatFirstPerson(partialTicks, handSide, stack);
                        transformSideFirstPerson(handSide, equippedProgress);
                        break;
                    case BOW:
                        transformSideFirstPerson(handSide, equippedProgress);
                        GlStateManager.translatef(sideOffset * -0.278_568_2F, 0.183_443_87F, 0.157_315_31F);
                        GlStateManager.rotatef(-13.935F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotatef(sideOffset * 35.3F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotatef(sideOffset * -9.785F, 0.0F, 0.0F, 1.0F);
                        float f8 = stack.getUseDuration() - (this.mc.player.getItemInUseCount() - partialTicks + 1.0F);
                        float f12 = f8 / 20.0F;
                        f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
                        if (f12 > 1.0F) {
                            f12 = 1.0F;
                        }
                        if (f12 > 0.1F) {
                            float f15 = MathHelper.sin((f8 - 0.1F) * 1.3F);
                            float f18 = f12 - 0.1F;
                            float f20 = f15 * f18;
                            GlStateManager.translatef(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
                        }
                        GlStateManager.translatef(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
                        GlStateManager.scalef(1.0F, 1.0F, 1.0F + f12 * 0.2F);
                        GlStateManager.rotatef(sideOffset * 45.0F, 0.0F, -1.0F, 0.0F);
                        break;
                    case SPEAR:
                        transformSideFirstPerson(handSide, equippedProgress);
                        GlStateManager.translatef(sideOffset * -0.5F, 0.7F, 0.1F);
                        GlStateManager.rotatef(-55.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotatef(sideOffset * 35.3F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotatef(sideOffset * -9.785F, 0.0F, 0.0F, 1.0F);
                        float f7 = stack.getUseDuration() - (this.mc.player.getItemInUseCount() - partialTicks + 1.0F);
                        float f11 = f7 / 10.0F;
                        if (f11 > 1.0F) {
                            f11 = 1.0F;
                        }
                        if (f11 > 0.1F) {
                            float f14 = MathHelper.sin((f7 - 0.1F) * 1.3F);
                            float f17 = f11 - 0.1F;
                            float f19 = f14 * f17;
                            GlStateManager.translatef(f19 * 0.0F, f19 * 0.004F, f19 * 0.0F);
                        }
                        GlStateManager.translatef(0.0F, 0.0F, f11 * 0.2F);
                        GlStateManager.scalef(1.0F, 1.0F, 1.0F + f11 * 0.2F);
                        GlStateManager.rotatef(sideOffset * 45.0F, 0.0F, -1.0F, 0.0F);
                }
            }
            else if (player.isSpinAttacking()) {
                transformSideFirstPerson(handSide, equippedProgress);
                int j = rightSide ? 1 : -1;
                GlStateManager.translatef(j * -0.4F, 0.8F, 0.3F);
                GlStateManager.rotatef(j * 65.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(j * -85.0F, 0.0F, 0.0F, 1.0F);
            }
            else {
                float f5 = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
                float f6 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.TAU);
                float f10 = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
                int sideOffset = rightSide ? 1 : -1;
                GlStateManager.translatef(sideOffset * f5, f6, f10);
                transformSideFirstPerson(handSide, equippedProgress);
                transformFirstPerson(handSide, swingProgress);
                if (hand == Hand.MAIN_HAND) {
                    if (InputHooks.isMainhandLungeInProgress) {
                        transformLungeFirstPerson(partialTicks, hand, handSide, stack);
                    }
                    else if (InputHooks.isMainhandLunging) {
                        this.transformLungingFirstPerson(partialTicks, hand, stack);
                    }
                }
                else {
                    if (InputHooks.isOffhandLungeInProgress) {
                        transformLungeFirstPerson(partialTicks, hand, handSide.opposite(), stack);
                    }
                    else if (InputHooks.isOffhandLunging) {
                        this.transformLungingFirstPerson(partialTicks, hand, stack);
                    }
                }
            }
            this.renderItemSide(player,
                                stack,
                                rightSide ?
                                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                                ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !rightSide);
        }
        GlStateManager.popMatrix();
    }

    public void renderItemSide(LivingEntity entity, ItemStack stack, ItemCameraTransforms.TransformType transform, boolean leftHanded) {
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.pushMatrix();
            boolean translucent = this.itemRenderer.shouldRenderItemIn3D(stack) && block.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
            if (translucent) {
                GlStateManager.depthMask(false);
            }
            this.itemRenderer.renderItem(stack, entity, transform, leftHanded);
            if (translucent) {
                GlStateManager.depthMask(true);
            }
            GlStateManager.popMatrix();
        }
    }

    private void renderMapFirstPerson(float pitch, float equippedProgress, float swingProgress) {
        float sqrtSwingProg = MathHelper.sqrt(swingProgress);
        float f1 = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
        float f2 = -0.4F * MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        GlStateManager.translatef(0.0F, -f1 / 2.0F, f2);
        float f3 = getMapAngleFromPitch(pitch);
        GlStateManager.translatef(0.0F, 0.04F + equippedProgress * -1.2F + f3 * -0.5F, -0.72F);
        GlStateManager.rotatef(f3 * -85.0F, 1.0F, 0.0F, 0.0F);
        this.renderArms();
        float f4 = MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        GlStateManager.rotatef(f4 * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scalef(2.0F, 2.0F, 2.0F);
        this.renderMapFirstPerson(this.mainHandStack);
    }

    private void renderMapFirstPerson(ItemStack stack) {
        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scalef(0.38F, 0.38F, 0.38F);
        GlStateManager.disableLighting();
        this.mc.getTextureManager().bindTexture(EvolutionResources.RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.translatef(-0.5F, -0.5F, 0.0F);
        GlStateManager.scalef(0.007_812_5F, 0.007_812_5F, 0.007_812_5F);
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-7, 135, 0).tex(0, 1).endVertex();
        buffer.pos(135, 135, 0).tex(1, 1).endVertex();
        buffer.pos(135, -7, 0).tex(1, 0).endVertex();
        buffer.pos(-7, -7, 0).tex(0, 0).endVertex();
        tessellator.draw();
        MapData mapData = FilledMapItem.getMapData(stack, this.mc.world);
        if (mapData != null) {
            this.mc.gameRenderer.getMapItemRenderer().renderMap(mapData, false);
        }
        GlStateManager.enableLighting();
    }

    private void renderMapFirstPersonSide(float equippedProgress, HandSide hand, float swingProgress, ItemStack stack) {
        float sideOffset = hand == HandSide.RIGHT ? 1.0F : -1.0F;
        GlStateManager.translatef(sideOffset * 0.125F, -0.125F, 0.0F);
        if (!this.mc.player.isInvisible()) {
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(sideOffset * 10.0F, 0.0F, 0.0F, 1.0F);
            this.renderArmFirstPerson(equippedProgress, swingProgress, hand);
            GlStateManager.popMatrix();
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef(sideOffset * 0.51F, -0.08F + equippedProgress * -1.2F, -0.75F);
        float sqrtSwingProg = MathHelper.sqrt(swingProgress);
        float f2 = MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * MathHelper.sin(sqrtSwingProg * MathHelper.TAU);
        float f5 = -0.3F * MathHelper.sin(swingProgress * MathHelper.PI);
        GlStateManager.translatef(sideOffset * f3, f4 - 0.3F * f2, f5);
        GlStateManager.rotatef(f2 * -45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * f2 * -30.0F, 0.0F, 1.0F, 0.0F);
        this.renderMapFirstPerson(stack);
        GlStateManager.popMatrix();
    }

    public void renderOutlines(VoxelShape shape, ActiveRenderInfo info, BlockPos pos) {
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

    public void renderPotionIcons(float partialTicks) {
        PotionSpriteUploader potionSpriteUploader = this.mc.getPotionSpriteUploader();
        while (!ClientEvents.EFFECTS_TO_ADD.isEmpty()) {
            EffectInstance addingInstance = ClientEvents.EFFECTS_TO_ADD.get(0);
            Effect addingEffect = addingInstance.getPotion();
            if (!addingEffect.shouldRenderHUD(addingInstance)) {
                ClientEvents.EFFECTS_TO_ADD.remove(addingInstance);
                ClientEvents.EFFECTS.add(addingInstance);
                this.client.effectToAddTicks = 0;
                continue;
            }
            float alpha;
            int x0 = (this.mc.mainWindow.getScaledWidth() - 24) / 2;
            int y0 = (this.mc.mainWindow.getScaledHeight() - 24) / 3;
            int x = x0;
            int y = y0;
            if (this.client.effectToAddTicks < 5) {
                alpha = this.client.effectToAddTicks / 5.0f;
            }
            else if (this.client.effectToAddTicks < 15) {
                alpha = 1.0f;
            }
            else {
                alpha = 1.0f;
                int x1 = this.mc.mainWindow.getScaledWidth() - 25;
                float t = (this.client.effectToAddTicks - 15 + partialTicks) / 6.0f;
                x = (int) ((x1 - x0) * t + x0);
                int y1 = addingEffect.isBeneficial() ? 1 : 26;
                y = (int) ((y1 - y0) * t + y0);
            }
            this.mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
            if (addingInstance.isAmbient()) {
                blit(x, y, 165, 166, 24, 24, -90);
            }
            else {
                blit(x, y, 141, 166, 24, 24, -90);
            }
            TextureAtlasSprite potionSprites = potionSpriteUploader.getSprite(addingEffect);
            this.mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_EFFECTS_TEXTURE);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, alpha);
            AbstractGui.blit(x + 3, y + 3, 1, 18, 18, potionSprites);
            this.client.shouldPassEffectTick = true;
            if (this.client.effectToAddTicks == 20) {
                this.client.effectToAddTicks = 0;
                ClientEvents.EFFECTS_TO_ADD.remove(addingInstance);
                ClientEvents.EFFECTS.add(addingInstance);
            }
            break;
        }
        if (!ClientEvents.EFFECTS.isEmpty()) {
            GlStateManager.enableBlend();
            int beneficalCount = 0;
            int harmfulCount = 0;
            for (EffectInstance effectInstance : Ordering.natural().reverse().sortedCopy(ClientEvents.EFFECTS)) {
                Effect effect = effectInstance.getPotion();
                if (!effect.shouldRenderHUD(effectInstance)) {
                    continue;
                }
                if (effectInstance.isShowIcon()) {
                    int x = this.mc.mainWindow.getScaledWidth();
                    int y = 1;
                    if (this.mc.isDemo()) {
                        y += 15;
                    }
                    if (effect.isBeneficial()) {
                        beneficalCount++;
                        x -= 25 * beneficalCount;
                    }
                    else {
                        harmfulCount++;
                        x -= 25 * harmfulCount;
                        y += 26;
                    }
                    this.mc.getTextureManager().bindTexture(ContainerScreen.INVENTORY_BACKGROUND);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    float alpha = 1.0F;
                    if (effectInstance.isAmbient()) {
                        blit(x, y, 165, 166, 24, 24, -90);
                    }
                    else {
                        blit(x, y, 141, 166, 24, 24, -90);
                        if (effectInstance.getDuration() <= 200) {
                            int remainingSeconds = 10 - effectInstance.getDuration() / 20;
                            alpha = MathHelper.clamp(effectInstance.getDuration() / 100.0F, 0.0F, 0.5F) +
                                    MathHelper.cos(effectInstance.getDuration() * MathHelper.PI / 5.0F) *
                                    MathHelper.clamp(remainingSeconds / 40.0F, 0.0F, 0.25F);
                        }
                    }
                    int finalX = x;
                    int finalY = y;
                    float finalAlpha = alpha;
                    TextureAtlasSprite potionSprites = potionSpriteUploader.getSprite(effect);
                    this.mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_EFFECTS_TEXTURE);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, finalAlpha);
                    AbstractGui.blit(finalX + 3, finalY + 3, -90, 18, 18, potionSprites);
                }
            }
        }
    }

    public void renderTooltip(RenderTooltipEvent.PostText event) {
        ItemStack stack = event.getStack();
        Item item = stack.getItem();
        if (stack.isFood()) {
            Food food = item.getFood();
            if (food != null) {
                GlStateManager.pushMatrix();
                GlStateManager.color3f(1.0F, 1.0F, 1.0F);
                Minecraft mc = Minecraft.getInstance();
                mc.getTextureManager().bindTexture(EvolutionResources.GUI_ICONS);
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
            mc.getTextureManager().bindTexture(EvolutionResources.GUI_ICONS);
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
            //Lunge line
            if (item instanceof ILunge) {
                if (hasAddedLine) {
                    line++;
                }
                else {
                    line += 2;
                }
                hasAddedLine = true;
            }
            //Parry line
            if (item instanceof IParry) {
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
            if (item instanceof ItemGenericTool && ((ItemGenericTool) item).getEfficiency() > 0) {
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

    public void resetEquipProgress(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            this.mainhandEquipProgress = 0;
        }
        else {
            this.offhandEquipProgress = 0;
        }
    }

    public void resetFullEquipProgress(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            this.mainhandEquipProgress = 0;
            this.mainhandPrevEquipProgress = 0;
        }
        else {
            this.offhandEquipProgress = 0;
            this.offhandPrevEquipProgress = 0;
        }
    }

    private void rotateArm(float partialTicks) {
        ClientPlayerEntity player = this.mc.player;
        float interpArmPitch = MathHelper.lerp(partialTicks, player.prevRenderArmPitch, player.renderArmPitch);
        float interpArmYaw = MathHelper.lerp(partialTicks, player.prevRenderArmYaw, player.renderArmYaw);
        GlStateManager.rotatef((player.getPitch(partialTicks) - interpArmPitch) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef((player.getYaw(partialTicks) - interpArmYaw) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private void setLightmap() {
        AbstractClientPlayerEntity player = this.mc.player;
        int light = this.mc.world.getCombinedLight(new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ), 0);
        float f = light & '\uffff';
        float f1 = light >> 16;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, f, f1);
    }

    public void swingArm(Hand hand) {
        if (hand == Hand.OFF_HAND) {
            if (!this.offhandIsSwingInProgress ||
                this.offhandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2 ||
                this.offhandSwingProgressInt < 0) {
                this.offhandSwingProgressInt = -1;
                this.offhandIsSwingInProgress = true;
            }
        }
        else {
            if (!this.mainhandIsSwingInProgress ||
                this.mainhandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2 ||
                this.mainhandSwingProgressInt < 0) {
                this.mainhandSwingProgressInt = -1;
                this.mainhandIsSwingInProgress = true;
            }
        }
    }

    public void tick() {
        this.mainhandPrevEquipProgress = this.mainhandEquipProgress;
        this.offhandPrevEquipProgress = this.offhandEquipProgress;
        this.mainhandPrevSwingProgress = this.mainhandSwingProgress;
        this.offhandPrevSwingProgress = this.offhandSwingProgress;
        this.updateArmSwingProgress();
        ClientPlayerEntity player = this.mc.player;
        ItemStack mainhandStack = player.getHeldItemMainhand();
        ItemStack offhandStack = player.getHeldItemOffhand();
        if (player.isRowingBoat()) {
            this.mainhandEquipProgress = MathHelper.clamp(this.mainhandEquipProgress - 0.4F, 0.0F, 1.0F);
            this.offhandEquipProgress = MathHelper.clamp(this.offhandEquipProgress - 0.4F, 0.0F, 1.0F);
        }
        else {
            boolean requipM = shouldCauseReequipAnimation(this.mainHandStack, mainhandStack, player.inventory.currentItem);
            boolean requipO = shouldCauseReequipAnimation(this.offhandStack, offhandStack, -1);
            if (requipM) {
                this.client.mainhandTimeSinceLastHit = 0;
            }
            else {
                this.mainHandStack = mainhandStack;
                this.client.mainhandTimeSinceLastHit++;
            }
            if (requipO) {
                this.client.offhandTimeSinceLastHit = 0;
            }
            else {
                this.offhandStack = offhandStack;
                this.client.offhandTimeSinceLastHit++;
            }
            float cooledAttackStrength = this.client.getOffhandCooledAttackStrength(this.mc.player.getHeldItemOffhand().getItem(), 1.0F);
            this.offhandEquipProgress += MathHelper.clamp((!requipO ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) -
                                                          this.offhandEquipProgress, -0.4f, 0.4F);
            cooledAttackStrength = this.client.getMainhandCooledAttackStrength(1.0F);
            this.mainhandEquipProgress += MathHelper.clamp((!requipM ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) -
                                                           this.mainhandEquipProgress, -0.4F, 0.4F);

        }
        if (this.mainhandEquipProgress < 0.1F && !InputHooks.isMainhandLunging) {
            this.mainHandStack = mainhandStack;
        }
        if (this.offhandEquipProgress < 0.1F && !InputHooks.isOffhandLunging) {
            this.offhandStack = offhandStack;
        }
        if (InputHooks.isMainhandLunging) {
            ClientEvents.LEFT_COUNTER_FIELD.set(this.mc, 1);
            this.client.performLungeMovement();
            this.mainhandLungingTicks++;
            if (this.mainhandLungingTicks == 4) {
                this.mainhandLungingTicks = 0;
                InputHooks.isMainhandLunging = false;
                this.client.performMainhandLunge(InputHooks.mainhandLungingStack, InputHooks.lastMainhandLungeStrength);
            }
        }
        else {
            this.mainhandLungingTicks = 0;
        }
        if (InputHooks.isOffhandLunging) {
            this.client.performLungeMovement();
            this.offhandLungingTicks++;
            if (this.offhandLungingTicks == 4) {
                this.offhandLungingTicks = 0;
                InputHooks.isOffhandLunging = false;
                this.client.performOffhandLunge(InputHooks.offhandLungingStack, InputHooks.lastOffhandLungeStrength);
            }
        }
        else {
            this.offhandLungingTicks = 0;
        }
    }

    private void transformEatFirstPerson(float partialTicks, HandSide hand, ItemStack stack) {
        float useTime = this.mc.player.getItemInUseCount() - partialTicks + 1.0F;
        float relativeUse = useTime / stack.getUseDuration();
        if (relativeUse < 0.8F) {
            float f2 = Math.abs(MathHelper.cos(useTime / 4.0F * MathHelper.PI) * 0.1F);
            GlStateManager.translatef(0.0F, f2, 0.0F);
        }
        float f3 = 1.0F - (float) Math.pow(relativeUse, 27);
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        GlStateManager.translatef(f3 * 0.6F * sideOffset, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotatef(sideOffset * f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(sideOffset * f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    private void transformLungingFirstPerson(float partialTicks, Hand hand, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ILunge) {
            if (hand == Hand.MAIN_HAND) {
                float lungeTime = this.mainhandLungingTicks + partialTicks;
                float relativeLunge = MathHelper.relativize(lungeTime, 0.0f, 5.0f);
                GlStateManager.rotatef(-5.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.translatef(0.0f, 0.85f * relativeLunge - 0.6f, 0.0f);
            }
            else {
                float lungeTime = this.offhandLungingTicks + partialTicks;
                float relativeLunge = MathHelper.relativize(lungeTime, 0.0f, 5.0f);
                GlStateManager.rotatef(5.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.translatef(0.0f, 0.85f * relativeLunge - 0.6f, 0.0f);
            }
        }
    }

    private void updateArmSwingProgress() {
        int i = this.getArmSwingAnimationEnd();
        if (this.offhandIsSwingInProgress) {
            ++this.offhandSwingProgressInt;
            if (this.offhandSwingProgressInt >= i) {
                this.offhandSwingProgressInt = 0;
                this.offhandIsSwingInProgress = false;
            }
        }
        else {
            this.offhandSwingProgressInt = 0;
        }
        this.offhandSwingProgress = this.offhandSwingProgressInt / (float) i;
        if (this.mainhandIsSwingInProgress) {
            ++this.mainhandSwingProgressInt;
            if (this.mainhandSwingProgressInt >= i) {
                this.mainhandSwingProgressInt = 0;
                this.mainhandIsSwingInProgress = false;
            }
        }
        else {
            this.mainhandSwingProgressInt = 0;
        }
        this.mainhandSwingProgress = this.mainhandSwingProgressInt / (float) i;
    }
}
