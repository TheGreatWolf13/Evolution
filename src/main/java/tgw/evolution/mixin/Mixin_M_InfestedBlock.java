package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(InfestedBlock.class)
public abstract class Mixin_M_InfestedBlock extends Block {

    public Mixin_M_InfestedBlock(Properties properties) {
        super(properties);
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
        if (level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) &&
            EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
            this.spawnInfestation(level, new BlockPos(x, y, z));
        }
    }

    @Shadow
    protected abstract void spawnInfestation(ServerLevel serverLevel, BlockPos blockPos);
}
