package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.capabilities.player.CapabilityTemperature;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.items.IItemTemperature;

public final class CommandTemperature implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandTemperature();
    private static final DoubleArgumentType KELVIN = DoubleArgumentType.doubleArg(0, 1_000_000_000);
    private static final DoubleArgumentType CELSIUS = DoubleArgumentType.doubleArg(-273, 1_000_000_000 - 273);

    private CommandTemperature() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("temperature")
                                    .requires(cs -> cs.getEntity() instanceof Player && cs.hasPermission(2))
                                    .then(Commands.literal("self")
                                                  .then(Commands.literal("current")
                                                                .then(Commands.argument("celsius", CELSIUS)
                                                                              .executes(CMD)
                                                                )
                                                  )
                                                  .then(Commands.literal("desired")
                                                                .then(Commands.argument("celsius", CELSIUS)
                                                                              .executes(CMD)
                                                                )
                                                  )
                                                  .then(Commands.literal("minComfort")
                                                                .then(Commands.argument("celsius", CELSIUS)
                                                                              .executes(CMD)
                                                                )
                                                  )
                                                  .then(Commands.literal("maxComfort")
                                                                .then(Commands.argument("celsius", CELSIUS)
                                                                              .executes(CMD)
                                                                )
                                                  )
                                    )
                                    .then(Commands.literal("item")
                                                  .then(Commands.argument("kelvin", KELVIN)
                                                                .executes(CMD)
                                                  )
                                    )
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = context.getInput();
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        if (input.contains("item")) {
            double temp = DoubleArgumentType.getDouble(context, "kelvin");
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IItemTemperature t) {
                t.setTemperature(stack, temp);
                source.sendSuccess(new TranslatableComponent("command.evolution.temperature.item.success", stack.getDisplayName(), temp), true);
                return SINGLE_SUCCESS;
            }
            source.sendFailure(EvolutionTexts.COMMAND_TEMPERATURE_ITEM_FAIL);
            return 0;
        }
        double temp = DoubleArgumentType.getDouble(context, "celsius");
        CapabilityTemperature temperature = player.getTemperatureStats();
        if (input.contains("current")) {
            temperature.setCurrentTemperature(temp);
            return SINGLE_SUCCESS;
        }
        if (input.contains("minComfort")) {
            temperature.setCurrentMinComfort(temp);
            return SINGLE_SUCCESS;
        }
        if (input.contains("maxComfort")) {
            temperature.setCurrentMaxComfort(temp);
        }
        temperature.setDesiredTemperature(temp);
        return SINGLE_SUCCESS;
    }
}
