package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.util.DirectionToIntMap;
import tgw.evolution.util.HarvestLevel;
import tgw.evolution.util.RockVariant;

public class BlockStone extends BlockGravity implements IStoneVariant {

    private final RockVariant variant;

    public BlockStone(RockVariant variant) {
        super(Block.Properties.create(Material.ROCK)
                              .hardnessAndResistance(variant.getRockType().getHardness() / 2.0F, 6.0F)
                              .sound(SoundType.STONE)
                              .harvestLevel(HarvestLevel.COPPER), variant.getMass());
        this.variant = variant;
    }

    @Override
    public int beamSize() {
        return this.variant.getRockType().getRangeStone();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength();
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        return this.variant.getCobble().getDefaultState();
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public boolean isPillar(BlockState state, World world, BlockPos pos, boolean nested) {
        BlockPos down = pos.down();
        if (canFallThrough(world.getBlockState(down))) {
            return false;
        }
        if (canFallThrough(world.getBlockState(pos.down(2)))) {
            if (nested) {
                return false;
            }
            DirectionToIntMap map = this.checkBeams(world, down, true);
            if (map.isEmpty()) {
                return false;
            }
            Axis axis = BlockUtils.getSmallestBeam(map);
            if (axis == null) {
                return false;
            }
            this.checkWeight(world, down, map, axis, false);
        }
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            mutablePos.setPos(pos).move(dir);
            if (Block.hasSolidSide(world.getBlockState(mutablePos), world, mutablePos, dir.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                spawnAsEntity(world, pos, new ItemStack(this));
                world.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public boolean specialCondition(World world, BlockPos pos) {
        BlockPos up = pos.up();
        if (world.getBlockState(up).getBlock() == this) {
            DirectionToIntMap map = this.checkBeams(world, up, true);
            if (map.isEmpty()) {
                return false;
            }
            Axis axis = BlockUtils.getSmallestBeam(map);
            if (axis == null) {
                return false;
            }
            this.checkWeight(world, up, map, axis, true);
            return true;
        }
        return false;
    }
}
