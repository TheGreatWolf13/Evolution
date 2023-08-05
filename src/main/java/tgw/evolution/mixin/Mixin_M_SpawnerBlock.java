package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SpawnerBlock.class)
public abstract class Mixin_M_SpawnerBlock extends BaseEntityBlock {

    public Mixin_M_SpawnerBlock(Properties properties) {
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
        int i = 15 + level.random.nextInt(15) + level.random.nextInt(15);
        this.popExperience(level, new BlockPos(x, y, z), i);
    }
}
