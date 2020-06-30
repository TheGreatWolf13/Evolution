package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.NutrientHelper;

public class BlockTallGrass extends BlockBush implements IShearable {

    protected static final VoxelShape SHAPE = EvolutionHitBoxes.GRASS;

    public BlockTallGrass() {
        super(Block.Properties.create(Material.TALL_PLANTS).doesNotBlockMovement().hardnessAndResistance(0F).sound(SoundType.PLANT));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        ChunkStorageCapability.getChunkStorage(worldIn, worldIn.getChunk(pos).getPos()).map(chunkStorages -> {
            chunkStorages.addMany(NutrientHelper.DECAY_TALL_GRASS);
            return true;
        }).orElseGet(() -> false);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualFlammability(state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualEncouragement(state);
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XYZ;
    }

    @Override
    public ItemStack getDrops(BlockState state) {
        return ItemStack.EMPTY;
    }
}
