package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemRock;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.math.MathHelper;

public class BlockKnapping extends BlockPhysics implements IReplaceable, IRockVariant, EntityBlock, IPoppable, IAir {

    private final RockVariant variant;

    public BlockKnapping(RockVariant variant) {
        super(Properties.of(Material.DECORATION).sound(SoundType.STONE).noOcclusion().dynamicShape());
        this.variant = variant;
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
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        BlockState up = level.getBlockState_(x, y + 1, z);
        if (!BlockUtils.isReplaceable(up)) {
            return false;
        }
        return BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, int x, int y, int z, Player player) {
        return new ItemStack(this.variant.get(EvolutionItems.ROCKS));
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8F;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter world, int x, int y, int z, @Nullable Entity entity) {
        if (world.getBlockEntity_(x, y, z) instanceof TEKnapping te) {
            return te.getOrMakeHitbox();
        }
        return EvolutionShapes.SLAB_16_D[0];
    }

    @Override
    public @Range(from = 1, to = 31) int increment(BlockState state, Direction from) {
        return 1;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEKnapping(pos, state);
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(player.getItemInHand(hand).getItem() instanceof ItemRock)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity_(x, y, z) instanceof TEKnapping tile) {
            double hitX = (hit.x() - x) * 16;
            double hitZ = (hit.z() - z) * 16;
            int partX = MathHelper.getIndex(8, 0, 16, MathHelper.hitOffset(Direction.Axis.X, hitX, hit.getDirection()));
            int partZ = MathHelper.getIndex(8, 0, 16, MathHelper.hitOffset(Direction.Axis.Z, hitZ, hit.getDirection()));
            if (!tile.getPart(partX, partZ) || tile.type.getPatternPart(partX, partZ)) {
                return InteractionResult.FAIL;
            }
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 0.75F);
            tile.clearPart(partX, partZ);
            tile.sendRenderUpdate();
            tile.checkParts(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
