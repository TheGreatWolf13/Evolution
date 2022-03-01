package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IFontPatch;

import java.util.List;

@Mixin(Font.class)
public abstract class FontMixin implements IFontPatch {

    @Shadow
    protected abstract int drawInternal(FormattedCharSequence pReorderingProcessor,
                                        float pX,
                                        float pY,
                                        int pColor,
                                        Matrix4f pMatrix,
                                        boolean pDrawShadow);

    @Override
    public void drawWordWrap(PoseStack matrices, FormattedText text, float x, float y, int maxWidth, int color, boolean shadow) {
        Matrix4f mat = matrices.last().pose();
        for (FormattedCharSequence charSequence : this.split(text, maxWidth)) {
            this.drawInternal(charSequence, x, y, color, mat, shadow);
            y += 9;
        }
    }

    @Shadow
    public abstract List<FormattedCharSequence> split(FormattedText pText, int pMaxWidth);
}
