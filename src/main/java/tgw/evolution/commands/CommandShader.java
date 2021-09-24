package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCShader;

public class CommandShader implements Command<CommandSource> {

    private static final Command<CommandSource> CMD = new CommandShader();
    private static final IntegerArgumentType SHADER = IntegerArgumentType.integer(0);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("shader")
                                    .requires(cs -> cs.getEntity() instanceof PlayerEntity && cs.hasPermission(2))
                                    .executes(CMD)
                                    .then(Commands.literal("toggle").executes(CMD))
                                    .then(Commands.argument("shaderId", SHADER).executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String input = context.getInput();
        CommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrException();
        switch (input) {
            case "/shader": {
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCShader(PacketSCShader.QUERY));
                return SINGLE_SUCCESS;
            }
            case "/shader toggle": {
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCShader(PacketSCShader.TOGGLE));
                return SINGLE_SUCCESS;
            }
        }
        int shaderId = IntegerArgumentType.getInteger(context, "shaderId");
        EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCShader(shaderId));
        return SINGLE_SUCCESS;
    }
}
