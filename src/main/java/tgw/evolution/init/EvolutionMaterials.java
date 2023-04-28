package tgw.evolution.init;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMaps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.util.collection.B2RMap;
import tgw.evolution.util.collection.B2ROpenHashMap;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.math.MathHelper;

import java.util.random.RandomGenerator;

/**
 * Values for metals and alloys are real. Values for rocks are tweaked for balance, but a stronger rock is still stronger than a weaker one.
 */
public enum EvolutionMaterials {
    //Stone
    ANDESITE(0, "andesite", Type.STONE, HarvestLevel.STONE, 2_425, 250, 40, 13, true),
    BASALT(1, "basalt", Type.STONE, HarvestLevel.STONE, 2_850, 234, 80, 13, true),
    CHERT(2, "chert", Type.STONE, HarvestLevel.STONE, 2_564, 234, 24, 12, true),
    DIORITE(3, "diorite", Type.STONE, HarvestLevel.STONE, 2_797, 234, 25, 13, true),
    GABBRO(4, "gabbro", Type.STONE, HarvestLevel.STONE, 2_884, 250, 25, 13, true),
    GNEISS(5, "gneiss", Type.STONE, HarvestLevel.STONE, 2_812, 250, 24, 13, true),
    GRANITE(6, "granite", Type.STONE, HarvestLevel.STONE, 2_640, 234, 25, 13, true),
    LIMESTONE(7, "limestone", Type.STONE, HarvestLevel.STONE, 2_484, 156, 25, 13, true),
    MARBLE(8, "marble", Type.STONE, HarvestLevel.STONE, 2_716, 156, 25, 13, true),
    RED_SANDSTONE(9, "red_sandstone", Type.STONE, HarvestLevel.STONE, 2_475, 234, 25, 13, true),
    RHYOLITE(10, "rhyolite", Type.STONE, HarvestLevel.STONE, 0, 0, 0, 0, true),
    SANDSTONE(11, "sandstone", Type.STONE, HarvestLevel.STONE, 2_463, 234, 25, 13, true),
    SCHIST(12, "schist", Type.STONE, HarvestLevel.STONE, 2_732, 188, 24, 13, true),
    SHALE(13, "shale", Type.STONE, HarvestLevel.STONE, 2_335, 31, 24, 13, true),
    SLATE(14, "slate", Type.STONE, HarvestLevel.STONE, 2_691, 156, 25, 13, true),
    //Metal
    BISMUTH(15, "bismuth", Type.METAL, HarvestLevel.LOW_METAL, 9_780, 17, 32, 4, true),
    COPPER(16, "copper", Type.METAL, HarvestLevel.COPPER, 8_920, 50, 110, 210, false),
    GOLD(17, "gold", Type.METAL, HarvestLevel.LOW_METAL, 19_300, 25, 77, 120, false),
    IRON(18, "iron", Type.METAL, HarvestLevel.IRON, 7_874, 55, 200, 540, false),
    LEAD(19, "lead", Type.METAL, HarvestLevel.LOW_METAL, 11_340, 5, 14, 18, false),
    SILVER(20, "silver", Type.METAL, HarvestLevel.LOW_METAL, 10_490, 25, 76, 140, false),
    TIN(21, "tin", Type.METAL, HarvestLevel.LOW_METAL, 7_265, 5, 44, 220, false),
    ZINC(22, "zinc", Type.METAL, HarvestLevel.LOW_METAL, 7_140, 30, 97, 37, true),
    //Wood
    WOOD(23, "wood", Type.WOOD, HarvestLevel.HAND, 1_500, 4, 10, 105, false);

    public static final EvolutionMaterials[] VALUES = values();
    private static final Byte2ReferenceMap<EvolutionMaterials> REGISTRY;

    static {
        B2RMap<EvolutionMaterials> map = new B2ROpenHashMap<>();
        for (EvolutionMaterials material : VALUES) {
            if (map.put(material.id, material) != null) {
                throw new IllegalStateException("EvolutionMaterials " + material + " has duplicate id: " + material.id);
            }
        }
        map.trimCollection();
        REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
    }

