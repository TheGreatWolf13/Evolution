package tgw.evolution.mixin;

import net.minecraft.core.IdMap;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CrudeIncrementalIntIdentityHashBiMap.class)
public abstract class MixinCrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {

    @Shadow private K[] byId;
    @Shadow private int size;

    @Override
    public long beginIteration() {
        K[] byId = this.byId;
        for (int i = 0, len = byId.length; i < len; i++) {
            if (byId[i] != null) {
                return (long) i << 32 | this.size;
            }
        }
        return 0;
    }

    @Override
    public K getIteration(long it) {
        return this.byId[(int) (it >> 32)];
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it > 0;
    }

    @Override
    public long nextEntry(long it) {
        int size = (int) it;
        if (--size == 0) {
            return 0;
        }
        int pos = (int) (it >> 32) + 1;
        K[] byId = this.byId;
        for (int i = pos, len = byId.length; i < len; ++i) {
            if (byId[i] != null) {
                return (long) i << 32 | size;
            }
        }
        throw new IllegalStateException("Should never reach here");
    }
}
