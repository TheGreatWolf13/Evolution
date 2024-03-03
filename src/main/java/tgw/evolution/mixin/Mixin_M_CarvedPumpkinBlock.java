package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(CarvedPumpkinBlock.class)
public abstract class Mixin_M_CarvedPumpkinBlock extends HorizontalDirectionalBlock implements Wearable {

    public Mixin_M_CarvedPumpkinBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            this.trySpawnGolem(level, new BlockPos(x, y, z));
        }
    }

    @Shadow
    protected abstract void trySpawnGolem(Level level, BlockPos blockPos);
}
