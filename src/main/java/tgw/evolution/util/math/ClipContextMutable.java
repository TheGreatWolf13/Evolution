package tgw.evolution.util.math;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.patches.IClipContextPatch;

public class ClipContextMutable extends ClipContext {

    public ClipContextMutable(Vec3 from,
                              Vec3 to,
                              Block block,
                              Fluid fluid, @Nullable Entity entity) {
        super(from, to, block, fluid, entity);
    }

    public void set(Vec3 from, Vec3 to, Block block, Fluid fluid, @Nullable Entity entity) {
        ((IClipContextPatch) this).setFrom(from);
        ((IClipContextPatch) this).setTo(to);
        ((IClipContextPatch) this).setBlock(block);
        ((IClipContextPatch) this).setFluid(fluid);
        ((IClipContextPatch) this).setEntity(entity);
    }
}
