package tgw.evolution.capabilities.player;

import org.jetbrains.annotations.Contract;

public final class TemperatureClient {

    public static final TemperatureClient INSTANCE = new TemperatureClient();
    private int currentMaxComfort = 25;
    private int currentMinComfort = 15;
    private int currentTemperature = 20;

    private TemperatureClient() {
    }

    @Contract(pure = true)
    public int getCurrentMaxComfort() {
        return this.currentMaxComfort;
    }

    @Contract(pure = true)
    public int getCurrentMinComfort() {
        return this.currentMinComfort;
    }

    @Contract(pure = true)
    public int getCurrentTemperature() {
        return this.currentTemperature;
    }

    public void setCurrentMaxComfort(int currentMaxComfort) {
        this.currentMaxComfort = currentMaxComfort;
    }

    public void setCurrentMinComfort(int currentMinComfort) {
        this.currentMinComfort = currentMinComfort;
    }

    public void setCurrentTemperature(int currentTemperature) {
        this.currentTemperature = currentTemperature;
    }
}
