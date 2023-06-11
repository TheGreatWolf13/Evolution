package tgw.evolution.world.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.ILocked;
import tgw.evolution.util.collection.AbstractReusableIterator;
import tgw.evolution.util.math.AABBMutable;

import java.util.Iterator;

public abstract class AbstractCollisionCalculator<T> extends AbstractReusableIterator<T> implements ILocked, Iterable<T> {
    protected final AABBMutable box = new AABBMutable();
    protected final Cursor3DMutable cursor = new Cursor3DMutable();
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    protected MutableCollisionContext context = new MutableCollisionContext();
    protected @Nullable VoxelShape entityShape;
    protected @Nullable CollisionGetter level;
    protected boolean onlySuffocatingBlocks;
    private @Nullable BlockGetter cachedChunk;
    private int cachedChunkX;
    private int cachedChunkZ;
    private boolean locked;

    protected AbstractCollisionCalculator() {
    }

    @Override
    public void close() {
        this.locked = false;
        this.context.reset();
        this.entityShape = null;
        this.level = null;
        this.cachedChunk = null;
        this.reset();
    }

    @Nullable
    protected BlockGetter getChunk(int x, int z) {
        byte flag = this.cursor.getRecalculationFlag();
        if (flag == 0) {
            assert this.cachedChunkX == SectionPos.blockToSectionCoord(x);
            assert this.cachedChunkZ == SectionPos.blockToSectionCoord(z);
            return this.cachedChunk;
        }
        if ((flag & 1) != 0) {
            this.cachedChunkX = SectionPos.blockToSectionCoord(x);
        }
        if ((flag & 2) != 0) {
            this.cachedChunkZ = SectionPos.blockToSectionCoord(z);
        }
        assert this.level != null;
        assert this.cachedChunkX == SectionPos.blockToSectionCoord(x);
        assert this.cachedChunkZ == SectionPos.blockToSectionCoord(z);
        this.cachedChunk = this.level.getChunkForCollisions(this.cachedChunkX, this.cachedChunkZ);
        return this.cachedChunk;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public void lock() {
        this.locked = true;
    }
}
