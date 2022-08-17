package tgw.evolution.entities.misc;

import org.jetbrains.annotations.Range;

public interface ISittableEntity {

    /**
     * @return An int from {@code 0} to {@code 100} representing the comfort of this seat.
     */
    @Range(from = 0, to = 100) int getComfort();
}
