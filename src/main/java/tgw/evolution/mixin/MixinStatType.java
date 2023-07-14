package tgw.evolution.mixin;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(StatType.class)
public abstract class MixinStatType<T> {

    @Shadow @Final private Map<T, Stat<T>> map;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public Stat<T> get(T value, StatFormatter formatter) {
        Stat<T> stat = this.map.get(value);
        if (stat == null) {
            stat = new Stat<>((StatType<T>) (Object) this, value, formatter);
            this.map.put(value, stat);
        }
        return stat;
    }
}
