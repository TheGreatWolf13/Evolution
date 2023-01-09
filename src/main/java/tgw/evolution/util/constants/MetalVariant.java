package tgw.evolution.util.constants;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMaps;
import net.minecraft.world.level.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.B2RMap;
import tgw.evolution.util.collection.B2ROpenHashMap;
import tgw.evolution.util.math.MathHelper;

public enum MetalVariant {
    COPPER(0, 8_920, HarvestLevel.LOW_METAL, 30.0F, 10.0F, 1.6f, true);

    public static final MetalVariant[] VALUES = values();
    public static final Byte2ReferenceMap<MetalVariant> REGISTRY;

    static {
        B2RMap<MetalVariant> map = new B2ROpenHashMap<>();
        for (MetalVariant variant : VALUES) {
            if (map.put(variant.id, variant) != null) {
                throw new IllegalStateException("MetalVariant " + variant + " has duplicate id: " + variant.id);
            }
        }
        map.trimCollection();
        REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
    }

    private final int density;
    private final float frictionCoef;
    private final float hardness;
    @HarvestLevel
    private final int harvestLevel;
    private final byte id;
    private final boolean oxidizes;
    private final float resistance;

    MetalVariant(int id, int density, @HarvestLevel int harvestLevel, float hardness, float resistance, float frictionCoef, boolean oxidizes) {
        this.id = MathHelper.toByteExact(id);
        this.density = density;
        this.harvestLevel = harvestLevel;
        this.hardness = hardness;
        this.resistance = resistance;
        this.frictionCoef = frictionCoef;
        this.oxidizes = oxidizes;
    }

    public boolean doesOxidize() {
        return this.oxidizes;
    }

    public Block getBlock(Oxidation oxidation) {
        switch (this) {
            case COPPER -> {
                return switch (oxidation) {
                    case NONE -> EvolutionBlocks.BLOCK_METAL_COPPER.get();
                    case EXPOSED -> EvolutionBlocks.BLOCK_METAL_COPPER_E.get();
                    case WEATHERED -> EvolutionBlocks.BLOCK_METAL_COPPER_W.get();
                    case OXIDIZED -> EvolutionBlocks.BLOCK_METAL_COPPER_O.get();
                };
            }
            default -> throw new UnregisteredFeatureException("Unregistered variant: " + this);
        }
    }

    public int getDensity() {
        return this.density;
    }

    public float getFrictionCoefficient() {
        return this.frictionCoef;
    }

    public float getHardness(Oxidation oxidation) {
        switch (this) {
            case COPPER -> {
                return this.hardness;
            }
            default -> Evolution.warn("Oxidation dependent hardness not defined for {}", this);
        }
        return this.hardness;
    }

    @HarvestLevel
    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public float getResistance(Oxidation oxidation) {
        switch (this) {
            case COPPER -> {
                return this.resistance;
            }
            default -> Evolution.warn("Oxidation dependent resistance not defined for {}", this);
        }
        return this.resistance;
    }
}
