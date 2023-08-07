package tgw.evolution.util;

import net.minecraft.core.Direction;
import tgw.evolution.util.math.DirectionList;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Arrays;

public class DirectionHolder {

    private final int[] values = new int[6];
    private boolean hasOnlyLowest;
    private int lowest;

    public DirectionHolder() {
        this.reset();
    }

    public int computeList(boolean allValid) {
        int list = 0;
        if (allValid) {
            for (int i = 0; i < 6; ++i) {
                int value = this.values[i];
                if (value != -1) {
                    list = DirectionList.add(list, DirectionUtil.ALL[i]);
                }
            }
            return list;
        }
        if (this.hasOnlyLowest) {
            for (int i = 0; i < 6; ++i) {
                if (this.values[i] == this.lowest) {
                    list = DirectionList.add(list, DirectionUtil.ALL[i]);
                }
            }
            return list;
        }
        for (int i = 0; i < 6; ++i) {
            int value = this.values[i];
            if (value != -1 || value != this.lowest) {
                list = DirectionList.add(list, DirectionUtil.ALL[i]);
            }
        }
        return list;
    }

    public String debugPrint() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            int value = this.values[i];
            if (value != -1) {
                builder.append(DirectionUtil.ALL[i]).append(": ").append(value).append(" / ");
            }
        }
        return builder.toString();
    }

    public int getLowestAtm() {
        return this.lowest;
    }

    public boolean isInvalidOrBlockedAbove() {
        int value = this.values[Direction.UP.ordinal()];
        return value == -1 || value == 31;
    }

    public void reset() {
        Arrays.fill(this.values, -1);
        this.lowest = 31;
        this.hasOnlyLowest = true;
    }

    public void store(Direction dir, int atm) {
        this.values[dir.ordinal()] = atm;
        if (atm < this.lowest) {
            if (this.lowest != 31) {
                this.hasOnlyLowest = false;
            }
            this.lowest = atm;
        }
        else if (atm > this.lowest && atm <= 31) {
            this.hasOnlyLowest = false;
        }
    }
}
