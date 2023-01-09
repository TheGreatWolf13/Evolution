package tgw.evolution.util.constants;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMaps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.capabilities.modular.part.IPart;
import tgw.evolution.capabilities.modular.part.IPartType;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.IVariant;
import tgw.evolution.init.Material;
import tgw.evolution.items.modular.part.ItemPart;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.B2RMap;
import tgw.evolution.util.collection.B2ROpenHashMap;

import java.util.Arrays;

import static tgw.evolution.util.constants.RockType.*;

public enum RockVariant implements IVariant {
    ANDESITE(0, IGNEOUS_EXTRUSIVE, "andesite", 2_565, 40_000_000),
    BASALT(1, IGNEOUS_EXTRUSIVE, "basalt", 2_768, 30_000_000),
    CHALK(2, SEDIMENTARY, "chalk", 2_499, 4_000_000),
    CHERT(3, SEDIMENTARY, "chert", 2_564, 9_000_000),
    CONGLOMERATE(4, SEDIMENTARY, "conglomerate", 2_570, 6_000_000),
    DACITE(5, IGNEOUS_EXTRUSIVE, "dacite", 2_402, 30_000_000),
    DIORITE(6, IGNEOUS_INTRUSIVE, "diorite", 2_797, 18_000_000),
    DOLOMITE(7, SEDIMENTARY, "dolomite", 2_899, 10_000_000),
    GABBRO(8, IGNEOUS_INTRUSIVE, "gabbro", 2_884, 60_000_000),
    GNEISS(9, METAMORPHIC, "gneiss", 2_812, 10_000_000),
    GRANITE(10, IGNEOUS_INTRUSIVE, "granite", 2_640, 20_000_000),
    LIMESTONE(11, SEDIMENTARY, "limestone", 2_484, 20_000_000),
    MARBLE(12, METAMORPHIC, "marble", 2_716, 20_000_000),
    PHYLLITE(13, METAMORPHIC, "phyllite", 2_575, 15_000_000),
    QUARTZITE(14, METAMORPHIC, "quartzite", 2_612, 7_500_000),
    RED_SANDSTONE(15, SEDIMENTARY, "red_sandstone", 2_475, 8_000_000),
    SANDSTONE(16, SEDIMENTARY, "sandstone", 2_463, 8_000_000),
    SCHIST(17, METAMORPHIC, "schist", 2_732, 5_000_000),
    SHALE(18, SEDIMENTARY, "shale", 2_335, 5_000_000),
    SLATE(19, METAMORPHIC, "slate", 2_691, 10_000_000),
    PEAT(20, null, "peat", 1_156, 0),
    CLAY(21, null, "clay", 2_067, 0);

    public static final RockVariant[] VALUES = values();
    public static final RockVariant[] VALUES_STONE = Arrays.stream(VALUES).filter(v -> v != PEAT && v != CLAY).toArray(RockVariant[]::new);
    private static final Byte2ReferenceMap<RockVariant> REGISTRY;

    static {
        B2RMap<RockVariant> map = new B2ROpenHashMap<>();
        for (RockVariant variant : VALUES) {
            if (map.put(variant.id, variant) != null) {
                throw new IllegalStateException("RockVariant " + variant + " has duplicate id: " + variant.id);
            }
        }
        map.trimCollection();
        REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
    }

    private final double density;
    private final byte id;
    private final String name;
    private final @Nullable RockType rockType;
    private final int shearStrength;

    RockVariant(int id, @Nullable RockType rockType, String name, double densityInkg, int shearStrengthInPa) {
        this.id = (byte) id;
        this.rockType = rockType;
        this.name = name;
        this.density = densityInkg;
        this.shearStrength = shearStrengthInPa;
    }

    public static RockVariant fromId(byte id) {
        RockVariant variant = REGISTRY.get(id);
        if (variant == null) {
            throw new UnregisteredFeatureException("Unregistered variant for id: " + id);
        }
        return variant;
    }

    @Contract(pure = true)
    public Block fromEnumVanillaRep(VanillaRockVariant vanilla) {
        return switch (vanilla) {
            case DIRT -> this.getDirt();
            case COBBLESTONE -> this.getCobble();
            case GRAVEL -> this.getGravel();
            case GRASS -> this.getGrass();
            case SAND -> this.getSand();
            case STONE -> this.getStone();
            case STONE_BRICKS -> this.getStoneBricks();
        };
    }

