package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IBiomeManagerPatch;
import tgw.evolution.patches.ILevelReaderPatch;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends ILevelReaderPatch {

    @Override
    default Holder<Biome> getBiome(int x, int y, int z) {
        return ((IBiomeManagerPatch) this.getBiomeManager()).getBiome(x, y, z);
    }

    @Shadow
    BiomeManager getBiomeManager();

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
