package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IMinecraftServerPatch;

public final class CommandPause implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandPause();

    private CommandPause() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pause").requires(cs -> cs.hasPermission(2)).executes(CMD));
        dispatcher.register(Commands.literal("resume").requires(cs -> cs.hasPermission(2)).executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        String input = context.getInput();
        IMinecraftServerPatch server = (IMinecraftServerPatch) context.getSource().getServer();
        switch (input) {
            case "/pause" -> {
                if (!server.isMultiplayerPaused()) {
                    server.setMultiplayerPaused(true);
                    context.getSource().sendSuccess(EvolutionTexts.COMMAND_PAUSE_PAUSE_SUCCESS, true);
                    return SINGLE_SUCCESS;
                }
                context.getSource().sendFailure(EvolutionTexts.COMMAND_PAUSE_PAUSE_FAIL);
                return 0;
            }
            case "/resume" -> {
                if (server.isMultiplayerPaused()) {
                    server.setMultiplayerPaused(false);
                    context.getSource().sendSuccess(EvolutionTexts.COMMAND_PAUSE_RESUME_SUCCESS, true);
                    return SINGLE_SUCCESS;
                }
                context.getSource().sendFailure(EvolutionTexts.COMMAND_PAUSE_RESUME_FAIL);
                return 0;
            }
        }
        return 0;
    }
}
