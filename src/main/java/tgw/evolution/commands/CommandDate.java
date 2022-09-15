package tgw.evolution.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import tgw.evolution.commands.argument.EnumEvArgument;
import tgw.evolution.util.time.Date;
import tgw.evolution.util.time.FullDate;
import tgw.evolution.util.time.Time;

public final class CommandDate implements Command<CommandSourceStack> {

    private static final Command<CommandSourceStack> CMD = new CommandDate();
    private static final IntegerArgumentType DAY = IntegerArgumentType.integer(1, Time.DAYS_PER_MONTH);
    private static final EnumEvArgument<Date.Month> MONTH = EnumEvArgument.enumArgument(Date.Month.class);
    private static final IntegerArgumentType YEAR = IntegerArgumentType.integer(1_000);
    private static final IntegerArgumentType HOUR = IntegerArgumentType.integer(0, 23);
    private static final IntegerArgumentType MINUTE = IntegerArgumentType.integer(0, 59);

    private CommandDate() {
    }

    public static int addDate(CommandContext<CommandSourceStack> context, int type) {
        int increment = IntegerArgumentType.getInteger(context, "increment");
        switch (type) {
            case 0 -> increment = (int) (increment * Time.TICKS_PER_HOUR / 60.0);
            case 1 -> increment *= Time.TICKS_PER_HOUR;
            case 2 -> increment *= Time.TICKS_PER_DAY;
            case 3 -> increment *= Time.TICKS_PER_MONTH;
            default -> increment *= Time.TICKS_PER_YEAR;
        }
        long daytime = 0;
        for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
            daytime = level.getDayTime();
            break;
        }
        daytime += increment;
        if (daytime < 0) {
            context.getSource()
                   .sendFailure(new TranslatableComponent("command.evolution.date.error", Date.STARTING_DATE.getDisplayName(), Time.START_TIME));
            return 0;
        }
        for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
            level.setDayTime(daytime);
        }
        FullDate fullDate = new FullDate(daytime);
        context.getSource().sendSuccess(new TranslatableComponent("command.evolution.date.success", fullDate.getDisplayName()), true);
        return SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("date")
                                    .requires(cs -> cs.hasPermission(3))
                                    .then(Commands.literal("add")
                                                  .then(Commands.argument("increment", IntegerArgumentType.integer())
                                                                .then(Commands.literal("minutes").executes(c -> addDate(c, 0)))
                                                                .then(Commands.literal("hours").executes(c -> addDate(c, 1)))
                                                                .then(Commands.literal("days").executes(c -> addDate(c, 2)))
                                                                .then(Commands.literal("months").executes(c -> addDate(c, 3)))
                                                                .then(Commands.literal("years").executes(c -> addDate(c, 4)))))
                                    .then(Commands.literal("set")
                                                  .then(Commands.argument("day", DAY)
                                                                .then(Commands.argument("month", MONTH)
                                                                              .then(Commands.argument("year", YEAR)
                                                                                            .executes(CMD)
                                                                                            .then(Commands.argument("hour", HOUR)
                                                                                                          .then(Commands.argument("minute", MINUTE)
                                                                                                                        .executes(CMD))))))));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
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
            Time time = new Time(hour, minute);
            FullDate fullDate = new FullDate(date, time);
            long ticks = fullDate.toTicks();
            for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
                level.setDayTime(ticks);
            }
            context.getSource().sendSuccess(new TranslatableComponent("command.evolution.date.success", fullDate.getDisplayName()), true);
            return SINGLE_SUCCESS;
        }
        catch (IllegalStateException e) {
            context.getSource()
                   .sendFailure(new TranslatableComponent("command.evolution.date.error", Date.STARTING_DATE.getDisplayName(), Time.START_TIME));
            return 0;
        }
    }
}