    public Block getCobble() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a cobble type!");
            default -> EvolutionBlocks.COBBLESTONES.get(this).get();
        };
    }

    public Block getDirt() {
        return switch (this) {
            case CLAY -> EvolutionBlocks.CLAY.get();
            case PEAT -> EvolutionBlocks.PEAT.get();
            default -> EvolutionBlocks.DIRTS.get(this).get();
        };
    }

    public Block getDryGrass() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a dry grass type!");
            default -> EvolutionBlocks.DRY_GRASSES.get(this).get();
        };
    }

    public Block getGrass() {
        return EvolutionBlocks.GRASSES.get(this).get();
    }

    public Block getGravel() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a gravel type!");
            default -> EvolutionBlocks.GRAVELS.get(this).get();
        };
    }

    public byte getId() {
        return this.id;
    }

    @Contract(pure = true, value = "_ -> new")
    public ItemStack getKnappedStack(KnappingRecipe knapping) {
        return switch (knapping) {
            case NULL -> new ItemStack(this.getRock());
            case AXE -> this.getPart(PartTypes.Head.AXE);
            case HAMMER -> this.getPart(PartTypes.Head.HAMMER);
            case HOE -> this.getPart(PartTypes.Head.HOE);
            case KNIFE -> this.getPart(PartTypes.Blade.KNIFE);
            case SHOVEL -> this.getPart(PartTypes.Head.SHOVEL);
            case SPEAR -> this.getPart(PartTypes.Head.SPEAR);
        };
    }

    public Block getKnapping() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a knapping type!");
            default -> EvolutionBlocks.KNAPPING_BLOCKS.get(this).get();
        };
    }

    public double getMass() {
        return this.density;
    }

    private Material getMaterial() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant is not a valid material!");
            case ANDESITE -> Material.ANDESITE;
            case BASALT -> Material.BASALT;
            case CHALK -> Material.CHALK;
            case CHERT -> Material.CHERT;
            case CONGLOMERATE -> Material.CONGLOMERATE;
            case DACITE -> Material.DACITE;
            case DIORITE -> Material.DIORITE;
            case DOLOMITE -> Material.DOLOMITE;
            case GABBRO -> Material.GABBRO;
            case GNEISS -> Material.GNEISS;
            case GRANITE -> Material.GRANITE;
            case LIMESTONE -> Material.LIMESTONE;
            case MARBLE -> Material.MARBLE;
            case PHYLLITE -> Material.PHYLLITE;
            case QUARTZITE -> Material.QUARTZITE;
            case RED_SANDSTONE -> Material.RED_SANDSTONE;
            case SANDSTONE -> Material.SANDSTONE;
            case SCHIST -> Material.SCHIST;
            case SHALE -> Material.SHALE;
            case SLATE -> Material.SLATE;
        };
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Contract(pure = true, value = "_ -> new")
    private <T extends IPartType<T, I, P>, I extends ItemPart<T, I, P>, P extends IPart<T, I, P>> ItemStack getPart(T type) {
        return type.partItem().newStack(type, this.getMaterial());
    }

    public Block getPolishedStone() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a polished stone type!");
            default -> EvolutionBlocks.POLISHED_STONES.get(this).get();
        };
    }

    public Block getRock() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a rock type!");
            default -> EvolutionBlocks.ROCKS.get(this).get();
        };
    }

    public RockType getRockType() {
        if (this.rockType == null) {
            throw new IllegalStateException("RockVariant " + this + " does not have a RockType");
        }
        return this.rockType;
    }

    public Block getSand() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a sand type!");
            default -> EvolutionBlocks.SANDS.get(this).get();
        };
    }

    public int getShearStrength() {
        return this.shearStrength;
    }

    public Block getStone() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a stone type!");
            default -> EvolutionBlocks.STONES.get(this).get();
        };
    }

    public Block getStoneBricks() {
        return switch (this) {
            case CLAY, PEAT -> throw new IllegalStateException("This variant does not have a stone bricks type!");
            default -> EvolutionBlocks.STONE_BRICKS.get(this).get();
        };
    }
}