    private final boolean brittle;
    private final int density;
    private final int hardness;
    @HarvestLevel
    private final short harvestLevel;
    private final byte id;
    private final int modElasticity;
    private final String name;
    private final int resistance;
    private final Component text;
    private final Type type;

    EvolutionMaterials(int id,
                       String name,
                       Type type,
                       @HarvestLevel int harvestLevel,
                       int density,
                       int hardness,
                       int modElasticity,
                       int resistance,
                       boolean brittle) {
        this.id = MathHelper.toByteExact(id);
        this.type = type;
        this.harvestLevel = MathHelper.toShortExact(harvestLevel);
        this.name = name;
        this.density = density;
        this.hardness = hardness;
        this.modElasticity = modElasticity;
        this.resistance = resistance;
        this.brittle = brittle;
        this.text = new TranslatableComponent("evolution.material." + name);
    }

    public static EvolutionMaterials byId(byte id) {
        return REGISTRY.getOrDefault(id, ANDESITE);
    }

    public static EvolutionMaterials getRandom(RandomGenerator random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    public double getAxeMass() {
        return this.density / 1_500.0;
    }

    public SoundEvent getBlockHitSound() {
        if (this.isMetal()) {
            return EvolutionSounds.METAL_WEAPON_HIT_BLOCK.get();
        }
        if (this.isStone()) {
            return EvolutionSounds.STONE_WEAPON_HIT_BLOCK.get();
        }
        throw new IllegalStateException("Make sound for other types!");
    }

    public int getDensity() {
        return this.density;
    }

    public double getHammerMass() {
        return this.density / 1_500.0;
    }

    public int getHardness() {
        return this.hardness;
    }

    @HarvestLevel
    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public byte getId() {
        return this.id;
    }

    public double getJavelinMass() {
        return this.density / 2_000.0;
    }

    public int getModulusOfElasticity() {
        return this.modElasticity;
    }

    public String getName() {
        return this.name;
    }

    public double getPickaxeMass() {
        return this.density / 2_000.0;
    }

    public int getResistance() {
        return this.resistance;
    }

    public double getShovelMass() {
        return this.density / 2_000.0;
    }

    public double getSwordMass() {
        return this.density / 2_000.0;
    }

    public Component getText() {
        return this.text;
    }

    public boolean isAllowedBy(PartTypes.Blade type) {
        return switch (type) {
            case NULL -> false;
            case ARMING_SWORD -> this.isMetal();
            case KNIFE -> !this.isWood();
        };

    }

    public boolean isAllowedBy(PartTypes.Guard type) {
        if (type == PartTypes.Guard.NULL) {
            return false;
        }
        return this.isMetal();
    }

    public boolean isAllowedBy(PartTypes.HalfHead type) {
        return switch (type) {
            case AXE, HAMMER -> !this.isWood();
            case PICKAXE -> this.isMetal();
            case NULL -> false;
        };
    }

    public boolean isAllowedBy(PartTypes.Handle type) {
        if (type == PartTypes.Handle.NULL) {
            return false;
        }
        return !this.isStone();
    }

    public boolean isAllowedBy(PartTypes.Head type) {
        return switch (type) {
            case NULL -> false;
            case AXE, HAMMER, HOE, SHOVEL, SPEAR -> !this.isWood();
            case MACE, PICKAXE -> this.isMetal();
        };
    }

    public boolean isAllowedBy(PartTypes.Hilt type) {
        if (type == PartTypes.Hilt.NULL) {
            return false;
        }
        return !this.isStone();
    }

    public boolean isAllowedBy(PartTypes.Pole type) {
        if (type == PartTypes.Pole.NULL) {
            return false;
        }
        return this.isWood() || this.isMetal();
    }

    public boolean isAllowedBy(PartTypes.Pommel type) {
        if (type == PartTypes.Pommel.NULL) {
            return false;
        }
        return this.isMetal();
    }

    public boolean isBrittle() {
        return this.brittle;
    }

    public boolean isMetal() {
        return this.type == Type.METAL;
    }

    public boolean isStone() {
        return this.type == Type.STONE;
    }

    public boolean isWood() {
        return this.type == Type.WOOD;
    }

    private enum Type {
        METAL,
        STONE,
        WOOD
    }
}
