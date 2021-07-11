package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.*;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.LAYERS_1_4;

public class BlockGravity extends BlockMass {

    public BlockGravity(Properties properties, int mass) {
        super(properties, mass);
    }

    public static boolean canFallThrough(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockPeat) {
            return state.get(LAYERS_1_4) != 4;
        }
        Material material = state.getMaterial();
        return state.isAir() || material.isLiquid() || material.isReplaceable() || block instanceof IReplaceable;
    }

    public boolean beamCondition(BlockState checkingState, BlockState state) {
        return true;
    }

    public Direction[] beamDirections(BlockState state) {
        return MathHelper.DIRECTIONS_HORIZONTAL;
    }

    public int beamSize() {
        return 0;
    }

    public SoundEvent breakSound() {
        return EvolutionSounds.STONE_BREAK.get();
    }

    public boolean canSlope() {
        return false;
    }

    public boolean canSlopeFail() {
        return false;
    }

    protected boolean canSustainWeight(BlockState state) {
        return this.getShearStrength() != 0;
    }

    public final DirectionToIntMap checkBeams(World world, BlockPos pos, boolean nested) {
        DirectionToIntMap beams = new DirectionToIntMap();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (Direction direction : this.beamDirections(world.getBlockState(pos))) {
            mutablePos.setPos(pos);
            for (int i = 1; i <= this.beamSize(); i++) {
                BlockState check = world.getBlockState(mutablePos.move(direction));
                if (check.getBlock() == this) {
                    if (this.isPillar(check, world, mutablePos, nested)) {
                        beams.put(direction, i);
                        break;
                    }
                    if (!this.beamCondition(check, world.getBlockState(pos))) {
                        break;
                    }
                }
                else if (this.supportCheck(check)) {
                    beams.put(direction, i);
                    break;
                }
                else {
                    break;
                }
            }
        }
        return beams;
    }

    public final void checkPhysics(World world, BlockPos pos) {
        if (this.shouldFall(world, pos)) {
            if (this.hasBeams()) {
                DirectionToIntMap beams = this.checkBeams(world, pos, false);
                if (beams.isEmpty()) {
                    if (this.specialCondition(world, pos)) {
                        return;
                    }
                    this.fall(world, pos);
                    return;
                }
                if (BlockUtils.hasMass(world.getBlockState(pos.up()))) {
                    if (!this.canSustainWeight(world.getBlockState(pos))) {
                        this.fall(world, pos);
                        return;
                    }
                    Axis beamAxis = BlockUtils.getSmallestBeam(beams);
                    if (beamAxis == null) {
                        if (this.specialCondition(world, pos)) {
                            return;
                        }
                        this.fall(world, pos);
                        return;
                    }
                    this.checkWeight(world, pos, beams, beamAxis, false);
                    return;
                }
                return;
            }
            this.fall(world, pos);
            return;
        }
        if (this.canSlope()) {
            if (this.preventSlope(world, pos)) {
                BlockUtils.scheduleBlockTick(world, pos.down(), 2);
                return;
            }
            if (this.RANDOM.nextFloat() < this.slopeChance()) {
                DirectionList slopePossibility = new DirectionList();
                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                for (Direction slopeDirection : MathHelper.DIRECTIONS_HORIZONTAL) {
                    mutablePos.setPos(pos);
                    if (BlockUtils.isReplaceable(world.getBlockState(mutablePos.move(slopeDirection)))) {
                        if (BlockUtils.isReplaceable(world.getBlockState(mutablePos.move(Direction.DOWN)))) {
                            slopePossibility.add(slopeDirection);
                        }
                    }
                }
                while (!slopePossibility.isEmpty()) {
                    Direction slopeDirection = slopePossibility.getRandomAndRemove(this.RANDOM);
                    //noinspection ObjectAllocationInLoop
                    if (world.getEntitiesWithinAABB(EntityFallingWeight.class, new AxisAlignedBB(pos.offset(slopeDirection))).isEmpty()) {
                        this.slope(world, pos, slopeDirection);
                        return;
                    }
                    if (this.canSlopeFail()) {
                        return;
                    }
                }
            }
        }
    }

    public void checkWeight(World world, BlockPos pos, DirectionToIntMap beams, Axis axis, boolean fallBelow) {
        int positiveSide = beams.get(MathHelper.getPositiveAxis(axis));
        int negativeSide = beams.get(MathHelper.getNegativeAxis(axis));
        int beamSize = positiveSide + negativeSide;
        int min = Math.min(positiveSide, negativeSide);
        int supportedMass = (int) ((this.beamSize() - min) / (double) beamSize * this.shearStrength(world.getDimension()) / 64);
        int mass = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int i = pos.getY() + 1; i < 256; i++) {
            BlockState stateUp = world.getBlockState(mutablePos.move(Direction.UP));
            Block blockUp = stateUp.getBlock();
            if (!(blockUp instanceof BlockMass)) {
                break;
            }
            mass += ((BlockMass) blockUp).getMass(world, mutablePos, stateUp);
            if (mass > supportedMass) {
                world.playSound(null, mutablePos, this.breakSound(), SoundCategory.BLOCKS, 2.0F, 1.0F);
                if (fallBelow) {
                    this.fall(world, pos.down());
                    return;
                }
                this.fall(world, pos);
                return;
            }
        }
    }

    /**
     * Executes when the block is determined to fall.
     */
    public void fall(World world, BlockPos pos) {
        EntityFallingWeight entity = new EntityFallingWeight(world,
                                                             pos.getX() + 0.5,
                                                             pos.getY(),
                                                             pos.getZ() + 0.5,
                                                             this.getStateForFalling(world.getBlockState(pos)));
        world.removeBlock(pos, true);
        entity.fallTime = 1;
        world.addEntity(entity);
        entity.playSound(this.fallSound(), 0.25F, 1.0F);
        for (Direction dir : MathHelper.DIRECTIONS_EXCEPT_DOWN) {
            BlockUtils.scheduleBlockTick(world, pos.offset(dir), 2);
        }
    }

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

    public boolean isPillar(BlockState state, World world, BlockPos pos, boolean nested) {
        return !canFallThrough(world.getBlockState(pos.down()));
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos up = pos.up();
        BlockUtils.scheduleBlockTick(world, up, 2);
        if (this.canSlope()) {
            for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
                BlockUtils.scheduleBlockTick(world, up.offset(dir), 2);
            }
        }
        else if (this.hasBeams()) {
            for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
                BlockUtils.scheduleBlockTick(world, pos.offset(dir), 2);
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    public boolean preventSlope(World world, BlockPos pos) {
        return false;
    }

    public final int shearStrength(Dimension dim) {
        return (int) (this.getShearStrength() / (Gravity.gravity(dim) * 400));
    }

    public boolean shouldFall(World world, BlockPos pos) {
        return canFallThrough(world.getBlockState(pos.down())) && pos.getY() >= 0;
    }

    public void slope(World world, BlockPos pos, Direction offset) {
        EntityFallingWeight entity = new EntityFallingWeight(world,
                                                             pos.getX() + offset.getXOffset() + 0.5D,
                                                             pos.getY(),
                                                             pos.getZ() + offset.getZOffset() + 0.5D,
                                                             this.getStateForFalling(world.getBlockState(pos)));
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), BlockFlags.IS_MOVING + BlockFlags.NOTIFY_AND_UPDATE);
        entity.fallTime = 1;
        world.addEntity(entity);
        entity.playSound(this.fallSound(), 0.125F, 1.0F);
        BlockUtils.scheduleBlockTick(world, pos.down(), 2);
        BlockPos up = pos.up();
        BlockUtils.scheduleBlockTick(world, up, 2);
        for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
            BlockUtils.scheduleBlockTick(world, up.offset(dir), 2);
        }
    }

    /**
     * The chance for sloping.
     */
    public float slopeChance() {
        return 1;
    }

    /**
     * @param world : The World
     * @param pos   : The BlockPos where physics is being calculated.
     *              Used by stone blocks to sustain blocks under a beam.
     */
    public boolean specialCondition(World world, BlockPos pos) {
        return false;
    }

    public boolean supportCheck(BlockState state) {
        return false;
    }

    @Override
    public void tick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.isRemote) {
            this.checkPhysics(world, pos);
        }
    }

    @Override
    public final int tickRate(IWorldReader world) {
        return 2;
    }
}
