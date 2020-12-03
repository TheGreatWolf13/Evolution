package tgw.evolution.util;

public final class Time {
    public static final int HOUR_IN_TICKS = 1_000;
    public static final int DAY_IN_TICKS = 24_000;
    public static final int DAYS_IN_A_MONTH = 21;
    public static final int MONTH_IN_TICKS = DAYS_IN_A_MONTH * DAY_IN_TICKS;
    public static final int MONTHS_IN_A_YEAR = 12;
    public static final int DAYS_IN_A_YEAR = MONTHS_IN_A_YEAR * DAYS_IN_A_MONTH;
    public static final int YEAR_IN_TICKS = DAYS_IN_A_YEAR * DAY_IN_TICKS;

    private Time() {
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
}
