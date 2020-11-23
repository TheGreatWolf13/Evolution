package tgw.evolution.hooks;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import tgw.evolution.entities.EvolutionAttributes;

public class PlayerHooks {

    @EvolutionHook
    public static void registerAttributes(PlayerEntity player) {
        player.getAttributes().registerAttribute(EvolutionAttributes.MASS);
        player.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);
    }
}
