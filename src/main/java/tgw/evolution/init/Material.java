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
public enum Material {
    //Stone
    ANDESITE(0, "andesite", HarvestLevel.STONE, 2_565, 250, 25, 13, true),
    BASALT(1, "basalt", HarvestLevel.STONE, 2_768, 234, 25, 13, true),
    CHALK(2, "chalk", HarvestLevel.STONE, 2_499, 31, 21, 10, true),
    CHERT(3, "chert", HarvestLevel.STONE, 2_564, 234, 24, 12, true),
    CONGLOMERATE(4, "conglomerate", HarvestLevel.STONE, 2_570, 63, 23, 10, true),
    DACITE(5, "dacite", HarvestLevel.STONE, 2_402, 63, 25, 11, true),
    DIORITE(6, "diorite", HarvestLevel.STONE, 2_797, 234, 25, 13, true),
    DOLOMITE(7, "dolomite", HarvestLevel.STONE, 2_899, 156, 24, 13, true),
    GABBRO(8, "gabbro", HarvestLevel.STONE, 2_884, 250, 25, 13, true),
    GNEISS(9, "gneiss", HarvestLevel.STONE, 2_812, 250, 24, 13, true),
    GRANITE(10, "granite", HarvestLevel.STONE, 2_640, 234, 25, 13, true),
    LIMESTONE(11, "limestone", HarvestLevel.STONE, 2_484, 156, 25, 13, true),
    MARBLE(12, "marble", HarvestLevel.STONE, 2_716, 156, 25, 13, true),
    PHYLLITE(13, "phyllite", HarvestLevel.STONE, 2_575, 47, 22, 11, true),
    QUARTZITE(14, "quartzite", HarvestLevel.STONE, 2_612, 234, 24, 13, true),
    RED_SANDSTONE(15, "red_sandstone", HarvestLevel.STONE, 2_475, 234, 25, 13, true),
    SANDSTONE(16, "sandstone", HarvestLevel.STONE, 2_463, 234, 25, 13, true),
    SCHIST(17, "schist", HarvestLevel.STONE, 2_732, 188, 24, 13, true),
    SHALE(18, "shale", HarvestLevel.STONE, 2_335, 31, 24, 13, true),
    SLATE(19, "slate", HarvestLevel.STONE, 2_691, 156, 25, 13, true),
    //Metal
    BISMUTH(20, "bismuth", HarvestLevel.LOW_METAL, 9_780, 17, 32, 4, true),
    COPPER(21, "copper", HarvestLevel.COPPER, 8_920, 50, 110, 210, false),
    GOLD(22, "gold", HarvestLevel.LOW_METAL, 19_300, 25, 77, 120, false),
    IRON(23, "iron", HarvestLevel.IRON, 7_874, 55, 200, 540, false),
    LEAD(24, "lead", HarvestLevel.LOW_METAL, 11_340, 5, 14, 18, false),
    SILVER(25, "silver", HarvestLevel.LOW_METAL, 10_490, 25, 76, 140, false),
    TIN(26, "tin", HarvestLevel.LOW_METAL, 7_265, 5, 44, 220, false),
    ZINC(27, "zinc", HarvestLevel.LOW_METAL, 7_140, 30, 97, 37, true),
    //Wood
    WOOD(28, "wood", HarvestLevel.HAND, 1_500, 4, 10, 105, false);

    public static final Material[] VALUES = values();
    private static final Byte2ReferenceMap<Material> REGISTRY;

    static {
        B2RMap<Material> map = new B2ROpenHashMap<>();
        for (Material material : VALUES) {
            if (map.put(material.id, material) != null) {
                throw new IllegalStateException("Material " + material + " has duplicate id: " + material.id);
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

    Material(int id, String name, @HarvestLevel int harvestLevel, int density, int hardness, int modElasticity, int resistance, boolean brittle) {
        this.id = MathHelper.toByteExact(id);
        this.harvestLevel = MathHelper.toShortExact(harvestLevel);
        this.name = name;
        this.density = density;
        this.hardness = hardness;
        this.modElasticity = modElasticity;
        this.resistance = resistance;
        this.brittle = brittle;
        this.text = new TranslatableComponent("evolution.material." + name);
    }

    public static Material byId(byte id) {
        return REGISTRY.getOrDefault(id, ANDESITE);
    }

    public static Material getRandom(RandomGenerator random) {
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
            case ARMING_SWORD -> switch (this) {
                case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                        QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, WOOD -> false;
                case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
            };
            case KNIFE -> true;
        };

    }

    public boolean isAllowedBy(PartTypes.Guard type) {
        if (type == PartTypes.Guard.NULL) {
            return false;
        }
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, WOOD -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.HalfHead type) {
        return switch (type) {
            case AXE, HAMMER -> this != WOOD;
            case PICKAXE -> switch (this) {
                case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                        QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, WOOD -> false;
                case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
            };
            case NULL -> false;
        };
    }

    public boolean isAllowedBy(PartTypes.Handle type) {
        if (type == PartTypes.Handle.NULL) {
            return false;
        }
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Head type) {
        return switch (type) {
            case NULL -> false;
            case AXE, HAMMER, HOE, SHOVEL, SPEAR -> this != WOOD;
            case MACE, PICKAXE -> switch (this) {
                case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                        QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, WOOD -> false;
                case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
            };
        };
    }

    public boolean isAllowedBy(PartTypes.Hilt type) {
        if (type == PartTypes.Hilt.NULL) {
            return false;
        }
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Pole type) {
        if (type == PartTypes.Pole.NULL) {
            return false;
        }
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Pommel type) {
        if (type == PartTypes.Pommel.NULL) {
            return false;
        }
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, WOOD -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
        };
    }

    public boolean isBrittle() {
        return this.brittle;
    }

    public boolean isMetal() {
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, WOOD -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
        };
    }

    public boolean isStone() {
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE -> true;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> false;
        };
    }

    public boolean isWood() {
        return switch (this) {
            case ANDESITE, BASALT, CHALK, CHERT, CONGLOMERATE, DACITE, DIORITE, DOLOMITE, GABBRO, GNEISS, GRANITE, LIMESTONE, MARBLE, PHYLLITE,
                    QUARTZITE, RED_SANDSTONE, SANDSTONE, SCHIST, SHALE, SLATE, BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> false;
            case WOOD -> true;
        };
    }
}
