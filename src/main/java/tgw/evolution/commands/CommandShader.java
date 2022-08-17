package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCShader;

public final class CommandShader implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandShader();
    private static final IntegerArgumentType SHADER = IntegerArgumentType.integer(0);

    private CommandShader() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shader")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(2))
                                    .executes(CMD)
                                    .then(Commands.literal("toggle").executes(CMD))
                                    .then(Commands.argument("shaderId", SHADER).executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = context.getInput();
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        switch (input) {
            case "/shader" -> {
                EvolutionNetwork.send(player, new PacketSCShader(PacketSCShader.QUERY));
                return SINGLE_SUCCESS;
            }
            case "/shader toggle" -> {
                EvolutionNetwork.send(player, new PacketSCShader(PacketSCShader.TOGGLE));
                return SINGLE_SUCCESS;
            }
        }
        int shaderId = IntegerArgumentType.getInteger(context, "shaderId");
        EvolutionNetwork.send(player, new PacketSCShader(shaderId));
        return SINGLE_SUCCESS;
    }
}
