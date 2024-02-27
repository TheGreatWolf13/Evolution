package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.client.util.Shader;
import tgw.evolution.network.PacketSCShader;

public final class CommandShader {

    private static final IntegerArgumentType SHADER = IntegerArgumentType.integer(0);

    private CommandShader() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shader")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(2))
                                    .executes(c -> run(c.getSource().getPlayerOrException(), Shader.QUERY))
                                    .then(Commands.literal("toggle")
                                                  .executes(c -> run(c.getSource().getPlayerOrException(), Shader.TOGGLE))
                                    )
                                    .then(Commands.literal("cycle")
                                                  .executes(c -> run(c.getSource().getPlayerOrException(), Shader.CYCLE))
                                    )
                                    .then(Commands.argument("shaderId", SHADER)
                                                  .executes(c -> run(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "shaderId")))
                                    )
        );
    }

    private static int run(ServerPlayer player, int id) {
        player.connection.send(new PacketSCShader(id));
        return 1;
    }
}
