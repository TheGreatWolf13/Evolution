package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.init.EvolutionHitBoxes;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.STAGE_0_1;

public class BlockSapling extends BlockBush {

    private final AbstractTreeGrower tree;

    public BlockSapling(AbstractTreeGrower tree) {
        super(Properties.of(Material.GRASS).noCollission().randomTicks().strength(0.0F, 0.0F).sound(SoundType.GRASS));
        this.tree = tree;
        this.registerDefaultState(this.defaultBlockState().setValue(STAGE_0_1, 0));
    }

//    public static boolean canGrowInto(IWorldGenerationReader worldIn, BlockPos pos) {
//        BlockState state = ((IBlockReader) worldIn).getBlockState(pos); //TODO proper vine
//        return state.isAir((IBlockReader) worldIn, pos) ||
//               state.getBlock() instanceof BlockLeaves ||
//               state.getBlock() instanceof BlockGrass ||
//               state.getBlock() instanceof BlockDirt ||
//               state.getBlock() instanceof BlockLog ||
//               state.getBlock() instanceof BlockSapling ||
//               state.getBlock() == Blocks.VINE ||
//               state.getBlock() instanceof BlockDryGrass;
//    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE_0_1);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EvolutionHitBoxes.SAPLING;
    }

//    @Override
//    public boolean isBonemealSuccess(Level level, Random rand, BlockPos pos, BlockState state) {
//        return true;
//    }

//    @Override
//    public boolean isValidBonemealTarget(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
//        return true;
//    }

//    @Override
//    public void performBonemeal(ServerWorld world, Random rand, BlockPos pos, BlockState state) {
//        this.placeTree(world, pos, state, rand);
//    }

//    public void placeTree(ServerWorld world, BlockPos pos, BlockState state, Random rand) {
//        if (state.getValue(STAGE_0_1) == 0) {
//            world.setBlock(pos, state.cycle(STAGE_0_1), BlockFlags.NO_RERENDER);
//        }
//        else {
//            if (!ForgeEventFactory.saplingGrowTree(world, rand, pos)) {
//                return;
//            }
//            this.tree.growTree(world, world.getChunkSource().getGenerator(), pos, state, rand);
//        }
//    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (level.getBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            if (!level.isAreaLoaded(pos, 1)) {
                return;
            }
//            this.placeTree(level, pos, state, random);
        }
    }
}