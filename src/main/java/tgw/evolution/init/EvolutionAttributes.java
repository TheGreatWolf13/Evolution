package tgw.evolution.init;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import tgw.evolution.Evolution;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.util.physics.SI;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class EvolutionAttributes {

    //Attributes
    public static final Attribute COLD_RESISTANCE;
    public static final Attribute FRICTION;
    public static final Attribute HEAT_RESISTANCE;
    public static final Attribute MASS;
    public static final Attribute REACH_DISTANCE;
    public static final Attribute SWIM_SPEED;
    //Modifier
    public static final AttributeModifier SLOW_FALLING = new AttributeModifier(UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA"),
                                                                               "Slow falling acceleration reduction",
                                                                               -8 * SI.METER / SI.SECOND / SI.SECOND,
                                                                               AttributeModifier.Operation.ADDITION);
    //UUIDs
    private static final UUIDHolder UUID_HEAT_RESISTANCE = new UUIDHolder("3c051607-b4ae-462c-b2aa-b0e5e02902ec");
    private static final UUIDHolder UUID_MASS = new UUIDHolder("d12c48de-b027-4f50-931e-81e7184a78a2");
    //

    static {
        COLD_RESISTANCE = register("cold_resistance", 0, -1_000, 1_000, true);
        FRICTION = register("friction", 2, 0, Integer.MAX_VALUE, true);
        HEAT_RESISTANCE = register("heat_resistance", 0, -1_000, 1_000, true);
        MASS = register("mass", 70, 0, Integer.MAX_VALUE, true);
        REACH_DISTANCE = register("reach_distance", 4.5, 0, 1_024, true);
        SWIM_SPEED = register("swim_speed", 1, 0, 1_024, true);
    }

    private EvolutionAttributes() {
    }

    public static AttributeModifier heatResistanceModifier(double value, SlotType slot) {
        return new AttributeModifier(UUID_HEAT_RESISTANCE.get(slot), "", value, AttributeModifier.Operation.ADDITION);
    }

    public static AttributeModifier massModifier(double value, SlotType slot) {
        return new AttributeModifier(UUID_MASS.get(slot), "", value, AttributeModifier.Operation.ADDITION);
    }

    private static Attribute register(String name, double value, double min, double max, boolean sync) {
        return Registry.register(Registry.ATTRIBUTE, Evolution.getResource(name),
                                 new RangedAttribute(Evolution.MODID + "." + name, value, min, max).setSyncable(sync));
    }

    public static void register() {
        //This is called on the <clinit> of Attributes.
    }

    public static class UUIDHolder {

        private final Map<SlotType, UUID> registry = new EnumMap<>(SlotType.class);

        public UUIDHolder(String baseUUID) {
            baseUUID = baseUUID.substring(0, baseUUID.length() - 2) + "00";
            UUID uuid = UUID.fromString(baseUUID);
            this.registry.put(SlotType.VALUES[0], uuid);
        }

        public UUID get(SlotType type) {
            UUID uuid = this.registry.get(type);
            if (uuid == null) {
                String base = this.registry.get(SlotType.VALUES[0]).toString();
                base = base.substring(0, base.length() - 2) + String.format("%02X", type.ordinal());
                uuid = UUID.fromString(base);
                this.registry.put(type, uuid);
            }
            return uuid;
        }
    }
}
