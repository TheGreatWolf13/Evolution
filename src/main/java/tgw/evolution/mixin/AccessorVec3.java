package tgw.evolution.mixin;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3.class)
public interface AccessorVec3 {

    @Mutable
    @Accessor(value = "x")
    void setX(double x);

    @Mutable
    @Accessor(value = "y")
    void setY(double y);

    @Mutable
    @Accessor(value = "z")
    void setZ(double z);
}
