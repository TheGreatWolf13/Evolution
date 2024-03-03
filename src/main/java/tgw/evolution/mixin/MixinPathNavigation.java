package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchPathNavigation;

@Mixin(PathNavigation.class)
public abstract class MixinPathNavigation implements PatchPathNavigation {

    @Shadow protected boolean hasDelayedRecomputation;
    @Shadow @Final protected Mob mob;
    @Shadow protected @Nullable Path path;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean shouldRecomputePath(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.shouldRecomputePath_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean shouldRecomputePath_(int x, int y, int z) {
        if (this.hasDelayedRecomputation) {
            return false;
        }
        if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            Node node = this.path.getEndNode();
            assert node != null;
            double dx = x + 0.5 - (node.x + this.mob.getX()) / 2;
            double dy = y + 0.5 - (node.y + this.mob.getY()) / 2;
            double dz = z + 0.5 - (node.z + this.mob.getZ()) / 2;
            int dist = this.path.getNodeCount() - this.path.getNextNodeIndex();
            return dx * dx + dy * dy + dz * dz < dist * dist;
        }
        return false;
    }
}
