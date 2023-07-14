package tgw.evolution.client.renderer;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSink;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import javax.annotation.Nullable;

public class StringSink implements FormattedCharSink {

    public static final StringSink INSTANCE = new StringSink();
    float x;
    float y;
    private float a;
    private float b;
    private MultiBufferSource bufferSource;
    private float dimFactor;
    private boolean dropShadow;
    @Nullable
    private OList<BakedGlyph.Effect> effects;
    private Font font;
    private float g;
    private Font.DisplayMode mode;
    private int packedLightCoords;
    private Matrix4f pose;
    private float r;

    protected StringSink() {
    }

    @Override
    public boolean accept(int pPositionInCurrentSequence, Style style, int ch) {
        FontSet fontSet = this.font.getFontSet(style.getFont());
        GlyphInfo info = fontSet.getGlyphInfo(ch);
        BakedGlyph glyph = style.isObfuscated() && ch != 32 ? fontSet.getRandomGlyph(info) : fontSet.getGlyph(ch);
        boolean bold = style.isBold();
        TextColor color = style.getColor();
        float r;
        float g;
        float b;
        if (color != null) {
            int i = color.getValue();
            r = (i >> 16 & 255) / 255.0F * this.dimFactor;
            g = (i >> 8 & 255) / 255.0F * this.dimFactor;
            b = (i & 255) / 255.0F * this.dimFactor;
        }
        else {
            r = this.r;
            g = this.g;
            b = this.b;
        }
        if (!(glyph instanceof EmptyGlyph)) {
            float boldOffset = bold ? info.getBoldOffset() : 0.0F;
            float shadowOffset = this.dropShadow ? info.getShadowOffset() : 0.0F;
            VertexConsumer buffer = this.bufferSource.getBuffer(glyph.renderType(this.mode));
            this.font.renderChar(glyph, bold, style.isItalic(), boldOffset, this.x + shadowOffset, this.y + shadowOffset, this.pose, buffer,
                                 r, g, b, this.a, this.packedLightCoords);
        }
        float advance = info.getAdvance(bold);
        float offset = this.dropShadow ? 1.0F : 0.0F;
        if (style.isStrikethrough()) {
            this.addEffect(
                    new BakedGlyph.Effect(this.x + offset - 1.0F, this.y + offset + 4.5F, this.x + offset + advance, this.y + offset + 4.5F - 1.0F,
                                          0.01F, r, g, b, this.a));
        }
        if (style.isUnderlined()) {
            this.addEffect(
                    new BakedGlyph.Effect(this.x + offset - 1.0F, this.y + offset + 9.0F, this.x + offset + advance, this.y + offset + 9.0F - 1.0F,
                                          0.01F, r, g, b, this.a));
        }
        this.x += advance;
        return true;
    }

    private void addEffect(BakedGlyph.Effect pEffect) {
        if (this.effects == null) {
            this.effects = new OArrayList<>();
        }
        this.effects.add(pEffect);
    }

    public float finish(int bgColor, float x) {
        if (bgColor != 0) {
            float f = (bgColor >> 24 & 255) / 255.0F;
            float f1 = (bgColor >> 16 & 255) / 255.0F;
            float f2 = (bgColor >> 8 & 255) / 255.0F;
            float f3 = (bgColor & 255) / 255.0F;
            this.addEffect(new BakedGlyph.Effect(x - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
        }
        if (this.effects != null) {
            BakedGlyph glyph = this.font.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
            VertexConsumer buffer = this.bufferSource.getBuffer(glyph.renderType(this.mode));
            for (int i = 0, len = this.effects.size(); i < len; i++) {
                glyph.renderEffect(this.effects.get(i), this.pose, buffer, this.packedLightCoords);
            }
        }
        return this.x;
    }

    public void set(Font font,
                    MultiBufferSource buffer,
                    float x,
                    float y,
                    int color,
                    boolean shadow,
                    Matrix4f matrix,
                    Font.DisplayMode mode,
                    int light) {
        this.font = font;
        this.bufferSource = buffer;
        this.x = x;
        this.y = y;
        this.dropShadow = shadow;
        this.dimFactor = shadow ? 0.25F : 1.0F;
        this.r = (color >> 16 & 255) / 255.0F * this.dimFactor;
        this.g = (color >> 8 & 255) / 255.0F * this.dimFactor;
        this.b = (color & 255) / 255.0F * this.dimFactor;
        this.a = (color >> 24 & 255) / 255.0F;
        this.pose = matrix;
        this.mode = mode;
        this.packedLightCoords = light;
        if (this.effects != null) {
            this.effects.clear();
        }
    }

    public void set(Font font,
                    MultiBufferSource buffer,
                    float x,
                    float y,
                    int color,
                    boolean shadow,
                    Matrix4f matrix,
                    boolean transparent,
                    int light) {
        this.set(font, buffer, x, y, color, shadow, matrix, transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, light);
    }
}
