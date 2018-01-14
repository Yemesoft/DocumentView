package com.deparse.documentviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author MartinKent
 * @time 2018/1/10
 */
public class DocumentViewerActivity extends AppCompatActivity {
    private DocumentView mDocumentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDocumentView = new DocumentView(this);
        setContentView(mDocumentView);
        mDocumentView.view(getIntent().getStringExtra(DocumentView.FILE_PATH), getIntent().getStringExtra(DocumentView.TEMP_PATH));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDocumentView.onResume();
    }

    @Override
    protected void onPause() {
        mDocumentView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mDocumentView.onDestroy();
        super.onDestroy();
    }
}
