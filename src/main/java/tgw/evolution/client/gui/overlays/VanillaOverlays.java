package tgw.evolution.client.gui.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import tgw.evolution.Evolution;
import tgw.evolution.client.gui.EvolutionGui;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.Blending;
import tgw.evolution.events.ClientEvents;

public final class VanillaOverlays {

    public static final ResourceLocation AIR_LEVEL = new ResourceLocation("air_level");
    public static final ResourceLocation BOSS_HEALTH = new ResourceLocation("boss_health");
    public static final ResourceLocation CHAT_HISTORY = new ResourceLocation("chat_history");
    public static final ResourceLocation FPS_GRAPH = new ResourceLocation("fps_graph");
    public static final ResourceLocation FROSTBITE = new ResourceLocation("frostbite");
    public static final ResourceLocation HOTBAR = new ResourceLocation("hotbar");
    public static final ResourceLocation ITEM_NAME = new ResourceLocation("item_name");
    public static final ResourceLocation JUMP_BAR = new ResourceLocation("jump_bar");
    public static final ResourceLocation MOUNT_HEALTH = new ResourceLocation("mount_health");
    public static final ResourceLocation PLAYER_LIST = new ResourceLocation("player_list");
    public static final ResourceLocation PORTAL = new ResourceLocation("portal");
    public static final ResourceLocation PUMPKIN = new ResourceLocation("pumpkin");
    public static final ResourceLocation RECORD = new ResourceLocation("record");
    public static final ResourceLocation SCOREBOARD = new ResourceLocation("scoreboard");
    public static final ResourceLocation SCREEN_EFFECTS = new ResourceLocation("screen_effects");
    public static final ResourceLocation SLEEP_FADE = new ResourceLocation("sleep_fade");
    public static final ResourceLocation SPYGLASS = new ResourceLocation("spyglass");
    public static final ResourceLocation SUBTITLES = new ResourceLocation("subtitles");
    public static final ResourceLocation TEXT_COLUMNS = new ResourceLocation("text_columns");
    public static final ResourceLocation TITLE_TEXT = new ResourceLocation("title_text");
    public static final ResourceLocation VIGNETTE = new ResourceLocation("vignette");
    private static final BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();

    private VanillaOverlays() {}

