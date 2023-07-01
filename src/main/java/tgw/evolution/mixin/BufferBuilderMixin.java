package tgw.evolution.mixin;

import com.google.common.primitives.Floats;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.ICrashReset;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.patches.ISortStatePatch;
import tgw.evolution.util.collection.FArrayList;
import tgw.evolution.util.collection.FList;
import tgw.evolution.util.collection.IList;
import tgw.evolution.util.math.MathHelper;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin extends DefaultedVertexConsumer implements BufferVertexConsumer, ICrashReset {

    @Shadow @Final private static Logger LOGGER;
    @Unique private final FList newSortingPoints = new FArrayList();
    @Shadow private ByteBuffer buffer;
    @Shadow private boolean building;
    @Shadow private @Nullable VertexFormatElement currentElement;
    @Shadow @Final private List<BufferBuilder.DrawState> drawStates;
    @Shadow private int elementIndex;
    @Shadow private boolean fastFormat;
    @Shadow private VertexFormat format;
    @Shadow private boolean indexOnly;
    @Shadow private VertexFormat.Mode mode;
    @Shadow private int nextElementByte;
    @Shadow private float sortX;
    @Shadow private float sortY;
    @Shadow private float sortZ;
    @Shadow private @Nullable Vector3f[] sortingPoints;
    @Shadow private int totalRenderedBytes;
    @Shadow private int vertices;

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

    @Shadow
    protected abstract void ensureCapacity(int pIncreaseAmount);

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
    public void putBulkData(PoseStack.Pose entry,
                            BakedQuad quad,
                            float[] brightnessTable,
                            float r,
                            float g,
                            float b,
                            int[] light,
                            int overlay,
                            boolean colorize) {
        if (!this.fastFormat) {
            super.putBulkData(entry, quad, brightnessTable, r, g, b, light, overlay, colorize);
            return;
        }
        if (this.defaultColorSet) {
            throw new IllegalStateException();
        }
        IMatrix4fPatch poseMat = MathHelper.getExtendedMatrix(entry.pose());
        IMatrix3fPatch normalMat = MathHelper.getExtendedMatrix(entry.normal());
        Vec3i normal = quad.getDirection().getNormal();
        float nx = normalMat.transformVecX(normal.getX(), normal.getY(), normal.getZ());
        float ny = normalMat.transformVecY(normal.getX(), normal.getY(), normal.getZ());
        float nz = normalMat.transformVecZ(normal.getX(), normal.getY(), normal.getZ());
        int[] vertices = quad.getVertices();
        for (int vertex = 0; vertex < 4; ++vertex) {
            int offset = vertex * 8;
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            float cr;
            float cg;
            float cb;
            float brightness = brightnessTable[vertex];
            if (colorize) {
                int color = vertices[offset + 3];
                cr = (color & 0xff) * (1 / 255.0f) * brightness * r;
                cg = (color >> 8 & 0xff) * (1 / 255.0f) * brightness * g;
                cb = (color >> 16 & 0xff) * (1 / 255.0f) * brightness * b;
            }
            else {
                cr = brightness * r;
                cg = brightness * g;
                cb = brightness * b;
            }
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            this.vertex(x, y, z).color(cr, cg, cb, 1.0f).uv(u, v).uv2(light[vertex]).overlayCoords(overlay).normal(nx, ny, nz).endVertex();
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

    @Override
    public void resetAfterCrash() {
        this.buffer.clear();
        this.building = false;
        this.vertices = 0;
        this.currentElement = null;
        this.elementIndex = 0;
        this.newSortingPoints.clear();
        this.sortX = Float.NaN;
        this.sortY = Float.NaN;
        this.sortZ = Float.NaN;
        this.indexOnly = false;
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
}
