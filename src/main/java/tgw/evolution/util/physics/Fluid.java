package tgw.evolution.util.physics;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

public enum Fluid {
    VACUUM(0, null),
    AIR(1.225 * SI.KILOGRAM / SI.CUBIC_METER, null),
    LAVA(3_100 * SI.KILOGRAM / SI.CUBIC_METER, FluidTags.LAVA),
    WATER(997 * SI.KILOGRAM / SI.CUBIC_METER, FluidTags.WATER);

    private final double density;
    private final @Nullable TagKey<net.minecraft.world.level.material.Fluid> tag;

    Fluid(double density, @Nullable TagKey<net.minecraft.world.level.material.Fluid> tag) {
        this.density = density;
        this.tag = tag;
    }

    public double density() {
        return this.density;
    }

    public @Nullable TagKey<net.minecraft.world.level.material.Fluid> tag() {
        return this.tag;
    }
}
