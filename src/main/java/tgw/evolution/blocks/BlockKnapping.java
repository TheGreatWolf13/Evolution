package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.items.ItemRock;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.RockVariant;

public class BlockKnapping extends BlockGravity implements IReplaceable, IRockVariant {

    private final RockVariant variant;

    public BlockKnapping(RockVariant variant, int mass) {
        super(Properties.of(Material.DECORATION).sound(SoundType.STONE).noOcclusion(), mass);
        this.variant = variant;
    }

    public static VoxelShape calculateHitbox(TEKnapping tile) {
        return MathHelper.generateShapeFromPattern(tile.getParts());
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        BlockState up = world.getBlockState(pos.above());
        if (!BlockUtils.isReplaceable(up)) {
            return false;
        }
        return BlockUtils.hasSolidSide(world, pos.below(), Direction.UP);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TEKnapping();
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this.variant.getRock()));
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
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TEKnapping tile = (TEKnapping) world.getBlockEntity(pos);
        if (tile != null) {
            if (tile.hitbox != null) {
                return tile.hitbox;
            }
            tile.hitbox = calculateHitbox(tile);
            return tile.hitbox;
        }
        return EvolutionHitBoxes.SIXTEENTH_SLAB_LOWER_1;
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            if (!state.canSurvive(world, pos)) {
                dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!(player.getItemInHand(hand).getItem() instanceof ItemRock)) {
            return ActionResultType.PASS;
        }
        double hitX = (hit.getLocation().x - pos.getX()) * 16;
        double hitZ = (hit.getLocation().z - pos.getZ()) * 16;
        TEKnapping tileEntity = (TEKnapping) world.getBlockEntity(pos);
        int x = MathHelper.getIndex(8, 0, 16, MathHelper.hitOffset(Direction.Axis.X, hitX, hit.getDirection()));
        int z = MathHelper.getIndex(8, 0, 16, MathHelper.hitOffset(Direction.Axis.Z, hitZ, hit.getDirection()));
        if (!tileEntity.getPart(x, z) || tileEntity.type.getPatternPart(x, z)) {
            return ActionResultType.FAIL;
        }
        world.playSound(player, pos, SoundEvents.STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 0.75F);
        tileEntity.clearPart(x, z);
        tileEntity.sendRenderUpdate();
        tileEntity.checkParts(player);
        return ActionResultType.SUCCESS;
    }
}
