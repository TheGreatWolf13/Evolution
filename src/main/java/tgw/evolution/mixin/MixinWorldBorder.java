package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
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

    @Shadow
    public abstract double getMaxX();

    @Shadow
    public abstract double getMaxZ();

    @Shadow
    public abstract double getMinX();

    @Shadow
    public abstract double getMinZ();

    /**
     * @reason _
     * @author TheGreatWolf
     */
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isWithinBounds(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.isWithinBounds_(pos.getX(), pos.getZ());
    }

    @Shadow
    public abstract boolean isWithinBounds(double d, double e, double f);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean isWithinBounds(AABB bb) {
        Evolution.deprecatedMethod();
        return this.isWithinBounds_(bb.minX, bb.minZ, bb.maxX, bb.maxZ);
    }

    @Override
    public boolean isWithinBounds_(int x, int z) {
        return x + 1 > this.getMinX() && x < this.getMaxX() && z + 1 > this.getMinZ() && z < this.getMaxZ();
    }

    @Override
    public boolean isWithinBounds_(double minX, double minZ, double maxX, double maxZ) {
        return maxX > this.getMinX() && minX < this.getMaxX() && maxZ > this.getMinZ() && minZ < this.getMaxZ();
    }
}
