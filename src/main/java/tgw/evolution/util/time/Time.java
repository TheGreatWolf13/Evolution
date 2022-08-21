package tgw.evolution.util.time;

import tgw.evolution.util.math.Metric;

public class Time {
    public static final Time START_TIME = new Time(6);
    public static final int HOUR_IN_TICKS = 1_000;
    public static final int DAY_IN_TICKS = 24_000;
    public static final int DAYS_IN_A_MONTH = 21;
    public static final int MONTH_IN_TICKS = DAYS_IN_A_MONTH * DAY_IN_TICKS;
    public static final int MONTHS_IN_A_YEAR = 12;
    public static final int DAYS_IN_A_YEAR = MONTHS_IN_A_YEAR * DAYS_IN_A_MONTH;
    public static final int YEAR_IN_TICKS = DAYS_IN_A_YEAR * DAY_IN_TICKS;
    /**
     * How long it takes for Mercury to orbit the Sun. In real life, this value is 87.9691 days, which is then multiplied by
     * {@link Time#YEAR_IN_TICKS}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int MERCURIAN_YEAR = (int) (87.969_1 * DAYS_IN_A_YEAR / 365.25) * DAY_IN_TICKS;
    /**
     * How long it takes for Venus to orbit the Sun. In real life, this value is 224.7 days, which is then multiplied by {@link Time#YEAR_IN_TICKS}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int VENUSIAN_YEAR = (int) (224.7 * DAYS_IN_A_YEAR / 365.25) * DAY_IN_TICKS;
    /**
     * How long it takes for Mars to orbit the Sun. In real life, this value is 686.971 days, which is then multiplied by {@link Time#YEAR_IN_TICKS}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int MARTIAN_YEAR = (int) (686.971 * DAYS_IN_A_YEAR / 365.25) * DAY_IN_TICKS;
    /**
     * How long it takes for Jupiter to orbit the Sun. In real life, this value is 4_331.572 days, which is then multiplied by
     * {@link Time#YEAR_IN_TICKS}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int JUPITERIAN_YEAR = (int) (4_331.572 * DAYS_IN_A_YEAR / 365.25) * DAY_IN_TICKS;
    /**
     * How long it takes for Saturn to orbit the Sun. In real life, this value is 10_759.22 days, which is then multiplied by
     * {@link Time#YEAR_IN_TICKS}
     * and divided by 365.25 (a real life, Earth year).
     */
    public static final int SATURNIAN_YEAR = (int) (10_759.22 * DAYS_IN_A_YEAR / 365.25) * DAY_IN_TICKS;
    /**
     * A sidereal day presents the time it takes for the Earth to spin around its axis by 360º relative to the background stars.
     * It is equivalent to 23h 56min 04s.
     */
    public static final int SIDEREAL_DAY_IN_TICKS = (int) (DAY_IN_TICKS * (23 + 56 / 60.0 + 4 / (60.0 * 60.0)) / 24.0);
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
        ticks += 6_000;
        long h = ticks % DAY_IN_TICKS / HOUR_IN_TICKS;
        int m = (int) ((ticks % HOUR_IN_TICKS) / (HOUR_IN_TICKS / 60.0));
        return new Time((int) h, m);
    }

    public static String get24HourTime(int timeInTicks) {
        timeInTicks %= 24_000;
        int hour = timeInTicks / 1_000 + 6;
        if (hour >= 24) {
            hour -= 24;
        }
        int minute = (int) ((timeInTicks % 1_000) / 16.6);
        return hour + "h" + minute;
    }

    public static String getFormattedTime(int timeInTicks) {
        if (timeInTicks < HOUR_IN_TICKS) {
            return timeInTicks + " ticks";
        }
        if (timeInTicks < DAY_IN_TICKS) {
            return timeInTicks / (float) HOUR_IN_TICKS + " hours";
        }
        if (timeInTicks < YEAR_IN_TICKS) {
            return timeInTicks / (float) DAY_IN_TICKS + " days";
        }
        return timeInTicks / (float) YEAR_IN_TICKS + " years";
    }

    public static long roundToLastFullHour(long ticks) {
        ticks /= 1_000;
        ticks *= 1_000;
        return ticks;
    }

    @Override
    public String toString() {
        return Metric.INT_2.format(this.hour) + "h" + Metric.INT_2.format(this.minute);
    }

    public int toTicks() {
        return this.hour * HOUR_IN_TICKS + this.minute * HOUR_IN_TICKS / 60;
    }
}
