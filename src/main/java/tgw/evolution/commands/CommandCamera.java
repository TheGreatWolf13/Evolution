package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

public final class CommandCamera {

    private CommandCamera() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("camera")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(2))
                                    .then(Commands.literal("reset")
                                                  .executes(CommandCamera::reset)
                                    )
                                    .then(Commands.literal("set")
                                                  .then(Commands.argument("target", EntityArgument.entity())
                                                                .executes(CommandCamera::set)
                                                  )
                                    )
        );
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().getPlayerOrException().setCamera(null);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().getPlayerOrException().setCamera(EntityArgument.getEntity(context, "target"));
        return 1;
    }
}
