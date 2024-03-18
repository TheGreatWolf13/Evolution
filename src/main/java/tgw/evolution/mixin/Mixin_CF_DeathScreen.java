package tgw.evolution.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.PatchDeathScreen;
import tgw.evolution.util.time.Time;

import java.util.List;

@Mixin(DeathScreen.class)
public abstract class Mixin_CF_DeathScreen extends Screen implements PatchDeathScreen {

    @Mutable @Shadow @Final @RestoreFinal private @Nullable Component causeOfDeath;
    @Shadow private Component deathScore;
    @Shadow private int delayTicker;
    @Shadow @Final @DeleteField private List<Button> exitButtons;
    @Mutable @Shadow @Final @RestoreFinal private boolean hardcore;
    @Unique private Button rageQuitBtn;
    @Unique private Button respawnBtn;
    @Unique private long timeAlive;

    @ModifyConstructor
    public Mixin_CF_DeathScreen(@Nullable Component causeOfDeath, boolean hardcore) {
        super(new TranslatableComponent(hardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.causeOfDeath = causeOfDeath;
        this.hardcore = hardcore;
    }

    @Shadow
    protected abstract void confirmResult(boolean bl);

    @Shadow
    protected abstract void exitToTitleScreen();

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void init() {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        this.delayTicker = 0;
        this.respawnBtn = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 72, 200, 20, this.hardcore ? new TranslatableComponent("deathScreen.spectate") : new TranslatableComponent("deathScreen.respawn"), b -> {
            this.minecraft.player.respawn();
            this.minecraft.setScreen(null);
        }));
        this.respawnBtn.active = false;
        this.rageQuitBtn = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 96, 200, 20, new TranslatableComponent("deathScreen.titleScreen"), b -> {
            if (this.hardcore) {
                this.exitToTitleScreen();
            }
            else {
                ConfirmScreen confirmScreen = new ConfirmScreen(this::confirmResult, new TranslatableComponent("deathScreen.quit.confirm"), TextComponent.EMPTY, new TranslatableComponent("deathScreen.titleScreen"), new TranslatableComponent("deathScreen.respawn"));
                this.minecraft.setScreen(confirmScreen);
                confirmScreen.setDelay(20);
            }
        }));
        this.rageQuitBtn.active = false;
        if (this.timeAlive != -1) {
            this.deathScore = new TranslatableComponent("evolution.gui.death.timeAlive").append(": ").append(Time.getFormattedTime(this.timeAlive).withStyle(ChatFormatting.YELLOW));
        }
        else {
            this.deathScore = EvolutionTexts.EMPTY;
        }
    }

    @Override
    public DeathScreen setTimeAlive(long timeAlive) {
        this.timeAlive = timeAlive;
        return (DeathScreen) (Object) this;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void tick() {
        super.tick();
        ++this.delayTicker;
        if (this.delayTicker == 20) {
            this.respawnBtn.active = true;
            this.rageQuitBtn.active = true;
        }
    }
}
