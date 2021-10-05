package tgw.evolution.util;

public enum Oxidation {
    NONE,
    EXPOSED,
    WEATHERED,
    OXIDIZED;

    public Oxidation getNextStage() {
        switch (this) {
            case NONE: {
                return EXPOSED;
            }
            case EXPOSED: {
                return WEATHERED;
            }
            case WEATHERED: {
                return OXIDIZED;
            }
            case OXIDIZED: {
                throw new IllegalStateException("Cannot oxidize beyond the Oxidized state!");
            }
        }
        throw new UnregisteredFeatureException("Unregistered Oxidation State: " + this);
    }

    public long getTimeForNextStage(MetalVariant metal) {
        switch (metal) {
            case COPPER: {
                switch (this) {
                    case NONE: {
                        return Time.MONTH_IN_TICKS;
                    }
                    case EXPOSED: {
                        return 2L * Time.MONTH_IN_TICKS;
                    }
                    case WEATHERED: {
                        return 3L * Time.MONTH_IN_TICKS;
                    }
                    case OXIDIZED: {
                        throw new IllegalStateException("Cannot oxidize beyond the Oxidized state!");
                    }
                    default: {
                        throw new UnregisteredFeatureException("Unregistered Oxidation State: " + this);
                    }
                }
            }
            default: {
                throw new UnregisteredFeatureException("Unregistered metal: " + metal);
            }
        }
    }
}
