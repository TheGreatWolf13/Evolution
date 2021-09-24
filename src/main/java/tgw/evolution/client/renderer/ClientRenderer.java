package tgw.evolution.client.renderer;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
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
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.NBTTypes;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.Matrix3d;

import javax.annotation.Nonnull;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientRenderer {

    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard" +
                                                                                                       ".png"));
    public static ClientRenderer instance;
    private static int slotMainHand;
    private final ClientEvents client;
    private final EntityRendererManager entityRendererManager;
    private final ItemRenderer itemRenderer;
    private final Minecraft mc;
    private final Random rand = new Random();
    private final byte[] thirstShakeAligment = new byte[10];
    public boolean isAddingEffect;
    public boolean isRenderingPlayer;
    private byte alphaDir = 1;
    private ItemStack currentMainhandItem = ItemStack.EMPTY;
    private ItemStack currentOffhandItem = ItemStack.EMPTY;
    private float flashAlpha;
    private long healthUpdateCounter;
    private int hitmarkerTick;
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

    public ClientRenderer(Minecraft mc, ClientEvents client) {
        instance = this;
        this.mc = mc;
        this.client = client;
        this.entityRendererManager = mc.getEntityRenderDispatcher();
        this.itemRenderer = mc.getItemRenderer();
    }

    private static void blit(MatrixStack matrices, int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        AbstractGui.blit(matrices, x, y, 20, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    public static void disableAlpha(float alpha) {
        RenderSystem.disableBlend();
        if (alpha == 1.0f) {
            return;
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawHitbox(MatrixStack matrices,
                                  IVertexBuilder buffer,
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
        boolean renderAll = Minecraft.getInstance().player.getMainHandItem().getItem() == EvolutionItems.debug_item.get() ||
                            Minecraft.getInstance().player.getOffhandItem().getItem() == EvolutionItems.debug_item.get();
        hitbox.getParent().init(entity, partialTicks);
        Matrix3d mainTransform = hitbox.getParent().getTransform().transpose();
        final double[] points = new double[3];
        Matrix4f mat = matrices.last().pose();
        for (Hitbox box : hitbox.getParent().getBoxes()) {
            if (!renderAll) {
                box = hitbox;
            }
            Hitbox finalBox = box;
            Matrix3d transform = finalBox.getTransformation().transpose();
            //noinspection ObjectAllocationInLoop
            finalBox.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
                transform.transform(x0, y0, z0, points);
                finalBox.doOffset(points);
                mainTransform.transform(points[0], points[1], points[2], points);
                finalBox.getParent().doOffset(points);
                buffer.vertex(mat, (float) (points[0] + x), (float) (points[1] + y), (float) (points[2] + z))
                      .color(red, green, blue, alpha)
                      .endVertex();
                transform.transform(x1, y1, z1, points);
                finalBox.doOffset(points);
                mainTransform.transform(points[0], points[1], points[2], points);
                finalBox.getParent().doOffset(points);
                buffer.vertex(mat, (float) (points[0] + x), (float) (points[1] + y), (float) (points[2] + z))
                      .color(red, green, blue, alpha)
                      .endVertex();
            });
            if (!renderAll) {
                break;
            }
        }
    }

    private static void drawSelectionBox(MatrixStack matrices,
                                         IVertexBuilder buffer,
                                         Entity entity,
                                         double x,
                                         double y,
                                         double z,
                                         BlockPos pos,
                                         BlockState state) {
        drawShape(matrices,
                  buffer,
                  state.getShape(entity.level, pos, ISelectionContext.of(entity)),
                  pos.getX() - x,
                  pos.getY() - y,
                  pos.getZ() - z,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.4F);
    }

    private static void drawShape(MatrixStack matrices,
                                  IVertexBuilder buffer,
                                  VoxelShape shape,
                                  double x,
                                  double y,
                                  double z,
                                  float red,
                                  float green,
                                  float blue,
                                  float alpha) {
        Matrix4f mat = matrices.last().pose();
        shape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
            buffer.vertex(mat, (float) (x0 + x), (float) (y0 + y), (float) (z0 + z)).color(red, green, blue, alpha).endVertex();
            buffer.vertex(mat, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).color(red, green, blue, alpha).endVertex();
        });
    }

    public static void enableAlpha(float alpha) {
        RenderSystem.enableBlend();
        if (alpha == 1.0f) {
            return;
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void floatBlit(MatrixStack matrices, float x, float y, int textureX, int textureY, int sizeX, int sizeY, int blitOffset) {
        GUIUtils.floatBlit(matrices, x, y, blitOffset, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    public static void floatBlit(MatrixStack matrices, float x, float y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        GUIUtils.innerFloatBlit(matrices.last().pose(),
                                x,
                                x + width,
                                y,
                                y + height,
                                blitOffset,
                                sprite.getU0(),
                                sprite.getU1(),
                                sprite.getV0(),
                                sprite.getV1());
    }

    private static float getMapAngleFromPitch(float pitch) {
        float f = 1.0F - pitch / 45.0F + 0.1F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * MathHelper.PI) * 0.5F + 0.5F;
        return f;
    }

    private static float roundToHearts(float currentHealth) {
        return MathHelper.ceil(currentHealth * 0.4F) / 0.4F;
    }

    private static int shiftTextByLines(int desiredLine, int y) {
        return y + 10 * (desiredLine - 1) + 1;
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

    private static void transformFirstPerson(MatrixStack matrices, HandSide hand, float swingProgress) {
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * MathHelper.PI);
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * (45.0F + f * -20.0F)));
        float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * f1 * -20.0F));
        matrices.mulPose(Vector3f.XP.rotationDegrees(f1 * -80.0F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * -45.0F));
    }

    private static void transformLungeFirstPerson(MatrixStack matrices, float partialTicks, Hand hand, HandSide handSide, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ILunge) {
            float lungeTime = InputHooks.getLungeTime(hand) + partialTicks;
            int minLunge = ((ILunge) item).getMinLungeTime();
            int fullLunge = ((ILunge) item).getFullLungeTime();
            float relativeLunge = MathHelper.relativize(lungeTime, minLunge, fullLunge);
            matrices.mulPose(Vector3f.YP.rotationDegrees(handSide == HandSide.RIGHT ? -5.0F : 5.0F));
            float relativeRotation = MathHelper.clamp(relativeLunge * 3.0f, 0.0f, 1.0f);
            matrices.mulPose(Vector3f.XP.rotationDegrees(-100.0F * relativeRotation));
            if (relativeLunge > 0.333f) {
                float relativeTranslation = MathHelper.relativize(relativeLunge, 0.333f, 1.0f);
                matrices.translate(0, -0.6 * relativeTranslation, 0);
            }
        }
    }

    private static void transformSideFirstPerson(MatrixStack matrices, HandSide hand, float equippedProg) {
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        matrices.translate(sideOffset * 0.56, -0.52 + equippedProg * -0.6, -0.72);
    }

    private static void transformSideFirstPersonParry(MatrixStack matrices, HandSide hand, float equippedProg) {
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        matrices.translate(sideOffset * 0.56, -0.52 + equippedProg * -0.6, -0.72);
        matrices.translate(sideOffset * -0.141_421_36, 0.08, 0.141_421_36);
        matrices.mulPose(Vector3f.XP.rotationDegrees(-102.25F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * 13.365F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * 78.05F));
    }

    public void drawHydrationOverlay(MatrixStack matrices, int hydrationGained, int hydrationLevel, int left, int top, float alpha) {
        for (int j = 0; j < 3; j++) {
            if (hydrationLevel + hydrationGained <= 0) {
                return;
            }
            int startBar = hydrationGained != 0 ? hydrationLevel / (int) ThirstStats.HYDRATION_SCALE : 0;
            int endBar = MathHelper.ceil(MathHelper.clampMax(ThirstStats.INTOXICATION, hydrationLevel + hydrationGained) /
                                         ThirstStats.HYDRATION_SCALE);
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

    public void drawThirstOverlay(MatrixStack matrices, int thirstRestored, int thirstLevel, int left, int top, float alpha) {
        if (thirstRestored == 0) {
            return;
        }
        int endBar = MathHelper.ceil(MathHelper.clampMax(ThirstStats.THIRST_CAPACITY, thirstLevel + thirstRestored) / ThirstStats.THIRST_SCALE);
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
        this.flashAlpha += this.alphaDir * 0.125f;
        if (this.flashAlpha >= 1.5f) {
            this.flashAlpha = 1.0f;
            this.alphaDir = -1;
        }
        else if (this.flashAlpha <= -0.5f) {
            this.flashAlpha = 0.0f;
            this.alphaDir = 1;
        }
        if (this.hitmarkerTick > 0) {
            this.hitmarkerTick--;
        }
        if (this.killmarkerTick > 0) {
            this.killmarkerTick--;
        }
    }

    private int getArmSwingAnimationEnd() {
        if (EffectUtils.hasDigSpeed(this.mc.player)) {
            return 6 - (1 + EffectUtils.getDigSpeedAmplification(this.mc.player));
        }
        return this.mc.player.hasEffect(Effects.DIG_SLOWDOWN) ? 6 + (1 + this.mc.player.getEffect(Effects.DIG_SLOWDOWN).getAmplifier()) * 2 : 6;
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

    private float getYPosForEffect(Effect effect) {
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

    private boolean rayTraceMouse(RayTraceResult rayTraceResult) {
        if (rayTraceResult == null) {
            return false;
        }
        if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
            return ((EntityRayTraceResult) rayTraceResult).getEntity() instanceof INamedContainerProvider;
        }
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
            World world = this.mc.level;
            return world.getBlockState(blockpos).getMenuProvider(world, blockpos) != null;
        }
        return false;
    }

    private void renderArm(MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, HandSide side) {
        this.mc.getTextureManager().bind(this.mc.player.getSkinTextureLocation());
        EntityRenderer<? super ClientPlayerEntity> renderer = this.entityRendererManager.getRenderer(this.mc.player);
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        matrices.pushPose();
        float sideOffset = side == HandSide.RIGHT ? 1.0F : -1.0F;
        matrices.mulPose(Vector3f.YP.rotationDegrees(92.0f));
        matrices.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * -41.0F));
        matrices.translate(sideOffset * 0.3, -1.1, 0.45);
        if (side == HandSide.RIGHT) {
            playerRenderer.renderRightHand(matrices, buffer, packedLight, this.mc.player);
        }
        else {
            playerRenderer.renderLeftHand(matrices, buffer, packedLight, this.mc.player);
        }
        matrices.popPose();
    }

    public void renderArmFirstPerson(MatrixStack matrices,
                                     IRenderTypeBuffer buffer,
                                     int packedLight,
                                     float equippedProgress,
                                     float swingProgress,
                                     HandSide side) {
        boolean right = side == HandSide.RIGHT;
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
        AbstractClientPlayerEntity player = this.mc.player;
        this.mc.getTextureManager().bind(player.getSkinTextureLocation());
        matrices.translate(sideOffset * -1, 3.6, 3.5);
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * 120.0F));
        matrices.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * -135.0F));
        matrices.translate(sideOffset * 5.6, 0, 0);
        PlayerRenderer playerRenderer = (PlayerRenderer) this.entityRendererManager.getRenderer(player);
        if (right) {
            playerRenderer.renderRightHand(matrices, buffer, packedLight, player);
        }
        else {
            playerRenderer.renderLeftHand(matrices, buffer, packedLight, player);
        }
    }

    public void renderBlockOutlines(MatrixStack matrices, IRenderTypeBuffer buffer, ActiveRenderInfo camera, BlockPos hitPos) {
        BlockState state = this.mc.level.getBlockState(hitPos);
        if (!state.isAir(this.mc.level, hitPos) && this.mc.level.getWorldBorder().isWithinBounds(hitPos)) {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
            RenderSystem.disableTexture();
            matrices.pushPose();
            RenderSystem.scalef(1.0F, 1.0F, 1.0F);
            double projX = camera.getPosition().x;
            double projY = camera.getPosition().y;
            double projZ = camera.getPosition().z;
            drawSelectionBox(matrices, buffer.getBuffer(RenderType.lines()), camera.getEntity(), projX, projY, projZ, hitPos, state);
            matrices.popPose();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public void renderCrosshair(MatrixStack matrices, float partialTicks) {
        GameSettings gamesettings = this.mc.options;
        boolean offhandValid = this.mc.player.getOffhandItem().getItem() instanceof IOffhandAttackable;
        this.mc.getTextureManager().bind(EvolutionResources.GUI_ICONS);
        int scaledWidth = this.mc.getWindow().getGuiScaledWidth();
        int scaledHeight = this.mc.getWindow().getGuiScaledHeight();
        if (gamesettings.getCameraType() == PointOfView.FIRST_PERSON) {
            if (this.mc.gameMode.getPlayerMode() != GameType.SPECTATOR || this.rayTraceMouse(this.mc.hitResult)) {
                if (gamesettings.renderDebug && !gamesettings.hideGui && !this.mc.player.isReducedDebugInfo() && !gamesettings.reducedDebugInfo) {
                    RenderSystem.pushMatrix();
                    int blitOffset = 0;
                    RenderSystem.translatef(scaledWidth / 2.0F, scaledHeight / 2.0F, blitOffset);
                    ActiveRenderInfo camera = this.mc.gameRenderer.getMainCamera();
                    RenderSystem.rotatef(camera.getXRot(), -1.0f, 0.0F, 0.0F);
                    RenderSystem.rotatef(camera.getYRot(), 0.0F, 1.0F, 0.0F);
                    RenderSystem.scalef(-1.0f, -1.0f, -1.0f);
                    RenderSystem.renderCrosshair(10);
                    RenderSystem.popMatrix();
                }
                else {
                    RenderSystem.enableBlend();
                    RenderSystem.enableAlphaTest();
                    if (EvolutionConfig.CLIENT.hitmarkers.get()) {
                        if (this.killmarkerTick > 0) {
                            if (this.killmarkerTick > 10) {
                                blit(matrices, (scaledWidth - 17) / 2, (scaledHeight - 17) / 2, 102, 94, 17, 17);
                            }
                            else if (this.killmarkerTick > 5) {
                                blit(matrices, (scaledWidth - 17) / 2, (scaledHeight - 17) / 2, 120, 94, 17, 17);
                            }
                            else {
                                RenderSystem.color4f(1.0f, 1.0f, 1.0f, (this.killmarkerTick - partialTicks) / 5);
                                blit(matrices, (scaledWidth - 17) / 2, (scaledHeight - 17) / 2, 120, 94, 17, 17);
                            }
                        }
                        else if (this.hitmarkerTick > 0) {
                            if (this.hitmarkerTick <= 5) {
                                RenderSystem.color4f(1.0f, 1.0f, 1.0f, (this.hitmarkerTick - partialTicks) / 5);
                            }
                            blit(matrices, (scaledWidth - 17) / 2, (scaledHeight - 17) / 2, 84, 94, 17, 17);
                        }
                    }
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                    Blending.INVERTED_ADD.apply();
                    RenderSystem.blendEquation(GL14.GL_FUNC_SUBTRACT);
                    blit(matrices, (scaledWidth - 15) / 2, (scaledHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.mc.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float leftCooledAttackStrength = this.client.getMainhandCooledAttackStrength(partialTicks);
                        boolean shouldShowLeftAttackIndicator = false;
                        if (this.client.leftPointedEntity instanceof LivingEntity && leftCooledAttackStrength >= 1) {
                            shouldShowLeftAttackIndicator = this.mc.player.getCurrentItemAttackStrengthDelay() > 5;
                            shouldShowLeftAttackIndicator &= shouldShowAttackIndicator(this.client.leftPointedEntity);
                            if (this.mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {
                                shouldShowLeftAttackIndicator = false;
                            }
                        }
                        int sideOffset = this.mc.player.getMainArm() == HandSide.RIGHT ? 1 : -1;
                        int x = scaledWidth / 2 - 8;
                        x = offhandValid ? x + 10 * sideOffset : x;
                        int y = scaledHeight / 2 - 7 + 16;
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
                            float rightCooledAttackStrength = this.client.getOffhandCooledAttackStrength(this.mc.player.getOffhandItem().getItem(),
                                                                                                         partialTicks);
                            if (this.client.rightPointedEntity instanceof LivingEntity && rightCooledAttackStrength >= 1) {
                                shouldShowRightAttackIndicator = shouldShowAttackIndicator(this.client.rightPointedEntity);
                                if (this.mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {
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
                        RenderSystem.disableAlphaTest();
                    }
                    RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                }
            }
        }
    }

    public void renderFog(EntityViewRenderEvent.FogDensity event) {
        if (this.mc.player != null && this.mc.player.hasEffect(Effects.BLINDNESS)) {
            float f1 = 5.0F;
            int duration = this.mc.player.getEffect(Effects.BLINDNESS).getDuration();
            int amplifier = this.mc.player.getEffect(Effects.BLINDNESS).getAmplifier() + 1;
            if (duration < 20) {
                f1 = 5.0F + (this.mc.options.renderDistance * 16 - 5.0F) * (1.0F - duration / 20.0F);
            }
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            float multiplier = 0.25F / amplifier;
            RenderSystem.fogStart(f1 * multiplier);
            RenderSystem.fogEnd(f1 * multiplier * 4.0F);
            if (GL.getCapabilities().GL_NV_fog_distance) {
                GL11.glFogi(0x855a, 0x855b);
            }
            event.setDensity(2.0F);
            event.setCanceled(true);
        }
    }

    public void renderFoodAndThirst(MatrixStack matrices) {
        this.mc.getProfiler().push("food");
        RenderSystem.enableBlend();
        final int width = this.mc.getWindow().getGuiScaledWidth();
        final int height = this.mc.getWindow().getGuiScaledHeight();
        int top = height - ForgeIngameGui.right_height;
        ForgeIngameGui.right_height += 10;
        FoodStats stats = this.mc.player.getFoodData();
        int level = stats.getFoodLevel();
        float saturation = stats.getSaturationLevel();
        int left = width / 2 + 91;
        for (int i = 0; i < 10; ++i) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int icon = 16;
            byte background = 0;
            if (this.mc.player.hasEffect(Effects.HUNGER)) {
                icon += 36;
                background = 13;
            }
            int y = top;
            if (saturation <= 0.0F && this.client.getTickCount() % Math.max(level * level, 1) == 0) {
                y = top + this.rand.nextInt(3) - 1;
            }
            blit(matrices, x, y, 16 + background * 9, 27, 9, 9);
            if (idx < level) {
                blit(matrices, x, y, icon + 36, 27, 9, 9);
            }
            else if (idx == level) {
                blit(matrices, x, y, icon + 45, 27, 9, 9);
            }
        }
        this.mc.getProfiler().popPush("thirst");
        PlayerEntity player = (PlayerEntity) this.mc.getCameraEntity();
        IThirst thirst = ThirstStats.CLIENT_INSTANCE;
        top = height - ForgeIngameGui.right_height;
        level = MathHelper.ceil(thirst.getThirstLevel() / (ThirstStats.THIRST_SCALE / 2));
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
        ItemStack heldStack = player.getMainHandItem();
        if (!(heldStack.getItem() instanceof IDrink)) {
            heldStack = player.getOffhandItem();
        }
        left = width / 2 + 91;
        top = height - ForgeIngameGui.right_height;
        ForgeIngameGui.right_height += 10;
        //Hydration overlay
        this.drawHydrationOverlay(matrices, 0, thirst.getHydrationLevel(), left, top, 1.0f);
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof IDrink)) {
            this.flashAlpha = 0;
            this.alphaDir = 1;
            return;
        }
        //Restored thirst/hydration overlay while holding drink
        IDrink drink = (IDrink) heldStack.getItem();
        this.drawThirstOverlay(matrices, drink.getThirst(), thirst.getThirstLevel(), left, top, this.flashAlpha);
        this.drawHydrationOverlay(matrices, drink.getThirst(), thirst.getHydrationLevel(), left, top, this.flashAlpha);
        RenderSystem.disableBlend();
        this.mc.getProfiler().pop();
    }

    public void renderHealth(MatrixStack matrices) {
        this.mc.getProfiler().push("health");
        this.mc.getTextureManager().bind(EvolutionResources.GUI_ICONS);
        int width = this.mc.getWindow().getGuiScaledWidth();
        int height = this.mc.getWindow().getGuiScaledHeight();
        RenderSystem.enableBlend();
        PlayerEntity player = (PlayerEntity) this.mc.getCameraEntity();
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
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());
        int healthRows = MathHelper.ceil((healthMax + absorb) / 100.0F);
        int top = height - ForgeIngameGui.left_height;
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        ForgeIngameGui.left_height += healthRows * rowHeight;
        if (rowHeight != 10) {
            ForgeIngameGui.left_height += 10 - rowHeight;
        }
        int regen = -1;
        if (player.hasEffect(Effects.REGENERATION)) {
            regen = this.client.getTickCount() % 25;
        }
        boolean hardcore = this.mc.level.getLevelData().isHardcore();
        final int heartTextureYPos = hardcore ? 45 : 0;
        final int absorbHeartTextureYPos = hardcore ? 36 : 9;
        final int heartBackgroundXPos = highlight ? 25 : 16;
        int margin = 16;
        if (player.hasEffect(Effects.POISON)) {
            margin += 72;
        }
        //TODO anaemia
        int absorbRemaining = MathHelper.ceil(absorb);
        this.rand.setSeed(312_871L * this.client.getTickCount());
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
                    case 1:
                    case 2:
                        blit(matrices, x, y, 169, absorbHeartTextureYPos, 9, 9);
                        break;
                    case 3:
                    case 4:
                    case 5:
                        blit(matrices, x, y, 160, absorbHeartTextureYPos, 9, 9);
                        break;
                    case 6:
                    case 7:
                        blit(matrices, x, y, 151, absorbHeartTextureYPos, 9, 9);
                        break;
                    case 8:
                    case 9:
                    case 0:
                        blit(matrices, x, y, 142, absorbHeartTextureYPos, 9, 9);
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

    public void renderHitbox(MatrixStack matrices,
                             IRenderTypeBuffer buffer,
                             Entity entity,
                             Hitbox hitbox,
                             ActiveRenderInfo camera,
                             float partialTicks) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
        RenderSystem.disableTexture();
        RenderSystem.scalef(1.0F, 1.0F, 0.999F);
        double projX = camera.getPosition().x;
        double projY = camera.getPosition().y;
        double projZ = camera.getPosition().z;
        double posX = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
        double posY = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
        double posZ = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
        drawHitbox(matrices,
                   buffer.getBuffer(RenderType.lines()),
                   hitbox,
                   posX - projX,
                   posY - projY,
                   posZ - projZ,
                   1.0F,
                   1.0F,
                   0.0F,
                   1.0F,
                   entity,
                   partialTicks);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderItemInFirstPerson(MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, float partialTicks) {
        if (this.client.shouldRenderPlayer() || !this.mc.player.isAlive()) {
            return;
        }
        AbstractClientPlayerEntity player = this.mc.player;
        float interpPitch = MathHelper.lerp(partialTicks, player.xRotO, player.xRot);
        boolean mainHand = true;
        boolean offHand = true;
        if (player.isUsingItem()) {
            ItemStack activeStack = player.getUseItem();
            if (activeStack.getItem() instanceof ShootableItem) {
                mainHand = player.getUsedItemHand() == Hand.MAIN_HAND;
                offHand = !mainHand;
            }
            Hand activeHand = player.getUsedItemHand();
            if (activeHand == Hand.MAIN_HAND) {
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
            float equippedProgress = 1.0F - MathHelper.lerp(partialTicks, this.mainhandPrevEquipProgress, this.mainhandEquipProgress);
            this.renderItemInFirstPerson(matrices,
                                         buffer,
                                         packedLight,
                                         player,
                                         partialTicks,
                                         interpPitch,
                                         Hand.MAIN_HAND,
                                         swingProg,
                                         this.mainHandStack,
                                         equippedProgress);
        }
        if (offHand) {
            float swingProg = this.getOffhandSwingProgress(partialTicks);
            float equippedProgress = 1.0F - MathHelper.lerp(partialTicks, this.offhandPrevEquipProgress, this.offhandEquipProgress);
            this.renderItemInFirstPerson(matrices,
                                         buffer,
                                         packedLight,
                                         player,
                                         partialTicks,
                                         interpPitch,
                                         Hand.OFF_HAND,
                                         swingProg,
                                         this.offhandStack,
                                         equippedProgress);
        }
        RenderSystem.disableRescaleNormal();
        RenderHelper.turnOff();
    }

    public void renderItemInFirstPerson(MatrixStack matrices,
                                        IRenderTypeBuffer buffer,
                                        int packedLight,
                                        AbstractClientPlayerEntity player,
                                        float partialTicks,
                                        float pitch,
                                        Hand hand,
                                        float swingProgress,
                                        ItemStack stack,
                                        float equippedProgress) {
        boolean mainHand = hand == Hand.MAIN_HAND;
        HandSide handSide = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        matrices.pushPose();
        if (stack.isEmpty()) {
            if (mainHand && !player.isInvisible()) {
                this.renderArmFirstPerson(matrices, buffer, packedLight, equippedProgress, swingProgress, handSide);
            }
        }
        else if (stack.getItem() instanceof FilledMapItem) {
            if (mainHand && this.offhandStack.isEmpty()) {
                this.renderMapFirstPerson(matrices, buffer, packedLight, pitch, equippedProgress, swingProgress);
            }
            else {
                this.renderMapFirstPersonSide(matrices, buffer, packedLight, equippedProgress, handSide, swingProgress, stack);
            }
        }
        else if (stack.getItem() == Items.CROSSBOW) {
            boolean isCrossbowCharged = CrossbowItem.isCharged(stack);
            boolean isRightSide = handSide == HandSide.RIGHT;
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
            this.renderItemSide(matrices,
                                buffer,
                                packedLight,
                                OverlayTexture.NO_OVERLAY,
                                player,
                                stack,
                                isRightSide ?
                                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                                ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !isRightSide);
        }
        else {
            boolean rightSide = handSide == HandSide.RIGHT;
            if (player.isUsingItem() && player.getTicksUsingItem() > 0 && player.getUsedItemHand() == hand) {
                int sideOffset = rightSide ? 1 : -1;
                switch (stack.getUseAnimation()) {
                    case NONE: {
                        transformSideFirstPerson(matrices, handSide, equippedProgress);
                        break;
                    }
                    case BLOCK: {
                        if (stack.getItem() instanceof IParry) {
                            transformSideFirstPersonParry(matrices, handSide, equippedProgress);
                        }
                        else {
                            transformSideFirstPerson(matrices, handSide, equippedProgress);
                        }
                        break;
                    }
                    case EAT:
                    case DRINK: {
                        this.transformEatFirstPerson(matrices, partialTicks, handSide, stack);
                        transformSideFirstPerson(matrices, handSide, equippedProgress);
                        break;
                    }
                    case BOW: {
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
                        break;
                    }
                    case SPEAR: {
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
                if (hand == Hand.MAIN_HAND) {
                    if (InputHooks.isMainhandLungeInProgress) {
                        transformLungeFirstPerson(matrices, partialTicks, hand, handSide, stack);
                    }
                    else if (InputHooks.isMainhandLunging) {
                        this.transformLungingFirstPerson(matrices, partialTicks, hand, stack);
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
            this.renderItemSide(matrices,
                                buffer,
                                packedLight,
                                OverlayTexture.NO_OVERLAY,
                                player,
                                stack,
                                rightSide ?
                                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                                ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !rightSide);
        }
        matrices.popPose();
    }

    public void renderItemSide(MatrixStack matrices,
                               IRenderTypeBuffer buffer,
                               int packedLight,
                               int packedOverlay,
                               LivingEntity entity,
                               ItemStack stack,
                               ItemCameraTransforms.TransformType transform,
                               boolean leftHanded) {
        if (!stack.isEmpty()) {
            this.itemRenderer.renderStatic(entity, stack, transform, leftHanded, matrices, buffer, entity.level, packedLight, packedOverlay);
        }
    }

    private void renderMapFirstPerson(MatrixStack matrices,
                                      IRenderTypeBuffer buffer,
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
            this.renderArm(matrices, buffer, packedLight, HandSide.RIGHT);
            this.renderArm(matrices, buffer, packedLight, HandSide.LEFT);
            matrices.popPose();
        }
        float f4 = MathHelper.sin(sqrtSwingProg * MathHelper.PI);
        matrices.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
        matrices.scale(2.0F, 2.0F, 2.0F);
        this.renderMapFirstPerson(matrices, buffer, packedLight, this.mainHandStack);
    }

    private void renderMapFirstPerson(MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, ItemStack stack) {
        matrices.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrices.scale(0.38F, 0.38F, 0.38F);
        matrices.translate(-0.5, -0.5, 0);
        matrices.scale(0.007_812_5F, 0.007_812_5F, 0.007_812_5F);
        MapData mapData = FilledMapItem.getSavedData(stack, this.mc.level);
        //noinspection VariableNotUsedInsideIf
        IVertexBuilder builder = buffer.getBuffer(mapData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f mat = matrices.last().pose();
        builder.vertex(mat, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(packedLight).endVertex();
        builder.vertex(mat, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(packedLight).endVertex();
        builder.vertex(mat, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(packedLight).endVertex();
        builder.vertex(mat, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(packedLight).endVertex();
        if (mapData != null) {
            this.mc.gameRenderer.getMapRenderer().render(matrices, buffer, mapData, false, packedLight);
        }
    }

    private void renderMapFirstPersonSide(MatrixStack matrices,
                                          IRenderTypeBuffer buffer,
                                          int packedLight,
                                          float equippedProgress,
                                          HandSide hand,
                                          float swingProgress,
                                          ItemStack stack) {
        float sideOffset = hand == HandSide.RIGHT ? 1.0F : -1.0F;
        matrices.translate(sideOffset * 0.125F, -0.125F, 0.0F);
        if (!this.mc.player.isInvisible()) {
            matrices.pushPose();
            matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * 10.0F));
            this.renderArmFirstPerson(matrices, buffer, packedLight, equippedProgress, swingProgress, hand);
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

    public void renderOutlines(MatrixStack matrices, IRenderTypeBuffer buffer, VoxelShape shape, ActiveRenderInfo info, BlockPos pos) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
        RenderSystem.disableTexture();
        matrices.pushPose();
        RenderSystem.scalef(1.0F, 1.0F, 1.0F);
        double projX = info.getPosition().x;
        double projY = info.getPosition().y;
        double projZ = info.getPosition().z;
        drawShape(matrices,
                  buffer.getBuffer(RenderType.LINES),
                  shape,
                  pos.getX() - projX,
                  pos.getY() - projY,
                  pos.getZ() - projZ,
                  1.0F,
                  1.0F,
                  0.0F,
                  1.0F);
        matrices.popPose();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderPotionIcons(MatrixStack matrices, float partialTicks) {
        PotionSpriteUploader potionSprite = this.mc.getMobEffectTextures();
        ClientEffectInstance movingInstance = null;
        Runnable runnable = null;
        while (!ClientEvents.EFFECTS_TO_ADD.isEmpty()) {
            ClientEffectInstance addingInstance = ClientEvents.EFFECTS_TO_ADD.get(0);
            Effect addingEffect = addingInstance.getEffect();
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
            float x0 = (this.mc.getWindow().getGuiScaledWidth() - 24) / 2.0f;
            float y0 = Math.max((this.mc.getWindow().getGuiScaledHeight() - 24) / 3.0f, 1 + 26 * 3 + 12 + (this.mc.isDemo() ? 15 : 0));
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
                float x1 = this.mc.getWindow().getGuiScaledWidth() - 25 * this.movingFinalCount;
                float t = (this.client.effectToAddTicks - 15 + partialTicks) / 5.0f;
                t = MathHelper.clamp(t, 0, 1);
                x = (x1 - x0) * t + x0;
                float y1 = this.getYPosForEffect(addingEffect);
                y = (y1 - y0) * t + y0;
            }
            float finalX = x;
            float finalY = y;
            runnable = () -> {
                this.mc.getTextureManager().bind(EvolutionResources.GUI_INVENTORY);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                if (addingInstance.isAmbient()) {
                    floatBlit(matrices, finalX, finalY, 24, 198, 24, 24, -80);
                }
                else {
                    floatBlit(matrices, finalX, finalY, 0, 198, 24, 24, -80);
                }
                TextureAtlasSprite atlasSprite = potionSprite.get(addingEffect);
                this.mc.getTextureManager().bind(atlasSprite.atlas().location());
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                floatBlit(matrices, finalX + 3, finalY + 3, -79, 18, 18, atlasSprite);
                if (addingInstance.getAmplifier() != 0) {
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(0.5f, 0.5f, 0.5f);
                    this.mc.font.drawShadow(matrices,
                                            MathHelper.getRomanNumber(ScreenDisplayEffects.getFixedAmplifier(addingInstance) + 1),
                                            (finalX + 3) * 2,
                                            (finalY + 17) * 2,
                                            0xffff_ffff);
                    RenderSystem.popMatrix();
                }
            };
            if (this.client.effectToAddTicks >= 20) {
                movingInstance = null;
                this.client.effectToAddTicks = 0;
                this.isAddingEffect = false;
                ClientEvents.removeEffect(ClientEvents.EFFECTS, addingEffect.getEffect());
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
            List<ClientEffectInstance> effects = new ArrayList<>(ClientEvents.EFFECTS);
            EffectType movingType = null;
            Effect repeated = null;
            if (movingInstance != null) {
                if (ClientEvents.containsEffect(effects, movingInstance.getEffect())) {
                    repeated = movingInstance.getEffect();
                }
                else {
                    effects.add(movingInstance);
                    movingType = movingInstance.getEffect().getCategory();
                }
            }
            Collections.sort(effects);
            Collections.reverse(effects);
            int beneficalCount = 0;
            int neutralCount = 0;
            int harmfulCount = 0;
            boolean isMoving = false;
            List<Runnable> runnables = new ArrayList<>(effects.size());
            for (ClientEffectInstance effectInstance : effects) {
                if (!effectInstance.isShowIcon()) {
                    continue;
                }
                Effect effect = effectInstance.getEffect();
                if (effectInstance == movingInstance) {
                    isMoving = true;
                }
                float x = this.mc.getWindow().getGuiScaledWidth();
                float y = 1;
                if (this.mc.isDemo()) {
                    y += 15;
                }
                switch (effect.getCategory()) {
                    case BENEFICIAL: {
                        beneficalCount++;
                        x -= 25 * beneficalCount;
                        if (effectInstance == movingInstance || effect == repeated) {
                            this.movingFinalCount = beneficalCount;
                        }
                        if (isMoving && movingType == EffectType.BENEFICIAL) {
                            x += 25 * MathHelper.clampMax(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                        }
                        break;
                    }
                    case NEUTRAL: {
                        neutralCount++;
                        x -= 25 * neutralCount;
                        if (this.lastBeneficalCount > 0) {
                            y += 26;
                            if (this.lastBeneficalCount == 1 && movingType == EffectType.BENEFICIAL) {
                                y -= 26 * MathHelper.clampMax(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                            }
                        }
                        if (effectInstance == movingInstance || effect == repeated) {
                            this.movingFinalCount = neutralCount;
                        }
                        if (isMoving && movingType == EffectType.NEUTRAL) {
                            x += 25 * MathHelper.clampMax(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                        }
                        break;
                    }
                    case HARMFUL: {
                        harmfulCount++;
                        x -= 25 * harmfulCount;
                        if (this.lastBeneficalCount > 0) {
                            y += 26;
                            if (this.lastBeneficalCount == 1 && movingType == EffectType.BENEFICIAL) {
                                y -= 26 * MathHelper.clampMax(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                            }
                        }
                        if (this.lastNeutralCount > 0) {
                            y += 26;
                            if (this.lastNeutralCount == 1 && movingType == EffectType.NEUTRAL) {
                                y -= 26 * MathHelper.clampMax(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                            }
                        }
                        if (effectInstance == movingInstance || effect == repeated) {
                            this.movingFinalCount = harmfulCount;
                        }
                        if (isMoving && movingType == EffectType.HARMFUL) {
                            x += 25 * MathHelper.clampMax(1.0f - (this.client.effectToAddTicks + partialTicks - 15) / 5.0f, 1);
                        }
                        break;
                    }
                }
                if (effectInstance != movingInstance && !this.mc.options.renderDebug) {
                    this.mc.getTextureManager().bind(ContainerScreen.INVENTORY_LOCATION);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
                    TextureAtlasSprite atlasSprite = potionSprite.get(effect);
                    this.mc.getTextureManager().bind(atlasSprite.atlas().location());
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
                    floatBlit(matrices, x + 3, y + 3, 0, 18, 18, atlasSprite);
                    if (effectInstance.getAmplifier() != 0) {
                        float finalX = x;
                        float finalY = y;
                        //noinspection ObjectAllocationInLoop
                        runnables.add(() -> this.mc.font.drawShadow(matrices,
                                                                    MathHelper.getRomanNumber(ScreenDisplayEffects.getFixedAmplifier(effectInstance) +
                                                                                              1),
                                                                    (finalX + 3) * 2,
                                                                    (finalY + 17) * 2,
                                                                    0xff_ffff));
                    }
                }
            }
            for (Runnable run : runnables) {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);
                if (run != null) {
                    run.run();
                }
                RenderSystem.popMatrix();
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

    public void renderTooltip(RenderTooltipEvent.PostText event) {
        MatrixStack matrices = event.getMatrixStack();
        ItemStack stack = event.getStack();
        Item item = stack.getItem();
        if (item instanceof IEvolutionItem) {
            RenderSystem.pushMatrix();
            RenderSystem.color3f(1.0F, 1.0F, 1.0F);
            Minecraft mc = Minecraft.getInstance();
            mc.getTextureManager().bind(EvolutionResources.GUI_ICONS);
            boolean hasMass = false;
            boolean hasDamage = false;
            boolean hasSpeed = false;
            boolean hasReach = false;
            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                Multimap<Attribute, AttributeModifier> multimap = stack.getAttributeModifiers(slot);
                if (!multimap.isEmpty()) {
                    for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        if (attributemodifier.getId().compareTo(EvolutionAttributes.ATTACK_DAMAGE_MODIFIER) == 0) {
                            hasDamage = true;
                            continue;
                        }
                        if (attributemodifier.getId().compareTo(EvolutionAttributes.ATTACK_SPEED_MODIFIER) == 0) {
                            hasSpeed = true;
                            continue;
                        }
                        if (attributemodifier.getId() == EvolutionAttributes.REACH_DISTANCE_MODIFIER) {
                            hasReach = true;
                            continue;
                        }
                        if (attributemodifier.getId() == EvolutionAttributes.MASS_MODIFIER ||
                            attributemodifier.getId() == EvolutionAttributes.MASS_MODIFIER_OFFHAND) {
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
                if (stack.getTag().contains("display", NBTTypes.COMPOUND_NBT)) {
                    CompoundNBT display = stack.getTag().getCompound("display");
                    if (display.contains("color", NBTTypes.INT)) {
                        line++;
                    }
                    if (display.getTagType("Lore") == NBTTypes.LIST_NBT) {
                        for (int j = 0; j < display.getList("Lore", NBTTypes.STRING).size(); ++j) {
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
                blit(matrices, x, y, textureX, textureY, 9, 9);
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
            //Consumable
            if (item instanceof IConsumable) {
                if (hasAddedLine) {
                    line++;
                }
                else {
                    line += 2;
                }
                hasAddedLine = true;
                if (item instanceof IFood) {
                    int x = event.getX() + 4;
                    int y = shiftTextByLines(line, event.getY() + 10);
                    int textureX = 81;
                    int textureY = 247;
                    blit(matrices, x, y, textureX, textureY, 9, 9);
                    line++;
                }
                if (item instanceof IDrink) {
                    int x = event.getX() + 4;
                    int y = shiftTextByLines(line, event.getY() + 10);
                    int textureX = 90;
                    int textureY = 247;
                    blit(matrices, x, y, textureX, textureY, 9, 9);
                    line++;
                }
            }
            //Attributes
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
                blit(matrices, x, y, textureX, textureY, 9, 9);
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
                blit(matrices, x, y, textureX, textureY, 9, 9);
                line++;
            }
            //Reach distance
            if (hasReach) {
                int x = event.getX() + 4;
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 18;
                int textureY = 247;
                blit(matrices, x, y, textureX, textureY, 9, 9);
                line++;
            }
            //Mining Speed
            if (item instanceof ItemGenericTool && ((ItemGenericTool) item).getEfficiency() > 0) {
                int x = event.getX() + 4;
                int y = shiftTextByLines(line, event.getY() + 10);
                int textureX = 9;
                int textureY = 247;
                blit(matrices, x, y, textureX, textureY, 9, 9);
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
                blit(matrices, x, y, textureX, textureY, 9, 9);
            }
            RenderSystem.popMatrix();
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

    public void startTick() {
        this.mainhandPrevEquipProgress = this.mainhandEquipProgress;
        this.offhandPrevEquipProgress = this.offhandEquipProgress;
        this.mainhandPrevSwingProgress = this.mainhandSwingProgress;
        this.offhandPrevSwingProgress = this.offhandSwingProgress;
        this.updateArmSwingProgress();
        ClientPlayerEntity player = this.mc.player;
        ItemStack mainhandStack = player.getMainHandItem();
        ItemStack offhandStack = player.getOffhandItem();
        if (player.isHandsBusy()) {
            this.mainhandEquipProgress = MathHelper.clamp(this.mainhandEquipProgress - 0.4F, 0.0F, 1.0F);
            this.offhandEquipProgress = MathHelper.clamp(this.offhandEquipProgress - 0.4F, 0.0F, 1.0F);
        }
        else {
            boolean requipM = shouldCauseReequipAnimation(this.mainHandStack, mainhandStack, player.inventory.selected);
            boolean requipO = shouldCauseReequipAnimation(this.offhandStack, offhandStack, -1);
            if (requipM) {
                this.client.mainhandTimeSinceLastHit = 0;
                if (!ItemStack.matches(this.currentMainhandItem, mainhandStack)) {
                    this.currentMainhandItem = mainhandStack;
                    if (this.currentMainhandItem.getItem() instanceof ItemSword) {
                        this.mc.getSoundManager()
                               .play(new SoundEntityEmitted(this.mc.player,
                                                            EvolutionSounds.SWORD_UNSHEATHE.get(),
                                                            SoundCategory.PLAYERS,
                                                            0.8f,
                                                            1.0f));
                        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlaySoundEntityEmitted(this.mc.player,
                                                                                                  EvolutionSounds.SWORD_UNSHEATHE.get(),
                                                                                                  SoundCategory.PLAYERS,
                                                                                                  0.8f,
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
                               .play(new SoundEntityEmitted(this.mc.player,
                                                            EvolutionSounds.SWORD_UNSHEATHE.get(),
                                                            SoundCategory.PLAYERS,
                                                            0.8f,
                                                            1.0f));
                        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSPlaySoundEntityEmitted(this.mc.player,
                                                                                                  EvolutionSounds.SWORD_UNSHEATHE.get(),
                                                                                                  SoundCategory.PLAYERS,
                                                                                                  0.8f,
                                                                                                  1.0f));
                    }
                }
            }
            else {
                this.offhandStack = offhandStack.copy();
                this.client.offhandTimeSinceLastHit++;
            }
            float cooledAttackStrength = this.client.getOffhandCooledAttackStrength(this.mc.player.getOffhandItem().getItem(), 1.0F);
            this.offhandEquipProgress += MathHelper.clamp((!requipO ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) -
                                                          this.offhandEquipProgress, -0.4f, 0.4F);
            cooledAttackStrength = this.client.getMainhandCooledAttackStrength(1.0F);
            this.mainhandEquipProgress += MathHelper.clamp((!requipM ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) -
                                                           this.mainhandEquipProgress, -0.4F, 0.4F);

        }
        if (this.mainhandEquipProgress < 0.1F && !InputHooks.isMainhandLunging) {
            this.mainHandStack = mainhandStack.copy();
        }
        if (this.offhandEquipProgress < 0.1F && !InputHooks.isOffhandLunging) {
            this.offhandStack = offhandStack.copy();
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

    private void transformEatFirstPerson(MatrixStack matrices, float partialTicks, HandSide hand, ItemStack stack) {
        float useTime = this.mc.player.getTicksUsingItem() - partialTicks + 1.0F;
        float relativeUse = useTime / stack.getUseDuration();
        if (relativeUse < 0.8F) {
            float f2 = Math.abs(MathHelper.cos(useTime / 4.0F * MathHelper.PI) * 0.1F);
            matrices.translate(0, f2, 0);
        }
        float f3 = 1.0F - (float) Math.pow(relativeUse, 27);
        int sideOffset = hand == HandSide.RIGHT ? 1 : -1;
        matrices.translate(f3 * 0.6 * sideOffset, f3 * -0.5, 0);
        matrices.mulPose(Vector3f.YP.rotationDegrees(sideOffset * f3 * 90.0F));
        matrices.mulPose(Vector3f.XP.rotationDegrees(f3 * 10.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(sideOffset * f3 * 30.0F));
    }

    private void transformLungingFirstPerson(MatrixStack matrices, float partialTicks, Hand hand, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ILunge) {
            if (hand == Hand.MAIN_HAND) {
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
