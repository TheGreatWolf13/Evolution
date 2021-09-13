package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.trees.Tree;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.BlockFlags;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.STAGE_0_1;

public class BlockSapling extends BlockBush implements IGrowable {

    private final Tree tree;

    public BlockSapling(Tree tree) {
        super(Properties.of(Material.GRASS).noCollission().randomTicks().strength(0.0F, 0.0F).sound(SoundType.GRASS));
        this.tree = tree;
        this.registerDefaultState(this.defaultBlockState().setValue(STAGE_0_1, 0));
    }

    public static boolean canGrowInto(IWorldGenerationReader worldIn, BlockPos pos) {
        BlockState state = ((IBlockReader) worldIn).getBlockState(pos); //TODO proper vine
        return state.isAir((IBlockReader) worldIn, pos) ||
               state.getBlock() instanceof BlockLeaves ||
               state.getBlock() instanceof BlockGrass ||
               state.getBlock() instanceof BlockDirt ||
               state.getBlock() instanceof BlockLog ||
               state.getBlock() instanceof BlockSapling ||
               state.getBlock() == Blocks.VINE ||
               state.getBlock() instanceof BlockDryGrass;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(STAGE_0_1);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.SAPLING;
    }

    @Override
    public boolean isBonemealSuccess(World world, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public void performBonemeal(ServerWorld world, Random rand, BlockPos pos, BlockState state) {
        this.placeTree(world, pos, state, rand);
    }

    public void placeTree(ServerWorld world, BlockPos pos, BlockState state, Random rand) {
        if (state.getValue(STAGE_0_1) == 0) {
            world.setBlock(pos, state.cycle(STAGE_0_1), BlockFlags.NO_RERENDER);
        }
        else {
            if (!ForgeEventFactory.saplingGrowTree(world, rand, pos)) {
                return;
            }
            this.tree.growTree(world, world.getChunkSource().getGenerator(), pos, state, rand);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            if (!world.isAreaLoaded(pos, 1)) {
                return;
            }
            this.placeTree(world, pos, state, random);
        }
    }
}