package tgw.evolution.mixin;

import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gl.IBufferVertexFormat;
import tgw.evolution.client.models.pipeline.IVertexBufferView;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.patches.IBakedQuadPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.patches.ISortStatePatch;
import tgw.evolution.util.collection.FArrayList;
import tgw.evolution.util.collection.FList;
import tgw.evolution.util.collection.IList;
import tgw.evolution.util.math.ColorABGR;
import tgw.evolution.util.math.IColor;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Norm3b;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin extends DefaultedVertexConsumer implements BufferVertexConsumer, IVertexBufferView {

    @Shadow
    @Final
    private static Logger LOGGER;
    private final FList newSortingPoints = new FArrayList();
    @Shadow
    private ByteBuffer buffer;
    @Shadow
    private boolean building;
    @Shadow
    @Nullable
    private VertexFormatElement currentElement;
    @Shadow
    @Final
    private List<BufferBuilder.DrawState> drawStates;
    @Shadow
    private int elementIndex;
    @Shadow
    private boolean fastFormat;
    @Shadow
    private VertexFormat format;
    @Shadow
    private boolean indexOnly;
    @Shadow
    private VertexFormat.Mode mode;
    @Shadow
    private int nextElementByte;
    @Shadow
    private float sortX;
    @Shadow
    private float sortY;
    @Shadow
    private float sortZ;
    @Shadow
    @Nullable
    private Vector3f[] sortingPoints;
    @Shadow
    private int totalRenderedBytes;
    @Shadow
    private int vertices;

    @Shadow
    private static int roundUp(int pX) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Use new sorting points
     */
    @Overwrite
    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
        int i = this.mode.indexCount(this.vertices);
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
        boolean flag;
        if (!this.newSortingPoints.isEmpty()) {
            int j = Mth.roundToward(i * indexType.bytes, 4);
            this.ensureCapacity(j);
            this.putSortedQuadIndices(indexType);
            flag = false;
            this.nextElementByte += j;
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize() + j;
        }
        else {
            flag = true;
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
        }
        this.building = false;
        this.drawStates.add(new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, indexType, this.indexOnly, flag));
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
        this.newSortingPoints.clear();
        this.sortX = Float.NaN;
        this.sortY = Float.NaN;
        this.sortZ = Float.NaN;
        this.indexOnly = false;
    }

    @Override
    public boolean ensureBufferCapacity(int bytes) {
        // Ensure that there is always space for 1 more vertex; see BufferBuilder.next()
        bytes += this.format.getVertexSize();
        if (this.elementIndex + bytes <= this.buffer.capacity()) {
            return false;
        }
        int newSize = this.buffer.capacity() + roundUp(bytes);
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", this.buffer.capacity(), newSize);
        this.buffer.position(0);
        ByteBuffer byteBuffer = GlUtil.allocateMemory(newSize);
        byteBuffer.put(this.buffer);
        byteBuffer.rewind();
        this.buffer = byteBuffer;
        return true;
    }

    @Shadow
    protected abstract void ensureCapacity(int pIncreaseAmount);

    @Override
    public void flush(int vertexCount, IBufferVertexFormat format) {
        if (IBufferVertexFormat.from(this.format) != format) {
            throw new IllegalStateException("Mis-matched vertex format (expected: [" + format + "], currently using: [" + this.format + "])");
        }
        this.vertices += vertexCount;
        this.elementIndex += vertexCount * format.getStride();
    }

    @Override
    public ByteBuffer getDirectBuffer() {
        return this.buffer;
    }

    /**
     * @author TheGreatWolf
     * @reason Use newSortingPoints
     */
    @Overwrite
    public BufferBuilder.SortState getSortState() {
        BufferBuilder.SortState sortState = new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY,
                                                                        this.sortZ);
        ((ISortStatePatch) sortState).putNewSortingPoints(this.newSortingPoints);
        return sortState;
    }

    @Override
    public int getWriterPosition() {
        return this.elementIndex;
    }

    @Shadow
    protected abstract IntConsumer intConsumer(VertexFormat.IndexType pIndexType);

    private void makeNewQuadSortingPoints() {
        FloatBuffer floatbuffer = this.buffer.asFloatBuffer();
        int i = this.totalRenderedBytes / 4;
        int j = this.format.getIntegerSize();
        int k = j * this.mode.primitiveStride;
        int l = this.vertices / this.mode.primitiveStride;
        for (int i1 = 0; i1 < l; ++i1) {
            float f = floatbuffer.get(i + i1 * k);
            float f1 = floatbuffer.get(i + i1 * k + 1);
            float f2 = floatbuffer.get(i + i1 * k + 2);
            float f3 = floatbuffer.get(i + i1 * k + j * 2);
            float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
            float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
            float f6 = (f + f3) / 2.0F;
            float f7 = (f1 + f4) / 2.0F;
            float f8 = (f2 + f5) / 2.0F;
            this.newSortingPoints.add(f6);
            this.newSortingPoints.add(f7);
            this.newSortingPoints.add(f8);
        }
    }

    @Override
    public void putBulkData(PoseStack.Pose matrices,
                            BakedQuad quad,
                            float[] brightnessTable,
                            float r,
                            float g,
                            float b,
                            int[] light,
                            int overlay,
                            boolean colorize) {
        if (!this.fastFormat) {
            super.putBulkData(matrices, quad, brightnessTable, r, g, b, light, overlay, colorize);
            return;
        }
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        }
        IBakedQuadPatch quadView = (IBakedQuadPatch) quad;
        Matrix4f pose = matrices.pose();
        IMatrix4fPatch poseExt = MathHelper.getExtendedMatrix(pose);
        Matrix3f normal = matrices.normal();
        int norm = MathHelper.computeNormal(normal, quad.getDirection());
        float nx = Norm3b.unpackX(norm);
        float ny = Norm3b.unpackY(norm);
        float nz = Norm3b.unpackZ(norm);
        for (int i = 0; i < 4; i++) {
            float x = quadView.getX(i);
            float y = quadView.getY(i);
            float z = quadView.getZ(i);
            float fR;
            float fG;
            float fB;
            float brightness = brightnessTable[i];
            if (colorize) {
                int color = quadView.getColor(i);
                float oR = IColor.normalize(ColorABGR.unpackRed(color));
                float oG = IColor.normalize(ColorABGR.unpackGreen(color));
                float oB = IColor.normalize(ColorABGR.unpackBlue(color));
                fR = oR * brightness * r;
                fG = oG * brightness * g;
                fB = oB * brightness * b;
            }
            else {
                fR = brightness * r;
                fG = brightness * g;
                fB = brightness * b;
            }
            float u = quadView.getTexU(i);
            float v = quadView.getTexV(i);
            float x2 = poseExt.transformVecX(x, y, z);
            float y2 = poseExt.transformVecY(x, y, z);
            float z2 = poseExt.transformVecZ(x, y, z);
            this.vertex(x2, y2, z2).color(fR, fG, fB, 1.0f).uv(u, v).uv2(light[i]).overlayCoords(overlay).normal(nx, ny, nz).endVertex();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Try to avoid allocations, but the sorting method tends to allocate a lot of {@code int[]}. Can we do better?
     */
    @Overwrite
    private void putSortedQuadIndices(VertexFormat.IndexType indexType) {
        FList floatList = RenderHelper.FLOAT_LIST.get();
        floatList.clear();
        floatList.size(this.newSortingPoints.size() / 3);
        IList intList = RenderHelper.INT_LIST.get();
        intList.clear();
        intList.size(this.newSortingPoints.size() / 3);
        for (int i = 0; i < this.newSortingPoints.size() / 3; intList.set(i, i++)) {
            float dx = this.newSortingPoints.getFloat(3 * i) - this.sortX;
            float dy = this.newSortingPoints.getFloat(3 * i + 1) - this.sortY;
            float dz = this.newSortingPoints.getFloat(3 * i + 2) - this.sortZ;
            floatList.set(i, dx * dx + dy * dy + dz * dz);
        }
        intList.sort((a, b) -> Floats.compare(floatList.getFloat(b), floatList.getFloat(a)));
        IntConsumer consumer = this.intConsumer(indexType);
        this.buffer.position(this.nextElementByte);
        for (int i = 0; i < this.newSortingPoints.size() / 3; i++) {
            int u = intList.getInt(i);
            consumer.accept(u * this.mode.primitiveStride);
            consumer.accept(u * this.mode.primitiveStride + 1);
            consumer.accept(u * this.mode.primitiveStride + 2);
            consumer.accept(u * this.mode.primitiveStride + 2);
            consumer.accept(u * this.mode.primitiveStride + 3);
            consumer.accept(u * this.mode.primitiveStride);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Use newSortingPoints.
     */
    @Overwrite
    public void restoreSortState(BufferBuilder.SortState sortState) {
        this.buffer.clear();
        this.mode = sortState.mode;
        this.vertices = sortState.vertices;
        this.nextElementByte = this.totalRenderedBytes;
        this.newSortingPoints.clear();
        this.newSortingPoints.addAll(((ISortStatePatch) sortState).getNewSortingPoints());
        this.sortX = sortState.sortX;
        this.sortY = sortState.sortY;
        this.sortZ = sortState.sortZ;
        this.indexOnly = true;
    }

    /**
     * @author TheGreatWolf
     * @reason Use newSortingPoints
     */
    @Overwrite
    public void setQuadSortOrigin(float sortX, float sortY, float sortZ) {
        if (this.mode == VertexFormat.Mode.QUADS) {
            if (this.sortX != sortX || this.sortY != sortY || this.sortZ != sortZ) {
                this.sortX = sortX;
                this.sortY = sortY;
                this.sortZ = sortZ;
                if (this.newSortingPoints.isEmpty()) {
                    this.makeNewQuadSortingPoints();
                }
            }
        }
    }

    @Override
    public IBufferVertexFormat vertexFormat() {
        return IBufferVertexFormat.from(this.format);
    }
}
