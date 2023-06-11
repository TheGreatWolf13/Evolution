package tgw.evolution.util.math;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.mixin.ClipContextAccessor;
import tgw.evolution.world.util.MutableCollisionContext;

public class ClipContextMutable extends ClipContext {

    public ClipContextMutable() {
        super(new Vec3d(), new Vec3d(), Block.COLLIDER, Fluid.NONE, null);
    }

    public void reset() {
        ((MutableCollisionContext) ((ClipContextAccessor) this).getCollisionContext()).reset();
    }

    public ClipContextMutable set(double fromX,
                                  double fromY,
                                  double fromZ,
                                  double toX,
                                  double toY,
                                  double toZ,
                                  Block block,
                                  Fluid fluid,
                                  @Nullable Entity entity) {
        ((Vec3d) this.getFrom()).set(fromX, fromY, fromZ);
        ((Vec3d) this.getTo()).set(toX, toY, toZ);
        ((ClipContextAccessor) this).setBlock(block);
        ((ClipContextAccessor) this).setFluid(fluid);
        ((MutableCollisionContext) ((ClipContextAccessor) this).getCollisionContext()).set(entity);
        return this;
    }

    public ClipContextMutable set(Vec3 from, Vec3 to, Block block, Fluid fluid, @Nullable Entity entity) {
        return this.set(from.x, from.y, from.z, to.x, to.y, to.z, block, fluid, entity);
    }
}
