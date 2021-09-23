package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.patches.IMinecraftPatch;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends Screen {

    public ContainerScreenMixin(ITextComponent title) {
        super(title);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;isAlive()Z", ordinal = 0))
    private boolean tickProxy(ClientPlayerEntity clientPlayerEntity) {
        if (((IMinecraftPatch) Minecraft.getInstance()).isMultiplayerPaused()) {
            return false;
        }
        return clientPlayerEntity.isAlive();
    }
}
