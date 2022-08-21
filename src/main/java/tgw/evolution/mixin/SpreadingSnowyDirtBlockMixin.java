package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.blocks.BlockUtils;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadingSnowyDirtBlockMixin extends SnowyDirtBlock {

    public SpreadingSnowyDirtBlockMixin(Properties p_56640_) {
        super(p_56640_);
    }

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isAreaLoaded" +
                                                                         "(Lnet/minecraft/core/BlockPos;I)Z"))
    private boolean proxyRandomTick(ServerLevel level, BlockPos pos, int range) {
        return BlockUtils.isAreaLoaded(level, pos, range);
    }
}
