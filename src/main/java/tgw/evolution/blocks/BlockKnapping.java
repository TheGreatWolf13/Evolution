package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.items.ItemRock;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.MathHelper;

import javax.annotation.Nullable;

public class BlockKnapping extends BlockGravity implements IReplaceable, IRockVariant, EntityBlock {

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
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState up = level.getBlockState(pos.above());
        if (!BlockUtils.isReplaceable(up)) {
            return false;
        }
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return new ItemStack(this.variant.getRock());
    }

    @Override
    public NonNullList<ItemStack> getDrops(Level level, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY, new ItemStack(this.variant.getRock()));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0F;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
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
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEKnapping(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(player.getItemInHand(hand).getItem() instanceof ItemRock)) {
            return InteractionResult.PASS;
        }
        double hitX = (hit.getLocation().x - pos.getX()) * 16;
        double hitZ = (hit.getLocation().z - pos.getZ()) * 16;
        TEKnapping tileEntity = (TEKnapping) level.getBlockEntity(pos);
        int x = MathHelper.getIndex(8, 0, 16, MathHelper.hitOffset(Direction.Axis.X, hitX, hit.getDirection()));
        int z = MathHelper.getIndex(8, 0, 16, MathHelper.hitOffset(Direction.Axis.Z, hitZ, hit.getDirection()));
        if (!tileEntity.getPart(x, z) || tileEntity.type.getPatternPart(x, z)) {
            return InteractionResult.FAIL;
        }
        level.playSound(player, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 0.75F);
        tileEntity.clearPart(x, z);
        tileEntity.sendRenderUpdate();
        tileEntity.checkParts(player);
        return InteractionResult.SUCCESS;
    }
}
