package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.DirectionToIntMap;
import tgw.evolution.util.math.DirectionUtil;

public class BlockStone extends BlockGravity implements IRockVariant {

    private final RockVariant variant;

    public BlockStone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().getHardness() / 2.0F, 6.0F).sound(SoundType.STONE), variant.getMass());
        this.variant = variant;
    }

    @Override
    public int beamSize() {
        return this.variant.getRockType().getRangeStone();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (Direction dir : DirectionUtil.ALL) {
            mutablePos.setWithOffset(pos, dir);
            if (BlockUtils.hasSolidSide(level, mutablePos, dir.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return HarvestLevel.LOW_METAL;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength();
    }

    @Override
    public BlockState getStateForFalling(BlockState state) {
        return this.variant.getCobble().defaultBlockState();
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public boolean isPillar(BlockState state, Level level, BlockPos pos, boolean nested) {
        BlockPos down = pos.below();
        if (canFallThrough(level.getBlockState(down))) {
            return false;
        }
        if (canFallThrough(level.getBlockState(pos.below(2)))) {
            if (nested) {
                return false;
            }
            DirectionToIntMap map = this.checkBeams(level, down, true);
            if (map.isEmpty()) {
                return false;
            }
            Direction.Axis axis = BlockUtils.getSmallestBeam(map);
            if (axis == null) {
                return false;
            }
            this.checkWeight(level, down, map, axis, false);
        }
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                popResource(level, pos, new ItemStack(this));
                level.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public boolean specialCondition(Level level, BlockPos pos) {
        BlockPos up = pos.above();
        if (level.getBlockState(up).getBlock() == this) {
            DirectionToIntMap map = this.checkBeams(level, up, true);
            if (map.isEmpty()) {
                return false;
            }
            Direction.Axis axis = BlockUtils.getSmallestBeam(map);
            if (axis == null) {
                return false;
            }
            this.checkWeight(level, up, map, axis, true);
            return true;
        }
        return false;
    }
}
