package tgw.evolution.util;

public class FullDate {
    private final Date date;
    private final Hour hour;

    public FullDate(Date date, Hour hour) {
        this.date = date;
        this.hour = hour;
        if (date.equals(Date.STARTING_DATE) && hour.toTicks() < 6_000) {
            throw new IllegalStateException("Time in day 1 starts at 06h and not midnight!");
        }
    }

    public FullDate(long ticks) {
        this(new Date(ticks), Hour.fromTicks(ticks));
    }

    public String getFullString() {
        return this.hour + " " + this.date.getFullString();
    }

    @Override
    public String toString() {
        return this.hour + " " + this.date;
    }

    public long toTicks() {
        return this.date.toTicks() + this.hour.toTicks();
    }
}
