package tgw.evolution.util.earth;

import org.jetbrains.annotations.Nullable;

public enum ClimateZone {

    POLAR_ARTIC(Region.POLAR),
    TEMPERATE_NORTH(Region.TEMPERATE),
    TROPICAL(Region.TROPICAL),
    TEMPERATE_SOUTH(Region.TEMPERATE),
    POLAR_ANTARTIC(Region.POLAR);

    private final Region region;

    ClimateZone(Region region) {
        this.region = region;
    }

    public static ClimateZone fromLatitude(float latitude) {
        if (latitude > 90 - EarthHelper.ECLIPTIC_INCLINATION) {
            return POLAR_ARTIC;
        }
        if (latitude < -90 + EarthHelper.ECLIPTIC_INCLINATION) {
            return POLAR_ANTARTIC;
        }
        if (latitude < EarthHelper.ECLIPTIC_INCLINATION && latitude > -EarthHelper.ECLIPTIC_INCLINATION) {
            return TROPICAL;
        }
        if (latitude > 0) {
            return TEMPERATE_NORTH;
        }
        return TEMPERATE_SOUTH;
    }

    public static ClimateZone fromZPos(double zPos) {
        if (zPos > EarthHelper.POLAR_CIRCLE) {
            return POLAR_ANTARTIC;
        }
        if (zPos < -EarthHelper.POLAR_CIRCLE) {
            return POLAR_ARTIC;
        }
        if (zPos < EarthHelper.TROPIC && zPos > -EarthHelper.TROPIC) {
            return TROPICAL;
        }
        if (zPos > 0) {
            return TEMPERATE_SOUTH;
        }
        return TEMPERATE_NORTH;
    }

    public Region getRegion() {
        return this.region;
    }

    public boolean isPolar() {
        return this.region == Region.POLAR;
    }

    public boolean isTemperate() {
        return this.region == Region.TEMPERATE;
    }

    public boolean isTropical() {
        return this.region == Region.TROPICAL;
    }

    public enum Region {
        POLAR,
        TEMPERATE,
        TROPICAL;

        @Nullable
        public static Region byId(int id) {
            return switch (id) {
                case 1 -> POLAR;
                case 2 -> TEMPERATE;
                case 3 -> TROPICAL;
                default -> null;
            };
        }

        public int getId() {
            return switch (this) {
                case POLAR -> 1;
                case TEMPERATE -> 2;
                case TROPICAL -> 3;
            };
        }
    }
}
