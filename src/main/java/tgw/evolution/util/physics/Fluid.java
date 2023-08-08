package tgw.evolution.util.physics;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.UnregisteredFeatureException;

public enum Fluid {
    VACUUM(0, 0, null),
    AIR(1.225 * SI.KILOGRAM / SI.CUBIC_METER, 1.825e-5 * SI.PASCAL * SI.SECOND, null),
    LAVA(3_100 * SI.KILOGRAM / SI.CUBIC_METER, 5e6 * SI.PASCAL * SI.SECOND, FluidTags.LAVA),
    WATER(997 * SI.KILOGRAM / SI.CUBIC_METER, 1.001_6e-3 * SI.PASCAL * SI.SECOND, FluidTags.WATER);

    private final double density;
    private final @Nullable TagKey<net.minecraft.world.level.material.Fluid> tag;
    private final double viscosity;

    Fluid(double density, double viscosity, @Nullable TagKey<net.minecraft.world.level.material.Fluid> tag) {
        this.density = density;
        this.viscosity = viscosity;
        this.tag = tag;
    }

    public static Fluid fromTag(TagKey<net.minecraft.world.level.material.Fluid> tag) {
        if (tag == FluidTags.WATER) {
            return WATER;
        }
        if (tag == FluidTags.LAVA) {
            return LAVA;
        }
        throw new UnregisteredFeatureException("Unknown fluid tag: " + tag);
    }

    public double density() {
        return this.density;
    }

    public @Nullable TagKey<net.minecraft.world.level.material.Fluid> tag() {
        return this.tag;
    }

    public double viscosity() {
        return this.viscosity;
    }
}
