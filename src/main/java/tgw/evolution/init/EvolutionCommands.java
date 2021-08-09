package tgw.evolution.init;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.server.command.EnumArgument;
import net.minecraftforge.server.command.ModIdArgument;
import tgw.evolution.commands.CommandDate;
import tgw.evolution.commands.CommandGamemode;
import tgw.evolution.commands.CommandRegen;
import tgw.evolution.commands.CommandTickrate;
import tgw.evolution.util.EnumArgumentSerializer;

public final class EvolutionCommands {

    private EvolutionCommands() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        CommandTickrate.register(dispatcher);
        CommandGamemode.register(dispatcher);
        CommandRegen.register(dispatcher);
        CommandDate.register(dispatcher);
    }

    public static void registerArguments() {
        ArgumentTypes.register("enum", EnumArgument.class, new EnumArgumentSerializer());
        ArgumentTypes.register("forge:mod_id", ModIdArgument.class, new ArgumentSerializer<>(ModIdArgument::new));
    }
}
