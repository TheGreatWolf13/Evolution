package tgw.evolution.util;

public class Hour {
    public static final Hour START_TIME = new Hour(6);
    private final int hour;
    private final int minute;

    public Hour() {
        this(0, 0);
    }

    public Hour(int hour) {
        this(hour, 0);
    }

    public Hour(int hour, int minute) {
        if (hour > 23 || hour < 0) {
            throw new IllegalStateException("Invalid hour: " + hour);
        }
        if (minute > 59 || minute < 0) {
            throw new IllegalStateException("Invalid minute: " + minute);
        }
        this.hour = hour;
        this.minute = minute;
    }

    public static Hour fromTicks(long ticks) {
        ticks += 6_000;
        long h = ticks % Time.DAY_IN_TICKS / Time.HOUR_IN_TICKS;
        int m = (int) ((ticks % Time.HOUR_IN_TICKS) / (Time.HOUR_IN_TICKS / 60.0));
        return new Hour((int) h, m);
    }

    @Override
    public String toString() {
        return String.format("%02d", this.hour) + "h" + String.format("%02d", this.minute);
    }

    public int toTicks() {
        return this.hour * Time.HOUR_IN_TICKS + this.minute * Time.HOUR_IN_TICKS / 60;
    }
}
