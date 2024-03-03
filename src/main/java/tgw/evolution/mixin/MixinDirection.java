package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Direction.class)
public abstract class MixinDirection {

    @Shadow @Final private static Direction[] VALUES;
    @Unique private int offsetX;
    @Unique private int offsetY;
    @Unique private int offsetZ;
    @Shadow @Final private int oppositeIndex;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static Direction getRandom(Random random) {
        return VALUES[random.nextInt(6)];
    }

    /**
     * @author JellySquid
     * @reason Avoid the modulo/abs operations
     */
    @Overwrite
    public Direction getOpposite() {
        return VALUES[this.oppositeIndex];
    }

    /**
     * @author JellySquid
     * @reason Avoid indirection to aid inlining
     */
    @Overwrite
    public int getStepX() {
        return this.offsetX;
    }

    /**
     * @author JellySquid
     * @reason Avoid indirection to aid inlining
     */
    @Overwrite
    public int getStepY() {
        return this.offsetY;
    }

    /**
     * @author JellySquid
     * @reason Avoid indirection to aid inlining
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
