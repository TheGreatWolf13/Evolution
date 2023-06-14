package tgw.evolution.mixin;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.EvLevelRenderer;

@Mixin(BlockInfo.class)
public abstract class BlockInfoMixin {

    @Shadow @Final private static Direction[] SIDES;
    @Shadow @Final private float[][][] ao;
    @Shadow @Final private int[][][] b;
    @Shadow @Final private float[][][][] blockLight;
    @Shadow private BlockPos blockPos;
    @Shadow private boolean full;
    @Shadow private BlockAndTintGetter level;
    @Shadow @Final private int[] packed;
    @Shadow @Final private int[][][] s;
    @Shadow @Final private float[][][][] skyLight;
    @Shadow private BlockState state;
    @Shadow @Final private boolean[][][] t;

    @Shadow
    protected abstract float combine(int c, int s1, int s2, int s3, boolean t0, boolean t1, boolean t2, boolean t3);

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer and avoid allocating 5 BlockPos
     */
    @Overwrite
    public void updateFlatLighting() {
        this.full = Block.isShapeFullBlock(this.state.getCollisionShape(this.level, this.blockPos));
        this.packed[0] = EvLevelRenderer.getLightColor(this.level, this.blockPos);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction side : SIDES) {
            int i = side.ordinal() + 1;
            this.packed[i] = EvLevelRenderer.getLightColor(this.level, mutablePos.set(this.blockPos).move(side));
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace LevelRenderer and avoid allocating 31 new BlockPos
     */
    @Overwrite
    public void updateLightMatrix() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = 0; x <= 2; ++x) {
            mutablePos.setX(this.blockPos.getX() + x - 1);
            for (int y = 0; y <= 2; ++y) {
                mutablePos.setY(this.blockPos.getY() + y - 1);
                for (int z = 0; z <= 2; ++z) {
                    BlockPos pos = mutablePos.setZ(this.blockPos.getZ() + z - 1);
                    BlockState state = this.level.getBlockState(pos);
                    this.t[x][y][z] = state.getLightBlock(this.level, pos) < 15;
                    int brightness = EvLevelRenderer.getLightColor(this.level, pos);
                    this.s[x][y][z] = LightTexture.sky(brightness);
                    this.b[x][y][z] = LightTexture.block(brightness);
                    this.ao[x][y][z] = state.getShadeBrightness(this.level, pos);
                }
            }
        }
        for (Direction side : SIDES) {
            BlockPos pos = mutablePos.set(this.blockPos).move(side);
            BlockState state = this.level.getBlockState(pos);
            BlockState thisStateShape = this.state.canOcclude() && this.state.useShapeForLightOcclusion() ?
                                        this.state :
                                        Blocks.AIR.defaultBlockState();
            BlockState otherStateShape = state.canOcclude() && state.useShapeForLightOcclusion() ? state : Blocks.AIR.defaultBlockState();
            if (state.getLightBlock(this.level, pos) == 15 ||
                Shapes.faceShapeOccludes(thisStateShape.getFaceOcclusionShape(this.level, this.blockPos, side),
                                         otherStateShape.getFaceOcclusionShape(this.level, pos, side.getOpposite()))) {
                int x = side.getStepX() + 1;
                int y = side.getStepY() + 1;
                int z = side.getStepZ() + 1;
                this.s[x][y][z] = Math.max(this.s[1][1][1] - 1, this.s[x][y][z]);
                this.b[x][y][z] = Math.max(this.b[1][1][1] - 1, this.b[x][y][z]);
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    int x1 = x * 2;
                    int y1 = y * 2;
                    int z1 = z * 2;
                    int sxyz = this.s[x1][y1][z1];
                    int bxyz = this.b[x1][y1][z1];
                    boolean txyz = this.t[x1][y1][z1];
                    int sxz = this.s[x1][1][z1];
                    int sxy = this.s[x1][y1][1];
                    int syz = this.s[1][y1][z1];
                    int bxz = this.b[x1][1][z1];
                    int bxy = this.b[x1][y1][1];
                    int byz = this.b[1][y1][z1];
                    boolean txz = this.t[x1][1][z1];
                    boolean txy = this.t[x1][y1][1];
                    boolean tyz = this.t[1][y1][z1];
                    int sx = this.s[x1][1][1];
                    int sy = this.s[1][y1][1];
                    int sz = this.s[1][1][z1];
                    int bx = this.b[x1][1][1];
                    int by = this.b[1][y1][1];
                    int bz = this.b[1][1][z1];
                    boolean tx = this.t[x1][1][1];
                    boolean ty = this.t[1][y1][1];
                    boolean tz = this.t[1][1][z1];
                    this.skyLight[0][x][y][z] = this.combine(sx, sxz, sxy, txz || txy ? sxyz : sx, tx, txz, txy, txz || txy ? txyz : tx);
                    this.blockLight[0][x][y][z] = this.combine(bx, bxz, bxy, txz || txy ? bxyz : bx, tx, txz, txy, txz || txy ? txyz : tx);
                    this.skyLight[1][x][y][z] = this.combine(sy, sxy, syz, txy || tyz ? sxyz : sy, ty, txy, tyz, txy || tyz ? txyz : ty);
                    this.blockLight[1][x][y][z] = this.combine(by, bxy, byz, txy || tyz ? bxyz : by, ty, txy, tyz, txy || tyz ? txyz : ty);
                    this.skyLight[2][x][y][z] = this.combine(sz, syz, sxz, tyz || txz ? sxyz : sz, tz, tyz, txz, tyz || txz ? txyz : tz);
                    this.blockLight[2][x][y][z] = this.combine(bz, byz, bxz, tyz || txz ? bxyz : bz, tz, tyz, txz, tyz || txz ? txyz : tz);
                }
            }
        }
    }
}
