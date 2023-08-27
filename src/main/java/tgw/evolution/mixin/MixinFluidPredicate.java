package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLocationPredicate;

@Mixin(FluidPredicate.class)
public abstract class MixinFluidPredicate implements PatchLocationPredicate {

    @Shadow @Final private @Nullable Fluid fluid;
    @Shadow @Final private StatePropertiesPredicate properties;
    @Shadow @Final private @Nullable TagKey<Fluid> tag;

    @Overwrite
    public boolean matches(ServerLevel level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.matches_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean matches_(ServerLevel level, int x, int y, int z) {
        if ((Object) this == FluidPredicate.ANY) {
            return true;
        }
        if (!level.isLoaded_(x, y, z)) {
            return false;
        }
        FluidState state = level.getFluidState_(x, y, z);
        if (this.tag != null && !state.is(this.tag)) {
            return false;
        }
        if (this.fluid != null && !state.is(this.fluid)) {
            return false;
        }
        return this.properties.matches(state);
    }
}
