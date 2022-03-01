package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class CommandGamemode implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandGamemode();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gm").requires(cs -> cs.getEntity() instanceof ServerPlayer && cs.hasPermission(2)).executes(CMD));
    }

    private static void sendGameModeFeedback(CommandSourceStack source, ServerPlayer player, GameType gameType) {
        Component comp = new TranslatableComponent("gameMode." + gameType.getName());
        if (source.getEntity() == player) {
            source.sendSuccess(new TranslatableComponent("commands.gamemode.success.self", comp), true);
        }
        else {
            if (source.getServer().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                player.displayClientMessage(new TranslatableComponent("gameMode.changed", comp), false);
            }
            source.sendSuccess(new TranslatableComponent("commands.gamemode.success.other", player.getDisplayName(), comp), true);
        }
    }

    private static int setGameMode(CommandContext<CommandSourceStack> source, ServerPlayer player, GameType gameType) {
        if (player.gameMode.getGameModeForPlayer() != gameType) {
            player.setGameMode(gameType);
            sendGameModeFeedback(source.getSource(), player, gameType);
            return 1;
        }
        return 0;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (source.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = source.getPlayerOrException();
            GameType gm = player.isCreative() ? GameType.SURVIVAL : GameType.CREATIVE;
            return setGameMode(context, player, gm);
        }
        return 0;
    }
}
