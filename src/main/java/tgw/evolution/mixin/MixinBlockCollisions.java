package tgw.evolution.mixin;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockCollisions.class)
public abstract class MixinBlockCollisions extends AbstractIterator<VoxelShape> {

    @Shadow @Final private AABB box;
    @Shadow @Final private CollisionGetter collisionGetter;
    @Shadow @Final private CollisionContext context;
    @Shadow @Final private Cursor3D cursor;
    @Shadow @Final private VoxelShape entityShape;
    @Shadow @Final private boolean onlySuffocatingBlocks;
    @Shadow @Final private BlockPos.MutableBlockPos pos;

    @Override
    @Overwrite
    public VoxelShape computeNext() {
        while (true) {
            if (this.cursor.advance()) {
                int x = this.cursor.nextX();
                int y = this.cursor.nextY();
                int z = this.cursor.nextZ();
                int l = this.cursor.getNextType();
                if (l == 3) {
                    continue;
                }
                BlockGetter blockGetter = this.getChunk(x, z);
                if (blockGetter == null) {
                    continue;
                }
                this.pos.set(x, y, z);
                BlockState blockState = blockGetter.getBlockState_(x, y, z);
                if (this.onlySuffocatingBlocks && !blockState.isSuffocating(blockGetter, this.pos) ||
                    l == 1 && !blockState.hasLargeCollisionShape() ||
                    l == 2 && !blockState.is(Blocks.MOVING_PISTON)) {
                    continue;
                }
                VoxelShape voxelShape = blockState.getCollisionShape(this.collisionGetter, this.pos, this.context);
                if (voxelShape == Shapes.block()) {
                    if (!this.box.intersects(x, y, z, x + 1, y + 1, z + 1)) {
                        continue;
                    }
                    return voxelShape.move(x, y, z);
                }
                VoxelShape voxelShape2 = voxelShape.move(x, y, z);
                if (!Shapes.joinIsNotEmpty(voxelShape2, this.entityShape, BooleanOp.AND)) {
                    continue;
                }
                return voxelShape2;
            }
            return this.endOfData();
        }
    }

    @Shadow
    protected abstract @Nullable BlockGetter getChunk(int i, int j);
}
