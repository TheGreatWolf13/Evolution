package tgw.evolution.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {

    public MixinAbstractContainerScreen(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void containerTick();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public final void tick() {
        super.tick();
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        if (!this.minecraft.isMultiplayerPaused() && this.minecraft.player.isAlive() && !this.minecraft.player.isRemoved()) {
            this.containerTick();
        }
        else {
            this.minecraft.player.closeContainer();
        }
    }
}
