package tgw.evolution.patches.obj;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.util.math.Vec3d;

public class NearPlane {

    private final Vec3d bottomLeft = new Vec3d();
    private final Vec3d bottomRight = new Vec3d();
    private final Vec3d forward = new Vec3d();
    private final Vec3d left = new Vec3d();
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private final Vec3d topLeft = new Vec3d();
    private final Vec3d topRight = new Vec3d();
    private final Vec3d up = new Vec3d();

    public FogType getFogType(Vec3 cameraPos, BlockGetter level) {
        for (int i = 0; i < 5; i++) {
            Vec3d vec = this.getVector(i);
            double x = cameraPos.x + vec.getX();
            double y = cameraPos.y + vec.getY();
            double z = cameraPos.z + vec.getZ();
            this.pos.set(x, y, z);
            FluidState fluidState = level.getFluidState(this.pos);
            if (fluidState.is(FluidTags.LAVA)) {
                if (y <= fluidState.getHeight(level, this.pos) + this.pos.getY()) {
                    return FogType.LAVA;
                }
            }
            else {
                BlockState state = level.getBlockState(this.pos);
                if (state.is(Blocks.POWDER_SNOW)) {
                    return FogType.POWDER_SNOW;
                }
            }
        }
        return FogType.NONE;
    }

    private Vec3d getVector(int index) {
        return switch (index) {
            case 0 -> this.forward;
            case 1 -> this.topLeft;
            case 2 -> this.topRight;
            case 3 -> this.bottomLeft;
            case 4 -> this.bottomRight;
            default -> throw new IllegalStateException("Invalid index for vector: " + index);
        };
    }

    public void setup(Vector3f forward, double forwardScale, Vector3f left, double leftScale, Vector3f up, double upScale) {
        this.forward.set(forward).scale(forwardScale);
        this.left.set(left).scale(leftScale);
        this.up.set(up).scale(upScale);
        this.topLeft.set(this.forward).add(this.up).add(this.left);
        this.topRight.set(this.forward).add(this.up).sub(this.left);
        this.bottomLeft.set(this.forward).sub(this.up).add(this.left);
        this.bottomRight.set(this.forward).sub(this.up).sub(this.left);
    }
}
