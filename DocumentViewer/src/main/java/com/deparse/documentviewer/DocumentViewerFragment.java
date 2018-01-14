package com.deparse.documentviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author MartinKent
 * @time 2018/1/12
 */
public class DocumentViewerFragment extends Fragment {
    private DocumentView mDocumentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDocumentView = new DocumentView(inflater.getContext());
        Bundle args = getArguments();
        mDocumentView.view(args.getString(DocumentView.FILE_PATH), args.getString(DocumentView.TEMP_PATH));
        return mDocumentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDocumentView.onResume();
    }

    @Override
    public void onStop() {
        mDocumentView.onPause();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mDocumentView.onDestroy();
        super.onDestroyView();
    }
}
