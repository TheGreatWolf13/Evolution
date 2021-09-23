package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.patches.IMinecraftServerPatch;

public class CommandPause implements Command<CommandSource> {

    private static final Command<CommandSource> CMD = new CommandPause();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("pause").requires(cs -> cs.hasPermission(2)).executes(CMD));
        dispatcher.register(Commands.literal("resume").requires(cs -> cs.hasPermission(2)).executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String input = context.getInput();
        MinecraftServer server = context.getSource().getServer();
        switch (input) {
            case "/pause": {
                if (!((IMinecraftServerPatch) server).isMultiplayerPaused()) {
                    ((IMinecraftServerPatch) server).setMultiplayerPaused(true);
                    context.getSource().sendSuccess(EvolutionTexts.COMMAND_PAUSE_PAUSE_SUCCESS, true);
                    return SINGLE_SUCCESS;
                }
                context.getSource().sendFailure(EvolutionTexts.COMMAND_PAUSE_PAUSE_FAIL);
                return 0;
            }
            case "/resume": {
                if (((IMinecraftServerPatch) server).isMultiplayerPaused()) {
                    ((IMinecraftServerPatch) server).setMultiplayerPaused(false);
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
