package com.alphasnap.app;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    private ProgressBar progressBar;
    private long backPressedTime = 0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);

        // WebView Settings
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // 로컬 스토리지 필수 (포트폴리오 정보 저장)
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // 팝업창 및 다중 창 설정 (구글 OAuth 로그인창 연동을 위한 설정)
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 구글 로그인 OAuth 통과를 위한 User-Agent 트윅
        // WebView 기본 UA의 "Version/4.0" 문자열을 제거하면 구글이 WebView가 아닌 일반 모바일 브라우저로 인식하여 로그인을 차단하지 않습니다.
        String defaultUserAgent = webSettings.getUserAgentString();
        if (defaultUserAgent != null) {
            String customUserAgent = defaultUserAgent.replace("Version/4.0 ", "");
            webSettings.setUserAgentString(customUserAgent);
        }

        // 웹뷰 클라이언트 설정 (웹 페이지 내부 이동 제어 및 로딩 상태 제어)
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE); // 로딩 스피너 보이기
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE); // 로딩 스피너 숨기기
            }
        });

        // 웹 크롬 클라이언트 설정 (자바스크립트 alert, 다중 창 팝업 등 제어)
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result) {
                new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("확인")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final android.webkit.JsPromptResult result) {
                final android.widget.EditText input = new android.widget.EditText(MainActivity.this);
                input.setText(defaultValue);
                new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("입력")
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            result.confirm(input.getText().toString());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
                return true;
            }

            // 구글 OAuth 등 window.open() 호출 시 팝업 다이얼로그 처리
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                WebView popupWebView = new WebView(MainActivity.this);
                popupWebView.getSettings().setJavaScriptEnabled(true);
                popupWebView.getSettings().setSupportMultipleWindows(true);
                popupWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                popupWebView.getSettings().setDomStorageEnabled(true);

                // 팝업 웹뷰에도 동일하게 User-Agent 트윅 적용 (구글 로그인 차단 해제)
                String defaultUA = popupWebView.getSettings().getUserAgentString();
                if (defaultUA != null) {
                    popupWebView.getSettings().setUserAgentString(defaultUA.replace("Version/4.0 ", ""));
                }

                popupWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }
                });

                popupWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        dialog.dismiss(); // 구글이 window.close() 호출 시 다이얼로그 닫기
                        super.onCloseWindow(window);
                    }
                });

                dialog.setContentView(popupWebView);
                dialog.show();

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });

        // 캐시 비우기: 앱 시작 시 캐시를 지워 갱신된 CSS/JS(캐시버스팅 포함)가 즉시 로드되도록 합니다.
        myWebView.clearCache(true);

        // 배포 완료된 알파스냅 실시간 웹앱 URL 호출
        myWebView.loadUrl("https://ahtue.github.io/stock/");
    }

    // 안드로이드 물리 뒤로가기 버튼 처리
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack(); // 웹뷰 페이지 뒤로가기
        } else {
            if (System.currentTimeMillis() - backPressedTime < 2000) {
                if (backToast != null) {
                    backToast.cancel();
                }
                super.onBackPressed(); // 첫 페이지라면 앱 종료
            } else {
                backToast = Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
                backToast.show();
                backPressedTime = System.currentTimeMillis();
            }
        }
    }
}
