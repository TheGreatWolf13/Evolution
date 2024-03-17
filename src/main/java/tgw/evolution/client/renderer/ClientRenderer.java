package tgw.evolution.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL14;
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.capabilities.player.TemperatureClient;
import tgw.evolution.client.audio.SoundEntityEmitted;
import tgw.evolution.client.gui.EvolutionGui;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.gui.ScreenDisplayEffects;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.client.util.Blending;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.items.IDrink;
import tgw.evolution.items.IFood;
import tgw.evolution.items.IMelee;
import tgw.evolution.network.PacketCSPlaySoundEntityEmitted;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.hitbox.Hitbox;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;
import tgw.evolution.util.math.MathHelper;

import java.util.Collections;
import java.util.Random;

public class ClientRenderer {

    private static @Nullable ClientRenderer instance;
    private static int slotMainHand;
    private final ClientEvents client;
    private final OList<ClientEffectInstance> effects = new OArrayList<>();
    private final Minecraft mc;
    private final Random rand = new Random();
    private final ListRunnable runnables = new ListRunnable();
    public boolean isAddingEffect;
    private @Nullable RunnableAddingEffect addingEffect;
    private byte healthFlashTicks;
    private short healthTick;
    private byte hitmarkerTick;
    private byte hungerAlphaMult = 1;
    private float hungerFlashAlpha;
    private byte hungerFlashTicks;
    private short hungerTick;
    private boolean isRenderingPlayer;
    private byte killmarkerTick;
    private short lastBeneficalCount;
    private int lastDisplayedHealth;
    private byte lastDisplayedHunger;
    private byte lastDisplayedThirst;
    private short lastNeutralCount;
    private int lastPlayerHealth;
    private ItemStack mainHandStack = ItemStack.EMPTY;
    private short movingFinalCount;
    private ItemStack offhandStack = ItemStack.EMPTY;
    private boolean shouldRenderLeftArm = true;
    private boolean shouldRenderRightArm = true;
    private byte thirstAlphaMult = 1;
    private float thirstFlashAlpha;
    private byte thirstFlashTicks;
    private short thirstTick;

    public ClientRenderer(Minecraft mc, ClientEvents client) {
        instance = this;
        this.mc = mc;
        this.client = client;
    }

    private static void blit(PoseStack matrices, int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        GuiComponent.blit(matrices, x, y, 20, textureX, textureY, sizeX, sizeY, 256, 256);
    }

