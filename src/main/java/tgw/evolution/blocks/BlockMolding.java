package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nonnull;

import static tgw.evolution.init.EvolutionBStates.LAYERS_1_5;

public class BlockMolding extends BlockGeneric implements IReplaceable {

    public BlockMolding() {
        super(Properties.of(Material.CLAY).strength(0.0F).sound(SoundType.GRAVEL).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_1_5, 1));
    }

    private static void dropItemStack(World world, BlockPos pos, @Nonnull ItemStack stack) {
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, stack);
        Vector3d motion = entity.getDeltaMovement();
        entity.push(-motion.x, -motion.y, -motion.z);
        world.addFreshEntity(entity);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        BlockState up = world.getBlockState(pos.above());
        return BlockUtils.isReplaceable(up) && BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_5);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEMolding();
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.clay.get(), state.getValue(LAYERS_1_5));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.45F;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(EvolutionItems.clayball.get());
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TEMolding tile = (TEMolding) world.getBlockEntity(pos);
        if (tile != null) {
            return tile.getHitbox(state);
        }
        return EvolutionHitBoxes.MOLD_1;
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.equals(newState)) {
            TEMolding tile = (TEMolding) worldIn.getBlockEntity(pos);
            if (tile != null) {
                tile.sendRenderUpdate();
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        int layers = state.getValue(LAYERS_1_5);
        if (player.getItemInHand(hand).getItem() == EvolutionItems.clayball.get()) {
            if (layers < 5) {
                world.setBlockAndUpdate(pos, state.setValue(LAYERS_1_5, layers + 1));
                TEMolding tile = (TEMolding) world.getBlockEntity(pos);
                tile.addLayer(layers);
                world.playSound(player, pos, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 0.5F, 0.8F);
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            return ActionResultType.PASS;
        }
        TEMolding tile = (TEMolding) world.getBlockEntity(pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        double hitX = (hit.getLocation().x - pos.getX()) * 16;
        if (!MathHelper.rangeInclusive(hitX, 0.5, 15.5)) {
            return ActionResultType.PASS;
        }
        double hitZ = (hit.getLocation().z - pos.getZ()) * 16;
        if (!MathHelper.rangeInclusive(hitZ, 0.5, 15.5)) {
            return ActionResultType.PASS;
        }
        double hitY = (hit.getLocation().y - pos.getY()) * 16;
        int x = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Axis.X, hitX, hit.getDirection()));
        int y = MathHelper.getIndex(5, 0, 15, MathHelper.hitOffset(Axis.Y, hitY, hit.getDirection()));
        int z = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Axis.Z, hitZ, hit.getDirection()));
//        if (!tile.matrices[y][x][z] || tile.molding.getPattern()[y][x][z]) {
//            return ActionResultType.PASS;
//        }
        world.playSound(player, pos, SoundEvents.GRAVEL_BREAK, SoundCategory.BLOCKS, 1.0F, 0.75F);
//        tile.matrices[y][x][z] = false;
        if (layers != 1) {
            int fail = tile.check();
            if (fail != -1) {
                int count = 0;
                for (int i = fail; i < layers; i++) {
                    count++;
                }
                if (layers == count) {
                    world.removeBlock(pos, false);
                }
                else {
                    world.setBlockAndUpdate(pos, state.setValue(LAYERS_1_5, layers - count));
                }
                if (!world.isClientSide) {
                    dropItemStack(world, pos, new ItemStack(EvolutionItems.clayball.get(), count));
                }
            }
        }
        tile.sendRenderUpdate();
        tile.checkPatterns();
        return ActionResultType.SUCCESS;
    }
}
