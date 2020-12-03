package tgw.evolution.util.damage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.Hand;
import tgw.evolution.init.EvolutionDamage;

public class DamageSourcePVP extends DamageSourcePlayer {

    private final EquipmentSlotType slot;

    public DamageSourcePVP(String damage, PlayerEntity entity, EvolutionDamage.Type type, Hand hand, EquipmentSlotType slot) {
        super(damage, entity, type, hand);
        this.slot = slot;
    }

    public EquipmentSlotType getHitLocation() {
        return this.slot;
    }
}
