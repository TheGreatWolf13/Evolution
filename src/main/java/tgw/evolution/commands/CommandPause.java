package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import tgw.evolution.init.EvolutionTexts;

public final class CommandPause {

    private CommandPause() {
    }

    private static int pause(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        if (!server.isMultiplayerPaused()) {
            server.setMultiplayerPaused(true);
            source.sendSuccess(EvolutionTexts.COMMAND_PAUSE_PAUSE_SUCCESS, true);
            return 1;
        }
        source.sendFailure(EvolutionTexts.COMMAND_PAUSE_PAUSE_FAIL);
        return 0;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pause")
                                    .requires(cs -> cs.hasPermission(2))
                                    .executes(c -> pause(c.getSource()))
        );
        dispatcher.register(Commands.literal("resume")
                                    .requires(cs -> cs.hasPermission(2))
                                    .executes(c -> resume(c.getSource()))
        );
    }

    private static int resume(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        if (server.isMultiplayerPaused()) {
            server.setMultiplayerPaused(false);
            source.sendSuccess(EvolutionTexts.COMMAND_PAUSE_RESUME_SUCCESS, true);
            return 1;
        }
        source.sendFailure(EvolutionTexts.COMMAND_PAUSE_RESUME_FAIL);
        return 0;
    }
}
