package tgw.evolution.capabilities.temperature;

public final class TemperatureClient {

    public static final TemperatureClient CLIENT_INSTANCE = new TemperatureClient();
    private int currentMaxComfort = 25;
    private int currentMinComfort = 15;
    private int currentTemperature = 20;

    private TemperatureClient() {
    }

    public int getCurrentMaxComfort() {
        return this.currentMaxComfort;
    }

    public int getCurrentMinComfort() {
        return this.currentMinComfort;
    }

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
