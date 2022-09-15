package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
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

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()" +
                                                                        "Lnet/minecraft/world/entity/Entity;"))
    private Entity proxyRenderAir(Minecraft mc) {
        assert mc.player != null;
        return mc.player;
    }

    @Redirect(method = "renderHealthMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()" +
                                                                                "Lnet/minecraft/world/entity/Entity;"))
    private Entity proxyRenderHealthMount(Minecraft mc) {
        assert mc.player != null;
        return mc.player;
    }

    @Redirect(method = "renderRecordOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;draw" +
                                                                                  "(Lcom/mojang/blaze3d/vertex/PoseStack;" +
                                                                                  "Lnet/minecraft/util/FormattedCharSequence;FFI)I"))
    private int proxyRenderRecordOverlay(Font font, PoseStack matrices, FormattedCharSequence text, float x, float y, int color) {
        return font.drawShadow(matrices, text, x, y, color);
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
        this.font = this.minecraft.font;
        this.random.setSeed(this.tickCount * 312_871L);
        for (int i = 0, l = OverlayRegistry.orderedEntries().size(); i < l; i++) {
            OverlayRegistry.OverlayEntry entry = OverlayRegistry.orderedEntries().get(i);
            try {
                if (!entry.isEnabled()) {
                    continue;
                }
                //noinspection ConstantConditions
                entry.getOverlay().render((ForgeIngameGui) (Object) this, matrices, partialTicks, this.screenWidth, this.screenHeight);
            }
            catch (Exception e) {
                LOGGER.error("Error rendering overlay '{}'", entry.getDisplayName(), e);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
