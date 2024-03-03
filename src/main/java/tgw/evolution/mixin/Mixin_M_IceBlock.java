package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(IceBlock.class)
public abstract class Mixin_M_IceBlock extends HalfTransparentBlock {

    public Mixin_M_IceBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void melt(BlockState blockState, Level level, BlockPos blockPos);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void playerDestroy(Level level,
                              Player player,
                              BlockPos blockPos,
                              BlockState blockState,
                              @Nullable BlockEntity blockEntity,
                              ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy_(level, player, x, y, z, state, te, stack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
            if (level.dimensionType().ultraWarm()) {
                level.removeBlock(new BlockPos(x, y, z), false);
                return;
            }
            Material material = level.getBlockState_(x, y - 1, z).getMaterial();
            if (material.blocksMotion() || material.isLiquid()) {
                level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState());
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
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getLightEngine().getClampedBlockLight(BlockPos.asLong(x, y, z)) > 11 - state.getLightBlock_(level, x, y, z)) {
            this.melt(state, level, new BlockPos(x, y, z));
        }
    }
}
