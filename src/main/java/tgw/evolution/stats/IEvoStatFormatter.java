package tgw.evolution.stats;

import net.minecraft.stats.StatFormatter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IEvoStatFormatter extends StatFormatter {

    @OnlyIn(Dist.CLIENT)
    String format(long value);
}
