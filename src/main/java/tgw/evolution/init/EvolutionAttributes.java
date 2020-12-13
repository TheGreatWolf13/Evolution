package tgw.evolution.init;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

import java.util.UUID;

public final class EvolutionAttributes {

    public static final IAttribute MASS = new RangedAttribute(null, "evolution.mass", 70, 0, Integer.MAX_VALUE).setShouldWatch(true);
    public static final UUID MASS_MODIFIER = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a2");
    public static final UUID MASS_MODIFIER_OFFHAND = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a3");
    public static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final UUID REACH_DISTANCE_MODIFIER = UUID.fromString("449b8c5d-47b0-4c67-a90e-758b956f2d3c");
    public static final UUID SLOW_FALLING_MODIFIER = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");
    public static final UUID WEIGHT_SLOWDOWN_MODIFIER = UUID.fromString("53ee6763-e72c-4b76-80f5-0c53d0038bac");
    public static final AttributeModifier SLOW_FALLING = new AttributeModifier(SLOW_FALLING_MODIFIER,
                                                                               "Slow falling acceleration reduction",
                                                                               -0.02,
                                                                               AttributeModifier.Operation.ADDITION).setSaved(false);

    private EvolutionAttributes() {
    }
}
