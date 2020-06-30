package tgw.evolution.util;

public class FullDate {
	private final Date date;
	private final Hour hour;
	
	public FullDate(Date date, Hour hour) {
		this.date = date;
		this.hour = hour;
		if (date.equals(Date.STARTING_DATE) && hour.toTicks() < 6000) {
			throw new IllegalStateException("Time in day 1 starts at 06h and not midnight!");
		}
	}
	
	public FullDate(int ticks) {
		this(new Date(ticks), Hour.fromTicks(ticks));
	}
	
	public int toTicks() {
		return this.date.toTicks() + this.hour.toTicks();
	}
	
	@Override
	public String toString() {
		return this.hour + " " + this.date;
	}
	
	public String getFullString() {
		return this.hour + " " + this.date.getFullString();
	}
}
