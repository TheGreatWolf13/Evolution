package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPos.MutableBlockPos.class)
public abstract class MixinBlockPos_MutableBlockPos extends BlockPos {

    public MixinBlockPos_MutableBlockPos(Position pPos) {
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

    @Shadow
    public abstract MutableBlockPos set(int pX, int pY, int pZ);

    /**
     * @author TheGreatWolf
     * @reason Simplify and inline.
     */
    @Overwrite
    public BlockPos.MutableBlockPos setWithOffset(Vec3i pos, Direction dir) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (dir) {
            case WEST -> --x;
            case EAST -> ++x;
            case DOWN -> --y;
            case UP -> ++y;
            case NORTH -> --z;
            case SOUTH -> ++z;
        }
        return this.set(x, y, z);
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
