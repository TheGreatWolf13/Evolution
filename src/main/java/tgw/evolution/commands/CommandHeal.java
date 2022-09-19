package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.food.HungerStats;
import tgw.evolution.capabilities.food.IHunger;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;
import tgw.evolution.init.EvolutionCapabilities;
import tgw.evolution.init.EvolutionEffects;

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
        EvolutionCapabilities.revive(player);
        IThirst thirst = EvolutionCapabilities.getCapabilityOrThrow(player, CapabilityThirst.INSTANCE);
        IHunger hunger = EvolutionCapabilities.getCapabilityOrThrow(player, CapabilityHunger.INSTANCE);
        player.setHealth(player.getMaxHealth());
        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
        thirst.setHydrationLevel(0);
        hunger.setHungerLevel(HungerStats.HUNGER_CAPACITY);
        hunger.setSaturationLevel(0);
        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.HYDRATION.get(), 99, false, false, true));
        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.SATURATION.get(), 99, false, false, true));
        EvolutionCapabilities.invalidate(player);
        return SINGLE_SUCCESS;
    }
}
