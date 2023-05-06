package tgw.evolution.util;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.patches.ILevelChunkSectionPatch;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.physics.ClimateZone;
import tgw.evolution.util.physics.EarthHelper;
import tgw.evolution.util.time.Time;

/**
 * To calculate the local temperature: <br>
 * <br>
 * 1. Calculate the maximum and minimum temperatures for that latitude, given the current time; <br>
 * 2. Modify the maximum and minimum values by the local biome modifiers; <br>
 * 3. Apply the diurnal high-low factor to determine the current temperature now: <br>
 * &emsp;a. If the place is outside ({@code atmFactor <= 15}), it is dependant on the {@code delayedDiurnalInsolation}; <br>
 * &emsp;b. Else if the place is inside ({@code atmFactor == 31}), it is {@code 0.5f}; <br>
 * &emsp;c. Else (the place has {@code atmFactor > 15 && atmFactor < 31}), it is {@code 0.25f + 0.5f * delayedDiurnalInsolation}; <br>
 * 4. Apply the altitude factor; <br>
 * 5. Apply local modifications (such as blocks, items, entities, etc). <br>
 */
public final class Temperature implements ILocked {

    private static final ThreadLocal<Temperature> CACHE = ThreadLocal.withInitial(Temperature::new);
    private static final byte[] MAX_DAYS_WITHOUT_SUN = new byte[48];
    private static final byte[] POLAR_NIGHT_START = new byte[48];

    static {
        //Max Days Without Sun
        MAX_DAYS_WITHOUT_SUN[0] = 126;
        int index = 1;
        for (byte i = 125; i >= 115; i -= 2) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        assert index == 7;
        for (byte i = 115; i >= 99; i -= 2) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        assert index == 16;
        for (byte i = 99; i >= 69; i -= 2) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        assert index == 32;
        for (byte i = 65; i >= 57; i -= 2) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        assert index == 37;
        for (byte i = 53; i >= 51; i -= 2) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        assert index == 39;
        for (byte i = 47; i >= 45; i -= 2) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        assert index == 41;
        for (byte i = 41; i >= 29; i -= 4) {
            MAX_DAYS_WITHOUT_SUN[index++] = i;
        }
        MAX_DAYS_WITHOUT_SUN[45] = 23;
        MAX_DAYS_WITHOUT_SUN[46] = 17;
        MAX_DAYS_WITHOUT_SUN[47] = 1;
        //Polar Night Start (South Pole)
        POLAR_NIGHT_START[0] = -1;
        index = 1;
        for (byte i = 0; i <= 5; i++) {
            POLAR_NIGHT_START[index++] = i;
        }
        for (byte i = 5; i <= 13; i++) {
            POLAR_NIGHT_START[index++] = i;
        }
        for (byte i = 13; i <= 28; i++) {
            POLAR_NIGHT_START[index++] = i;
        }
        for (byte i = 30; i <= 34; i++) {
            POLAR_NIGHT_START[index++] = i;
        }
        POLAR_NIGHT_START[index++] = 36;
        POLAR_NIGHT_START[index++] = 37;
        POLAR_NIGHT_START[index++] = 39;
        POLAR_NIGHT_START[index++] = 40;
        POLAR_NIGHT_START[index++] = 42;
        POLAR_NIGHT_START[index++] = 44;
        POLAR_NIGHT_START[index++] = 46;
        POLAR_NIGHT_START[index++] = 48;
        POLAR_NIGHT_START[index++] = 51;
        POLAR_NIGHT_START[index++] = 54;
        POLAR_NIGHT_START[index] = 62;
    }

    private float cachedAnnualHighLowFactor = Float.NaN;
    private float cachedAnnualInsolation = Float.NaN;
    private float cachedDeclination = Float.NaN;
    private float cachedFrostAmount = Float.NaN;
    private float cachedLatitude = Float.NaN;
    private int cachedSunrise = Integer.MIN_VALUE;
    private @Nullable ServerLevel level;
    private boolean locked;
    private long t;
    private double x;
    private double y;
    private double z;

    private Temperature() {
    }

    public static double C2F(double celsius) {
        return C2FRelative(celsius) + 32;
    }

