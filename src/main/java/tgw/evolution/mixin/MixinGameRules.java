package tgw.evolution.mixin;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRules.class)
public abstract class MixinGameRules {

    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$BooleanValue;create(Z)" +
                                                "Lnet/minecraft/world/level/GameRules$Type;", ordinal = 19),
            index = 0)
    private static boolean onClinitDoInsomnia(boolean bl) {
        return false;
    }

    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$BooleanValue;create(Z)" +
                                                "Lnet/minecraft/world/level/GameRules$Type;", ordinal = 23),
            index = 0)
    private static boolean onClinitDoPatrolSpawning(boolean bl) {
        return false;
    }

    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$BooleanValue;create(Z)" +
                                                "Lnet/minecraft/world/level/GameRules$Type;", ordinal = 24),
            index = 0)
    private static boolean onClinitDoTraderSpawning(boolean bl) {
        return false;
    }

    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$BooleanValue;create(ZLjava/util/function/BiConsumer;)" +
                                                "Lnet/minecraft/world/level/GameRules$Type;", ordinal = 0),
            index = 0)
    private static boolean onClinitReducedDebugInfo(boolean bl) {
        return true;
    }
}
