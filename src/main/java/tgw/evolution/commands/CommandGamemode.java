package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;

public class CommandGamemode implements Command<CommandSource> {

    private static final CommandGamemode CMD = new CommandGamemode();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("gm").requires(cs -> cs.getEntity() instanceof ServerPlayerEntity && cs.hasPermission(2)).executes(CMD));
    }

    private static void sendGameModeFeedback(CommandSource source, ServerPlayerEntity player, GameType gameType) {
        ITextComponent comp = new TranslationTextComponent("gameMode." + gameType.getName());
        if (source.getEntity() == player) {
            source.sendSuccess(new TranslationTextComponent("commands.gamemode.success.self", comp), true);
        }
        else {
            if (source.getServer().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                player.displayClientMessage(new TranslationTextComponent("gameMode.changed", comp), false);
            }
            source.sendSuccess(new TranslationTextComponent("commands.gamemode.success.other", player.getDisplayName(), comp), true);
        }
    }

    private static int setGameMode(CommandContext<CommandSource> source, ServerPlayerEntity player, GameType gameType) {
        if (player.gameMode.getGameModeForPlayer() != gameType) {
            player.setGameMode(gameType);
            sendGameModeFeedback(source.getSource(), player, gameType);
            return 1;
        }
        return 0;
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        CommandSource source = context.getSource();
        if (source.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = source.getPlayerOrException();
            GameType gm = player.isCreative() ? GameType.SURVIVAL : GameType.CREATIVE;
            return setGameMode(context, player, gm);
        }
        return 0;
    }
}
