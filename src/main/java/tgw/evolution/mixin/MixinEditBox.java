package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiFunction;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends AbstractWidget {

    @Shadow private boolean bordered;
    @Shadow private int cursorPos;
    @Shadow private int displayPos;
    @Shadow @Final private Font font;
    @Shadow private BiFunction<String, Integer, FormattedCharSequence> formatter;
    @Shadow private int frame;
    @Shadow private int highlightPos;
    @Shadow private boolean isEditable;
    @Shadow private @Nullable String suggestion;
    @Shadow private int textColor;
    @Shadow private int textColorUneditable;
    @Shadow private String value;

    public MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Shadow
    public abstract int getInnerWidth();

    @Shadow
    protected abstract int getMaxLength();

    @Shadow
    protected abstract boolean isBordered();

    @Shadow
    public abstract boolean isVisible();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (!this.isVisible()) {
            return;
        }
        RenderSystem.enableDepthTest();
        if (this.isBordered()) {
            fill(matrices, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, this.isFocused() ? -1 : 0xffa0_a0a0);
            fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0xff00_0000);
        }
        int k = this.isEditable ? this.textColor : this.textColorUneditable;
        int l = this.cursorPos - this.displayPos;
        int m = this.highlightPos - this.displayPos;
        String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        boolean bl = l >= 0 && l <= string.length();
        boolean bl2 = this.isFocused() && this.frame / 6 % 2 == 0 && bl;
        int n = this.bordered ? this.x + 4 : this.x;
        int o = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
        int p = n;
        if (m > string.length()) {
            m = string.length();
        }
        if (!string.isEmpty()) {
            String string2 = bl ? string.substring(0, l) : string;
            p = this.font.drawShadow(matrices, this.formatter.apply(string2, this.displayPos), n, o, k);
        }
        boolean bl3 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
        int q = p;
        if (!bl) {
            q = l > 0 ? n + this.width : n;
        }
        else if (bl3) {
            q = p - 1;
            --p;
        }
        if (!string.isEmpty() && bl && l < string.length()) {
            this.font.drawShadow(matrices, this.formatter.apply(string.substring(l), this.cursorPos), p, o, k);
        }
        if (!bl3 && this.suggestion != null) {
            this.font.drawShadow(matrices, this.suggestion, q - 1, o, 0xff80_8080);
        }
        if (bl2) {
            if (bl3) {
                GuiComponent.fill(matrices, q, o - 1, q + 1, o + 1 + 9, 0xffd0_d0d0);
            }
            else {
                this.font.drawShadow(matrices, "_", q, o, k);
            }
        }
        if (m != l) {
            int r = n + this.font.width(string.substring(0, m));
            this.renderHighlight(q, o - 1, r - 1, o + 1 + 9);
        }
    }

    @Shadow
    protected abstract void renderHighlight(int i, int j, int k, int l);
}
