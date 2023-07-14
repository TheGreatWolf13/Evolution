package tgw.evolution.mixin;

import net.minecraft.world.level.chunk.DataLayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DataLayer.class)
public abstract class MixinDataLayer {

    @Shadow protected byte @Nullable [] data;

    /**
     * @reason Avoid an additional branch.
     * @author JellySquid
     */
    @Overwrite
    private int get(int idx) {
        byte[] arr = this.data;
        if (arr == null) {
            return 0;
        }
        int byteIdx = idx >> 1;
        int shift = (idx & 1) << 2;
        return arr[byteIdx] >>> shift & 15;
    }

    /**
     * @reason Avoid an additional branch.
     * @author JellySquid
     */
    @Overwrite
    private void set(int idx, int value) {
        byte[] arr = this.data;
        if (arr == null) {
            this.data = arr = new byte[2_048];
        }
        int byteIdx = idx >> 1;
        int shift = (idx & 1) << 2;
        arr[byteIdx] = (byte) (arr[byteIdx] & ~(15 << shift) | (value & 15) << shift);
    }
}
