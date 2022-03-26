package tgw.evolution.client.models.pipeline;

public final class VanillaVertexTypes {

    public static final IVanillaVertexType<IQuadVertexSink> QUADS = new QuadVertexType();
    public static final IVanillaVertexType<ILineVertexSink> LINES = new LineVertexType();
    public static final IVanillaVertexType<IGlyphVertexSink> GLYPHS = new GlyphVertexType();
    public static final IVanillaVertexType<IParticleVertexSink> PARTICLES = new ParticleVertexType();
    public static final IVanillaVertexType<IBasicScreenQuadVertexSink> BASIC_SCREEN_QUADS = new BasicScreenQuadVertexType();

    private VanillaVertexTypes() {
    }
}
