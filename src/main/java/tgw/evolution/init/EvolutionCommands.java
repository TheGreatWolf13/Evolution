package tgw.evolution.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import tgw.evolution.commands.*;
import tgw.evolution.commands.regen.CommandRegen;

public final class EvolutionCommands {

    private EvolutionCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandTickrate.register(dispatcher);
        CommandGamemode.register(dispatcher);
        CommandRegen.register(dispatcher);
        CommandDate.register(dispatcher);
        CommandHeal.register(dispatcher);
        CommandPause.register(dispatcher);
        CommandShader.register(dispatcher);
        CommandTemperature.register(dispatcher);
    }
}
