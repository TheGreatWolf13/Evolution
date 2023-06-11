package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.SubShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SubShape.class)
public abstract class SubShapeMixin extends DiscreteVoxelShape {

    @Shadow
    @Final
    private int endX;
    @Shadow
    @Final
    private int endY;
    @Shadow
    @Final
    private int endZ;
    @Shadow
    @Final
    private int startX;
    @Shadow
    @Final
    private int startY;
    @Shadow
    @Final
    private int startZ;

    public SubShapeMixin(int pXSize, int pYSize, int pZSize) {
        super(pXSize, pYSize, pZSize);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    private int clampToShape(Direction.Axis axis, int value) {
        int start;
        int end;
        switch (axis) {
            case X -> {
                start = this.startX;
                end = this.endX;
            }
            case Y -> {
                start = this.startY;
                end = this.endY;
            }
            case Z -> {
                start = this.startZ;
                end = this.endZ;
            }
            default -> throw new IncompatibleClassChangeError();
        }
        return Mth.clamp(value, start, end) - start;
    }
}
