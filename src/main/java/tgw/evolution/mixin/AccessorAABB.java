package tgw.evolution.mixin;

import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AABB.class)
public interface AccessorAABB {

    @Mutable
    @Accessor
    void setMaxX(double maxX);

    @Mutable
    @Accessor
    void setMaxY(double maxY);

    @Mutable
    @Accessor
    void setMaxZ(double maxZ);

    @Mutable
    @Accessor
    void setMinX(double minX);

    @Mutable
    @Accessor
    void setMinY(double minY);

    @Mutable
    @Accessor
    void setMinZ(double minZ);
}
