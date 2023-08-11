package tgw.evolution.init;

import net.minecraft.world.level.GameRules;

public final class EvolutionGameRules {

    public static final GameRules.Key<GameRules.IntegerValue> TORCH_DURATION = GameRules.register("torchDuration", GameRules.Category.UPDATES, GameRules.IntegerValue.create(8));

    private EvolutionGameRules() {
    }

    public static void register() {
        //Registered via class-loading
    }
}
