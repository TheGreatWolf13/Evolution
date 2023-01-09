package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.IChunkStorage;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.patches.ILevelChunkSectionPatch;
import tgw.evolution.util.constants.BlockFlags;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public final class CommandAtm implements Command<CommandSourceStack> {

    public static final Predicate<BlockState> AIR = s -> s.isAir() || s.getBlock() == EvolutionBlocks.ATM.get();
    public static final Predicate<BlockState> ATM = s -> s.getBlock() == EvolutionBlocks.ATM.get();
    public static final IntFunction<BlockState> AIR_MAKER = i -> Blocks.AIR.defaultBlockState();
    public static final IntFunction<BlockState> ATM_MAKER = i -> i == 0 ?
                                                                 Blocks.AIR.defaultBlockState() :
                                                                 EvolutionBlocks.ATM.get().defaultBlockState().setValue(EvolutionBStates.ATM, i);
    private static final Command<CommandSourceStack> CMD = new CommandAtm();

    public static int fill(LevelChunk chunk, Predicate<BlockState> filter, boolean shouldCheckAtm, IntFunction<BlockState> blockMaker) {
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
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
                            int atm = shouldCheckAtm ? ((ILevelChunkSectionPatch) section).getAtmStorage().get(x, y, z) : 0;
                            level.setBlock(pos.set(globalX + x, globalY + y, globalZ + z), blockMaker.apply(atm), BlockFlags.BLOCK_UPDATE);
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
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(2))
                                    .then(Commands.literal("debug").executes(CMD))
                                    .then(Commands.literal("reset").executes(CMD))
                                    .then(Commands.literal("mark").then(Commands.argument("pos", ColumnPosArgument.columnPos()).executes(CMD)))
                                    .then(Commands.literal("remove").then(Commands.argument("pos", ColumnPosArgument.columnPos()).executes(CMD))));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = context.getInput();
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        boolean add = false;
        boolean remove = false;
        if (input.contains("mark")) {
            add = true;
        }
        else if (input.contains("remove")) {
            remove = true;
        }
        if (add || remove) {
            ColumnPos pos = ColumnPosArgument.getColumnPos(context, "pos");
            LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(pos.x), SectionPos.blockToSectionCoord(pos.z));
            if (chunk.isEmpty()) {
                source.sendFailure(new TranslatableComponent("command.evolution.atm.chunkEmpty"));
                return 0;
            }
            IChunkStorage chunkStorage = EvolutionCapabilities.getCapabilityOrThrow(chunk, CapabilityChunkStorage.INSTANCE);
            if (add) {
                if (chunkStorage.setContinuousAtmDebug(chunk, true)) {
                    source.sendSuccess(new TranslatableComponent("command.evolution.atm.markSuccess"), true);
                    return SINGLE_SUCCESS;
                }
                source.sendFailure(new TranslatableComponent("command.evolution.atm.markFail"));
                return 0;
            }
            if (chunkStorage.setContinuousAtmDebug(chunk, false)) {
                source.sendSuccess(new TranslatableComponent("command.evolution.atm.removeSuccess"), true);
                return SINGLE_SUCCESS;
            }
            source.sendFailure(new TranslatableComponent("command.evolution.atm.removeFail"));
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        BlockPos pos = player.blockPosition();
        LevelChunk chunk = level.getChunkAt(pos);
        if (chunk.isEmpty()) {
            source.sendFailure(new TranslatableComponent("command.evolution.atm.chunkEmpty"));
            return 0;
        }
        int count = 0;
        switch (input) {
            case "/atm debug" -> count = fill(chunk, AIR, true, ATM_MAKER);
            case "/atm reset" -> count = fill(chunk, ATM, false, AIR_MAKER);
        }
        source.sendSuccess(new TranslatableComponent("command.evolution.atm.success", count), true);
        return SINGLE_SUCCESS;
    }
}
