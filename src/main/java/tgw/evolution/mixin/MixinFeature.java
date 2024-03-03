package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.Random;

@Mixin(Feature.class)
public abstract class MixinFeature<FC extends FeatureConfiguration> {

    @Shadow
    public abstract boolean place(FeaturePlaceContext<FC> featurePlaceContext);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean place(FC config, WorldGenLevel level, ChunkGenerator chunkGenerator, Random random, BlockPos origin) {
        return level.ensureCanWrite_(origin.getX(), origin.getY(), origin.getZ()) &&
               this.place(new FeaturePlaceContext<>(Optional.empty(), level, chunkGenerator, random, origin, config));
    }
}
