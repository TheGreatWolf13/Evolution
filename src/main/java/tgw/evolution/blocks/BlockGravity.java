package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.earth.Gravity;
import tgw.evolution.util.math.DirectionUtil;

import static tgw.evolution.init.EvolutionBStates.LAYERS_1_4;

public class BlockGravity {

//    public BlockGravity(Properties properties, int mass) {
//        super(properties, mass);
//    }

    public static boolean canFallThrough(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockPeat) {
            return state.getValue(LAYERS_1_4) != 4;
        }
        Material material = state.getMaterial();
        return state.isAir() || material.isLiquid() || material.isReplaceable() || block instanceof IReplaceable;
    }

    public boolean beamCondition(BlockState checkingState, BlockState state) {
        return true;
    }

    public Direction[] beamDirections(BlockState state) {
        return DirectionUtil.HORIZ_NESW;
    }

    public int beamSize() {
        return 0;
    }

    public SoundEvent breakSound() {
        return EvolutionSounds.STONE_BREAK.get();
    }

    public boolean canSlopeFail() {
        return false;
    }

    protected boolean canSustainWeight(BlockState state) {
        return this.getShearStrength() != 0;
    }

//    public final DirectionToIntMap checkBeams(Level level, BlockPos pos, boolean nested) {
//        DirectionToIntMap beams = new DirectionToIntMap();
//        MutableBlockPos mutablePos = new MutableBlockPos();
//        mutablePos.set(pos);
//        for (Direction direction : this.beamDirections(level.getBlockState(pos))) {
//            mutablePos.set(pos);
//            for (int i = 1; i <= this.beamSize(); i++) {
//                BlockState check = level.getBlockState(mutablePos.move(direction));
//                if (check.getBlock() == this) {
//                    if (this.isPillar(check, level, mutablePos, nested)) {
//                        beams.put(direction, i);
//                        break;
//                    }
//                    if (!this.beamCondition(check, level.getBlockState(pos))) {
//                        break;
//                    }
//                }
//                else if (this.supportCheck(check)) {
//                    beams.put(direction, i);
//                    break;
//                }
//                else {
//                    break;
//                }
//            }
//        }
//        return beams;
//    }

//    @Override
//    public final void checkPhysics(Level level, BlockPos pos) {
//        if (this.shouldFall(level, pos)) {
//            if (this.hasBeams()) {
//                DirectionToIntMap beams = this.checkBeams(level, pos, false);
//                if (beams.isEmpty()) {
//                    if (this.specialCondition(level, pos)) {
//                        return;
//                    }
//                    this.fall(level, pos);
//                    return;
//                }
//                if (BlockUtils.hasMass(level.getBlockState(pos.above()))) {
//                    if (!this.canSustainWeight(level.getBlockState(pos))) {
//                        this.fall(level, pos);
//                        return;
//                    }
//                    Axis beamAxis = BlockUtils.getSmallestBeam(beams);
//                    if (beamAxis == null) {
//                        if (this.specialCondition(level, pos)) {
//                            return;
//                        }
//                        this.fall(level, pos);
//                        return;
//                    }
//                    this.checkWeight(level, pos, beams, beamAxis, false);
//                    return;
//                }
//                return;
//            }
//            this.fall(level, pos);
//            return;
//        }
//        if (this.slopes()) {
//            if (this.preventSlope(level, pos)) {
//                BlockUtils.scheduleBlockTick(level, pos.below(), 2);
//                return;
//            }
//            if (this.RANDOM.nextFloat() < this.slopeChance()) {
//                DirectionList slopePossibility = new DirectionList();
//                MutableBlockPos mutablePos = new MutableBlockPos();
//                for (Direction slopeDirection : DirectionUtil.HORIZ_NESW) {
//                    mutablePos.set(pos);
//                    if (BlockUtils.isReplaceable(level.getBlockState(mutablePos.move(slopeDirection)))) {
//                        if (BlockUtils.isReplaceable(level.getBlockState(mutablePos.move(Direction.DOWN)))) {
//                            slopePossibility.add(slopeDirection);
//                        }
//                    }
//                }
//                while (!slopePossibility.isEmpty()) {
//                    Direction slopeDirection = slopePossibility.getRandomAndRemove(this.RANDOM);
//                    //noinspection ObjectAllocationInLoop
//                    if (level.getEntitiesOfClass(EntityFallingWeight.class, new AABB(pos.relative(slopeDirection))).isEmpty()) {
//                        this.slope(level, pos, slopeDirection);
//                        return;
//                    }
//                    if (this.canSlopeFail()) {
//                        return;
//                    }
//                }
//            }
//        }
//    }

