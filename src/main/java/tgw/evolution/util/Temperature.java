package tgw.evolution.util;

public abstract class Temperature {

    public static int celsiusToKelvin(int temperatureInCelsius) {
        return temperatureInCelsius - 273;
    }

    public static int kelvinToCelsius(int temperatureInKelvin) {
        return temperatureInKelvin + 273;
    }
}
