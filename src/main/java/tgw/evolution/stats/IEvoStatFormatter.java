package tgw.evolution.stats;

import net.minecraft.stats.IStatFormatter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IEvoStatFormatter extends IStatFormatter {

    @OnlyIn(Dist.CLIENT)
    String format(long value);
}
