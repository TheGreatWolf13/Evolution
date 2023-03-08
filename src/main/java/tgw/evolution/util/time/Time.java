package tgw.evolution.util.time;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.util.math.Metric;

public class Time {
    public static final Time START_TIME = new Time(6);
    public static final int STARTING_YEAR = 1_000;
    public static final int TICKS_PER_DAY = 36_000;
    public static final int DAYS_PER_MONTH = 21;
    public static final int MONTHS_PER_YEAR = 12;
    public static final int TICKS_PER_HOUR = TICKS_PER_DAY / 24;
    public static final int TICKS_PER_MONTH = DAYS_PER_MONTH * TICKS_PER_DAY;
    public static final int DAYS_PER_YEAR = MONTHS_PER_YEAR * DAYS_PER_MONTH;
    public static final int TICKS_PER_YEAR = DAYS_PER_YEAR * TICKS_PER_DAY;
    /**
     * How long it takes for Mercury to orbit the Sun. In real life, this value is 87.9691 days, which is then multiplied by
     * {@link Time#DAYS_PER_YEAR}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int MERCURIAN_YEAR = (int) (87.969_1 * DAYS_PER_YEAR / 365.25 * TICKS_PER_DAY);
    /**
     * How long it takes for Venus to orbit the Sun. In real life, this value is 224.7 days, which is then multiplied by {@link Time#DAYS_PER_YEAR}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int VENUSIAN_YEAR = (int) (224.7 * DAYS_PER_YEAR / 365.25 * TICKS_PER_DAY);
    /**
     * How long it takes for Mars to orbit the Sun. In real life, this value is 686.971 days, which is then multiplied by {@link Time#DAYS_PER_YEAR}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int MARTIAN_YEAR = (int) (686.971 * DAYS_PER_YEAR / 365.25 * TICKS_PER_DAY);
    /**
     * How long it takes for Jupiter to orbit the Sun. In real life, this value is 4_331.572 days, which is then multiplied by
     * {@link Time#DAYS_PER_YEAR}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int JUPITERIAN_YEAR = (int) (4_331.572 * DAYS_PER_YEAR / 365.25 * TICKS_PER_DAY);
    /**
     * How long it takes for Saturn to orbit the Sun. In real life, this value is 10_759.22 days, which is then multiplied by
     * {@link Time#DAYS_PER_YEAR}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int SATURNIAN_YEAR = (int) (10_759.22 * DAYS_PER_YEAR / 365.25 * TICKS_PER_DAY);
    /**
     * A sidereal day presents the time it takes for the Earth to spin around its axis by 360º relative to the background stars.
     * It is equivalent to 23h 56min 04s.
     */
    public static final int SIDEREAL_DAY_IN_TICKS = (int) (TICKS_PER_DAY * (23 + 56 / 60.0 + 4 / (60.0 * 60.0)) / 24.0);

    private final int hour;
    private final int minute;

    public Time() {
        this(0, 0);
    }

    public Time(int hour) {
        this(hour, 0);
    }

    public Time(int hour, int minute) {
        if (hour > 23 || hour < 0) {
            throw new IllegalStateException("Invalid hour: " + hour);
        }
        if (minute > 59 || minute < 0) {
            throw new IllegalStateException("Invalid minute: " + minute);
        }
        this.hour = hour;
        this.minute = minute;
    }

    public static Time fromTicks(long ticks) {
        ticks += 6L * TICKS_PER_HOUR;
        long h = ticks % TICKS_PER_DAY / TICKS_PER_HOUR;
        int m = (int) ((ticks % TICKS_PER_HOUR) / (TICKS_PER_HOUR / 60.0));
        return new Time((int) h, m);
    }

    public static String getFormattedTime(int timeInTicks) {
        if (timeInTicks < TICKS_PER_HOUR) {
            return timeInTicks + " ticks";
        }
        if (timeInTicks < TICKS_PER_DAY) {
            return timeInTicks / (float) TICKS_PER_HOUR + " hours";
        }
        if (timeInTicks < TICKS_PER_YEAR) {
            return timeInTicks / (float) TICKS_PER_DAY + " days";
        }
        return timeInTicks / (float) TICKS_PER_YEAR + " years";
    }

    public static long roundToLastFullHour(long ticks) {
        ticks /= TICKS_PER_HOUR;
        ticks *= TICKS_PER_HOUR;
        return ticks;
    }

    public Component getDisplayName() {
        int hour12format = this.hour == 0 ? 12 : this.hour > 12 ? this.hour - 12 : this.hour;
        String amPm = this.hour >= 12 ? "PM" : "AM";
        return new TranslatableComponent("evolution.calendar.time", this.hour, Metric.INT_2.format(this.minute), hour12format, amPm);
    }

    @Override
    public String toString() {
        return Metric.INT_2.format(this.hour) + "h" + Metric.INT_2.format(this.minute);
    }

    public int toTicks() {
        return this.hour * TICKS_PER_HOUR + this.minute * TICKS_PER_HOUR / 60;
    }
}
