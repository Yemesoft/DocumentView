package com.deparse.testlib;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.deparse.documentviewer.DocumentView;
import com.deparse.documentviewer.DocumentViewerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        setContentView(listView);
//        TbsVideo.openVideo();
        try {
            String[] files = getAssets().list("files");
            for (int i = 0; i < files.length; i++) {
                files[i] = "file:///android_asset/files/" + files[i];
            }
            List<String> paths = new ArrayList<>();
            Collections.addAll(paths, files);
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/创意视频_团结合作.mp4");
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/第三方应用 文件方案接口介绍.pdf");
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/#test - %2F副$本.pptx");
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/35258493e4d5ee97d827eb74fdab156b.gif");
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/test.html");
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/test.pptx");
            paths.add("https://raw.githubusercontent.com/MartinKent/DocumentView/master/app/src/main/assets/files/文件方案常见问题解答.docx");
            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, paths));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, DocumentViewerActivity.class);
                    intent.putExtra(DocumentView.FILE_PATH, listView.getAdapter().getItem(position).toString());
                    intent.putExtra(DocumentView.TEMP_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/test01/");
                    startActivity(intent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
