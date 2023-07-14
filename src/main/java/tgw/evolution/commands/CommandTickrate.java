package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.hooks.TickrateChanger;

public final class CommandTickrate implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandTickrate();
    private static final FloatArgumentType TPS = FloatArgumentType.floatArg(TickrateChanger.MIN_TICKRATE, TickrateChanger.MAX_TICKRATE);

    private CommandTickrate() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tickrate")
                                    .requires(cs -> cs.hasPermission(3))
                                    .executes(CMD)
                                    .then(Commands.argument("tps", TPS).executes(CMD))
                                    .then(Commands.literal("default").executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        String input = context.getInput();
        CommandSourceStack source = context.getSource();
        float tickrate = Float.NaN;
        switch (input) {
            case "/tickrate" -> {
                source.sendSuccess(new TranslatableComponent("command.evolution.tickrate.current", TickrateChanger.getCurrentTickrate()), false);
                return SINGLE_SUCCESS;
            }
            case "/tickrate default" -> tickrate = 20;
        }
        if (Float.isNaN(tickrate)) {
            tickrate = FloatArgumentType.getFloat(context, "tps");
        }
        boolean change = TickrateChanger.updateServerTickrate(source.getServer(), tickrate);
        if (!change) {
            return 0;
        }
        source.sendSuccess(new TranslatableComponent("command.evolution.tickrate.change", tickrate), true);
        return SINGLE_SUCCESS;
    }
}
