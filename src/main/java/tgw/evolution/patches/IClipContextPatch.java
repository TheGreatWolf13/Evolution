package tgw.evolution.patches;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface IClipContextPatch {

    void setBlock(ClipContext.Block block);

    void setEntity(@Nullable Entity entity);

    void setFluid(ClipContext.Fluid fluid);

    void setFrom(Vec3 from);

    void setTo(Vec3 to);
}
