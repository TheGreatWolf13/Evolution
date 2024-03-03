package tgw.evolution.world.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.math.AABBMutable;
import tgw.evolution.util.math.MathHelper;

public final class CollisionShapeCalculator extends AbstractCollisionCalculator<VoxelShape> {

    private static final ThreadLocal<CollisionShapeCalculator> CACHE = ThreadLocal.withInitial(CollisionShapeCalculator::new);

    private CollisionShapeCalculator() {
    }

    public static CollisionShapeCalculator getInstance(CollisionGetter level, @Nullable Entity entity, AABB bb, boolean onlySuffocatingBlocks) {
        return getInstance(level, entity, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, onlySuffocatingBlocks);
    }

    public static CollisionShapeCalculator getInstance(CollisionGetter level,
                                                       @Nullable Entity entity,
                                                       double minX,
                                                       double minY,
                                                       double minZ,
                                                       double maxX,
                                                       double maxY,
                                                       double maxZ,
                                                       boolean onlySuffocatingBlocks) {
        CollisionShapeCalculator calculator = CACHE.get();
        assert !calculator.isLocked() : "The local instance of CollisionShapeCalculator is locked, you probably forgot to unlock it! Use it with try-with-resources to unlock automatically.";
        calculator.entity = entity;
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
    protected @Nullable VoxelShape computeNext() {
        while (this.cursor.advance()) {
            int x = this.cursor.nextX();
            int y = this.cursor.nextY();
            int z = this.cursor.nextZ();
            int type = this.cursor.getNextType();
            if (type == Cursor3DMutable.CORNER) {
                continue;
            }
            BlockGetter chunk = this.getChunk(x, z);
            if (chunk == null) {
                continue;
            }
            BlockState state = chunk.getBlockState_(x, y, z);
            assert this.level != null;
            if (this.onlySuffocatingBlocks && !state.isSuffocating_(this.level, x, y, z) ||
                type == Cursor3DMutable.FACE && !state.hasLargeCollisionShape() ||
                type == Cursor3DMutable.EDGE && !state.is(Blocks.MOVING_PISTON)) {
                continue;
            }
            VoxelShape shape = state.getCollisionShape_(this.level, x, y, z, this.entity);
            AABBMutable box = this.box;
            if (!MathHelper.doesShapeIntersect(shape, box.minX - x, box.minY - y, box.minZ - z, box.maxX - x, box.maxY - y, box.maxZ - z)) {
                continue;
            }
            return shape;
        }
        return this.endOfData();
    }
}
