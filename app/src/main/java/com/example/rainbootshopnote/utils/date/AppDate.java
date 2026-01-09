package com.example.rainbootshopnote.utils.date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Central date/time helper for the app.
 *
 * Goals:
 * - DB always stores Latin digits.
 * - Old rows that contain Persian/Arabic-Indic digits are still readable/filterable.
 * - UI can show Gregorian or Afghan (Jalali) date with a single boolean.
 */
public final class AppDate {

    private AppDate() {}

    // DB format (always Latin digits)
    public static final String DB_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DB_DATE_PATTERN = "yyyy-MM-dd";

    /** Returns current timestamp as: yyyy-MM-dd HH:mm:ss (Latin digits) */
    @NonNull
    public static String nowDbDateTime() {
        return formatDbDateTime(new Date());
    }

    /** Formats a Date as: yyyy-MM-dd HH:mm:ss (Latin digits) */
    @NonNull
    public static String formatDbDateTime(@NonNull Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DB_DATETIME_PATTERN, Locale.US);
        return DateNormalize.toLatinDigits(sdf.format(date));
    }

    /**
     * Extracts YYYY-MM-DD from a DB field that can be:
     * - "2025-12-23 10:45:00"
     * - "۲۰۲۵-۱۲-۲۳ ۱۰:۴۵:۰۰"
     * - "2025-12-23"
     */
    @Nullable
    public static String extractIsoDay(@Nullable String raw) {
        return DateNormalize.extractIsoDay(raw);
    }

    /** Normalizes Persian/Arabic-Indic digits to Latin. */
    @NonNull
    public static String toLatinDigits(@NonNull String s) {
        return DateNormalize.toLatinDigits(s);
    }

    /**
     * UI date text.
     * @param rawDbOrAny a DB datetime or any string that contains YYYY-MM-DD
     * @param showAf if true shows Afghan (Jalali) numeric date like 1404-04-14
     */
    @NonNull
    public static String toUiDate(@Nullable String rawDbOrAny, boolean showAf) {
        String day = extractIsoDay(rawDbOrAny); // "yyyy-MM-dd" (Latin)
        if (day == null) return "";

        if (!showAf) {
            return day;
        }

        // 1) Önce mevcut util
        try {
            AfPersianDateUtils.AfDate af = AfPersianDateUtils.gregToAf(day);
            if (af != null) return AfPersianDateUtils.formatAfNumeric(af);
        } catch (Exception ignore) {}

        // 2) Fallback: kesin Jalali dönüşümü (1404-04-14)
        int[] j = gregorianToJalali(day);
        if (j == null) return day;

        return String.format(Locale.US, "%04d-%02d-%02d", j[0], j[1], j[2]);
    }

    @Nullable
    private static int[] gregorianToJalali(@NonNull String isoDay) {
        try {
            int gy = Integer.parseInt(isoDay.substring(0, 4));
            int gm = Integer.parseInt(isoDay.substring(5, 7));
            int gd = Integer.parseInt(isoDay.substring(8, 10));
            return gregorianToJalali(gy, gm, gd);
        } catch (Exception e) {
            return null;
        }
    }

    private static int[] gregorianToJalali(int gy, int gm, int gd) {
        int[] g_d_m = {0,31,59,90,120,151,181,212,243,273,304,334};

        int jy;
        if (gy > 1600) {
            jy = 979;
            gy -= 1600;
        } else {
            jy = 0;
            gy -= 621;
        }

        int gy2 = (gm > 2) ? (gy + 1) : gy;
        int days = (365 * gy)
                + ((gy2 + 3) / 4)
                - ((gy2 + 99) / 100)
                + ((gy2 + 399) / 400)
                - 80 + gd + g_d_m[gm - 1];

        jy += 33 * (days / 12053);
        days %= 12053;

        jy += 4 * (days / 1461);
        days %= 1461;

        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int jm, jd;
        if (days < 186) {
            jm = 1 + (days / 31);
            jd = 1 + (days % 31);
        } else {
            jm = 7 + ((days - 186) / 30);
            jd = 1 + ((days - 186) % 30);
        }

        return new int[]{jy, jm, jd};
    }


    /**
     * UI time text (12h): "hh:mm a" => "10:45 AM".
     * Accepts DB datetime that might contain Persian digits.
     */
    @NonNull
    public static String toUiTime12h(@Nullable String rawDbOrAny) {
        if (rawDbOrAny == null) return "";
        String s = toLatinDigits(rawDbOrAny.trim());
        if (s.length() < 16) return "";

        // Best-effort parse, supports "yyyy-MM-dd HH:mm:ss" and "yyyy-MM-dd HH:mm"
        Date d = tryParseDbDateTime(s);
        if (d == null) return "";

        SimpleDateFormat out = new SimpleDateFormat("hh:mm a", Locale.US);
        return DateNormalize.toLatinDigits(out.format(d));
    }

    /**
     * Converts an Afghan date (1404-04-14 or 1404-جوزا-14) to Gregorian yyyy-MM-dd.
     * If input already looks like Gregorian, returns it normalized.
     */
    @Nullable
    public static String toGregDay(@Nullable String maybeAfOrGreg) {
        if (maybeAfOrGreg == null) return null;
        String s = toLatinDigits(maybeAfOrGreg.trim());
        if (s.isEmpty()) return null;

        // If it already contains a valid greg day, return it.
        String greg = extractIsoDay(s);
        if (greg != null && greg.equals(s.substring(0, Math.min(s.length(), 10)))) {
            return greg;
        }

        // Otherwise try Afghan/Jalali.
        AfPersianDateUtils.AfDate af = AfPersianDateUtils.parseAf(s);
        return AfPersianDateUtils.afToGreg(af);
    }

    /** Build inclusive start/end datetime strings for a day range (Latin digits). */
    @NonNull
    public static Range toDbDateTimeRangeInclusive(@Nullable String fromDay, @Nullable String toDay) {
        String from = toGregDay(fromDay);
        String to = toGregDay(toDay);

        if (from == null || from.isEmpty()) from = "0000-01-01";
        if (to == null || to.isEmpty()) to = "9999-12-31";

        return new Range(from + " 00:00:00", to + " 23:59:59");
    }

    public static final class Range {
        public final String from;
        public final String to;
        public Range(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    /** UI datetime text: "<date> <time>" */
    @NonNull
    public static String toUiDateTime(@Nullable String rawDbOrAny, boolean showAf) {
        String d = toUiDate(rawDbOrAny, showAf);
        if (d.isEmpty()) return "";
        String t = toUiTime12h(rawDbOrAny);
        if (t.isEmpty()) return d;
        return d + " " + t;
    }


    @Nullable
    private static Date tryParseDbDateTime(@NonNull String latin) {
        // Try with seconds
        try {
            return new SimpleDateFormat(DB_DATETIME_PATTERN, Locale.US).parse(latin);
        } catch (ParseException ignore) {}

        // Try without seconds
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(latin);
        } catch (ParseException ignore) {}

        // Try only day
        try {
            Date day = new SimpleDateFormat(DB_DATE_PATTERN, Locale.US).parse(latin.substring(0, 10));
            if (day == null) return null;
            Calendar c = Calendar.getInstance(Locale.getDefault());
            c.setTime(day);
            return c.getTime();
        } catch (Exception ignore) {
            return null;
        }
    }

}
