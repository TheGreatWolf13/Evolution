package tgw.evolution.entities;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

import java.util.UUID;

public class EvolutionAttributes {

    public static final IAttribute MASS = new RangedAttribute(null, "evolution.mass", 70, 0, Integer.MAX_VALUE).setShouldWatch(true);
    public static final UUID MASS_MODIFIER = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a2");
    public static final UUID MASS_MODIFIER_OFFHAND = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a3");
    public static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final UUID REACH_DISTANCE_MODIFIER = UUID.fromString("449b8c5d-47b0-4c67-a90e-758b956f2d3c");
}
