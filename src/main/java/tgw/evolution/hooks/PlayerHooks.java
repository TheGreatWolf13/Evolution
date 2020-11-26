package tgw.evolution.hooks;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import tgw.evolution.entities.EvolutionAttributes;
import tgw.evolution.util.PlayerHelper;

public final class PlayerHooks {

    private PlayerHooks() {
    }

    /**
     * Hooks from {@link PlayerEntity#registerAttributes()}
     */
    @EvolutionHook
    public static void registerAttributes(PlayerEntity player) {
        player.getAttributes().registerAttribute(EvolutionAttributes.MASS);
        player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);
        player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.5);
        player.getAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(PlayerHelper.ATTACK_SPEED);
        player.getAttribute(PlayerEntity.REACH_DISTANCE).setBaseValue(PlayerHelper.REACH_DISTANCE);
        player.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(PlayerHelper.WALK_SPEED);
    }
}