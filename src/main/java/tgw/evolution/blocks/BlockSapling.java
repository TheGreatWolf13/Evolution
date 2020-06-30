package tgw.evolution.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.trees.Tree;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.IWorldGenerationReader;

import java.util.Random;

public class BlockSapling extends BlockBush implements IGrowable {

    public static final IntegerProperty STAGE = BlockStateProperties.STAGE_0_1;
    protected static final VoxelShape SHAPE = EvolutionHitBoxes.SAPLING;
    private final Tree tree;

    public BlockSapling(Tree tree) {
        super(Block.Properties.create(Material.PLANTS).doesNotBlockMovement().tickRandomly().hardnessAndResistance(0F, 0F).sound(SoundType.PLANT));
        this.tree = tree;
        this.setDefaultState(this.stateContainer.getBaseState().with(STAGE, 0));
    }

    public static boolean canGrowInto(IWorldGenerationReader worldIn, BlockPos pos) {
        BlockState state = ((IBlockReader) worldIn).getBlockState(pos); //TODO proper vine
        return state.isAir((IBlockReader) worldIn, pos) || state.getBlock() instanceof BlockLeaves || state.getBlock() instanceof BlockGrass || state.getBlock() instanceof BlockDirt || state.getBlock() instanceof BlockLog || state.getBlock() instanceof BlockSapling || state.getBlock() == Blocks.VINE || state.getBlock() instanceof BlockDryGrass;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        super.tick(state, worldIn, pos, random);
        if (!worldIn.isAreaLoaded(pos, 1)) {
            return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        }
        if (worldIn.getLight(pos.up()) >= 9 && random.nextInt(7) == 0) {
            this.grow(worldIn, pos, state, random);
        }
    }

    public void grow(IWorld worldIn, BlockPos pos, BlockState state, Random rand) {
        if (state.get(STAGE) == 0) {
            worldIn.setBlockState(pos, state.with(STAGE, 1), 4);
            return;
        }
        this.tree.spawn(worldIn, pos, state, rand);

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
    public void grow(World worldIn, Random rand, BlockPos pos, BlockState state) {
        this.grow(worldIn, pos, state, rand);
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}