package tgw.evolution.commands;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

public class CommandVariantDebug implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandVariantDebug();

    private static void make3by3(LevelWriter level, BlockPos.MutableBlockPos mutablePos, BlockState state, Direction primary, Direction secondary) {
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                level.setBlock(mutablePos, state, BlockFlags.NOTIFY_AND_UPDATE);
                mutablePos.move(primary);
            }
            mutablePos.move(primary.getOpposite(), 3);
            mutablePos.move(secondary);
        }
        mutablePos.move(secondary.getOpposite(), 3);
    }

    private static void makeBorder(LevelWriter level, BlockPos.MutableBlockPos mutablePos, int minX, int minZ, int maxX, int maxZ) {
        mutablePos.setZ(minZ);
        for (int x = minX; x <= maxX; x++) {
            mutablePos.setX(x);
            level.setBlock(mutablePos, Blocks.GOLD_BLOCK.defaultBlockState(), BlockFlags.NOTIFY_AND_UPDATE);
        }
        mutablePos.setZ(maxZ);
        for (int x = minX; x <= maxX; x++) {
            mutablePos.setX(x);
            level.setBlock(mutablePos, Blocks.GOLD_BLOCK.defaultBlockState(), BlockFlags.NOTIFY_AND_UPDATE);
        }
        mutablePos.setX(minX);
        for (int z = minZ; z <= maxZ; z++) {
            mutablePos.setZ(z);
            level.setBlock(mutablePos, Blocks.GOLD_BLOCK.defaultBlockState(), BlockFlags.NOTIFY_AND_UPDATE);
        }
        mutablePos.setX(maxX);
        for (int z = minZ; z <= maxZ; z++) {
            mutablePos.setZ(z);
            level.setBlock(mutablePos, Blocks.GOLD_BLOCK.defaultBlockState(), BlockFlags.NOTIFY_AND_UPDATE);
        }
    }

    @CanIgnoreReturnValue
    private static int makeCake(LevelWriter level, BlockPos.MutableBlockPos mutablePos, BlockState state, Direction primary, Direction secondary) {
        mutablePos.move(Direction.DOWN);
        make3by3(level, mutablePos, Blocks.BEDROCK.defaultBlockState(), primary, secondary);
        mutablePos.move(Direction.UP);
        make3by3(level, mutablePos, state, primary, secondary);
        mutablePos.move(primary, 3);
        return mutablePos.get(secondary.getAxis()) + 3 * secondary.getStepX() + 3 * secondary.getStepZ();
    }

    private static void placeRockVariant(LevelWriter level, BlockPos pos, Direction facing) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(pos);
        Direction secondaryDir = facing.getClockWise();
        int returningPos = mutablePos.get(facing.getAxis());
        int primaryMaxPos = returningPos;
        int secondaryMaxPos = pos.get(secondaryDir.getAxis());
        for (RockVariant variant : RockVariant.VALUES_STONE) {
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.STONES).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.COBBLESTONES).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.POLISHED_STONES).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.STONEBRICKS).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.GRAVELS).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.SANDS).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.DIRTS).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.DRY_GRASSES).defaultBlockState(), facing, secondaryDir);
            secondaryMaxPos = makeCake(level, mutablePos, variant.get(EvolutionBlocks.GRASSES).defaultBlockState(), facing, secondaryDir);
            primaryMaxPos = mutablePos.get(facing.getAxis());
            int delta = Math.abs(mutablePos.get(facing.getAxis()) - returningPos);
            mutablePos.move(facing.getOpposite(), delta);
            mutablePos.move(secondaryDir, 3);
        }
        int primaryMinPos = pos.get(facing.getAxis()) - facing.getStepX() - facing.getStepZ();
        int secondaryMinPos = pos.get(secondaryDir.getAxis()) - secondaryDir.getStepX() - secondaryDir.getStepZ();
        if (facing.getAxis() == Direction.Axis.X) {
            makeBorder(level, mutablePos, Math.min(primaryMinPos, primaryMaxPos), Math.min(secondaryMinPos, secondaryMaxPos),
                       Math.max(primaryMinPos, primaryMaxPos), Math.max(secondaryMinPos, secondaryMaxPos));
        }
        else {
            makeBorder(level, mutablePos, Math.min(secondaryMinPos, secondaryMaxPos), Math.min(primaryMinPos, primaryMaxPos),
                       Math.max(secondaryMinPos, secondaryMaxPos), Math.max(primaryMinPos, primaryMaxPos));
        }
    }

    private static void placeWoodVariant(LevelWriter level, BlockPos pos, Direction facing) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        mutablePos.set(pos);
        Direction secondaryDir = facing.getClockWise();
        int returningPos = mutablePos.get(facing.getAxis());
        for (WoodVariant variant : WoodVariant.VALUES) {
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.LOGS).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.PLANKS).defaultBlockState(), facing, secondaryDir);
            makeCake(level, mutablePos, variant.get(EvolutionBlocks.LEAVES).defaultBlockState(), facing, secondaryDir);
            int delta = Math.abs(mutablePos.get(facing.getAxis()) - returningPos);
            mutablePos.move(facing.getOpposite(), delta);
            mutablePos.move(secondaryDir, 3);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("variant_debug")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(4))
                                    .then(Commands.literal("rock").executes(CMD))
                                    .then(Commands.literal("wood").executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos pos = player.blockPosition();
        ServerLevel level = player.getLevel();
        if (pos.getY() < level.getMinBuildHeight() + 2) {
            return 0;
        }
        if (pos.getY() > level.getMaxBuildHeight()) {
            return 0;
        }
        switch (context.getInput()) {
            case "/variant_debug rock" -> placeRockVariant(level, pos.below(), player.getDirection());
            case "/variant_debug wood" -> placeWoodVariant(level, pos.below(), player.getDirection());
        }
        return SINGLE_SUCCESS;
    }
}
