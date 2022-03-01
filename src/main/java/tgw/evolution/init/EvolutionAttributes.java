package tgw.evolution.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.Evolution;
import tgw.evolution.inventory.SlotType;
import tgw.evolution.util.UnregisteredFeatureException;

import java.util.UUID;

public final class EvolutionAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Evolution.MODID);

    public static final RegistryObject<Attribute> COLD_RESISTANCE = register("cold_resistance", 0, -1_000, 1_000, true);
    public static final RegistryObject<Attribute> FRICTION = register("friction", 2, 2, Integer.MAX_VALUE, true);
    public static final RegistryObject<Attribute> HEAT_RESISTANCE = register("heat_resistance", 0, -1_000, 1_000, true);
    public static final RegistryObject<Attribute> MASS = register("mass", 70, 0, Integer.MAX_VALUE, true);

    //Attack Damage
    private static final UUID ATTACK_DAMAGE_MOD_MAINHAND = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    //Attack Speed
    private static final UUID ATTACK_SPEED_MOD_MAINHAND = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    //Cold Resistance
    private static final UUID COLD_RESISTANCE_MOD_FEET = UUID.fromString("e0b77758-e2c6-462f-bf15-d6615e028414");
    //Friction
//    private static final UUID FRICTION_MOD = UUID.fromString("c9907da6-8dd4-11eb-8dcd-0242ac130003");
    //Heat Resistance
    private static final UUID HEAT_RESISTANCE_MOD_FEET = UUID.fromString("3c051607-b4ae-462c-b2aa-b0e5e02902ec");
    //Mass
    private static final UUID MASS_MOD_MAINHAND = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a2");
    private static final UUID MASS_MOD_OFFHAND = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a3");
    private static final UUID MASS_MOD_FEET = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a4");
    private static final UUID MASS_MOD_BOOTS = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a5");
    private static final UUID MASS_MOD_LEGGINGS = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a6");
    private static final UUID MASS_MOD_CHESTPLATE = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a7");
    private static final UUID MASS_MOD_HELMET = UUID.fromString("d12c48de-b027-4f50-931e-81e7184a78a8");
    //Reach
    private static final UUID REACH_MOD_MAINHAND = UUID.fromString("449b8c5d-47b0-4c67-a90e-758b956f2d3c");
    //Effects
    private static final UUID SLOW_FALLING_MOD = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");

    public static final AttributeModifier SLOW_FALLING = new AttributeModifier(SLOW_FALLING_MOD,
                                                                               "Slow falling acceleration reduction",
                                                                               -0.02,
                                                                               AttributeModifier.Operation.ADDITION);

    private EvolutionAttributes() {
    }

    public static AttributeModifier attackDamageModifier(double value, SlotType slot) {
        switch (slot) {
            case MAINHAND -> {
                return new AttributeModifier(ATTACK_DAMAGE_MOD_MAINHAND, "Damage modifier", value, AttributeModifier.Operation.ADDITION);
            }
        }
        throw new UnregisteredFeatureException("Unregistered slot for Attribute: " + slot);
    }

    public static AttributeModifier attackSpeedModifier(double value, SlotType slot) {
        switch (slot) {
            case MAINHAND -> {
                return new AttributeModifier(ATTACK_SPEED_MOD_MAINHAND, "Attack Speed Modifier", value, AttributeModifier.Operation.ADDITION);
            }
        }
        throw new UnregisteredFeatureException("Unregistered slot for Attribute: " + slot);
    }

    public static AttributeModifier coldResistanceModifier(double value, SlotType slot) {
        switch (slot) {
            case FEET -> {
                return new AttributeModifier(COLD_RESISTANCE_MOD_FEET, "Cold Resistance Modifier", value, AttributeModifier.Operation.ADDITION);
            }
        }
        throw new UnregisteredFeatureException("Unregistered slot for Attribute: " + slot);
    }

    public static AttributeModifier heatResistanceModifier(double value, SlotType slot) {
        switch (slot) {
            case FEET -> {
                return new AttributeModifier(HEAT_RESISTANCE_MOD_FEET, "Heat Resistance Modifier", value, AttributeModifier.Operation.ADDITION);
            }
        }
        throw new UnregisteredFeatureException("Unregistered slot for Attribute: " + slot);
    }

    public static AttributeModifier massModifier(double value, SlotType slot) {
        return switch (slot) {
            case MAINHAND -> new AttributeModifier(MASS_MOD_MAINHAND, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            case OFFHAND -> new AttributeModifier(MASS_MOD_OFFHAND, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            case FEET -> new AttributeModifier(MASS_MOD_FEET, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            case BOOTS -> new AttributeModifier(MASS_MOD_BOOTS, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            case LEGGINGS -> new AttributeModifier(MASS_MOD_LEGGINGS, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            case CHESTPLATE -> new AttributeModifier(MASS_MOD_CHESTPLATE, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            case HELMET -> new AttributeModifier(MASS_MOD_HELMET, "Mass Modifier", value, AttributeModifier.Operation.ADDITION);
            default -> throw new UnregisteredFeatureException("Unregistered slot for Attribute: " + slot);
        };
    }

    public static AttributeModifier reachModifier(double value, SlotType slot) {
        switch (slot) {
            case MAINHAND -> {
                return new AttributeModifier(REACH_MOD_MAINHAND, "Reach Modifier", value, AttributeModifier.Operation.ADDITION);
            }
        }
        throw new UnregisteredFeatureException("Unregistered slot for Attribute: " + slot);
    }

    private static RegistryObject<Attribute> register(String name, double value, double min, double max, boolean sync) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute(Evolution.MODID + "." + name, value, min, max).setSyncable(sync));
    }

    public static void register() {
        ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
