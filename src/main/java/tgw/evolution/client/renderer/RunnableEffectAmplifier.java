package tgw.evolution.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

public class RunnableEffectAmplifier implements Runnable {

    private boolean active;
    private @Nullable Font font;
    private @Nullable PoseStack matrices;
    private @Nullable String text;
    private float x;
    private float y;

    public void discard() {
        this.active = false;
        this.font = null;
        this.matrices = null;
        this.text = null;
    }

    @Override
    public void run() {
        if (this.active) {
            assert this.matrices != null;
            assert this.font != null;
            assert this.text != null;
            this.matrices.pushPose();
            this.matrices.scale(0.5f, 0.5f, 0.5f);
            this.font.drawShadow(this.matrices, this.text, (this.x + 3) * 2, (this.y + 17) * 2, 0xff_ffff);
            this.matrices.popPose();
        }
    }

    public void set(PoseStack matrices, float x, float y, String text, Font font) {
        this.active = true;
        this.matrices = matrices;
        this.x = x;
        this.y = y;
        this.text = text;
        this.font = font;
    }
}
