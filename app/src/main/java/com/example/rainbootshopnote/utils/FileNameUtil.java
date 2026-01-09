package com.example.rainbootshopnote.utils;

public final class FileNameUtil {
    private FileNameUtil() {}

    public static String safePart(String s) {
        if (s == null) return "user";
        s = s.trim();
        if (s.isEmpty()) return "user";

        s = s.replace(' ', '_');
        // dosya isminde sorun çıkaran karakterleri temizle
        s = s.replaceAll("[^a-zA-Z0-9_\\-\\u0600-\\u06FF]", "");
        if (s.isEmpty()) return "user";
        return s;
    }
}
