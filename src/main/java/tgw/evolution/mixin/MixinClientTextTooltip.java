package tgw.evolution.mixin;

import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientTextTooltip.class)
public abstract class MixinClientTextTooltip implements ClientTooltipComponent {

    @Shadow @Final private FormattedCharSequence text;

    @Override
    @Overwrite
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        font.drawInBatch(this.text, x, y, 0xffff_ffff, true, matrix, buffer, false, 0x0, 0xff_00ff);
    }
}
