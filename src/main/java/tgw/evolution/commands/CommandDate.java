package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import tgw.evolution.commands.argument.EnumArgument;
import tgw.evolution.util.time.Date;
import tgw.evolution.util.time.FullDate;
import tgw.evolution.util.time.Time;

public final class CommandDate {

    private static final IntegerArgumentType DAY = IntegerArgumentType.integer(1, Time.DAYS_PER_MONTH);
    private static final IntegerArgumentType HOUR = IntegerArgumentType.integer(0, 23);
    private static final IntegerArgumentType MINUTE = IntegerArgumentType.integer(0, 59);
    private static final EnumArgument<Date.Month> MONTH = EnumArgument.enumArgument(Date.Month.class);
    private static final IntegerArgumentType YEAR = IntegerArgumentType.integer(Time.STARTING_YEAR);

    private CommandDate() {
    }

    private static int addDate(CommandContext<CommandSourceStack> context, int type) {
        long increment = IntegerArgumentType.getInteger(context, "increment");
        switch (type) {
            case 0 -> increment = (int) (increment * Time.TICKS_PER_HOUR / 60.0);
            case 1 -> increment *= Time.TICKS_PER_HOUR;
            case 2 -> increment *= Time.TICKS_PER_DAY;
            case 3 -> increment *= Time.TICKS_PER_MONTH;
            default -> increment *= Time.TICKS_PER_YEAR;
        }
        CommandSourceStack source = context.getSource();
        long daytime = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            daytime = level.getDayTime();
            break;
        }
        daytime += increment;
        if (daytime < 0) {
            source.sendFailure(new TranslatableComponent("command.evolution.date.error", Date.STARTING_DATE.getDisplayName(), String.valueOf(Time.START_TIME)));
            return 0;
        }
        for (ServerLevel level : source.getServer().getAllLevels()) {
            level.setDayTime(daytime);
        }
        FullDate fullDate = new FullDate(daytime);
        source.sendSuccess(new TranslatableComponent("command.evolution.date.success", fullDate.getDisplayName()), true);
        return 1;
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
                                                                                            .executes(c -> set(c.getSource(),
                                                                                                               IntegerArgumentType.getInteger(c, "day"),
                                                                                                               c.getArgument("month", Date.Month.class),
                                                                                                               IntegerArgumentType.getInteger(c, "year"),
                                                                                                               6,
                                                                                                               0)
                                                                                            )
                                                                                            .then(Commands.argument("hour", HOUR)
                                                                                                          .then(Commands.argument("minute", MINUTE)
                                                                                                                        .executes(c -> set(c.getSource(),
                                                                                                                                           IntegerArgumentType.getInteger(c, "day"),
                                                                                                                                           c.getArgument("month", Date.Month.class),
                                                                                                                                           IntegerArgumentType.getInteger(c, "year"),
                                                                                                                                           IntegerArgumentType.getInteger(c, "hour"),
                                                                                                                                           IntegerArgumentType.getInteger(c, "minute"))
                                                                                                                        )
                                                                                                          )
                                                                                            )
                                                                              )
                                                                )
                                                  )
                                    )
        );
    }

    private static int set(CommandSourceStack source, int day, Date.Month month, int year, int hour, int minute) {
        try {
            Date date = new Date(year, month, day);
            Time time = new Time(hour, minute);
            FullDate fullDate = new FullDate(date, time);
            long ticks = fullDate.toTicks();
            for (ServerLevel level : source.getServer().getAllLevels()) {
                level.setDayTime(ticks);
            }
            source.sendSuccess(new TranslatableComponent("command.evolution.date.success", fullDate.getDisplayName()), true);
            return 1;
        }
        catch (IllegalStateException e) {
            source.sendFailure(new TranslatableComponent("command.evolution.date.error", Date.STARTING_DATE.getDisplayName(), String.valueOf(Time.START_TIME)));
            return 0;
        }
    }
}
