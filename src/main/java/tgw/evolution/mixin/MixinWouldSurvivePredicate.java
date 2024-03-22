package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.WouldSurvivePredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;

@Mixin(WouldSurvivePredicate.class)
public abstract class MixinWouldSurvivePredicate implements BlockPredicate {

    @Shadow @Final private Vec3i offset;
    @Shadow @Final private BlockState state;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean test(WorldGenLevel level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.test_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean test_(WorldGenLevel level, int x, int y, int z) {
        Vec3i offset = this.offset;
        return this.state.canSurvive_(level, x + offset.getX(), y + offset.getY(), z + offset.getZ());
    }
}
