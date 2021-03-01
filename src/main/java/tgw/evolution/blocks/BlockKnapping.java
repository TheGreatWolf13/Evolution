package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.items.ItemRock;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.RockVariant;

public class BlockKnapping extends BlockGravity implements IReplaceable, IStoneVariant {

    private final RockVariant variant;

    public BlockKnapping(RockVariant variant, int mass) {
        super(Block.Properties.create(Material.MISCELLANEOUS).sound(SoundType.STONE), mass);
        this.variant = variant;
    }

    public static VoxelShape calculateHitbox(TEKnapping tile) {
        VoxelShape shape = VoxelShapes.empty();
        for (int i = 0; i < tile.matrix.length; i++) {
            for (int j = 0; j < tile.matrix[i].length; j++) {
                if (tile.matrix[i][j]) {
                    shape = MathHelper.union(shape, EvolutionHitBoxes.KNAPPING_PART.withOffset(3 * i / 16.0f, 0, 3 * j / 16.0f));
                }
            }
        }
        return shape;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEKnapping();
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return new ItemStack(this.variant.getRock());
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0F;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(this.variant.getRock());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TEKnapping tile = (TEKnapping) world.getTileEntity(pos);
        if (tile != null) {
            if (tile.hitbox != null) {
                return tile.hitbox;
            }
            tile.hitbox = calculateHitbox(tile);
            return tile.hitbox;
        }
        return EvolutionHitBoxes.KNAPPING_FULL;
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
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
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos posDown = pos.down();
        BlockState down = world.getBlockState(posDown);
        BlockState up = world.getBlockState(pos.up());
        if (!BlockUtils.isReplaceable(up)) {
            return false;
        }
        return Block.hasSolidSide(down, world, posDown, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isRemote) {
            if (!state.isValidPosition(world, pos)) {
                spawnDrops(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!(player.getHeldItem(hand).getItem() instanceof ItemRock)) {
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
        TEKnapping tileEntity = (TEKnapping) world.getTileEntity(pos);
        int x = MathHelper.getIndex(5, 0.5, 15.5, hitX);
        int z = MathHelper.getIndex(5, 0.5, 15.5, hitZ);
        if (!tileEntity.matrix[x][z] || tileEntity.type.getPattern()[x][z]) {
            return false;
        }
        world.playSound(player, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 0.75F);
        tileEntity.matrix[x][z] = false;
        tileEntity.sendRenderUpdate();
        tileEntity.checkParts();
        return true;
    }
}
