package tgw.evolution.util.constants;

import tgw.evolution.util.UnregisteredFeatureException;
import tgw.evolution.util.time.Time;

public enum Oxidation {
    NONE,
    EXPOSED,
    WEATHERED,
    OXIDIZED;

    public Oxidation getNextStage() {
        return switch (this) {
            case NONE -> EXPOSED;
            case EXPOSED -> WEATHERED;
            case WEATHERED -> OXIDIZED;
            case OXIDIZED -> throw new IllegalStateException("Cannot oxidize beyond the Oxidized state!");
        };
    }

    public long getTimeForNextStage(MetalVariant metal) {
        switch (metal) {
            case COPPER -> {
                return switch (this) {
                    case NONE -> Time.MONTH_IN_TICKS;
                    case EXPOSED -> 2L * Time.MONTH_IN_TICKS;
                    case WEATHERED -> 3L * Time.MONTH_IN_TICKS;
                    case OXIDIZED -> throw new IllegalStateException("Cannot oxidize beyond the Oxidized state!");
                };
            }
            default -> throw new UnregisteredFeatureException("Unregistered metal: " + metal);
        }
    }
}
