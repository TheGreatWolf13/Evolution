package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.patches.PatchServerPlayer;

public final class CommandHeal implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandHeal();

    private CommandHeal() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("heal").requires(cs -> cs.getEntity() instanceof ServerPlayer && cs.hasPermission(2)).executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        CapabilityThirst thirst = ((PatchServerPlayer) player).getThirstStats();
        CapabilityHunger hunger = ((PatchServerPlayer) player).getHungerStats();
        player.setHealth(player.getMaxHealth());
        thirst.setThirstLevel(CapabilityThirst.THIRST_CAPACITY);
        thirst.setHydrationLevel(0);
        hunger.setHungerLevel(CapabilityHunger.HUNGER_CAPACITY);
        hunger.setSaturationLevel(0);
        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.HYDRATION, 99, false, false, true));
        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.SATURATION, 99, false, false, true));
        return SINGLE_SUCCESS;
    }
}
