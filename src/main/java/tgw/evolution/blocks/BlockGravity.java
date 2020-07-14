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
import tgw.evolution.entities.EntityFallingWeight;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.DirectionList;
import tgw.evolution.util.DirectionToIntMap;
import tgw.evolution.util.Gravity;
import tgw.evolution.util.MathHelper;

import java.util.Random;

public class BlockGravity extends BlockMass {

    public BlockGravity(Properties properties, int mass) {
        super(properties, mass);
    }

    public static boolean canFallThrough(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockPeat) {
            return state.get(BlockPeat.LAYERS) != 4;
        }
        Material material = state.getMaterial();
        return state.isAir() || material.isLiquid() || material.isReplaceable() || block instanceof IReplaceable;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos up = pos.up();
        BlockUtils.scheduleBlockTick(worldIn, up, 2);
        if (this.canSlope()) {
            for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
                BlockUtils.scheduleBlockTick(worldIn, up.offset(dir), 2);
            }
        }
        else if (this.hasBeams()) {
            for (Direction dir : MathHelper.DIRECTIONS_HORIZONTAL) {
                BlockUtils.scheduleBlockTick(worldIn, pos.offset(dir), 2);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public boolean shouldFall(World worldIn, BlockPos pos) {
        return canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0 && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32));
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            this.checkPhysics(worldIn, pos);
        }
    }

    @Override
    public final int tickRate(IWorldReader worldIn) {
        return 2;
    }

    protected final void checkPhysics(World worldIn, BlockPos pos) {
        if (this.shouldFall(worldIn, pos)) {
            if (this.hasBeams()) {
                DirectionToIntMap map = this.checkBeams(worldIn, pos, false);
                if (map.isEmpty()) {
                    if (this.specialCondition(worldIn, pos)) {
                        return;
                    }
                    this.fall(worldIn, pos);
                    return;
                }
                if (BlockUtils.hasMass(worldIn.getBlockState(pos.up()))) {
                    if (!this.canSustainWeight(worldIn.getBlockState(pos))) {
                        this.fall(worldIn, pos);
                        return;
                    }
                    Axis axis = BlockUtils.getSmallestBeam(map);
                    if (axis == null) {
                        if (this.specialCondition(worldIn, pos)) {
                            return;
                        }
                        this.fall(worldIn, pos);
                        return;
                    }
                    this.checkWeight(worldIn, pos, map, axis, false);
                    return;
                }
                return;
            }
            this.fall(worldIn, pos);
            return;
        }
        if (this.canSlope()) {
            if (this.preventSlope(worldIn, pos)) {
                BlockUtils.scheduleBlockTick(worldIn, pos.down(), 2);
                return;
            }
            if (this.RANDOM.nextFloat() < this.slopeChance()) {
                DirectionList slopePossibility = new DirectionList();
                BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                for (Direction slopeDirection : Direction.Plane.HORIZONTAL) {
                    mutablePos.setPos(pos);
                    if (BlockUtils.isReplaceable(worldIn.getBlockState(mutablePos.move(slopeDirection)))) {
                        if (BlockUtils.isReplaceable(worldIn.getBlockState(mutablePos.move(Direction.DOWN)))) {
                            slopePossibility.add(slopeDirection);
                        }
                    }
                }
                while (true) {
                    if (slopePossibility.isEmpty()) {
                        return;
                    }
                    Direction slopeDirection = slopePossibility.getRandomAndRemove(this.RANDOM);
                    //noinspection ObjectAllocationInLoop
                    if (worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)) && worldIn.getEntitiesWithinAABB(EntityFallingWeight.class, new AxisAlignedBB(pos.offset(slopeDirection))).isEmpty()) {
                        this.slope(worldIn, pos, slopeDirection);
                        return;
                    }
                    if (this.canSlopeFail()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * @param worldIn : The World
     * @param pos     : The BlockPos where the slope is being calculated
     */
    public boolean preventSlope(World worldIn, BlockPos pos) {
        return false;
    }

    /**
     * @param worldIn : The World
     * @param pos     : The BlockPos where physics is being calculated.
     *                Used by stone blocks to sustain blocks under a beam.
     */
    public boolean specialCondition(World worldIn, BlockPos pos) {
        return false;
    }

    public void checkWeight(World worldIn, BlockPos pos, DirectionToIntMap map, Axis axis, boolean fallBelow) {
        int beamSize = map.get(MathHelper.getPositiveAxis(axis)) + map.get(MathHelper.getNegativeAxis(axis));
        int min = Math.min(map.get(MathHelper.getPositiveAxis(axis)), map.get(MathHelper.getNegativeAxis(axis)));
        int weight = (int) ((this.beamSize() - min) / (double) beamSize * this.shearStrength(worldIn.getDimension()) / 64);
        int mass = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int i = pos.getY() + 1; i < 256; i++) {
            BlockState up = worldIn.getBlockState(mutablePos.move(Direction.UP));
            if (!(up.getBlock() instanceof BlockMass)) {
                break;
            }
            mass += ((BlockMass) up.getBlock()).getMass(up);
            if (mass > weight) {
                worldIn.playSound(null, mutablePos, this.breakSound(), SoundCategory.BLOCKS, 2.0F, 1.0F);
                if (fallBelow) {
                    this.fall(worldIn, pos.down());
                    return;
                }
                this.fall(worldIn, pos);
                return;
            }
        }
    }

    /**
     * Executes when the block is determined to fall.
     */
    public void fall(World worldIn, BlockPos pos) {
        EntityFallingWeight entity = new EntityFallingWeight(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, this.getStateForFalling(worldIn.getBlockState(pos)));
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 67);
        entity.fallTime = 1;
        worldIn.addEntity(entity);
        entity.playSound(this.fallSound(), 0.25F, 1.0F);
        for (Direction dir : MathHelper.DIRECTIONS_EXCEPT_DOWN) {
            BlockUtils.scheduleBlockTick(worldIn, pos.offset(dir), 2);
        }
    }

    public void slope(World worldIn, BlockPos pos, Direction offset) {
        EntityFallingWeight entity = new EntityFallingWeight(worldIn, pos.getX() + offset.getXOffset() + 0.5D, pos.getY(), pos.getZ() + offset.getZOffset() + 0.5D, this.getStateForFalling(worldIn.getBlockState(pos)));
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 67);
        entity.fallTime = 1;
        worldIn.addEntity(entity);
        entity.playSound(this.fallSound(), 0.125F, 1.0F);
        BlockUtils.scheduleBlockTick(worldIn, pos.down(), 2);
        BlockPos up = pos.up();
        BlockUtils.scheduleBlockTick(worldIn, up, 2);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockUtils.scheduleBlockTick(worldIn, up.offset(dir), 2);
        }
    }

    public BlockState getStateForFalling(BlockState state) {
        return state;
    }

    /**
     * The SoundEvent to play when the block falls.
     */
    public SoundEvent fallSound() {
        return EvolutionSounds.STONE_COLLAPSE.get();
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

    public int beamSize() {
        return 0;
    }

    /**
     * The chance for sloping.
     */
    public float slopeChance() {
        return 1;
    }

    protected final int shearStrength(Dimension dim) {
        return (int) (this.getShearStrength() / (Gravity.gravity(dim) * 400));
    }

    private boolean hasBeams() {
        return this.beamSize() > 0;
    }

    public int getShearStrength() {
        return 0;
    }

    public final DirectionToIntMap checkBeams(World worldIn, BlockPos pos, boolean nested) {
        DirectionToIntMap map = new DirectionToIntMap();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (Direction direction : this.beamDirections(worldIn.getBlockState(pos))) {
            mutablePos.setPos(pos);
            for (int i = 1; i <= this.beamSize(); i++) {
                BlockState check = worldIn.getBlockState(mutablePos.move(direction));
                if (check.getBlock() == this) {
                    if (this.isPillar(check, worldIn, mutablePos, nested)) {
                        map.put(direction, i);
                        break;
                    }
                    if (!this.beamCondition(check, worldIn.getBlockState(pos))) {
                        break;
                    }
                }
                else if (this.supportCheck(check)) {
                    map.put(direction, i);
                    break;
                }
                else {
                    break;
                }
            }
        }
        return map;
    }

    public boolean supportCheck(BlockState state) {
        return false;
    }

    public boolean beamCondition(BlockState checkingState, BlockState state) {
        return true;
    }

    /**
     * @param state : The current BlockState of the Block
     */
    public Direction[] beamDirections(BlockState state) {
        return MathHelper.DIRECTIONS_HORIZONTAL;
    }

    public boolean isPillar(BlockState state, World worldIn, BlockPos pos, boolean nested) {
        return !canFallThrough(worldIn.getBlockState(pos.down()));
    }

    public boolean canSlope() {
        return false;
    }
}
