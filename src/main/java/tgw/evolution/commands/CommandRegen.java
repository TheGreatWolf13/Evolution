package tgw.evolution.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.food.HungerStats;
import tgw.evolution.capabilities.food.IHunger;
import tgw.evolution.capabilities.stamina.CapabilityStamina;
import tgw.evolution.capabilities.stamina.IStamina;
import tgw.evolution.capabilities.stamina.StaminaStats;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;
import tgw.evolution.commands.argument.EnumEvArgument;
import tgw.evolution.init.EvolutionCapabilities;

import java.util.Collection;

public class CommandRegen implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandRegen();
    private static final EnumEvArgument<RegenType> TYPE = EnumEvArgument.enumArgument(RegenType.class);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("regen")
                                    .requires(cs -> cs.hasPermission(2))
                                    .executes(CMD)
                                    .then(Commands.argument("type", TYPE)
                                                  .executes(CMD)
                                                  .then(Commands.argument("target", EntityArgument.players()).executes(CMD))));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        Collection<? extends Entity> targets;
        try {
            targets = EntityArgument.getEntities(context, "target");
        }
        catch (Throwable t) {
            Entity source = context.getSource().getEntity();
            if (source != null) {
                targets = ImmutableList.of(source);
            }
            else {
                targets = ImmutableList.of();
            }
        }
        int count = 0;
        for (Entity target : targets) {
            if (target instanceof ServerPlayer player) {
                EvolutionCapabilities.revive(player);
                IThirst thirst = EvolutionCapabilities.getCapabilityOrThrow(player, CapabilityThirst.INSTANCE);
                IHunger hunger = EvolutionCapabilities.getCapabilityOrThrow(player, CapabilityHunger.INSTANCE);
                IStamina stamina = EvolutionCapabilities.getCapabilityOrThrow(player, CapabilityStamina.INSTANCE);
                RegenType type = RegenType.ALL;
                try {
                    type = context.getArgument("type", RegenType.class);
                }
                catch (Throwable ignored) {
                }
                switch (type) {
                    case ALL -> {
                        player.setHealth(player.getMaxHealth());
                        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
                        thirst.setHydrationLevel(0);
                        hunger.setHungerLevel(HungerStats.HUNGER_CAPACITY);
                        hunger.setSaturationLevel(0);
                    }
                    case HEALTH -> player.setHealth(player.getMaxHealth());
                    case THIRST -> {
                        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
                        thirst.setHydrationLevel(0);
                    }
                    case HUNGER -> {
                        hunger.setHungerLevel(HungerStats.HUNGER_CAPACITY);
                        hunger.setSaturationLevel(0);
                    }
                    case STAMINA -> stamina.setStamina(StaminaStats.MAX_STAMINA);
                }
                count++;
                EvolutionCapabilities.invalidate(player);
            }
        }
        return count;
    }

    public enum RegenType {
        ALL,
        HEALTH,
        THIRST,
        HUNGER,
        STAMINA
    }
}
