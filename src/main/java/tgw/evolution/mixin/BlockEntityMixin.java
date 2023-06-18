package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.client.renderer.chunk.EvModelDataManager;
import tgw.evolution.util.math.AABBMutable;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements IForgeBlockEntity {

    @Unique private static final AABBMutable TE_BOX = new AABBMutable();

    @Shadow public abstract BlockPos getBlockPos();

    @Shadow public abstract BlockState getBlockState();

    @Shadow @Nullable public abstract Level getLevel();

    //Force override
    @Override
    public AABB getRenderBoundingBox() {
        BlockState state = this.getBlockState();
        Block block = state.getBlock();
        BlockPos pos = this.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (block == Blocks.ENCHANTING_TABLE) {
            return TE_BOX.setUnchecked(x, y, z, x + 1, y + 1, z + 1);
        }
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST) {
            return TE_BOX.setUnchecked(x - 1, y, z - 1, x + 2, y + 2, z + 2);
        }
        if (block == Blocks.STRUCTURE_BLOCK) {
            return INFINITE_EXTENT_AABB;
        }
        if (block != Blocks.BEACON) {
            AABB cbb = null;
            try {
                Level level = this.getLevel();
                if (level != null) {
                    VoxelShape collisionShape = state.getCollisionShape(level, pos);
                    if (!collisionShape.isEmpty()) {
                        AABBMutable box = TE_BOX;
                        box.setUnchecked(collisionShape.min(Direction.Axis.X), collisionShape.min(Direction.Axis.Y),
                                         collisionShape.min(Direction.Axis.Z), collisionShape.max(Direction.Axis.X),
                                         collisionShape.max(Direction.Axis.Y), collisionShape.max(Direction.Axis.Z));
                        cbb = box.moveMutable(x, y, z);
                    }
                }
            }
            catch (Exception e) {
                // We have to capture any exceptions that may occur here because BUKKIT servers like to send
                // the tile entity data BEFORE the chunk data, you know, the OPPOSITE of what vanilla does!
                // So we can not GUARANTEE that the world state is the real state for the block...
                // So, once again in the long line of US having to accommodate BUKKIT breaking things,
                // here it is, assume that the TE is only 1 cubic block. Problem with this is that it may
                // cause the TileEntity renderer to error further down the line! But alas, nothing we can do.
                cbb = TE_BOX.setUnchecked(x - 1, y, z - 1, x + 1, y + 1, z + 1);
            }
            if (cbb != null) {
                return cbb;
            }
        }
        return INFINITE_EXTENT_AABB;
    }

    //Force override
    @Override
    public void requestModelDataUpdate() {
        Level level = this.getLevel();
        if (level != null && level.isClientSide) {
            EvModelDataManager.requestModelDataRefresh((BlockEntity) (Object) this);
        }
    }
}
