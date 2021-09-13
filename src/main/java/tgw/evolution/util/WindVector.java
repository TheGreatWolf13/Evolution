package tgw.evolution.util;

import java.util.Random;

public class WindVector {

    private int x;
    private int z;

    private static int randomize(Random rand, int value) {
        int chosen;
        switch (value) {
            case -2: {
                chosen = rand.nextInt(8);
                switch (chosen) {
                    case 0:
                    case 1:
                    case 2:
                    case 3: {
                        return -2;
                    }
                    case 4:
                    case 5:
                    case 6: {
                        return -1;
                    }
                    case 7: {
                        return 0;
                    }
                }
            }
            case -1: {
                chosen = rand.nextInt(4);
                switch (chosen) {
                    case 0: {
                        return -2;
                    }
                    case 1:
                    case 2: {
                        return -1;
                    }
                    case 3: {
                        return 0;
                    }
                }
            }
            case 0: {
                chosen = rand.nextInt(8);
                switch (chosen) {
                    case 0: {
                        return -2;
                    }
                    case 1:
                    case 2: {
                        return -1;
                    }
                    case 3:
                    case 4: {
                        return 0;
                    }
                    case 5:
                    case 6: {
                        return 1;
                    }
                    case 7: {
                        return 2;
                    }
                }
            }
            case 1: {
                chosen = rand.nextInt(4);
                switch (chosen) {
                    case 0: {
                        return 0;
                    }
                    case 1:
                    case 2: {
                        return 1;
                    }
                    case 3: {
                        return 2;
                    }
                }
            }
            case 2: {
                chosen = rand.nextInt(8);
                switch (chosen) {
                    case 0: {
                        return 0;
                    }
                    case 1:
                    case 2:
                    case 3: {
                        return 1;
                    }
                    case 4:
                    case 5:
                    case 6:
                    case 7: {
                        return 2;
                    }
                }
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

    public void randomize(Random rand) {
        this.x = randomize(rand, this.x);
        this.z = randomize(rand, this.z);
    }
}
