package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.patches.PatchPlayer;

@Mixin(BlockBehaviour.class)
public abstract class MixinBlockBehaviour {

    /**
     * @author TheGreatWolf
     * @reason Call more sensitive getDestroySpeed
     */
    @Deprecated
    @Overwrite
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float speed = state.getDestroySpeed(level, pos);
        if (speed == -1.0F) {
            return 0.0F;
        }
        int i = player.hasCorrectToolForDrops(state) ? 30 : 100;
        return ((PatchPlayer) player).getDestroySpeed(state, pos) / speed / i;
    }
}
