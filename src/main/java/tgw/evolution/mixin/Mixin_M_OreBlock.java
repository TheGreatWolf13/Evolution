package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(OreBlock.class)
public abstract class Mixin_M_OreBlock extends Block {

    @Shadow @Final private UniformInt xpRange;

    public Mixin_M_OreBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack) {
        throw new AbstractMethodError();
    }

    @Override
    public void spawnAfterBreak_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
        super.spawnAfterBreak_(state, level, x, y, z, stack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
            int xp = this.xpRange.sample(level.random);
            if (xp > 0) {
                this.popExperience(level, new BlockPos(x, y, z), xp);
            }
        }
    }
}
