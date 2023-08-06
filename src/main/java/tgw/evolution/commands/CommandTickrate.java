package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.hooks.TickrateChanger;

public final class CommandTickrate {

    private static final FloatArgumentType TPS = FloatArgumentType.floatArg(TickrateChanger.MIN_TICKRATE, TickrateChanger.MAX_TICKRATE);

    private CommandTickrate() {
    }

    private static int changeTps(CommandSourceStack source, float tickrate) {
        boolean change = TickrateChanger.updateServerTickrate(source.getServer(), tickrate);
        if (!change) {
            return 0;
        }
        source.sendSuccess(new TranslatableComponent("command.evolution.tickrate.change", tickrate), true);
        return 1;
    }

    private static int query(CommandSourceStack source) {
        source.sendSuccess(new TranslatableComponent("command.evolution.tickrate.current", TickrateChanger.getCurrentTickrate()), false);
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tickrate")
                                    .requires(cs -> cs.hasPermission(3))
                                    .executes(c -> query(c.getSource()))
                                    .then(Commands.argument("tps", TPS)
                                                  .executes(c -> changeTps(c.getSource(), FloatArgumentType.getFloat(c, "tps")))
                                    )
                                    .then(Commands.literal("default")
                                                  .executes(c -> changeTps(c.getSource(), 20))
                                    )
        );
    }
}
