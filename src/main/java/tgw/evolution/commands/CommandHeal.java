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
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.patches.IEffectInstancePatch;

public class CommandHeal implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandHeal();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("heal").requires(cs -> cs.getEntity() instanceof ServerPlayer && cs.hasPermission(2)).executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
        IHunger hunger = player.getCapability(CapabilityHunger.INSTANCE).orElseThrow(IllegalStateException::new);
        player.setHealth(player.getMaxHealth());
        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
        thirst.setHydrationLevel(0);
        hunger.setHungerLevel(HungerStats.HUNGER_CAPACITY);
        hunger.setSaturationLevel(0);
        player.addEffect(IEffectInstancePatch.newInfinite(EvolutionEffects.HYDRATION.get(), 99, false, false, true));
        player.addEffect(IEffectInstancePatch.newInfinite(EvolutionEffects.SATURATION.get(), 99, false, false, true));
        return SINGLE_SUCCESS;
    }
}
