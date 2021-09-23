package tgw.evolution.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.command.EnumArgument;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.capabilities.thirst.ThirstStats;
import tgw.evolution.util.RegenType;

import java.util.Collection;

public class CommandRegen implements Command<CommandSource> {

    private static final Command<CommandSource> CMD = new CommandRegen();
    private static final EnumArgument<RegenType> TYPE = EnumArgument.enumArgument(RegenType.class);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("regen")
                                    .requires(cs -> cs.hasPermission(2))
                                    .executes(CMD)
                                    .then(Commands.argument("type", TYPE)
                                                  .executes(CMD)
                                                  .then(Commands.argument("target", EntityArgument.players()).executes(CMD))));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
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
            if (target instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) target;
                IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
                RegenType type = RegenType.ALL;
                try {
                    type = context.getArgument("type", RegenType.class);
                }
                catch (Throwable ignored) {
                }
                switch (type) {
                    case ALL:
                        player.setHealth(player.getMaxHealth());
                        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
                        thirst.setHydrationLevel(0);
                        break;
                    case HEALTH:
                        player.setHealth(player.getMaxHealth());
                        break;
                    case THIRST:
                        thirst.setThirstLevel(ThirstStats.THIRST_CAPACITY);
                        thirst.setHydrationLevel(0);
                        break;
                }
                count++;
            }
        }
        return count;
    }
}
