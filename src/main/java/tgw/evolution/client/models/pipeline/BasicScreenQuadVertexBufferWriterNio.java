package tgw.evolution.client.models.pipeline;

import java.nio.ByteBuffer;

public class BasicScreenQuadVertexBufferWriterNio extends VertexBufferWriterNio implements IBasicScreenQuadVertexSink {

    public BasicScreenQuadVertexBufferWriterNio(IVertexBufferView backingBuffer) {
        super(backingBuffer, VanillaVertexTypes.BASIC_SCREEN_QUADS);
    }

    @Override
    public void writeQuad(float x, float y, float z, int color) {
        int i = this.writeOffset;
        ByteBuffer buf = this.byteBuffer;
        buf.putFloat(i, x);
        buf.putFloat(i + 4, y);
        buf.putFloat(i + 8, z);
        buf.putInt(i + 12, color);
        this.advance();
    }
}
