package tgw.evolution.stats;

import net.minecraft.stats.StatFormatter;

public interface IEvoStatFormatter extends StatFormatter {

    String format(long value);
}
