package tgw.evolution.util.damage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.Hand;
import tgw.evolution.init.EvolutionDamage;

public class DamageSourcePVP extends DamageSourcePlayer implements IHitLocation {

    private final EquipmentSlotType hitLocation;

    public DamageSourcePVP(String damage, PlayerEntity entity, EvolutionDamage.Type type, Hand hand, EquipmentSlotType hitLocation) {
        super(damage, entity, type, hand);
        this.hitLocation = hitLocation;
    }

    @Override
    public EquipmentSlotType getHitLocation() {
        return this.hitLocation;
    }
}
