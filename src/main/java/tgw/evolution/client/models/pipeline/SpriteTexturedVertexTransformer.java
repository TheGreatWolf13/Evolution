package tgw.evolution.client.models.pipeline;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class SpriteTexturedVertexTransformer<T extends IVertexSink> extends AbstractVertexTransformer<T> {
    private final float uMaxMin;
    private final float uMin;
    private final float vMaxMin;
    private final float vMin;

    public SpriteTexturedVertexTransformer(T delegate, TextureAtlasSprite sprite) {
        super(delegate);
        this.uMin = sprite.getU0();
        this.vMin = sprite.getV0();
        this.uMaxMin = sprite.getU1() - this.uMin;
        this.vMaxMin = sprite.getV1() - this.vMin;
    }

    protected float transformTextureU(float u) {
        return this.uMaxMin * u + this.uMin;
    }

    protected float transformTextureV(float v) {
        return this.vMaxMin * v + this.vMin;
    }

    public static class Quad extends SpriteTexturedVertexTransformer<IQuadVertexSink> implements IQuadVertexSink {

        public Quad(IQuadVertexSink delegate, TextureAtlasSprite sprite) {
            super(delegate, sprite);
        }

        @Override
        public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
            u = this.transformTextureU(u);
            v = this.transformTextureV(v);
            this.delegate.writeQuad(x, y, z, color, u, v, light, overlay, normal);
        }
    }

    public static class Particle extends SpriteTexturedVertexTransformer<IParticleVertexSink> implements IParticleVertexSink {
        public Particle(IParticleVertexSink delegate, TextureAtlasSprite sprite) {
            super(delegate, sprite);
        }

        @Override
        public void writeParticle(float x, float y, float z, float u, float v, int color, int light) {
            u = this.transformTextureU(u);
            v = this.transformTextureV(v);
            this.delegate.writeParticle(x, y, z, u, v, color, light);
        }
    }

    public static class Glyph extends SpriteTexturedVertexTransformer<IGlyphVertexSink> implements IGlyphVertexSink {
        public Glyph(IGlyphVertexSink delegate, TextureAtlasSprite sprite) {
            super(delegate, sprite);
        }

        @Override
        public void writeGlyph(float x, float y, float z, int color, float u, float v, int light) {
            u = this.transformTextureU(u);
            v = this.transformTextureV(v);
            this.delegate.writeGlyph(x, y, z, color, u, v, light);
        }
    }
}
