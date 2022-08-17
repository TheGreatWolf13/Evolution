package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ForgeIngameGui.class)
public abstract class ForgeIngameGuiMixin extends Gui {

    public ForgeIngameGuiMixin(Minecraft pMinecraft) {
        super(pMinecraft);
    }

    @Inject(method = "shouldDrawSurvivalElements", at = @At("HEAD"), cancellable = true)
    private void onShouldDrawSurvivalElements(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.minecraft.gameMode.canHurtPlayer());
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()" +
                                                                        "Lnet/minecraft/world/entity/Entity;"))
    private Entity proxyRenderAir(Minecraft mc) {
        return mc.player;
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()" +
                                                                                "Lnet/minecraft/world/entity/Entity;"))
    private Entity proxyRenderHealthMount(Minecraft mc) {
        return mc.player;
    }
}
