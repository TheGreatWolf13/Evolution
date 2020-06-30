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
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;
import tgw.evolution.util.HarvestLevel;

public class BlockStone extends BlockGravity implements IStoneVariant {

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockStone(EnumRockNames name) {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(name.getRockType().getHardness() / 2F, 6F).sound(SoundType.STONE).harvestLevel(HarvestLevel.COPPER), name.getMass());
        this.name = name;
    }

    @Override
    public EnumRockVariant getVariant() {
        return this.variant;
    }

    @Override
    public void setVariant(EnumRockVariant variant) {
        this.variant = variant;
    }

    @Override
    public EnumRockNames getStoneName() {
        return this.name;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            mutablePos.setPos(pos).move(dir);
            if (Block.hasSolidSide(worldIn.getBlockState(mutablePos), worldIn, mutablePos, dir.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnAsEntity(worldIn, pos, new ItemStack(this));
                worldIn.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        return this.variant.getCobble().getDefaultState();
    }

    @Override
    public int beamSize() {
        return this.name.getRockType().getRangeStone();
    }

    @Override
    public int getShearStrength() {
        return this.name.getShearStrength();
    }

    @Override
    public boolean isPillar(BlockState state, World worldIn, BlockPos pos, boolean nested) {
        BlockPos down = pos.down();
        if (canFallThrough(worldIn.getBlockState(down))) {
            return false;
        }
        if (canFallThrough(worldIn.getBlockState(pos.down(2)))) {
            if (nested) {
                return false;
            }
            DirectionToIntMap map = this.checkBeams(worldIn, down, true);
            if (map.isEmpty()) {
                return false;
            }
            Axis axis = BlockUtils.getSmallestBeam(map);
            if (axis == null) {
                return false;
            }
            this.checkWeight(worldIn, down, map, axis, false);
        }
        return true;
    }

    @Override
    public boolean specialCondition(World worldIn, BlockPos pos) {
        BlockPos up = pos.up();
        if (worldIn.getBlockState(up).getBlock() == this) {
            DirectionToIntMap map = this.checkBeams(worldIn, up, true);
            if (map.isEmpty()) {
                return false;
            }
            Axis axis = BlockUtils.getSmallestBeam(map);
            if (axis == null) {
                return false;
            }
            this.checkWeight(worldIn, up, map, axis, true);
            return true;
        }
        return false;
    }
}
