package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.trees.Tree;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.IWorldGenerationReader;
import tgw.evolution.init.EvolutionHitBoxes;

import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.STAGE_0_1;

public class BlockSapling extends BlockBush implements IGrowable {

    private final Tree tree;

    public BlockSapling(Tree tree) {
        super(Block.Properties.create(Material.PLANTS)
                              .doesNotBlockMovement()
                              .tickRandomly()
                              .hardnessAndResistance(0.0F, 0.0F)
                              .sound(SoundType.PLANT));
        this.tree = tree;
        this.setDefaultState(this.getDefaultState().with(STAGE_0_1, 0));
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
    public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(STAGE_0_1);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.SAPLING;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, BlockState state) {
        this.grow(worldIn, pos, state, rand);
    }

    public void grow(IWorld worldIn, BlockPos pos, BlockState state, Random rand) {
        if (state.get(STAGE_0_1) == 0) {
            worldIn.setBlockState(pos, state.with(STAGE_0_1, 1), 4);
            return;
        }
        this.tree.spawn(worldIn, pos, state, rand);

    }

    @Override
    public void tick(BlockState state, World world, BlockPos pos, Random random) {
        super.tick(state, world, pos, random);
        if (!world.isAreaLoaded(pos, 1)) {
            return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        }
        if (world.getLight(pos.up()) >= 9 && random.nextInt(7) == 0) {
            this.grow(world, pos, state, random);
        }
    }
}