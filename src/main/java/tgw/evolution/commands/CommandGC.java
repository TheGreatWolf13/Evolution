package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CommandGC implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandGC();
    private long lastExecution = Util.getMillis();

    private CommandGC() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gc").requires(cs -> cs.hasPermission(4)).executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        long now = Util.getMillis();
        if (now - this.lastExecution >= 30_000) {
            this.lastExecution = now;
            System.gc();
        }
        return SINGLE_SUCCESS;
    }
}
