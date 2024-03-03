package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.blocks.util.BlockUtils;

@Mixin(LocationPredicate.class)
public abstract class MixinLocationPredicate {

    @Shadow @Final private @Nullable ResourceKey<Biome> biome;
    @Shadow @Final private BlockPredicate block;
    @Shadow @Final private @Nullable ResourceKey<Level> dimension;
    @Shadow @Final private @Nullable ResourceKey<ConfiguredStructureFeature<?, ?>> feature;
    @Shadow @Final private FluidPredicate fluid;
    @Shadow @Final private LightPredicate light;
    @Shadow @Final private @Nullable Boolean smokey;
    @Shadow @Final private MinMaxBounds.Doubles x;
    @Shadow @Final private MinMaxBounds.Doubles y;
    @Shadow @Final private MinMaxBounds.Doubles z;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean matches(ServerLevel level, double x, double y, double z) {
        if (!this.x.matches(x)) {
            return false;
        }
        if (!this.y.matches(y)) {
            return false;
        }
        if (!this.z.matches(z)) {
            return false;
        }
        if (this.dimension != null && this.dimension != level.dimension()) {
            return false;
        }
        int px = Mth.floor(x);
        int py = Mth.floor(y);
        int pz = Mth.floor(z);
        boolean loaded = level.isLoaded_(px, py, pz);
        if (this.biome != null && (!loaded || !level.getBiome_(px, py, pz).is(this.biome))) {
            return false;
        }
        if (this.feature != null && (!loaded || !level.structureFeatureManager().getStructureWithPieceAt(new BlockPos(x, y, z), this.feature).isValid())) {
            return false;
        }
        if (this.smokey != null && (!loaded || this.smokey != BlockUtils.isSmokeyPos(level, px, py, pz))) {
            return false;
        }
        if (!this.light.matches_(level, px, py, pz)) {
            return false;
        }
        if (!this.block.matches_(level, px, py, pz)) {
            return false;
        }
        return this.fluid.matches_(level, px, py, pz);
    }
}
