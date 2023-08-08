package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(RedStoneOreBlock.class)
public abstract class Mixin_M_RedStoneOreBlock extends Block {

    @Shadow @Final public static BooleanProperty LIT;

    public Mixin_M_RedStoneOreBlock(Properties properties) {
        super(properties);
    }

    @Overwrite
    @DeleteMethod
    private static void interact(BlockState blockState, Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Unique
    private static void interact(BlockState state, Level level, int x, int y, int z) {
        spawnParticles(level, x, y, z);
        if (!state.getValue(LIT)) {
            level.setBlockAndUpdate_(x, y, z, state.setValue(LIT, true));
        }
    }

    @Overwrite
    @DeleteMethod
    private static void spawnParticles(Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Unique
    private static void spawnParticles(Level level, int x, int y, int z) {
        Random random = level.random;
        if (!level.getBlockState_(x - 1, y, z).isSolidRender_(level, x - 1, y, z)) {
            level.addParticle(DustParticleOptions.REDSTONE, x + 0.5 - 0.562_5, y + random.nextFloat(), z + random.nextFloat(), 0, 0, 0);
        }
        if (!level.getBlockState_(x + 1, y, z).isSolidRender_(level, x + 1, y, z)) {
            level.addParticle(DustParticleOptions.REDSTONE, x + 0.5 + 0.562_5, y + random.nextFloat(), z + random.nextFloat(), 0, 0, 0);
        }
        if (!level.getBlockState_(x, y - 1, z).isSolidRender_(level, x, y - 1, z)) {
            level.addParticle(DustParticleOptions.REDSTONE, x + random.nextFloat(), y + 0.5 - 0.562_5, z + random.nextFloat(), 0, 0, 0);
        }
        if (!level.getBlockState_(x, y + 1, z).isSolidRender_(level, x, y + 1, z)) {
            level.addParticle(DustParticleOptions.REDSTONE, x + random.nextFloat(), y + 0.5 + 0.562_5, z + random.nextFloat(), 0, 0, 0);
        }
        if (!level.getBlockState_(x, y, z - 1).isSolidRender_(level, x, y, z - 1)) {
            level.addParticle(DustParticleOptions.REDSTONE, x + random.nextFloat(), y + random.nextFloat(), z + 0.5 - 0.562_5, 0, 0, 0);
        }
        if (!level.getBlockState_(x, y, z + 1).isSolidRender_(level, x, y, z + 1)) {
            level.addParticle(DustParticleOptions.REDSTONE, x + random.nextFloat(), y + random.nextFloat(), z + 0.5 + 0.562_5, 0, 0, 0);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (state.getValue(LIT)) {
            spawnParticles(level, x, y, z);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public void attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        interact(state, level, x, y, z);
        super.attack_(state, level, x, y, z, face, hitX, hitY, hitZ, player);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(LIT)) {
            level.setBlockAndUpdate_(x, y, z, state.setValue(LIT, false));
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Override
    public void spawnAfterBreak_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
        super.spawnAfterBreak_(state, level, x, y, z, stack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
            int i = 1 + level.random.nextInt(5);
            this.popExperience(level, new BlockPos(x, y, z), i);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        throw new AbstractMethodError();
    }

    @Override
    public void stepOn_(Level level, int x, int y, int z, BlockState state, Entity entity) {
        interact(state, level, x, y, z);
        super.stepOn_(level, x, y, z, state, entity);
    }

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
            spawnParticles(level, x, y, z);
        }
        else {
            interact(state, level, x, y, z);
        }
        ItemStack stack = player.getItemInHand(hand);
        return stack.getItem() instanceof BlockItem && new BlockPlaceContext(player, hand, stack, hitResult).canPlace() ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }
}
