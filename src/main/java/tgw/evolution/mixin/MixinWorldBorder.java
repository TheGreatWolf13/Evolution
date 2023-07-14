package tgw.evolution.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchWorldBorder;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder implements PatchWorldBorder {

    @Shadow
    public abstract double getDistanceToBorder(Entity entity);

    @Overwrite
    public boolean isInsideCloseToBorder(Entity entity, AABB bb) {
        Evolution.deprecatedMethod();
        return this.isInsideCloseToBorder_(entity, bb.getXsize(), bb.getZsize());
    }

    @Override
    public boolean isInsideCloseToBorder_(Entity entity, double xSize, double zSize) {
        double maxSize = Math.max(Mth.absMax(xSize, zSize), 1);
        return this.getDistanceToBorder(entity) < maxSize * 2.0D && this.isWithinBounds(entity.getX(), entity.getZ(), maxSize);
    }

    @Shadow
    public abstract boolean isWithinBounds(double d, double e, double f);
}
