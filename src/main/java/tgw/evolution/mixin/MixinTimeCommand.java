package tgw.evolution.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.time.Time;

@Mixin(TimeCommand.class)
public abstract class MixinTimeCommand {

    @Shadow
    public static int addTime(CommandSourceStack pSource, int pAmount) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Sync to Evolution Time
     */
    @Overwrite
    private static int getDayTime(ServerLevel pLevel) {
        return (int) (pLevel.getDayTime() % Time.TICKS_PER_DAY);
    }

    @Shadow
    private static int queryTime(CommandSourceStack pSource, int pTime) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Sync to Evolution Time
     */
    @Overwrite
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("time")
                                    .requires(cmd -> cmd.hasPermission(2))
                                    .then(Commands.literal("set")
                                                  .then(Commands.literal("day").executes(c -> setTime(c.getSource(), Time.TICKS_PER_HOUR)))
                                                  .then(Commands.literal("noon").executes(c -> setTime(c.getSource(), 6 * Time.TICKS_PER_HOUR)))
                                                  .then(Commands.literal("night").executes(c -> setTime(c.getSource(), 13 * Time.TICKS_PER_HOUR)))
                                                  .then(Commands.literal("midnight").executes(c -> setTime(c.getSource(), 18 * Time.TICKS_PER_HOUR)))
                                                  .then(Commands.argument("time", TimeArgument.time())
                                                                .executes(c -> setTime(c.getSource(), IntegerArgumentType.getInteger(c, "time")))))
                                    .then(Commands.literal("add").then(Commands.argument("time", TimeArgument.time()).executes(
                                            c -> addTime(c.getSource(), IntegerArgumentType.getInteger(c, "time")))))
                                    .then(Commands.literal("query")
                                                  .then(Commands.literal("daytime").executes(
                                                          c -> queryTime(c.getSource(), getDayTime(c.getSource().getLevel()))))
                                                  .then(Commands.literal("gametime").executes(
                                                          c -> queryTime(c.getSource(),
                                                                         (int) (c.getSource().getLevel().getGameTime() % Integer.MAX_VALUE))))
                                                  .then(Commands.literal("day").executes(
                                                          c -> queryTime(c.getSource(),
                                                                         (int) (c.getSource().getLevel().getDayTime() / Time.TICKS_PER_DAY %
                                                                                Integer.MAX_VALUE))))));
    }

    @Shadow
    public static int setTime(CommandSourceStack pSource, int pTime) {
        throw new AbstractMethodError();
    }
}
