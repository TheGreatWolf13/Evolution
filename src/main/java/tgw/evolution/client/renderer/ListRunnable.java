package tgw.evolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import tgw.evolution.util.collection.RArrayList;

public class ListRunnable extends RArrayList<RunnableEffectAmplifier> {

    protected int validSize;

    public void setNew(PoseStack matrices, float x, float y, String text, Font font) {
        RunnableEffectAmplifier r;
        if (this.validSize >= this.size()) {
            r = new RunnableEffectAmplifier();
            this.add(this.validSize, r);
        }
        else {
            r = this.get(this.validSize);
            if (r == null) {
                r = new RunnableEffectAmplifier();
                this.add(this.validSize, r);
            }
        }
        r.set(matrices, x, y, text, font);
        this.validSize++;
    }

    public void softClear() {
        for (int i = 0, l = this.size(); i < l; i++) {
            RunnableEffectAmplifier r = this.get(i);
            if (r != null) {
                r.discard();
            }
        }
        this.validSize = 0;
    }
}
