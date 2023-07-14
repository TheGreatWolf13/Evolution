package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.SubShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SliceShape.class)
public abstract class MixinSliceShape {

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape shape, Direction.Axis axis, int index) {
        return switch (axis) {
            case X -> new SubShape(shape, index, 0, 0, index + 1, shape.getYSize(), shape.getZSize());
            case Y -> new SubShape(shape, 0, index, 0, shape.getXSize(), index + 1, shape.getZSize());
            case Z -> new SubShape(shape, 0, 0, index, shape.getXSize(), shape.getYSize(), index + 1);
        };
    }
}
