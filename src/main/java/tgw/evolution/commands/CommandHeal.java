package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.patches.IEffectInstancePatch;

public class CommandHeal implements Command<CommandSource> {

    private static final Command<CommandSource> CMD = new CommandHeal();

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("heal")
                                    .requires(cs -> cs.getEntity() instanceof ServerPlayerEntity && cs.hasPermission(2))
                                    .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
        player.setHealth(player.getMaxHealth());
        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
        thirst.setHydrationLevel(0);
        player.addEffect(IEffectInstancePatch.newInfinite(EvolutionEffects.HYDRATION.get(), 99, false, false, true));
        return SINGLE_SUCCESS;
    }
}
