package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(targets = "net.minecraftforge.client.gui.ForgeIngameGui$ForgeDebugScreenOverlay")
public abstract class ForgeIngameGui_ForgeDebugScreenOverlay extends DebugScreenOverlay {

    public ForgeIngameGui_ForgeDebugScreenOverlay(Minecraft pMinecraft) {
        super(pMinecraft);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()" +
                                                                     "Lnet/minecraft/world/entity/Entity;"))
    private Entity proxyUpdate(Minecraft mc) {
        return mc.player;
    }
}