    public static double C2FRelative(double celsius) {
        return 9 / 5.0 * celsius;
    }

    /**
     * @return Converts a temperature given in degrees Celsius to Kelvin.
     */
    public static double C2K(double celsius) {
        return celsius + 273.15;
    }

    public static double C2R(double celsius) {
        return C2RRelative(celsius) + 491.67;
    }

    public static double C2RRelative(double celsius) {
        return 9 / 5.0 * celsius;
    }

    /**
     * @return Converts a temperature given in Kelvin to degrees Celsius.
     */
    public static double K2C(double kelvin) {
        return kelvin - 273.15;
    }

    /**
     * @return [ºC]
     */
    public static double getBaseTemperatureForRegion(@Nullable ClimateZone.Region region) {
        if (region == null) {
            return 0;
        }
        return switch (region) {
            case POLAR -> 15;
            case TEMPERATE -> 20;
            case TROPICAL -> 25;
        };
    }

    /**
     * @return A color in a 32 bit, ARGB value. The 8 most significant bits represent alpha, while the least significant bits represent blue.
     * This color is equivalent to the black body radiation, given a temperature in Kelvin.
     */
    public static int getBlackBodySpectrumColor(double kelvin) {
        kelvin /= 100;
        int red;
        int green;
        int blue;
        int alpha;
        if (kelvin <= 8) {
            red = 0;
            green = 0;
            blue = 0;
            alpha = 0;
        }
        else if (kelvin <= 9) {
            red = (int) (255 * (kelvin - 8));
            green = 0;
            blue = 0;
            alpha = (int) (191 * (kelvin - 8));
        }
        else if (kelvin <= 10) {
            red = 255;
            green = (int) (68 * (kelvin - 9));
            blue = 0;
            alpha = 191;
        }
        else if (kelvin <= 19) {
            red = 255;
            green = (int) (99.470_802_586_1 * Math.log(kelvin) - 161.119_568_166_1);
            green = MathHelper.clamp(green, 0, 255);
            blue = 0;
            alpha = (int) (191 + 64 * (kelvin - 10) / 9.0);
        }
        else if (kelvin <= 66) {
            red = 255;
            green = (int) (99.470_802_586_1 * Math.log(kelvin) - 161.119_568_166_1);
            green = MathHelper.clamp(green, 0, 255);
            blue = (int) (138.517_731_223_1 * Math.log(kelvin - 10) - 305.044_792_730_7);
            blue = MathHelper.clamp(blue, 0, 255);
            alpha = 255;
        }
        else {
            red = (int) (329.698_727_446 * Math.pow(kelvin - 60, -0.133_204_759_2));
            red = MathHelper.clamp(red, 0, 255);
            green = (int) (288.122_169_528_3 * Math.pow(kelvin - 60, -0.075_514_849_2));
            green = MathHelper.clamp(green, 0, 255);
            blue = 255;
            alpha = 255;
        }
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static Temperature getInstance(ServerLevel level, int x, int y, int z, long t) {
        return getInstance(level, x + 0.5, y + 0.5, z + 0.5, t);
    }

    public static Temperature getInstance(ServerLevel level, double x, double y, double z, long t) {
        Temperature temperature = CACHE.get();
        assert !temperature.isLocked() : "The local instance of Temperature is locked, you probably forgot to unlock it! Use it with " +
                                         "try-with-resources to unlock automatically.";
        temperature.x = x;
        temperature.y = y;
        temperature.z = z;
        temperature.t = t;
        temperature.level = level;
        temperature.lock();
        return temperature;
    }

    /**
     * @return [ºC]
     */
    public static short getMaxComfortForRegion(@Nullable ClimateZone.Region region) {
        if (region == null) {
            return 0;
        }
        return (short) switch (region) {
            case POLAR -> 20;
            case TEMPERATE -> 25;
            case TROPICAL -> 30;
        };
    }

    /**
     * @return [ºC]
     */
    public static short getMinComfortForRegion(@Nullable ClimateZone.Region region) {
        if (region == null) {
            return 0;
        }
        return (short) switch (region) {
            case POLAR -> 10;
            case TEMPERATE -> 15;
            case TROPICAL -> 20;
        };
    }

    @Override
    public void close() {
        this.locked = false;
        this.level = null;
        this.cachedLatitude = Float.NaN;
        this.cachedSunrise = Integer.MIN_VALUE;
        this.cachedDeclination = Float.NaN;
        this.cachedAnnualHighLowFactor = Float.NaN;
        this.cachedAnnualInsolation = Float.NaN;
        this.cachedFrostAmount = Float.NaN;
    }

    private int getAfternoon() {
        int sunrise = this.getSunrise();
        int delta = Time.TICKS_PER_DAY / 2 - sunrise;
        return (Time.TICKS_PER_DAY + delta) / 2;
    }

    public double getAmbientBasedTemperature() {
        //Step 3: Apply diurnal high-low factor
        double min = this.getBiomeBasedMinTemperature();
        double max = this.getBiomeBasedMaxTemperature();
        if (max == min) {
            return max;
        }
        if (min > max) {
            Evolution.warn("Min temperature is greater than max, some Biome multipliers are probably wrong or exaggerated!");
            return (min + max) / 2.0;
        }
        return min + this.getDiurnalHighLowFactor() * (max - min);
    }

    private float getAnnualHighLowFactor() {
        if (Float.isNaN(this.cachedAnnualHighLowFactor)) {
            float latitudeFactor = (90.0f - Math.abs(this.getLatitude())) / 90.0f;
            this.cachedAnnualHighLowFactor = (2 * this.getAnnualInsolation() + latitudeFactor) / 3;
        }
        return this.cachedAnnualHighLowFactor;
    }

    public float getAnnualInsolation() {
        if (Float.isNaN(this.cachedAnnualInsolation)) {
            double absDeltaDecl = Math.abs(this.getDeclination() - this.getLatitude());
            this.cachedAnnualInsolation = MathHelper.sinDeg((float) (90 - Math.min(absDeltaDecl, 90)));
        }
        return this.cachedAnnualInsolation;
    }

    public double getBiomeBasedMaxTemperature() {
        //Step 2: modify max by latitude using the local biome
        //TODO
        return this.getLatitudeBasedMaxTemperature();
    }

    public double getBiomeBasedMinTemperature() {
        //Step 2: modify min by latitude using the local biome
        //TODO
        return this.getLatitudeBasedMinTemperature();
    }

    private float getDeclination() {
        if (Float.isNaN(this.cachedDeclination)) {
            this.cachedDeclination = EarthHelper.sunSeasonalDeclination(this.t);
        }
        return this.cachedDeclination;
    }

    public float getDiurnalHighLowFactor() {
        assert this.level != null;
        int x = Mth.floor(this.x);
        int z = Mth.floor(this.z);
        LevelChunk chunk = this.level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        if (chunk.isEmpty()) {
            //Outside
            return this.getSolarHighLowFactor();
        }
        if (this.y > this.level.getMaxBuildHeight()) {
            //Outside
            return this.getSolarHighLowFactor();
        }
        int y = Mth.floor(this.y);
        int index = chunk.getSectionIndex(y);
        ILevelChunkSectionPatch section = (ILevelChunkSectionPatch) chunk.getSection(index);
        int atm = section.getAtmStorage().get(x & 15, y & 15, z & 15);
        if (atm == 31) {
            //Inside
            return 0.5f;
        }
        if (15 < atm && atm < 31) {
            //Semi-Inside
            return 0.25f + 0.5f * this.getSolarHighLowFactor();
        }
        //Outside
        return this.getSolarHighLowFactor();
    }

    private double getFrostAmount() {
        if (Float.isNaN(this.cachedFrostAmount)) {
            if (Math.abs(this.getLatitude()) >= 90 - EarthHelper.ECLIPTIC_INCLINATION) {
                if (this.isInPolarNight()) {
                    int timeWithoutSun = this.timeWithoutSun();
                    //-10 degree for every month spent without sun, capped at 3 months or -30 degrees
                    this.cachedFrostAmount = 10.0f / Time.TICKS_PER_MONTH * Math.min(timeWithoutSun, 3 * Time.TICKS_PER_MONTH);
                }
                else {
                    //-20 degree for every month spent since sun came back, capped at 2 months or -40 degrees
                    int recoveryTime = this.maxTimeWithoutSun() / 3 - this.timeSinceSunCameBack();
                    assert recoveryTime <= 2 * Time.TICKS_PER_MONTH : "Recovery time greater than 2 months: " + recoveryTime;
                    if (recoveryTime > 0) {
                        this.cachedFrostAmount = 20.0f / Time.TICKS_PER_MONTH * recoveryTime;
                    }
                    else {
                        this.cachedFrostAmount = 0;
                    }
                }
            }
            else {
                this.cachedFrostAmount = 0;
            }
        }
        return this.cachedFrostAmount;
    }

    /**
     * @return [K].
     */
    public double getHeightBasedTemperature() {
        //Step 4: apply altitude factor
        //Temperature will fluctuate above y=80 and below y=60
        //IRL, temperature at 11km is -56.5ºC
        //Temperature at 1 km is -56.5ºC
        //Temperature at y = -64 is 1ºC
        if (60 <= this.y && this.y <= 80) {
            return this.getAmbientBasedTemperature();
        }
        if (this.y >= 1_000) {
            return -56.5 + 273.15;
        }
        if (this.y < -64) {
            return 1 + 273.15;
        }
        if (this.y >= 80) {
            double t = MathHelper.relativize(this.y, 80, 1_000);
            return t * (-56.5 + 273.15) + (1 - t) * this.getAmbientBasedTemperature();
        }
        double t = MathHelper.relativize(this.y, -64, 60);
        return t * this.getAmbientBasedTemperature() + (1 - t) * (1 + 273.15);
    }

    public float getLatitude() {
        if (Float.isNaN(this.cachedLatitude)) {
            this.cachedLatitude = EarthHelper.calculateLatitude(this.z);
        }
        return this.cachedLatitude;
    }

    public double getLatitudeBasedMaxTemperature() {
        return this.getTemperatureForLatitude(-10 + 273.15, 34 + 273.15);
    }

    public double getLatitudeBasedMinTemperature() {
        return this.getTemperatureForLatitude(-20 + 273.15, 24 + 273.15);
    }

    /**
     * @return [K].
     */
    public double getLocalTemperature() {
        double localTemp = this.getHeightBasedTemperature();
        //Step 5: apply local factors
        //TODO
        return localTemp;
    }

    public float getSolarHighLowFactor() {
        if (this.isInPolarNight()) {
            return 0.0f;
        }
        if (this.isInPolarDay()) {
            return 1.0f;
        }
        long timeInDay = this.t + 6 * Time.TICKS_PER_HOUR;
        if (timeInDay > Time.TICKS_PER_DAY) {
            timeInDay %= Time.TICKS_PER_DAY;
        }
        int sunrise = this.getSunrise();
        if (timeInDay == sunrise) {
            return 0.0f;
        }
        int afternoon = this.getAfternoon();
        if (this.t % 200 == 0) {
            Evolution.info("Sunrise is at {}", Time.fromTicks(sunrise - 6L * Time.TICKS_PER_HOUR));
            Evolution.info("Afternoon is at {}", Time.fromTicks(afternoon - 6L * Time.TICKS_PER_HOUR));
        }
        if (timeInDay == afternoon) {
            return 1.0f;
        }
        if (sunrise < timeInDay && timeInDay < afternoon) {
            return MathHelper.relativize(timeInDay, sunrise, afternoon);
        }
        if (timeInDay < sunrise) {
            timeInDay += Time.TICKS_PER_DAY;
        }
        return 1.0f - MathHelper.relativize(timeInDay, afternoon, sunrise + Time.TICKS_PER_DAY);
    }

    private int getSunrise() {
        if (this.cachedSunrise == Integer.MIN_VALUE) {
            float decl = this.getDeclination();
            float latitude = this.getLatitude();
            double cDiff = MathHelper.cosDeg(latitude - decl);
            double cSum = MathHelper.cosDeg(latitude + decl);
            double arg = (cDiff - cSum) / (cDiff + cSum);
            double angle = MathHelper.arcSin(arg);
            if (Double.isNaN(angle)) {
                return arg < 0 ? -1 : -2;
            }
            int daylightTime = (int) (Time.TICKS_PER_DAY / 2.0 / Math.PI * (Math.PI + 2 * angle));
            this.cachedSunrise = (Time.TICKS_PER_DAY - daylightTime) / 2;
        }
        return this.cachedSunrise;
    }

    /**
     * @return [K]
     */
    private double getTemperatureForLatitude(double minTemp, double maxTemp) {
        double temp = minTemp + this.getAnnualHighLowFactor() * (maxTemp - minTemp);
        return temp - this.getFrostAmount();
    }

    private boolean isInPolarDay() {
        int sunrise = this.getSunrise();
        return sunrise == -2 || sunrise == 0;
    }

    private boolean isInPolarNight() {
        int sunrise = this.getSunrise();
        return sunrise == -1 || sunrise == 12 * Time.TICKS_PER_HOUR;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void lock() {
        this.locked = true;
    }

    private int maxTimeWithoutSun() {
        double latitude = Math.abs(this.getLatitude());
        if (latitude < 90 - EarthHelper.ECLIPTIC_INCLINATION) {
            return 0;
        }
        double factor = 47 * (1 - MathHelper.relativize(latitude, 90 - EarthHelper.ECLIPTIC_INCLINATION, 90));
        int index = (int) factor;
        if (index == factor) {
            return MAX_DAYS_WITHOUT_SUN[index] * Time.TICKS_PER_DAY;
        }
        return (int) (Mth.lerp(factor - index, MAX_DAYS_WITHOUT_SUN[index], MAX_DAYS_WITHOUT_SUN[index + 1]) * Time.TICKS_PER_DAY);
    }

    private int timeSinceSunCameBack() {
        if (this.getAnnualInsolation() < 0) {
            return 0;
        }
        double latitude = this.getLatitude();
        double factor = 47 * (1 - MathHelper.relativize(Math.abs(latitude), 90 - EarthHelper.ECLIPTIC_INCLINATION, 90));
        int index = (int) factor;
        long polarNightEnd;
        if (index == factor) {
            polarNightEnd = (POLAR_NIGHT_START[index] + MAX_DAYS_WITHOUT_SUN[index]) * Time.TICKS_PER_DAY;
        }
        else {
            double start = Mth.lerp(factor - index, POLAR_NIGHT_START[index], POLAR_NIGHT_START[index + 1]);
            double length = Mth.lerp(factor - index, MAX_DAYS_WITHOUT_SUN[index], MAX_DAYS_WITHOUT_SUN[index + 1]);
            polarNightEnd = (long) ((start + length) * Time.TICKS_PER_DAY);
        }
        if (latitude > 0) {
            polarNightEnd += Time.TICKS_PER_YEAR / 2;
        }
        long t = this.t;
        if (t >= Time.TICKS_PER_YEAR) {
            t %= Time.TICKS_PER_YEAR;
        }
        long timeSinceSunCameBack = t - polarNightEnd;
        if (timeSinceSunCameBack < 0) {
            timeSinceSunCameBack += Time.TICKS_PER_YEAR;
        }
        return (int) timeSinceSunCameBack;
    }

    private int timeWithoutSun() {
        if (this.getAnnualInsolation() > 0) {
            return 0;
        }
        double latitude = this.getLatitude();
        double factor = 47 * (1 - MathHelper.relativize(Math.abs(latitude), 90 - EarthHelper.ECLIPTIC_INCLINATION, 90));
        int index = (int) factor;
        long polarNightStart;
        if (index == factor) {
            polarNightStart = POLAR_NIGHT_START[index] * Time.TICKS_PER_DAY;
        }
        else {
            polarNightStart = (long) (Mth.lerp(factor - index, POLAR_NIGHT_START[index], POLAR_NIGHT_START[index + 1]) * Time.TICKS_PER_DAY);
        }
        if (latitude > 0) {
            polarNightStart += Time.TICKS_PER_YEAR / 2;
        }
        long t = this.t;
        if (t >= Time.TICKS_PER_YEAR) {
            t %= Time.TICKS_PER_YEAR;
        }
        if (polarNightStart > t) {
            return 0;
        }
        return (int) (t - polarNightStart);
    }
}
