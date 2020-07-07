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
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.items.ItemRock;
import tgw.evolution.util.EnumRockNames;
import tgw.evolution.util.EnumRockVariant;
import tgw.evolution.util.MathHelper;

public class BlockKnapping extends BlockGravity implements IReplaceable, IStoneVariant {

    private static final VoxelShape FULL_SHAPE = Block.makeCuboidShape(0.5, 0, 0.5, 15.5, 1, 15.5);
    private static final VoxelShape SHAPE = Block.makeCuboidShape(0.5, 0, 0.5, 3.5, 1, 3.5);

    private final EnumRockNames name;
    private EnumRockVariant variant;

    public BlockKnapping(EnumRockNames name, int mass) {
        super(Block.Properties.create(Material.MISCELLANEOUS).sound(SoundType.STONE), mass);
        this.name = name;
    }

    public static VoxelShape calculateHitbox(TEKnapping tile) {
        VoxelShape shape = VoxelShapes.empty();
        for (int i = 0; i < tile.matrix.length; i++) {
            for (int j = 0; j < tile.matrix[i].length; j++) {
                if (tile.matrix[i][j]) {
                    shape = VoxelShapes.combineAndSimplify(shape, SHAPE.withOffset(3 * i / 16f, 0, 3 * j / 16f), IBooleanFunction.OR);
                }
            }
        }
        return shape;
    }

    @Override
    public EnumRockVariant getVariant() {
        return this.variant;
    }

    @Override
    public void setVariant(EnumRockVariant variant) {
        this.variant = variant;
    }

    @Override
    public EnumRockNames getStoneName() {
        return this.name;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return new ItemStack(this.variant.getRock());
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
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
        if (!BlockUtils.isReplaceable(up)) {
            return false;
        }
        return Block.hasSolidSide(down, worldIn, posDown, Direction.UP);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!(player.getHeldItem(handIn).getItem() instanceof ItemRock)) {
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
        TEKnapping tileEntity = (TEKnapping) worldIn.getTileEntity(pos);
        int x = MathHelper.getIndex(5, 0.5, 15.5, hitX);
        int z = MathHelper.getIndex(5, 0.5, 15.5, hitZ);
        if (!tileEntity.matrix[x][z] || tileEntity.type.getPattern()[x][z]) {
            return false;
        }
        worldIn.playSound(player, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1F, 0.75F);
        tileEntity.matrix[x][z] = false;
        tileEntity.sendRenderUpdate();
        tileEntity.checkParts();
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TEKnapping tile = (TEKnapping) worldIn.getTileEntity(pos);
        if (tile != null) {
            if (tile.hitbox != null) {
                return tile.hitbox;
            }
            tile.hitbox = calculateHitbox(tile);
            return tile.hitbox;
        }
        return FULL_SHAPE;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEKnapping();
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
    public ItemStack getDrops(BlockState state) {
        return new ItemStack(this.variant.getRock());
    }
}
