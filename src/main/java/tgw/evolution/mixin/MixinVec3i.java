package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3i.class)
public abstract class MixinVec3i {

    @Shadow private int x;
    @Shadow private int y;
    @Shadow private int z;

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public int get(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.x;
            case Y -> this.y;
            case Z -> this.z;
        };
    }

    @Shadow
    public abstract int getX();

    @Shadow
    public abstract int getY();

    @Shadow
    public abstract int getZ();

    /**
     * @author TheGreatWolf
     * @reason Simplify
     */
    @Overwrite
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + "}";
    }
}
