package tgw.evolution.client.gui.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;
import tgw.evolution.util.toast.ToastHolder;

public abstract class ToastGeneric<T extends ToastHolder> implements Toast {

    private final OList<T> toasts = new OArrayList<>();
    private boolean changed;
    private long lastChanged;

    protected ToastGeneric(T holder) {
        this.toasts.add(holder);
    }

    protected void addItem(T holder) {
        this.toasts.add(holder);
        this.changed = true;
    }

    protected abstract Component getDescription();

    protected abstract Component getTitle();

    @Override
    public Visibility render(PoseStack matrices, ToastComponent gui, long time) {
        if (this.changed) {
            this.lastChanged = time;
            this.changed = false;
        }
        if (this.toasts.isEmpty()) {
            return Visibility.HIDE;
        }
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        gui.blit(matrices, 0, 0, 0, 32, this.width(), this.height());
        gui.getMinecraft().font.draw(matrices, this.getTitle(), 30.0F, 7.0F, 0xff50_0050);
        gui.getMinecraft().font.draw(matrices, this.getDescription(), 30.0F, 18.0F, 0xff00_0000);
        ToastHolder holder = this.toasts.get((int) (time / Math.max(1L, 5_000L / this.toasts.size()) % this.toasts.size()));
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.pushPose();
        internalMat.scale(0.6F, 0.6F, 1.0F);
        gui.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(holder.getShowItem(), 3, 3);
        internalMat.popPose();
        RenderSystem.applyModelViewMatrix();
        gui.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(holder.getToastSymbol(), 8, 8);
        return time - this.lastChanged >= 5_000L ? Visibility.HIDE : Visibility.SHOW;
    }
}
