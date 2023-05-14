package tgw.evolution.mixin;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.INarratorChatListenerPatch;

import javax.annotation.Nullable;

@Mixin(SimpleOptionsSubScreen.class)
public abstract class SimpleOptionsSubScreenMixin extends OptionsSubScreen {

    @Shadow
    private OptionsList list;
    @Shadow
    @Nullable
    private AbstractWidget narratorButton;
    @Shadow
    @Final
    private Option[] smallOptions;

    public SimpleOptionsSubScreenMixin(Screen pLastScreen,
                                       Options pOptions,
                                       Component pTitle) {
        super(pLastScreen, pOptions, pTitle);
    }

    @Shadow
    protected abstract void createFooter();

    /**
     * @author TheGreatWolf
     * @reason Fix narrator button not working
     */
    @Override
    @Overwrite
    protected void init() {
        assert this.minecraft != null;
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSmall(this.smallOptions);
        this.addWidget(this.list);
        this.createFooter();
        this.narratorButton = this.list.findOption(Option.NARRATOR);
        if (this.narratorButton != null) {
            this.narratorButton.active = ((INarratorChatListenerPatch) NarratorChatListener.INSTANCE).isAvailable();
        }
    }
}
