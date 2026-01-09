package com.example.rainbootshopnote.security;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppSecurity {

    private static final String PREF = "app_security";
    private static final String KEY_PIN_HASH = "pin_hash";

    private AppSecurity() {}

    public static boolean isPinSet(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String hash = sp.getString(KEY_PIN_HASH, null);
        return hash != null && !hash.trim().isEmpty();
    }

    public static void setPin(Context ctx, String pinPlain) {
        if (pinPlain == null || pinPlain.length() < 4) {
            throw new IllegalArgumentException("PIN too short");
        }
        String hash = HashUtil.sha256(pinPlain);
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PIN_HASH, hash)
                .apply();
    }

    public static boolean verifyPin(Context ctx, String pinPlain) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String hash = sp.getString(KEY_PIN_HASH, null);
        if (hash == null || hash.trim().isEmpty()) return false;
        String inputHash = HashUtil.sha256(pinPlain == null ? "" : pinPlain);
        return hash.equals(inputHash);
    }

    public static void clearPin(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_PIN_HASH)
                .apply();
    }
}
