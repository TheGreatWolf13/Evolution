package tgw.evolution.mixin;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IVec3Patch;

@Mixin(Vec3.class)
public abstract class Vec3Mixin implements IVec3Patch {

    @Mutable
    @Shadow
    @Final
    public double x;

    @Mutable
    @Shadow
    @Final
    public double y;

    @Mutable
    @Shadow
    @Final
    public double z;

    @Override
    public void setPosX(double x) {
        this.x = x;
    }

    @Override
    public void setPosY(double y) {
        this.y = y;
    }

    @Override
    public void setPosZ(double z) {
        this.z = z;
    }
}
