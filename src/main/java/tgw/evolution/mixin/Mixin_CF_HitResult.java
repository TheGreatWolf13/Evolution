package tgw.evolution.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchHitResult;

@Mixin(HitResult.class)
public abstract class Mixin_CF_HitResult implements PatchHitResult {

    @Shadow @Final @DeleteField protected Vec3 location;
    @Unique private double x;
    @Unique private double y;
    @Unique private double z;

    @ModifyConstructor
    protected Mixin_CF_HitResult(Vec3 v) {
        this.x = 0;
    }

    @Overwrite
    public double distanceTo(Entity entity) {
        Vec3 position = entity.position();
        double dx = this.x - position.x;
        double dy = this.y - position.y;
        double dz = this.z - position.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Overwrite
    public Vec3 getLocation() {
        Evolution.warn("getLocation() should not be called!");
        return Vec3.ZERO;
    }

    @Override
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    @Override
    public double z() {
        return this.z;
    }
}
