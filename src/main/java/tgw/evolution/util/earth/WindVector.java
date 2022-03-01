package tgw.evolution.util.earth;

import java.util.random.RandomGenerator;

public class WindVector {

    private int x;
    private int z;

    private static int randomize(RandomGenerator rand, int value) {
        int chosen;
        switch (value) {
            case -2 -> {
                chosen = rand.nextInt(8);
                return switch (chosen) {
                    case 0, 1, 2, 3 -> -2;
                    case 4, 5, 6 -> -1;
                    case 7 -> 0;
                    default -> throw new IllegalStateException("Unexpected value: " + chosen);
                };
            }
            case -1 -> {
                chosen = rand.nextInt(4);
                return switch (chosen) {
                    case 0 -> -2;
                    case 1, 2 -> -1;
                    case 3 -> 0;
                    default -> throw new IllegalStateException("Unexpected value: " + chosen);
                };
            }
            case 0 -> {
                chosen = rand.nextInt(8);
                return switch (chosen) {
                    case 0 -> -2;
                    case 1, 2 -> -1;
                    case 3, 4 -> 0;
                    case 5, 6 -> 1;
                    case 7 -> 2;
                    default -> throw new IllegalStateException("Unexpected value: " + chosen);
                };
            }
            case 1 -> {
                chosen = rand.nextInt(4);
                return switch (chosen) {
                    case 0 -> 0;
                    case 1, 2 -> 1;
                    case 3 -> 2;
                    default -> throw new IllegalStateException("Unexpected value: " + chosen);
                };
            }
            case 2 -> {
                chosen = rand.nextInt(8);
                return switch (chosen) {
                    case 0 -> 0;
                    case 1, 2, 3 -> 1;
                    case 4, 5, 6, 7 -> 2;
                    default -> throw new IllegalStateException("Unexpected value: " + chosen);
                };
            }
        }
        return 0;
    }

    public int getWindX() {
        return this.x;
    }

    public int getWindZ() {
        return this.z;
    }

    public void randomize(RandomGenerator rand) {
        this.x = randomize(rand, this.x);
        this.z = randomize(rand, this.z);
    }
}
