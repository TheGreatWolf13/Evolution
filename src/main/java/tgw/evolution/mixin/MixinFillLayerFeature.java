package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.FillLayerFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(FillLayerFeature.class)
public abstract class MixinFillLayerFeature extends Feature<LayerConfiguration> {

    public MixinFillLayerFeature(Codec<LayerConfiguration> codec) {
        super(codec);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean place(FeaturePlaceContext<LayerConfiguration> featurePlaceContext) {
        BlockPos origin = featurePlaceContext.origin();
        LayerConfiguration config = featurePlaceContext.config();
        WorldGenLevel level = featurePlaceContext.level();
        int y = level.getMinBuildHeight() + config.height;
        int x0 = origin.getX();
        int x1 = x0 + 16;
        int z0 = origin.getZ();
        int z1 = z0 + 16;
        for (int x = x0; x < x1; ++x) {
            for (int z = z0; z < z1; ++z) {
                if (level.getBlockState_(x, y, z).isAir()) {
                    level.setBlock_(x, y, z, config.state, BlockFlags.BLOCK_UPDATE);
                }
            }
        }
        return true;
    }
}
