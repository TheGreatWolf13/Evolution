package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlockHitResult;

@Mixin(BlockHitResult.class)
public abstract class Mixin_CF_BlockHitResult extends HitResult implements PatchBlockHitResult {

    @Shadow @Final @DeleteField private BlockPos blockPos;
    @Mutable @Shadow @Final @RestoreFinal private Direction direction;
    @Mutable @Shadow @Final @RestoreFinal private boolean inside;
    @Mutable @Shadow @Final @RestoreFinal private boolean miss;
    @Unique private int posX;
    @Unique private int posY;
    @Unique private int posZ;

    @ModifyConstructor
    public Mixin_CF_BlockHitResult(boolean miss, Vec3 v, Direction direction, BlockPos pos, boolean inside) {
        super(v);
        this.miss = miss;
        this.direction = direction;
        this.inside = inside;
    }

    @ModifyConstructor
    public Mixin_CF_BlockHitResult(Vec3 v, Direction direction, BlockPos pos, boolean inside) {
        this(false, Vec3.ZERO, direction, BlockPos.ZERO, inside);
        Evolution.deprecatedConstructor();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static BlockHitResult miss(Vec3 v, Direction direction, BlockPos pos) {
        Evolution.deprecatedMethod();
        return PatchBlockHitResult.createMiss(v.x, v.y, v.z, direction, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BlockPos getBlockPos() {
        Evolution.warn("getBlockPos() should not be called!");
        return BlockPos.ZERO;
    }

    @Override
    public int posX() {
        return this.posX;
    }

    @Override
    public int posY() {
        return this.posY;
    }

    @Override
    public int posZ() {
        return this.posZ;
    }

    @Override
    public void setPos(int x, int y, int z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BlockHitResult withDirection(Direction direction) {
        BlockHitResult hitResult = new BlockHitResult(this.miss, Vec3.ZERO, direction, BlockPos.ZERO, this.inside);
        hitResult.set(this.x(), this.y(), this.z());
        hitResult.setPos(this.posX, this.posY, this.posZ);
        return hitResult;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BlockHitResult withPosition(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.withPosition_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockHitResult withPosition_(int x, int y, int z) {
        BlockHitResult hitResult = new BlockHitResult(this.miss, Vec3.ZERO, this.direction, BlockPos.ZERO, this.inside);
        hitResult.set(this.x(), this.y(), this.z());
        hitResult.setPos(x, y, z);
        return hitResult;
    }
}
