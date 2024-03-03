package tgw.evolution.mixin;

import net.minecraft.client.CycleOption;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.client.gui.controls.ScreenKeyBinds;
import tgw.evolution.config.EvolutionConfig;

@Mixin(ControlsScreen.class)
public abstract class MixinControlsScreen extends OptionsSubScreen {

    @Unique private static final CycleOption<Boolean> TOGGLE_CRAWL = CycleOption.createBinaryOption("key.crawl", new TranslatableComponent("options.key.toggle"), new TranslatableComponent("options.key.hold"), op -> EvolutionConfig.toggleCrawl, (op, op1, value) -> EvolutionConfig.toggleCrawl = value);

    public MixinControlsScreen(Screen pLastScreen, Options pOptions, Component pTitle) {
        super(pLastScreen, pOptions, pTitle);
    }

    /**
     * @author TheGreatWolf
     * @reason Remove auto jump, add toggle crawl
     */
    @Override
    @Overwrite
    public void init() {
        super.init();
        assert this.minecraft != null;
        int leftX = this.width / 2 - 155;
        int rightX = leftX + 160;
        int y = this.height / 6 - 12;
        this.addRenderableWidget(new Button(leftX, y, 150, 20, new TranslatableComponent("options.mouse_settings"),
                                            b -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))));
        this.addRenderableWidget(new Button(rightX, y, 150, 20, new TranslatableComponent("controls.keybinds"),
                                            b -> this.minecraft.setScreen(new ScreenKeyBinds(this, this.options))));
        y += 24;
        this.addRenderableWidget(Option.TOGGLE_CROUCH.createButton(this.options, leftX, y, 150));
        this.addRenderableWidget(Option.TOGGLE_SPRINT.createButton(this.options, rightX, y, 150));
        y += 24;
        this.addRenderableWidget(TOGGLE_CRAWL.createButton(this.options, leftX, y, 150));
        y += 24;
        this.addRenderableWidget(
                new Button(this.width / 2 - 100, y, 200, 20, CommonComponents.GUI_DONE, b -> this.minecraft.setScreen(this.lastScreen)));
    }
}
