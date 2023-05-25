package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPos.MutableBlockPos.class)
public abstract class BlockPos_MutableBlockPosMixin extends BlockPos {

    public BlockPos_MutableBlockPosMixin(Position pPos) {
        super(pPos);
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public BlockPos.MutableBlockPos move(Direction dir) {
        return switch (dir) {
            case UP -> this.setY(this.getY() + 1);
            case DOWN -> this.setY(this.getY() - 1);
            case NORTH -> this.setZ(this.getZ() - 1);
            case SOUTH -> this.setZ(this.getZ() + 1);
            case EAST -> this.setX(this.getX() + 1);
            case WEST -> this.setX(this.getX() - 1);
        };
    }

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public BlockPos.MutableBlockPos move(Direction dir, int n) {
        return switch (dir) {
            case UP -> this.setY(this.getY() + n);
            case DOWN -> this.setY(this.getY() - n);
            case NORTH -> this.setZ(this.getZ() - n);
            case SOUTH -> this.setZ(this.getZ() + n);
            case EAST -> this.setX(this.getX() + n);
            case WEST -> this.setX(this.getX() - n);
        };
    }

    @Override
    @Shadow
    public abstract MutableBlockPos setX(int pX);

    @Override
    @Shadow
    public abstract MutableBlockPos setY(int pY);

    @Override
    @Shadow
    public abstract MutableBlockPos setZ(int pZ);
}
