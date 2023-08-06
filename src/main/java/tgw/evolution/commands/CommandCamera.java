package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class CommandCamera implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandCamera();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("camera")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(2))
                                    .then(Commands.literal("reset")
                                                  .executes(CMD)
                                    )
                                    .then(Commands.argument("target", EntityArgument.entity())
                                                  .executes(CMD)
                                    )
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = context.getInput();
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        if ("/camera reset".equals(input)) {
            player.setCamera(null);
            return SINGLE_SUCCESS;
        }
        Entity entity = EntityArgument.getEntity(context, "target");
        player.setCamera(entity);
        return SINGLE_SUCCESS;
    }
}
