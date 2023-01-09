package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemRock;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.MathHelper;

public class BlockKnapping extends BlockPhysics implements IReplaceable, IRockVariant, EntityBlock, IPoppable, IAir {

    private final RockVariant variant;

    public BlockKnapping(RockVariant variant) {
        super(Properties.of(Material.DECORATION).sound(SoundType.STONE).noOcclusion());
        this.variant = variant;
    }

    public static VoxelShape calculateHitbox(TEKnapping tile) {
        return MathHelper.generateShapeFromPattern(tile.getParts());
    }

    @Override
    public boolean allowsFrom(BlockState state, Direction from) {
        return from != Direction.DOWN;
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
    public float getFrictionCoefficient(BlockState state) {
        return 1.0F;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return 0;
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
        return EvolutionShapes.SLAB_16_D[0];
    }

    @Override
    public @Range(from = 1, to = 63) int increment(BlockState state, Direction from) {
        return 1;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEKnapping(pos, state);
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
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
        assert tileEntity != null;
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
