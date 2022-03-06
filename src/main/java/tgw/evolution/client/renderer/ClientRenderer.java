package tgw.evolution.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import tgw.evolution.capabilities.food.HungerStats;
import tgw.evolution.capabilities.food.IHunger;
import tgw.evolution.capabilities.temperature.TemperatureClient;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.gui.ScreenDisplayEffects;
import tgw.evolution.client.util.Blending;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.InputHooks;
import tgw.evolution.init.*;
import tgw.evolution.items.*;
import tgw.evolution.network.PacketCSPlaySoundEntityEmitted;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.Matrix3d;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientRenderer {

    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(
            new ResourceLocation("textures/map/map_background_checkerboard" + ".png"));
    public static ClientRenderer instance;
    private static int slotMainHand;
    private final ClientEvents client;
    private final List<ClientEffectInstance> effects = new ArrayList<>();
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final byte[] hungerShakeAligment = new byte[10];
    private final ItemRenderer itemRenderer;
    private final Minecraft mc;
    private final Random rand = new Random();
    private final List<Runnable> runnables = new ArrayList<>();
    private final byte[] thirstShakeAligment = new byte[10];
    public boolean isAddingEffect;
    public boolean isRenderingPlayer;
    private ItemStack currentMainhandItem = ItemStack.EMPTY;
    private ItemStack currentOffhandItem = ItemStack.EMPTY;
    private long healthUpdateCounter;
    private int hitmarkerTick;
    private byte hungerAlphaDir = 1;
    private float hungerFlashAlpha;
    private int killmarkerTick;
    private int lastBeneficalCount;
    private int lastNeutralCount;
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
    private int movingFinalCount;
    private float offhandEquipProgress;
    private boolean offhandIsSwingInProgress;
    private int offhandLungingTicks;
    private float offhandPrevEquipProgress;
    private float offhandPrevSwingProgress;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private float offhandSwingProgress;
    private int offhandSwingProgressInt;
    private float playerDisplayedHealth;
    private byte thirstAlphaDir = 1;
    private float thirstFlashAlpha;

    public ClientRenderer(Minecraft mc, ClientEvents client) {
        instance = this;
        this.mc = mc;
        this.client = client;
        this.entityRenderDispatcher = mc.getEntityRenderDispatcher();
        this.itemRenderer = mc.getItemRenderer();
        EvolutionOverlays.init();
    }

    private static void blit(PoseStack matrices, int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        GuiComponent.blit(matrices, x, y, 20, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    public static void disableAlpha(float alpha) {
        RenderSystem.disableBlend();
        if (alpha == 1.0f) {
            return;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawHitbox(PoseStack matrices,
                                  VertexConsumer buffer,
                                  Hitbox hitbox,
                                  double x,
                                  double y,
                                  double z,
                                  float red,
                                  float green,
                                  float blue,
                                  float alpha,
                                  Entity entity,
                                  float partialTicks) {
        boolean renderAll = Minecraft.getInstance().player.getMainHandItem().getItem() == EvolutionItems.DEBUG_ITEM.get() ||
                            Minecraft.getInstance().player.getOffhandItem().getItem() == EvolutionItems.DEBUG_ITEM.get();
        hitbox.getParent().init(entity, partialTicks);
        Matrix3d mainTransform = hitbox.getParent().getTransform().transpose();
        final double[] points0 = new double[3];
        final double[] points1 = new double[3];
        Matrix4f pose = matrices.last().pose();
        Matrix3f normal = matrices.last().normal();
        for (Hitbox box : hitbox.getParent().getBoxes()) {
            if (!renderAll) {
                box = hitbox;
            }
            Hitbox finalBox = box;
            Matrix3d transform = finalBox.getTransformation().transpose();
            //noinspection ObjectAllocationInLoop
            finalBox.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
                //Transform 0 coordinates
                transform.transform(x0, y0, z0, points0);
                finalBox.doOffset(points0);
                mainTransform.transform(points0[0], points0[1], points0[2], points0);
                finalBox.getParent().doOffset(points0);
                //Transform 1 coordinates
                transform.transform(x1, y1, z1, points1);
                finalBox.doOffset(points1);
                mainTransform.transform(points1[0], points1[1], points1[2], points1);
                finalBox.getParent().doOffset(points1);
                //Calculate normals
                float nx = (float) (points1[0] - points0[0]);
                float ny = (float) (points1[1] - points0[1]);
                float nz = (float) (points1[2] - points0[2]);
                float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                nx /= length;
                ny /= length;
                nz /= length;
                //Fill vertices
                buffer.vertex(pose, (float) (points0[0] + x), (float) (points0[1] + y), (float) (points0[2] + z))
                      .color(red, green, blue, alpha)
                      .normal(normal, nx, ny, nz)
                      .endVertex();
                buffer.vertex(pose, (float) (points1[0] + x), (float) (points1[1] + y), (float) (points1[2] + z))
                      .color(red, green, blue, alpha)
                      .normal(normal, nx, ny, nz)
                      .endVertex();
            });
            if (!renderAll) {
                break;
            }
        }
        if (renderAll) {
            for (Hitbox box : hitbox.getParent().getEquipment()) {
                Matrix3d transform = box.getTransformation().transpose();
                //noinspection ObjectAllocationInLoop
                box.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
                    //Transform 0 coordinates
                    transform.transform(x0, y0, z0, points0);
                    box.doOffset(points0);
                    mainTransform.transform(points0[0], points0[1], points0[2], points0);
                    box.getParent().doOffset(points0);
                    //Transform 1 coordinates
                    transform.transform(x1, y1, z1, points1);
                    box.doOffset(points1);
                    mainTransform.transform(points1[0], points1[1], points1[2], points1);
                    box.getParent().doOffset(points1);
                    //Calculate normals
                    float nx = (float) (points1[0] - points0[0]);
                    float ny = (float) (points1[1] - points0[1]);
                    float nz = (float) (points1[2] - points0[2]);
                    float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                    nx /= length;
                    ny /= length;
                    nz /= length;
                    //Fill vertices
                    buffer.vertex(pose, (float) (points0[0] + x), (float) (points0[1] + y), (float) (points0[2] + z))
                          .color(1.0f, 0.0f, 1.0f, 1.0f)
                          .normal(normal, nx, ny, nz)
                          .endVertex();
                    buffer.vertex(pose, (float) (points1[0] + x), (float) (points1[1] + y), (float) (points1[2] + z))
                          .color(1.0f, 0.0f, 1.0f, 1.0f)
                          .normal(normal, nx, ny, nz)
                          .endVertex();
                });
            }
        }
    }

    private static void drawSelectionBox(PoseStack matrices,
                                         VertexConsumer buffer,
                                         Entity entity,
                                         double x,
                                         double y,
                                         double z,
                                         BlockPos pos,
                                         BlockState state) {
        drawShape(matrices, buffer, state.getShape(entity.level, pos, CollisionContext.of(entity)), pos.getX() - x, pos.getY() - y, pos.getZ() - z,
                  0.0F, 0.0F, 0.0F, 0.4F);
    }

    private static void drawShape(PoseStack matrices,
                                  VertexConsumer buffer,
                                  VoxelShape shape,
                                  double x,
                                  double y,
                                  double z,
                                  float red,
                                  float green,
                                  float blue,
                                  float alpha) {
        Matrix4f mat = matrices.last().pose();
        Matrix3f normal = matrices.last().normal();
        shape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            float nx = (float) (x1 - x0);
            float ny = (float) (y1 - y0);
            float nz = (float) (z1 - z0);
            float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= length;
            ny /= length;
            nz /= length;
            buffer.vertex(mat, (float) (x0 + x), (float) (y0 + y), (float) (z0 + z))
                  .color(red, green, blue, alpha)
                  .normal(normal, nx, ny, nz)
                  .endVertex();
            buffer.vertex(mat, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z))
                  .color(red, green, blue, alpha)
                  .normal(normal, nx, ny, nz)
                  .endVertex();
        });
    }

    public static void enableAlpha(float alpha) {
        RenderSystem.enableBlend();
        if (alpha == 1.0f) {
            return;
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void floatBlit(PoseStack matrices, float x, float y, int textureX, int textureY, int sizeX, int sizeY, int blitOffset) {
        GUIUtils.floatBlit(matrices, x, y, blitOffset, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    public static void floatBlit(PoseStack matrices, float x, float y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        GUIUtils.innerFloatBlit(matrices.last().pose(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(),
                                sprite.getV1());
    }

    private static float getMapAngleFromPitch(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * MathHelper.PI) * 0.5F + 0.5F;
        return f;
    }

    private static float roundToHearts(float currentHealth) {
        return Mth.ceil(currentHealth * 0.4F) / 0.4F;
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

    private static boolean shouldShowAttackIndicator(Entity entity) {
        return entity.isAttackable() && entity.isAlive();
    }

    private static void transformFirstPerson(PoseStack matrices, HumanoidArm hand, float swingProgress) {
        int sideOffset = hand == HumanoidArm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * MathHelper.PI);
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * (45.0F + f * -20.0F)));
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * f1 * -20.0F));
        matrices.mulPose(Vector3f.XP.rotationDegrees(f1 * -80.0F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * -45.0F));
    }

    private static void transformLungeFirstPerson(PoseStack matrices,
                                                  float partialTicks,
                                                  InteractionHand hand,
                                                  HumanoidArm handSide,
                                                  ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ILunge) {
            float lungeTime = InputHooks.getLungeTime(hand) + partialTicks;
            int minLunge = ((ILunge) item).getMinLungeTime();
            int fullLunge = ((ILunge) item).getFullLungeTime();
            float relativeLunge = MathHelper.relativize(lungeTime, minLunge, fullLunge);
            matrices.mulPose(Vector3f.YP.rotationDegrees(handSide == HumanoidArm.RIGHT ? -5.0F : 5.0F));
            float relativeRotation = MathHelper.clamp(relativeLunge * 3.0f, 0.0f, 1.0f);
            matrices.mulPose(Vector3f.XP.rotationDegrees(-100.0F * relativeRotation));
            if (relativeLunge > 0.333f) {
                float relativeTranslation = MathHelper.relativize(relativeLunge, 0.333f, 1.0f);
                matrices.translate(0, -0.6 * relativeTranslation, 0);
            }
        }
    }

    private static void transformSideFirstPerson(PoseStack matrices, HumanoidArm arm, float equippedProg) {
        int sideOffset = arm == HumanoidArm.RIGHT ? 1 : -1;
        matrices.translate(sideOffset * 0.56, -0.52 + equippedProg * -0.6, -0.72);
    }

    private static void transformSideFirstPersonParry(PoseStack matrices, HumanoidArm arm, float equippedProg) {
        int sideOffset = arm == HumanoidArm.RIGHT ? 1 : -1;
        matrices.translate(sideOffset * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        matrices.translate(sideOffset * -0.141_421_36, 0.08, 0.141_421_36);
        matrices.mulPose(Vector3f.XP.rotationDegrees(-102.25F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 13.365F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * 78.05F));
    }

    private static void transformSwordFirstPerson(PoseStack matrices,
                                                  Player player,
                                                  float partialTicks,
                                                  InteractionHand hand,
                                                  HumanoidArm arm,
                                                  ItemStack stack) {
        float progress = ((ILivingEntityPatch) player).getMainhandCustomAttackProgress(partialTicks);
        matrices.mulPose(Vector3f.XP.rotationDegrees(-90));
        matrices.mulPose(Vector3f.YP.rotationDegrees(-90));
        matrices.translate(progress / 2, 0, progress);
    }

    private void drawHungerOverlay(PoseStack matrices, int hungerGained, int hungerLevel, int left, int top, float alpha) {
        if (hungerGained == 0) {
            return;
        }
        int endBar = Mth.ceil(Math.min(hungerLevel + hungerGained, HungerStats.HUNGER_CAPACITY) / HungerStats.HUNGER_SCALE);
        enableAlpha(alpha);
        int startBar = hungerLevel / (int) HungerStats.HUNGER_SCALE;
        int barsNeeded = endBar - startBar;
        for (int i = startBar; i < startBar + barsNeeded; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top + this.hungerShakeAligment[i];
            int icon = 16;
            if (this.mc.player.hasEffect(MobEffects.HUNGER)) {
                icon += 36;
            }
            if (idx < hungerLevel + hungerGained) {
                blit(matrices, x, y, icon + 36, 27, 9, 9);
            }
            else if (idx == hungerLevel + hungerGained) {
                blit(matrices, x, y, icon + 45, 27, 9, 9);
            }
        }
        disableAlpha(alpha);
    }

    public void drawHydrationOverlay(PoseStack matrices, int hydrationGained, int hydrationLevel, int left, int top, float alpha) {
        for (int j = 0; j < 3; j++) {
            if (hydrationLevel + hydrationGained <= 0) {
                return;
            }
            int startBar = hydrationGained != 0 ? hydrationLevel / (int) ThirstStats.HYDRATION_SCALE : 0;
            int endBar = Mth.ceil(Math.min(hydrationLevel + hydrationGained, ThirstStats.INTOXICATION) / ThirstStats.HYDRATION_SCALE);
            enableAlpha(alpha);
            int barsNeeded = endBar - startBar;
            for (int i = startBar; i < startBar + barsNeeded; i++) {
                int x = left - i * 8 - 9;
                int y = top + this.thirstShakeAligment[i];
                float effectiveSaturationOfBar = (hydrationLevel + hydrationGained) / ThirstStats.HYDRATION_SCALE - i;
                if (effectiveSaturationOfBar > 0.75) {
                    blit(matrices, x, y, 169 + 36 * j, 54, 9, 9);
                }
                else if (effectiveSaturationOfBar > 0.5) {
                    blit(matrices, x, y, 160 + 36 * j, 54, 9, 9);
                }
                else if (effectiveSaturationOfBar > 0.25) {
                    blit(matrices, x, y, 151 + 36 * j, 54, 9, 9);
                }
                else if (effectiveSaturationOfBar > 0) {
                    blit(matrices, x, y, 142 + 36 * j, 54, 9, 9);
                }
            }
            if (hydrationLevel >= 1_000) {
                hydrationLevel -= 1_000;
            }
            else {
                hydrationGained -= 1_000 - hydrationLevel;
                if (hydrationGained < 0) {
                    hydrationGained = 0;
                }
                hydrationLevel = 0;
            }
            disableAlpha(alpha);
        }
    }

    private void drawSaturationOverlay(PoseStack matrices, int saturationGained, int saturationLevel, int left, int top, float alpha) {
        for (int j = 0; j < 3; j++) {
            if (saturationLevel + saturationGained <= 0) {
                return;
            }
            int startBar = saturationGained != 0 ? saturationLevel / (int) HungerStats.SATURATION_SCALE : 0;
            int endBar = Mth.ceil(Math.min(saturationLevel + saturationGained, HungerStats.OVEREAT) / HungerStats.SATURATION_SCALE);
            enableAlpha(alpha);
            int barsNeeded = endBar - startBar;
            for (int i = startBar; i < startBar + barsNeeded; i++) {
                int x = left - i * 8 - 9;
                int y = top + this.hungerShakeAligment[i];
                float effectiveSaturationOfBar = (saturationLevel + saturationGained) / HungerStats.SATURATION_SCALE - i;
                if (effectiveSaturationOfBar > 0.75) {
                    blit(matrices, x, y, 169 + 36 * j, 27, 9, 9);
                }
                else if (effectiveSaturationOfBar > 0.5) {
                    blit(matrices, x, y, 160 + 36 * j, 27, 9, 9);
                }
                else if (effectiveSaturationOfBar > 0.25) {
                    blit(matrices, x, y, 151 + 36 * j, 27, 9, 9);
                }
                else if (effectiveSaturationOfBar > 0) {
                    blit(matrices, x, y, 142 + 36 * j, 27, 9, 9);
                }
            }
            if (saturationLevel >= 1_000) {
                saturationLevel -= 1_000;
            }
            else {
                saturationGained -= 1_000 - saturationLevel;
                if (saturationGained < 0) {
                    saturationGained = 0;
                }
                saturationLevel = 0;
            }
            disableAlpha(alpha);
        }
    }

    public void drawThirstOverlay(PoseStack matrices, int thirstRestored, int thirstLevel, int left, int top, float alpha) {
        if (thirstRestored == 0) {
            return;
        }
        int endBar = Mth.ceil(Math.min(thirstLevel + thirstRestored, ThirstStats.THIRST_CAPACITY) / ThirstStats.THIRST_SCALE);
        enableAlpha(alpha);
        int startBar = thirstLevel / (int) ThirstStats.THIRST_SCALE;
        int barsNeeded = endBar - startBar;
        for (int i = startBar; i < startBar + barsNeeded; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top + this.thirstShakeAligment[i];
            int icon = 16;
            if (this.mc.player.hasEffect(EvolutionEffects.THIRST.get())) {
                icon += 36;
            }
            if (idx < thirstLevel + thirstRestored) {
                blit(matrices, x, y, icon + 36, 54, 9, 9);
            }
            else if (idx == thirstLevel + thirstRestored) {
                blit(matrices, x, y, icon + 45, 54, 9, 9);
            }
        }
        disableAlpha(alpha);
    }

    public void endTick() {
        this.hungerFlashAlpha += this.hungerAlphaDir * 0.125f;
        this.thirstFlashAlpha += this.thirstAlphaDir * 0.125f;
        if (this.hungerFlashAlpha >= 1.5f) {
            this.hungerFlashAlpha = 1.0f;
            this.hungerAlphaDir = -1;
        }
        else if (this.hungerFlashAlpha <= -0.5f) {
            this.hungerFlashAlpha = 0.0f;
            this.hungerAlphaDir = 1;
        }
        if (this.thirstFlashAlpha >= 1.5f) {
            this.thirstFlashAlpha = 1.0f;
            this.thirstAlphaDir = -1;
        }
        else if (this.thirstFlashAlpha <= -0.5f) {
            this.thirstFlashAlpha = 0.0f;
            this.thirstAlphaDir = 1;
        }
        if (this.hitmarkerTick > 0) {
            this.hitmarkerTick--;
        }
        if (this.killmarkerTick > 0) {
            this.killmarkerTick--;
        }
    }

    private int getArmSwingAnimationEnd() {
        if (MobEffectUtil.hasDigSpeed(this.mc.player)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this.mc.player));
        }
        return this.mc.player.hasEffect(MobEffects.DIG_SLOWDOWN) ? 6 + (1 + this.mc.player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
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

    private float getYPosForEffect(MobEffect effect) {
        float y = 1;
        if (this.mc.isDemo()) {
            y += 15;
        }
        switch (effect.getCategory()) {
            case HARMFUL: {
                if (this.lastNeutralCount > 0) {
                    y += 26;
                }
            }
            case NEUTRAL: {
                if (this.lastBeneficalCount > 0) {
                    y += 26;
                }
            }
            case BENEFICIAL: {
                return y;
            }
        }
        return y;
    }

    private boolean rayTraceMouse(HitResult rayTraceResult) {
        if (rayTraceResult == null) {
            return false;
        }
        if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) rayTraceResult).getEntity() instanceof InventoryCarrier;
        }
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult) rayTraceResult).getBlockPos();
            Level level = this.mc.level;
            return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
        }
        return false;
    }

    private void renderArm(PoseStack matrices, MultiBufferSource buffer, int packedLight, HumanoidArm arm) {
        RenderSystem.setShaderTexture(0, this.mc.player.getSkinTextureLocation());
        EntityRenderer<? super LocalPlayer> renderer = this.entityRenderDispatcher.getRenderer(this.mc.player);
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        matrices.pushPose();
        float sideOffset = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        matrices.mulPose(Vector3f.YP.rotationDegrees(92.0f));
        matrices.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * -41.0F));
        matrices.translate(sideOffset * 0.3, -1.1, 0.45);
        if (arm == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(matrices, buffer, packedLight, this.mc.player);
        }
        else {
            playerRenderer.renderLeftHand(matrices, buffer, packedLight, this.mc.player);
        }
        matrices.popPose();
    }

    public void renderArmFirstPerson(PoseStack matrices,
                                     MultiBufferSource buffer,
                                     int packedLight,
                                     float equippedProgress,
                                     float swingProgress,
                                     HumanoidArm arm) {
        boolean right = arm == HumanoidArm.RIGHT;
        float sideOffset = right ? 1.0F : -1.0F;
        float sqrtSwingProgress = MathHelper.sqrt(swingProgress);
        float f2 = -0.3F * MathHelper.sin(sqrtSwingProgress * MathHelper.PI);
        float f3 = 0.4F * MathHelper.sin(sqrtSwingProgress * MathHelper.TAU);
        float f4 = -0.4F * MathHelper.sin(swingProgress * MathHelper.PI);
        matrices.translate(sideOffset * (f2 + 0.640_000_05), f3 - 0.6 + equippedProgress * -0.6, f4 - 0.719_999_97);
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 45.0F));
        float f5 = MathHelper.sin(swingProgress * swingProgress * MathHelper.PI);
        float f6 = MathHelper.sin(sqrtSwingProgress * MathHelper.PI);
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * f6 * 70.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * f5 * -20.0F));
        AbstractClientPlayer player = this.mc.player;
        RenderSystem.setShaderTexture(0, player.getSkinTextureLocation());
        matrices.translate(sideOffset * -1, 3.6, 3.5);
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * 120.0F));
        matrices.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * -135.0F));
        matrices.translate(sideOffset * 5.6, 0, 0);
        PlayerRenderer playerRenderer = (PlayerRenderer) this.entityRenderDispatcher.getRenderer(player);
        if (right) {
            playerRenderer.renderRightHand(matrices, buffer, packedLight, player);
        }
        else {
            playerRenderer.renderLeftHand(matrices, buffer, packedLight, player);
        }
    }

    public void renderBlockOutlines(PoseStack matrices, MultiBufferSource buffer, Camera camera, BlockPos hitPos) {
        BlockState state = this.mc.level.getBlockState(hitPos);
        if (!state.isAir() && this.mc.level.getWorldBorder().isWithinBounds(hitPos)) {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
            RenderSystem.disableTexture();
            matrices.pushPose();
            double projX = camera.getPosition().x;
            double projY = camera.getPosition().y;
            double projZ = camera.getPosition().z;
            drawSelectionBox(matrices, buffer.getBuffer(RenderType.lines()), camera.getEntity(), projX, projY, projZ, hitPos, state);
            matrices.popPose();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public void renderCrosshair(PoseStack matrices, float partialTicks, int width, int height) {
        Options options = this.mc.options;
        boolean offhandValid = this.mc.player.getOffhandItem().getItem() instanceof IOffhandAttackable;
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        if (options.getCameraType() == CameraType.FIRST_PERSON) {
            if (this.mc.gameMode.getPlayerMode() != GameType.SPECTATOR || this.rayTraceMouse(this.mc.hitResult)) {
                if (options.renderDebug && !options.hideGui && !this.mc.player.isReducedDebugInfo() && !options.reducedDebugInfo) {
                    PoseStack internalMat = RenderSystem.getModelViewStack();
                    internalMat.pushPose();
                    internalMat.translate(width / 2.0, height / 2.0, this.mc.gui.getBlitOffset());
                    Camera camera = this.mc.gameRenderer.getMainCamera();
                    IPoseStackPatch internalMatExt = MathHelper.getExtendedMatrix(internalMat);
                    internalMatExt.mulPoseX(-camera.getXRot());
                    internalMatExt.mulPoseY(camera.getYRot());
                    internalMat.scale(-1.0f, -1.0f, -1.0f);
                    RenderSystem.applyModelViewMatrix();
                    RenderSystem.renderCrosshair(10);
                    internalMat.popPose();
                    RenderSystem.applyModelViewMatrix();
                }
                else {
                    RenderSystem.enableBlend();
                    //enable alpha test
                    if (EvolutionConfig.CLIENT.hitmarkers.get()) {
                        if (this.killmarkerTick > 0) {
                            if (this.killmarkerTick > 10) {
                                blit(matrices, (width - 17) / 2, (height - 17) / 2, 102, 94, 17, 17);
                            }
                            else if (this.killmarkerTick > 5) {
                                blit(matrices, (width - 17) / 2, (height - 17) / 2, 120, 94, 17, 17);
                            }
                            else {
                                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (this.killmarkerTick - partialTicks) / 5);
                                blit(matrices, (width - 17) / 2, (height - 17) / 2, 120, 94, 17, 17);
                            }
                        }
                        else if (this.hitmarkerTick > 0) {
                            if (this.hitmarkerTick <= 5) {
                                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (this.hitmarkerTick - partialTicks) / 5);
                            }
                            blit(matrices, (width - 17) / 2, (height - 17) / 2, 84, 94, 17, 17);
                        }
                    }
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    Blending.INVERTED_ADD.apply();
                    RenderSystem.blendEquation(GL14.GL_FUNC_SUBTRACT);
                    blit(matrices, (width - 15) / 2, (height - 15) / 2, 0, 0, 15, 15);
                    if (this.mc.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float leftCooledAttackStrength = this.client.getMainhandCooledAttackStrength(partialTicks);
                        boolean shouldShowLeftAttackIndicator = false;
                        if (this.client.leftPointedEntity instanceof LivingEntity && leftCooledAttackStrength >= 1) {
                            shouldShowLeftAttackIndicator = shouldShowAttackIndicator(this.client.leftPointedEntity);
                            if (this.mc.hitResult.getType() == HitResult.Type.BLOCK) {
                                shouldShowLeftAttackIndicator = false;
                            }
                        }
                        int sideOffset = this.mc.player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
                        int x = width / 2 - 8;
                        x = offhandValid ? x + 10 * sideOffset : x;
                        int y = height / 2 - 7 + 16;
                        if (shouldShowLeftAttackIndicator) {
                            blit(matrices, x, y, 68, 94, 16, 16);
                        }
                        else if (leftCooledAttackStrength < 1.0F) {
                            int l = (int) (leftCooledAttackStrength * 17.0F);
                            blit(matrices, x, y, 36, 94, 16, 4);
                            blit(matrices, x, y, 52, 94, l, 4);
                        }
                        if (offhandValid) {
                            boolean shouldShowRightAttackIndicator = false;
                            float rightCooledAttackStrength = this.client.getOffhandCooledAttackStrength(this.mc.player.getOffhandItem(),
                                                                                                         partialTicks);
                            if (this.client.rightPointedEntity instanceof LivingEntity && rightCooledAttackStrength >= 1) {
                                shouldShowRightAttackIndicator = shouldShowAttackIndicator(this.client.rightPointedEntity);
                                if (this.mc.hitResult.getType() == HitResult.Type.BLOCK) {
                                    shouldShowRightAttackIndicator = false;
                                }
                            }
                            x -= 20 * sideOffset;
                            if (shouldShowRightAttackIndicator) {
                                blit(matrices, x, y, 68, 110, 16, 16);
                            }
                            else if (rightCooledAttackStrength < 1.0F) {
                                int l = (int) (rightCooledAttackStrength * 17.0F);
                                blit(matrices, x, y, 36, 110, 16, 4);
                                blit(matrices, x, y, 52, 110, l, 4);
                            }
                        }
                    }
                    //Disable alpha test
                    RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                }
            }
        }
    }

    public void renderFog(EntityViewRenderEvent.FogDensity event) {
        if (this.mc.player != null && this.mc.player.hasEffect(MobEffects.BLINDNESS)) {
            float f1 = 5.0F;
            int duration = this.mc.player.getEffect(MobEffects.BLINDNESS).getDuration();
            int amplifier = this.mc.player.getEffect(MobEffects.BLINDNESS).getAmplifier() + 1;
            if (duration < 20) {
                f1 = 5.0F + (this.mc.options.renderDistance * 16 - 5.0F) * (1.0F - duration / 20.0F);
            }
//            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            float multiplier = 0.25F / amplifier;
//            RenderSystem.fogStart(f1 * multiplier);
//            RenderSystem.fogEnd(f1 * multiplier * 4.0F);
            if (GL.getCapabilities().GL_NV_fog_distance) {
                GL11.glFogi(0x855a, 0x855b);
            }
            event.setDensity(2.0F);
            event.setCanceled(true);
        }
    }

    public void renderFoodAndThirst(PoseStack matrices, int width, int height) {
        this.mc.getProfiler().push("food");
        Player player = (Player) this.mc.getCameraEntity();
        RenderSystem.enableBlend();
        ForgeIngameGui gui = (ForgeIngameGui) this.mc.gui;
        int top = height - gui.right_height;
        gui.right_height += 10;
        IHunger hunger = HungerStats.CLIENT_INSTANCE;
        int level = Mth.ceil(hunger.getHungerLevel() / (HungerStats.HUNGER_SCALE / 2));
        int left = width / 2 + 91;
        for (int i = 0; i < 10; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int icon = 16;
            byte background = 0;
            if (this.mc.player.hasEffect(MobEffects.HUNGER)) {
                icon += 36;
                background = 13;
            }
            if (this.client.getTickCount() % Math.max(level * level, 1) == 0) {
                int shake = this.rand.nextInt(3) - 1;
                this.hungerShakeAligment[i] = (byte) shake;
            }
            else {
                this.hungerShakeAligment[i] = 0;
            }
            int y = top + this.hungerShakeAligment[i];
            blit(matrices, x, y, 16 + background * 9, 27, 9, 9);
            if (idx < level) {
                blit(matrices, x, y, icon + 36, 27, 9, 9);
            }
            else if (idx == level) {
                blit(matrices, x, y, icon + 45, 27, 9, 9);
            }
        }
        //Saturation
        ItemStack heldStack = player.getMainHandItem();
        if (!(heldStack.getItem() instanceof IFood)) {
            heldStack = player.getOffhandItem();
        }
        //Saturation overlay
        this.drawSaturationOverlay(matrices, 0, hunger.getSaturationLevel(), left, top, 1.0f);
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof IFood food)) {
            this.hungerFlashAlpha = 0;
            this.hungerAlphaDir = 1;
        }
        else {
            //Restored hunger/saturation overlay while holding food
            this.drawHungerOverlay(matrices, food.getHunger(), hunger.getHungerLevel(), left, top, this.hungerFlashAlpha);
            this.drawSaturationOverlay(matrices, food.getHunger(), hunger.getSaturationLevel(), left, top, this.hungerFlashAlpha);
        }
        this.mc.getProfiler().popPush("thirst");
        IThirst thirst = ThirstStats.CLIENT_INSTANCE;
        top = height - gui.right_height;
        level = Mth.ceil(thirst.getThirstLevel() / (ThirstStats.THIRST_SCALE / 2));
        left = width / 2 + 91;
        for (int i = 0; i < 10; i++) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int icon = 16;
            byte background = 0;
            if (this.mc.player.hasEffect(EvolutionEffects.THIRST.get())) {
                icon += 36;
                background = 13;
            }
            if (this.client.getTickCount() % Math.max(level * level, 1) == 0) {
                int shake = this.rand.nextInt(3) - 1;
                this.thirstShakeAligment[i] = (byte) shake;
            }
            else {
                this.thirstShakeAligment[i] = 0;
            }
            int y = top + this.thirstShakeAligment[i];
            blit(matrices, x, y, 16 + background * 9, 54, 9, 9);
            if (idx < level) {
                blit(matrices, x, y, icon + 36, 54, 9, 9);
            }
            else if (idx == level) {
                blit(matrices, x, y, icon + 45, 54, 9, 9);
            }
        }
        //Hydration
        heldStack = player.getMainHandItem();
        if (!(heldStack.getItem() instanceof IDrink)) {
            heldStack = player.getOffhandItem();
        }
        left = width / 2 + 91;
        top = height - gui.right_height;
        gui.right_height += 10;
        //Hydration overlay
        this.drawHydrationOverlay(matrices, 0, thirst.getHydrationLevel(), left, top, 1.0f);
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof IDrink drink)) {
            this.thirstFlashAlpha = 0;
            this.thirstAlphaDir = 1;
        }
        else {
            //Restored thirst/hydration overlay while holding drink
            this.drawThirstOverlay(matrices, drink.getThirst(), thirst.getThirstLevel(), left, top, this.thirstFlashAlpha);
            this.drawHydrationOverlay(matrices, drink.getThirst(), thirst.getHydrationLevel(), left, top, this.thirstFlashAlpha);
        }
        RenderSystem.disableBlend();
        this.mc.getProfiler().pop();
    }

    public void renderHealth(PoseStack matrices, int width, int height) {
        this.mc.getProfiler().push("health");
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        RenderSystem.enableBlend();
        Player player = (Player) this.mc.getCameraEntity();
        float currentHealth = player.getHealth();
        boolean updateHealth = false;
        //Take damage
        if (roundToHearts(currentHealth) - this.playerDisplayedHealth <= -2.5f) {
            this.lastSystemTime = Util.getMillis();
            this.healthUpdateCounter = this.client.getTickCount() + 20;
            updateHealth = true;
        }
        //Regen Health
        else if (roundToHearts(currentHealth) - this.playerDisplayedHealth >= 2.5f) {
            this.lastSystemTime = Util.getMillis();
            this.healthUpdateCounter = this.client.getTickCount() + 10;
            updateHealth = true;
        }
        //Update variables every 1s
        if (Util.getMillis() - this.lastSystemTime > 1_000L) {
            this.lastPlayerHealth = currentHealth;
            this.lastSystemTime = Util.getMillis();
        }
        if (updateHealth) {
            this.playerDisplayedHealth = roundToHearts(currentHealth);
        }
        float healthMax = (float) player.getAttribute(Attributes.MAX_HEALTH).getValue();
        //The health bar flashes
        boolean highlight = this.healthUpdateCounter > this.client.getTickCount() &&
                            (this.healthUpdateCounter - this.client.getTickCount()) / 3L % 2L != 0L;
        float healthLast = this.lastPlayerHealth;
        float absorb = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((healthMax + absorb) / 100.0F);
        ForgeIngameGui gui = (ForgeIngameGui) this.mc.gui;
        int top = height - gui.left_height;
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        gui.left_height += healthRows * rowHeight;
        if (rowHeight != 10) {
            gui.left_height += 10 - rowHeight;
        }
        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regen = this.client.getTickCount() % 25;
        }
        boolean hardcore = this.mc.level.getLevelData().isHardcore();
        final int heartTextureYPos = hardcore ? 45 : 0;
        final int absorbHeartTextureYPos = hardcore ? 36 : 9;
        final int heartBackgroundXPos = highlight ? 25 : 16;
        int margin = 16;
        if (player.hasEffect(MobEffects.POISON)) {
            margin += 72;
        }
        //TODO anaemia
        int absorbRemaining = Mth.ceil(absorb);
        this.rand.setSeed(312_871L * this.client.getTickCount());
        int left = width / 2 - 91;
        for (int currentHeart = Mth.ceil((healthMax + absorb) / 10.0F) - 1; currentHeart >= 0; --currentHeart) {
            int row = Mth.ceil((currentHeart + 1) / 10.0F) - 1;
            int x = left + currentHeart % 10 * 8;
            int y = top - row * rowHeight;
            //Shake hearts if 20% HP
            if (currentHealth <= 20.0F) {
                y += this.rand.nextInt(2);
            }
            if (currentHeart == regen) {
                y -= 2;
            }
            blit(matrices, x, y, heartBackgroundXPos, heartTextureYPos, 9, 9);
            if (highlight) {
                if (currentHeart * 10.0F + 7.5F < healthLast) {
                    //Faded full heart
                    blit(matrices, x, y, margin + 54, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 5.0F < healthLast) {
                    //Faded 3/4 heart
                    blit(matrices, x, y, margin + 63, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 2.5F < healthLast) {
                    //Faded half heart
                    blit(matrices, x, y, margin + 72, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F < healthLast) {
                    //Faded 1/4 heart
                    blit(matrices, x, y, margin + 81, heartTextureYPos, 9, 9);
                }
            }
            if (absorbRemaining > 0) {
                int absorbHeart = absorbRemaining % 10;
                switch (absorbHeart) {
                    case 1, 2 -> blit(matrices, x, y, 169, absorbHeartTextureYPos, 9, 9);
                    case 3, 4, 5 -> blit(matrices, x, y, 160, absorbHeartTextureYPos, 9, 9);
                    case 6, 7 -> blit(matrices, x, y, 151, absorbHeartTextureYPos, 9, 9);
                    case 8, 9, 0 -> blit(matrices, x, y, 142, absorbHeartTextureYPos, 9, 9);
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
                    blit(matrices, x, y, margin + 18, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 5.0F < currentHealth) {
                    //3/4 heart
                    blit(matrices, x, y, margin + 27, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F + 2.5F < currentHealth) {
                    //Half heart
                    blit(matrices, x, y, margin + 36, heartTextureYPos, 9, 9);
                }
                else if (currentHeart * 10.0F < currentHealth) {
                    //1/4 heart
                    blit(matrices, x, y, margin + 45, heartTextureYPos, 9, 9);
                }
            }
        }
        RenderSystem.disableBlend();
        this.mc.getProfiler().pop();
    }

    public void renderHitbox(PoseStack matrices, MultiBufferSource buffer, Entity entity, Hitbox hitbox, Camera camera, float partialTicks) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
        RenderSystem.disableTexture();
        double projX = camera.getPosition().x;
        double projY = camera.getPosition().y;
        double projZ = camera.getPosition().z;
        double posX = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double posY = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double posZ = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        drawHitbox(matrices, buffer.getBuffer(RenderType.lines()), hitbox, posX - projX, posY - projY, posZ - projZ, 1.0F, 1.0F, 0.0F, 1.0F, entity,
                   partialTicks);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderItemInFirstPerson(PoseStack matrices, MultiBufferSource buffer, int packedLight, float partialTicks) {
        if (this.client.shouldRenderPlayer() || !this.mc.player.isAlive()) {
            return;
        }
        AbstractClientPlayer player = this.mc.player;
        float interpPitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        boolean mainHand = true;
        boolean offHand = true;
        if (player.isUsingItem()) {
            ItemStack activeStack = player.getUseItem();
            if (activeStack.getItem() instanceof ProjectileWeaponItem) {
                mainHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
                offHand = !mainHand;
            }
            InteractionHand activeHand = player.getUsedItemHand();
            if (activeHand == InteractionHand.MAIN_HAND) {
                ItemStack offHandStack = player.getOffhandItem();
                if (offHandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offHandStack)) {
                    offHand = false;
                }
            }
        }
        else {
            ItemStack mainHandStack = player.getMainHandItem();
            ItemStack offhandStack = player.getOffhandItem();
            if (mainHandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(mainHandStack)) {
                offHand = false;
            }
            if (offhandStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offhandStack)) {
                mainHand = !mainHandStack.isEmpty();
                offHand = !mainHand;
            }
        }
        if (mainHand) {
            float swingProg = this.getMainhandSwingProgress(partialTicks);
            float equippedProgress = 1.0F - Mth.lerp(partialTicks, this.mainhandPrevEquipProgress, this.mainhandEquipProgress);
            this.renderItemInFirstPerson(matrices, buffer, packedLight, player, partialTicks, interpPitch, InteractionHand.MAIN_HAND, swingProg,
                                         this.mainHandStack, equippedProgress);
        }
        if (offHand) {
            float swingProg = this.getOffhandSwingProgress(partialTicks);
            float equippedProgress = 1.0F - Mth.lerp(partialTicks, this.offhandPrevEquipProgress, this.offhandEquipProgress);
            this.renderItemInFirstPerson(matrices, buffer, packedLight, player, partialTicks, interpPitch, InteractionHand.OFF_HAND, swingProg,
                                         this.offhandStack, equippedProgress);
        }
//        RenderSystem.disableRescaleNormal();
//        RenderHelper.turnOff();
    }

    public void renderItemInFirstPerson(PoseStack matrices,
                                        MultiBufferSource buffer,
                                        int packedLight,
                                        AbstractClientPlayer player,
                                        float partialTicks,
                                        float pitch,
                                        InteractionHand hand,
                                        float swingProgress,
                                        ItemStack stack,
                                        float equippedProgress) {
        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm handSide = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        matrices.pushPose();
        if (stack.isEmpty()) {
            if (mainHand && !player.isInvisible()) {
                this.renderArmFirstPerson(matrices, buffer, packedLight, equippedProgress, swingProgress, handSide);
            }
        }
        else if (stack.getItem() instanceof MapItem) {
            if (mainHand && this.offhandStack.isEmpty()) {
                this.renderMapFirstPerson(matrices, buffer, packedLight, pitch, equippedProgress, swingProgress);
            }
            else {
                this.renderMapFirstPersonSide(matrices, buffer, packedLight, equippedProgress, handSide, swingProgress, stack);
            }
        }
        else if (stack.getItem() == Items.CROSSBOW) {
            boolean isCrossbowCharged = CrossbowItem.isCharged(stack);
            boolean isRightSide = handSide == HumanoidArm.RIGHT;
            int sideOffset = isRightSide ? 1 : -1;
            if (player.isUsingItem() && player.getTicksUsingItem() > 0 && player.getUsedItemHand() == hand) {
                transformSideFirstPerson(matrices, handSide, equippedProgress);
                matrices.translate(sideOffset * -0.478_568_2, -0.094_387, 0.057_315_31);
                matrices.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
                matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 65.3F));
                matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * -9.785F));
                float f9 = stack.getUseDuration() - (this.mc.player.getTicksUsingItem() - partialTicks + 1.0F);
                float f13 = f9 / CrossbowItem.getChargeDuration(stack);
                if (f13 > 1.0F) {
                    f13 = 1.0F;
                }
                if (f13 > 0.1F) {
                    float f16 = MathHelper.sin((f9 - 0.1F) * 1.3F);
                    float f3 = f13 - 0.1F;
                    float f4 = f16 * f3;
                    matrices.translate(0, f4 * 0.004, 0);
                }
                matrices.translate(0, 0, f13 * 0.04);
                matrices.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
                matrices.mulPose(Vector3f.YN.rotationDegrees(sideOffset * 45.0F));
            }
            else {
                float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
                float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.TAU);
                float f2 = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
                matrices.translate(sideOffset * f, f1, f2);
                transformSideFirstPerson(matrices, handSide, equippedProgress);
                transformFirstPerson(matrices, handSide, swingProgress);
                if (isCrossbowCharged && swingProgress < 0.001F) {
                    matrices.translate(sideOffset * -0.641_864, 0, 0);
                    matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 10.0F));
                }
            }
            this.renderItemSide(matrices, buffer, packedLight, OverlayTexture.NO_OVERLAY, player, stack, isRightSide ?
                                                                                                         ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                                                                                                         ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !isRightSide);
        }
        else {
            boolean rightSide = handSide == HumanoidArm.RIGHT;
            if (player.isUsingItem() && player.getTicksUsingItem() > 0 && player.getUsedItemHand() == hand) {
                int sideOffset = rightSide ? 1 : -1;
                switch (stack.getUseAnimation()) {
                    case NONE -> transformSideFirstPerson(matrices, handSide, equippedProgress);
                    case BLOCK -> {
                        if (stack.getItem() instanceof IParry) {
                            transformSideFirstPersonParry(matrices, handSide, equippedProgress);
                        }
                        else {
                            transformSideFirstPerson(matrices, handSide, equippedProgress);
                        }
                    }
                    case EAT, DRINK -> {
                        this.transformEatFirstPerson(matrices, partialTicks, handSide, stack);
                        transformSideFirstPerson(matrices, handSide, equippedProgress);
                    }
                    case BOW -> {
                        transformSideFirstPerson(matrices, handSide, equippedProgress);
                        matrices.translate(sideOffset * -0.278_568_2, 0.183_443_87, 0.157_315_31);
                        matrices.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
                        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 35.3F));
                        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * -9.785F));
                        float f8 = stack.getUseDuration() - (this.mc.player.getTicksUsingItem() - partialTicks + 1.0F);
                        float f12 = f8 / 20.0F;
                        f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
                        if (f12 > 1.0F) {
                            f12 = 1.0F;
                        }
                        if (f12 > 0.1F) {
                            float f15 = MathHelper.sin((f8 - 0.1F) * 1.3F);
                            float f18 = f12 - 0.1F;
                            float f20 = f15 * f18;
                            matrices.translate(0, f20 * 0.004, 0);
                        }
                        matrices.translate(0, 0, f12 * 0.04);
                        matrices.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
                        matrices.mulPose(Vector3f.YN.rotationDegrees(sideOffset * 45.0F));
                    }
                    case SPEAR -> {
                        transformSideFirstPerson(matrices, handSide, equippedProgress);
                        matrices.translate(sideOffset * -0.5, 0.7, 0.1);
                        matrices.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
                        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 35.3F));
                        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * -9.785F));
                        float f7 = stack.getUseDuration() - (this.mc.player.getTicksUsingItem() - partialTicks + 1.0F);
                        float f11 = f7 / 10.0F;
                        if (f11 > 1.0F) {
                            f11 = 1.0F;
                        }
                        if (f11 > 0.1F) {
                            float f14 = MathHelper.sin((f7 - 0.1F) * 1.3F);
                            float f17 = f11 - 0.1F;
                            float f19 = f14 * f17;
                            matrices.translate(0, f19 * 0.004, 0);
                        }
                        matrices.translate(0, 0, f11 * 0.2);
                        matrices.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
                        matrices.mulPose(Vector3f.YN.rotationDegrees(sideOffset * 45.0F));
                    }
                }
            }
            else if (player.isAutoSpinAttack()) {
                transformSideFirstPerson(matrices, handSide, equippedProgress);
                int j = rightSide ? 1 : -1;
                matrices.translate(j * -0.4, 0.8, 0.3);
                matrices.mulPose(Vector3f.YP.rotationDegrees(j * 65.0F));
                matrices.mulPose(Vector3f.ZP.rotationDegrees(j * -85.0F));
            }
            else {
                float f5 = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
                float f6 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.TAU);
                float f10 = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
                int sideOffset = rightSide ? 1 : -1;
                matrices.translate(sideOffset * f5, f6, f10);
                transformSideFirstPerson(matrices, handSide, equippedProgress);
                transformFirstPerson(matrices, handSide, swingProgress);
                if (hand == InteractionHand.MAIN_HAND) {
                    if (InputHooks.isMainhandLungeInProgress) {
                        transformLungeFirstPerson(matrices, partialTicks, hand, handSide, stack);
                    }
                    else if (InputHooks.isMainhandLunging) {
                        this.transformLungingFirstPerson(matrices, partialTicks, hand, stack);
                    }
                    else if (this.client.isMainhandCustomAttacking()) {
                        if (((ILivingEntityPatch) player).getMainhandCustomAttackType() == ICustomAttack.AttackType.SWORD) {
                            transformSwordFirstPerson(matrices, player, partialTicks, hand, handSide, stack);
                        }
                    }
                }
                else {
                    if (InputHooks.isOffhandLungeInProgress) {
                        transformLungeFirstPerson(matrices, partialTicks, hand, handSide.getOpposite(), stack);
                    }
                    else if (InputHooks.isOffhandLunging) {
                        this.transformLungingFirstPerson(matrices, partialTicks, hand, stack);
                    }
                }
            }
            this.renderItemSide(matrices, buffer, packedLight, OverlayTexture.NO_OVERLAY, player, stack, rightSide ?
                                                                                                         ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                                                                                                         ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !rightSide);
        }
        matrices.popPose();
    }

    public void renderItemSide(PoseStack matrices,
                               MultiBufferSource buffer,
                               int packedLight,
                               int packedOverlay,
                               LivingEntity entity,
                               ItemStack stack,
                               ItemTransforms.TransformType transform,
                               boolean leftHanded) {
        if (!stack.isEmpty()) {
            this.itemRenderer.renderStatic(entity, stack, transform, leftHanded, matrices, buffer, entity.level, packedLight, packedOverlay, 0);
        }
    }

    private void renderMapFirstPerson(PoseStack matrices,
                                      MultiBufferSource buffer,
                                      int packedLight,
                                      float pitch,
                                      float equippedProgress,
                                      float swingProgress) {
        float sqrtSwingProg = MathHelper.sqrt(swingProgress);
        float f1 = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
        float f2 = -0.4F * MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        matrices.translate(0, -f1 / 2.0, f2);
        float f3 = getMapAngleFromPitch(pitch);
        matrices.translate(0, 0.04 + equippedProgress * -1.2 + f3 * -0.5, -0.72);
        matrices.mulPose(Vector3f.XP.rotationDegrees(f3 * -85.0F));
        if (!this.mc.player.isInvisible()) {
            matrices.pushPose();
            matrices.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.renderArm(matrices, buffer, packedLight, HumanoidArm.RIGHT);
            this.renderArm(matrices, buffer, packedLight, HumanoidArm.LEFT);
            matrices.popPose();
        }
        float f4 = MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        matrices.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
        matrices.scale(2.0F, 2.0F, 2.0F);
        this.renderMapFirstPerson(matrices, buffer, packedLight, this.mainHandStack);
    }

    private void renderMapFirstPerson(PoseStack matrices, MultiBufferSource buffer, int packedLight, ItemStack stack) {
        matrices.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrices.scale(0.38F, 0.38F, 0.38F);
        matrices.translate(-0.5, -0.5, 0);
        matrices.scale(0.007_812_5F, 0.007_812_5F, 0.007_812_5F);
        MapItemSavedData mapData = MapItem.getSavedData(stack, this.mc.level);
        //noinspection VariableNotUsedInsideIf
        VertexConsumer consumer = buffer.getBuffer(mapData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f mat = matrices.last().pose();
        consumer.vertex(mat, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(packedLight).endVertex();
        consumer.vertex(mat, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(packedLight).endVertex();
        consumer.vertex(mat, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(packedLight).endVertex();
        consumer.vertex(mat, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(packedLight).endVertex();
        if (mapData != null) {
            this.mc.gameRenderer.getMapRenderer().render(matrices, buffer, MapItem.getMapId(stack), mapData, false, packedLight);
        }
    }

    private void renderMapFirstPersonSide(PoseStack matrices,
                                          MultiBufferSource buffer,
                                          int packedLight,
                                          float equippedProgress,
                                          HumanoidArm arm,
                                          float swingProgress,
                                          ItemStack stack) {
        float sideOffset = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        matrices.translate(sideOffset * 0.125F, -0.125F, 0.0F);
        if (!this.mc.player.isInvisible()) {
            matrices.pushPose();
            matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * 10.0F));
            this.renderArmFirstPerson(matrices, buffer, packedLight, equippedProgress, swingProgress, arm);
            matrices.popPose();
        }
        matrices.pushPose();
        matrices.translate(sideOffset * 0.51, -0.08 + equippedProgress * -1.2, -0.75);
        float sqrtSwingProg = MathHelper.sqrt(swingProgress);
        float f2 = MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * MathHelper.sin(sqrtSwingProg * MathHelper.TAU);
        float f5 = -0.3F * MathHelper.sin(swingProgress * MathHelper.PI);
        matrices.translate(sideOffset * f3, f4 - 0.3 * f2, f5);
        matrices.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * f2 * -30.0F));
        this.renderMapFirstPerson(matrices, buffer, packedLight, stack);
        matrices.popPose();
    }

    public void renderOutlines(PoseStack matrices, MultiBufferSource buffer, VoxelShape shape, Camera info, BlockPos pos) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
        RenderSystem.disableTexture();
        matrices.pushPose();
        double projX = info.getPosition().x;
        double projY = info.getPosition().y;
        double projZ = info.getPosition().z;
        drawShape(matrices, buffer.getBuffer(RenderType.LINES), shape, pos.getX() - projX, pos.getY() - projY, pos.getZ() - projZ, 1.0F, 1.0F, 0.0F,
                  1.0F);
        matrices.popPose();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderPotionIcons(PoseStack matrices, float partialTicks, int width, int height) {
        MobEffectTextureManager effectTextures = this.mc.getMobEffectTextures();
        ClientEffectInstance movingInstance = null;
        Runnable runnable = null;
        while (!ClientEvents.EFFECTS_TO_ADD.isEmpty()) {
            ClientEffectInstance addingInstance = ClientEvents.EFFECTS_TO_ADD.get(0);
            MobEffect addingEffect = addingInstance.getEffect();
            if (!addingInstance.isShowIcon()) {
                ClientEvents.EFFECTS_TO_ADD.remove(addingInstance);
                ClientEvents.EFFECTS.add(addingInstance);
                this.client.effectToAddTicks = 0;
                continue;
            }
            if (!this.isAddingEffect) {
                this.isAddingEffect = true;
                this.client.effectToAddTicks = 0;
            }
            float alpha;
            float x0 = (width - 24) / 2.0f;
            float y0 = Math.max((height - 24) / 3.0f, 1 + 26 * 3 + 12 + (this.mc.isDemo() ? 15 : 0));
            float x = x0;
            float y = y0;
            if (this.client.effectToAddTicks < 5) {
                alpha = (this.client.effectToAddTicks + partialTicks) / 5.0f;
            }
            else if (this.client.effectToAddTicks < 15) {
                alpha = 1.0f;
                if (this.client.effectToAddTicks == 14) {
                    movingInstance = addingInstance;
                }
            }
            else {
                movingInstance = addingInstance;
                alpha = 1.0f;
                float x1 = width - 25 * this.movingFinalCount;
                float t = (this.client.effectToAddTicks - 15 + partialTicks) / 5.0f;
                t = MathHelper.clamp(t, 0, 1);
                x = (x1 - x0) * t + x0;
                float y1 = this.getYPosForEffect(addingEffect);
                y = (y1 - y0) * t + y0;
            }
            float finalX = x;
            float finalY = y;
            runnable = () -> {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                if (addingInstance.isAmbient()) {
                    floatBlit(matrices, finalX, finalY, 24, 198, 24, 24, -80);
                }
                else {
                    floatBlit(matrices, finalX, finalY, 0, 198, 24, 24, -80);
                }
                TextureAtlasSprite atlasSprite = effectTextures.get(addingEffect);
                RenderSystem.setShaderTexture(0, atlasSprite.atlas().location());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                floatBlit(matrices, finalX + 3, finalY + 3, -79, 18, 18, atlasSprite);
                if (addingInstance.getAmplifier() != 0) {
                    matrices.pushPose();
                    matrices.scale(0.5f, 0.5f, 0.5f);
                    this.mc.font.drawShadow(matrices, MathHelper.getRomanNumber(ScreenDisplayEffects.getFixedAmplifier(addingInstance) + 1),
                                            (finalX + 3) * 2, (finalY + 17) * 2, 0xffff_ffff);
                    matrices.popPose();
                }
            };
            if (this.client.effectToAddTicks >= 20) {
                movingInstance = null;
                this.client.effectToAddTicks = 0;
                this.isAddingEffect = false;
                ClientEvents.removeEffect(ClientEvents.EFFECTS, addingEffect);
                for (ClientEffectInstance instance : ClientEvents.EFFECTS_TO_ADD) {
                    for (int i = 0; i < 20; i++) {
                        instance.tick();
                    }
                }
                ClientEvents.EFFECTS_TO_ADD.remove(addingInstance);
                ClientEvents.EFFECTS.add(addingInstance);
            }
            break;
        }
        if (!ClientEvents.EFFECTS.isEmpty()) {
            RenderSystem.enableBlend();
            this.effects.clear();
            this.effects.addAll(ClientEvents.EFFECTS);
            MobEffectCategory movingCategory = null;
            MobEffect repeated = null;
            if (movingInstance != null) {
                if (ClientEvents.containsEffect(this.effects, movingInstance.getEffect())) {
                    repeated = movingInstance.getEffect();
                }
                else {
                    this.effects.add(movingInstance);
                    movingCategory = movingInstance.getEffect().getCategory();
                }
            }
            Collections.sort(this.effects);
            Collections.reverse(this.effects);
            this.runnables.clear();
            int beneficalCount = 0;
            int neutralCount = 0;
            int harmfulCount = 0;
            boolean isMoving = false;
            for (ClientEffectInstance effectInstance : this.effects) {
                if (!effectInstance.isShowIcon()) {
                    continue;
                }
                MobEffect effect = effectInstance.getEffect();
                if (effectInstance == movingInstance) {
                    isMoving = true;
                }
                float y = 1;
                if (this.mc.isDemo()) {
                    y += 15;
                }
                float x = width;
                switch (effect.getCategory()) {
                    case BENEFICIAL -> {
                        beneficalCount++;
                        x -= 25 * beneficalCount;
                        if (effectInstance == movingInstance || effect == repeated) {
                            this.movingFinalCount = beneficalCount;
                        }
                        if (isMoving && movingCategory == MobEffectCategory.BENEFICIAL) {
                            x += 25 * Math.min(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                        }
                    }
                    case NEUTRAL -> {
                        neutralCount++;
                        x -= 25 * neutralCount;
                        if (this.lastBeneficalCount > 0) {
                            y += 26;
                            if (this.lastBeneficalCount == 1 && movingCategory == MobEffectCategory.BENEFICIAL) {
                                y -= 26 * Math.min(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                            }
                        }
                        if (effectInstance == movingInstance || effect == repeated) {
                            this.movingFinalCount = neutralCount;
                        }
                        if (isMoving && movingCategory == MobEffectCategory.NEUTRAL) {
                            x += 25 * Math.min(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                        }
                    }
                    case HARMFUL -> {
                        harmfulCount++;
                        x -= 25 * harmfulCount;
                        if (this.lastBeneficalCount > 0) {
                            y += 26;
                            if (this.lastBeneficalCount == 1 && movingCategory == MobEffectCategory.BENEFICIAL) {
                                y -= 26 * Math.min(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                            }
                        }
                        if (this.lastNeutralCount > 0) {
                            y += 26;
                            if (this.lastNeutralCount == 1 && movingCategory == MobEffectCategory.NEUTRAL) {
                                y -= 26 * Math.min(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                            }
                        }
                        if (effectInstance == movingInstance || effect == repeated) {
                            this.movingFinalCount = harmfulCount;
                        }
                        if (isMoving && movingCategory == MobEffectCategory.HARMFUL) {
                            x += 25 * Math.min(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                        }
                    }
                }
                if (effectInstance != movingInstance && !this.mc.options.renderDebug) {
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, ContainerScreen.INVENTORY_LOCATION);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    float alpha = 1.0F;
                    if (effectInstance.isAmbient()) {
                        floatBlit(matrices, x, y, 165, 166, 24, 24, 0);
                    }
                    else {
                        floatBlit(matrices, x, y, 141, 166, 24, 24, 0);
                        if (effectInstance.getDuration() <= 200) {
                            int remainingSeconds = 10 - effectInstance.getDuration() / 20;
                            alpha = MathHelper.clamp(effectInstance.getDuration() / 100.0F, 0.0F, 0.5F) +
                                    MathHelper.cos(effectInstance.getDuration() * MathHelper.PI / 5.0F) *
                                    MathHelper.clamp(remainingSeconds / 40.0F, 0.0F, 0.25F);
                        }
                    }
                    TextureAtlasSprite atlasSprite = effectTextures.get(effect);
                    RenderSystem.setShaderTexture(0, atlasSprite.atlas().location());
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
                    floatBlit(matrices, x + 3, y + 3, 0, 18, 18, atlasSprite);
                    if (effectInstance.getAmplifier() != 0) {
                        float finalX = x;
                        float finalY = y;
                        //noinspection ObjectAllocationInLoop
                        this.runnables.add(() -> this.mc.font.drawShadow(matrices, MathHelper.getRomanNumber(
                                ScreenDisplayEffects.getFixedAmplifier(effectInstance) + 1), (finalX + 3) * 2, (finalY + 17) * 2, 0xff_ffff));
                    }
                }
            }
            for (Runnable run : this.runnables) {
                matrices.pushPose();
                matrices.scale(0.5f, 0.5f, 0.5f);
                if (run != null) {
                    run.run();
                }
                matrices.popPose();
            }
            this.lastBeneficalCount = beneficalCount;
            this.lastNeutralCount = neutralCount;
        }
        else {
            this.movingFinalCount = 1;
            this.lastBeneficalCount = 0;
            this.lastNeutralCount = 0;
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    public void renderTemperature(PoseStack matrices, int width, int height) {
        if (this.mc.gameMode.hasExperience()) {
            this.mc.getProfiler().push("temperature");
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
            TemperatureClient temperature = TemperatureClient.CLIENT_INSTANCE;
            int currentTemp = temperature.getCurrentTemperature();
            int minComf = temperature.getCurrentMinComfort() + 70;
            int maxComf = temperature.getCurrentMaxComfort() + 70;
            if (minComf != maxComf) {
                //Draw Comfort zone
                blit(matrices, width / 2 - 90 + minComf, height - 29, 1, 120, maxComf - minComf, 5);
            }
            if (minComf > 0) {
                //Draw Cold zone
                blit(matrices, width / 2 - 90, height - 29, 1 + 180 - minComf, 125, minComf, 5);
            }
            if (maxComf < 180) {
                //Draw Hot zone
                blit(matrices, width / 2 - 90 + maxComf, height - 29, 1, 130, 180 - maxComf, 5);
            }
            //Draw bar
            blit(matrices, width / 2 - 91, height - 29, 0, 64, 182, 5);
            if (currentTemp > -69 && currentTemp < 109) {
                currentTemp += 69;
                //Draw meter
                blit(matrices, width / 2 - 93 + currentTemp, height - 29, 0, 69, 8, 5);
            }
            else if (currentTemp < 0) {
                //Draw too cold indicator
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.sinDeg(this.client.getTickCount() * 9));
                RenderSystem.enableBlend();
                blit(matrices, width / 2 - 97, height - 29, 9, 69, 5, 5);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            else {
                //Draw too hot indicator
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.sinDeg(this.client.getTickCount() * 9));
                RenderSystem.enableBlend();
                blit(matrices, width / 2 + 92, height - 29, 15, 69, 5, 5);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            this.mc.getProfiler().pop();
        }
    }

    public void resetEquipProgress(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandEquipProgress = 0;
        }
        else {
            this.offhandEquipProgress = 0;
        }
    }

    public void resetFullEquipProgress(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainhandEquipProgress = 0;
            this.mainhandPrevEquipProgress = 0;
        }
        else {
            this.offhandEquipProgress = 0;
            this.offhandPrevEquipProgress = 0;
        }
    }

    public void startTick() {
        this.mainhandPrevEquipProgress = this.mainhandEquipProgress;
        this.offhandPrevEquipProgress = this.offhandEquipProgress;
        this.mainhandPrevSwingProgress = this.mainhandSwingProgress;
        this.offhandPrevSwingProgress = this.offhandSwingProgress;
        this.updateArmSwingProgress();
        LocalPlayer player = this.mc.player;
        ItemStack mainhandStack = player.getMainHandItem();
        ItemStack offhandStack = player.getOffhandItem();
        if (player.isHandsBusy()) {
            this.mainhandEquipProgress = MathHelper.clamp(this.mainhandEquipProgress - 0.4F, 0.0F, 1.0F);
            this.offhandEquipProgress = MathHelper.clamp(this.offhandEquipProgress - 0.4F, 0.0F, 1.0F);
        }
        else {
            boolean requipM = shouldCauseReequipAnimation(this.mainHandStack, mainhandStack, player.getInventory().selected);
            boolean requipO = shouldCauseReequipAnimation(this.offhandStack, offhandStack, -1);
            if (requipM) {
                this.client.mainhandTimeSinceLastHit = 0;
                if (!ItemStack.matches(this.currentMainhandItem, mainhandStack)) {
                    this.currentMainhandItem = mainhandStack;
                    if (this.currentMainhandItem.getItem() instanceof ItemSword) {
                        this.mc.getSoundManager()
                               .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_UNSHEATHE.get(), SoundSource.PLAYERS, 0.8f, 1.0f));
                        EvolutionNetwork.INSTANCE.sendToServer(
                                new PacketCSPlaySoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_UNSHEATHE.get(), SoundSource.PLAYERS, 0.8f,
                                                                   1.0f));
                    }
                }
            }
            else {
                this.mainHandStack = mainhandStack.copy();
                this.client.mainhandTimeSinceLastHit++;
            }
            if (requipO) {
                this.client.offhandTimeSinceLastHit = 0;
                if (!ItemStack.matches(this.currentOffhandItem, offhandStack)) {
                    this.currentOffhandItem = offhandStack;
                    if (this.currentOffhandItem.getItem() instanceof ItemSword) {
                        this.mc.getSoundManager()
                               .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_UNSHEATHE.get(), SoundSource.PLAYERS, 0.8f, 1.0f));
                        EvolutionNetwork.INSTANCE.sendToServer(
                                new PacketCSPlaySoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_UNSHEATHE.get(), SoundSource.PLAYERS, 0.8f,
                                                                   1.0f));
                    }
                }
            }
            else {
                this.offhandStack = offhandStack.copy();
                this.client.offhandTimeSinceLastHit++;
            }
            float cooledAttackStrength = this.client.getOffhandCooledAttackStrength(this.mc.player.getOffhandItem(), 1.0F);
            this.offhandEquipProgress += MathHelper.clamp(
                    (!requipO ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) - this.offhandEquipProgress, -0.4f, 0.4F);
            cooledAttackStrength = this.client.getMainhandCooledAttackStrength(1.0F);
            this.mainhandEquipProgress += MathHelper.clamp(
                    (!requipM ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) - this.mainhandEquipProgress, -0.4F, 0.4F);

        }
        if (this.mainhandEquipProgress < 0.1F && !InputHooks.isMainhandLunging) {
            this.mainHandStack = mainhandStack.copy();
        }
        if (this.offhandEquipProgress < 0.1F && !InputHooks.isOffhandLunging) {
            this.offhandStack = offhandStack.copy();
        }
        if (InputHooks.isMainhandLunging) {
            ClientEvents.MISS_TIME.set(this.mc, 1);
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

    public void swingArm(InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
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

    private void transformEatFirstPerson(PoseStack matrices, float partialTicks, HumanoidArm hand, ItemStack stack) {
        float useTime = this.mc.player.getTicksUsingItem() - partialTicks + 1.0F;
        float relativeUse = useTime / stack.getUseDuration();
        if (relativeUse < 0.8F) {
            float f2 = Math.abs(MathHelper.cos(useTime / 4.0F * MathHelper.PI) * 0.1F);
            matrices.translate(0, f2, 0);
        }
        float f3 = 1.0F - (float) Math.pow(relativeUse, 27);
        int sideOffset = hand == HumanoidArm.RIGHT ? 1 : -1;
        matrices.translate(f3 * 0.6 * sideOffset, f3 * -0.5, 0);
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * f3 * 90.0F));
        matrices.mulPose(Vector3f.XP.rotationDegrees(f3 * 10.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * f3 * 30.0F));
    }

    private void transformLungingFirstPerson(PoseStack matrices, float partialTicks, InteractionHand hand, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ILunge) {
            if (hand == InteractionHand.MAIN_HAND) {
                float lungeTime = this.mainhandLungingTicks + partialTicks;
                float relativeLunge = MathHelper.relativize(lungeTime, 0.0f, 5.0f);
                matrices.mulPose(Vector3f.YP.rotationDegrees(-5.0F));
                matrices.mulPose(Vector3f.XP.rotationDegrees(-100.0F));
                matrices.translate(0, 0.85 * relativeLunge - 0.6, 0);
            }
            else {
                float lungeTime = this.offhandLungingTicks + partialTicks;
                float relativeLunge = MathHelper.relativize(lungeTime, 0.0f, 5.0f);
                matrices.mulPose(Vector3f.YP.rotationDegrees(5.0F));
                matrices.mulPose(Vector3f.XP.rotationDegrees(-100.0F));
                matrices.translate(0, 0.85 * relativeLunge - 0.6, 0);
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

    public void updateHitmarkers(boolean isKill) {
        if (isKill) {
            this.killmarkerTick = 15;
        }
        else {
            this.hitmarkerTick = 15;
        }
    }
}
