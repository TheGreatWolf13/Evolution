package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(BreakDoorGoal.class)
public abstract class MixinBreakDoorGoal extends DoorInteractGoal {

    public MixinBreakDoorGoal(Mob pMob) {
        super(pMob);
    }

    @Redirect(method = {"tick", "stop"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlockProgress" +
                                                                             "(ILnet/minecraft/core/BlockPos;I)V"))
    private void onBlockDestroyProgress(Level level, int breakerId, BlockPos pos, int progress) {
        level.destroyBlockProgress(breakerId, pos.asLong(), progress, Direction.SOUTH, 0, 0, 0);
    }
}
