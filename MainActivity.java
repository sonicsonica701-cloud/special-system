package com.example.browser;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

    WebView webView;
    ImageView cursor;
    boolean mouseEnabled = false;
    float cursorX = 100, cursorY = 100; // Initial cursor position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        cursor = findViewById(R.id.mouse_cursor);
        Button btnMouse = findViewById(R.id.btn_mouse);
        Button btnKeyboard = findViewById(R.id.btn_keyboard);
        RelativeLayout rootLayout = findViewById(R.id.root_layout);

        // Setup WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        // Load a desktop-ish site to test
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.google.com");

        // Toggle Keyboard
        btnKeyboard.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        });

        // Toggle Mouse Mode
        btnMouse.setOnClickListener(v -> {
            mouseEnabled = !mouseEnabled;
            cursor.setVisibility(mouseEnabled ? View.VISIBLE : View.GONE);
            // Reset cursor to center
            if(mouseEnabled) {
                cursorX = rootLayout.getWidth() / 2f;
                cursorY = rootLayout.getHeight() / 2f;
                updateCursor();
            }
        });

        // TRACKPAD LOGIC: Intercept touches on the root layout to move cursor
        rootLayout.setOnTouchListener((v, event) -> {
            if (!mouseEnabled) return false; // If mouse off, let WebView handle touch naturally

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    // Move cursor based on finger movement
                    // (In a real app, you would calculate delta from previous touch)
                    cursorX = event.getX();
                    cursorY = event.getY();
                    updateCursor();
                    return true; // Consume event

                case MotionEvent.ACTION_UP:
                    // CLICK: When finger lifts, click WHERE THE CURSOR IS, not where finger is
                    simulateClick(cursorX, cursorY);
                    return true;
            }
            return true;
        });
    }

    private void updateCursor() {
        cursor.setX(cursorX);
        cursor.setY(cursorY);
    }

    // This tricks the WebView into thinking you touched a specific spot
    private void simulateClick(float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        
        // Touch Down
        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        webView.dispatchTouchEvent(downEvent);
        
        // Touch Up
        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        webView.dispatchTouchEvent(upEvent);
        
        downEvent.recycle();
        upEvent.recycle();
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
