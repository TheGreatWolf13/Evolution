package tgw.evolution.commands;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.init.IVariant;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.util.Map;

public class CommandVariantDebug implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandVariantDebug();

    private static void make3by3(LevelWriter level, int x, int y, int z, BlockState state) {
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                level.setBlockAndUpdate_(x + dx, y, z + dz, state);
            }
        }
    }

    private static void makeBorder(LevelWriter level, int y, int startX, int startZ, int endX, int endZ) {
        int minX;
        int maxX;
        if (startX < endX) {
            minX = startX;
            maxX = endX;
        }
        else {
            minX = endX;
            maxX = startX;
        }
        int minZ;
        int maxZ;
        if (startZ < endZ) {
            minZ = startZ;
            maxZ = endZ;
        }
        else {
            minZ = endZ;
            maxZ = startZ;
        }
        for (int x = minX; x <= maxX; x++) {
            level.setBlockAndUpdate_(x, y, minZ, Blocks.GOLD_BLOCK.defaultBlockState());
        }
        for (int x = minX; x <= maxX; x++) {
            level.setBlockAndUpdate_(x, y, maxZ, Blocks.GOLD_BLOCK.defaultBlockState());
        }
        for (int z = minZ; z <= maxZ; z++) {
            level.setBlockAndUpdate_(minX, y, z, Blocks.GOLD_BLOCK.defaultBlockState());
        }
        for (int z = minZ; z <= maxZ; z++) {
            level.setBlockAndUpdate_(maxX, y, z, Blocks.GOLD_BLOCK.defaultBlockState());
        }
    }

    private static void makeCake(LevelWriter level, int x, int y, int z, BlockState state) {
        make3by3(level, x, y - 1, z, Blocks.BEDROCK.defaultBlockState());
        make3by3(level, x, y, z, state);
    }

    private static <T extends IVariant<T>> void placeVariant(LevelWriter level, int x, int y, int z, Direction facing, T[] array) {
        int startX = x;
        int startZ = z;
        Direction secondaryDir = facing.getClockWise();
        boolean isPrimaryX = facing.getAxis() == Direction.Axis.X;
        boolean isPrimaryNegative = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        int primaryInc;
        if (isPrimaryNegative) {
            primaryInc = -3;
            if (isPrimaryX) {
                x -= 2;
                ++startX;
            }
            else {
                z -= 2;
                ++startZ;
            }
        }
        else {
            primaryInc = 3;
            if (isPrimaryX) {
                --startX;
            }
            else {
                --startZ;
            }
        }
        boolean isSecondaryNegative = secondaryDir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        int secondaryInc;
        if (isSecondaryNegative) {
            secondaryInc = -3;
            if (isPrimaryX) {
                z -= 2;
            }
            else {
                x -= 2;
            }
        }
        else {
            secondaryInc = 3;
        }
        int px = x;
        int pz = z;
        OList<Map<T, ? extends Block>> blocks = array[0].getBlocks();
        for (T variant : array) {
            for (int i = 0, len = blocks.size(); i < len; ++i) {
                makeCake(level, px, y, pz, variant.get(blocks.get(i)).defaultBlockState());
                if (isPrimaryX) {
                    px += primaryInc;
                }
                else {
                    pz += primaryInc;
                }
            }
            if (isPrimaryX) {
                px = x;
                pz += secondaryInc;
            }
            else {
                px += secondaryInc;
                pz = z;
            }
        }
        if (isPrimaryX) {
            makeBorder(level, y, startX, startZ + (isSecondaryNegative ? 1 : -1), startX + blocks.size() * primaryInc + (isPrimaryNegative ? -1 : 1), pz + (isSecondaryNegative ? 2 : 0));
        }
        else {
            makeBorder(level, y, startX + (isSecondaryNegative ? 1 : -1), startZ, px + (isSecondaryNegative ? 2 : 0), startZ + blocks.size() * primaryInc + (isPrimaryNegative ? -1 : 1));
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("variant_debug")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(4))
                                    .then(Commands.literal("rock")
                                                  .executes(CMD)
                                    )
                                    .then(Commands.literal("wood")
                                                  .executes(CMD)
                                    )
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos pos = player.blockPosition();
        ServerLevel level = player.getLevel();
        int y = pos.getY();
        if (y < level.getMinBuildHeight() + 2) {
            return 0;
        }
        if (y > level.getMaxBuildHeight()) {
            return 0;
        }
        switch (context.getInput()) {
            case "/variant_debug rock" -> placeVariant(level, pos.getX(), y - 1, pos.getZ(), player.getDirection(), RockVariant.VALUES_STONE);
            case "/variant_debug wood" -> placeVariant(level, pos.getX(), y - 1, pos.getZ(), player.getDirection(), WoodVariant.VALUES);
        }
        return SINGLE_SUCCESS;
    }
}
