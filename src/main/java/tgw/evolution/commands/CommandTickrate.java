package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.hooks.TickrateChanger;

public class CommandTickrate implements Command<CommandSource> {

    private static final Command<CommandSource> CMD = new CommandTickrate();
    private static final FloatArgumentType TPS = FloatArgumentType.floatArg(TickrateChanger.MIN_TICKRATE, TickrateChanger.MAX_TICKRATE);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tickrate").requires(cs -> cs.hasPermission(3)).then(Commands.argument("tps", TPS).executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSource> context) {
        float tickrate = FloatArgumentType.getFloat(context, "tps");
        boolean change = TickrateChanger.updateServerTickrate(tickrate);
        if (!change) {
            return 0;
        }
        context.getSource().sendSuccess(new TranslationTextComponent("command.evolution.tickrate", tickrate), true);
        return SINGLE_SUCCESS;
    }
}
