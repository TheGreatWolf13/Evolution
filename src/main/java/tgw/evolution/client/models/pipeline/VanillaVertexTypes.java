package tgw.evolution.client.models.pipeline;

public final class VanillaVertexTypes {

    public static final IBufferVertexType<IQuadVertexSink> QUADS = new QuadVertexType();
    public static final IBufferVertexType<ILineVertexSink> LINES = new LineVertexType();
    public static final IBufferVertexType<IGlyphVertexSink> GLYPHS = new GlyphVertexType();
    public static final IBufferVertexType<IParticleVertexSink> PARTICLES = new ParticleVertexType();
    public static final IBufferVertexType<IBasicScreenQuadVertexSink> BASIC_SCREEN_QUADS = new BasicScreenQuadVertexType();

    private VanillaVertexTypes() {
    }
}
