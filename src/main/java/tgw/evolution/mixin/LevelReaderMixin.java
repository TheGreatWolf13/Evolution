package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelReader.class)
public interface LevelReaderMixin {

    @Shadow
    boolean hasChunksAt(int pFromX, int pFromY, int pFromZ, int pToX, int pToY, int pToZ);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    default boolean isAreaLoaded(BlockPos center, int range) {
        return this.hasChunksAt(center.getX() - range, center.getY() - range, center.getZ() - range, center.getX() + range, center.getY() + range,
                                center.getZ() + range);
    }
}
