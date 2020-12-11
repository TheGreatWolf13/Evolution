package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.entities.misc.EntitySit;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.items.ItemAxe;
import tgw.evolution.items.ItemLog;
import tgw.evolution.util.EnumWoodNames;
import tgw.evolution.util.EnumWoodVariant;
import tgw.evolution.util.HarvestLevel;

import javax.annotation.Nullable;

public class BlockChopping extends BlockMass implements IReplaceable, ISittable {

    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    private static final VoxelShape SHAPE = EvolutionHitBoxes.SLAB_LOWER;

    public BlockChopping(EnumWoodNames name) {
        super(Block.Properties.create(Material.WOOD).harvestLevel(HarvestLevel.STONE).sound(SoundType.WOOD).hardnessAndResistance(8.0F, 2.0F),
              name.getMass());
        this.setDefaultState(this.getDefaultState().with(OCCUPIED, false));
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEChopping();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualFlammability(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public double getYOffset() {
        return 0.3;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockState down = worldIn.getBlockState(pos.down());
        return Block.hasSolidSide(down, worldIn, pos.down(), Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                ItemStack stack = this.getItem(worldIn, pos, this.getDefaultState());
                spawnAsEntity(worldIn, pos, stack);
                worldIn.removeBlock(pos, false);
            }
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TEChopping tile = (TEChopping) worldIn.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        if (player.getHeldItem(handIn).getItem() instanceof ItemLog && !state.get(OCCUPIED)) {
            if (tile.id == -1) {
                tile.id = ((ItemLog) player.getHeldItem(handIn).getItem()).variant.getId();
                tile.sendRenderUpdate();
                if (!player.isCreative()) {
                    player.getHeldItem(handIn).shrink(1);
                }
                return true;
            }
        }
        if (tile.id == -1 && !state.get(OCCUPIED)) {
            if (EntitySit.create(worldIn, pos, player)) {
                worldIn.setBlockState(pos, state.with(OCCUPIED, true));
                return true;
            }
            return false;
        }
        if (tile.id != -1) {
            ItemStack stack = new ItemStack(EnumWoodVariant.byId(tile.id).getLog());
            if (!worldIn.isRemote && !player.inventory.addItemStackToInventory(stack)) {
                BlockUtils.dropItemStack(worldIn, pos, stack);
            }
            tile.id = -1;
            tile.breakProgress = 0;
            tile.sendRenderUpdate();
            return true;
        }
        return false;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        TEChopping tile = (TEChopping) worldIn.getTileEntity(pos);
        if (tile == null) {
            return;
        }
        if (tile.id != -1 && player.getHeldItemMainhand().getItem() instanceof ItemAxe) {
            if (tile.breakProgress++ == 3) {
                if (!worldIn.isRemote) {
                    ItemStack stack = new ItemStack(EnumWoodVariant.byId(tile.id).getPlank(), 8);
                    BlockUtils.dropItemStack(worldIn, pos, stack);
                    player.getHeldItemMainhand().damageItem(1, player, playerEntity -> playerEntity.sendBreakAnimation(EquipmentSlotType.MAINHAND));
                }
                tile.id = -1;
                tile.breakProgress = 0;
                tile.sendRenderUpdate();
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TEChopping tile = (TEChopping) worldIn.getTileEntity(pos);
        if (tile != null) {
            tile.onRemoved();
            worldIn.removeTileEntity(pos);
        }
    }
}
