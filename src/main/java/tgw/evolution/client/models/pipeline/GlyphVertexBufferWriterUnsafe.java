package tgw.evolution.client.models.pipeline;

import org.lwjgl.system.MemoryUtil;

public class GlyphVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements IGlyphVertexSink {

    public GlyphVertexBufferWriterUnsafe(IVertexBufferView backingBuffer) {
        super(backingBuffer, VanillaVertexTypes.GLYPHS);
    }

    @Override
    public void writeGlyph(float x, float y, float z, int color, float u, float v, int light) {
        long i = this.writePointer;
        MemoryUtil.memPutFloat(i, x);
        MemoryUtil.memPutFloat(i + 4, y);
        MemoryUtil.memPutFloat(i + 8, z);
        MemoryUtil.memPutInt(i + 12, color);
        MemoryUtil.memPutFloat(i + 16, u);
        MemoryUtil.memPutFloat(i + 20, v);
        MemoryUtil.memPutInt(i + 24, light);
        this.advance();
    }
}
