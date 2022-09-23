package tgw.evolution.util.time;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.util.math.Metric;

public class Date {

    public static final Date STARTING_DATE = new Date(Month.JUNE, 1, Time.STARTING_YEAR);
    public static final int DAYS_SINCE_MARCH_EQUINOX = 2 * Time.DAYS_PER_MONTH + 1;
    private final int day;
    private final Month month;
    private final int year;

    public Date(int year, Month month, int day) {
        if (day > Time.DAYS_PER_MONTH || day < 1) {
            throw new IllegalStateException("Invalid day: " + day);
        }
        this.year = year;
        this.month = month;
        this.day = day;
        if (this.isBefore(STARTING_DATE)) {
            throw new IllegalStateException("Date cannot be before starting date " +
                                            STARTING_DATE +
                                            ": day = " +
                                            this.day +
                                            " month = " +
                                            this.month +
                                            " year = " +
                                            this.year);
        }
    }

    private Date(Month month, int day, int year) {
        if (day > Time.DAYS_PER_MONTH || day < 1) {
            throw new IllegalStateException("Invalid day: " + day);
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public Date(long ticks) {
        ticks += 6L * Time.TICKS_PER_HOUR;
        long y = ticks / Time.TICKS_PER_YEAR;
        ticks -= Time.TICKS_PER_YEAR * y;
        long m = ticks / Time.TICKS_PER_MONTH;
        ticks -= Time.TICKS_PER_MONTH * m;
        long d = ticks / Time.TICKS_PER_DAY;
        d += STARTING_DATE.day;
        m += STARTING_DATE.month.numerical;
        y += STARTING_DATE.year;
        if (d > Time.DAYS_PER_MONTH) {
            d -= Time.DAYS_PER_MONTH;
            m++;
        }
        if (m > Time.MONTHS_PER_YEAR) {
            m -= Time.MONTHS_PER_YEAR;
            y++;
        }
        this.year = (int) y;
        this.month = Month.byNumerical((int) m);
        this.day = (int) d;
    }

    public Date add(int days, int months, int years) {
        int d = this.day + days;
        int m = this.month.getNumerical() + months;
        int y = this.year + years;
        while (d > Time.DAYS_PER_MONTH) {
            m++;
            d -= Time.DAYS_PER_MONTH;
        }
        while (d < 1) {
            m--;
            d += Time.DAYS_PER_MONTH;
        }
        while (m > 12) {
            y++;
            m -= 12;
        }
        while (m < 1) {
            y--;
            m += 12;
        }
        return new Date(y, Month.byNumerical(m), d);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Date date)) {
            return false;
        }
        return this.year == date.year && this.month == date.month && this.day == date.day;
    }

    public int getDay() {
        return this.day;
    }

    public Component getDayDisplayName() {
        return new TranslatableComponent("evolution.calendar.day." + this.day, this.day);
    }

    public Component getDisplayName() {
        return new TranslatableComponent("evolution.calendar.date", this.getDayDisplayName(), this.month.getDisplayName(), this.year);
    }

    public Month getMonth() {
        return this.month;
    }

    public Component getShortDisplayName() {
        return new TranslatableComponent("evolution.calendar.dateShort", Metric.INT_2.format(this.day), Metric.INT_2.format(this.month.numerical),
                                         this.year);
    }

    public int getYear() {
        return this.year;
    }

    @Override
    public int hashCode() {
        int hash = this.day;
        hash = hash * 31 + this.month.hashCode();
        hash = hash * 31 + this.year;
        return hash;
    }

    /**
     * Returns whether this date happened before the argument date.
     */
    public boolean isBefore(Date date) {
        return this.year <= date.year &&
               (this.year != date.year || this.month.getNumerical() <= date.month.getNumerical()) &&
               (this.year != date.year || this.month.getNumerical() != date.month.getNumerical() || this.day < date.day);
    }

    @Override
    public String toString() {
        return Metric.INT_2.format(this.day) + "/" + Metric.INT_2.format(this.month.getNumerical()) + "/" + this.year;
    }

    public long toTicks() {
        long temp = (long) (this.year - STARTING_DATE.year) * Time.TICKS_PER_YEAR;
        temp += (long) (this.month.getNumerical() - STARTING_DATE.month.getNumerical()) * Time.TICKS_PER_MONTH;
        temp += (long) (this.day - STARTING_DATE.day) * Time.TICKS_PER_DAY;
        temp -= 6L * Time.TICKS_PER_HOUR;
        return temp;
    }

    public enum Month {
        JANUARY("january", 1, 0),
        FEBRUARY("february", 2, 1),
        MARCH("march", 3, 2),
        APRIL("april", 4, 3),
        MAY("may", 5, 4),
        JUNE("june", 6, 5),
        JULY("july", 7, 6),
        AUGUST("august", 8, 7),
        SEPTEMBER("september", 9, 8),
        OCTOBER("october", 10, 9),
        NOVEMBER("november", 11, 10),
        DECEMBER("december", 12, 11);

        public static final Month[] VALUES = values();
        private final int index;
        private final String name;
        private final int numerical;
        private final Component textComponent;

        Month(String name, int numerical, int index) {
            this.name = name;
            this.numerical = numerical;
            this.index = index;
            this.textComponent = new TranslatableComponent("evolution.calendar.month." + this.name);
        }

        public static Month byIndex(int index) {
            index %= 12;
            for (Month month : VALUES) {
                if (month.index == index) {
                    return month;
                }
            }
            throw new IllegalStateException("Invalid month index: " + index);
        }

        public static Month byNumerical(int number) {
            return switch (number) {
                case 1 -> JANUARY;
                case 2 -> FEBRUARY;
                case 3 -> MARCH;
                case 4 -> APRIL;
                case 5 -> MAY;
                case 6 -> JUNE;
                case 7 -> JULY;
                case 8 -> AUGUST;
                case 9 -> SEPTEMBER;
                case 10 -> OCTOBER;
                case 11 -> NOVEMBER;
                case 12 -> DECEMBER;
                default -> throw new IllegalStateException("Invalid month number: " + number);
            };
        }

        public Component getDisplayName() {
            return this.textComponent;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return this.name;
        }

        public Month getNext(int n) {
            int index = (this.index + n) % 12;
            return byIndex(index);
        }

        public Month getNext() {
            return this.getNext(1);
        }

        public int getNumerical() {
            return this.numerical;
        }
    }
}
