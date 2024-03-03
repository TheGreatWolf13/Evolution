package tgw.evolution.mixin;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    @Shadow @Final public static Component COPYRIGHT_TEXT;
    @Shadow @Final private static ResourceLocation ACCESSIBILITY_TEXTURE;
    @Shadow @Final private static ResourceLocation PANORAMA_OVERLAY;
    @Shadow @Final private static ResourceLocation MINECRAFT_LOGO;
    @Shadow @Final private static ResourceLocation MINECRAFT_EDITION;
    @Unique private String evolutionVersion;
    @Shadow private long fadeInStart;
    @Shadow @Final private boolean fading;
    @Shadow @Final private boolean minceraftEasterEgg;
    @Shadow @Final private PanoramaRenderer panorama;
    @Shadow private @Nullable Screen realmsNotificationsScreen;
    @Shadow private @Nullable String splash;
    @Shadow private @Nullable TitleScreen.Warning32Bit warning32Bit;

    public MixinTitleScreen(Component component) {
        super(component);
    }

    @Shadow
    protected abstract void createDemoMenuOptions(int i, int j);

    @Shadow
    protected abstract void createNormalMenuOptions(int i, int j);

    @Shadow
    protected abstract boolean hasRealmsSubscription();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    @Override
    public void init() {
        assert this.minecraft != null;
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }
        int i = this.font.width(COPYRIGHT_TEXT);
        int j = this.width - i - 2;
        int l = this.height / 4 + 48;
        if (this.minecraft.isDemo()) {
            this.createDemoMenuOptions(l, 24);
        }
        else {
            this.createNormalMenuOptions(l, 24);
        }
        this.addRenderableWidget(new ImageButton(this.width / 2 - 124, l + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256,
                                                 b -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())),
                                                 new TranslatableComponent("narrator.button.language"))
        );
        this.addRenderableWidget(new Button(this.width / 2 - 100, l + 72 + 12, 98, 20, new TranslatableComponent("menu.options"), b -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));
        this.addRenderableWidget(new Button(this.width / 2 + 2, l + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), b -> this.minecraft.stop()));
        this.addRenderableWidget(new ImageButton(this.width / 2 + 104, l + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64,
                                                 b -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)),
                                                 new TranslatableComponent("narrator.button.accessibility"))
        );
        this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, b -> this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing())), this.font));
        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        if (this.realmsNotificationsEnabled()) {
            assert this.realmsNotificationsScreen != null;
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }
        if (!this.minecraft.is64Bit()) {
            CompletableFuture<Boolean> future = this.warning32Bit != null ? this.warning32Bit.realmsSubscriptionFuture : CompletableFuture.supplyAsync(this::hasRealmsSubscription, Util.backgroundExecutor());
            this.warning32Bit = new TitleScreen.Warning32Bit(MultiLineLabel.create(this.font, new TranslatableComponent("title.32bit.deprecation"), 350, 2), this.width / 2, l - 24, future);
        }
        //noinspection OptionalGetWithoutIsPresent
        this.evolutionVersion = "Evolution " + FabricLoader.getInstance().getModContainer(Evolution.MODID).get().getMetadata().getVersion().getFriendlyString();
    }

    @Shadow
    protected abstract boolean realmsNotificationsEnabled();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        assert this.minecraft != null;
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        float g = this.fading ? (Util.getMillis() - this.fadeInStart) / 1_000.0F : 1.0F;
        this.panorama.render(partialTicks, Mth.clamp(g, 0.0F, 1.0F));
        AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? Mth.ceil(Mth.clamp(g, 0.0F, 1.0F)) : 1.0F);
        blit(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float h = this.fading ? Mth.clamp(g - 1.0F, 0.0F, 1.0F) : 1.0F;
        int n = Mth.ceil(h * 255.0F) << 24;
        if ((n & 0xfc00_0000) != 0) {
            int l = this.width / 2 - 137;
            AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
            RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, h);
            if (this.minceraftEasterEgg) {
                this.blitOutlineBlack(l, 30, (integer, integer2) -> {
                    this.blit(matrices, integer, integer2, 0, 0, 99, 44);
                    this.blit(matrices, integer + 99, integer2, 129, 0, 27, 44);
                    this.blit(matrices, integer + 99 + 26, integer2, 126, 0, 3, 44);
                    this.blit(matrices, integer + 99 + 26 + 3, integer2, 99, 0, 26, 44);
                    this.blit(matrices, integer + 155, integer2, 0, 45, 155, 44);
                });
            }
            else {
                this.blitOutlineBlack(l, 30, (integer, integer2) -> {
                    this.blit(matrices, integer, integer2, 0, 0, 155, 44);
                    this.blit(matrices, integer + 155, integer2, 0, 45, 155, 44);
                });
            }
            RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
            blit(matrices, l + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
            if (this.warning32Bit != null) {
                MultiLineLabel label = this.warning32Bit.label;
                int x = this.warning32Bit.x;
                int y = this.warning32Bit.y;
                label.renderBackgroundCentered(matrices, x, y, 9, 2, 0x5520_0000);
                label.renderCentered(matrices, x, y, 9, 0xff_ffff | n);
            }
            if (this.splash != null) {
                matrices.pushPose();
                matrices.translate(this.width / 2.0f + 90, 70, 0);
                matrices.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
                float o = 1.8F - Mth.abs(Mth.sin((Util.getMillis() % 1_000L) / 1_000.0F * Mth.TWO_PI) * 0.1F);
                o = o * 100.0F / (this.font.width(this.splash) + 32);
                matrices.scale(o, o, o);
                drawCenteredString(matrices, this.font, this.splash, 0, -8, 0xff_ff00 | n);
                matrices.popPose();
            }
            String mc = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                mc += " Demo";
            }
            else {
                mc += "release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType();
            }
            if (Minecraft.checkModStatus().shouldReportAsModified()) {
                mc += I18n.get("menu.modded");
            }
            drawString(matrices, this.font, mc, 2, this.height - 10, 0xff_ffff | n);
            drawString(matrices, this.font, this.evolutionVersion, 2, this.height - 20, 0xff_ffff | n);
            List<? extends GuiEventListener> children = this.children();
            for (int i = 0, len = children.size(); i < len; ++i) {
                if (children.get(i) instanceof AbstractWidget widget) {
                    widget.setAlpha(h);
                }
            }
            super.render(matrices, mouseX, mouseY, partialTicks);
            if (this.realmsNotificationsEnabled() && h >= 1.0F) {
                assert this.realmsNotificationsScreen != null;
                this.realmsNotificationsScreen.render(matrices, mouseX, mouseY, partialTicks);
            }
        }
    }
}
