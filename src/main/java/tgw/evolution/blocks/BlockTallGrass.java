package tgw.evolution.blocks;

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
import net.minecraftforge.common.IForgeShearable;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.NutrientHelper;

public class BlockTallGrass extends BlockBush implements IForgeShearable {

    public BlockTallGrass() {
        super(Properties.of(Material.PLANT).noCollission().strength(0.0F).sound(SoundType.GRASS));
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XYZ;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return EvolutionHitBoxes.GRASS;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        CapabilityChunkStorage.getChunkStorage(worldIn, worldIn.getChunk(pos).getPos())
                              .ifPresent(chunkStorages -> chunkStorages.addMany(NutrientHelper.DECAY_TALL_GRASS));
    }
}
