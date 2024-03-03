package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(BiomeFilter.class)
public abstract class MixinBiomeFilter extends PlacementFilter {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean shouldPlace(PlacementContext context, Random random, BlockPos pos) {
        //TODO BlockPos only used for storing parameters
        PlacedFeature placedFeature = context.topFeature().orElse(null);
        if (placedFeature == null) {
            throw new IllegalStateException("Tried to biome check an unregistered feature");
        }
        Biome biome = context.getLevel().getBiome_(pos).value();
        return biome.getGenerationSettings().hasFeature(placedFeature);
    }
}
