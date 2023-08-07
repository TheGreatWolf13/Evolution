package tgw.evolution.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.commands.vanilla.FillMode;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.BlockInWorldPredicate;
import tgw.evolution.util.collection.lists.LArrayList;
import tgw.evolution.util.collection.lists.LList;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.Predicate;

@Mixin(FillCommand.class)
public abstract class Mixin_FillCommand {

    @Shadow @Final private static Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE;
    @Shadow @Final private static SimpleCommandExceptionType ERROR_FAILED;

    @Overwrite
    @DeleteMethod
    private static int fillBlocks(CommandSourceStack commandSourceStack, BoundingBox boundingBox, BlockInput blockInput, FillCommand.Mode mode, @Nullable Predicate<BlockInWorld> predicate) {
        throw new AbstractMethodError();
    }

    @Unique
    private static int fillBlocks(CommandSourceStack source, BoundingBox bb, BlockInput blockInput, FillCommand.Mode vanillaMode, @Nullable BlockInWorldPredicate predicate) throws CommandSyntaxException {
        int size = bb.getXSpan() * bb.getYSpan() * bb.getZSpan();
        if (size > 32_768) {
            throw ERROR_AREA_TOO_LARGE.create(32_768, size);
        }
        LList list = new LArrayList(size);
        ServerLevel level = source.getLevel();
        int count = 0;
        FillMode mode = FillMode.fromVanilla(vanillaMode);
        int x0 = bb.minX();
        int x1 = bb.maxX();
        int y0 = bb.minY();
        int y1 = bb.maxY();
        int z0 = bb.minZ();
        int z1 = bb.maxZ();
        for (int x = x0; x <= x1; ++x) {
            for (int y = y0; y <= y1; ++y) {
                for (int z = z0; z <= z1; ++z) {
                    if (predicate != null && !predicate.test(level, x, y, z, true)) {
                        continue;
                    }
                    BlockInput filteredInput = mode.filter(bb, x, y, z, blockInput, level);
                    if (filteredInput != null) {
                        BlockEntity blockEntity = level.getBlockEntity_(x, y, z);
                        Clearable.tryClear(blockEntity);
                        if (filteredInput.place_(level, x, y, z, BlockFlags.BLOCK_UPDATE)) {
                            list.add(BlockPos.asLong(x, y, z));
                            ++count;
                        }
                    }
                }
            }
        }
        for (int i = 0, len = list.size(); i < len; ++i) {
            long pos = list.getLong(i);
            int x = BlockPos.getX(pos);
            int y = BlockPos.getY(pos);
            int z = BlockPos.getZ(pos);
            level.blockUpdated_(x, y, z, level.getBlockState_(x, y, z).getBlock());
        }
        if (count == 0) {
            throw ERROR_FAILED.create();
        }
        source.sendSuccess(new TranslatableComponent("commands.fill.success", count), true);
        return count;
    }

    @Overwrite
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fill")
                                    .requires(cs -> cs.hasPermission(2))
                                    .then(Commands.argument("from", BlockPosArgument.blockPos())
                                                  .then(Commands.argument("to", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("block", BlockStateArgument.block())
                                                                              .executes(c -> fillBlocks(c.getSource(),
                                                                                                        BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                        BlockStateArgument.getBlock(c, "block"),
                                                                                                        FillCommand.Mode.REPLACE,
                                                                                                        (BlockInWorldPredicate) null)
                                                                              )
                                                                              .then(Commands.literal("replace")
                                                                                            .executes(c -> fillBlocks(c.getSource(),
                                                                                                                      BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                              BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                                      BlockStateArgument.getBlock(c, "block"),
                                                                                                                      FillCommand.Mode.REPLACE,
                                                                                                                      (BlockInWorldPredicate) null)
                                                                                            )
                                                                                            .then(Commands.argument("filter", BlockPredicateArgument.blockPredicate())
                                                                                                          .executes(c -> {
                                                                                                                        Predicate<BlockInWorld> filter = BlockPredicateArgument.getBlockPredicate(c, "filter");
                                                                                                                        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                                                                                                                        return fillBlocks(c.getSource(),
                                                                                                                                          BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                                                  BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                                                          BlockStateArgument.getBlock(c, "block"),
                                                                                                                                          FillCommand.Mode.REPLACE,
                                                                                                                                          (l, x, y, z, b) -> filter.test(new BlockInWorld(l, mutable.set(x, y, z), b))
                                                                                                                        );
                                                                                                                    }
                                                                                                          )
                                                                                            )
                                                                              )
                                                                              .then(Commands.literal("keep")
                                                                                            .executes(c -> fillBlocks(c.getSource(),
                                                                                                                      BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                              BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                                      BlockStateArgument.getBlock(c, "block"),
                                                                                                                      FillCommand.Mode.REPLACE,
                                                                                                                      (l, x, y, z, b) -> l.isEmptyBlock_(x, y, z))
                                                                                            )
                                                                              )
                                                                              .then(Commands.literal("outline")
                                                                                            .executes(c -> fillBlocks(c.getSource(),
                                                                                                                      BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                              BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                                      BlockStateArgument.getBlock(c, "block"),
                                                                                                                      FillCommand.Mode.OUTLINE,
                                                                                                                      (BlockInWorldPredicate) null)
                                                                                            )
                                                                              )
                                                                              .then(Commands.literal("hollow")
                                                                                            .executes(c -> fillBlocks(c.getSource(),
                                                                                                                      BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                              BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                                      BlockStateArgument.getBlock(c, "block"),
                                                                                                                      FillCommand.Mode.HOLLOW,
                                                                                                                      (BlockInWorldPredicate) null)
                                                                                            )
                                                                              )
                                                                              .then(Commands.literal("destroy")
                                                                                            .executes(c -> fillBlocks(c.getSource(),
                                                                                                                      BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(c, "from"),
                                                                                                                                              BlockPosArgument.getLoadedBlockPos(c, "to")),
                                                                                                                      BlockStateArgument.getBlock(c, "block"),
                                                                                                                      FillCommand.Mode.DESTROY,
                                                                                                                      (BlockInWorldPredicate) null)
                                                                                            )
                                                                              )
                                                                )
                                                  )
                                    )
        );
    }
}
