package tgw.evolution.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.BlockInWorldPredicate;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.Predicate;

@Mixin(SetBlockCommand.class)
public abstract class Mixin_M_SetBlockCommand {

    @Shadow @Final private static SimpleCommandExceptionType ERROR_FAILED;

    @Overwrite
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setblock")
                                    .requires(cs -> cs.hasPermission(2))
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                  .then(Commands.argument("block", BlockStateArgument.block())
                                                                .executes(c -> {
                                                                    return setBlock(c.getSource(),
                                                                                    BlockPosArgument.getLoadedBlockPos(c, "pos"),
                                                                                    BlockStateArgument.getBlock(c, "block"),
                                                                                    SetBlockCommand.Mode.REPLACE,
                                                                                    (BlockInWorldPredicate) null);
                                                                })
                                                                .then(Commands.literal("destroy")
                                                                              .executes(c -> {
                                                                                  return setBlock(c.getSource(),
                                                                                                  BlockPosArgument.getLoadedBlockPos(c, "pos"),
                                                                                                  BlockStateArgument.getBlock(c, "block"),
                                                                                                  SetBlockCommand.Mode.DESTROY,
                                                                                                  (BlockInWorldPredicate) null);
                                                                              }))
                                                                .then(Commands.literal("keep")
                                                                              .executes(c -> {
                                                                                  return setBlock(c.getSource(),
                                                                                                  BlockPosArgument.getLoadedBlockPos(c, "pos"),
                                                                                                  BlockStateArgument.getBlock(c, "block"),
                                                                                                  SetBlockCommand.Mode.REPLACE,
                                                                                                  (l, x, y, z, b) -> {
                                                                                                      return l.isEmptyBlock_(x, y, z);
                                                                                                  });
                                                                              }))
                                                                .then(Commands.literal("replace")
                                                                              .executes(c -> {
                                                                                  return setBlock(c.getSource(),
                                                                                                  BlockPosArgument.getLoadedBlockPos(c, "pos"),
                                                                                                  BlockStateArgument.getBlock(c, "block"),
                                                                                                  SetBlockCommand.Mode.REPLACE,
                                                                                                  (BlockInWorldPredicate) null);
                                                                              })))));
    }

    @Overwrite
    @DeleteMethod
    private static int setBlock(CommandSourceStack commandSourceStack,
                                BlockPos blockPos,
                                BlockInput blockInput,
                                SetBlockCommand.Mode mode,
                                @Nullable Predicate<BlockInWorld> predicate) {
        throw new AbstractMethodError();
    }

    @Unique
    private static int setBlock(CommandSourceStack stack,
                                BlockPos pos,
                                BlockInput input,
                                SetBlockCommand.Mode mode,
                                @Nullable BlockInWorldPredicate predicate) throws CommandSyntaxException {
        ServerLevel level = stack.getLevel();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (predicate != null && !predicate.test(level, x, y, z, true)) {
            throw ERROR_FAILED.create();
        }
        boolean worked;
        if (mode == SetBlockCommand.Mode.DESTROY) {
            level.destroyBlock_(x, y, z, true);
            worked = !input.getState().isAir() || !level.getBlockState_(x, y, z).isAir();
        }
        else {
            Clearable.tryClear(level.getBlockEntity_(x, y, z));
            worked = true;
        }
        if (worked && !input.place_(level, x, y, z, BlockFlags.BLOCK_UPDATE)) {
            throw ERROR_FAILED.create();
        }
        level.blockUpdated_(x, y, z, input.getState().getBlock());
        stack.sendSuccess(new TranslatableComponent("commands.setblock.success", x, y, z), true);
        return 1;
    }
}
