package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.server.command.EnumArgument;
import tgw.evolution.util.Date;
import tgw.evolution.util.FullDate;
import tgw.evolution.util.Hour;
import tgw.evolution.util.Time;

public class CommandDate implements Command<CommandSource> {

    private static final Command<CommandSource> CMD = new CommandDate();
    private static final IntegerArgumentType DAY = IntegerArgumentType.integer(1, Time.DAYS_IN_A_MONTH);
    private static final EnumArgument<Date.Month> MONTH = EnumArgument.enumArgument(Date.Month.class);
    private static final IntegerArgumentType YEAR = IntegerArgumentType.integer(1_000);
    private static final IntegerArgumentType HOUR = IntegerArgumentType.integer(0, 23);
    private static final IntegerArgumentType MINUTE = IntegerArgumentType.integer(0, 59);

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("date")
                                    .requires(cs -> cs.hasPermissionLevel(3))
                                    .then(Commands.argument("day", DAY)
                                                  .then(Commands.argument("month", MONTH)
                                                                .then(Commands.argument("year", YEAR)
                                                                              .executes(CMD)
                                                                              .then(Commands.argument("hour", HOUR)
                                                                                            .then(Commands.argument("minute", MINUTE).executes(CMD))))

                                                  )));
    }

    @Override
    public int run(CommandContext<CommandSource> context) {
        int day = IntegerArgumentType.getInteger(context, "day");
        Date.Month month = context.getArgument("month", Date.Month.class);
        int year = IntegerArgumentType.getInteger(context, "year");
        int hour = 6;
        int minute = 0;
        try {
            hour = IntegerArgumentType.getInteger(context, "hour");
            minute = IntegerArgumentType.getInteger(context, "minute");
        }
        catch (IllegalArgumentException ignored) {
        }
        try {
            Date date = new Date(year, month, day);
            Hour time = new Hour(hour, minute);
            FullDate fullDate = new FullDate(date, time);
            long ticks = fullDate.toTicks();
            for (ServerWorld serverWorld : context.getSource().getServer().getWorlds()) {
                serverWorld.setDayTime(ticks);
            }
            context.getSource().sendFeedback(new TranslationTextComponent("command.evolution.date.success", fullDate.getFullString()), true);
            return SINGLE_SUCCESS;
        }
        catch (IllegalStateException e) {
            context.getSource()
                   .sendErrorMessage(new TranslationTextComponent("command.evolution.date.error",
                                                                  Date.STARTING_DATE.getFullString(),
                                                                  Hour.START_TIME));
            return 0;
        }
    }
}
