package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.patches.ILevelPatch;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorGoalMixin extends DoorInteractGoal {

    public BreakDoorGoalMixin(Mob pMob) {
        super(pMob);
    }

    @Redirect(method = {"tick",
                        "stop"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlockProgress" +
                                                                     "(ILnet/minecraft/core/BlockPos;I)V"))
    private void onBlockDestroyProgress(Level level, int breakerId, BlockPos pos, int progress) {
        ((ILevelPatch) level).destroyBlockProgress(breakerId, pos.asLong(), progress);
    }
}
