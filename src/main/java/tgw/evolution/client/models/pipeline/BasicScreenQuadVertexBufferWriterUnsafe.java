package tgw.evolution.client.models.pipeline;

import org.lwjgl.system.MemoryUtil;

public class BasicScreenQuadVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements IBasicScreenQuadVertexSink {

    public BasicScreenQuadVertexBufferWriterUnsafe(IVertexBufferView backingBuffer) {
        super(backingBuffer, VanillaVertexTypes.BASIC_SCREEN_QUADS);
    }

    @Override
    public void writeQuad(float x, float y, float z, int color) {
        long i = this.writePointer;
        MemoryUtil.memPutFloat(i, x);
        MemoryUtil.memPutFloat(i + 4, y);
        MemoryUtil.memPutFloat(i + 8, z);
        MemoryUtil.memPutInt(i + 12, color);
        this.advance();
    }
}
