package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Direction.class)
public abstract class DirectionMixin {

    @Shadow
    @Final
    private static Direction[] VALUES;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    @Shadow
    @Final
    private int oppositeIndex;

    /**
     * @author JellySquid
     * <p>
     * Avoid the modulo/abs operations
     */
    @Overwrite
    public Direction getOpposite() {
        return VALUES[this.oppositeIndex];
    }

    /**
     * @author JellySquid
     * <p>
     * Avoid indirection to aid inlining
     */
    @Overwrite
    public int getStepX() {
        return this.offsetX;
    }

    /**
     * @author JellySquid
     * <p>
     * Avoid indirection to aid inlining
     */
    @Overwrite
    public int getStepY() {
        return this.offsetY;
    }

    /**
     * @author JellySquid
     * <p>
     * Avoid indirection to aid inlining
     */
    @Overwrite
    public int getStepZ() {
        return this.offsetZ;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(String enumName,
                        int ordinal,
                        int id,
                        int idOpposite,
                        int idHorizontal,
                        String name,
                        Direction.AxisDirection direction,
                        Direction.Axis axis,
                        Vec3i vector,
                        CallbackInfo ci) {
        this.offsetX = vector.getX();
        this.offsetY = vector.getY();
        this.offsetZ = vector.getZ();
    }
}
