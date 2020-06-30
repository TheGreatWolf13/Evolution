package tgw.evolution.util;

import net.minecraft.util.text.TranslationTextComponent;

public class Date {

    public static final Date STARTING_DATE = new Date(Month.JUNE, 1, 1000);
    private int day;
    private Month month;
    private int year;

    public Date(int year, Month month, int day) {
        if (day > Time.DAYS_IN_A_MONTH || day < 1) {
            throw new IllegalStateException("Invalid day: " + day);
        }
        this.year = year;
        this.month = month;
        this.day = day;
        if (this.isBefore(STARTING_DATE)) {
            throw new IllegalStateException("Date cannot be before starting date " + STARTING_DATE + ": day = " + this.day + " month = " + this.month + " year = " + this.year);
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

    public Date(int ticks) {
        ticks += 6000;
        int y = STARTING_DATE.year;
        int m = STARTING_DATE.month.getNumerical();
        int d = STARTING_DATE.day;
        while (ticks >= Time.YEAR_IN_TICKS) {
            y++;
            ticks -= Time.YEAR_IN_TICKS;
        }
        while (ticks >= Time.MONTH_IN_TICKS) {
            m++;
            ticks -= Time.MONTH_IN_TICKS;
        }
        while (ticks >= Time.DAY_IN_TICKS) {
            d++;
            ticks -= Time.DAY_IN_TICKS;
        }
        this.year = y;
        this.month = Month.byNumerical(m);
        this.day = d;
    }

    public static String getConnector() {
        return new TranslationTextComponent("evolution.calendar.connector").getFormattedText();
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

    public Date addAndSet(int days, int months, int years) {
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
        this.day = d;
        this.month = Month.byNumerical(m);
        this.year = y;
        if (this.isBefore(STARTING_DATE)) {
            throw new IllegalStateException("Date cannot be before starting date " + STARTING_DATE + ": day = " + this.day + " month = " + this.month + " year = " + this.year);
        }
        return this;
    }

    public boolean equals(Date date) {
        return this.year == date.year && this.month == date.month && this.day == date.day;
    }

    /**
     * Returns whether this date happened before the argument date.
     */
    public boolean isBefore(Date date) {
        return this.year <= date.year && (this.year != date.year || this.month.getNumerical() <= date.month.getNumerical()) && (this.year != date.year || this.month.getNumerical() != date.month.getNumerical() || this.day < date.day);
    }

    /**
     * Returns whether this date happened after the argument date.
     */
    public boolean isAfter(Date date) {
        return !this.isBefore(date) && !this.equals(date);
    }

    @Override
    public String toString() {
        return String.format("%02d", this.day) + "/" + String.format("%02d", this.month.getNumerical()) + "/" + this.year;
    }

    public String getDayTranslation() {
        return new TranslationTextComponent("evolution.calendar.day." + this.day, this.day).getFormattedText();
    }

    public String getFullString() {
        String space = getConnector().isEmpty() ? "" : " ";
        return this.getDayTranslation() + " " + getConnector() + space + this.month.getTranslatedName() + " " + getConnector() + space + this.year;
    }

    public int getYear() {
        return this.year;
    }

    public int getDay() {
        return this.day;
    }

    public Month getMonth() {
        return this.month;
    }

    public int toTicks() {
        int temp = (this.year - STARTING_DATE.year) * Time.YEAR_IN_TICKS;
        temp += (this.month.getNumerical() - STARTING_DATE.month.getNumerical()) * Time.MONTH_IN_TICKS;
        temp += (this.day - STARTING_DATE.day) * Time.DAY_IN_TICKS;
        temp -= 6000;
        return Math.max(temp, 0);
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

        private final String name;
        private final int numerical;
        private final int index;

        Month(String name, int numerical, int index) {
            this.name = name;
            this.numerical = numerical;
            this.index = index;
        }

        public static Month byIndex(int index) {
            index %= 12;
            for (Month month : Month.values()) {
                if (month.index == index) {
                    return month;
                }
            }
            throw new IllegalStateException("Invalid month index: " + index);
        }

        public static Month byNumerical(int number) {
            for (Month month : Month.values()) {
                if (month.numerical == number) {
                    return month;
                }
            }
            throw new IllegalStateException("Invalid month number: " + number);
        }

        public String getName() {
            return this.name;
        }

        public String getTranslatedName() {
            return new TranslationTextComponent("evolution.calendar.month." + this.name).getFormattedText();
        }

        public int getIndex() {
            return this.index;
        }

        public int getNumerical() {
            return this.numerical;
        }

        public Month getNext() {
            return this.getNext(1);
        }

        public Month getNext(int n) {
            int index = (this.index + n) % 12;
            return byIndex(index);
        }
    }
}
