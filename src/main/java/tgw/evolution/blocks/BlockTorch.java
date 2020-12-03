package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockTorch extends Block implements IReplaceable, IFireSource {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = EvolutionHitBoxes.TORCH;

    public BlockTorch() {
        super(Block.Properties.create(Material.MISCELLANEOUS)
                              .hardnessAndResistance(0.0F)
                              .tickRandomly()
                              .doesNotBlockMovement()
                              .sound(SoundType.WOOD));
        this.setDefaultState(this.getDefaultState().with(LIT, true));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!stateIn.get(LIT)) {
            return;
        }
        double posX = pos.getX() + 0.5;
        double posY = pos.getY() + 0.7;
        double posZ = pos.getZ() + 0.5;
        worldIn.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, posX, posY, posZ, 0, 0, 0);
    }

    @Override
    public boolean canBeReplacedByLiquid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TETorch();
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        if (state.get(LIT)) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TETorch) {
                return ItemTorch.getDroppedStack((TETorch) tile);
            }
            throw new IllegalStateException("Invalid Tile Entity for BlockTorch: " + tile);
        }
        return new ItemStack(EvolutionItems.torch_unlit.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (!state.get(LIT)) {
            List<ItemStack> list = new ArrayList<>(1);
            list.add(new ItemStack(EvolutionItems.torch_unlit.get()));
            return list;
        }
        TileEntity tile = builder.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TETorch) {
            List<ItemStack> list = new ArrayList<>(1);
            list.add(ItemTorch.getDroppedStack((TETorch) tile));
            return list;
        }
        throw new IllegalStateException("Invalid Tile Entity for BlockTorch: " + tile);
    }

    @Override
    public int getLightValue(BlockState state) {
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (ChunkStorageCapability.contains(context.getWorld().getChunkAt(context.getPos()), EnumStorage.OXYGEN, 1)) {
            return this.getDefaultState();
        }
        return this.getDefaultState().with(LIT, false);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.get(LIT);
    }

    @Override
    public boolean isFireSource(BlockState state) {
        return state.get(LIT);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        return hasEnoughSolidSide(world, pos.down(), Direction.UP);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(handIn);
        if (stack.isEmpty() && state.get(LIT)) {
            world.setBlockState(pos, state.with(LIT, false), Constants.BlockFlags.IS_MOVING);
            world.removeTileEntity(pos);
            world.playSound(player,
                            pos,
                            SoundEvents.BLOCK_FIRE_EXTINGUISH,
                            SoundCategory.BLOCKS,
                            1.0F,
                            2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            return true;
        }
        if (stack.getItem() == EvolutionItems.torch.get() && !state.get(LIT)) {
            if (ChunkStorageCapability.remove(world.getChunkAt(pos), EnumStorage.OXYGEN, 1)) {
                ChunkStorageCapability.add(world.getChunkAt(pos), EnumStorage.CARBON_DIOXIDE, 1);
                world.setBlockState(pos, state.with(LIT, true));
                world.playSound(player, pos, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.7F + 0.3F);
                if (!world.isRemote) {
                    TETorch tile = (TETorch) world.getTileEntity(pos);
                    tile.create();
                }
                player.addStat(Stats.ITEM_CRAFTED.get(EvolutionItems.torch.get()));
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (worldIn.isRemote) {
            return;
        }
        if (!state.get(LIT)) {
            return;
        }
        TETorch tile = (TETorch) worldIn.getTileEntity(pos);
        tile.create();
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (worldIn.isRemote || !state.get(LIT)) {
            return;
        }
        if (worldIn.getTileEntity(pos) != null) {
            TETorch tile = (TETorch) worldIn.getTileEntity(pos);
            int torchTime = EvolutionConfig.COMMON.torchTime.get();
            if (torchTime == 0) {
                return;
            }
            if (worldIn.getDayTime() >= tile.getTimePlaced() + (long) torchTime * Time.HOUR_IN_TICKS) {
                worldIn.setBlockState(pos, state.with(LIT, false));
                worldIn.removeTileEntity(pos);
            }
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(LIT);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld worldIn,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        return facing == Direction.DOWN && !this.isValidPosition(stateIn, worldIn, currentPos) ?
               Blocks.AIR.getDefaultState() :
               super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }
}	
