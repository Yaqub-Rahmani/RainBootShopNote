package com.example.rainbootshopnote.utils;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;

import java.util.Locale;

public final class PdfExportHelper {

    private PdfExportHelper() {}

    /**
     * Print / Save as PDF akışını kullanır.
     * Not: Android’in PDF kaydetme ekranında kullanıcı dosya adını değiştirebilir.
     * Job adı olarak title gönderilir (çoğu cihazda önerilen isim olur).
     */
    public static void printHtml(Context context, String title, String html) {
        WebView webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setDomStorageEnabled(false);

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                if (printManager == null) return;

                PrintAttributes attributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();

                String jobName = (title == null || title.trim().isEmpty()) ? "Report" : title.trim();
                printManager.print(jobName, view.createPrintDocumentAdapter(jobName), attributes);
            }
        });

        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    /** Kullanıcı adını sistemden okumak için: burada default değer döner. Sen login sistemin varsa burayı bağlarız. */
    public static String getDefaultUserName(Context context) {
        // Projede net bir “user” kaynağı görmediğim için güvenli default.
        // İstersen burayı SharedPreferences/DB ile bağlarız.
        return "User";
    }

    /** Dosya adında sorun çıkaran karakterleri temizler. */
    public static String safeFilePart(String input) {
        if (input == null) return "User";
        String s = input.trim();
        if (s.isEmpty()) return "User";
        s = s.replaceAll("[\\\\/:*?\"<>|]", "_");
        s = s.replaceAll("\\s+", "_");
        return s;
    }
}
