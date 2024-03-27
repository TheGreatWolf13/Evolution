package tgw.evolution.util.constants;

import net.minecraft.world.level.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.collection.maps.B2OHashMap;
import tgw.evolution.util.collection.maps.B2OMap;
import tgw.evolution.util.math.MathHelper;

public enum MetalVariant {
    COPPER(0, 8_920, HarvestLevel.LOW_METAL, 30.0F, 10.0F, 1.6f, true);

    public static final MetalVariant[] VALUES = values();
    public static final B2OMap<MetalVariant> REGISTRY;

    static {
        B2OMap<MetalVariant> map = new B2OHashMap<>();
        for (MetalVariant variant : VALUES) {
            if (map.put(variant.id, variant) != null) {
                throw new IllegalStateException("MetalVariant " + variant + " has duplicate id: " + variant.id);
            }
        }
        map.trim();
        REGISTRY = map.view();
    }

    private final int density;
    private final float frictionCoef;
    private final float hardness;
    private final @HarvestLevel int harvestLevel;
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
                    case NONE -> EvolutionBlocks.BLOCK_METAL_COPPER;
                    case EXPOSED -> EvolutionBlocks.BLOCK_METAL_COPPER_E;
                    case WEATHERED -> EvolutionBlocks.BLOCK_METAL_COPPER_W;
                    case OXIDIZED -> EvolutionBlocks.BLOCK_METAL_COPPER_O;
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

    public @HarvestLevel int getHarvestLevel() {
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
