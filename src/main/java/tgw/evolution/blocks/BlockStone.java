package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.DirectionUtil;

public class BlockStone extends BlockPhysics implements IRockVariant, IPoppable, IFallable {

    private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
    private final RockVariant variant;

    public BlockStone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().getHardness() / 2.0F, 6.0F).sound(SoundType.STONE));
        this.variant = variant;
    }

//    @Override
//    public int beamSize() {
//        return this.variant.getRockType().getRangeStone();
//    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = MUTABLE_POS.get();
        for (Direction dir : DirectionUtil.ALL) {
            mutablePos.setWithOffset(pos, dir);
            if (BlockUtils.hasSolidSide(level, mutablePos, dir.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.STONE_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8f;
    }

//    @Override
//    public int getShearStrength() {
//        return this.variant.getShearStrength();
//    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return HarvestLevel.LOW_METAL;
    }

//    @Override
//    public boolean isPillar(BlockState state, Level level, BlockPos pos, boolean nested) {
//        BlockPos down = pos.below();
//        if (canFallThrough(level.getBlockState(down))) {
//            return false;
//        }
//        if (canFallThrough(level.getBlockState(pos.below(2)))) {
//            if (nested) {
//                return false;
//            }
//            DirectionToIntMap map = this.checkBeams(level, down, true);
//            if (map.isEmpty()) {
//                return false;
//            }
//            Direction.Axis axis = BlockUtils.getSmallestBeam(map);
//            if (axis == null) {
//                return false;
//            }
//            this.checkWeight(level, down, map, axis, false);
//        }
//        return true;
//    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return this.rockVariant().getMass();
    }

    @Override
    public BlockState getStateForPhysicsChange(BlockState state) {
        return this.variant.getCobble().defaultBlockState();
    }

    @Override
    public void popDrops(BlockState state, Level level, BlockPos pos) {
        popResource(level, pos, new ItemStack(this));
    }

    //    @Override
//    public boolean specialCondition(Level level, BlockPos pos) {
//        BlockPos up = pos.above();
//        if (level.getBlockState(up).getBlock() == this) {
//            DirectionToIntMap map = this.checkBeams(level, up, true);
//            if (map.isEmpty()) {
//                return false;
//            }
//            Direction.Axis axis = BlockUtils.getSmallestBeam(map);
//            if (axis == null) {
//                return false;
//            }
//            this.checkWeight(level, up, map, axis, true);
//            return true;
//        }
//        return false;
//    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }
}
