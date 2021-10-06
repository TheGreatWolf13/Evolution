package tgw.evolution.client.gui.toast;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.util.text.ITextComponent;
import tgw.evolution.util.toast.ToastHolder;

import java.util.List;

public abstract class ToastGeneric<T extends ToastHolder> implements IToast {

    private final List<T> toasts = Lists.newArrayList();
    private boolean changed;
    private long lastChanged;

    protected ToastGeneric(T holder) {
        this.toasts.add(holder);
    }

    protected void addItem(T holder) {
        this.toasts.add(holder);
        this.changed = true;
    }

    protected abstract ITextComponent getDescription();

    protected abstract ITextComponent getTitle();

    @Override
    public Visibility render(MatrixStack matrices, ToastGui gui, long time) {
        if (this.changed) {
            this.lastChanged = time;
            this.changed = false;
        }
        if (this.toasts.isEmpty()) {
            return IToast.Visibility.HIDE;
        }
        gui.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        gui.blit(matrices, 0, 0, 0, 32, this.width(), this.height());
        gui.getMinecraft().font.draw(matrices, this.getTitle(), 30.0F, 7.0F, 0xff50_0050);
        gui.getMinecraft().font.draw(matrices, this.getDescription(), 30.0F, 18.0F, 0xff00_0000);
        ToastHolder holder = this.toasts.get((int) (time / Math.max(1L, 5_000L / this.toasts.size()) % this.toasts.size()));
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.6F, 0.6F, 1.0F);
        gui.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(holder.getShowItem(), 3, 3);
        RenderSystem.popMatrix();
        gui.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(holder.getToastSymbol(), 8, 8);
        return time - this.lastChanged >= 5_000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
