package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ForgeIngameGui.class)
public abstract class ForgeIngameGuiMixin extends Gui {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    public int left_height;
    @Shadow
    public int right_height;
    @Shadow
    private RenderGameOverlayEvent eventParent;
    @Shadow
    private Font font;

    public ForgeIngameGuiMixin(Minecraft pMinecraft) {
        super(pMinecraft);
    }

    @Inject(method = "lambda$static$24", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onLambda(ForgeIngameGui gui, PoseStack matrices, float partialTicks, int width, int height, CallbackInfo ci) {
        if (gui.minecraft.options.hideGui) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldDrawSurvivalElements", at = @At("HEAD"), cancellable = true)
    private void onShouldDrawSurvivalElements(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.minecraft.gameMode.canHurtPlayer());
    }

    @Shadow
    protected abstract void post(RenderGameOverlayEvent.ElementType type, PoseStack mStack);

    @Shadow
    protected abstract void post(IIngameOverlay overlay, PoseStack mStack);

    @Shadow
    protected abstract boolean pre(RenderGameOverlayEvent.ElementType type, PoseStack mStack);

    @Shadow
    protected abstract boolean pre(IIngameOverlay overlay, PoseStack mStack);

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

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible
     */
    @Override
    @Overwrite
    public void render(PoseStack matrices, float partialTicks) {
        this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        this.eventParent = new RenderGameOverlayEvent(matrices, partialTicks, this.minecraft.getWindow());
        this.right_height = 39;
        this.left_height = 39;
        if (this.pre(ALL, matrices)) {
            return;
        }
        this.font = this.minecraft.font;
        this.random.setSeed(this.tickCount * 312_871L);
        for (OverlayRegistry.OverlayEntry entry : OverlayRegistry.orderedEntries()) {
            try {
                if (!entry.isEnabled()) {
                    continue;
                }
                IIngameOverlay overlay = entry.getOverlay();
                if (this.pre(overlay, matrices)) {
                    continue;
                }
                overlay.render((ForgeIngameGui) (Object) this, matrices, partialTicks, this.screenWidth, this.screenHeight);
                this.post(overlay, matrices);
            }
            catch (Exception e) {
                LOGGER.error("Error rendering overlay '{}'", entry.getDisplayName(), e);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.post(ALL, matrices);
    }
}
