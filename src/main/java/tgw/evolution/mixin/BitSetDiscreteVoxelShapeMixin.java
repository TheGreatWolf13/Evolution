package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BitSetDiscreteVoxelShape.class)
public abstract class BitSetDiscreteVoxelShapeMixin extends DiscreteVoxelShape {

    @Shadow
    private int xMax;
    @Shadow
    private int xMin;
    @Shadow
    private int yMax;
    @Shadow
    private int yMin;
    @Shadow
    private int zMax;
    @Shadow
    private int zMin;

    public BitSetDiscreteVoxelShapeMixin(int pXSize, int pYSize, int pZSize) {
        super(pXSize, pYSize, pZSize);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Override
    @Overwrite
    public int firstFull(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.xMin;
            case Y -> this.yMin;
            case Z -> this.zMin;
        };
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Override
    @Overwrite
    public int lastFull(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.xMax;
            case Y -> this.yMax;
            case Z -> this.zMax;
        };
    }
}