    private static void blitInBatch(Matrix4f matrix, int x, int y, int textureX, int textureY, int sizeX, int sizeY) {
        GUIUtils.blitInBatch(matrix, x, y, 20, textureX, textureY, sizeX, sizeY, 256, 256);
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
        HitboxEntity<T> hitboxes = (HitboxEntity<T>) entity.getHitboxes();
        if (hitboxes == null) {
            return;
        }
        assert Minecraft.getInstance().player != null;
        boolean renderAll = Minecraft.getInstance().player.getMainHandItem().getItem() == EvolutionItems.DEBUG_ITEM ||
                            Minecraft.getInstance().player.getOffhandItem().getItem() == EvolutionItems.DEBUG_ITEM;
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
                                         int pX, int pY, int pZ,
                                         BlockState state) {
        drawShape(matrices, buffer, state.getShape_(entity.level, pX, pY, pZ, entity), pX - x, pY - y, pZ - z,
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

    public static void floatBlit(PoseStack matrices, float x, float y, int textureX, int textureY, int sizeX, int sizeY, int blitOffset) {
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

    private static void renderAttackIndicator(HumanoidArm arm, Matrix4f matrix, int x, int y, float perc, boolean shouldRenderOther) {
        int l = (int) (perc * 18.0F);
        int baseIndex = 5;
        int fullIndex = 6;
        int i = 0;
        if (arm == HumanoidArm.LEFT) {
            baseIndex += 2;
            fullIndex += 2;
            if (shouldRenderOther) {
                x -= 10;
            }
            i = 17 - l;
        }
        else if (shouldRenderOther) {
            x += 10;
        }
        //Render base
        blitInBatch(matrix, x, y, baseIndex * 17, EvolutionResources.ICON_17_17, 17, 17);
        //Render full
        blitInBatch(matrix, x + i, y, fullIndex * 17 + i, EvolutionResources.ICON_17_17, l, 17);
    }

    public static int roundToHearts(float currentHealth) {
        return Mth.ceil(currentHealth / 2.5f);
    }

    public static boolean shouldReequip(ItemStack from, ItemStack to, int slot) {
        boolean fromInvalid = from.isEmpty();
        boolean toInvalid = to.isEmpty();
        if (fromInvalid && toInvalid) {
            return false;
        }
        if (fromInvalid || toInvalid) {
            return true;
        }
        if (slot != -1) {
            boolean changed = slot != slotMainHand;
            if (changed) {
                slotMainHand = slot;
                return true;
            }
        }
        return !MathHelper.areStacksSimilar(from, to);
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

    public boolean isRenderingPlayer() {
        return this.isRenderingPlayer;
    }

    private boolean rayTraceMouse(@Nullable HitResult rayTraceResult) {
        if (rayTraceResult == null) {
            return false;
        }
        if (rayTraceResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) rayTraceResult).getEntity() instanceof InventoryCarrier;
        }
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) rayTraceResult;
            int x = blockHitResult.posX();
            int y = blockHitResult.posY();
            int z = blockHitResult.posZ();
            Level level = this.mc.level;
            assert level != null;
            return level.getBlockState_(x, y, z).getMenuProvider(level, new BlockPos(x, y, z)) != null;
        }
        return false;
    }

