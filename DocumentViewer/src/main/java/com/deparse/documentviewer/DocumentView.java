package com.deparse.documentviewer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;

/**
 * @author MartinKent
 * @time 2018/1/11
 */
public class DocumentView extends FrameLayout implements TbsReaderView.ReaderCallback {
    public static final String FILE_PATH = "filePath";
    public static final String TEMP_PATH = "tempPath";

    private TextView mInfoView;
    private WebView mWebView;
    private TbsReaderView mTbsReaderView;
    private PinchImageView mPinchImageView;

    private Listener mListener;

    public DocumentView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public DocumentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DocumentView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mWebView = new WebView(context);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setJavaScriptEnabled(true);

        mTbsReaderView = new TbsReaderView(context, this);

        mInfoView = new TextView(context);
        mInfoView.setGravity(Gravity.CENTER);

        mPinchImageView = new PinchImageView(context);
    }

    public void view(String filePath) {
        view(filePath, null);
    }

    public void view(String filePath, String tmpPath) {
        DocumentHelper.view(getContext(), filePath, tmpPath, new Callback() {
            @Override
            public void showDoc(String filePath, String tempPath) {
                DocumentView.this.showDoc(filePath, tempPath);
                if (null != mListener) {
                    mListener.onShowFile(filePath, tempPath);
                }
            }

            @Override
            public void showImage(String filePath, boolean isUrl) {
                DocumentView.this.showImage(filePath, isUrl);
                if (null != mListener) {
                    mListener.onShowImage(filePath, isUrl);
                }
            }

            @Override
            public void showWeb(String url) {
                DocumentView.this.showWeb(url);
                if (null != mListener) {
                    mListener.onShowWeb(url);
                }
            }

            @Override
            public void showMsg(String msg) {
                DocumentView.this.showMsg(msg);
            }

            @Override
            public void showError(String msg) {
                DocumentView.this.showMsg(msg);
                if (null != mListener) {
                    mListener.onError(msg);
                }
            }
        });
    }

    public WebView getWebView() {
        return mWebView;
    }

    public TbsReaderView getReaderView() {
        return mTbsReaderView;
    }

    public ImageView getImageView() {
        return mPinchImageView;
    }

    public TextView getInfoView() {
        return mInfoView;
    }

    private void showWeb(String url) {
        removeAllViews();
        addView(mWebView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (url.toLowerCase().endsWith(".gif")) {
            mWebView.loadDataWithBaseURL(null, "<!DOCTYPE html><html><head><meta name=”x5-page-mode” content=”app”><meta name=”viewport” content=”initial-scale=1, maximum-scale=3, minimum-scale=1, user-scalable=yes” /><style> *{margin:0;padding:0;} html,body{text-align:center;height:100%;line-height:100%;} img{margin:auto;position:absolute;top:0;left:0;bottom:0;right:0;}</style></head><body><img id='image' src='" + url + "'></body></html>", "text/html", "utf-8", null);
        } else {
            mWebView.loadUrl(url);
        }
    }

    private void showDoc(String filePath, String tempPath) {
        removeAllViews();
        addView(mTbsReaderView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", tempPath);
        String ext = filePath.substring(filePath.lastIndexOf(".") + 1);
        boolean result = mTbsReaderView.preOpen(ext, false);
        if (!result) {
            showMsg("无法在移动端查看该文件\n" + filePath);
            return;
        }
        mTbsReaderView.openFile(bundle);
    }

    private void showImage(String imagePath, boolean isUrl) {
        removeAllViews();
        if (isUrl) {
            mPinchImageView.setImageURI(Uri.parse(imagePath));
        } else {
            mPinchImageView.setImageURI(Uri.fromFile(new File(imagePath)));
        }
        addView(mPinchImageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showMsg(String msg) {
        removeAllViews();
        addView(mInfoView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mInfoView.setText(msg);
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {
        System.out.println("onCallBackAction integer=" + integer + ", o=" + o + ", o1=" + o1);
        showMsg("文件预览失败");
    }

    public void onResume() {
        mWebView.onResume();
    }

    public void onPause() {
        mWebView.onPause();
    }

    public void onDestroy() {
        mWebView.destroy();
        mTbsReaderView.onStop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTbsReaderView.onSizeChanged(w, h);
    }

    interface Callback {
        void showDoc(String filePath, String tempPath);

        void showImage(String filePath, boolean isUrl);

        void showWeb(String url);

        void showMsg(String msg);

        void showError(String msg);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public interface Listener {

        void onShowFile(String filePath, String tempPath);

        void onShowImage(String filePath, boolean isUrl);

        void onShowWeb(String url);

        void onError(String msg);
    }
}
