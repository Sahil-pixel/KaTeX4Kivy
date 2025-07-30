// File: KaTeXRenderer.java
package org.katex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// Author : Sahil-Pixel
// Date : 30 July 2025
// https://github.com/Sahil-pixel
// https://kivy.org/
// https://katex.org/

public class KaTeXRenderer {
    private static final String TAG = "KaTeXRenderer";
    private Context context;
    private WebView webView;
    private FrameLayout frameLayout;
    private Handler handler;
    private String latex;
    private RenderCallback callback;
    private int width = 512;
    private int height = 512;
    private boolean pageLoaded = false;

    // Render options
    private String fontSize = "8px";
    private String textColor = "#0000ff";
    private String bgColor = "#ffffff";
    private String padding = "2px";
    private String paddingBody = "0";
    private String marginBody = "0";
    private String justify = "flex-start";
    private String align = "flex-start";
    private String htmlHeight = "100vh";
    private String htmlWidth = "100vw";
    private String customMathStyle = "";
    private String fontFamily = "sans-serif";

    public interface RenderCallback {
        void onRendered(Bitmap bitmap);
    }

    public KaTeXRenderer(Context ctx, RenderCallback cb) {
        this.context = ctx;
        this.callback = cb;
        this.handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "Initializing KaTeXRenderer...");

        handler.post(() -> {
            webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setBackgroundColor(0xFFFFFFFF);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    Log.d(TAG, "Page loaded. Beginning render capture.");
                    pageLoaded = true;
                    //handler.postDelayed(() -> renderNow(), 10);
                    measureHtmlContent();
                }
            });

            frameLayout = new FrameLayout(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            frameLayout.addView(webView, params);
            //frameLayout.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
             //                   View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            //frameLayout.layout(0, 0, frameLayout.getMeasuredWidth(), frameLayout.getMeasuredHeight());
        });
    }

    public void renderLatex(String latexStr) {
        this.latex = latexStr;
        // String content = "<!DOCTYPE html><html><head>" +
        // "<meta charset='utf-8'>" +
        // "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
        // "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css'>" +
        // "<script defer src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js'></script>" +
        // "<script defer>document.addEventListener('DOMContentLoaded',function(){" +
        // "  katex.render(String.raw`" + latexStr + "`, document.getElementById('math'), {throwOnError:false,displayMode: false});" +
        // "  setTimeout(function() { if (window.MathBridge) window.MathBridge.onRendered(); }, 10);" +
        // "});</script>" +
        // "</head><body style='margin:" + marginBody + ";" +
        // "padding:" + paddingBody + ";" +
        // "background:" + bgColor + ";" +
        // "display:inline-block;'>" +  // KEY FIX HERE
        // "<div id='math' style='font-size:" + fontSize + ";color:" + textColor + ";padding:" + padding + ";" +
        // "font-family:" + fontFamily + ";" + customMathStyle + "white-space:nowrap;'></div>" +
        // "</body></html>";

        String content = "<!DOCTYPE html><html><head>" +
    "<meta charset='utf-8'>" +
    "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
    "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css'>" +
    "<script defer src='https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js'></script>" +
    "<script defer>document.addEventListener('DOMContentLoaded',function(){" +
    "  katex.render(String.raw`" + latexStr + "`, document.getElementById('math'), {" +
    "    throwOnError:false, displayMode:false" +
    "  });" +
    "  setTimeout(function() { if (window.MathBridge) window.MathBridge.onRendered(); }, 10);" +
    "});</script>" +
    "</head><body style='margin:" + marginBody + ";" +
    "padding:" + paddingBody + ";" +
    "background:" + bgColor + ";'>" +

    // Scrollable wrapper with proper sizing
    "<div style='width:100%; overflow-x:auto;'>" +
        "<div id='math' style='display:inline-block; min-width:100%; white-space:nowrap;" +
        "font-size:" + fontSize + ";" +
        "color:" + textColor + ";" +
        "padding:" + padding + ";" +
        "font-family:" + fontFamily + ";" +
        customMathStyle + "'></div>" +
    "</div>" +

    "</body></html>";




        handler.post(() -> {
            Log.d(TAG, "Rendering LaTeX: " + latexStr);
            pageLoaded = false;
            webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        });
    }

    public void measureHtmlContent() {
    handler.post(() -> {
        webView.evaluateJavascript(
            "(function() {" +
            "  var el = document.getElementById('math');" +
            "  if (!el) return '0,0';" +
            "  var rect = el.getBoundingClientRect();" +
            "  var w = Math.max(rect.width);" +
            "  var h = Math.max(rect.height);" +
            "  return Math.ceil(w) + ',' + Math.ceil(h);" +
            "})()",
            value -> {
                try {
                    value = value.replace("\"", "");
                    String[] parts = value.split(",");
                    if (parts.length < 2) throw new IllegalArgumentException("Invalid size: " + value);
                    int cssWidth = Integer.parseInt(parts[0]);
                    int cssHeight = Integer.parseInt(parts[1]);
                    Log.i(TAG, "Css device pixel size: " + cssWidth + " x " + cssHeight);
                    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                    int androidWidth = (int) Math.ceil(cssWidth * metrics.density);
                    int androidHeight = (int) Math.ceil(cssHeight * metrics.density);

                    this.width = Math.max(1, androidWidth);
                    this.height = Math.max(1, androidHeight);
                    Log.i(TAG, "Actual device pixel size: " + width + " x " + height);

                    // Resize the layout now
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
                    webView.setLayoutParams(params);
                    frameLayout.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
                    frameLayout.layout(0, 0, width, height);
                    frameLayout.invalidate();

                    // Now render
                    renderNow();

                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse size: " + value, e);
                }
            }
        );
    });
}


    public void renderNow() {
        handler.postDelayed(() -> {
            if (!pageLoaded) {
                Log.d(TAG, "Waiting for page to load...");
                return;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawNow(canvas);
            if (callback != null) {
                callback.onRendered(bitmap);
            }
        }, 100);
    }

    public void drawNow(Canvas canvas) {
        frameLayout.draw(canvas);
        frameLayout.invalidate();
        Log.d(TAG, "Forced drawNow() called.");
    }

    public void saveBitmapToJpg(Bitmap bitmap, String filename) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), "KaTeXDebug");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, filename + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.d(TAG, "Saved image to: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to save bitmap", e);
        }
    }

    public static String toJsString(String input) {
        return "\"" + input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") + "\"";
    }

    // Getters and Setters

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }

    public String getPadding() { return padding; }
    public void setPadding(String padding) { this.padding = padding; }

    public String getPaddingBody() { return paddingBody; }
    public void setPaddingBody(String paddingBody) { this.paddingBody = paddingBody; }

    public String getMarginBody() { return marginBody; }
    public void setMarginBody(String marginBody) { this.marginBody = marginBody; }

    public String getJustify() { return justify; }
    public void setJustify(String justify) { this.justify = justify; }

    public String getAlign() { return align; }
    public void setAlign(String align) { this.align = align; }

    public String getHtmlHeight() { return htmlHeight; }
    public void setHtmlHeight(String htmlHeight) { this.htmlHeight = htmlHeight; }

    public String getHtmlWidth() { return htmlWidth; }
    public void setHtmlWidth(String htmlWidth) { this.htmlWidth = htmlWidth; }

    public String getCustomMathStyle() { return customMathStyle; }
    public void setCustomMathStyle(String customMathStyle) { this.customMathStyle = customMathStyle; }

    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

    public String getLatex() { return latex; }
    public void setLatex(String latex) { this.latex = latex; }

    public boolean isPageLoaded() { return pageLoaded; }
}
