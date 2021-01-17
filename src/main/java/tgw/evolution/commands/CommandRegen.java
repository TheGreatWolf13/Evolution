package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandRegen implements Command<CommandSource> {

    private static final CommandRegen CMD = new CommandRegen();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("regen")
                                    .requires(cs -> cs.getEntity() instanceof ServerPlayerEntity && cs.hasPermissionLevel(2))
                                    .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        CommandSource source = context.getSource();
        if (source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = source.asPlayer();
            player.setHealth(player.getMaxHealth());
            return 1;
        }
        return 0;
    }
}
