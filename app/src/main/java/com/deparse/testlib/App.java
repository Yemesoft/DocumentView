package com.deparse.testlib;

import android.app.Application;

import com.deparse.documentviewer.DocumentHelper;

/**
 * @author MartinKent
 * @time 2018/1/10
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DocumentHelper.init(this);
    }
}
