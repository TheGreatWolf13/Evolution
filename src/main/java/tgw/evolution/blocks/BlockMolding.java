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
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nonnull;

import static tgw.evolution.init.EvolutionBStates.LAYERS_1_5;

public class BlockMolding extends BlockEvolution implements IReplaceable {

    public BlockMolding() {
        super(Block.Properties.create(Material.CLAY).hardnessAndResistance(0.0F).sound(SoundType.GROUND));
        this.setDefaultState(this.getDefaultState().with(LAYERS_1_5, 1));
    }

    private static void dropItemStack(World world, BlockPos pos, @Nonnull ItemStack stack) {
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, stack);
        Vec3d motion = entity.getMotion();
        entity.addVelocity(-motion.x, -motion.y, -motion.z);
        world.addEntity(entity);
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
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEMolding();
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_5);
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(EvolutionItems.clay.get(), state.get(LAYERS_1_5));
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
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TEMolding tile = (TEMolding) world.getTileEntity(pos);
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
    public boolean isSolid(BlockState state) {
        return false;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos posDown = pos.down();
        BlockState down = worldIn.getBlockState(posDown);
        BlockState up = worldIn.getBlockState(pos.up());
        return BlockUtils.isReplaceable(up) && Block.hasSolidSide(down, worldIn, posDown, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            if (!state.isValidPosition(worldIn, pos)) {
                spawnDrops(state, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        int layers = state.get(LAYERS_1_5);
        if (player.getHeldItem(handIn).getItem() == EvolutionItems.clayball.get()) {
            if (layers < 5) {
                worldIn.setBlockState(pos, state.with(LAYERS_1_5, layers + 1));
                TEMolding tile = (TEMolding) worldIn.getTileEntity(pos);
                tile.addLayer(layers);
                worldIn.playSound(player, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 0.5F, 0.8F);
                if (!player.isCreative()) {
                    player.getHeldItem(handIn).shrink(1);
                }
                return true;
            }
            return false;
        }
        if (!player.getHeldItem(handIn).isEmpty()) {
            return false;
        }
        TEMolding tile = (TEMolding) worldIn.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        double hitX = (hit.getHitVec().x - pos.getX()) * 16;
        if (!MathHelper.rangeInclusive(hitX, 0.5, 15.5)) {
            return false;
        }
        double hitZ = (hit.getHitVec().z - pos.getZ()) * 16;
        if (!MathHelper.rangeInclusive(hitZ, 0.5, 15.5)) {
            return false;
        }
        double hitY = (hit.getHitVec().y - pos.getY()) * 16;
        int x = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Axis.X, hitX, hit.getFace()));
        int y = MathHelper.getIndex(5, 0, 15, MathHelper.hitOffset(Axis.Y, hitY, hit.getFace()));
        int z = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Axis.Z, hitZ, hit.getFace()));
        if (!tile.matrices[y][x][z] || tile.molding.getPattern()[y][x][z]) {
            return false;
        }
        worldIn.playSound(player, pos, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.BLOCKS, 1.0F, 0.75F);
        tile.matrices[y][x][z] = false;
        if (layers != 1) {
            int fail = tile.check();
            if (fail != -1) {
                int count = 0;
                for (int i = fail; i < layers; i++) {
                    count++;
                }
                if (layers == count) {
                    worldIn.removeBlock(pos, false);
                }
                else {
                    worldIn.setBlockState(pos, state.with(LAYERS_1_5, layers - count));
                }
                if (!worldIn.isRemote) {
                    dropItemStack(worldIn, pos, new ItemStack(EvolutionItems.clayball.get(), count));
                }
            }
        }
        tile.sendRenderUpdate();
        tile.checkPatterns();
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.equals(newState)) {
            TEMolding tile = (TEMolding) worldIn.getTileEntity(pos);
            if (tile != null) {
                tile.sendRenderUpdate();
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
