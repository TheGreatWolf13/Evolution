package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchBoundingBox;

@Mixin(BoundingBox.class)
public abstract class MixinBoundingBox implements PatchBoundingBox {

    @Shadow private int maxX;
    @Shadow private int maxY;
    @Shadow private int maxZ;
    @Shadow private int minX;
    @Shadow private int minY;
    @Shadow private int minZ;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Deprecated
    @Overwrite
    public BoundingBox encapsulate(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.encapsulate_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BoundingBox encapsulate_(int x, int y, int z) {
        this.minX = Math.min(this.minX, x);
        this.minY = Math.min(this.minY, y);
        this.minZ = Math.min(this.minZ, z);
        this.maxX = Math.max(this.maxX, x);
        this.maxY = Math.max(this.maxY, y);
        this.maxZ = Math.max(this.maxZ, z);
        return (BoundingBox) (Object) this;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean isInside(Vec3i vec) {
        Evolution.deprecatedMethod();
        return this.isInside_(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public boolean isInside_(int x, int y, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
    }
}
