package tgw.evolution.mixin;

import com.google.common.util.concurrent.Runnables;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    @Shadow @Final public static Component COPYRIGHT_TEXT;
    @Shadow @Final private static ResourceLocation ACCESSIBILITY_TEXTURE;
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
     * @author TheGreatWolf
     * @reason Add Mods button
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
//            this.addRenderableWidget(new Button(this.width / 2 - 100, l + 24 * 2, 98, 20, new TranslatableComponent("evolution.gui.menu.mods"),
//                                                b -> this.minecraft.setScreen(new ScreenModList())));
        }
        this.addRenderableWidget(
                new ImageButton(this.width / 2 - 124, l + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256,
                                b -> this.minecraft.setScreen(
                                        new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())),
                                new TranslatableComponent("narrator.button.language")));
        this.addRenderableWidget(new Button(this.width / 2 - 100, l + 72 + 12, 98, 20, new TranslatableComponent("menu.options"),
                                            b -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));
        this.addRenderableWidget(
                new Button(this.width / 2 + 2, l + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), b -> this.minecraft.stop()));
        this.addRenderableWidget(new ImageButton(this.width / 2 + 104, l + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64,
                                                 b -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)),
                                                 new TranslatableComponent("narrator.button.accessibility")));
        this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT,
                                                     b -> this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing())), this.font));
        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        if (this.realmsNotificationsEnabled()) {
            assert this.realmsNotificationsScreen != null;
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }
        if (!this.minecraft.is64Bit()) {
            CompletableFuture<Boolean> future = this.warning32Bit != null ?
                                                this.warning32Bit.realmsSubscriptionFuture :
                                                CompletableFuture.supplyAsync(this::hasRealmsSubscription, Util.backgroundExecutor());
            this.warning32Bit = new TitleScreen.Warning32Bit(
                    MultiLineLabel.create(this.font, new TranslatableComponent("title.32bit.deprecation"), 350, 2), this.width / 2, l - 24, future);
        }
    }

    @Shadow
    protected abstract boolean realmsNotificationsEnabled();
}
