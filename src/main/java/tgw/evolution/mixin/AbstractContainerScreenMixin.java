package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.patches.IMinecraftPatch;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    public AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isAlive()Z", ordinal = 0))
    private boolean tickProxy(LocalPlayer player) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused()) {
            return false;
        }
        return player.isAlive();
    }
}
