package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public final class CommandAtm {

    public static final Predicate<BlockState> AIR = s -> s.isAir() || s.getBlock() == EvolutionBlocks.ATM;
    public static final Predicate<BlockState> ATM = s -> s.getBlock() == EvolutionBlocks.ATM;
    public static final IntFunction<BlockState> AIR_MAKER = i -> Blocks.AIR.defaultBlockState();
    public static final IntFunction<BlockState> ATM_MAKER = i -> EvolutionBlocks.ATM.defaultBlockState().setValue(EvolutionBStates.ATM, i);

    private CommandAtm() {}

    public static int fill(LevelChunk chunk, Predicate<BlockState> filter, boolean shouldCheckAtm, IntFunction<BlockState> blockMaker) {
        int count = 0;
        Level level = chunk.getLevel();
        LevelChunkSection[] sections = chunk.getSections();
        int globalX = chunk.getPos().x << 4;
        int globalZ = chunk.getPos().z << 4;
        for (int i = 0, len = sections.length; i < len; i++) {
            LevelChunkSection section = sections[i];
            if (!shouldCheckAtm && section.hasOnlyAir()) {
                continue;
            }
            int globalY = chunk.getSectionYFromSectionIndex(i) << 4;
            for (int x = 0; x < 16; x++) {
                for (int y = 15; y >= 0; y--) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (filter.test(state)) {
                            int atm = shouldCheckAtm ? section.getAtmStorage().get(x, y, z) : 0;
                            level.setBlock_(globalX + x, globalY + y, globalZ + z, blockMaker.apply(atm), BlockFlags.BLOCK_UPDATE);
                            ++count;
                        }
                    }
                }
            }
        }
        return count;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("atm")
                                    .requires(cs -> cs.hasPermission(2))
                                    .then(Commands.literal("debug")
                                                  .executes(c -> {
                                                      CommandSourceStack s = c.getSource();
                                                      Vec3 p = s.getPosition();
                                                      return run(s, true, Mth.floor(p.x), Mth.floor(p.z));
                                                  })
                                                  .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                                                                .executes(c -> {
                                                                    CommandSourceStack s = c.getSource();
                                                                    ColumnPos p = ColumnPosArgument.getColumnPos(c, "pos");
                                                                    return run(s, true, p.x, p.z);
                                                                })
                                                  )
                                    )
                                    .then(Commands.literal("reset")
                                                  .executes(c -> {
                                                      CommandSourceStack s = c.getSource();
                                                      Vec3 p = s.getPosition();
                                                      return run(s, false, Mth.floor(p.x), Mth.floor(p.z));
                                                  })
                                                  .then(Commands.argument("pos", ColumnPosArgument.columnPos())
                                                                .executes(c -> {
                                                                    CommandSourceStack s = c.getSource();
                                                                    ColumnPos p = ColumnPosArgument.getColumnPos(c, "pos");
                                                                    return run(s, false, p.x, p.z);
                                                                })
                                                  )
                                    )
        );
    }

    public static int run(CommandSourceStack source, boolean debug, int x, int z) throws CommandSyntaxException {
        LevelChunk chunk = source.getLevel().getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        if (chunk.isEmpty()) {
            source.sendFailure(new TranslatableComponent("command.evolution.atm.chunkEmpty"));
            return 0;
        }
        if (debug) {
            if (chunk.getChunkStorage().setContinuousAtmDebug(chunk, true)) {
                source.sendSuccess(new TranslatableComponent("command.evolution.atm.debugSuccess"), true);
                return 1;
            }
            source.sendFailure(new TranslatableComponent("command.evolution.atm.debugFail"));
            return 0;
        }
        if (chunk.getChunkStorage().setContinuousAtmDebug(chunk, false)) {
            source.sendSuccess(new TranslatableComponent("command.evolution.atm.resetSuccess"), true);
            return 1;
        }
        source.sendFailure(new TranslatableComponent("command.evolution.atm.resetFail"));
        return 0;
    }
}
