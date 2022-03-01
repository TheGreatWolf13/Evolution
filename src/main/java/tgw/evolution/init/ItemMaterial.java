package tgw.evolution.init;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.intellij.lang.annotations.MagicConstant;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.util.constants.HarvestLevel;

/**
 * Values for metals and alloys are real. Values for rocks are tweaked for balance, but a stronger rock is still stronger than a weaker one.
 */
public enum ItemMaterial {
    //Stone
    STONE_ANDESITE("andesite", HarvestLevel.STONE, 2_565, 250, 25, 13, true),
    STONE_BASALT("basalt", HarvestLevel.STONE, 2_768, 234, 25, 13, true),
    STONE_CHALK("chalk", HarvestLevel.STONE, 2_499, 31, 21, 10, true),
    STONE_CHERT("chert", HarvestLevel.STONE, 2_564, 234, 24, 12, true),
    STONE_CONGLOMERATE("conglomerate", HarvestLevel.STONE, 2_570, 63, 23, 10, true),
    STONE_DACITE("dacite", HarvestLevel.STONE, 2_402, 63, 25, 11, true),
    STONE_DIORITE("diorite", HarvestLevel.STONE, 2_797, 234, 25, 13, true),
    STONE_DOLOMITE("dolomite", HarvestLevel.STONE, 2_899, 156, 24, 13, true),
    STONE_GABBRO("gabbro", HarvestLevel.STONE, 2_884, 250, 25, 13, true),
    STONE_GNEISS("gneiss", HarvestLevel.STONE, 2_812, 250, 24, 13, true),
    STONE_GRANITE("granite", HarvestLevel.STONE, 2_640, 234, 25, 13, true),
    STONE_LIMESTONE("limestone", HarvestLevel.STONE, 2_484, 156, 25, 13, true),
    STONE_MARBLE("marble", HarvestLevel.STONE, 2_716, 156, 25, 13, true),
    STONE_PHYLLITE("phyllite", HarvestLevel.STONE, 2_575, 47, 22, 11, true),
    STONE_QUARTZITE("quartzite", HarvestLevel.STONE, 2_612, 234, 24, 13, true),
    STONE_RED_SANDSTONE("red_sandstone", HarvestLevel.STONE, 2_475, 234, 25, 13, true),
    STONE_SANDSTONE("sandstone", HarvestLevel.STONE, 2_463, 234, 25, 13, true),
    STONE_SCHIST("schist", HarvestLevel.STONE, 2_732, 188, 24, 13, true),
    STONE_SHALE("shale", HarvestLevel.STONE, 2_335, 31, 24, 13, true),
    STONE_SLATE("slate", HarvestLevel.STONE, 2_691, 156, 25, 13, true),
    //Metal
    BISMUTH("bismuth", HarvestLevel.LOW_METAL, 9_780, 17, 32, 4, true),
    COPPER("copper", HarvestLevel.COPPER, 8_920, 50, 110, 210, false),
    GOLD("gold", HarvestLevel.LOW_METAL, 19_300, 25, 77, 120, false),
    IRON("iron", HarvestLevel.IRON, 7_874, 55, 200, 540, false),
    LEAD("lead", HarvestLevel.LOW_METAL, 11_340, 5, 14, 18, false),
    SILVER("silver", HarvestLevel.LOW_METAL, 10_490, 25, 76, 140, false),
    TIN("tin", HarvestLevel.LOW_METAL, 7_265, 5, 44, 220, false),
    ZINC("zinc", HarvestLevel.LOW_METAL, 7_140, 30, 97, 37, true),
    //Wood
    WOOD("wood", HarvestLevel.HAND, 1_500, 4, 10, 105, false);

    public static final ItemMaterial[] VALUES = values();
    private static final Object2ReferenceMap<String, ItemMaterial> REGISTRY;

    static {
        Object2ReferenceMap<String, ItemMaterial> map = new Object2ReferenceOpenHashMap<>();
        for (ItemMaterial material : VALUES) {
            map.put(material.name, material);
        }
        REGISTRY = Object2ReferenceMaps.unmodifiable(map);
    }

    private final boolean brittle;
    private final int density;
    private final int hardness;
    @MagicConstant(valuesFromClass = HarvestLevel.class)
    private final short harvestLevel;
    private final int modElasticity;
    private final String name;
    private final int resistance;
    private final Component text;

    ItemMaterial(String name,
                 @MagicConstant(valuesFromClass = HarvestLevel.class) int harvestLevel,
                 int density,
                 int hardness,
                 int modElasticity,
                 int resistance,
                 boolean brittle) {
        this.harvestLevel = (short) harvestLevel;
        this.name = name;
        this.density = density;
        this.hardness = hardness;
        this.modElasticity = modElasticity;
        this.resistance = resistance;
        this.brittle = brittle;
        this.text = new TranslatableComponent("evolution.material." + name);
    }

    public static ItemMaterial byName(String name) {
        return REGISTRY.getOrDefault(name, STONE_ANDESITE);
    }

    public double getAxeMass() {
        return this.density / 1_500.0;
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

    @MagicConstant(valuesFromClass = HarvestLevel.class)
    public int getHarvestLevel() {
        return this.harvestLevel;
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
        if (type == PartTypes.Blade.NULL) {
            return false;
        }
        return switch (this) {
            case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                    STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE, STONE_RED_SANDSTONE,
                    STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE, WOOD -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Guard type) {
        if (type == PartTypes.Guard.NULL) {
            return false;
        }
        return switch (this) {
            case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                    STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE, STONE_RED_SANDSTONE,
                    STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE, WOOD -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.HalfHead type) {
        return switch (type) {
            case AXE, HAMMER -> this != WOOD;
            case PICKAXE -> switch (this) {
                case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                        STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE,
                        STONE_RED_SANDSTONE, STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE, WOOD -> false;
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
            case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                    STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE, STONE_RED_SANDSTONE,
                    STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Head type) {
        return switch (type) {
            case NULL -> false;
            case AXE, HAMMER, HOE, SHOVEL, SPEAR -> this != WOOD;
            case MACE, PICKAXE -> switch (this) {
                case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                        STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE,
                        STONE_RED_SANDSTONE, STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE, WOOD -> false;
                case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
            };
        };
    }

    public boolean isAllowedBy(PartTypes.Hilt type) {
        if (type == PartTypes.Hilt.NULL) {
            return false;
        }
        return switch (this) {
            case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                    STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE, STONE_RED_SANDSTONE,
                    STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Pole type) {
        if (type == PartTypes.Pole.NULL) {
            return false;
        }
        return switch (this) {
            case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                    STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE, STONE_RED_SANDSTONE,
                    STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC, WOOD -> true;
        };
    }

    public boolean isAllowedBy(PartTypes.Pommel type) {
        if (type == PartTypes.Pommel.NULL) {
            return false;
        }
        return switch (this) {
            case STONE_ANDESITE, STONE_BASALT, STONE_CHALK, STONE_CHERT, STONE_CONGLOMERATE, STONE_DACITE, STONE_DIORITE, STONE_DOLOMITE,
                    STONE_GABBRO, STONE_GNEISS, STONE_GRANITE, STONE_LIMESTONE, STONE_MARBLE, STONE_PHYLLITE, STONE_QUARTZITE, STONE_RED_SANDSTONE,
                    STONE_SANDSTONE, STONE_SCHIST, STONE_SHALE, STONE_SLATE, WOOD -> false;
            case BISMUTH, COPPER, GOLD, IRON, LEAD, SILVER, TIN, ZINC -> true;
        };
    }

    public boolean isBrittle() {
        return this.brittle;
    }
}
