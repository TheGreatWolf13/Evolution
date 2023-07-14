package tgw.evolution.mixin;

import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.client.gui.advancements.ScreenAdvancements;
import tgw.evolution.client.gui.stats.ScreenStats;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {

    public MixinPauseScreen(Component component) {
        super(component);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace Advancements screen
     */
    @Overwrite
    private void createPauseMenu() {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        assert this.minecraft.level != null;
        int xLeft = this.width / 2 - 102;
        int xMiddle = this.width / 2 + 4;
        int y = this.height / 4 + 8;
        this.addRenderableWidget(new Button(xLeft, y, 204, 20, new TranslatableComponent("menu.returnToGame"), b -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        y += 24;
        this.addRenderableWidget(new Button(xLeft, y, 98, 20, new TranslatableComponent("gui.advancements"), b -> {
            this.minecraft.setScreen(new ScreenAdvancements(this.minecraft.player.connection.getAdvancements()));
        }));
        this.addRenderableWidget(new Button(xMiddle, y, 98, 20, new TranslatableComponent("gui.stats"), b -> {
            this.minecraft.setScreen(new ScreenStats(this.minecraft.player.getStats()));
        }));
        y += 24;
        this.addRenderableWidget(new Button(xLeft, y, 98, 20, new TranslatableComponent("menu.sendFeedback"), b -> {
            this.minecraft.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getPlatform().openUri("https://github.com/MGSchultz-13/Evolution/discussions/categories/feedback");
                }
                this.minecraft.setScreen(this);
            }, "https://github.com/MGSchultz-13/Evolution/discussions/categories/feedback", true));
        }));
        this.addRenderableWidget(new Button(xMiddle, y, 98, 20, new TranslatableComponent("menu.reportBugs"), b -> {
            this.minecraft.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getPlatform().openUri("https://github.com/MGSchultz-13/Evolution/issues");
                }
                this.minecraft.setScreen(this);
            }, "https://github.com/MGSchultz-13/Evolution/issues", true));
        }));
        y += 24;
        Button lanBtn = this.addRenderableWidget(new Button(xLeft, y, 204, 20, new TranslatableComponent("menu.shareToLan"), b -> {
            this.minecraft.setScreen(new ShareToLanScreen(this));
        }));
        //noinspection ConstantConditions
        lanBtn.active = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();
        y += 24;
        this.addRenderableWidget(new Button(xLeft, y, 98, 20, new TranslatableComponent("menu.options"), b -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));
//        this.addRenderableWidget(new Button(xMiddle, y, 98, 20, EvolutionTexts.GUI_MENU_MOD_OPTIONS, b -> {
//            this.minecraft.setScreen(new ScreenModList());
//        }));
        Component quit = this.minecraft.isLocalServer() ?
                         new TranslatableComponent("menu.returnToMenu") :
                         new TranslatableComponent("menu.disconnect");
        y += 24;
        this.addRenderableWidget(new Button(xLeft, y, 204, 20, quit, b -> {
            boolean local = this.minecraft.isLocalServer();
            boolean realms = this.minecraft.isConnectedToRealms();
            b.active = false;
            this.minecraft.level.disconnect();
            if (local) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
            }
            else {
                this.minecraft.clearLevel();
            }
            TitleScreen titleScreen = new TitleScreen();
            if (local) {
                this.minecraft.setScreen(titleScreen);
            }
            else if (realms) {
                this.minecraft.setScreen(new RealmsMainScreen(titleScreen));
            }
            else {
                this.minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
            }
        }));
    }
}