    private static void airLevel(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.gameMode != null;
        if (!mc.gameMode.canHurtPlayer()) {
            return;
        }
        Player player = mc.player;
        assert player != null;
        int air = player.getAirSupply();
        if (player.isEyeInFluid(FluidTags.WATER) || air < 300) {
            gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false);
            int top = height - gui.getRightHeightAndIncrease();
            int full = Mth.ceil((air - 2) * 10.0 / 300.0);
            int partial = Mth.ceil(air * 10.0 / 300.0) - full;
            int left = width / 2 + 91;
            for (int i = 0; i < full + partial; ++i) {
                gui.blit(matrices, left - i * 8 - 9, top, i < full ? 16 : 25, 18, 9, 9);
            }
        }
    }

    private static void bossHealth(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, GuiComponent.GUI_ICONS_LOCATION);
        gui.setBlitOffset(-90);
        gui.getBossOverlay().render(matrices);
    }

    private static void chatHistory(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        Blending.DEFAULT_1_0.apply();
        matrices.pushPose();
        matrices.translate(0, height - 58, 0);
        gui.getChat().render(matrices, gui.getGuiTicks());
        matrices.popPose();
    }

    private static void fpsGraph(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui || !mc.options.renderDebug || !mc.options.renderFpsChart) {
            return;
        }
        gui.renderFPSGraph(matrices);
    }

    private static void frostbite(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        assert mc.getCameraEntity() != null;
        if (mc.getCameraEntity().getTicksFrozen() > 0) {
            gui.renderTextureOverlay(Gui.POWDER_SNOW_OUTLINE_LOCATION, mc.getCameraEntity().getPercentFrozen());
        }
    }

    private static void hotbar(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        Player player = mc.player;
        if (player == null) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, Gui.WIDGETS_LOCATION);
        assert mc.gameMode != null;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            gui.getSpectatorGui().renderHotbar(matrices);
            return;
        }
        ItemStack offhandStack = player.getOffhandItem();
        HumanoidArm offArm = player.getMainArm().getOpposite();
        int xMid = width / 2;
        Matrix4f matrix = matrices.last().pose();
        GUIUtils.startBlitBatch(Tesselator.getInstance().getBuilder());
        GUIUtils.blitInBatch(matrix, xMid - 91, height - 22, -90, 0, 0, 182, 22, 256, 256);
        GUIUtils.blitInBatch(matrix, xMid - 91 - 1 + player.getInventory().selected * 20, height - 22 - 1, -90, 0, 22, 24, 22, 256, 256);
        if (!offhandStack.isEmpty()) {
            if (offArm == HumanoidArm.LEFT) {
                GUIUtils.blitInBatch(matrix, xMid - 91 - 29, height - 23, -90, 24, 22, 29, 24, 256, 256);
            }
            else {
                GUIUtils.blitInBatch(matrix, xMid + 91, height - 23, -90, 53, 22, 29, 24, 256, 256);
            }
        }
        GUIUtils.endBlitBatch();
        int seed = 1;
        NonNullList<ItemStack> items = player.getInventory().items;
        for (int i = 0; i < 9; i++) {
            int x = xMid - 90 + i * 20 + 2;
            int y = height - 16 - 3;
            gui.renderSlot(x, y, partialTicks, player, items.get(i), seed++);
        }
        if (!offhandStack.isEmpty()) {
            int y = height - 16 - 3;
            if (offArm == HumanoidArm.LEFT) {
                gui.renderSlot(xMid - 91 - 26, y, partialTicks, player, offhandStack, seed);
            }
            else {
                gui.renderSlot(xMid + 91 + 10, y, partialTicks, player, offhandStack, seed);
            }
        }
        if (mc.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
            ClientEvents client = ClientEvents.getInstance();
            float mainhandPerc = client.getMainhandIndicatorPercentage(partialTicks);
            float offhandPerc = client.getOffhandIndicatorPercentage(partialTicks);
            boolean shouldRenderMain = mainhandPerc < 1;
            boolean shouldRenderOff = offhandPerc < 1;
            if (shouldRenderMain || shouldRenderOff) {
                RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                int y = height - 20;
                if (shouldRenderMain) {
                    int x = offArm == HumanoidArm.LEFT ? xMid + 125 : xMid - 125 - 18;
                    int i = (int) (mainhandPerc * 19.0F);
                    gui.blit(matrices, x, y, 0, 94, 18, 18);
                    gui.blit(matrices, x, y + 18 - i, 18, 112 - i, 18, i);
                }
                if (shouldRenderOff) {
                    int x = offArm == HumanoidArm.RIGHT ? xMid + 125 : xMid - 125 - 18;
                    int i = (int) (offhandPerc * 19.0F);
                    gui.blit(matrices, x, y, 0, 94, 18, 18);
                    gui.blit(matrices, x, y + 18 - i, 18, 112 - i, 18, i);
                }
            }
        }
    }

    private static void itemName(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.player != null;
        assert mc.gameMode != null;
        if (mc.options.heldItemTooltips && mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false);
            gui.renderSelectedItemName(matrices);
        }
        else if (mc.player.isSpectator()) {
            gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false);
            gui.getSpectatorGui().renderTooltip(matrices);
        }
    }

    private static void jumpBar(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.player != null;
        if (!mc.player.isRidingJumpable()) {
            return;
        }
        gui.setupOverlayRenderState(null, false);
        gui.renderJumpMeter(matrices, width / 2 - 91);
    }

    private static void mountHealth(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.gameMode != null;
        if (!mc.gameMode.canHurtPlayer()) {
            return;
        }
        Player player = mc.player;
        assert player != null;
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof LivingEntity mount)) {
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false);
        int leftAlign = width / 2 + 91;
        int health = (int) Math.ceil(mount.getHealth());
        float healthMax = mount.getMaxHealth();
        int hearts = (int) (healthMax + 0.5F) / 2;
        if (hearts > 30) {
            hearts = 30;
        }
        final int margin = 52;
        final int half = margin + 45;
        final int full = margin + 36;
        for (int heart = 0; hearts > 0; heart += 20) {
            int top = height - gui.getRightHeightAndIncrease();
            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;
            for (int i = 0; i < rowCount; ++i) {
                int x = leftAlign - i * 8 - 9;
                gui.blit(matrices, x, top, margin, 9, 9, 9);
                if (i * 2 + 1 + heart < health) {
                    gui.blit(matrices, x, top, full, 9, 9, 9);
                }
                else if (i * 2 + 1 + heart == health) {
                    gui.blit(matrices, x, top, half, 9, 9, 9);
                }
            }
        }
    }

    private static void playerList(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        Blending.DEFAULT_1_0.apply();
        assert mc.level != null;
        assert mc.player != null;
        Objective objective = mc.level.getScoreboard().getDisplayObjective(0);
        ClientPacketListener handler = mc.player.connection;
        if (mc.options.keyPlayerList.isDown() && (!mc.isLocalServer() || handler.getOnlinePlayers().size() > 1 || objective != null)) {
            gui.getTabList().setVisible(true);
            gui.getTabList().render(matrices, width, mc.level.getScoreboard(), objective);
        }
        else {
            gui.getTabList().setVisible(false);
        }
    }

    private static void portal(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!(mc.getCameraEntity() instanceof LocalPlayer player)) {
            return;
        }
        if (player.hasEffect(MobEffects.CONFUSION)) {
            return;
        }
        float portalTime = Mth.lerp(partialTicks, player.oPortalTime, player.portalTime);
        if (portalTime > 0) {
            gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, TextureAtlas.LOCATION_BLOCKS);
            if (portalTime < 1.0F) {
                portalTime *= portalTime;
                portalTime *= portalTime;
                portalTime = portalTime * 0.8F + 0.2F;
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, portalTime);
            TextureAtlasSprite sprite = mc.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(0, height, -90).uv(u0, v1).endVertex();
            builder.vertex(width, height, -90).uv(u1, v1).endVertex();
            builder.vertex(width, 0, -90).uv(u1, v0).endVertex();
            builder.vertex(0, 0, -90).uv(u0, v0).endVertex();
            tesselator.end();
        }
    }

    private static void pumpkin(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!(mc.getCameraEntity() instanceof LivingEntity living)) {
            return;
        }
        ItemStack helmet = living.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return;
        }
        Item item = helmet.getItem();
        if (item == Items.CARVED_PUMPKIN) {
            gui.renderTextureOverlay(Gui.PUMPKIN_BLUR_LOCATION, 1.0f);
        }
    }

    private static void record(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!mc.options.hideGui) {
            int overlayMessageTime = gui.getOverlayMessageTime();
            if (overlayMessageTime > 0) {
                float hue = overlayMessageTime - partialTicks;
                int opacity = (int) (hue * 255.0F / 20.0F);
                if (opacity > 255) {
                    opacity = 255;
                }
                if (opacity > 8) {
                    matrices.pushPose();
                    matrices.translate(width / 2.0, height - 68, 0);
                    Font font = mc.font;
                    Blending.DEFAULT_1_0.apply();
                    int color = gui.animateOverlayMessageColor() ? Mth.hsvToRgb(hue / 50.0F, 0.7F, 0.6F) & 0xff_ffff : 0xff_ffff;
                    Component message = gui.getOverlayMessage();
                    assert message != null;
                    gui.drawBackdrop(matrices, font, -4, font.width(message), 0xff_ffff | opacity << 24);
                    font.drawShadow(matrices, message.getVisualOrderText(), -font.width(message) / 2.0f, -4, color | opacity << 24);
                    matrices.popPose();
                }
            }
        }
    }

    public static void register() {
        Overlays.registerGameOverlayTop(SCREEN_EFFECTS, VanillaOverlays::screenEffects);
        Overlays.registerGameOverlayTop(VIGNETTE, VanillaOverlays::vignette);
        Overlays.registerGameOverlayTop(SPYGLASS, VanillaOverlays::spyglass);
        Overlays.registerGameOverlayTop(PUMPKIN, VanillaOverlays::pumpkin);
        Overlays.registerGameOverlayTop(FROSTBITE, VanillaOverlays::frostbite);
        Overlays.registerGameOverlayTop(PORTAL, VanillaOverlays::portal);
        Overlays.registerHudOverlayTop(HOTBAR, VanillaOverlays::hotbar);
        Overlays.registerHudOverlayTop(BOSS_HEALTH, VanillaOverlays::bossHealth);
        Overlays.registerHudOverlayTop(MOUNT_HEALTH, VanillaOverlays::mountHealth);
        Overlays.registerHudOverlayTop(AIR_LEVEL, VanillaOverlays::airLevel);
        Overlays.registerHudOverlayTop(JUMP_BAR, VanillaOverlays::jumpBar);
        Overlays.registerHudOverlayTop(ITEM_NAME, VanillaOverlays::itemName);
        Overlays.registerGameOverlayTop(SLEEP_FADE, VanillaOverlays::sleepFade);
        Overlays.registerHudOverlayTop(TEXT_COLUMNS, VanillaOverlays::textColumns);
        Overlays.registerHudOverlayTop(FPS_GRAPH, VanillaOverlays::fpsGraph);
        Overlays.registerHudOverlayTop(RECORD, VanillaOverlays::record);
        Overlays.registerHudOverlayTop(SUBTITLES, VanillaOverlays::subtitles);
        Overlays.registerHudOverlayTop(TITLE_TEXT, VanillaOverlays::titleText);
        Overlays.registerHudOverlayTop(SCOREBOARD, VanillaOverlays::scoreboard);
        Overlays.registerHudOverlayTop(CHAT_HISTORY, VanillaOverlays::chatHistory);
        Overlays.registerHudOverlayTop(PLAYER_LIST, VanillaOverlays::playerList);
        Evolution.info("Registered Vanilla Overlays");
    }

    private static void scoreboard(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        assert mc.level != null;
        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective objective = null;
        assert mc.player != null;
        PlayerTeam team = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
        if (team != null) {
            int slot = team.getColor().getId();
            if (slot >= 0) {
                objective = scoreboard.getDisplayObjective(3 + slot);
            }
        }
        Objective otherObjective = objective != null ? objective : scoreboard.getDisplayObjective(1);
        if (otherObjective != null) {
            gui.displayScoreboardSidebar(matrices, otherObjective);
        }
    }

    private static void screenEffects(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        Entity entity = mc.getCameraEntity();
        if (entity == null || entity instanceof LivingEntity living && living.isSleeping()) {
            return;
        }
        if (!entity.noPhysics) {
            Vec3 eyePos = entity.getEyePosition(partialTicks);
            POS.set(eyePos.x, eyePos.y, eyePos.z);
            BlockState state = entity.level.getBlockState(POS);
            if (state.getRenderShape() != RenderShape.INVISIBLE && state.isViewBlocking(entity.level, POS)) {
                GUIUtils.renderTex(width, height, mc.getBlockRenderer().getBlockModelShaper().getParticleIcon(state), matrices);
            }
        }
        if (!(entity instanceof Player player && player.isSpectator())) {
            if (entity.isEyeInFluid(FluidTags.WATER)) {
                GUIUtils.renderWater(width, height, entity, matrices);
            }
            if (entity.isOnFire()) {
                GUIUtils.renderFire(width, height, matrices);
            }
        }
    }

    private static void sleepFade(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        assert mc.player != null;
        if (mc.player.getSleepTimer() > 0) {
            gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false);
            int sleepTime = mc.player.getSleepTimer();
            float opacity = sleepTime / 100.0F;
            if (opacity > 1.0F) {
                opacity = 1.0F - (sleepTime - 100) / 10.0F;
            }
            int color = (int) (220.0F * opacity) << 24 | 0x10_1020;
            GuiComponent.fill(matrices, 0, 0, width, height, color);
        }
    }

    private static void spyglass(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!(mc.getCameraEntity() instanceof Player player && player.isScoping())) {
            gui.setScopeScale(0.5f);
            return;
        }
        gui.setupOverlayRenderState(Blending.DEFAULT_1_0, false, Gui.SPYGLASS_SCOPE_LOCATION);
        float deltaFrame = mc.getDeltaFrameTime();
        gui.setScopeScale(Mth.lerp(0.5F * deltaFrame, gui.getScopeScale(), 1.125F));
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        double minDim = Math.min(width, height);
        double mult = Math.min(width / minDim, height / minDim) * gui.getScopeScale();
        double size = minDim * mult;
        double x0 = (width - size) / 2.0F;
        double y0 = (height - size) / 2.0F;
        double x1 = x0 + size;
        double y1 = y0 + size;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(x0, y1, -90).uv(0, 1).endVertex();
        builder.vertex(x1, y1, -90).uv(1, 1).endVertex();
        builder.vertex(x1, y0, -90).uv(1, 0).endVertex();
        builder.vertex(x0, y0, -90).uv(0, 0).endVertex();
        tesselator.end();
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
        RenderSystem.disableTexture();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(0, height, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(width, height, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(width, y1, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(0, y1, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(0, y0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(width, y0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(width, 0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(0, 0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(0, y1, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(x0, y1, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(x0, y0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(0, y0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(x1, y1, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(width, y1, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(width, y0, -90).color(0, 0, 0, 255).endVertex();
        builder.vertex(x1, y0, -90).color(0, 0, 0, 255).endVertex();
        tesselator.end();
    }

    private static void subtitles(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        gui.renderSubtitles(matrices);
    }

    private static void textColumns(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        gui.renderHUDText(matrices);
    }

    private static void titleText(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (mc.options.hideGui) {
            return;
        }
        Component title = gui.getTitle();
        int titleTime = gui.getTitleTime();
        if (title != null && titleTime > 0) {
            Font font = mc.font;
            float age = titleTime - partialTicks;
            int opacity = 255;
            int titleFadeOutTime = gui.getTitleFadeOutTime();
            int titleStayTime = gui.getTitleStayTime();
            if (titleTime > titleFadeOutTime + titleStayTime) {
                int titleFadeInTime = gui.getTitleFadeInTime();
                float f3 = (titleFadeInTime + titleStayTime + titleFadeOutTime) - age;
                opacity = (int) (f3 * 255.0F / titleFadeInTime);
            }
            if (titleTime <= titleFadeOutTime) {
                opacity = (int) (age * 255.0F / titleFadeOutTime);
            }
            opacity = Mth.clamp(opacity, 0, 255);
            if (opacity > 8) {
                Blending.DEFAULT_1_0.apply();
                matrices.pushPose();
                matrices.translate(width / 2.0, height / 2.0, 0);
                matrices.pushPose();
                matrices.scale(4.0F, 4.0F, 4.0F);
                int l = opacity << 24 & 0xff00_0000;
                font.drawShadow(matrices, title.getVisualOrderText(), -font.width(title) / 2.0f, -10.0F, 16_777_215 | l);
                matrices.popPose();
                Component subtitle = gui.getSubtitle();
                if (subtitle != null) {
                    matrices.pushPose();
                    matrices.scale(2.0F, 2.0F, 2.0F);
                    font.drawShadow(matrices, subtitle.getVisualOrderText(), -font.width(subtitle) / 2.0f, 5.0F, 16_777_215 | l);
                    matrices.popPose();
                }
                matrices.popPose();
            }
        }
    }

    private static void vignette(Minecraft mc, EvolutionGui gui, PoseStack matrices, float partialTicks, int width, int height) {
        if (!Minecraft.useFancyGraphics()) {
            return;
        }
        gui.setupOverlayRenderState(Blending.VIGNETTE, false, Gui.VIGNETTE_LOCATION);
        Entity entity = mc.getCameraEntity();
        assert mc.level != null;
        WorldBorder border = mc.level.getWorldBorder();
        assert entity != null;
        float distToBorder = (float) border.getDistanceToBorder(entity);
        double d0 = Math.min(border.getLerpSpeed() * border.getWarningTime() * 1_000, Math.abs(border.getLerpTarget() - border.getSize()));
        double d1 = Math.max(border.getWarningBlocks(), d0);
        if (distToBorder < d1) {
            distToBorder = 1.0F - (float) (distToBorder / d1);
        }
        else {
            distToBorder = 0.0F;
        }
        if (distToBorder > 0.0F) {
            distToBorder = Mth.clamp(distToBorder, 0.0F, 1.0F);
            RenderSystem.setShaderColor(0.0F, distToBorder, distToBorder, 1.0F);
        }
        else {
            float brightness = gui.vignetteBrightness;
            brightness = Mth.clamp(brightness, 0.0F, 1.0F);
            RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);
        }
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(0, height, -90).uv(0, 1).endVertex();
        builder.vertex(width, height, -90).uv(1, 1).endVertex();
        builder.vertex(width, 0, -90).uv(1, 0).endVertex();
        builder.vertex(0, 0, -90).uv(0, 0).endVertex();
        tesselator.end();
    }
}
