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
                    case NONE -> Time.TICKS_PER_MONTH;
                    case EXPOSED -> 2L * Time.TICKS_PER_MONTH;
                    case WEATHERED -> 3L * Time.TICKS_PER_MONTH;
                    case OXIDIZED -> throw new IllegalStateException("Cannot oxidize beyond the Oxidized state!");
                };
            }
            default -> throw new UnregisteredFeatureException("Unregistered metal: " + metal);
        }
    }
}
