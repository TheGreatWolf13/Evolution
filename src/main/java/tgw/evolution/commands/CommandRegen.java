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
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.capabilities.player.CapabilityStamina;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.commands.argument.EnumArgument;
import tgw.evolution.patches.PatchServerPlayer;

import java.util.Collection;

public class CommandRegen implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandRegen();
    private static final EnumArgument<RegenType> TYPE = EnumArgument.enumArgument(RegenType.class);

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
                CapabilityThirst thirst = ((PatchServerPlayer) player).getThirstStats();
                CapabilityHunger hunger = ((PatchServerPlayer) player).getHungerStats();
                CapabilityStamina stamina = ((PatchServerPlayer) player).getStaminaStats();
                RegenType type = RegenType.ALL;
                try {
                    type = context.getArgument("type", RegenType.class);
                }
                catch (Throwable ignored) {
                }
                switch (type) {
                    case ALL -> {
                        player.setHealth(player.getMaxHealth());
                        thirst.setThirstLevel(CapabilityThirst.THIRST_CAPACITY);
                        thirst.setHydrationLevel(0);
                        hunger.setHungerLevel(CapabilityHunger.HUNGER_CAPACITY);
                        hunger.setSaturationLevel(0);
                    }
                    case HEALTH -> player.setHealth(player.getMaxHealth());
                    case THIRST -> {
                        thirst.setThirstLevel(CapabilityThirst.THIRST_CAPACITY);
                        thirst.setHydrationLevel(0);
                    }
                    case HUNGER -> {
                        hunger.setHungerLevel(CapabilityHunger.HUNGER_CAPACITY);
                        hunger.setSaturationLevel(0);
                    }
                    case STAMINA -> stamina.setStamina(CapabilityStamina.MAX_STAMINA);
                }
                count++;
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
