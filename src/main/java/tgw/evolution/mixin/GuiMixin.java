package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {

    @Shadow
    @Final
    public Minecraft minecraft;

    @Redirect(method = "getPlayerVehicleWithHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getCameraPlayer()" +
                                                                                         "Lnet/minecraft/world/entity/player/Player;"))
    private Player proxyGetPlayerVehicleWithHealth(Gui gui) {
        return gui.minecraft.player;
    }

    @Redirect(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getCameraPlayer()" +
                                                                           "Lnet/minecraft/world/entity/player/Player;"))
    private Player proxyRenderHotbar(Gui gui) {
        return gui.minecraft.player;
    }
}
