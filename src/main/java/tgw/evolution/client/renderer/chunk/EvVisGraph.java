package tgw.evolution.client.renderer.chunk;

import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import tgw.evolution.util.collection.IArrayFIFOQueue;
import tgw.evolution.util.math.DirectionUtil;

import java.util.BitSet;

public class EvVisGraph {
    private static final int[] INDEX_OF_EDGES = Util.make(new int[1_352], a -> {
        int k = 0;
        for (int x = 0; x < 16; ++x) {
            for (int y = 0; y < 16; ++y) {
                for (int z = 0; z < 16; ++z) {
                    if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                        a[k++] = getIndex(x, y, z);
                    }
                }
            }
        }
    });
    private final BitSet bitSet = new BitSet(4_096);
    private final IntPriorityQueue queue = new IArrayFIFOQueue();
    private int empty = 4_096;

    private static byte addEdges(int index, byte faces) {
        int i = index & 15;
        if (i == 0) {
            faces |= 16;
        }
        else if (i == 15) {
            faces |= 32;
        }
        int j = index >> 8 & 15;
        if (j == 0) {
            faces |= 1;
        }
        else if (j == 15) {
            faces |= 2;
        }
        int k = index >> 4 & 15;
        if (k == 0) {
            faces |= 4;
        }
        else if (k == 15) {
            faces |= 8;
        }
        return faces;
    }

    private static int getIndex(int x, int y, int z) {
        return x | y << 8 | z << 4;
    }

    private static int getNeighborIndexAtFace(int index, Direction face) {
        return switch (face) {
            case DOWN -> {
                if ((index >> 8 & 15) == 0) {
                    yield -1;
                }
                yield index - 256;
            }
            case UP -> {
                if ((index >> 8 & 15) == 15) {
                    yield -1;
                }
                yield index + 256;
            }
            case NORTH -> {
                if ((index >> 4 & 15) == 0) {
                    yield -1;
                }
                yield index - 16;
            }
            case SOUTH -> {
                if ((index >> 4 & 15) == 15) {
                    yield -1;
                }
                yield index + 16;
            }
            case WEST -> {
                if ((index & 15) == 0) {
                    yield -1;
                }
                yield index - 1;
            }
            case EAST -> {
                if ((index & 15) == 15) {
                    yield -1;
                }
                yield index + 1;
            }
        };
    }

    private byte floodFill(int index) {
        byte set = 0;
        this.queue.clear();
        this.queue.enqueue(index);
        this.bitSet.set(index, true);
        while (!this.queue.isEmpty()) {
            int i = this.queue.dequeueInt();
            set = addEdges(i, set);
            for (Direction direction : DirectionUtil.ALL) {
                int j = getNeighborIndexAtFace(i, direction);
                if (j >= 0 && !this.bitSet.get(j)) {
                    this.bitSet.set(j, true);
                    this.queue.enqueue(j);
                }
            }
        }
        return set;
    }

    public void reset() {
        this.bitSet.clear();
        this.queue.clear();
        this.empty = 4_096;
    }

    public long resolve() {
        if (this.empty == 0) {
            return 0;
        }
        if (4_096 - this.empty < 256) {
            return 0b111111_111111_111111_111111_111111_111111L;
        }
        byte directions = 0;
        for (int i : INDEX_OF_EDGES) {
            if (!this.bitSet.get(i)) {
                directions |= this.floodFill(i);
            }
        }
        long visibilitySet = 0;
        for (Direction face : DirectionUtil.ALL) {
            if ((directions & 1 << face.ordinal()) != 0) {
                for (Direction other : DirectionUtil.ALL) {
                    if ((directions & 1 << other.ordinal()) != 0) {
                        visibilitySet |= 1L << face.ordinal() + other.ordinal() * 6;
                        visibilitySet |= 1L << other.ordinal() + face.ordinal() * 6;
                    }
                }
            }
        }
        return visibilitySet;
    }

    public void setOpaque(int x, int y, int z) {
        this.bitSet.set(getIndex(x, y, z), true);
        --this.empty;
    }
}
