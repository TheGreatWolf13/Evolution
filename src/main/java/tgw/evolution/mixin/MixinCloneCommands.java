package tgw.evolution.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.CloneCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.CloneBlockInfo;
import tgw.evolution.util.MutableBlockInWorld;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.queues.LArrayQueue;
import tgw.evolution.util.collection.queues.LQueue;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.Predicate;

@Mixin(CloneCommands.class)
public abstract class MixinCloneCommands {

    @Shadow @Final private static Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_FAILED;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_OVERLAP;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static int clone(CommandSourceStack sourceStack, BlockPos corner1, BlockPos corner2, BlockPos destination, Predicate<BlockInWorld> predicate, CloneCommands.Mode mode) throws CommandSyntaxException {
        BoundingBox box = BoundingBox.fromCorners(corner1, corner2);
        int destX = destination.getX();
        int destY = destination.getY();
        int destZ = destination.getZ();
        int destToX = destX + box.maxX() - box.minX();
        int destToY = destY + box.maxY() - box.minY();
        int destToZ = destZ + box.maxZ() - box.minZ();
        int destBoxMinX = Math.min(destX, destToX);
        int destBoxMinY = Math.min(destY, destToY);
        int destBoxMinZ = Math.min(destZ, destToZ);
        int destBoxMaxX = Math.max(destX, destToX);
        int destBoxMaxY = Math.max(destY, destToY);
        int destBoxMaxZ = Math.max(destZ, destToZ);
        if (!mode.canOverlap() && destBoxMaxX >= box.minX() && destBoxMinX <= box.maxX() && destBoxMaxZ >= box.minZ() && destBoxMinZ <= box.maxZ() && destBoxMaxY >= box.minY() && destBoxMinY <= box.maxY()) {
            throw ERROR_OVERLAP.create();
        }
        int size = box.getXSpan() * box.getYSpan() * box.getZSpan();
        if (size > 32_768) {
            throw ERROR_AREA_TOO_LARGE.create(32_768, size);
        }
        ServerLevel level = sourceStack.getLevel();
        if (!level.hasChunksAt(corner1, corner2) || !level.hasChunksAt(destX, destY, destZ, destToX, destToY, destToZ)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
        OList<CloneBlockInfo> listNormal = new OArrayList<>();
        OList<CloneBlockInfo> listWithTE = new OArrayList<>();
        OList<CloneBlockInfo> listNotSolid = new OArrayList<>();
        LQueue deque;
        if (mode == CloneCommands.Mode.MOVE) {
            deque = new LArrayQueue();
        }
        else {
            deque = null;
        }
        int xOffset = destBoxMinX - box.minX();
        int yOffset = destBoxMinY - box.minY();
        int zOffset = destBoxMinZ - box.minZ();
        MutableBlockInWorld blockInWorld = new MutableBlockInWorld();
        for (int z = box.minZ(); z <= box.maxZ(); ++z) {
            for (int y = box.minY(); y <= box.maxY(); ++y) {
                for (int x = box.minX(); x <= box.maxX(); ++x) {
                    blockInWorld.set(level, x, y, z, false);
                    BlockState state = blockInWorld.getState();
                    if (predicate.test(blockInWorld)) {
                        int toX = x + xOffset;
                        int toY = y + yOffset;
                        int toZ = z + zOffset;
                        BlockEntity blockEntity = level.getBlockEntity_(x, y, z);
                        if (blockEntity != null) {
                            //noinspection ObjectAllocationInLoop
                            listWithTE.add(new CloneBlockInfo(toX, toY, toZ, state, blockEntity.saveWithoutMetadata()));
                            if (mode == CloneCommands.Mode.MOVE) {
                                deque.enqueueFirst(BlockPos.asLong(x, y, z));
                            }
                        }
                        else if (!state.isSolidRender_(level, x, y, z) && !state.isCollisionShapeFullBlock_(level, x, y, z)) {
                            //noinspection ObjectAllocationInLoop
                            listNotSolid.add(new CloneBlockInfo(toX, toY, toZ, state, null));
                            if (mode == CloneCommands.Mode.MOVE) {
                                deque.enqueueFirst(BlockPos.asLong(x, y, z));
                            }
                        }
                        else {
                            //noinspection ObjectAllocationInLoop
                            listNormal.add(new CloneBlockInfo(toX, toY, toZ, state, null));
                            if (mode == CloneCommands.Mode.MOVE) {
                                deque.enqueue(BlockPos.asLong(x, y, z));
                            }
                        }
                    }
                }
            }
        }
        if (mode == CloneCommands.Mode.MOVE) {
            for (long it = deque.beginIteration(); deque.hasNextIteration(it); it = deque.nextEntry(it)) {
                long pos = deque.getIteration(it);
                int x = BlockPos.getX(pos);
                int y = BlockPos.getY(pos);
                int z = BlockPos.getZ(pos);
                BlockEntity blockEntity = level.getBlockEntity_(x, y, z);
                Clearable.tryClear(blockEntity);
                level.setBlock_(x, y, z, Blocks.BARRIER.defaultBlockState(), BlockFlags.BLOCK_UPDATE);
            }
            while (!deque.isEmpty()) {
                long pos = deque.dequeue();
                level.setBlock_(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), Blocks.AIR.defaultBlockState(), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
            }
        }
        listNormal.addAll(listWithTE);
        listNormal.addAll(listNotSolid);
        for (int i = listNormal.size() - 1; i >= 0; --i) {
            CloneBlockInfo cloneBlockInfo = listNormal.get(i);
            Clearable.tryClear(level.getBlockEntity_(cloneBlockInfo.x, cloneBlockInfo.y, cloneBlockInfo.z));
            level.setBlock_(cloneBlockInfo.x, cloneBlockInfo.y, cloneBlockInfo.z, Blocks.BARRIER.defaultBlockState(), BlockFlags.BLOCK_UPDATE);
        }
        int count = 0;
        for (int i = 0, len = listNormal.size(); i < len; ++i) {
            CloneBlockInfo cloneBlockInfo = listNormal.get(i);
            if (level.setBlock_(cloneBlockInfo.x, cloneBlockInfo.y, cloneBlockInfo.z, cloneBlockInfo.state, BlockFlags.BLOCK_UPDATE)) {
                ++count;
            }
        }
        for (int i = 0, len = listWithTE.size(); i < len; ++i) {
            CloneBlockInfo cloneBlockInfo = listWithTE.get(i);
            BlockEntity blockEntity4 = level.getBlockEntity_(cloneBlockInfo.x, cloneBlockInfo.y, cloneBlockInfo.z);
            if (cloneBlockInfo.tag != null && blockEntity4 != null) {
                blockEntity4.load(cloneBlockInfo.tag);
                blockEntity4.setChanged();
            }
            level.setBlock_(cloneBlockInfo.x, cloneBlockInfo.y, cloneBlockInfo.z, cloneBlockInfo.state, BlockFlags.BLOCK_UPDATE);
        }
        for (int i = listNormal.size() - 1; i >= 0; --i) {
            CloneBlockInfo cloneBlockInfo = listNormal.get(i);
            level.blockUpdated_(cloneBlockInfo.x, cloneBlockInfo.y, cloneBlockInfo.z, cloneBlockInfo.state.getBlock());
        }
        level.getBlockTicks().copyArea(box, new BlockPos(xOffset, yOffset, zOffset));
        if (count == 0) {
            throw ERROR_FAILED.create();
        }
        sourceStack.sendSuccess(new TranslatableComponent("commands.clone.success", count), true);
        return count;
    }
}
