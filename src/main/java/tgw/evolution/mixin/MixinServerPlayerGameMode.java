package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.patches.PatchLevel;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Redirect(method = {"incrementDestroyProgress", "tick", "handleBlockBreakAction"}, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/server/level/ServerLevel;" +
            "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void onDestroyBlockProgress(ServerLevel level, int breakerId, BlockPos pos, int progress) {
        ((PatchLevel) level).destroyBlockProgress(breakerId, pos.asLong(), progress);
    }
}