    public void renderBlockOutlines(PoseStack matrices, MultiBufferSource buffer, Camera camera, int x, int y, int z) {
        assert this.mc.level != null;
        BlockState state = this.mc.level.getBlockState_(x, y, z);
        if (!state.isAir() && this.mc.level.getWorldBorder().isWithinBounds_(x, z)) {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
            RenderSystem.disableTexture();
            matrices.pushPose();
            double projX = camera.getPosition().x;
            double projY = camera.getPosition().y;
            double projZ = camera.getPosition().z;
            drawSelectionBox(matrices, buffer.getBuffer(RenderType.lines()), camera.getEntity(), projX, projY, projZ, x, y, z, state);
            matrices.popPose();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public void renderCrosshair(PoseStack matrices, float partialTicks, int width, int height) {
        Options options = this.mc.options;
        LocalPlayer player = this.mc.player;
        if (options.getCameraType() == CameraType.FIRST_PERSON) {
            assert this.mc.gameMode != null;
            assert player != null;
            if (this.mc.gameMode.getPlayerMode() != GameType.SPECTATOR || this.rayTraceMouse(this.mc.hitResult)) {
                if (options.renderDebug && !options.hideGui && !player.isReducedDebugInfo() && !options.reducedDebugInfo) {
                    PoseStack internalMat = RenderSystem.getModelViewStack();
                    internalMat.pushPose();
                    internalMat.translate(width / 2.0, height / 2.0, this.mc.gui.getBlitOffset());
                    Camera camera = this.mc.gameRenderer.getMainCamera();
                    internalMat.mulPoseX(-camera.getXRot());
                    internalMat.mulPoseY(camera.getYRot());
                    internalMat.scale(-1.0f, -1.0f, -1.0f);
                    RenderSystem.applyModelViewMatrix();
                    RenderSystem.renderCrosshair(10);
                    internalMat.popPose();
                    RenderSystem.applyModelViewMatrix();
                }
                else {
                    boolean inverted = false;
                    //Hitmarker
                    if (EvolutionConfig.HITMARKERS.get()) {
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
                            inverted = true;
                            Blending.INVERTED_ADD.apply();
                            RenderSystem.blendEquation(GL14.GL_FUNC_SUBTRACT);
                            if (this.hitmarkerTick < 5) {
                                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (this.hitmarkerTick + partialTicks) / 5);
                            }
                            blit(matrices, (width - 17) / 2, (height - 17) / 2, 17, EvolutionResources.ICON_17_17, 17, 17);
                        }
                    }
                    //Crosshair
                    Matrix4f matrix = matrices.last().pose();
                    GUIUtils.startBlitBatch(Tesselator.getInstance().getBuilder());
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (!inverted) {
                        Blending.INVERTED_ADD.apply();
                        RenderSystem.blendEquation(GL14.GL_FUNC_SUBTRACT);
                    }
                    int x = (width - 17) / 2;
                    int y = (height - 17) / 2;
                    blitInBatch(matrix, x, y, 0, EvolutionResources.ICON_17_17, 17, 17);
                    //Interaction indicator
                    //noinspection VariableNotUsedInsideIf
                    if (this.client.leftPointedEntity != null) {
                        assert this.mc.hitResult != null;
                        if (this.mc.hitResult.getType() != HitResult.Type.BLOCK) {
                            blitInBatch(matrix, x, y, 4 * 17, EvolutionResources.ICON_17_17, 17, 17);
                        }
                    }
                    if (this.mc.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float mainhandPerc = this.client.getMainhandIndicatorPercentage(partialTicks);
                        float offhandPerc = this.client.getOffhandIndicatorPercentage(partialTicks);
                        boolean shouldRenderMain = mainhandPerc < 1;
                        boolean shouldRenderOff = offhandPerc < 1;
                        y += 17;
                        if (shouldRenderMain) {
                            renderAttackIndicator(player.getMainArm(), matrix, x, y, mainhandPerc, shouldRenderOff || !player.getOffhandItem().isEmpty());
                        }
                        if (shouldRenderOff) {
                            renderAttackIndicator(player.getMainArm().getOpposite(), matrix, x, y, offhandPerc, true);
                        }
                    }
                    GUIUtils.endBlitBatch();
                    //FollowUp Indicator
                    if (EvolutionConfig.FOLLOW_UPS.get() && this.client.shouldRenderSpecialAttack()) {
                        IMelee.IAttackType type = player.getSpecialAttackType();
                        if (type != null) {
                            if (type.getFollowUps() > 0) {
                                String s = String.valueOf(player.isOnGracePeriod() ? player.getFollowUp() : player.getFollowUp() + 1);
                                MultiBufferSource.BufferSource bufferSource = this.mc.renderBuffers().bufferSource();
                                this.mc.font.drawInBatch8xOutline(new TextComponent(s).getVisualOrderText(), (width - this.mc.font.width(s)) / 2.0f, height / 2.0f - 17, 0xffff_ffff, 0x0, matrix, bufferSource, DynamicLights.FULL_LIGHTMAP);
                                bufferSource.endBatch();
                            }
                        }
                    }
                    RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
                }
            }
        }
    }

