package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class CommandIntegrity {

    private CommandIntegrity() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("integrity")
                                    .requires(cs -> cs.hasPermission(2))
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                  .executes(CommandIntegrity::run))
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        CommandSourceStack source = context.getSource();
        int x = pos.getX();
        int z = pos.getZ();
        LevelChunk chunk = source.getLevel().getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        if (chunk.isEmpty()) {
            source.sendFailure(new TranslatableComponent("command.evolution.integrity.chunkEmpty"));
            return 0;
        }
        chunk.primeStructural(true);
        int y = pos.getY();
        int index = chunk.getSectionIndex(y);
        LevelChunkSection section = chunk.getSection(index);
        if (section.getStabilityStorage().get(x & 15, y & 15, z & 15)) {
            source.sendSuccess(new TranslatableComponent("command.evolution.integrity.query.stable", x, y, z, section.getLoadFactorStorage().get(x & 15, y & 15, z & 15), section.getIntegrityStorage().get(x & 15, y & 15, z & 15)), true);
        }
        else {
            source.sendSuccess(new TranslatableComponent("command.evolution.integrity.query", x, y, z, section.getLoadFactorStorage().get(x & 15, y & 15, z & 15), section.getIntegrityStorage().get(x & 15, y & 15, z & 15)), true);
        }
        return 1;
    }
}
