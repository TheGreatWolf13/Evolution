package tgw.evolution.mixin;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.math.MathHelper;

@Mixin(BlockCollisions.class)
public abstract class Mixin_CF_BlockCollisions extends AbstractIterator<VoxelShape> {

    private final @Nullable Entity entity;
    @Mutable @Shadow @Final @RestoreFinal private AABB box;
    @Mutable @Shadow @Final @RestoreFinal private CollisionGetter collisionGetter;
    @Shadow @Final @DeleteField private CollisionContext context;
    @Mutable @Shadow @Final @RestoreFinal private Cursor3D cursor;
    @Mutable @Shadow @Final @RestoreFinal private VoxelShape entityShape;
    @Mutable @Shadow @Final @RestoreFinal private boolean onlySuffocatingBlocks;
    @Shadow @Final @DeleteField private BlockPos.MutableBlockPos pos;

    @ModifyConstructor
    public Mixin_CF_BlockCollisions(CollisionGetter collisionGetter, @Nullable Entity entity, AABB aABB, boolean bl) {
        this.entityShape = Shapes.create(aABB);
        this.collisionGetter = collisionGetter;
        this.box = aABB;
        this.onlySuffocatingBlocks = bl;
        int i = Mth.floor(aABB.minX - 1.0E-7D) - 1;
        int j = Mth.floor(aABB.maxX + 1.0E-7D) + 1;
        int k = Mth.floor(aABB.minY - 1.0E-7D) - 1;
        int l = Mth.floor(aABB.maxY + 1.0E-7D) + 1;
        int m = Mth.floor(aABB.minZ - 1.0E-7D) - 1;
        int n = Mth.floor(aABB.maxZ + 1.0E-7D) + 1;
        this.cursor = new Cursor3D(i, k, m, j, l, n);
        this.entity = entity;
    }

    @Override
    @Overwrite
    public VoxelShape computeNext() {
        while (true) {
            if (this.cursor.advance()) {
                int x = this.cursor.nextX();
                int y = this.cursor.nextY();
                int z = this.cursor.nextZ();
                int type = this.cursor.getNextType();
                if (type == Cursor3D.TYPE_CORNER) {
                    continue;
                }
                BlockGetter blockGetter = this.getChunk(x, z);
                if (blockGetter == null) {
                    continue;
                }
                BlockState blockState = blockGetter.getBlockState_(x, y, z);
                if (this.onlySuffocatingBlocks && !blockState.isSuffocating_(this.collisionGetter, x, y, z) ||
                    type == Cursor3D.TYPE_FACE && !blockState.hasLargeCollisionShape() ||
                    type == Cursor3D.TYPE_EDGE && !blockState.is(Blocks.MOVING_PISTON)) {
                    continue;
                }
                VoxelShape shape = blockState.getCollisionShape_(this.collisionGetter, x, y, z, this.entity);
                AABB box = this.box;
                if (!MathHelper.doesShapeIntersect(shape, box.minX - x, box.minY - y, box.minZ - z, box.maxX - x, box.maxY - y, box.maxZ - z)) {
                    continue;
                }
                return shape.move(x, y, z);
            }
            return this.endOfData();
        }
    }

    @Shadow
    protected abstract @Nullable BlockGetter getChunk(int i, int j);
}
