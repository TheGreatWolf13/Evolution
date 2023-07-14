package tgw.evolution.patches.obj;

import com.mojang.math.Vector3f;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
    private final Vec3d topLeft = new Vec3d();
    private final Vec3d topRight = new Vec3d();
    private final Vec3d up = new Vec3d();

    public FogType getFogType(Vec3 cameraPos, BlockGetter level) {
        for (int i = 0; i < 5; i++) {
            Vec3d vec = this.getVector(i);
            int x = Mth.floor(cameraPos.x + vec.x());
            double height = cameraPos.y + vec.y();
            int y = Mth.floor(height);
            int z = Mth.floor(cameraPos.z + vec.z());
            FluidState fluidState = level.getFluidState_(x, y, z);
            if (fluidState.is(FluidTags.LAVA)) {
                if (height <= fluidState.getHeight_(level, x, y, z) + y) {
                    return FogType.LAVA;
                }
            }
            else {
                BlockState state = level.getBlockState_(x, y, z);
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
        this.forward.set(forward).scaleMutable(forwardScale);
        this.left.set(left).scaleMutable(leftScale);
        this.up.set(up).scaleMutable(upScale);
        this.topLeft.set(this.forward).addMutable(this.up).addMutable(this.left);
        this.topRight.set(this.forward).addMutable(this.up).subMutable(this.left);
        this.bottomLeft.set(this.forward).subMutable(this.up).addMutable(this.left);
        this.bottomRight.set(this.forward).subMutable(this.up).subMutable(this.left);
    }
}
