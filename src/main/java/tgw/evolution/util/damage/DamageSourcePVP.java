package tgw.evolution.util.damage;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import tgw.evolution.init.EvolutionDamage;

public class DamageSourcePVP extends DamageSourcePlayer implements IHitLocation {

    private final EquipmentSlot hitLocation;

    public DamageSourcePVP(String damage, Player entity, EvolutionDamage.Type type, InteractionHand hand, EquipmentSlot hitLocation) {
        super(damage, entity, type, hand);
        this.hitLocation = hitLocation;
    }

    @Override
    public EquipmentSlot getHitLocation() {
        return this.hitLocation;
    }
}
