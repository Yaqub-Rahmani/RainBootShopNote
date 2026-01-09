package com.example.rainbootshopnote.utils.date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AfPersianDateUtils {

    private AfPersianDateUtils() {}

    // Afghan (Dari) Jalali month names
    public static final String[] MONTHS_FA_AF = new String[]{
            "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
            "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت"
    };

    private static final Map<String, Integer> MONTH_NAME_TO_NUM = buildMonthMap();
    private static Map<String, Integer> buildMonthMap() {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < MONTHS_FA_AF.length; i++) m.put(MONTHS_FA_AF[i], i + 1);
        return m;
    }

    public static final class AfDate {
        public final int year;
        public final int month; // 1..12
        public final int day;   // 1..31
        public AfDate(int year, int month, int day) {
            this.year = year; this.month = month; this.day = day;
        }
    }

    // Accepts: 1404-04-14 , 1404-حمل-14 , 1404/04/14
    @Nullable
    public static AfDate parseAf(@Nullable String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        s = s.replace('/', '-');
        String[] parts = s.split("-");
        if (parts.length != 3) return null;

        Integer y = tryParseInt(parts[0]);
        if (y == null) return null;

        String monthPart = parts[1].trim();
        Integer m = tryParseInt(monthPart);
        if (m == null) m = MONTH_NAME_TO_NUM.get(monthPart);

        Integer d = tryParseInt(parts[2]);
        if (m == null || d == null) return null;

        if (m < 1 || m > 12) return null;
        if (d < 1 || d > 31) return null;

        return new AfDate(y, m, d);
    }

    @NonNull
    public static String formatAfNumeric(@NonNull AfDate d) {
        return String.format(Locale.US, "%04d-%02d-%02d", d.year, d.month, d.day);
    }

    @NonNull
    public static String formatAfWithMonthName(@NonNull AfDate d) {
        String monthName = MONTHS_FA_AF[d.month - 1];
        return d.year + "-" + monthName + "-" + String.format(Locale.US, "%02d", d.day);
    }

    // Gregorian yyyy-MM-dd -> Jalali AfDate
    @Nullable
    public static AfDate gregToAf(@Nullable String gregYyyyMmDd) {
        int[] g = parseGregYyyyMmDd(gregYyyyMmDd);
        if (g == null) return null;
        int[] j = gregorianToJalali(g[0], g[1], g[2]);
        return new AfDate(j[0], j[1], j[2]);
    }

    // Jalali AfDate -> Gregorian yyyy-MM-dd
    @Nullable
    public static String afToGreg(@Nullable AfDate af) {
        if (af == null) return null;
        int[] g = jalaliToGregorian(af.year, af.month, af.day);
        return String.format(Locale.US, "%04d-%02d-%02d", g[0], g[1], g[2]);
    }

    // Jalali numeric -> Gregorian Calendar (picker initial/min)
    @Nullable
    public static Calendar afNumericToGregCalendar(@Nullable String afNumeric) {
        AfDate af = parseAf(afNumeric);
        if (af == null) return null;

        int[] g = jalaliToGregorian(af.year, af.month, af.day);
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.set(Calendar.YEAR, g[0]);
        c.set(Calendar.MONTH, g[1] - 1);
        c.set(Calendar.DAY_OF_MONTH, g[2]);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    // ---------------- internal helpers ----------------

    @Nullable
    private static Integer tryParseInt(@Nullable String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try { return Integer.parseInt(t); } catch (Exception ignore) { return null; }
    }

    @Nullable
    private static int[] parseGregYyyyMmDd(@Nullable String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.length() < 10) return null;
        try {
            int y = Integer.parseInt(t.substring(0, 4));
            int m = Integer.parseInt(t.substring(5, 7));
            int d = Integer.parseInt(t.substring(8, 10));
            if (m < 1 || m > 12) return null;
            if (d < 1 || d > 31) return null;
            return new int[]{y, m, d};
        } catch (Exception ignore) {
            return null;
        }
    }

    private static final int[] gDaysInMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
    private static final int[] jDaysInMonth = {31,31,31,31,31,31,30,30,30,30,30,29};

    private static boolean isGregorianLeap(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private static int[] gregorianToJalali(int gy, int gm, int gd) {
        int gy2 = gy - 1600;
        int gm2 = gm - 1;
        int gd2 = gd - 1;

        int gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400;
        for (int i = 0; i < gm2; ++i) gDayNo += gDaysInMonth[i];
        if (gm2 > 1 && isGregorianLeap(gy)) gDayNo++;
        gDayNo += gd2;

        int jDayNo = gDayNo - 79;
        int jNp = jDayNo / 12053;
        jDayNo %= 12053;

        int jy = 979 + 33 * jNp + 4 * (jDayNo / 1461);
        jDayNo %= 1461;

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365;
            jDayNo = (jDayNo - 1) % 365;
        }

        int jm;
        for (jm = 0; jm < 11 && jDayNo >= jDaysInMonth[jm]; ++jm) jDayNo -= jDaysInMonth[jm];
        int jd = jDayNo + 1;

        return new int[]{jy, jm + 1, jd};
    }

    private static int[] jalaliToGregorian(int jy, int jm, int jd) {
        int jy2 = jy - 979;
        int jm2 = jm - 1;
        int jd2 = jd - 1;

        int jDayNo = 365 * jy2 + jy2 / 33 * 8 + (jy2 % 33 + 3) / 4;
        for (int i = 0; i < jm2; ++i) jDayNo += jDaysInMonth[i];
        jDayNo += jd2;

        int gDayNo = jDayNo + 79;

        int gy = 1600 + 400 * (gDayNo / 146097);
        gDayNo %= 146097;

        boolean leap = true;
        if (gDayNo >= 36525) {
            gDayNo--;
            gy += 100 * (gDayNo / 36524);
            gDayNo %= 36524;

            if (gDayNo >= 365) gDayNo++;
            else leap = false;
        }

        gy += 4 * (gDayNo / 1461);
        gDayNo %= 1461;

        if (gDayNo >= 366) {
            leap = false;
            gDayNo--;
            gy += gDayNo / 365;
            gDayNo = gDayNo % 365;
        }

        int gm;
        for (gm = 0; gm < 11; gm++) {
            int days = gDaysInMonth[gm];
            if (gm == 1 && leap) days = 29;
            if (gDayNo < days) break;
            gDayNo -= days;
        }
        int gd = gDayNo + 1;

        return new int[]{gy, gm + 1, gd};
    }
}
