package tgw.evolution.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class Date {

    public static final Date STARTING_DATE = new Date(Month.JUNE, 1, 1_000);
    public static final int DAYS_SINCE_MARCH_EQUINOX = 2 * Time.DAYS_IN_A_MONTH + 1;
    private final int day;
    private final Month month;
    private final int year;

    public Date(int year, Month month, int day) {
        if (day > Time.DAYS_IN_A_MONTH || day < 1) {
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
        if (day > Time.DAYS_IN_A_MONTH || day < 1) {
            throw new IllegalStateException("Invalid day: " + day);
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public Date(long ticks) {
        ticks += 6_000;
        long y = ticks / Time.YEAR_IN_TICKS;
        ticks -= Time.YEAR_IN_TICKS * y;
        long m = ticks / Time.MONTH_IN_TICKS;
        ticks -= Time.MONTH_IN_TICKS * m;
        long d = ticks / Time.DAY_IN_TICKS;
        d += STARTING_DATE.day;
        m += STARTING_DATE.month.numerical;
        y += STARTING_DATE.year;
        if (d > Time.DAYS_IN_A_MONTH) {
            d -= Time.DAYS_IN_A_MONTH;
            m++;
        }
        if (m > Time.MONTHS_IN_A_YEAR) {
            m -= Time.MONTHS_IN_A_YEAR;
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
        while (d > Time.DAYS_IN_A_MONTH) {
            m++;
            d -= Time.DAYS_IN_A_MONTH;
        }
        while (d < 1) {
            m--;
            d += Time.DAYS_IN_A_MONTH;
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
        if (!(obj instanceof Date)) {
            return false;
        }
        Date date = (Date) obj;
        return this.year == date.year && this.month == date.month && this.day == date.day;
    }

    public int getDay() {
        return this.day;
    }

    public ITextComponent getDayDisplayName() {
        return new TranslationTextComponent("evolution.calendar.day." + this.day, this.day);
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("evolution.calendar.full_date", this.getDayDisplayName(), this.month.getDisplayName(), this.year);
    }

    public Month getMonth() {
        return this.month;
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
     * Returns whether this date happened after the argument date.
     */
    public boolean isAfter(Date date) {
        return !this.isBefore(date) && !this.equals(date);
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
        long temp = (long) (this.year - STARTING_DATE.year) * Time.YEAR_IN_TICKS;
        temp += (long) (this.month.getNumerical() - STARTING_DATE.month.getNumerical()) * Time.MONTH_IN_TICKS;
        temp += (long) (this.day - STARTING_DATE.day) * Time.DAY_IN_TICKS;
        temp -= 6_000;
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

        private final int index;
        private final String name;
        private final int numerical;
        private final ITextComponent textComponent;

        Month(String name, int numerical, int index) {
            this.name = name;
            this.numerical = numerical;
            this.index = index;
            this.textComponent = new TranslationTextComponent("evolution.calendar.month." + this.name);
        }

        public static Month byIndex(int index) {
            index %= 12;
            for (Month month : values()) {
                if (month.index == index) {
                    return month;
                }
            }
            throw new IllegalStateException("Invalid month index: " + index);
        }

        public static Month byNumerical(int number) {
            switch (number) {
                case 1:
                    return JANUARY;
                case 2:
                    return FEBRUARY;
                case 3:
                    return MARCH;
                case 4:
                    return APRIL;
                case 5:
                    return MAY;
                case 6:
                    return JUNE;
                case 7:
                    return JULY;
                case 8:
                    return AUGUST;
                case 9:
                    return SEPTEMBER;
                case 10:
                    return OCTOBER;
                case 11:
                    return NOVEMBER;
                case 12:
                    return DECEMBER;
            }
            throw new IllegalStateException("Invalid month number: " + number);
        }

        public ITextComponent getDisplayName() {
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
