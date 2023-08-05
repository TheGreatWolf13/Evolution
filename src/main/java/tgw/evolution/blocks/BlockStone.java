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
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockStone extends BlockPhysics implements IRockVariant, IPoppable, IFallable {

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
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        if (BlockUtils.hasSolidFace(level, x + 1, y, z, Direction.WEST)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x - 1, y, z, Direction.EAST)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x, y + 1, z, Direction.DOWN)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x, y, z + 1, Direction.NORTH)) {
            return true;
        }
        return BlockUtils.hasSolidFace(level, x, y, z - 1, Direction.SOUTH);
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.STONE_COLLAPSE;
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
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
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
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        return this.rockVariant().getMass();
    }

    @Override
    public BlockState getStateForPhysicsChange(BlockState state) {
        return this.variant.get(EvolutionBlocks.COBBLESTONES).defaultBlockState();
    }

    @Override
    public void popDrops(BlockState state, Level level, int x, int y, int z) {
        popResource(level, new BlockPos(x, y, z), new ItemStack(this));
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
