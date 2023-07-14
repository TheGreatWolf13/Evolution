package tgw.evolution.mixin;

import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(WeightedRandom.class)
public abstract class MixinWeightedRandom {

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public static int getTotalWeight(List<? extends WeightedEntry> entries) {
        long count = 0L;
        for (int i = 0, l = entries.size(); i < l; i++) {
            count += entries.get(i).getWeight().asInt();
        }
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int) count;
    }
}
