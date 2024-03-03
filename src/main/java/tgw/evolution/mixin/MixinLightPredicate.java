package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLocationPredicate;

@Mixin(LightPredicate.class)
public abstract class MixinLightPredicate implements PatchLocationPredicate {

    @Shadow @Final private MinMaxBounds.Ints composite;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean matches(ServerLevel level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.matches_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean matches_(ServerLevel level, int x, int y, int z) {
        if ((Object) this == LightPredicate.ANY) {
            return true;
        }
        if (!level.isLoaded_(x, y, z)) {
            return false;
        }
        return this.composite.matches(level.getMaxLocalRawBrightness_(x, y, z));
    }
}
