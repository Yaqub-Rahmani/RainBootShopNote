package com.example.rainbootshopnote.utils.date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DateNormalize {
    private DateNormalize() {}

    /**
     * Farsça (۰۱۲۳...) ve Arabic-Indic (٠١٢٣...) rakamları Latin'e çevirir.
     * Ayrıca Unicode boşluk/karakter farklarını olduğu gibi bırakır.
     */
    @NonNull
    public static String toLatinDigits(@NonNull String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Persian digits ۰۱۲۳۴۵۶۷۸۹
            if (c >= '۰' && c <= '۹') {
                out.append((char) ('0' + (c - '۰')));
                continue;
            }

            // Arabic-Indic digits ٠١٢٣٤٥٦٧٨٩
            if (c >= '٠' && c <= '٩') {
                out.append((char) ('0' + (c - '٠')));
                continue;
            }

            out.append(c);
        }
        return out.toString();
    }

    /**
     * DB veya UI'dan gelen tarih/datetime string'i normalize eder:
     * - rakamları Latin'e çevirir
     * - '/' -> '-'
     * - 'T' -> ' ' (ISO)
     */
    @Nullable
    public static String normalizeAny(@Nullable String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        s = toLatinDigits(s);
        s = s.replace('/', '-');
        s = s.replace('T', ' ');

        return s;
    }

    /**
     * "yyyy-MM-dd" çıkarır:
     * Örnekler:
     * - "۲۰۲۵-۱۲-۲۳" -> "2025-12-23"
     * - "2025-12-23 10:45:00" -> "2025-12-23"
     * - "2025-12-23T10:45:00" -> "2025-12-23"
     * - "2025/12/23" -> "2025-12-23"
     */
    @Nullable
    public static String extractIsoDay(@Nullable String raw) {
        String s = normalizeAny(raw);
        if (s == null || s.length() < 10) return null;

        String d = s.substring(0, 10);

        // Basit doğrulama: yyyy-MM-dd
        if (d.charAt(4) != '-' || d.charAt(7) != '-') return null;

        // Sayı kontrolü (opsiyonel ama güvenli)
        for (int i = 0; i < 10; i++) {
            if (i == 4 || i == 7) continue;
            char c = d.charAt(i);
            if (c < '0' || c > '9') return null;
        }

        return d;
    }

    /**
     * Datetime normalize:
     * - sadece date geldiyse " 00:00:00" ekler
     * - fazla uzunsa 19'a kırpar: "yyyy-MM-dd HH:mm:ss"
     */
    @Nullable
    public static String normalizeIsoDateTime(@Nullable String raw) {
        String s = normalizeAny(raw);
        if (s == null || s.length() < 10) return null;

        if (s.length() == 10) return s + " 00:00:00";
        if (s.length() >= 19) return s.substring(0, 19);

        // 10..18 arası ise: date al, saat sıfırla
        return s.substring(0, 10) + " 00:00:00";
    }
}
