package tgw.evolution.blocks;

import org.jetbrains.annotations.Range;

public interface ISittableBlock {

    @Range(from = 0, to = 100) int getComfort();

    double getYOffset();

    float getZOffset();
}
