package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.capabilities.player.CapabilityStamina;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.commands.argument.EnumArgument;

import java.util.Collection;

public final class CommandRegen {

    private static final EnumArgument<RegenType> TYPE = EnumArgument.enumArgument(RegenType.class);

    private CommandRegen() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("regen")
                                    .requires(cs -> cs.hasPermission(2))
                                    .executes(c -> {
                                        return run(c.getSource().getEntity(),
                                                   RegenType.ALL);
                                    })
                                    .then(Commands.argument("type", TYPE)
                                                  .executes(c -> {
                                                      return run(c.getSource().getEntity(),
                                                                 c.getArgument("type", RegenType.class));
                                                  })
                                                  .then(Commands.argument("target", EntityArgument.players())
                                                                .executes(c -> {
                                                                    return run(c.getArgument("type", RegenType.class),
                                                                               EntityArgument.getPlayers(c, "target"));
                                                                }))));
    }

    private static int run(@Nullable Entity entity, RegenType type) {
        if (entity instanceof ServerPlayer player) {
            CapabilityThirst thirst = player.getThirstStats();
            CapabilityHunger hunger = player.getHungerStats();
            CapabilityStamina stamina = player.getStaminaStats();
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
            return 1;
        }
        return 0;
    }

    private static int run(RegenType type, Collection<? extends Entity> targets) {
        int count = 0;
        for (Entity target : targets) {
            if (run(target, type) != 0) {
                ++count;
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
