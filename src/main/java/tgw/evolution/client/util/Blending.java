package tgw.evolution.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public enum Blending {

    DEFAULT(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA),
    ALPHA(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.SRC_ALPHA),
    PRE_ALPHA(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA),
    MULTIPLY(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA),
    ADDITIVE(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE),
    ADDITIVE_DARK(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR),
    OVERLAY_DARK(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE),
    ADDITIVE_ALPHA(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE),
    CONSTANT_ALPHA(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA),
    INVERTED_ADD(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);

    private final GlStateManager.DestFactor alphaDstFactor;
    private final GlStateManager.SourceFactor alphaSrcFactor;
    private final GlStateManager.DestFactor colorDstFactor;
    private final GlStateManager.SourceFactor colorSrcFactor;

    Blending(GlStateManager.SourceFactor src, GlStateManager.DestFactor dst) {
        this(src, dst, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    Blending(GlStateManager.SourceFactor src,
             GlStateManager.DestFactor dst,
             GlStateManager.SourceFactor srcAlpha,
             GlStateManager.DestFactor dstAlpha) {
        this.colorSrcFactor = src;
        this.colorDstFactor = dst;
        this.alphaSrcFactor = srcAlpha;
        this.alphaDstFactor = dstAlpha;
    }

    public void apply() {
        RenderSystem.blendFuncSeparate(this.colorSrcFactor, this.colorDstFactor, this.alphaSrcFactor, this.alphaDstFactor);
    }
}
