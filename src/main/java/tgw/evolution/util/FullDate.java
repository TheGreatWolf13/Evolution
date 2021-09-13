package tgw.evolution.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class FullDate {
    private final Date date;
    private final Time hour;

    public FullDate(Date date, Time hour) {
        this.date = date;
        this.hour = hour;
        if (date.equals(Date.STARTING_DATE) && hour.toTicks() < 6_000) {
            throw new IllegalStateException("Time in day 1 starts at 06h and not midnight!");
        }
    }

    public FullDate(long ticks) {
        this(new Date(ticks), Time.fromTicks(ticks));
    }

    public ITextComponent getDisplayName() {
        return new StringTextComponent(this.hour + " ").append(this.date.getDisplayName());
    }

    @Override
    public String toString() {
        return this.hour + " " + this.date;
    }

    public long toTicks() {
        return this.date.toTicks() + this.hour.toTicks();
    }
}
