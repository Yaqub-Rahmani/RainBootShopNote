package com.example.rainbootshopnote.security.appSecur;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class ActivationGate {

    private ActivationGate() {}

    // ===== Preferences =====
    private static final String PREF = "activation_gate";
    private static final String K_ACTIVATED = "activated";
    private static final String K_DEVICE_ID = "device_id";
    private static final String K_ACTIVATED_AT = "activated_at";

    // ===== Validation rules =====
    private static final int TOLERANCE_MINUTES = 2; // ±2 dk

    // ===== Optional expiry (offline) =====
    // İstersen aktif et: LoginActivity içinde isExpiredNow() kontrolü var.
    // Örnek: 2028-01-01 sonrası kapat.
    private static final int EXP_YEAR = 2028;
    private static final int EXP_MONTH_1_12 = 1; // 1-12
    private static final int EXP_DAY = 1;

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    /** Uygulama süresi doldu mu? (offline kontrol) */
    public static boolean isExpiredNow() {
        Calendar now = Calendar.getInstance();
        Calendar exp = Calendar.getInstance();
        exp.set(Calendar.YEAR, EXP_YEAR);
        exp.set(Calendar.MONTH, EXP_MONTH_1_12 - 1);
        exp.set(Calendar.DAY_OF_MONTH, EXP_DAY);
        exp.set(Calendar.HOUR_OF_DAY, 0);
        exp.set(Calendar.MINUTE, 0);
        exp.set(Calendar.SECOND, 0);
        exp.set(Calendar.MILLISECOND, 0);
        return now.after(exp);
    }

    /** Tek seferlik: Bu cihazda aktivasyon yapıldı mı? */
    public static boolean isActivatedForThisDevice(@NonNull Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (!sp.getBoolean(K_ACTIVATED, false)) return false;

        String savedDev = sp.getString(K_DEVICE_ID, "");
        String currentDev = getDeviceId(ctx);
        return currentDev.equals(savedDev);
    }

    /** Aktivasyonu kaydet (tek seferlik) */
    public static void markActivated(@NonNull Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putBoolean(K_ACTIVATED, true)
                .putString(K_DEVICE_ID, getDeviceId(ctx))
                .putLong(K_ACTIVATED_AT, System.currentTimeMillis())
                .apply();
    }

    /** İstersen manuel reset (kullanma; sadece debug için) */
    public static void clearActivation(@NonNull Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    /**
     * 11 haneli anahtar doğrulama:
     * Format: YYMMDDHHmmS
     * Kural: En son hane S, diğer tüm parçalardan ÇIKARILIR.
     * Örn: 29043404323 -> S=3 => 26/01/31 01:29
     */
    public static boolean validateKeyNow(@NonNull Context ctx, @NonNull String rawKeyAnyDigits) {
        String key = toLatinDigits(rawKeyAnyDigits).trim();

        if (!key.matches("\\d{11}")) return false;

        int yy = parse2(key, 0);
        int mo = parse2(key, 2);
        int dd = parse2(key, 4);
        int hh = parse2(key, 6);
        int mi = parse2(key, 8);
        int s  = parse1(key, 10);

        // S için istersen limit uygula (1..9):
        // if (s < 1 || s > 9) return false;

        // ÇÖZ: her birinden S çıkar
        yy -= s;
        mo -= s;
        dd -= s;
        hh -= s;
        mi -= s;

        // YY -> 2000 + YY
        int year = 2000 + yy;

        // Aralık kontrolleri
        if (year < 2000 || year > 2099) return false;
        if (mo < 1 || mo > 12) return false;
        if (dd < 1 || dd > 31) return false;
        if (hh < 0 || hh > 23) return false;
        if (mi < 0 || mi > 59) return false;

        // Tarih gerçekten geçerli mi (31 Şubat vs.)
        Calendar target = Calendar.getInstance();
        target.setLenient(false);
        try {
            target.set(Calendar.YEAR, year);
            target.set(Calendar.MONTH, mo - 1);
            target.set(Calendar.DAY_OF_MONTH, dd);
            target.set(Calendar.HOUR_OF_DAY, hh);
            target.set(Calendar.MINUTE, mi);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);
            target.getTimeInMillis(); // compute
        } catch (Exception ex) {
            return false;
        }

        Calendar now = Calendar.getInstance();

        long diffMillis = Math.abs(now.getTimeInMillis() - target.getTimeInMillis());
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);

        return diffMinutes <= TOLERANCE_MINUTES;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static int parse2(String s, int start) {
        return Integer.parseInt(s.substring(start, start + 2));
    }

    private static int parse1(String s, int start) {
        return Integer.parseInt(s.substring(start, start + 1));
    }

    /** Farsça / Arabic-Indic rakamları Latin'e çevirir. */
    public static String toLatinDigits(@NonNull String in) {
        StringBuilder out = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char ch = in.charAt(i);

            // Persian digits (۰۱۲۳۴۵۶۷۸۹) U+06F0..U+06F9
            if (ch >= 0x06F0 && ch <= 0x06F9) {
                out.append((char) ('0' + (ch - 0x06F0)));
                continue;
            }

            // Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩) U+0660..U+0669
            if (ch >= 0x0660 && ch <= 0x0669) {
                out.append((char) ('0' + (ch - 0x0660)));
                continue;
            }

            out.append(ch);
        }
        return out.toString();
    }

    private static String getDeviceId(@NonNull Context ctx) {
        String id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        return (id == null) ? "" : id;
    }
}
