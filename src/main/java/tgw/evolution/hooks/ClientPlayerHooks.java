package tgw.evolution.hooks;

import net.minecraft.client.entity.player.ClientPlayerEntity;

public final class ClientPlayerHooks {

    private ClientPlayerHooks() {
    }

    /**
     * Hooks from {@link ClientPlayerEntity#livingTick()}
     */
    @EvolutionHook
    public static boolean getSprintBoolean(boolean bool, ClientPlayerEntity player) {
        return bool || player.collidedHorizontally || player.getSubmergedHeight() > 0.4 && !player.canSwim();
    }
}
