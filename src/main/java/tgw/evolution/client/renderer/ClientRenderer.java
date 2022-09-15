package tgw.evolution.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.jetbrains.annotations.Nullable;
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
import tgw.evolution.init.*;
import tgw.evolution.items.IDrink;
import tgw.evolution.items.IFood;
import tgw.evolution.items.IMelee;
import tgw.evolution.network.PacketCSPlaySoundEntityEmitted;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;
import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.HitboxEntity;
import tgw.evolution.util.math.MathHelper;

import java.util.Collections;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientRenderer {

    private static @Nullable ClientRenderer instance;
    private static int slotMainHand;
    private final ClientEvents client;
    private final OList<ClientEffectInstance> effects = new OArrayList<>();
    private final Minecraft mc;
    private final Random rand = new Random();
    private final RList<Runnable> runnables = new RArrayList<>();
    public boolean isAddingEffect;
    public boolean isRenderingPlayer;
    public boolean shouldRenderLeftArm = true;
    public boolean shouldRenderRightArm = true;
    private ItemStack currentMainhandStack = ItemStack.EMPTY;
    private ItemStack currentOffhandStack = ItemStack.EMPTY;
    private byte healthFlashTicks;
    private short healthTick;
    private byte hitmarkerTick;
    private byte hungerAlphaMult = 1;
    private float hungerFlashAlpha;
    private byte hungerFlashTicks;
    private short hungerTick;
    private byte killmarkerTick;
    private short lastBeneficalCount;
    private int lastDisplayedHealth;
    private byte lastDisplayedHunger;
    private byte lastDisplayedThirst;
    private short lastNeutralCount;
    private int lastPlayerHealth;
    private ItemStack mainHandStack = ItemStack.EMPTY;
    private float mainhandEquipProgress;
    private boolean mainhandIsSwingInProgress;
    private int mainhandSwingProgressInt;
    private short movingFinalCount;
    private float offhandEquipProgress;
    private boolean offhandIsSwingInProgress;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private int offhandSwingProgressInt;
    private byte thirstAlphaMult = 1;
    private float thirstFlashAlpha;
    private byte thirstFlashTicks;
    private short thirstTick;

    public ClientRenderer(Minecraft mc, ClientEvents client) {
        instance = this;
        this.mc = mc;
        this.client = client;
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

    public static <T extends Entity> void drawHitbox(PoseStack matrices,
                                                     VertexConsumer buffer,
                                                     Hitbox hitbox,
                                                     float x,
                                                     float y,
                                                     float z,
                                                     float red,
                                                     float green,
                                                     float blue,
                                                     float alpha,
                                                     T entity,
                                                     float partialTicks) {
        HitboxEntity hitboxes = ((IEntityPatch) entity).getHitboxes();
        if (hitboxes == null) {
            return;
        }
        assert Minecraft.getInstance().player != null;
        boolean renderAll = Minecraft.getInstance().player.getMainHandItem().getItem() == EvolutionItems.DEBUG_ITEM.get() ||
                            Minecraft.getInstance().player.getOffhandItem().getItem() == EvolutionItems.DEBUG_ITEM.get();
        if (!renderAll) {
            hitboxes.drawBox(hitbox, entity, partialTicks, buffer, matrices, x, y, z, red, green, blue, alpha);
        }
        else {
            hitboxes.drawAllBoxes(entity, partialTicks, buffer, matrices, x, y, z);
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
            float norm = Mth.fastInvSqrt(nx * nx + ny * ny + nz * nz);
            nx *= norm;
            ny *= norm;
            nz *= norm;
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
        Blending.DEFAULT.apply();
    }

    private static void floatBlit(PoseStack matrices, float x, float y, int textureX, int textureY, int sizeX, int sizeY, int blitOffset) {
        GUIUtils.floatBlit(matrices, x, y, blitOffset, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    public static void floatBlit(PoseStack matrices, float x, float y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        GUIUtils.innerFloatBlit(matrices.last().pose(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(),
                                sprite.getV1());
    }

    public static ClientRenderer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ClientRenderer not initialized!");
        }
        return instance;
    }

    private static int roundToHearts(float currentHealth) {
        return Mth.ceil(currentHealth / 2.5f);
    }

    public static boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, int slot) {
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

    public void endTick() {
        this.hungerFlashAlpha += this.hungerAlphaMult * 0.125f;
        this.thirstFlashAlpha += this.thirstAlphaMult * 0.125f;
        if (this.hungerFlashAlpha >= 1.5f) {
            this.hungerFlashAlpha = 1.0f;
            this.hungerAlphaMult = -1;
        }
        else if (this.hungerFlashAlpha <= -0.5f) {
            this.hungerFlashAlpha = 0.0f;
            this.hungerAlphaMult = 1;
        }
        if (this.thirstFlashAlpha >= 1.5f) {
            this.thirstFlashAlpha = 1.0f;
            this.thirstAlphaMult = -1;
        }
        else if (this.thirstFlashAlpha <= -0.5f) {
            this.thirstFlashAlpha = 0.0f;
            this.thirstAlphaMult = 1;
        }
        if (this.hitmarkerTick >= 0) {
            this.hitmarkerTick--;
        }
        if (this.killmarkerTick >= 0) {
            this.killmarkerTick--;
        }
        if (this.healthFlashTicks > 0) {
            this.healthFlashTicks--;
        }
        if (this.hungerFlashTicks > 0) {
            this.hungerFlashTicks--;
        }
        if (this.thirstFlashTicks > 0) {
            this.thirstFlashTicks--;
        }
        if (this.healthTick > 0) {
            this.healthTick--;
        }
        if (this.hungerTick > 0) {
            this.hungerTick--;
        }
        if (this.thirstTick > 0) {
            this.thirstTick--;
        }
    }

    private int getArmSwingAnimationEnd() {
        assert this.mc.player != null;
        if (MobEffectUtil.hasDigSpeed(this.mc.player)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(this.mc.player));
        }
        MobEffectInstance effect = this.mc.player.getEffect(MobEffects.DIG_SLOWDOWN);
        return effect != null ? 6 + (1 + effect.getAmplifier()) * 2 : 6;
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

    private boolean rayTraceMouse(@Nullable HitResult rayTraceResult) {
        if (rayTraceResult == null) {
            return false;
        }
        if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) rayTraceResult).getEntity() instanceof InventoryCarrier;
        }
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult) rayTraceResult).getBlockPos();
            Level level = this.mc.level;
            assert level != null;
            return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
        }
        return false;
    }

    public void renderBlockOutlines(PoseStack matrices, MultiBufferSource buffer, Camera camera, BlockPos hitPos) {
        assert this.mc.level != null;
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
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        if (options.getCameraType() == CameraType.FIRST_PERSON) {
            assert this.mc.gameMode != null;
            assert this.mc.player != null;
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
                    if (EvolutionConfig.CLIENT.hitmarkers.get()) {
                        if (this.killmarkerTick >= 0) {
                            if (this.killmarkerTick >= 10) {
                                blit(matrices, (width - 17) / 2, (height - 17) / 2, 2 * 17, EvolutionResources.ICON_17_17, 17, 17);
                            }
                            else if (this.killmarkerTick >= 5) {
                                blit(matrices, (width - 17) / 2, (height - 17) / 2, 3 * 17, EvolutionResources.ICON_17_17, 17, 17);
                            }
                            else {
                                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (this.killmarkerTick + partialTicks) / 5);
                                blit(matrices, (width - 17) / 2, (height - 17) / 2, 3 * 17, EvolutionResources.ICON_17_17, 17, 17);
                            }
                        }
                        else if (this.hitmarkerTick >= 0) {
                            if (this.hitmarkerTick < 5) {
                                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (this.hitmarkerTick + partialTicks) / 5);
                            }
                            blit(matrices, (width - 17) / 2, (height - 17) / 2, 17, EvolutionResources.ICON_17_17, 17, 17);
                        }
                    }
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    Blending.INVERTED_ADD.apply();
                    RenderSystem.blendEquation(GL14.GL_FUNC_SUBTRACT);
                    blit(matrices, (width - 17) / 2, (height - 17) / 2, 0, EvolutionResources.ICON_17_17, 17, 17);
                    if (this.mc.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float leftCooledAttackStrength = this.client.getMainhandCooledAttackStrength(partialTicks);
                        boolean shouldShowLeftAttackIndicator = false;
                        if (this.client.leftPointedEntity instanceof LivingEntity && leftCooledAttackStrength >= 1) {
                            shouldShowLeftAttackIndicator = shouldShowAttackIndicator(this.client.leftPointedEntity);
                            assert this.mc.hitResult != null;
                            if (this.mc.hitResult.getType() == HitResult.Type.BLOCK) {
                                shouldShowLeftAttackIndicator = false;
                            }
                        }
                        int x = width / 2 - 8;
                        int y = height / 2 - 7 + 17;
                        if (shouldShowLeftAttackIndicator) {
                            blit(matrices, x, y, 6 * 17, EvolutionResources.ICON_17_17, 17, 17);
                        }
                        else if (leftCooledAttackStrength < 1.0F) {
                            int l = (int) (leftCooledAttackStrength * 18.0F);
                            blit(matrices, x, y, 4 * 17, EvolutionResources.ICON_17_17, 17, 17);
                            blit(matrices, x, y, 5 * 17, EvolutionResources.ICON_17_17, l, 17);
                        }
                    }
                    RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                }
            }
        }
    }

    public void renderFoodAndThirst(PoseStack matrices, int width, int height) {
        this.mc.getProfiler().push("food");
        //Preparations
        LivingEntity player = this.mc.player;
        if (player != null) {
            ForgeIngameGui gui = (ForgeIngameGui) this.mc.gui;
            int top = height - gui.right_height;
            gui.right_height += 10;
            int left = width / 2 + 91;
            RenderSystem.enableBlend();
            //Holding item
            ItemStack heldStack = player.getMainHandItem();
            int holdingValue = 0;
            if (!(heldStack.getItem() instanceof IFood)) {
                heldStack = player.getOffhandItem();
            }
            if (heldStack.isEmpty() || !(heldStack.getItem() instanceof IFood food)) {
                this.hungerFlashAlpha = 0;
                this.hungerAlphaMult = 1;
            }
            else {
                holdingValue = food.getHunger();
            }
            //Values
            IHunger hunger = HungerStats.CLIENT_INSTANCE;
            int value = hunger.getHungerLevel();
            int extraValue = hunger.getSaturationLevel();
            int level = HungerStats.hungerLevel(value);
            int futureLevel = HungerStats.hungerLevel(Math.min(value + extraValue, HungerStats.HUNGER_CAPACITY));
            int holdingLevel = HungerStats.hungerLevel(Math.min(value + extraValue + holdingValue, HungerStats.HUNGER_CAPACITY));
            int extraLevel = HungerStats.saturationLevel(extraValue);
            int extraHoldingLevel = HungerStats.saturationLevel(Math.min(extraValue + holdingValue, HungerStats.SATURATION_CAPACITY));
            boolean shake = this.client.getTickCount() % Math.max(level * level, 1) == 0;
            //Flash
            if (level > this.lastDisplayedHunger) {
                this.hungerFlashTicks = 11; //Two flashes that start immediately
                this.hungerTick = 1_000;
                this.lastDisplayedHunger = (byte) level;
            }
            else if (this.hungerTick == 0) {
                this.lastDisplayedHunger = (byte) level;
                this.thirstTick = 1_000;
            }
            boolean flash = this.hungerFlashTicks > 0 && this.hungerFlashTicks / 3 % 2 != 0;
            //Rendering
            int icon = 9 * 3;
            int extraIcon = 9 * 11;
            if (extraLevel > 26) {
                extraIcon += 9 * 8;
            }
            else if (extraLevel > 13) {
                extraIcon += 9 * 4;
            }
            int extraHoldingIcon = 9 * 11;
            if (extraHoldingLevel > 26) {
                extraHoldingIcon += 9 * 8;
            }
            else if (extraHoldingLevel > 13) {
                extraHoldingIcon += 9 * 4;
            }
            int background = 0;
            if (this.mc.player.hasEffect(MobEffects.HUNGER)) {
                icon += 9 * 4;
                background = 9 * 2;
            }
            if (flash) {
                background = 9;
            }
            for (int i = 0; i < 10; i++) {
                int baseline = i * 2 + 1;
                int extraBaseline = i * 4 + 1;
                int x = left - i * 8 - 9;
                int aligment;
                if (shake) {
                    aligment = (byte) (this.rand.nextInt(3) - 1);
                }
                else {
                    aligment = 0;
                }
                int y = top + aligment;
                blit(matrices, x, y, background, EvolutionResources.ICON_HUNGER, 9, 9);
                if (holdingLevel > futureLevel) {
                    icon += 9 * 2;
                    enableAlpha(this.hungerFlashAlpha);
                    if (holdingLevel > baseline) {
                        blit(matrices, x, y, icon, EvolutionResources.ICON_HUNGER, 9, 9);
                    }
                    else if (holdingLevel == baseline) {
                        blit(matrices, x, y, icon + 9, EvolutionResources.ICON_HUNGER, 9, 9);
                    }
                    disableAlpha(this.hungerFlashAlpha);
                    icon -= 9 * 2;
                }
                if (futureLevel > level) {
                    icon += 9 * 2;
                    if (futureLevel > baseline) {
                        blit(matrices, x, y, icon, EvolutionResources.ICON_HUNGER, 9, 9);
                    }
                    else if (futureLevel == baseline) {
                        blit(matrices, x, y, icon + 9, EvolutionResources.ICON_HUNGER, 9, 9);
                    }
                    icon -= 9 * 2;
                }
                if (level > baseline) {
                    blit(matrices, x, y, icon, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                else if (level == baseline) {
                    blit(matrices, x, y, icon + 9, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                //Saturation
                if (extraLevel > extraBaseline) {
                    int offset = switch (extraLevel - extraBaseline) {
                        case 1 -> 9;
                        case 2 -> 9 * 2;
                        default -> 9 * 3;
                    };
                    blit(matrices, x, y, extraIcon + offset, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                else if (extraLevel == extraBaseline) {
                    blit(matrices, x, y, extraIcon, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                if (extraHoldingLevel > extraLevel) {
                    enableAlpha(this.hungerFlashAlpha);
                    if (extraHoldingLevel > extraBaseline) {
                        int offset = switch (extraHoldingLevel - extraBaseline) {
                            case 1 -> 9;
                            case 2 -> 9 * 2;
                            default -> 9 * 3;
                        };
                        blit(matrices, x, y, extraHoldingIcon + offset, EvolutionResources.ICON_HUNGER, 9, 9);
                    }
                    else if (extraHoldingLevel == extraBaseline) {
                        blit(matrices, x, y, extraHoldingIcon, EvolutionResources.ICON_HUNGER, 9, 9);
                    }
                    disableAlpha(this.hungerFlashAlpha);
                }
            }
            this.mc.getProfiler().popPush("thirst");
            //Preparations
            top = height - gui.right_height;
            gui.right_height += 10;
            //Holding Item
            heldStack = player.getMainHandItem();
            holdingValue = 0;
            if (!(heldStack.getItem() instanceof IDrink)) {
                heldStack = player.getOffhandItem();
            }
            if (heldStack.isEmpty() || !(heldStack.getItem() instanceof IDrink drink)) {
                this.thirstFlashAlpha = 0;
                this.thirstAlphaMult = 1;
            }
            else {
                holdingValue = drink.getThirst();
            }
            //Values
            IThirst thirst = ThirstStats.CLIENT_INSTANCE;
            value = thirst.getThirstLevel();
            extraValue = thirst.getHydrationLevel();
            level = ThirstStats.thirstLevel(value);
            futureLevel = ThirstStats.thirstLevel(Math.min(value + extraValue, ThirstStats.THIRST_CAPACITY));
            holdingLevel = ThirstStats.thirstLevel(Math.min(value + extraValue + holdingValue, ThirstStats.THIRST_CAPACITY));
            extraLevel = ThirstStats.hydrationLevel(extraValue);
            extraHoldingLevel = ThirstStats.hydrationLevel(Math.min(extraValue + holdingValue, ThirstStats.HYDRATION_CAPACITY));
            shake = this.client.getTickCount() % Math.max(level * level, 1) == 0;
            //Flash
            if (level > this.lastDisplayedThirst) {
                this.thirstFlashTicks = 11; //Two flashes that start immediately
                this.lastDisplayedThirst = (byte) level;
                this.thirstTick = 1_000;
            }
            else if (this.thirstTick == 0) {
                this.lastDisplayedThirst = (byte) level;
                this.thirstTick = 1_000;
            }
            flash = this.thirstFlashTicks > 0 && this.thirstFlashTicks / 3 % 2 != 0;
            //Rendering
            icon = 9 * 3;
            extraIcon = 9 * 11;
            if (extraLevel > 26) {
                extraIcon += 9 * 8;
            }
            else if (extraLevel > 13) {
                extraIcon += 9 * 4;
            }
            extraHoldingIcon = 9 * 11;
            if (extraHoldingLevel > 26) {
                extraHoldingIcon += 9 * 8;
            }
            else if (extraHoldingLevel > 13) {
                extraHoldingIcon += 9 * 4;
            }
            background = 0;
            if (this.mc.player.hasEffect(EvolutionEffects.THIRST.get())) {
                icon += 9 * 4;
                background = 9 * 2;
            }
            if (flash) {
                background = 9;
            }
            for (int i = 0; i < 10; i++) {
                int baseline = i * 2 + 1;
                int extraBaseline = i * 4 + 1;
                int x = left - i * 8 - 9;
                int aligment;
                if (shake) {
                    aligment = (byte) (this.rand.nextInt(3) - 1);
                }
                else {
                    aligment = 0;
                }
                int y = top + aligment;
                blit(matrices, x, y, background, EvolutionResources.ICON_THIRST, 9, 9);
                if (holdingLevel > futureLevel) {
                    icon += 9 * 2;
                    enableAlpha(this.thirstFlashAlpha);
                    if (holdingLevel > baseline) {
                        blit(matrices, x, y, icon, EvolutionResources.ICON_THIRST, 9, 9);
                    }
                    else if (holdingLevel == baseline) {
                        blit(matrices, x, y, icon + 9, EvolutionResources.ICON_THIRST, 9, 9);
                    }
                    disableAlpha(this.thirstFlashAlpha);
                    icon -= 9 * 2;
                }
                if (futureLevel > level) {
                    icon += 9 * 2;
                    if (futureLevel > baseline) {
                        blit(matrices, x, y, icon, EvolutionResources.ICON_THIRST, 9, 9);
                    }
                    else if (futureLevel == baseline) {
                        blit(matrices, x, y, icon + 9, EvolutionResources.ICON_THIRST, 9, 9);
                    }
                    icon -= 9 * 2;
                }
                if (level > baseline) {
                    blit(matrices, x, y, icon, EvolutionResources.ICON_THIRST, 9, 9);
                }
                else if (level == baseline) {
                    blit(matrices, x, y, icon + 9, EvolutionResources.ICON_THIRST, 9, 9);
                }
                //Saturation
                if (extraLevel > extraBaseline) {
                    int offset = switch (extraLevel - extraBaseline) {
                        case 1 -> 9;
                        case 2 -> 9 * 2;
                        default -> 9 * 3;
                    };
                    blit(matrices, x, y, extraIcon + offset, EvolutionResources.ICON_THIRST, 9, 9);
                }
                else if (extraLevel == extraBaseline) {
                    blit(matrices, x, y, extraIcon, EvolutionResources.ICON_THIRST, 9, 9);
                }
                if (extraHoldingLevel > extraLevel) {
                    enableAlpha(this.thirstFlashAlpha);
                    if (extraHoldingLevel > extraBaseline) {
                        int offset = switch (extraHoldingLevel - extraBaseline) {
                            case 1 -> 9;
                            case 2 -> 9 * 2;
                            default -> 9 * 3;
                        };
                        blit(matrices, x, y, extraHoldingIcon + offset, EvolutionResources.ICON_THIRST, 9, 9);
                    }
                    else if (extraHoldingLevel == extraBaseline) {
                        blit(matrices, x, y, extraHoldingIcon, EvolutionResources.ICON_THIRST, 9, 9);
                    }
                    disableAlpha(this.thirstFlashAlpha);
                }
            }
            RenderSystem.disableBlend();
        }
        this.mc.getProfiler().pop();
    }

    public void renderHealth(PoseStack matrices, int width, int height) {
        this.mc.getProfiler().push("health");
        Player player = this.mc.player;
        if (player != null) {
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
            RenderSystem.enableBlend();
            float currentHealth = player.getHealth();
            int currentDisplayedHealth = roundToHearts(currentHealth);
            //Take damage
            if (this.lastDisplayedHealth > currentDisplayedHealth) {
                this.healthTick = 1_000;
                this.healthFlashTicks = 20; //Three flashes that have a delay to start
                this.lastDisplayedHealth = (byte) currentDisplayedHealth;
            }
            //Regen Health
            else if (currentDisplayedHealth > this.lastDisplayedHealth) {
                this.healthTick = 1_000;
                this.healthFlashTicks = 11; //Two flashes that start immediately
                this.lastDisplayedHealth = (byte) currentDisplayedHealth;
            }
            //Update variables every 1s
            else if (this.healthTick == 0) {
                this.lastPlayerHealth = currentDisplayedHealth;
                this.healthTick = 1_000;
            }
            float healthMax = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
            boolean flash = this.healthFlashTicks > 0 && this.healthFlashTicks / 3 % 2 != 0;
            int healthLast = this.lastPlayerHealth;
            float absorb = player.getAbsorptionAmount();
            int absorbHearts = Mth.ceil(absorb / 10);
            int normalHearts = Mth.ceil(healthMax / 10);
            int heartRows = Mth.ceil((absorbHearts + normalHearts) / 10.0F);
            ForgeIngameGui gui = (ForgeIngameGui) this.mc.gui;
            int top = height - gui.left_height;
            int rowHeight = Math.max(10 - (heartRows - 2), 3);
            gui.left_height += heartRows * rowHeight;
            if (rowHeight != 10) {
                gui.left_height += 10 - rowHeight;
            }
            int regen = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                regen = this.client.getTickCount() % Math.max(normalHearts + absorbHearts, 25);
            }
            assert this.mc.level != null;
            boolean hardcore = this.mc.level.getLevelData().isHardcore();
            final int heartTextureYPos = hardcore ? EvolutionResources.ICON_HEARTS_HARDCORE : EvolutionResources.ICON_HEARTS;
            final int absorbHeartTextureYPos = heartTextureYPos + 9;
            final int heartBackgroundXPos = flash ? 9 : 0;
            int icon = 9 * 2;
            if (player.hasEffect(MobEffects.POISON)) {
                icon = 9 * 10;
            }
            else if (player.hasEffect(EvolutionEffects.ANAEMIA.get())) {
                icon = 9 * 18;
            }
            int absorbRemaining = roundToHearts(absorb);
            this.rand.setSeed(312_871L * this.client.getTickCount());
            int left = width / 2 - 91;
            for (int currentHeart = absorbHearts + normalHearts - 1; currentHeart >= 0; currentHeart--) {
                int row = Mth.ceil((currentHeart + 1) / 10.0F) - 1;
                int x = left + currentHeart % 10 * 8;
                int y = top - row * rowHeight;
                //Shake hearts if 20% HP
                if (currentDisplayedHealth <= 8) {
                    y += this.rand.nextInt(2);
                }
                if (currentHeart == regen) {
                    y -= 2;
                }
                blit(matrices, x, y, heartBackgroundXPos, heartTextureYPos, 9, 9);
                if (flash) {
                    if (healthLast > currentHeart * 4) {
                        int offset = switch (healthLast - currentHeart * 4) {
                            case 1 -> 9 * 7;
                            case 2 -> 9 * 6;
                            case 3 -> 9 * 5;
                            default -> 9 * 4;
                        };
                        blit(matrices, x, y, icon + offset, heartTextureYPos, 9, 9);
                    }
                }
                if (absorbRemaining > 0) {
                    int absorbHeart = absorbRemaining % 4;
                    int offset = switch (absorbHeart) {
                        case 3 -> 9;
                        case 2 -> 9 * 2;
                        case 1 -> 9 * 3;
                        default -> 0;
                    };
                    blit(matrices, x, y, offset, absorbHeartTextureYPos, 9, 9);
                    if (absorbHeart == 0) {
                        absorbRemaining -= 4;
                    }
                    else {
                        absorbRemaining -= absorbHeart;
                    }
                }
                else {
                    if (currentDisplayedHealth > currentHeart * 4) {
                        int offset = switch (currentDisplayedHealth - currentHeart * 4) {
                            case 1 -> 9 * 3;
                            case 2 -> 9 * 2;
                            case 3 -> 9;
                            default -> 0;
                        };
                        blit(matrices, x, y, icon + offset, heartTextureYPos, 9, 9);
                    }
                }
            }
            RenderSystem.disableBlend();
        }
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
        drawHitbox(matrices, buffer.getBuffer(RenderType.lines()), hitbox, (float) (posX - projX), (float) (posY - projY), (float) (posZ - projZ),
                   1.0F, 1.0F, 0.0F, 1.0F, entity,
                   partialTicks);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
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
                for (int i = 0, l = ClientEvents.EFFECTS_TO_ADD.size(); i < l; i++) {
                    ClientEffectInstance instance = ClientEvents.EFFECTS_TO_ADD.get(i);
                    for (int j = 0; j < 20; j++) {
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
            byte beneficalCount = 0;
            byte neutralCount = 0;
            byte harmfulCount = 0;
            boolean isMoving = false;
            for (int i = 0, l = this.effects.size(); i < l; i++) {
                ClientEffectInstance effectInstance = this.effects.get(i);
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
                                    Mth.cos(effectInstance.getDuration() * Mth.PI / 5.0F) *
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
                        String text = MathHelper.getRomanNumber(ScreenDisplayEffects.getFixedAmplifier(effectInstance) + 1);
                        //noinspection ObjectAllocationInLoop
                        this.runnables.add(() -> this.mc.font.drawShadow(matrices, text, (finalX + 3) * 2, (finalY + 17) * 2, 0xff_ffff));
                    }
                }
            }
            for (int i = 0, l = this.runnables.size(); i < l; i++) {
                Runnable run = this.runnables.get(i);
                if (run != null) {
                    matrices.pushPose();
                    matrices.scale(0.5f, 0.5f, 0.5f);
                    run.run();
                    matrices.popPose();
                }
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

    public void renderStamina(PoseStack matrices, int width, int height) {
        this.mc.getProfiler().push("stamina");
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        RenderSystem.enableBlend();
        ForgeIngameGui gui = (ForgeIngameGui) this.mc.gui;
        int left = width / 2 - 91;
        int y = height - gui.left_height;
        gui.left_height += 10;
        for (int i = 0; i < 10; i++) {
            int x = left + 8 * i;
            blit(matrices, x, y, 0, EvolutionResources.ICON_STAMINA, 9, 9);
        }
        this.mc.getProfiler().pop();
    }

    public void renderTemperature(PoseStack matrices, int width, int height) {
        this.mc.getProfiler().push("temperature");
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        TemperatureClient temperature = TemperatureClient.CLIENT_INSTANCE;
        int currentTemp = temperature.getCurrentTemperature();
        int minComf = temperature.getCurrentMinComfort() + 70;
        int maxComf = temperature.getCurrentMaxComfort() + 70;
        if (minComf != maxComf) {
            //Draw Comfort zone
            blit(matrices, width / 2 - 90 + minComf, height - 29, 1, EvolutionResources.ICON_TEMPERATURE + 5, maxComf - minComf, 5);
        }
        if (minComf > 0) {
            //Draw Cold zone
            blit(matrices, width / 2 - 90, height - 29, 1 + 180 - minComf, EvolutionResources.ICON_TEMPERATURE + 5 * 2, minComf, 5);
        }
        if (maxComf < 180) {
            //Draw Hot zone
            blit(matrices, width / 2 - 90 + maxComf, height - 29, 1, EvolutionResources.ICON_TEMPERATURE + 5 * 3, 180 - maxComf, 5);
        }
        //Draw bar
        blit(matrices, width / 2 - 91, height - 29, 0, EvolutionResources.ICON_TEMPERATURE, 182, 5);
        if (currentTemp > -69 && currentTemp < 109) {
            currentTemp += 69;
            //Draw meter
            blit(matrices, width / 2 - 93 + currentTemp, height - 29, 183, EvolutionResources.ICON_TEMPERATURE, 8, 5);
        }
        else if (currentTemp < 0) {
            //Draw too cold indicator
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.sinDeg(this.client.getTickCount() * 9));
            RenderSystem.enableBlend();
            blit(matrices, width / 2 - 97, height - 29, 192, EvolutionResources.ICON_TEMPERATURE, 5, 5);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        else {
            //Draw too hot indicator
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.sinDeg(this.client.getTickCount() * 9));
            RenderSystem.enableBlend();
            blit(matrices, width / 2 + 92, height - 29, 198, EvolutionResources.ICON_TEMPERATURE, 5, 5);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        this.mc.getProfiler().pop();
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
        }
        else {
            this.offhandEquipProgress = 0;
        }
    }

    public void startTick() {
//        this.updateArmSwingProgress();
        LocalPlayer player = this.mc.player;
        assert player != null;
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
                if (!ItemStack.matches(this.currentMainhandStack, mainhandStack)) {
                    this.currentMainhandStack = mainhandStack;
                    if (this.currentMainhandStack.getItem() instanceof IMelee melee && melee.shouldPlaySheatheSound(this.currentMainhandStack)) {
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
                if (!ItemStack.matches(this.currentOffhandStack, offhandStack)) {
                    this.currentOffhandStack = offhandStack;
                    if (this.currentOffhandStack.getItem() instanceof IMelee melee && melee.shouldPlaySheatheSound(this.currentOffhandStack)) {
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
            float cooledAttackStrength = this.client.getOffhandCooledAttackStrength(1.0F);
            this.offhandEquipProgress += MathHelper.clamp(
                    (!requipO ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) - this.offhandEquipProgress, -0.4f, 0.4F);
            cooledAttackStrength = this.client.getMainhandCooledAttackStrength(1.0F);
            this.mainhandEquipProgress += MathHelper.clamp(
                    (!requipM ? cooledAttackStrength * cooledAttackStrength * cooledAttackStrength : 0.0F) - this.mainhandEquipProgress, -0.4F, 0.4F);

        }
        if (this.mainhandEquipProgress < 0.1F) {
            this.mainHandStack = mainhandStack.copy();
        }
        if (this.offhandEquipProgress < 0.1F) {
            this.offhandStack = offhandStack.copy();
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

//    private void updateArmSwingProgress() {
//        int i = this.getArmSwingAnimationEnd();
//        if (this.offhandIsSwingInProgress) {
//            ++this.offhandSwingProgressInt;
//            if (this.offhandSwingProgressInt >= i) {
//                this.offhandSwingProgressInt = 0;
//                this.offhandIsSwingInProgress = false;
//            }
//        }
//        else {
//            this.offhandSwingProgressInt = 0;
//        }
//        if (this.mainhandIsSwingInProgress) {
//            ++this.mainhandSwingProgressInt;
//            if (this.mainhandSwingProgressInt >= i) {
//                this.mainhandSwingProgressInt = 0;
//                this.mainhandIsSwingInProgress = false;
//            }
//        }
//        else {
//            this.mainhandSwingProgressInt = 0;
//        }
//    }

    public void updateHitmarkers(boolean isKill) {
        if (isKill) {
            this.killmarkerTick = 14;
        }
        else {
            this.hitmarkerTick = 14;
        }
    }
}
