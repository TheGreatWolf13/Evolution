package tgw.evolution.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import tgw.evolution.commands.*;

public final class EvolutionCommands {

    private EvolutionCommands() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        CommandTickrate.register(dispatcher);
        CommandGamemode.register(dispatcher);
        CommandRegen.register(dispatcher);
        CommandDate.register(dispatcher);
        CommandHeal.register(dispatcher);
    }
}
