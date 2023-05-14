package tgw.evolution.world.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.ILocked;
import tgw.evolution.util.collection.AbstractReusableIterator;
import tgw.evolution.util.math.AABBMutable;

public final class CollisionCalculator extends AbstractReusableIterator<BlockPos> implements ILocked {

    private static final ThreadLocal<CollisionCalculator> CACHE = ThreadLocal.withInitial(CollisionCalculator::new);
    private final AABBMutable box = new AABBMutable();
    private final Cursor3DMutable cursor = new Cursor3DMutable();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    @Nullable
    private BlockGetter cachedBlockGetter;
    private long cachedBlockGetterPos;
    private CollisionContext context = CollisionContext.empty();
    @Nullable
    private VoxelShape entityShape;
    @Nullable
    private CollisionGetter level;
    private boolean locked;
    private boolean onlySuffocatingBlocks;

    private CollisionCalculator() {
    }

    public static CollisionCalculator getInstance(CollisionGetter level,
                                                  @Nullable Entity entity,
                                                  double minX,
                                                  double minY,
                                                  double minZ,
                                                  double maxX,
                                                  double maxY,
                                                  double maxZ,
                                                  boolean onlySuffocatingBlocks) {
        CollisionCalculator calculator = CACHE.get();
        assert !calculator.isLocked() :
                "The local instance of CollisionCalculator is locked, you probably forgot to unlock it! Use it with try-with-resources to " +
                "unlock automatically.";
        calculator.context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        calculator.box.set(minX, minY, minZ, maxX, maxY, maxZ);
        calculator.entityShape = Shapes.create(calculator.box);
        calculator.level = level;
        calculator.onlySuffocatingBlocks = onlySuffocatingBlocks;
        int x0 = Mth.floor(minX - 1.0E-7) - 1;
        int x1 = Mth.floor(maxX + 1.0E-7) + 1;
        int y0 = Mth.floor(minY - 1.0E-7) - 1;
        int y1 = Mth.floor(maxY + 1.0E-7) + 1;
        int z0 = Mth.floor(minZ - 1.0E-7) - 1;
        int z1 = Mth.floor(maxZ + 1.0E-7) + 1;
        calculator.cursor.set(x0, y0, z0, x1, y1, z1);
        calculator.lock();
        return calculator;
    }

    @Override
    public void close() {
        this.locked = false;
        this.context = CollisionContext.empty();
        this.entityShape = null;
        this.level = null;
        this.cachedBlockGetter = null;
        this.reset();
    }

    @Nullable
    @Override
    protected BlockPos computeNext() {
        while (this.cursor.advance()) {
            int x = this.cursor.nextX();
            int y = this.cursor.nextY();
            int z = this.cursor.nextZ();
            int type = this.cursor.getNextType();
            if (type == 3) {
                continue;
            }
            BlockGetter chunk = this.getChunk(x, z);
            if (chunk == null) {
                continue;
            }
            this.pos.set(x, y, z);
            BlockState state = chunk.getBlockState(this.pos);
            if (this.onlySuffocatingBlocks && !state.isSuffocating(chunk, this.pos) ||
                type == 1 && !state.hasLargeCollisionShape() ||
                type == 2 && !state.is(Blocks.MOVING_PISTON)) {
                continue;
            }
            assert this.level != null;
            VoxelShape shape = state.getCollisionShape(this.level, this.pos, this.context);
            if (shape == Shapes.block()) {
                if (!this.box.intersects(x, y, z, x + 1, y + 1, z + 1)) {
                    continue;
                }
                return this.pos;
            }
            VoxelShape movedShape = shape.move(x, y, z);
            assert this.entityShape != null;
            if (movedShape.isEmpty() || !Shapes.joinIsNotEmpty(movedShape, this.entityShape, BooleanOp.AND)) {
                continue;
            }
            return this.pos;
        }
        return this.endOfData();
    }

    @Nullable
    private BlockGetter getChunk(int x, int z) {
        int secX = SectionPos.blockToSectionCoord(x);
        int secZ = SectionPos.blockToSectionCoord(z);
        long pos = ChunkPos.asLong(secX, secZ);
        if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == pos) {
            return this.cachedBlockGetter;
        }
        assert this.level != null;
        BlockGetter chunk = this.level.getChunkForCollisions(secX, secZ);
        this.cachedBlockGetter = chunk;
        this.cachedBlockGetterPos = pos;
        return chunk;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void lock() {
        this.locked = true;
    }
}