//    public void checkWeight(Level level, BlockPos pos, DirectionToIntMap beams, Axis axis, boolean fallBelow) {
//        int positiveSide = beams.get(MathHelper.getPositiveAxis(axis));
//        int negativeSide = beams.get(MathHelper.getNegativeAxis(axis));
//        int beamSize = positiveSide + negativeSide;
//        int min = Math.min(positiveSide, negativeSide);
//        int supportedMass = (int) ((this.beamSize() - min) / (double) beamSize * this.shearStrength(level.dimensionType()) / 64);
//        MutableBlockPos mutablePos = new MutableBlockPos();
//        mutablePos.set(pos);
//        int mass = 0;
//        for (int i = pos.getY() + 1; i < 256; i++) {
//            BlockState stateUp = level.getBlockState(mutablePos.move(Direction.UP));
//            Block blockUp = stateUp.getBlock();
//            if (!(blockUp instanceof IPhysics)) {
//                break;
//            }
//            mass += ((IPhysics) blockUp).getMass(level, mutablePos, stateUp);
//            if (mass > supportedMass) {
//                level.playSound(null, mutablePos, this.breakSound(), SoundSource.BLOCKS, 2.0F, 1.0F);
//                if (fallBelow) {
//                    this.fall(level, pos.below());
//                    return;
//                }
//                this.fall(level, pos);
//                return;
//            }
//        }
//    }

    /**
     * Executes when the block is determined to fall.
     */
//    public void fall(Level level, BlockPos pos) {
//        EntityFallingWeight entity = new EntityFallingWeight(level,
//                                                             pos.getX() + 0.5,
//                                                             pos.getY(),
//                                                             pos.getZ() + 0.5,
//                                                             this.getStateForFalling(level.getBlockState(pos)));
//        level.removeBlock(pos, true);
//        entity.fallTime = 1;
//        level.addFreshEntity(entity);
//        entity.playSound(this.fallSound(), 0.25F, 1.0F);
//        for (Direction dir : DirectionUtil.ALL_EXCEPT_DOWN) {
//            BlockUtils.scheduleBlockTick(level, pos.relative(dir), 2);
//        }
//    }

    /**
     * The SoundEvent to play when the block falls.
     */
    public SoundEvent fallSound() {
        return EvolutionSounds.STONE_COLLAPSE.get();
    }

    public int getShearStrength() {
        return 0;
    }

    public BlockState getStateForFalling(BlockState state) {
        return state;
    }

    private boolean hasBeams() {
        return this.beamSize() > 0;
    }

    public boolean isPillar(BlockState state, Level level, BlockPos pos, boolean nested) {
        return !canFallThrough(level.getBlockState(pos.below()));
    }

//    @Override
//    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
//        BlockPos up = pos.above();
//        BlockUtils.scheduleBlockTick(level, up, 2);
//        if (this.slopes()) {
//            for (Direction dir : DirectionUtil.HORIZ_NESW) {
//                BlockUtils.scheduleBlockTick(level, up.relative(dir), 2);
//            }
//        }
//        else if (this.hasBeams()) {
//            for (Direction dir : DirectionUtil.HORIZ_NESW) {
//                BlockUtils.scheduleBlockTick(level, pos.relative(dir), 2);
//            }
//        }
//        super.playerWillDestroy(level, pos, state, player);
//    }

    public boolean preventSlope(Level level, BlockPos pos) {
        return false;
    }

    public final int shearStrength(DimensionType dim) {
        return (int) (this.getShearStrength() / (Gravity.gravity(dim) * 400));
    }

    public boolean shouldFall(BlockGetter level, BlockPos pos) {
        return canFallThrough(level.getBlockState(pos.below())) && pos.getY() >= 0;
    }

    /**
     * The chance for sloping.
     */
    public float slopeChance() {
        return 1;
    }

//    @Override
//    public boolean slopes() {
//        return false;
//    }

    /**
     * @param level The World
     * @param pos   The BlockPos where physics is being calculated.
     *              Used by stone blocks to sustain blocks under a beam.
     */
    public boolean specialCondition(Level level, BlockPos pos) {
        return false;
    }

    public boolean supportCheck(BlockState state) {
        return false;
    }

//    @Override
//    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
//        this.checkPhysics(level, pos);
//    }
}
