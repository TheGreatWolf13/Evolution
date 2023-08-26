package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.FormattedText;

public interface PatchFont {

    default void drawWordWrap(PoseStack matrices, FormattedText text, float x, float y, int maxWidth, int color, boolean shadow) {
        throw new AbstractMethodError();
    }

    default void drawWordWrapCenter(PoseStack matrices, FormattedText formattedText, int x, int y, int maxWidth, int color, boolean shadow) {
        throw new AbstractMethodError();
    }
}
