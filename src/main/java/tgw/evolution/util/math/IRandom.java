package tgw.evolution.util.math;

import java.util.random.RandomGenerator;

public interface IRandom extends RandomGenerator {

    void setSeed(long seed);

    <T extends IRandom> T setSeedAndReturn(long l);
}
