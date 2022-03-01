package tgw.evolution.patches;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.FormattedText;

public interface IFontPatch {

    void drawWordWrap(PoseStack matrices, FormattedText text, float x, float y, int maxWidth, int color, boolean shadow);
}
