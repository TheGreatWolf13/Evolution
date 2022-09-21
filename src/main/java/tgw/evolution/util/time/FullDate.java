package tgw.evolution.util.time;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

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

    public Component getDisplayName() {
        return new TranslatableComponent("evolution.calendar.dateTime", this.date.getDisplayName(), this.hour.getDisplayName());
    }

    public Component getShortDisplayName() {
        return new TranslatableComponent("evolution.calendar.dateTimeShort", this.date.getShortDisplayName(), this.hour.getDisplayName());
    }

    @Override
    public String toString() {
        return this.hour + " " + this.date;
    }

    public long toTicks() {
        return this.date.toTicks() + this.hour.toTicks();
    }
}