    public void renderEffectIcons(PoseStack matrices, float partialTicks, int width, int height) {
        MobEffectTextureManager effectTextures = this.mc.getMobEffectTextures();
        ClientEffectInstance movingInstance = null;
        if (this.addingEffect == null) {
            this.addingEffect = new RunnableAddingEffect();
        }
        else {
            this.addingEffect.discard();
        }
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
            this.addingEffect.set(matrices, x, y, alpha, addingInstance, effectTextures.get(addingEffect), this.mc.font);
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
            this.runnables.softClear();
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
                    RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
                    RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    float alpha = 1.0F;
                    if (effectInstance.isAmbient()) {
                        floatBlit(matrices, x, y, 180, 180, 24, 24, 0);
                    }
                    else {
                        floatBlit(matrices, x, y, 156, 180, 24, 24, 0);
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
                        String text = MathHelper.getRomanNumber(ScreenDisplayEffects.getFixedAmplifier(effectInstance) + 1);
                        this.runnables.setNew(matrices, x, y, text, this.mc.font);
                    }
                }
            }
            for (int i = 0, l = this.runnables.size(); i < l; i++) {
                RunnableEffectAmplifier run = this.runnables.get(i);
                run.run();
            }
            this.lastBeneficalCount = beneficalCount;
            this.lastNeutralCount = neutralCount;
        }
        else {
            this.movingFinalCount = 1;
            this.lastBeneficalCount = 0;
            this.lastNeutralCount = 0;
            this.runnables.clear();
        }
        this.addingEffect.run();
    }

    public void renderFoodAndThirst(PoseStack matrices, int width, int height) {
        //Preparations
        LivingEntity player = this.mc.player;
        assert player != null;
        EvolutionGui gui = (EvolutionGui) this.mc.gui;
        int top = height - gui.getRightHeightAndIncrease();
        int left = width / 2 + 91;
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
        CapabilityHunger hunger = CapabilityHunger.CLIENT_INSTANCE;
        int value = hunger.getHungerLevel();
        int extraValue = hunger.getSaturationLevel();
        int level = CapabilityHunger.hungerLevel(value);
        int futureLevel = CapabilityHunger.hungerLevel(Math.min(value + extraValue, CapabilityHunger.HUNGER_CAPACITY));
        int holdingLevel = CapabilityHunger.hungerLevel(Math.min(value + extraValue + holdingValue, CapabilityHunger.HUNGER_CAPACITY));
        int extraLevel = CapabilityHunger.saturationLevel(extraValue);
        int extraHoldingLevel = CapabilityHunger.saturationLevel(Math.min(extraValue + holdingValue, CapabilityHunger.SATURATION_CAPACITY));
        boolean shake = this.client.getTicks() % Math.max(level * level, 1) == 0;
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
        Matrix4f matrix = matrices.last().pose();
        GUIUtils.startBlitBatch(Tesselator.getInstance().getBuilder());
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
            blitInBatch(matrix, x, y, background, EvolutionResources.ICON_HUNGER, 9, 9);
            if (holdingLevel > futureLevel) {
                icon += 9 * 2;
                enableAlpha(this.hungerFlashAlpha);
                if (holdingLevel > baseline) {
                    blitInBatch(matrix, x, y, icon, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                else if (holdingLevel == baseline) {
                    blitInBatch(matrix, x, y, icon + 9, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                disableAlpha(this.hungerFlashAlpha);
                icon -= 9 * 2;
            }
            if (futureLevel > level) {
                icon += 9 * 2;
                if (futureLevel > baseline) {
                    blitInBatch(matrix, x, y, icon, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                else if (futureLevel == baseline) {
                    blitInBatch(matrix, x, y, icon + 9, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                icon -= 9 * 2;
            }
            if (level > baseline) {
                blitInBatch(matrix, x, y, icon, EvolutionResources.ICON_HUNGER, 9, 9);
            }
            else if (level == baseline) {
                blitInBatch(matrix, x, y, icon + 9, EvolutionResources.ICON_HUNGER, 9, 9);
            }
            //Saturation
            if (extraLevel > extraBaseline) {
                int offset = switch (extraLevel - extraBaseline) {
                    case 1 -> 9;
                    case 2 -> 9 * 2;
                    default -> 9 * 3;
                };
                blitInBatch(matrix, x, y, extraIcon + offset, EvolutionResources.ICON_HUNGER, 9, 9);
            }
            else if (extraLevel == extraBaseline) {
                blitInBatch(matrix, x, y, extraIcon, EvolutionResources.ICON_HUNGER, 9, 9);
            }
            if (extraHoldingLevel > extraLevel) {
                enableAlpha(this.hungerFlashAlpha);
                if (extraHoldingLevel > extraBaseline) {
                    int offset = switch (extraHoldingLevel - extraBaseline) {
                        case 1 -> 9;
                        case 2 -> 9 * 2;
                        default -> 9 * 3;
                    };
                    blitInBatch(matrix, x, y, extraHoldingIcon + offset, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                else if (extraHoldingLevel == extraBaseline) {
                    blitInBatch(matrix, x, y, extraHoldingIcon, EvolutionResources.ICON_HUNGER, 9, 9);
                }
                disableAlpha(this.hungerFlashAlpha);
            }
        }
        //Thirst
        //Preparations
        top = height - gui.getRightHeightAndIncrease();
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
        CapabilityThirst thirst = CapabilityThirst.CLIENT_INSTANCE;
        value = thirst.getThirstLevel();
        extraValue = thirst.getHydrationLevel();
        level = CapabilityThirst.thirstLevel(value);
        futureLevel = CapabilityThirst.thirstLevel(Math.min(value + extraValue, CapabilityThirst.THIRST_CAPACITY));
        holdingLevel = CapabilityThirst.thirstLevel(Math.min(value + extraValue + holdingValue, CapabilityThirst.THIRST_CAPACITY));
        extraLevel = CapabilityThirst.hydrationLevel(extraValue);
        extraHoldingLevel = CapabilityThirst.hydrationLevel(Math.min(extraValue + holdingValue, CapabilityThirst.HYDRATION_CAPACITY));
        shake = this.client.getTicks() % Math.max(level * level, 1) == 0;
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
        if (this.mc.player.hasEffect(EvolutionEffects.THIRST)) {
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
            blitInBatch(matrix, x, y, background, EvolutionResources.ICON_THIRST, 9, 9);
            if (holdingLevel > futureLevel) {
                icon += 9 * 2;
                enableAlpha(this.thirstFlashAlpha);
                if (holdingLevel > baseline) {
                    blitInBatch(matrix, x, y, icon, EvolutionResources.ICON_THIRST, 9, 9);
                }
                else if (holdingLevel == baseline) {
                    blitInBatch(matrix, x, y, icon + 9, EvolutionResources.ICON_THIRST, 9, 9);
                }
                disableAlpha(this.thirstFlashAlpha);
                icon -= 9 * 2;
            }
            if (futureLevel > level) {
                icon += 9 * 2;
                if (futureLevel > baseline) {
                    blitInBatch(matrix, x, y, icon, EvolutionResources.ICON_THIRST, 9, 9);
                }
                else if (futureLevel == baseline) {
                    blitInBatch(matrix, x, y, icon + 9, EvolutionResources.ICON_THIRST, 9, 9);
                }
                icon -= 9 * 2;
            }
            if (level > baseline) {
                blitInBatch(matrix, x, y, icon, EvolutionResources.ICON_THIRST, 9, 9);
            }
            else if (level == baseline) {
                blitInBatch(matrix, x, y, icon + 9, EvolutionResources.ICON_THIRST, 9, 9);
            }
            //Hydration
            if (extraLevel > extraBaseline) {
                int offset = switch (extraLevel - extraBaseline) {
                    case 1 -> 9;
                    case 2 -> 9 * 2;
                    default -> 9 * 3;
                };
                blitInBatch(matrix, x, y, extraIcon + offset, EvolutionResources.ICON_THIRST, 9, 9);
            }
            else if (extraLevel == extraBaseline) {
                blitInBatch(matrix, x, y, extraIcon, EvolutionResources.ICON_THIRST, 9, 9);
            }
            if (extraHoldingLevel > extraLevel) {
                enableAlpha(this.thirstFlashAlpha);
                if (extraHoldingLevel > extraBaseline) {
                    int offset = switch (extraHoldingLevel - extraBaseline) {
                        case 1 -> 9;
                        case 2 -> 9 * 2;
                        default -> 9 * 3;
                    };
                    blitInBatch(matrix, x, y, extraHoldingIcon + offset, EvolutionResources.ICON_THIRST, 9, 9);
                }
                else if (extraHoldingLevel == extraBaseline) {
                    blitInBatch(matrix, x, y, extraHoldingIcon, EvolutionResources.ICON_THIRST, 9, 9);
                }
                disableAlpha(this.thirstFlashAlpha);
            }
        }
        GUIUtils.endBlitBatch();
    }

    public void renderHealth(PoseStack matrices, int width, int height) {
        Player player = this.mc.player;
        assert player != null;
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
        EvolutionGui gui = (EvolutionGui) this.mc.gui;
        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int top = height - gui.getLeftHeightAndIncrease(rowHeight * heartRows + 10 - rowHeight);
        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regen = this.client.getTicks() % Math.max(normalHearts + absorbHearts, 25);
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
        else if (player.hasEffect(EvolutionEffects.ANAEMIA)) {
            icon = 9 * 18;
        }
        int absorbRemaining = roundToHearts(absorb);
        this.rand.setSeed(312_871L * this.client.getTicks());
        int left = width / 2 - 91;
        GUIUtils.startBlitBatch(Tesselator.getInstance().getBuilder());
        Matrix4f matrix = matrices.last().pose();
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
            blitInBatch(matrix, x, y, heartBackgroundXPos, heartTextureYPos, 9, 9);
            if (flash) {
                if (healthLast > currentHeart * 4) {
                    int offset = switch (healthLast - currentHeart * 4) {
                        case 1 -> 9 * 7;
                        case 2 -> 9 * 6;
                        case 3 -> 9 * 5;
                        default -> 9 * 4;
                    };
                    blitInBatch(matrix, x, y, icon + offset, heartTextureYPos, 9, 9);
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
                blitInBatch(matrix, x, y, offset, absorbHeartTextureYPos, 9, 9);
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
                    blitInBatch(matrix, x, y, icon + offset, heartTextureYPos, 9, 9);
                }
            }
        }
        GUIUtils.endBlitBatch();
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

    public void renderOutlines(PoseStack matrices, MultiBufferSource buffer, VoxelShape shape, Camera info, int x, int y, int z) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderSystem.lineWidth(Math.max(2.5F, this.mc.getWindow().getWidth() / 1_920.0F * 2.5F));
        RenderSystem.disableTexture();
        matrices.pushPose();
        matrices.scale(0.99f, 0.99f, 0.99f);
        double projX = info.getPosition().x;
        double projY = info.getPosition().y;
        double projZ = info.getPosition().z;
        drawShape(matrices, buffer.getBuffer(RenderType.LINES), shape, x - projX, y - projY, z - projZ, 1.0F, 1.0F, 0.0F, 1.0F);
        matrices.popPose();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void renderStamina(PoseStack matrices, int width, int height) {
        EvolutionGui gui = (EvolutionGui) this.mc.gui;
        int left = width / 2 - 91;
        int y = height - gui.getLeftHeightAndIncrease();
        GUIUtils.startBlitBatch(Tesselator.getInstance().getBuilder());
        Matrix4f matrix = matrices.last().pose();
        for (int i = 0; i < 10; i++) {
            int x = left + 8 * i;
            blitInBatch(matrix, x, y, 0, EvolutionResources.ICON_STAMINA, 9, 9);
        }
        GUIUtils.endBlitBatch();
    }

    public void renderTemperature(PoseStack matrices, int width, int height) {
        TemperatureClient temperature = TemperatureClient.INSTANCE;
        int currentTemp = temperature.getCurrentTemperature();
        int minComf = temperature.getCurrentMinComfort() + 70;
        int maxComf = temperature.getCurrentMaxComfort() + 70;
        GUIUtils.startBlitBatch(Tesselator.getInstance().getBuilder());
        Matrix4f matrix = matrices.last().pose();
        if (minComf != maxComf) {
            //Draw Comfort zone
            blitInBatch(matrix, width / 2 - 90 + minComf, height - 29, 1, EvolutionResources.ICON_TEMPERATURE + 5, maxComf - minComf, 5);
        }
        if (minComf > 0) {
            //Draw Cold zone
            blitInBatch(matrix, width / 2 - 90, height - 29, 1 + 180 - minComf, EvolutionResources.ICON_TEMPERATURE + 5 * 2, minComf, 5);
        }
        if (maxComf < 180) {
            //Draw Hot zone
            blitInBatch(matrix, width / 2 - 90 + maxComf, height - 29, 1, EvolutionResources.ICON_TEMPERATURE + 5 * 3, 180 - maxComf, 5);
        }
        //Draw bar
        blitInBatch(matrix, width / 2 - 91, height - 29, 0, EvolutionResources.ICON_TEMPERATURE, 182, 5);
        if (currentTemp > -69 && currentTemp < 109) {
            currentTemp += 69;
            //Draw meter
            blitInBatch(matrix, width / 2 - 93 + currentTemp, height - 29, 183, EvolutionResources.ICON_TEMPERATURE, 8, 5);
            GUIUtils.endBlitBatch();
        }
        else {
            GUIUtils.endBlitBatch();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, MathHelper.sinDeg(this.client.getTicks() * 9));
            RenderSystem.enableBlend();
            if (currentTemp < 0) {
                //Draw too cold indicator
                blit(matrices, width / 2 - 97, height - 29, 192, EvolutionResources.ICON_TEMPERATURE, 5, 5);
            }
            else {
                //Draw too hot indicator
                blit(matrices, width / 2 + 92, height - 29, 198, EvolutionResources.ICON_TEMPERATURE, 5, 5);
            }
        }
    }

    public void setRenderingPlayer(boolean rendering) {
        this.isRenderingPlayer = rendering;
    }

    public void setVisibility(HumanoidArm arm, boolean visible) {
        if (arm == HumanoidArm.RIGHT) {
            this.shouldRenderRightArm = visible;
        }
        else {
            this.shouldRenderLeftArm = visible;
        }
    }

    public boolean shouldRenderArm(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? this.shouldRenderRightArm() : this.shouldRenderLeftArm();
    }

    public boolean shouldRenderLeftArm() {
        if (!this.isRenderingPlayer) {
            return true;
        }
        return this.shouldRenderLeftArm;
    }

    public boolean shouldRenderRightArm() {
        if (!this.isRenderingPlayer) {
            return true;
        }
        return this.shouldRenderRightArm;
    }

    public void startTick() {
        LocalPlayer player = this.mc.player;
        assert player != null;
        ItemStack mainhandStack = player.getMainHandItem();
        ItemStack offhandStack = player.getOffhandItem();
        if (!player.isHandsBusy()) {
            boolean requipM = shouldReequip(this.mainHandStack, mainhandStack, player.getInventory().selected);
            boolean requipO = shouldReequip(this.offhandStack, offhandStack, -1);
            if (requipM) {
                this.client.resetCooldown(InteractionHand.MAIN_HAND);
                if (!ItemStack.matches(this.mainHandStack, mainhandStack)) {
                    if (mainhandStack.getItem() instanceof IMelee melee && melee.shouldPlaySheatheSound(mainhandStack)) {
                        this.mc.getSoundManager()
                               .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_UNSHEATHE, SoundSource.PLAYERS, 0.8f, 1.0f));
                        player.connection.send(new PacketCSPlaySoundEntityEmitted(EvolutionSounds.SWORD_UNSHEATHE, SoundSource.PLAYERS, 0.8f, 1.0f));
                    }
                }
            }
            else {
                this.client.incrementCooldown(InteractionHand.MAIN_HAND);
            }
            if (requipO) {
                this.client.resetCooldown(InteractionHand.OFF_HAND);
                if (!ItemStack.matches(this.offhandStack, offhandStack)) {
                    if (offhandStack.getItem() instanceof IMelee melee && melee.shouldPlaySheatheSound(offhandStack)) {
                        this.mc.getSoundManager()
                               .play(new SoundEntityEmitted(this.mc.player, EvolutionSounds.SWORD_UNSHEATHE, SoundSource.PLAYERS, 0.8f, 1.0f));
                        player.connection.send(new PacketCSPlaySoundEntityEmitted(EvolutionSounds.SWORD_UNSHEATHE, SoundSource.PLAYERS, 0.8f, 1.0f));
                    }
                }
            }
            else {
                this.client.incrementCooldown(InteractionHand.OFF_HAND);
            }
            this.offhandStack = offhandStack;
            this.mainHandStack = mainhandStack;
        }
    }

    public void updateHitmarkers(boolean isKill) {
        if (isKill) {
            this.killmarkerTick = 14;
        }
        else {
            this.hitmarkerTick = 14;
        }
    }
}
