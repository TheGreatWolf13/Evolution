package tgw.evolution.util;

import net.minecraft.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;

public enum MetalVariant {
    COPPER(8_920, HarvestLevel.COPPER, 30.0F, 10.0F, 1.6f, true);
    private final int density;
    private final float frictionCoef;
    private final float hardness;
    private final int harvestLevel;
    private final boolean oxidizes;
    private final float resistance;

    MetalVariant(int density, int harvestLevel, float hardness, float resistance, float frictionCoef, boolean oxidizes) {
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
            case COPPER: {
                switch (oxidation) {
                    case NONE: {
                        return EvolutionBlocks.BLOCK_METAL_COPPER.get();
                    }
                    case EXPOSED: {
                        return EvolutionBlocks.BLOCK_METAL_COPPER_EXP.get();
                    }
                    case WEATHERED: {
                        return EvolutionBlocks.BLOCK_METAL_COPPER_WEAT.get();
                    }
                    case OXIDIZED: {
                        return EvolutionBlocks.BLOCK_METAL_COPPER_OXID.get();
                    }
                    default: {
                        throw new UnregisteredFeatureException("Unregistered Oxidation State: " + oxidation);
                    }
                }
            }
            default: {
                throw new UnregisteredFeatureException("Unregistered variant: " + this);
            }
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
            case COPPER: {
                return this.hardness;
            }
            default: {
                Evolution.LOGGER.warn("Oxidation dependent hardness not defined for {}", this);
                break;
            }
        }
        return this.hardness;
    }

    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public float getResistance(Oxidation oxidation) {
        switch (this) {
            case COPPER: {
                return this.resistance;
            }
            default: {
                Evolution.LOGGER.warn("Oxidation dependent resistance not defined for {}", this);
                break;
            }
        }
        return this.resistance;
    }
}
