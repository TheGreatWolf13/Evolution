package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(EnchantmentTableBlock.class)
public abstract class Mixin_M_EnchantmentTableBlock extends BaseEntityBlock {

    @Shadow @Final public static List<BlockPos> BOOKSHELF_OFFSETS;
    @Shadow @Final protected static VoxelShape SHAPE;

    public Mixin_M_EnchantmentTableBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static boolean isValidBookShelf(Level level, BlockPos pos, BlockPos offset) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int dx = offset.getX();
        int dy = offset.getY();
        int dz = offset.getZ();
        return level.getBlockState_(x + dx, y + dy, z + dz).is(Blocks.BOOKSHELF) && level.isEmptyBlock_(x + dx / 2, y + dy / 2, z + dz / 2);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        super.animateTick_(state, level, x, y, z, random);
        for (int i = 0, len = BOOKSHELF_OFFSETS.size(); i < len; ++i) {
            BlockPos offset = BOOKSHELF_OFFSETS.get(i);
            if (random.nextInt(16) == 0) {
                if (isValidBookShelf(level, new BlockPos(x, y, z), offset)) {
                    level.addParticle(ParticleTypes.ENCHANT, x + 0.5, y + 2, z + 0.5, offset.getX() + random.nextFloat() - 0.5, offset.getY() - random.nextFloat() - 1.0F, offset.getZ() + random.nextFloat() - 0.5);
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(state.getMenuProvider(level, new BlockPos(x, y, z)));
        return InteractionResult.CONSUME;
    }
}
